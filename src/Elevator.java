
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author villiam
 */
public class Elevator extends Thread {

    private int id;
    private PriorityBlockingQueue<Stop> pathUp;
    private PriorityBlockingQueue<Stop> pathDown;
    private PriorityBlockingQueue<Stop> currentPath;
    private Floor floor;
    private AtomicBoolean emergencyStopped;
    private Communicator com;
    private static final double WRONG_DIRECTION = 3.1;

    public Elevator(int elevatorID, Communicator com) {
        this.id = elevatorID;
        this.pathUp = new PriorityBlockingQueue<>();
        this.pathDown = new PriorityBlockingQueue<>();
        this.currentPath = pathUp;
        this.com = com;
        this.floor = new Floor();
        this.emergencyStopped = new AtomicBoolean(false);
    }

    private void add(Queue<Stop> q, Stop value) {
        if (!q.contains(value)) {
            q.add(value);
        }
    }

    private String queueToString() {
        StringBuilder sb = new StringBuilder();
        for (Stop s : currentPath) {
            sb.append(s.floor).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    public synchronized void addToPath(Stop stop) {
        float stopAsFloat = (float) stop.floor;
        System.out.println("Elevator " + id + " adding stop " + stop + " to path,  currenct direction is " + getDirection());

        if (floor.getCurrentFloorNumberAsFloat() < stopAsFloat) {
            add(pathUp, stop);
        } else {
            add(pathDown, stop.stopWithNegativeFloor());
        }

        System.out.println("Elevator " + id + " now has queue " + queueToString());
    }

    public Floor getFloor() {
        return floor;
    }

    private synchronized void switchDirectionPath() {
        if (currentPath == pathUp) {
            currentPath = pathDown;
        } else {
            currentPath = pathUp;
        }
    }

    private void stopAtFloor() throws InterruptedException {
        com.move(id, Const.DIRECTION_STOP);
        com.openDoor(id);
        Thread.sleep(1000);
        com.closeDoor(id);
        Thread.sleep(500);
    }

    private synchronized int getDirection() {
        if (pathUp.size() == 0 && pathDown.size() == 0)
            return Const.DIRECTION_STOP;
        else if (currentPath == pathUp)
            return Const.DIRECTION_UP;
        else
            return Const.DIRECTION_DOWN;

    }

    private synchronized int getNextFloor() {
        return Math.abs(currentPath.peek().floor);
    }

    // Just in case we just missed our destination
    // If it was added just before elevator past floor
    private synchronized void validateNextDestination() {
        int dest = Math.abs(currentPath.peek().floor);
        int direction = getDirection();
        float current = floor.getCurrentFloorNumberAsFloat();
        if (direction == Const.DIRECTION_UP && current >= dest)
            addToPath(currentPath.poll());
        else if (direction == Const.DIRECTION_DOWN && current <= dest)
            addToPath(currentPath.peek());
    }

    @Override
    public void run() {
        float diff;
        int currentFloor = 0, oldFloor = 0;
        int atFloor, outerCount = 0, sizeCount = 0, atFloorCount = 0, middleCount = 0;
        try {
            while (true) {
                while (pathUp.size() == 0 && pathDown.size() == 0) {
                    Thread.yield();
                }
                if (currentPath.size() > 0) {
                    com.move(id, getDirection());
                    do {
                        validateNextDestination();
                        synchronized (this) {
                            if (isEmergencyStopped()) {
                                com.move(id, Const.DIRECTION_STOP);
                                wait();
                                com.move(id, getDirection());
                            }
                        }
                        oldFloor = currentFloor;
                        currentFloor = floor.getCurrentFloorNumber();
                        // Update scale if we are at a new floor
                        if (oldFloor != currentFloor) {
                            com.setScale(id, currentFloor);
                        }
                        //Thread.yield();
                        diff = Math.abs(floor.getCurrentFloorNumberAsFloat() - getNextFloor());
                        atFloorCount = (atFloorCount + 1) % 10000000;
                        if (atFloorCount == 0)
                            System.out.println(id + " atFloor 1000 " + floor.getCurrentFloorNumberAsFloat() + " size of queues " + pathUp.size() + " " + pathDown.size() + " diff " + diff + " " + queueToString() + " nextfloor " + getNextFloor() + " direction " + getDirection());
                    } while (diff > 0.05);
                    currentPath.remove();
                    stopAtFloor();
                } else {
                    switchDirectionPath();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
            System.exit(0);
        }

    }

    /**
     * @return the id
     */
    public int getElevatorId() {
        return id;
    }

    public boolean isEmergencyStopped() {
        return emergencyStopped.get();
    }

    public synchronized void setEmergencyStopped(boolean value) {
        this.emergencyStopped.set(value);
        if (value == false) {
            notify();
        }
    }

    private int sumScore(Stop[] arr, float lowerLimit, float upperLimit) {
        int score = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].floor < upperLimit && arr[i].floor > lowerLimit) {
                score += 1;
            }
        }
        return score;
    }

    private static Stop[] deepCopyQueue(Queue<Stop> q) {
        return q.toArray(new Stop[q.size()]);
    }


    public float score(Command cmd) {

        float currFloorNumber = floor.getCurrentFloorNumberAsFloat();
        float score = 0;// Math.abs(currFloorNumber - (float) cmd.args[0]);
        System.out.println("Elevator " + id + " has command, to floor: " + cmd.args[0] + " ,direction: " + cmd.args[1]);
        //  System.out.println("Elevator " + id + " has initial score " + score);
        Stop[] upArr = deepCopyQueue(pathUp);
        Stop[] downArr = deepCopyQueue(pathDown);
        int savedDirection = getDirection();

        int directionToFloor;
        float position = floor.getCurrentFloorNumberAsFloat();

        if (position > cmd.args[0]) {
            directionToFloor = Const.DIRECTION_DOWN;
        } else {
            directionToFloor = Const.DIRECTION_UP;
        }
        System.out.println("Elevator " + id + " have direction " + savedDirection + " ,direction to floor " + directionToFloor);
        if (savedDirection == Const.DIRECTION_UP && upArr.length > 0) {
            if (savedDirection == directionToFloor) {
                if (cmd.args[1] == upArr[upArr.length - 1].nextDirection || upArr[upArr.length - 1].nextDirection == Const.NO_NEXT_DIRECTION) {
                    score += Math.abs(upArr[upArr.length - 1].floor - cmd.args[0]) + (0.5 * upArr.length);
                } else {
                    score += Math.abs(upArr[upArr.length - 1].floor - cmd.args[0]);
                    score += WRONG_DIRECTION;
                }
            } else {
                score += WRONG_DIRECTION;
                score += sumScore(downArr, cmd.args[0], position) + (downArr.length * 0.5);
                score += sumScore(upArr, position, upArr.length) + (upArr.length * 0.5);
                if (cmd.args[1] == Const.DIRECTION_UP) {
                    score += 1.5 * WRONG_DIRECTION;
                }
            }
        } else if (savedDirection == Const.DIRECTION_DOWN && downArr.length > 0) {
            if (savedDirection == directionToFloor) {
                if (cmd.args[1] == downArr[downArr.length - 1].nextDirection || downArr[downArr.length - 1].nextDirection == Const.NO_NEXT_DIRECTION) {
                    score += Math.abs(downArr[downArr.length - 1].floor - cmd.args[0]) + (0.5 * downArr.length);
                } else {
                    score += Math.abs(downArr[downArr.length - 1].floor - cmd.args[0]);
                    score += WRONG_DIRECTION;
                }
            } else {
                score += WRONG_DIRECTION;
                score += sumScore(downArr, 0, position) + (downArr.length * 0.5);
                score += sumScore(upArr, position, cmd.args[0]) + (upArr.length * 0.5);
                if (cmd.args[1] == Const.DIRECTION_DOWN) {
                    score += 1.5 * WRONG_DIRECTION;
                }
            }
        } else {
            score += Math.abs(position - cmd.args[0]);
        }

        System.out.println("Elevator " + id + " has score " + score);
        return score;
    }

}

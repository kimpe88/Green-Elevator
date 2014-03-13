
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author villiam
 */
public class Elevator extends Thread {

    private int id;
    private PriorityBlockingQueue<Stop> pathUp;
    private PriorityBlockingQueue<Stop> pathDown;
    // This is volatile because both main thread has to be able to get an always up to date current reference for scoring
    // and elevator thread switches it around between queues
    private volatile PriorityBlockingQueue<Stop> currentPath;
    private Floor floor;
    private boolean emergencyStopped;
    private Communicator com;
    private static final double WRONG_DIRECTION = 3.1;

    public Elevator(int elevatorID, Communicator com) {
        this.id = elevatorID;
        this.pathUp = new PriorityBlockingQueue<>();
        this.pathDown = new PriorityBlockingQueue<>();
        this.currentPath = pathUp;
        this.com = com;
        this.floor = new Floor();
        this.emergencyStopped = false;
    }

    private void add(Queue<Stop> q, Stop value) {
        if (!q.contains(value)) {
            q.add(value);
        }
    }

    public void addToPath(Stop stop) {
        System.out.println("Elevator " + id + " adding stop " + stop + " to path,  currenct direction is " + getDirection() + " at floor " + floor.getCurrentFloorNumberAsFloat() + " now");

        if (floor.getCurrentFloorNumber() < stop.floor) {
            add(pathUp, stop);
        } else {
            add(pathDown, stop.stopWithNegativeFloor());
        }
    }

    public Floor getFloor() {
        return floor;
    }

    private void switchDirectionPath() {
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

    private int getDirection() {
            if (pathUp.size() == 0 && pathDown.size() == 0)
                return Const.DIRECTION_STOP;
            else if (currentPath == pathUp)
                return Const.DIRECTION_UP;
            else
                return Const.DIRECTION_DOWN;
    }

    private int getNextFloor(PriorityBlockingQueue<Stop> q) {
        return Math.abs(q.peek().floor);
    }

    private int atFloor() {
        int ret = 0;
            if (currentPath.size() > 0) {
                int direction = getDirection();
                int next = getNextFloor(currentPath);
                float currPosition = floor.getCurrentFloorNumberAsFloat();
                // We somehow ended up with an invalid entry put it on other queue
                if (direction == Const.DIRECTION_DOWN && next > currPosition ) {
                    add(pathUp, currentPath.poll());
                    ret = -1;
                    System.out.println("Elevator " + id +" has entry in wrong queue, moving to other");
                } else if(direction == Const.DIRECTION_UP && next < currPosition){
                    add(pathDown, currentPath.poll());
                    ret = -1;
                    System.out.println("Elevator " + id +" has entry in wrong queue, moving to other");
                }else {
                    if(Math.abs(currPosition - next) < 0.05)
                        ret = 1;
                    else
                        ret = 0;
                }
            }
        return ret;
    }

    @Override
    public void run() {
        int currentFloor = 0, oldFloor = 0;
        int atFloor = 0;
        try {
            while (true) {
                while (pathUp.size() == 0 && pathDown.size() == 0) {
                    Thread.yield();
                }

                if (currentPath.size() > 0) {
                    com.move(id, getDirection());
                    do {
                        synchronized (this) {
                            if (emergencyStopped) {
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
                    } while (currentPath.size() > 0 && (atFloor = atFloor()) == 0);
                    if(atFloor == 1) {
                        currentPath.remove();
                        stopAtFloor();
                    }
                } else {
                    switchDirectionPath();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

    }

    /**
     * @return the id
     */
    public int getElevatorId() {
        return id;
    }


    public synchronized void changeEmergencyStopped() {
        emergencyStopped = !emergencyStopped;
        if (emergencyStopped == false) {
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

    private static Stop[] copyQueueToArray(Queue<Stop> q) {
        return q.toArray(new Stop[q.size()]);
    }


    public float score(Command cmd) {

        float score = 0;
        System.out.println("Elevator " + id + " has command, to floor: " + cmd.args[0] + " ,direction: " + cmd.args[1]);
        Stop[] upArr = copyQueueToArray(pathUp);
        Stop[] downArr = copyQueueToArray(pathDown);
        int savedDirection = getDirection();

        int directionToFloor;
        float position = floor.getCurrentFloorNumberAsFloat();

        if (position > cmd.args[0]) {
            directionToFloor = Const.DIRECTION_DOWN;
        } else {
            directionToFloor = Const.DIRECTION_UP;
        }
        System.out.println("Elevator " + id + " has direction " + savedDirection + " ,direction to floor " + directionToFloor);
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

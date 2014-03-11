
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author villiam
 */
public class Elevator extends Thread {

    private int id;
    private PriorityBlockingQueue<Integer> pathUp;
    private PriorityBlockingQueue<Integer> pathDown;
    private PriorityBlockingQueue<Integer> currentPath;
    private Floor floor;
    private AtomicBoolean emergencyStopped;
    private boolean firstTimeCheckingEmergency = true, emergencyReset = false;
    private Communicator com;
    private AtomicInteger direction;
    private static final int WRONG_DIRECTION = 3;

    public Elevator(int elevatorID, Communicator com) {
        this.id = elevatorID;
        this.pathUp = new PriorityBlockingQueue<>();
        this.pathDown = new PriorityBlockingQueue<>();
        this.direction = new AtomicInteger(Const.DIRECTION_STOP);
        this.currentPath = pathUp;
        this.com = com;
        this.floor = new Floor();
        this.emergencyStopped = new AtomicBoolean(false);
    }

    private void add(Queue<Integer> q, int value) {
        if (!q.contains(value)) {
            q.add(value);
        }
    }

    private String queueToString() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : currentPath) {
            sb.append(i).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    //TODO If button is pressed when halfway to a floor it gets added to other queue should incorporate half floors
    public void addToPath(int stop) {
        float stopAsFloat = (float) stop;
        System.out.println("Elevator " + id + " adding stop " + stop + " to path,  currenct direction is " + direction.get());
        if (direction.get() == Const.DIRECTION_UP) {
            if (floor.getCurrentFloorNumberAsFloat() < stopAsFloat) {
                add(pathUp, stop);
            } else {
                add(pathDown, -stop);
            }
        } else if (direction.get() == Const.DIRECTION_DOWN) {
            if (floor.getCurrentFloorNumberAsFloat() > stopAsFloat) {
                add(pathDown, -stop);
            } else {
                add(pathUp, stop);
            }
        } else {
            if (floor.getCurrentFloorNumberAsFloat() < stopAsFloat) {
                direction.set(Const.DIRECTION_UP);
                add(pathUp, stop);
            } else {
                direction.set(Const.DIRECTION_DOWN);
                add(pathDown, -stop);
            }
        }
        System.out.println("Elevator " + id + " now has queue " + queueToString());
    }

    public int getDirection() {
        return direction.get();
    }

    public Floor getFloor() {
        return floor;
    }

    private void switchDirectionPath() {
        if (currentPath == pathUp) {
            currentPath = pathDown;
            direction.set(Const.DIRECTION_DOWN);
        } else {
            currentPath = pathUp;
            direction.set(Const.DIRECTION_UP);
        }
    }

    private void stopAtFloor() throws InterruptedException {
        com.move(id, Const.DIRECTION_STOP);
        com.openDoor(id);
        Thread.sleep(1000);
        com.closeDoor(id);
        Thread.sleep(500);
        if (pathUp.size() == 0 && pathDown.size() == 0) {
            direction.set(Const.DIRECTION_STOP);
        }
    }

    private void calcAndSetDirection() {
        int nextFloor = getNextFloor();
        if (floor.getCurrentFloorNumber() < nextFloor) {
            direction.set(Const.DIRECTION_UP);
        } else {
            direction.set(Const.DIRECTION_DOWN);
        }
        System.out.println("Direction " + direction);
    }

    private int getNextFloor() {
        return Math.abs(currentPath.peek());
    }
    /*ToDo
     We assume that no elevator can pass a floor just as it is added to the list
     */

    @Override
    public void run() {
        int currentFloor = 0, oldFloor = 0;

        while (true) {
            try {
                while (pathUp.size() == 0 && pathDown.size() == 0);
                if (currentPath.size() > 0) {
                    calcAndSetDirection();
                    com.move(id, direction.get());
                    do {
                        synchronized (this) {
                            if (isEmergencyStopped()) {
                                com.move(id, Const.DIRECTION_STOP);
                                wait();
                                com.move(id, direction.get());
                            }
                        }
                        oldFloor = currentFloor;
                        currentFloor = floor.getCurrentFloorNumber();
                        // Update scale if we are at a new floor
                        if (oldFloor != currentFloor) {
                            com.setScale(id, currentFloor);
                        }
                    } while (!floor.atFloor(getNextFloor()));
                    // TODO check here so we haven't past the floor already?
                    currentPath.remove();
                    stopAtFloor();
                } else {
                    switchDirectionPath();
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
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

    private int sumScore(int[] arr, float lowerLimit, float upperLimit) {
        int score = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < upperLimit && arr[i] > lowerLimit) {
                score += 1;
                arr[i] = -1;
            }
        }
        return score;
    }

    private static int[] deepCopyQueue(Queue<Integer> q) {
        int[] arr = new int[q.size()];
        int i = 0;
        for (Integer val : q) {
            arr[i] = Math.abs(val);
            i++;
        }
        return arr;
    }

    public float score(Command cmd) {

        float currFloorNumber = floor.getCurrentFloorNumberAsFloat();
        float score = Math.abs(currFloorNumber - (float) cmd.args[0]);
        System.out.println("Elevator " + id + " has command, to floor: " + cmd.args[0] + " ,direction: " + cmd.args[1]);
        System.out.println("Elevator " + id + " has initial score " + score);
        int[] upArr = deepCopyQueue(pathUp);
        int[] downArr = deepCopyQueue(pathDown);
        int savedDirection = direction.get();

        int directionToFloor;
        float position = floor.getCurrentFloorNumberAsFloat();
        if (position > cmd.args[0]) {
            directionToFloor = Const.DIRECTION_DOWN;
        } else {
            directionToFloor = Const.DIRECTION_UP;
        }

        if (savedDirection == directionToFloor) {
            if (savedDirection == Const.DIRECTION_UP) {
                score += sumScore(upArr, position, cmd.args[0]);
                if (cmd.args[1] != Const.DIRECTION_UP) {
                    score += sumScore(upArr, cmd.args[0], Integer.MAX_VALUE) + WRONG_DIRECTION;
                }
            } else if (savedDirection == Const.DIRECTION_DOWN) {
                score += sumScore(downArr, cmd.args[0], position);
                if (cmd.args[1] != Const.DIRECTION_DOWN) {
                    score += sumScore(downArr, 0, cmd.args[0]) + WRONG_DIRECTION;
                }
            }
        } else {
            if (savedDirection == Const.DIRECTION_UP) {
                score += sumScore(downArr, 0, position);
                score += sumScore(upArr, 0, cmd.args[0]);
                if (cmd.args[1] != Const.DIRECTION_UP) {
                    score += sumScore(upArr, cmd.args[0], Integer.MAX_VALUE) + WRONG_DIRECTION;
                }
            } else if (savedDirection == Const.DIRECTION_DOWN) {
                score += sumScore(upArr, position, Integer.MAX_VALUE);
                score += sumScore(downArr, cmd.args[0], Integer.MAX_VALUE);

                if (cmd.args[1] != Const.DIRECTION_DOWN) {
                    score += sumScore(downArr, 0, cmd.args[0]) + WRONG_DIRECTION;
                }
            }
        }
        System.out.println("Elevator " + id + " has score " + score);
        return score;
    }

    public float score2(Command cmd) {

        float currFloorNumber = floor.getCurrentFloorNumberAsFloat();
        float score = 0;// Math.abs(currFloorNumber - (float) cmd.args[0]);
        System.out.println("Elevator " + id + " has command, to floor: " + cmd.args[0] + " ,direction: " + cmd.args[1]);
      //  System.out.println("Elevator " + id + " has initial score " + score);
        int[] upArr = deepCopyQueue(pathUp);
        int[] downArr = deepCopyQueue(pathDown);
        int savedDirection = direction.get();

        int directionToFloor;
        float position = floor.getCurrentFloorNumberAsFloat();
        if (position > cmd.args[0]) {
            directionToFloor = Const.DIRECTION_DOWN;
        } else {
            directionToFloor = Const.DIRECTION_UP;
        }

        if (savedDirection == directionToFloor) {
            if (savedDirection == Const.DIRECTION_UP) {
                if (upArr.length != 0) {
                    score += Math.abs(upArr[upArr.length - 1] - cmd.args[0]) + (0.5 * upArr.length);
                    if (cmd.args[1] != Const.DIRECTION_UP) {
                        score += WRONG_DIRECTION;
                    }
                } else {
                    score += Math.abs(position - cmd.args[0]);
                }
            } else if (savedDirection == Const.DIRECTION_DOWN) {
                if (upArr.length != 0) {
                    score += Math.abs(downArr[downArr.length - 1] - cmd.args[0]) + (0.5 * downArr.length);
                    if (cmd.args[1] != Const.DIRECTION_DOWN) {
                        score += WRONG_DIRECTION;
                    }
                } else {
                    score += Math.abs(position - cmd.args[0]);
                }
            }
        } else {
            if(downArr.length == 0 && upArr.length == 0) {
               score += Math.abs(position - cmd.args[0]);
            }
            else if (savedDirection == Const.DIRECTION_UP) {
                score += sumScore(downArr, cmd.args[0], position) * 0.5;
                score += sumScore(upArr, position, upArr.length) * 0.5;
                if (cmd.args[1] == Const.DIRECTION_UP) {
                    score += WRONG_DIRECTION;
                }
            } else if (savedDirection == Const.DIRECTION_DOWN) {
                score += sumScore(downArr, 0, position) * 0.5;
                score += sumScore(upArr, position, cmd.args[0]) * 0.5;
                if (cmd.args[1] == Const.DIRECTION_DOWN) {
                    score += WRONG_DIRECTION;
                }
            }
        }
        System.out.println("Elevator " + id + " has score " + score);
        return score;
    }

}

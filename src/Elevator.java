
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private int id, direction;
    private PriorityBlockingQueue<Integer> pathUp;
    private PriorityBlockingQueue<Integer> pathDown;
    private PriorityBlockingQueue<Integer> currentPath;
    private Floor floor;
    private AtomicBoolean emergencyStopped;
    private boolean firstTimeCheckingEmergency = true,emergencyReset = false;
    private Communicator com;


    public Elevator(int elevatorID,Communicator com) {
        this.id = elevatorID;
        this.pathUp = new PriorityBlockingQueue<>();
        this.pathDown = new PriorityBlockingQueue<>();
        this.direction = Const.DIRECTION_STOP;
        this.currentPath = pathUp;
        this.com = com;
        this.floor = new Floor();
        this.emergencyStopped = new AtomicBoolean(false);
    }

    private void add(Queue<Integer> q, int value)  {
        if(!q.contains(value))
            q.add(value);
    }
    private void printQueue() {
        System.out.print("Printing queue");
        for (Integer i : currentPath)
            System.out.print(i + " ");
        System.out.println();
    }

    //TODO If button is pressed when halfway to a floor it gets added to other queue should incorporate half floors
    public void addToPath(int stop) {
        float stopAsFloat = (float) stop;
        System.out.println("Adding stop to path " + stop + " currenct direction " + direction );
        System.out.println("Adding stop to path " + " currenct direction " + direction );
        if (stop == Const.EMERGENCY_STOP) {
            System.out.println("EMERGENCY STOP PRESSED");
            Thread.currentThread().interrupt();

        } else if (direction == Const.DIRECTION_UP) {
            if (floor.getCurrentFloorNumberAsFloat()< stopAsFloat) {
                add(pathUp,stop);
            } else {
                add(pathDown,-stop);
            }
        } else if (direction == Const.DIRECTION_DOWN) {
            if (floor.getCurrentFloorNumberAsFloat() > stopAsFloat) {
                add(pathDown,-stop);
            } else {
                add(pathUp, stop);
            }
        } else {
            if (floor.getCurrentFloorNumberAsFloat() < stopAsFloat) {
                direction = Const.DIRECTION_UP;
                add(pathUp, stop);
            } else {
                direction = Const.DIRECTION_DOWN;
                add(pathDown,-stop);
            }
        }
        printQueue();
    }

    public int getDirection() {
        return direction;
    }

    public Floor getFloor() {
        return floor;
    }

    private void switchDirectionPath() {
        if(currentPath == pathUp) {
            currentPath = pathDown;
            direction = Const.DIRECTION_DOWN;
        } else {
            currentPath = pathUp;
            direction = Const.DIRECTION_UP;
        }
    }

    private void stopAtFloor() throws InterruptedException{
        com.move(id,Const.DIRECTION_STOP);
        com.openDoor(id);
        Thread.sleep(1000);
        com.closeDoor(id);
        Thread.sleep(500);
        direction = Const.DIRECTION_STOP;
    }
    private void calcAndSetDirection() {
        int nextFloor = getNextFloor();
        if(floor.getCurrentFloorNumber() < nextFloor)
            direction = Const.DIRECTION_UP;
        else
            direction = Const.DIRECTION_DOWN;
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
        int currentFloor = 0,oldFloor = 0;

        while (true) {
            try {
                while(pathUp.size() == 0 && pathDown.size() == 0);
                if (currentPath.size() > 0) {
                    calcAndSetDirection();
                    com.move(id, direction);
                    do {
                        while (isEmergencyStopped()) {
                            if(firstTimeCheckingEmergency){
                                com.move(id, Const.DIRECTION_STOP);
                                firstTimeCheckingEmergency = false;
                            }
                            Thread.yield();
                        }
                        if(emergencyReset) {
                            com.move(id, direction);
                            emergencyReset = false;
                        }

                        oldFloor = currentFloor;
                        currentFloor = floor.getCurrentFloorNumber();
                        // Update scale if we are at a new floor
                        if (oldFloor != currentFloor)
                            com.setScale(id, currentFloor);
                    }while(!floor.atFloor(getNextFloor()));
                    // TODO check here so we haven't past the floor already?
                    currentPath.remove();
                    stopAtFloor();
                } else
                    switchDirectionPath();
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

    public void setEmergencyStopped(boolean value) {
        if(!this.emergencyStopped.get())
            emergencyReset = true;
        this.emergencyStopped.set(value);
    }
}

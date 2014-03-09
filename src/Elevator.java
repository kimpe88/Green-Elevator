
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author villiam
 */
public class Elevator implements Runnable {

    public static final int UP = 1, DOWN = -1, IDLE = 0, DOOR_OPEN = 1, DOOR_CLOSED = -1, DOOR_STOP = 0;
    private int id, direction;
    private PriorityBlockingQueue<Integer> pathUp;
    private PriorityBlockingQueue<Integer> pathDown;
    private PriorityBlockingQueue<Integer> currentPath;
    private Floor floor;
    private Communicator com;


    public Elevator(int elevatorID,Communicator com) {
        this.id = elevatorID;
        this.pathUp = new PriorityBlockingQueue<>();
        this.pathDown = new PriorityBlockingQueue<>();
        this.direction = IDLE;
        this.currentPath = pathUp;
        this.com = com;
        this.floor = new Floor();
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
    public void addToPath(int stop) {
        System.out.println("Adding stop to path " + stop + " currenct direction " + direction );
        if (direction == UP) {
            if (floor.getCurrentFloorNumber() > stop) {
                add(pathDown,-stop);
            } else {
                add(pathUp,stop);
            }
        } else if (direction == DOWN) {
            if (floor.getCurrentFloorNumber() > stop) {
                add(pathUp, stop);
            } else {
                add(pathDown,-stop);
            }
        } else {
            if (floor.getCurrentFloorNumber() < stop) {
                direction = UP;
                add(pathUp, stop);
            } else {
                direction = DOWN;
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
            direction = DOWN;
        } else {
            currentPath = pathUp;
            direction = UP;
        }
    }
    
    private void stopAtFloor() throws InterruptedException{
        com.move(id,Communicator.DIRECTION_STOP);
        com.openDoor(id);
        Thread.sleep(1000);
        com.closeDoor(id);
        Thread.sleep(500);
        direction = IDLE;
    }
    private void calcAndSetDirection() {
        int nextFloor = getNextFloor();
        if(floor.getCurrentFloorNumber() < nextFloor)
            direction = UP;
        else
            direction = DOWN;
        System.out.println("Direction " + direction);
    }
    
    private int getNextFloor() {
        return Math.abs(currentPath.peek());
    }
    /*ToDo
       We assume that no elevator can pass a florr just as it is added to the list
    */
    @Override
    public void run() {
        try {
            while (true) {
                while(pathUp.size() == 0 && pathDown.size() == 0);
                if (currentPath.size() > 0) {
                    calcAndSetDirection();
                    com.move(id, direction);
                    while(!floor.atFloor(getNextFloor()));
                    // TODO check herre so we havent past the floor already?
                    currentPath.remove();
                    stopAtFloor();
                } else 
                    switchDirectionPath();
               
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}

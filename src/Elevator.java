
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
    private PriorityBlockingQueue<Integer> pathUp = new PriorityBlockingQueue<>();
    private PriorityBlockingQueue<Integer> pathDown = new PriorityBlockingQueue<>();
    private PriorityBlockingQueue<Integer> currentPath;
    private Floor floor = new Floor();
   


    public Elevator(int elevatorID) {
        this.id = elevatorID;
        this.direction = IDLE;
        this.currentPath = pathUp;
    }

    private void add(Queue<Integer> q, int value)  {
        if(!q.contains(value))
            q.add(value);
    }
    public void addToPath(int stop) {
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
        }
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
    /*ToDo
       We assume that no elevator can pass a florr just as it is added to the list
    */
    @Override
    public void run() {
        while (true) {
            while(pathUp.size() == 0 && pathDown.size() == 0);
            if (currentPath.size() > 0) {
                while(!floor.atFloor(currentPath.peek()));
                
                    
            } else 
                switchDirectionPath();
           
        }
    }
    private void stop(){
        pr.
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}

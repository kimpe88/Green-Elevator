
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author villiam
 */
public class Elevator implements Runnable{
    
    
    
    private int id,direction,doorStatus,floor;
    double velocity;
    private Queue<Command> commands = new ConcurrentLinkedQueue<Command>();
    public Elevator(int elevatorID){
        this.id = elevatorID;
    }
    
    public void addCommand(Command cmd) {
        this.commands.add(cmd);
    }
    
    public int getDirection(){
        return direction;
    }
    
    public int getDoorStatus(){
        return doorStatus;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public double getVelocity(){
        return velocity;
    }
    
    @Override
    public void run() {
        Command cmd;
        while(true) {
            while(commands.size() > 0) {
                cmd = commands.poll();
                System.out.println(id + " received command " + cmd.command.toString());
                
            }
        }
    }
    public void buttonPressed() {
        
    }
    public void printCmd(int id,String[] cmdSplit){
        System.out.print("Elevator " + id + " has received commannd ");
        for(String s : cmdSplit)
            System.out.print(s + " ");
        System.out.println("");
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
}

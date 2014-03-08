
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kim
 */
public class Communicator {
    
    public static final int DIRECTION_UP = 1, DIRECTION_DOWN = -1, DIRECTION_STOP = 0;
    
    Socket s;
    BufferedReader br;
    PrintWriter pw;
    public Communicator(String ip, int port) throws IOException{
        this.s = new Socket(ip,port);
        this.s.setTcpNoDelay(false);
        this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.pw = new PrintWriter(s.getOutputStream());
    }
    
    public synchronized BufferedReader getBufferedReader(){
        return this.br;
    }
    public synchronized void move(int elevatorId, int direction){
        pw.println("m " + elevatorId + " " + direction);
        pw.flush();
    }
    public synchronized void openDoor(int elevatorId){
        pw.println("d " + elevatorId + " " + 1);
        pw.flush();
    }
    public synchronized void closeDoor(int elevatorId){
        pw.println("d " + elevatorId + " " + -1);
        pw.flush();
    }
    public synchronized void setScale(int elevatorId,int scale){
        pw.println("s "  + elevatorId + " " + scale);
        pw.flush();
    }
    public synchronized void quit(){
        pw.println("q");
        pw.flush();
    }
    public synchronized void printCommand(String s){
        pw.println(s);
        pw.flush();
    }
    
}


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author villiam
 */
public class Controller {

    private static ConcurrentLinkedQueue<ResponseObject> response = new ConcurrentLinkedQueue<ResponseObject>();
    private Elevator[] elevators;
    private AlgorithmI alg;
    Communicator communicator;
    
    public Controller(AlgorithmI alg) {
        this.alg = alg;
        
    }

    public void runElevator(String hostName, int port, int numElevators) {
        elevators = new Elevator[numElevators];

        try {
            communicator = new Communicator(hostName,port);
            for (int i = 0; i < numElevators; i++) {
                elevators[i] = new Elevator(i, communicator);
                new Thread(elevators[i]).start();
            }
            Reader reader = new Reader(communicator);
            reader.start();
    
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }


    public void buttonPressed(Command cmd) throws IOException {
        int bestScore = Integer.MAX_VALUE;
        int bestId=0, curr;
        for (int i = 0; i < elevators.length; i++) {
            curr = alg.score(elevators[i], cmd);
            if ( curr < bestScore) {
                bestScore = curr;
                bestId = i;
            }
        }
        
        elevators[bestId].addToPath(cmd.args[0]);
    }

    public static void main(String[] args) {
        String hostName = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4711;
        int numElevators = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        Controller c = new Controller(new SSTAlgorithm());
        c.runElevator(hostName, port, numElevators);

    }

    private class Reader extends Thread {
        
        private Communicator communicator;

        public Reader(Communicator c) {
            this.communicator = c;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = communicator.getBufferedReader();
                String msg;
                Command cmd;
                System.out.println("Reader started");
                while ((msg = br.readLine()) != null) {
                    System.out.println(msg);
                    cmd = new Command(msg);
                    switch(cmd.command){
                        case f:
                            elevators[cmd.args[0] - 1].getFloor().setPosition(cmd.position);
                            break;
                        case b:
                            buttonPressed(cmd);
                            break;
                    }
                }
                System.out.println("Reader finished");
                
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}

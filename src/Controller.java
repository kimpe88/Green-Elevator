
import java.io.BufferedReader;
import java.io.IOException;
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
            for (int i = 1; i < numElevators; i++) {
                elevators[i] = new Elevator(i, communicator);
                elevators[i].start();
            }
            Reader reader = new Reader(communicator);
            reader.start();
    
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    private void floorButtonPressed(Command cmd) {
        if(cmd.command == Command.Commands.p && cmd.args[1] == 32000) {

            if (elevators[cmd.args[0]].isEmergencyStopped())
                elevators[cmd.args[0]].setEmergencyStopped(false);
            else
                elevators[cmd.args[0]].setEmergencyStopped(true);
        } else
            elevators[cmd.args[0]].addToPath(cmd.args[1]);
    }
    private void callElevatorButtonPressed(Command cmd) throws IOException {
        float bestScore = Integer.MAX_VALUE;
        int bestId=0;
        float curr;
        for (int i = 1; i < elevators.length; i++) {
            curr = elevators[i].score(cmd);
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
        int numElevators = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        numElevators++;
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
                    cmd = new Command(msg);
                    switch(cmd.command){
                        case f:
                            elevators[cmd.args[0]].getFloor().setPosition(cmd.position);
                            break;
                        case b:
                            callElevatorButtonPressed(cmd);
                            break;
                        case p:
                            floorButtonPressed(cmd);
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

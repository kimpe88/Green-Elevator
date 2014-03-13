
import java.io.BufferedReader;
import java.io.IOException;
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

    private Elevator[] elevators;
    Communicator communicator;

    public void runElevator(String hostName, int port, int numElevators) {
        elevators = new Elevator[numElevators];

        try {
            communicator = new Communicator(hostName,port);
            for (int i = 1; i < numElevators; i++) {
                elevators[i] = new Elevator(i, communicator);
                elevators[i].start();
            }
            BufferedReader br = communicator.getBufferedReader();
            String msg;
            Command cmd;
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
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
             communicator.close();
        }
    }

    private void floorButtonPressed(Command cmd) {
        if(cmd.command == Command.Commands.p && cmd.args[1] == 32000) {
            elevators[cmd.args[0]].changeEmergencyStopped();
        } else{
            System.out.println(cmd.toString());
            Stop s = new Stop(cmd.args[1]);
            elevators[cmd.args[0]].addToPath(s);
        }
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
        Stop s = new Stop(cmd.args[0],cmd.args[1]);
        elevators[bestId].addToPath(s);
    }


    public static void main(String[] args) {
        String hostName = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4711;
        int numElevators = args.length > 2 ? Integer.parseInt(args[2]) : 5;
        numElevators++;
        Controller c = new Controller();
        c.runElevator(hostName, port, numElevators);
        System.exit(0);

    }
}

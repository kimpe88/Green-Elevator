
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
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

    private static ConcurrentLinkedQueue<ResponseObject> rq = new ConcurrentLinkedQueue<ResponseObject>();
    private static ConcurrentLinkedQueue<Command> requests = new ConcurrentLinkedQueue<Command>();
    private Elevator[] elevators;
    private AlgorithmI alg;

    public Controller(AlgorithmI alg) {
        this.alg = alg;
    }

    public void runElevator(String hostName, int port, int numElevators) {
        elevators = new Elevator[numElevators];
        for (int i = 0; i < numElevators; i++) {
            elevators[i] = new Elevator(i);
            new Thread(elevators[i]).start();
        }
        Socket s = null;
        try {
            s = new Socket(hostName, port);
            String cmdStr;
            Command cmd;
            Reader r = new Reader(s);
            r.start();
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            while (true) {
                if (requests.size() > 0) {
                    cmd = requests.poll();
                    switch (cmd.command) {
                        case b:
                            buttonPressed(pw, cmd);
                            break;
                    }
                }

            }
//            pw.println("m 0 1");
//            pw.flush();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private Command findCommand(Command.Commands toFind){
        boolean found = false;
        Iterator<Command> it;
        Command ret =null,tmp;
        while(!found){
            it = requests.iterator();
            while(it.hasNext())
            {
                tmp = it.next();
                if(tmp.command == toFind){
                    ret = tmp;
                    found = true;
                    it.remove();
                    break;
                }
            }
        }
        return ret;
    }

    public void buttonPressed(PrintWriter pw, Command cmd) throws IOException {
        pw.println(Command.Commands.v.toString());
        pw.flush();
        Command v = findCommand(Command.Commands.v);
        for (int i = 0; i < elevators.length; i++) {
            pw.println(Command.Commands.w.toString() + " " + i);
            pw.flush();
            elevators[i].addCommand(v);
            elevators[i].addCommand(findCommand(Command.Commands.w));
        }
    }

    public static void addResponse(ResponseObject ro) {
        rq.add(ro);
    }

    public static void main(String[] args) {
        String hostName = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4711;
        int numElevators = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        Controller c = new Controller(new SSTAlgorithm());
        c.runElevator(hostName, port, numElevators);

    }

    private class Reader extends Thread {

        private Socket s;

        public Reader(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String msg;
                System.out.println("Reader started");
                while ((msg = br.readLine()) != null) {
                    requests.add(new Command(msg));
                }
                System.out.println("Reader finished");
                
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}

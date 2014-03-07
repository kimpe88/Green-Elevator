/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author villiam
 */
public class Command {

    public static enum Commands {
        b, p, q, m, d,
        s, w, v, UNKNOWN
    };
    public final Commands command;
    public final double velocity;
    public final int[] args;

    public Command(String cmd) {
        String[] cmdSplit = cmd.split(" ");
        switch (cmdSplit[0]) {
            case "b":
                this.command = Commands.b;
                args = new int[2];
                args[0] = Integer.parseInt(cmdSplit[1]);
                args[1] = Integer.parseInt(cmdSplit[2]);
                velocity = -1;
                break;
            case "m":
                this.command = Commands.m;
                args = new int[2];
                args[0] = Integer.parseInt(cmdSplit[1]);
                args[1] = Integer.parseInt(cmdSplit[2]);
                velocity = -1;
                break;
            case "d":
                this.command = Commands.d;
                args = new int[2];
                args[0] = Integer.parseInt(cmdSplit[1]);
                args[1] = Integer.parseInt(cmdSplit[2]);
                velocity = -1;
                break;
            case "s":
                this.command = Commands.s;
                args = new int[2];
                args[0] = Integer.parseInt(cmdSplit[1]);
                args[1] = Integer.parseInt(cmdSplit[2]);
                velocity = -1;
                break;
            case "f":
            case "w":
                this.command = Commands.w;
                args = new int[1];
                args[0] = Integer.parseInt(cmdSplit[1]);
                velocity = -1;
                break;
            case "v":
                this.command = Commands.v;
                velocity = Double.parseDouble(cmdSplit[1]);
                args = null;
                break;
            case "p":
                this.command = Commands.p;
                args = new int[2];
                args[0] = Integer.parseInt(cmdSplit[1]);
                args[1] = Integer.parseInt(cmdSplit[2]);
                velocity = -1;
                break;
            default:
                this.command = Commands.UNKNOWN;
                velocity = -1;
                args = null;
                break;
        }
    }

}

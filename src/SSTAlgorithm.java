/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author villiam
 */
public class SSTAlgorithm implements AlgorithmI{

    @Override
    public int score(Command to, Command where) {
        return Math.abs(where.args[0] - to.args[0]);
    }
    
}

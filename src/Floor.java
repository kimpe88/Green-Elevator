/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kim
 */
public class Floor {
    private AtomicFloat position = new AtomicFloat();
    
    public void setPosition(float pos) {
        position.set(pos);
    }
    
    public int getCurrentFloorNumber() {
        return Math.round(position.get());
    }
    
    public float getCurrentFloorNumberAsFloat(){
        return position.get();
    }

}

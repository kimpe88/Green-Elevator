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


    public int atFloor(int floorNumber) {
        float diff = Math.abs(position.get() - floorNumber);

        if(diff > 0.05 && diff < 0.09)
            return 0;
        else if ( diff < 0.05)
            return  1;
        else
            return -1;
    }
    
}


import java.util.concurrent.atomic.AtomicInteger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kim
 */
import static java.lang.Float.*;
public class AtomicFloat {
    private AtomicInteger bits;
    
    public AtomicFloat(){
        this.bits = new AtomicInteger(floatToIntBits(0f));
    }
    
    public AtomicFloat(float value){
        this.bits = new AtomicInteger(floatToIntBits(value));
    }

    public final void set(float newValue) {
        bits.set(floatToIntBits(newValue));
    }

    public final float get() {
        return intBitsToFloat(bits.get());
    }    
    
    
}

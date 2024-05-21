package ec.support.awt;

import sun.awt.AWTFinalizeable;
import sun.awt.AWTFinalizer;

public class AWTFinalizeHack
{
    public static void warmup () {
        Object object = new Object();
        synchronized(object) {
            new SecretAWTFinalizeHack(object);
            try {
                object.wait();
            } catch (Exception e) {
            }
        }
    }   
}   

class SecretAWTFinalizeHack implements AWTFinalizeable
{
    private Object object;
    private AWTFinalizeable next = null;
    
    SecretAWTFinalizeHack(Object object) {
        this.object = object;
        AWTFinalizer.addFinalizeable(this);
    }   
    
    public void setNextFinalizeable (AWTFinalizeable next) {
        this.next = next;
    }
    
    public AWTFinalizeable getNextFinalizeable () {
        return next;
    }   
    
    public void doFinalization() {
        synchronized(object) {
            try {
                object.notify();
            } catch (Exception e) {
                System.out.println("AWTFinalizeHack: Exception notifying object");
                e.printStackTrace();
            }
        }
    }   
}   

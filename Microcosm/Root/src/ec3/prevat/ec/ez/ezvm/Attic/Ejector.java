package ec.ez.ezvm;

import ec.ez.runtime.Ejection;


/**
 * An ejector works with a EscapeExpr or a LoopExpr to cause an escape
 * to take place or a loop to terminate.
 */

public class Ejector {

    private Object myResult;
    private Ejection myEjection = null;

    public Ejector() {}

    public void run(Object result) throws Ejection {
        myResult = result;
        myEjection = new Ejection();
        throw myEjection;
    }

    public Object run() throws Ejection {
        myResult = null;
        myEjection = new Ejection();
        throw myEjection;

    }

    /*package*/ Object result(Ejection ej) throws Ejection {
        if (myEjection == ej && ej != null) {
            return myResult;
        } else {
            throw ej;
        }
    }

    /*package*/ void disable() {
        myEjection = null;
        myResult = null; //just to help gc
    }
}


package ec.ez.ezvm;

/**
 * How a non-EZObject is made accessible to EZ
 */
public class EZWrapper implements EZObject {

    private Object myWrapped;

    private EZWrapper(Object wrapped) {
        myWrapped = wrapped;
    }

    static public EZObject make(Object wrapped) {
        if (wrapped instanceof EZObject) {
            return (EZObject)wrapped;
        } else {
            return new EZWrapper(wrapped);
        }
    }

    public Object wrapped() { return myWrapped; }

    public EZObject apply(String verb, EZObject[] args) throws Exception, Ejection {
        throw new RuntimeException("NotYetImplemented, waiting for CRAPI");
    }

    public EZObject applyLater(String verb, EZObject[] args) {
        throw new RuntimeException("NotYetImplemented");
    }
}


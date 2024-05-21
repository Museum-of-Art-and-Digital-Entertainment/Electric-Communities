package ec.ez.runtime;
import java.lang.reflect.*;

/**
 * How a non-EZObject is made accessible to EZ
 */
public class EZWrapper extends EZBaseClass {

    private Object myWrapped;
    private Class itsClass;

    EZWrapper(Object wrapped) {
        myWrapped = wrapped;
        if(myWrapped != null)
            itsClass = myWrapped.getClass();
         else itsClass = null;
   }

    static public EZObject make(Object wrapped) {
        if (wrapped instanceof EZObject) {
            return (EZObject)wrapped;
        } else
        if (wrapped instanceof Boolean) {
            return (EZBoolean.convert(((Boolean) wrapped).booleanValue()));
        } else
        if (wrapped instanceof Long) {
            return (new EZLong(((Long) wrapped).longValue()));
        } else
        if (wrapped instanceof Double) {
            return (new EZDouble(((Double) wrapped).doubleValue()));
        } else
        {
            return new EZWrapper(wrapped);
        }
    }

    public Object wrapped() { return myWrapped; }

    public Object target() {
        return(myWrapped);
    }

    public Object  coerceTo(Class targetType) throws Exception {
        return(myWrapped);
    }

}


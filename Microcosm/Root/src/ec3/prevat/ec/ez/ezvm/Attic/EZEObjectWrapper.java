package ec.ez.ezvm;
import ec.e.run.*;

/**
 * Objects that handle EZ messages themselves.
 */
public class EZEObjectWrapper extends EObject_$_Impl {
    Object wrapped;

    public EZEObjectWrapper(Object wrappee) {
        wrapped = wrappee;
    }

    public Object value() {
        return wrapped;
    }
}

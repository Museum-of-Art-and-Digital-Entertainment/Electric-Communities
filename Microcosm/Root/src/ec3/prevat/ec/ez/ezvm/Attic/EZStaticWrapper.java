package ec.ez.ezvm;

/**
 * How a Java class's static methods are made accessible to EZ
 */
public class EZStaticWrapper implements EZObject {

    private Class myClass;

    public EZStaticWrapper(Class clazz) {
        myClass = clazz;
    }

    public Class clazz() { return myClass; }

    public EZObject apply(String verb, EZObject[] args) throws Exception {
        throw new RuntimeException("NotYetImplemented, waiting for 1.1's CRAPI");
    }

    public EZObject applyLater(String verb, EZObject[] args) {
        throw new RuntimeException("NotYetImplemented");
    }
}


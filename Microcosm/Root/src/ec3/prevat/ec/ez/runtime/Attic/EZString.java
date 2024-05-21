package ec.ez.runtime;

/**
 * BNF: literal <p>
 *
 * Returns the literal value
 */
public class EZString implements EZObject {

    private String myValue;

    public EZString(String value) {
    myValue = value;
    }

    public EZObject apply(String verb, EZObject[] args) throws Exception, Ejection {
        return(null);
    }

    public EZObject applyLater(String verb, EZObject[] args) {
        return(null);
    }

    public Object  coerceTo(Class targetType) throws Exception {
        return(this);
    }

    public void print() {
        System.out.println(myValue);
    }
}


package ec.ez.prim;

/**
 * EZBoolean - an EZ Sugaring of java.lang.Boolean
 *
 */
public class EZBoolean {

    /** work around ecomp's ignornace of '.class' */
    static public final Class TYPE = new EZBoolean().getClass();
    private EZBoolean() {}
    static public final Class OTHER_TYPE = Boolean.TRUE.getClass();


    static public boolean not(boolean rec) {
        return ! rec;
    }

    static public boolean and(boolean rec, boolean arg) {
        return rec && arg;
    }

    static public boolean or(boolean rec, boolean arg) {
        return rec || arg;
    }

    static public boolean xor(boolean rec, boolean arg) {
        return rec ^ arg;
    }

    static public Object pick(boolean rec, 
                              Object trueChoice, 
                              Object falseChoice) {
        if (rec) {
            return(trueChoice);
        } else {
            return(falseChoice);
        }
    }
}


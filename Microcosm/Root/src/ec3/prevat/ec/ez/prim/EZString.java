package ec.ez.prim;

/**
 * EZString - an EZ Sugaring of java.lang.String
 *
 */
public class EZString {

    /** work around ecomp's ignornace of '.class' */
    static public final Class TYPE = new EZString().getClass();
    private EZString() {}
    static public final Class OTHER_TYPE = "".getClass();

    static public String add(String rec, String arg) {
        return new String(rec + arg);
    }
}

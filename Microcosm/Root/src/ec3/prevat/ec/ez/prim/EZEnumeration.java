package ec.ez.prim;

import ec.ez.runtime.Ejection;
import java.util.Enumeration;
import ec.ez.collect.AssociationEnumeration;

public class EZEnumeration {

    /** work around ecomp's ignornace of '.class' */
    static public final Class TYPE = new EZEnumeration().getClass();
    private EZEnumeration() {}
    static public final Class OTHER_TYPE = otherType();

    static private Class otherType() {
        try {
            return Class.forName("java.util.Enumeration");
        } catch (Exception ex) {
            throw new Error("internal in initialization " + ex);
        }
    }


    static public Enumeration asEnumeration(Enumeration iter) {
        return iter;
    }

    static public void each(AssociationEnumeration iter, Object closure)
         throws Exception, Ejection {

        while (iter.hasMoreElements()) {
            Object thisValue = iter.nextElement();
            Object[] args = {iter.currentKey(), thisValue };
            EZUniversal.perform(closure, "run", args);
        }
    }

}

package ec.ez.collect;
import ec.ez.ezvm.EZUniversal;
import ec.ez.ezvm.Ejection;
import java.util.Enumeration;


public class EZEnumeration {

    static public Enumeration asEnumeration(Enumeration iter) {
        return iter;
    }

    static public void each(Enumeration iter, Object closure) throws Exception, Ejection {
        while (iter.hasMoreElements()) {

            Object[] args = { iter.nextElement() };
            EZUniversal.perform(closure, "of", args);
        }
    }
}
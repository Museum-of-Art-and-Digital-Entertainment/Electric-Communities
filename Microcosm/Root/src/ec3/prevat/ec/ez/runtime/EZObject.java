package ec.ez.runtime;

/**
 * Objects that handle EZ messages themselves.
 */
public interface EZObject {

    public Object perform(String verb, Object[] args)
         throws Exception, Ejection;
}


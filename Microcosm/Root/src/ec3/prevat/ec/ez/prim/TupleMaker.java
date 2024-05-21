package ec.ez.prim;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.EZObject;
import ec.ez.collect.TupleImpl;


/*package*/ class TupleMaker implements EZObject {
    
    static private final Object THE_ONE = new TupleMaker();

    private TupleMaker() {}

    static public Object theOne() {
        return THE_ONE;
    }

    /**
     * Implements all arities of "run" to return a Tuple of their
     * arguments.  XXX Should also provide for the standard
     * meta-messages. 
     */
    public Object perform(String verb, Object[] args) 
         throws Exception, Ejection
    {
        if (! verb.equals("run")) {
            throw new NoSuchMethodException(verb + " not found");
        }
        return TupleImpl.make(args);
    }
}

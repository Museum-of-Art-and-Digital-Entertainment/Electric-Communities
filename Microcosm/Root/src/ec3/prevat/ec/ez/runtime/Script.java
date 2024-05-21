package ec.ez.runtime;

/**
 * The "code" executed to process a request sent to an object.
 */
public interface Script {

    /**
     *
     */
    public Object execute(Object rec, String verb, Object[] args)
         throws Exception, Ejection;
}


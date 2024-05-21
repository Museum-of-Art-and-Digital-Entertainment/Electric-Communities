package ec.e.run;

/**
 * An operation which may only happen once was attempted a second time
 */
public class OnceOnlyException extends Exception {

    /**
     * 'msg' is the standard Exception detail message
     */
    public OnceOnlyException(String msg) {
        super(msg);
    }
}

package ec.plcompile;

/**
  Error thrown when Pluribus compiler has problems calling into Java.
*/
public class CallError extends Error {

    /**
    * Constructs a CallError with no detail message.
    */
    public CallError() {
    }

    /**
    * Constructs a CallError with the specified detail message.
    *
    * @param msg the detail message
    */
    public CallError(String msg) {
        super(msg);
    }
}

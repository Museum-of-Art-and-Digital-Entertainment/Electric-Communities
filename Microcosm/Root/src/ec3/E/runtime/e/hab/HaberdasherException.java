package ec.e.hab;

/**
  Catch-all exception for Haberdashery errors, until we figure out what the
  real error set consists of.
*/
public class HaberdasherException extends RuntimeException {

    /**
    * Constructs a HaberdasherException with no detail message.
    */
    public HaberdasherException() {
    }

    /**
    * Constructs a HaberdasherException with the specified detail message.
    *
    * @param msg the detail message
    */
    public HaberdasherException(String msg) {
        super(msg);
    }
}

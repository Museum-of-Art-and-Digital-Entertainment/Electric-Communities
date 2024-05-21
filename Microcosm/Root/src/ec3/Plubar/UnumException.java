package ec.plubar;

/**
 * An exception class for the Unum creation process.
 *
 * @author Karl Schumaker
 * @version 1.0
 */
public class UnumException extends Exception {
    /**
     * A simple constructor which merely calls super(String) on Throwable.
     */
    UnumException (String message) {
        super(message);
    }
}

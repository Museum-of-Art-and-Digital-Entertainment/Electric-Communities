package ec.ez.runtime;

/**
 * ArityMismatchException - broken out to please the Symantec Cafe Compiler
 */
public class ArityMismatchException extends Exception {
    public ArityMismatchException(String desc) {
        super(desc);
    }
}


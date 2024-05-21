package ec.ez.ezvm;
/**
 * ArityMismatchException - broken out to please the Symantec Cafe Compiler
 */
public class ArityMismatchException extends Exception {
    public ArityMismatchException(String desc) {
        super(desc);
    }
}


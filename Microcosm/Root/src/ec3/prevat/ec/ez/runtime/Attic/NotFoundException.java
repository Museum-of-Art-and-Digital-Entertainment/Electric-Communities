package ec.ez.runtime;

/**
 * NotFoundException - broken out to please the Symantec Cafe Compiler
 */
public class NotFoundException extends Exception {
    public NotFoundException(String desc) {
        super(desc);
    }
}


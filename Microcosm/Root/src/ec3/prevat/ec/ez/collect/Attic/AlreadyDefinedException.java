package ec.ez.collect;
/**
 * AlreadyDefinedException - broken out to please the Symantec Cafe Compiler
 */
public class AlreadyDefinedException extends Exception {
    public AlreadyDefinedException(String desc) {
        super(desc);
    }
}


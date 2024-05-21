package ec.e.db;

/**
 * Signals a formatting problem on a stream.
 * @version     1.0, 11/30/95
 */
public class DBAccessException extends Exception {
    
    public DBAccessException() {
        super();
    }
    
    public DBAccessException(String s) {
        super(s);
    }
}

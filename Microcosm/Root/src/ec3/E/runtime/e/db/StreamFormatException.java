package ec.e.db;

/**
 * Signals a formatting problem on a stream.
 * @version     1.0, 11/30/95
 */
public class StreamFormatException extends Exception {
    
    public StreamFormatException() {
        super();
    }
    
    public StreamFormatException(String s) {
        super(s);
    }
}

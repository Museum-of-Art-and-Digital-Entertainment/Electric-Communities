package ec.e.db;
import java.lang.RuntimeException;

public class RtSeekException extends RuntimeException {
    public RtSeekException () {
        super("Seek past end of stream");
    }
}

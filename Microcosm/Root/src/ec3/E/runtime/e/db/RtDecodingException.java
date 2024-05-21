package ec.e.db;
import java.lang.RuntimeException;
import java.lang.String;

public class RtDecodingException extends RuntimeException {
    public RtDecodingException (String info) {
        super(info);
    }
}

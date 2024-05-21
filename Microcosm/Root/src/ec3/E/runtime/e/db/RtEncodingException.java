package ec.e.db;
import java.lang.RuntimeException;
import java.lang.String;

public class RtEncodingException extends RuntimeException {
    public RtEncodingException (String info) {
        super(info);
    }
}

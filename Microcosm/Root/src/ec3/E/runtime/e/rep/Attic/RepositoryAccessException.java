package ec.e.rep;
import java.lang.RuntimeException;
import java.lang.String;

public class RepositoryAccessException extends RuntimeException
{
    public RepositoryAccessException (String info) {
        super(info);
    }
}

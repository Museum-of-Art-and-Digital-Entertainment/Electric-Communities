package ec.e.rep;
import java.lang.RuntimeException;
import java.lang.String;

public class RepositoryKeyNotFoundException extends RuntimeException
{
    public RepositoryKeyNotFoundException (String info) {
        super(info);
    }
}

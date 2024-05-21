package ec.e.rep;
import java.lang.RuntimeException;
import java.lang.String;

public class RepositoryNeedParameterException extends RuntimeException
{
    public RepositoryNeedParameterException (String info) {
        super(info);
    }
}

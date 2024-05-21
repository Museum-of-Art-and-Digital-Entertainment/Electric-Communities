package ec.e.rep.steward;
import java.io.IOException;

public class RepositoryNeedParameterException extends IOException
{
    public RepositoryNeedParameterException (String info) {
        super(info);
    }
}

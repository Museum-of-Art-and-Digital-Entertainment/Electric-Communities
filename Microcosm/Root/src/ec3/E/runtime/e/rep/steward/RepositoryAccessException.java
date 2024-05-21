package ec.e.rep.steward;
import java.io.IOException;

public class RepositoryAccessException extends IOException
{
    public RepositoryAccessException (String info) {
        super(info);
    }
}

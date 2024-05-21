package ec.e.rep.steward;
import java.io.IOException;

public class RepositoryKeyNotFoundException extends IOException
{
    public RepositoryKeyNotFoundException (String info) {
        super(info);
    }
}

package ec.e.rep.steward;
import java.io.IOException;

public class NotRepositoryFileException extends IOException
{
    public NotRepositoryFileException(String info) {
        super(info);
    }
}

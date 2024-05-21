package ec.e.run;
import java.lang.RuntimeException;
import java.lang.String;

public class RtObsoleteMethodException extends RuntimeException
{
	public RtObsoleteMethodException (String name) {
		super(name);
	}
}

package ec.e.run;

import java.lang.RuntimeException;

public class RtRuntimeException extends RuntimeException
{
    // Have to provide info, so disallow this
    private RtRuntimeException () {
    }

    public RtRuntimeException (String info) {
        super(info);
    }
}


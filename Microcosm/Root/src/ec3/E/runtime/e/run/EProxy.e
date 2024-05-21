package ec.e.run;

import ec.util.NestedException;

/** Deprecated and unused now. Left in to keep ecomp happy for
 * the time being. */
eclass EProxy
{
}

public class ConnectionDeadEException extends NestedException {
    public ConnectionDeadEException(String msg) {
        super(msg);
    }
    public ConnectionDeadEException(String msg, Throwable t) { super(msg, t); }
}

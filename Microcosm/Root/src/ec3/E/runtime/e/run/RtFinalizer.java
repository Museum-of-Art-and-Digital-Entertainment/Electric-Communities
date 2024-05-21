package ec.e.run;

/**
 * Interface used by Vat objects that want to be finalized
 * from within the Vat context. They call RtRun.queueReallyFinalize(this)
 * from their Java finalize method (doing nothing else), and then do
 * whatever it is they would want to do in the reallyFinalize() method,
 * safe in the Vat context.
 */
public interface RtFinalizer {
    void reallyFinalize();
}

public interface JavaIsStupidFinalizer
{
    void stupidFinalize() throws Throwable;
}

class FinalizerDummy implements RtFinalizer
{
    public void reallyFinalize() {
    }
}

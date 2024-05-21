package ec.e.run;

eclass ECatchClosure
implements EResult
{
    private boolean myOldStyle;
    private (Throwable->void) myMethod;
    private InternalECatchClosure myInternalClosure;
    private RtExceptionEnv myEnv;

    emethod forwardException(Throwable exception) {
        if (exception == null) {
            System.err.println("ECatch got null exception");
        }
        //System.out.println ("Catch callback called with " + exception +
        //" for closure " + this);
        RtRun.CurrentExceptionEnv = myEnv;
        // XXX - See note on EWhenClosure warning about direct dispatch
        if (myOldStyle) {
            myMethod(exception);
        } else {
            myInternalClosure.catchMe(exception);
        }
    }

    emethod forward(Object o) {
        ethrow new RtRuntimeException(
            "cannot call forward() on an ECatchClosure");
    }

    public ECatchClosure((Throwable->void) in_method, RtExceptionEnv in_env) {
        myMethod = in_method;
        myInternalClosure = null;
        myOldStyle = true;
        myEnv = in_env;
    }

    public ECatchClosure(InternalECatchClosure ic, RtExceptionEnv in_env) {
        myInternalClosure = ic;
        myMethod = null;
        myOldStyle = false;
        myEnv = in_env;
    }
}

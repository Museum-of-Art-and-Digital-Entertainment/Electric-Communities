package ec.e.run;

eclass EWhenClosure
implements EResult
{
    private boolean myOldStyle;
    private (Object->void) myMethod;
    private InternalEWhenClosure myInternalClosure;

    emethod forward(Object arg) {
        // XXX - Note we are subversively sneaking in the back door and
        // sending an emethod to the closure's target (which is
        // the EObject implementing the body of the ewhen). Because
        // only one emethod should be executing on a particular EObject
        // at a time, we have to rerndezvous with the Runloop emethod
        // dispatch, or send an emethod here rather than call directly (slow).
        // Currently, we don't have to do anything, as there is only
        // a singlethreaded runloop and t isn't possible for another
        // emethod to execute on the target object, but in the future
        // we may have to revisit this.
        if (myOldStyle) {
            myMethod(arg);
        } else {
            myInternalClosure.doit(arg);
        }
    }

    emethod forwardException(Throwable t) {
        ethrow new RtRuntimeException(
            "cannot call forwardException() on an EWhenClosure");
    }

    public EWhenClosure((Object->void) in_method)
    {
        myMethod = in_method;
        myInternalClosure = null;
        myOldStyle = true;
    }

    public EWhenClosure(InternalEWhenClosure ic) {
        myInternalClosure = ic;
        myMethod = null;
        myOldStyle = false;
    }
}

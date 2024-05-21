package ec.e.run;

public class RtInvocation
{
    RtTether myTarget;
    RtSealer mySealer;
    Object[] myArgs; // the version only ever known to the E runtime
    Object[] mySharedArgs; // the version we hand out publicly
    RtExceptionEnv myEE;

    public RtInvocation(RtTether target, RtEnvelope env) {
        this(target, env.mySealer, env.myArgs, env.myEE);
    }

    public RtInvocation(RtTether target, RtSealer sealer, Object[] args,
            RtExceptionEnv ee) {
        if (target == null) {
            throw new RtRuntimeException("null target");
        }
        if (sealer == null) {
            throw new RtRuntimeException("null sealer");
        }
        if (args == null) {
            throw new RtRuntimeException("null args");
        }
        myTarget = target;
        mySealer = sealer;
        myArgs = null;
        mySharedArgs = args;
        myEE = ee;
    }

    public RtTether getTarget() {
        return myTarget;
    }

    public RtSealer getSealer() {
        return mySealer;
    }

    public Object[] getArgs() {
        if (mySharedArgs == null) {
            mySharedArgs = new Object[myArgs.length];
            System.arraycopy(myArgs, 0, mySharedArgs, 0, myArgs.length);
        }

        return mySharedArgs;
    }

    Object[] getInternalArgs() {
        if (myArgs == null) {
            // XXX-NOTE: here is where we allocate from a pooled array
            myArgs = new Object[mySharedArgs.length];
            System.arraycopy(mySharedArgs, 0, myArgs, 0, myArgs.length);
        }

        return myArgs;
    }

    public RtExceptionEnv getKeeper() {
        return myEE;
    }

    public void send() {
        if (myTarget instanceof EObject_$_Impl) {
            getInternalArgs();
            RtEnqueue.enq(myTarget, mySealer, myEE, myArgs);
        } else {
            getArgs();
            myTarget.invoke(mySealer, mySharedArgs, myEE);
        }
    }
}

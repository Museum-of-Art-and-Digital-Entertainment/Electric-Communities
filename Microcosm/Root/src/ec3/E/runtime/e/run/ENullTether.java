package ec.e.run;

final public class ENullTether
implements RtTether
{
    static public final ENullTether TheNullTether = new ENullTether();

    public void invoke(RtSealer s, Object[] args, RtExceptionEnv ee) {
        // do nothing. that's the point
        if (RtRun.tr.debug && Trace.ON) {
            RtRun.tr.debugm("ignoring message in invoke(): " + 
                RtEnvelope.messageToString(this, s, args, ee));
        } 
    }

    public void invokeNow(RtSealer s, Object[] args, RtExceptionEnv ee) {
        // do nothing. that's the point
        if (RtRun.tr.debug && Trace.ON) {
            RtRun.tr.debugm("ignoring message in invokeNow(): " + 
                RtEnvelope.messageToString(this, s, args, ee));
        } 
    }

    public boolean encodeMeForDeflector() {
        return false;
    }

    public String toString() {
        return "#<null tether>";
    }
}

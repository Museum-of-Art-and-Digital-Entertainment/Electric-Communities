package ec.e.run;

public class EStone
implements RtAssignedTether
{
    private RtTether myTarget = null;
    private EObject myDeflector = null;

    public EStone(RtTether target) {
        myTarget = target;
    }

    public boolean encodeMeForDeflector() {
        return false;
    }   
    
    public void assignDeflector(RtDeflector d) {
        if (myDeflector != null) {
            throw new RtRuntimeException("Deflector already assigned");
        }
        myDeflector = (EObject_$_Intf) d;
    }
    
    public void unassignDeflector(RtDeflector d) {
        if (d == myDeflector) {
            myDeflector = null;
        }
    }

    public void invoke(RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (! RtDeflector.stdInvokeBehaviors(myDeflector, s, args, ee)) {
            myTarget.invoke(s, args, ee);
        }
    }

    public void invokeNow(RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (! RtDeflector.stdInvokeNowBehaviors(myDeflector, s, args, ee)) {
            myTarget.invokeNow(s, args, ee);
        }
    }
}

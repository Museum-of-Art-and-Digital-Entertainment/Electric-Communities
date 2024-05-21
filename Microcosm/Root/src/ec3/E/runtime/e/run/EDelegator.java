package ec.e.run;

/** 
 * This class is an RtTether which takes an array of sealers and an
 * associated array of targets and delegates the methods (sealers) to
 * the targets. It also optionally takes a "default" target to handle
 * any method not explicitly mentioned. It also can be told whether or
 * not to deal with "respond" specially. If respond is handled
 * specially, that means that the delegator returns its deflector in
 * response to respond() rather than just passing it through.
 */

public class EDelegator
implements RtAssignedTether
{
    protected RtSealer[] mySealers = null;
    protected RtTether[] myTargets = null;
    protected RtTether myDefaultTarget = null;
    protected boolean myRespondSpecial = false;
    protected boolean myIsInvalidated = false;

    private static final Trace theTrace = new Trace("ec.e.run.EDelegator");

    /** only assigned if myRespondSpecial == true */
    private RtDeflector myDeflector = null;

    public EDelegator() {}

    public EDelegator(RtSealer[] sealers, RtTether[] targets,
        RtTether defaultTarget, boolean respondSpecial)
    {
        if (sealers.length != targets.length) {
            throw new RtRuntimeException("array lengths must match");
        }

        // XXXBUG--we should sort the sealers by hashCode and then
        // be doing binary search in invoke() to make things faster
        mySealers = sealers;
        myTargets = targets;
        myDefaultTarget = defaultTarget;
        myRespondSpecial = respondSpecial;
    }

    protected void setInvalidated () {
        myIsInvalidated = true;
    }

    public void initialize(RtSealer[] sealers, RtTether[] targets,
                           RtTether defaultTarget, boolean respondSpecial)
    {
        if (mySealers != null) {
            throw new RtRuntimeException("Attempt to re-initialize EDelegator");
        }
        if (sealers.length != targets.length) {
            throw new RtRuntimeException("array lengths must match");
        }

        // XXXBUG--we should sort the sealers by hashCode and then
        // be doing binary search in invoke() to make things faster
        mySealers = sealers;
        myTargets = targets;
        myDefaultTarget = defaultTarget;
        myRespondSpecial = respondSpecial;
    }

    public void revoke(Object key) {
        if (myDeflector != null) {
            RtDeflector.setTarget(myDeflector, key, (RtTether)null);
        }
    }

    public RtDeflector getDeflector() {
        return myDeflector;
    }

    public void setDefaultTarget(RtTether t) {
        myDefaultTarget = t;
    }

    /** called by the system when the deflector gets created. */
    public void assignDeflector(RtDeflector d) {

        if (theTrace.debug && Trace.ON) theTrace.debugm("Assigning deflector "+d+" to "+this);

        if (myRespondSpecial) {
            if (myDeflector != null) {
                throw new RtRuntimeException("Deflector already assigned");
            }
            myDeflector = d;
        }
    }

    /** called by the system if this tether is detached from its
     * deflector */
    public void unassignDeflector(RtDeflector d) {

        if (theTrace.debug && Trace.ON) theTrace.debugm("*****UN*****assigning deflector "+d+" from "+this);

        if (myRespondSpecial) {
            if (d == myDeflector) {
                myDeflector = null;
            }
        }
    }

    private RtTether lookupTarget(RtSealer s) {
        for (int i = 0; i < mySealers.length; i++) {
            if (mySealers[i] == s) {
                return myTargets[i];
            }
        }
        return myDefaultTarget;
    }

    private void doMessageNotUnderstood(RtExceptionEnv ee) {
        ekeep (ee) {
            ethrow new RtInvocationException("message not understood");
        }
    }

    public void invoke(RtSealer s, Object[] args, RtExceptionEnv ee) {

        if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", invoking for sealer "+s+", args "+args);

        if (myIsInvalidated) {
            if (theTrace.warning && Trace.ON) theTrace.warningm(this.toString() + " is invalidated, dropping message "+s+" "+args);
            return;
        }

        if (myRespondSpecial) {
            if (RtDeflector.stdInvokeBehaviors((EObject_$_Intf)myDeflector, s, args, ee)) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", invoked standard behaviors for sealer "+s);
                return;
            }
        }

        RtTether targ = lookupTarget(s);

        if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", Found target "+targ+" for sealer "+s);

        if (targ != null) {
            targ.invoke(s, args, ee);
        } else {
            // BUG--should queue this up instead of doing it directly
            doMessageNotUnderstood(ee);
        }
    }

    public void invokeNow(RtSealer s, Object[] args, RtExceptionEnv ee) {

        if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", invoking for sealer "+s+", args "+args);

        if (myIsInvalidated) {
            if (theTrace.warning && Trace.ON) theTrace.warningm(this.toString() + " is invalidated, dropping message "+s+" "+args);
            return;
        }

        if (myRespondSpecial) {
            if (RtDeflector.stdInvokeNowBehaviors((EObject_$_Intf)myDeflector, s, args, ee)) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", invoked standard behaviors for sealer "+s);
                return;
            }
        }

        RtTether targ = lookupTarget(s);

        if (theTrace.debug && Trace.ON) theTrace.debugm("this is "+this+", Found target "+targ+" for sealer "+s);

        if (targ != null) {
            targ.invokeNow(s, args, ee);
        } else {
            doMessageNotUnderstood(ee);
        }
    }
    
    public boolean encodeMeForDeflector() {
        return false;
    }   
}

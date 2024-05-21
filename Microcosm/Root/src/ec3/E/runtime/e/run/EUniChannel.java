package ec.e.run;

import java.util.Vector;
import ec.util.NestedException;

/** Important note: EUniChannel expects that the deflector that
 * points to it has as a key the EUniChannel object itself. It relies
 * on this fact to do the forwarding optimization. (That is, when the
 * distributor gets collected, the channel can short-circuit itself
 * away by replacing the deflector's target.
 */
public class EUniChannel
implements RtAssignedTether, RtDelegatingSerializable
{
    private static Trace theTrace = new Trace("EUniChannel");

    private boolean myIsForwarded = false;
    private RtTether myTarget = null;
    private Throwable myException = null;
    private Vector myPendingMessages = null;
    private RtDeflector myDeflector = null;
    private EUniDistributor myDistributor;

    public String toString () {
        return super.toString() + "[myIsForwarded "+myIsForwarded+", myTarget "+myTarget+"]";
    }

    public EUniChannel() {
        myDistributor = new EUniDistributor(this);
    }

    public EUniDistributor getDistributor() {
        if (myDistributor != null) {
            EUniDistributor result = myDistributor;
            myDistributor = null;
            return result;
        }
        throw new RtRuntimeException("Distributor already handed out");
    }

    /** Note that this returns true both if this object was forwarded
     * a value or if it was forwardException'ed an exception. 
     * You need to call isExceptional() to check to see which particular
     * forwarded state you have.
     */
    public boolean isForwarded() {
        return myIsForwarded;
    }

    public boolean isExceptional() {
        return (myException != null);
    }

    public RtTether getTarget() {
        if (myIsForwarded && (myException != null)) {
            return myTarget;
        } else {
            return null;
        }
    }

//ABS 971215 Start
    public Object delegateToSerialize() {
        if (myIsForwarded && (myException == null)) {
            return myTarget;
        } else {
            return null;
        }
    }
//ABS 971215 End

    public Throwable getException() {
        if (myIsForwarded) {
            return myException;
        } else {
            return null;
        }
    }

    public void assignDeflector(RtDeflector defl) {
        if (myDeflector != null) {
            throw new RtRuntimeException("Deflector already assigned");
        }
        myDeflector = defl;
    }

    public void unassignDeflector(RtDeflector defl) {
        if (myDeflector == defl) {
            myDeflector = null;
        }
    }

    /*package*/ void forward(Object value) {
        if (myIsForwarded) {
            

            theTrace.warningm("UniChannels may only take one value.  Current value is "+myTarget+", new value tried to be "+value);

/*
            throw new RtRuntimeException(
                "UniChannels may only take one value");
*/

        }
        try {
            myTarget = (RtTether) value;
        } catch (ClassCastException e) {
            throw new RtRuntimeException(
                "UniChannels may only be forwarded to RtTethers");
        }
        myIsForwarded = true;
        if ((myTarget != null) && (myPendingMessages != null)) {
            int sz = myPendingMessages.size();
            for (int i = 0; i < sz; i++) {
                RtEnvelope e = (RtEnvelope) myPendingMessages.elementAt(i);
                myTarget.invoke(e.mySealer, e.myArgs, e.myEE);
            }
        }
        myPendingMessages = null;
    }

    /*package*/ void forwardException(Throwable t) {
        if (myException != null) {
            throw new RtRuntimeException(
                "UniChannels may only take one exception");
        }
        myTarget = null;
        myException = t;
        myIsForwarded = true;
        myPendingMessages = null; // would never get delivered anyway
    }

    /** Knowing this allows us to do some optimizations */
    /*package*/ void distributorGotCollected() {
        if (!myIsForwarded) {
            // will never be forwarded; treat it as forward to null
            forward(null);
        }

        if ((myException != null) || (myTarget == null)) {
            // will never have a recipient
            RtDeflector.setTarget(myDeflector, this, 
                ENullTether.TheNullTether);
        } else {
            // will always have the same non-null target
            RtDeflector.setTarget(myDeflector, this, myTarget);
        }

        RtDeflector.setKey(myDeflector, this, null);
    }

    private void addNewMessage(RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (myPendingMessages == null) {
            myPendingMessages = new Vector(5);
        }
        myPendingMessages.addElement(new RtEnvelope(s, args, ee));
    }

    public void invoke(RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (myException == null) {
            if (myIsForwarded) {
                if (myTarget != null) {
                    myTarget.invoke(s, args, ee);
                } else {
                    // goes into the bit-bucket
                }
            } else {
                addNewMessage(s, args, ee);
            }
        } else {
            // goes into the bit-bucket
        }
    }

    public void invokeNow(RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (myException == null) {
            if (myIsForwarded) {
                if (myTarget != null) {
                    myTarget.invokeNow(s, args, ee);
                } else {
                    // goes into the bit-bucket
                }
            } else {
                addNewMessage(s, args, ee);
            }
        } else {
            // goes into the bit-bucket
        }
    }

    public boolean encodeMeForDeflector() {
        return false;
    }

    // BEGIN new E stuff
    /**
     * Construct a unichannel and return a deflector for the given
     * class pointing at that unichannel.
     * @param cl the non-deflector class to get a deflector from
     */
    static public Object construct(Class cl) {
        Class deflectorClass;
        try {
            deflectorClass = Class.forName(cl.getName() + "_$_Deflector");
        } catch (ClassNotFoundException e) {
            throw new NestedException("Couldn't get deflector class", e);
        }
        EUniChannel teth = new EUniChannel();
        return RtDeflector.construct(deflectorClass, teth, null);
    }

    /**
     * Get the distributor out of the given object, but only if the
     * given object is in fact a deflector to a unichannel whose
     * distributor has not yet been taken.
     */
    static public EUniDistributor getDistributor(Object chan) {
        if (chan instanceof RtDeflector) {
            RtDeflector defl = (RtDeflector) chan;
            Object targ = RtDeflector.unsafeGetTarget(defl);
            if (targ instanceof EUniChannel) {
                EUniChannel uc = (EUniChannel) targ;
                return uc.getDistributor();
            }
        } 

        throw new RtRuntimeException("getDistributor called on bad object: " +
            chan);
    }
    // END new E stuff
}

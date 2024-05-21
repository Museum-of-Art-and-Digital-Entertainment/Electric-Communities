package ec.e.run;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import ec.vcache.ClassCache;
import ec.util.NestedException;

public class RtDeflector
implements Exportable, RtDelegatingEncodeable, RtDelegatingSerializable
//ABS 971214 Added RtSerializingEncodeable support
{
    /** key to use when a deflector is explicitly unlocked */
    static final public Object TheOpenKey = "unlocked-deflector";

    /** trace object for deflector ops */
    static private final Trace TheTrace = new Trace("ec.e.run.RtDeflector");

    protected RtTether target_$_;
    private boolean mustNotify;
    private Object key;

    // Identity is maintained invariant for all exports of this Object
    private long identity = 0L;

    //ABS Serializing delegate object used for just Deflector facets
    private Object mySerializingDelegate;

    public RtDeflector(RtTether target, Object key) {
        if (target == null) {
            target = ENullTether.TheNullTether;
        }
        target_$_ = target;
        this.key = key;
        if (target instanceof RtAssignedTether) {
            mustNotify = true;
            ((RtAssignedTether) target).assignDeflector(this);
        } else {
            mustNotify = false;
        }
    }

    public String toString () {
        return super.toString() + "[target " + target_$_ + "]";
    }

    public boolean encodeMeForDeflector()  {
        return false;
    }

    /** This just calls through to the target.
     */
    public void invoke(RtSealer s, Object[] args, RtExceptionEnv ee) {
        target_$_.invoke(s, args, ee);
    }

    /** This also just calls through to the target.
     */
    public void invokeNow(RtSealer s, Object[] args, RtExceptionEnv ee) {
        target_$_.invokeNow(s, args, ee);
    }

    private static Class[] constructTypes;
    static {
        try {
            constructTypes = new Class[2];
            constructTypes[0] = RtTether.class;
            constructTypes[1] = Object.class;
        } catch (ClassNotFoundException e) {
            // shouldn't happen
            throw new Error("couldn't initialize RtDeflector");
        }
    }

    /**
     * Construct a deflector given the (unmunged) name of the eclass/einterface
     * and a tether and key.
     */
    static public RtDeflector construct(String className, RtTether tether,
            Object key) {
        Class theClass;

        try {
            theClass = ClassCache.forName(className + "_$_Deflector");
        } catch (ClassNotFoundException x) {
            throw new NestedException("couldn't construct deflector for " +
                className, x);
        }

        return RtDeflector.construct(theClass, tether, key);
    }

    /**
     * Construct a deflector given the class object for the deflector
     * class and a tether and key.
     */
    static public RtDeflector construct(Class deflClass, RtTether tether,
            Object key) {
        Throwable e = null;
        try {
            Constructor cons = deflClass.getDeclaredConstructor(
                constructTypes);
            Object[] args = { tether, key };
            return (RtDeflector) cons.newInstance(args);
        } catch (NoSuchMethodException x) {
            e = x;
        } catch (InstantiationException x) {
            e = x;
        } catch (IllegalAccessException x) {
            e = x;
        } catch (InvocationTargetException x) {
            e = x;
        } catch (RuntimeException x) {
            e = x;
        }
        throw new NestedException("couldn't construct deflector " +
            deflClass, e);
    }

    /**
     * Get the target of the given deflector, but only if the key
     * matches.
     */
    static public RtTether getTarget(Object o, Object key) {
        if (! (o instanceof RtDeflector)) {
            return null;
        }
        RtDeflector defl = (RtDeflector) o;
        if (defl.key == key) {
            return unsafeGetTarget(defl);
        } else {
            return null;
        }
    }

    /**
     * Set the target of the given deflector, but only if the key
     * matches.
     */
    static public void setTarget(Object o, Object key,
            RtTether target) {
        if (! (o instanceof RtDeflector)) {
            return;
        }
        RtDeflector defl = (RtDeflector) o;
        if (defl.key == key) {
            unsafeSetTarget(defl, target);
        }
    }

    /**
     * Set the key of the given deflector, but only if the current
     * key matches.
     */
    static public void setKey(Object o, Object key,
            Object newKey) {
        if (! (o instanceof RtDeflector)) {
            return;
        }
        RtDeflector defl = (RtDeflector) o;
        if (defl.key == key) {
            unsafeSetKey(defl, newKey);
        }
    }

    /**
     * Get the target of the given deflector. Package method,
     * does not check for matching key.
     */
    static /*package*/ RtTether unsafeGetTarget(RtDeflector defl) {
        RtTether result = defl.target_$_;
        // we use instanceof in order to be resilient wrt checkpointing
        if (result instanceof ENullTether) {
            result = null;
        }
        return result;
    }

    /**
     * Set the target of the given deflector. Package method,
     * does not check for matching key.
     */
    static /*package*/ void unsafeSetTarget(RtDeflector defl,
            RtTether target) {
        if (target == null) {
            target = ENullTether.TheNullTether;
        }
        if (defl.mustNotify) {
            ((RtAssignedTether) defl.target_$_).unassignDeflector(defl);
        }
        defl.target_$_ = target;
        if (target instanceof RtAssignedTether) {
            defl.mustNotify = true;
            ((RtAssignedTether) target).assignDeflector(defl);
        } else {
            defl.mustNotify = false;
        }
    }

    /**
     * Set the key of the given deflector. Package method,
     * does not check for matching key.
     */
    static /*package*/ void unsafeSetKey(RtDeflector defl,
            Object newKey) {
        defl.key = newKey;
    }

    /**
     * Standard net identity method.
     */
    public final long getIdentity() {
        if (identity == 0L) {
            identity = NetIdentityMaker.nextIdentity();
        }
        return identity;
    }

    /**
     * RtDelegatingEncoder method. If Tether wants to
     * be encoded instead of this, then returns it.
     */
    public final Object delegateToEncode ()  {
        if (target_$_.encodeMeForDeflector()) {
            return target_$_;
        }
        else {
            return this;
        }
    }

    //971215 ABS Start
    /**
     * RtDelegatingSerializable method.
     * @return own serializingDelegate if not null, otherwise if
     *   my tether implements RtDelegatingSerializable return its
     *   delegate, otherwise just return my tether.
     */

    public final Object delegateToSerialize() {
        if (mySerializingDelegate != null) {
          // If i have mySerializingDelegate target return that.
          return mySerializingDelegate;
        } else if (target_$_ instanceof RtDelegatingSerializable) {
          // Otherwise if tether implements the RtDelegatingSerializable
          // return its own delegate.
          return ((RtDelegatingSerializable)target_$_).delegateToSerialize();
        } else {
          // Return target
          return target_$_;
        }
    }

    /**
     * Set the serializing delegate of the given deflector,
     * but only if the key matches.
     * @param defl - deflector instance.
     * @param key - corresponding key.
     * @param serializableDelegate - object to encode instead of Deflector.
     */

    static public void setSerializableDelegate(Object o, Object key,
            Object serializableDelegate) {
        if (! (o instanceof RtDeflector)) {
            return;
        }
        RtDeflector defl = (RtDeflector) o;
        if (defl.key == key) {
            defl.mySerializingDelegate = serializableDelegate;
        }
    }
    //971215 ABS End

    static private RtSealer TheRespondSealer =
        sealer (EObject <- respond(EResult));

    static private RtSealer TheOrderSealer =
        sealer (EObject <- order(RtEnvelope, EResult));

    static private RtSealer TheWhenSealer =
        sealer (EObject <- when(EResult));

    static private RtSealer TheMessageWithCauseSealer =
        sealer (EObject <- messageWithCause(RtSealer, RtExceptionEnv, Object,
            String));

    static private void doRespond(
            EObject defl, EResult r, RtExceptionEnv ee) {
        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("defl "+defl+", sending respond-forward to "+r);
        }

        ekeep (ee) {
            if (defl == null) {
                ethrow new RtRuntimeException(
                    "unable to do respond()--improperly set-up deflector");
                return;
            }
            r <- forward(defl);
        }
    }

    static private void doWhen(EObject defl, EResult cl,
            RtExceptionEnv ee) {
        if (TheTrace.debug && Trace.ON) {
            TheTrace.debugm("defl "+defl+", sending when-forward to "+cl);
        }

        ekeep (ee) {
            if (defl == null) {
                ethrow new RtRuntimeException(
                    "unable to do when()--improperly set-up deflector");
                return;
            }
            if (cl != null) {
                cl <- forward(defl);
            } else {
                ethrow new RtRuntimeException("when() got null closure");
            }
        }
    }

    static private void doOrder(EObject defl, RtEnvelope env,
            EResult r, RtExceptionEnv ee) {
        ekeep (ee) {
            if (defl == null) {
                ethrow new RtRuntimeException(
                    "unable to do order()--improperly set-up deflector");
                return;
            }
            if ((env == null) || (r == null)) {
                ethrow new RtRuntimeException(
                    "order() got empty envelope or result");
                return;
            }
            ((RtTether) defl).invokeNow(env.mySealer, env.myArgs, env.myEE);
            r <- forward(defl);
        }
    }

    /**
     * This static method is intended to be used inside the definition
     * of invokeNow() by RtAssignedTethers
     * that desire the "standard" behavior of the base EObject methods
     * respond, order and when.
     *
     * @return true if the message was handled
     */
    static public boolean stdInvokeNowBehaviors(
            EObject defl, RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (s == TheRespondSealer) {
            doRespond(defl, (EResult) args[0], ee);
            return true;
        }
        if (s == TheOrderSealer) {
            doOrder(defl, (RtEnvelope) args[0], (EResult) args[1], ee);
            return true;
        }
        if (s == TheWhenSealer) {
            doWhen(defl, (EResult) args[0], ee);
            return true;
        }
        if (s == TheMessageWithCauseSealer) {
            RtCausality.doMessageWithCause(
                (RtTether) defl, (RtSealer) args[0], (RtExceptionEnv) args[1],
                (Object[]) args[2], (String) args[3]);
            return true;
        }
        return false;
    }

    /**
     * This static method is intended to be used inside the definition
     * of invoke() by RtAssignedTethers
     * that desire the "standard" behavior of the base EObject methods
     * respond, order and when.
     *
     * @return true if the message was handled
     */
    static public boolean stdInvokeBehaviors(
            EObject defl, RtSealer s, Object[] args, RtExceptionEnv ee) {
        if (   (s == TheRespondSealer) || (s == TheOrderSealer)
            || (s == TheWhenSealer) || (s == TheMessageWithCauseSealer)) {
            RtEnqueue.enq((RtTether) defl, s, ee, args);
            return true;
        }
        return false;
    }
}

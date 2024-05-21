package ec.e.run;

// ----------------------------------------
// E$Interface -- base interface type--ecomp requires it

public interface E$Interface
{
}

// ----------------------------------------
// EBoolean

public interface EBoolean
{
}

// ----------------------------------------
// EChannel

public class EChannel
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EChannel class");
    }
}

// ----------------------------------------
// EDistributor

public class EDistributor
extends EObject
implements EDistributor_$_Intf
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EDistributor class");
    }
}

public class EDistributor_$_Impl
extends EObject_$_Impl
implements EDistributor_$_Intf
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EDistributor_$_Impl class");
    }

    public EDistributor_$_Impl(Object x) {
    }
}

public interface EDistributor_$_Intf
extends EResult_$_Intf
{
}

// ----------------------------------------
// EObject

public class EObject 
implements EObject_$_Intf
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EObject class");
    }

    public void when$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void order$async(RtEnvelope x, EResult_$_Intf y) {
        throw new Error("dummy");
    }

    public void respond$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void messageWithCause$async(RtSealer s, RtExceptionEnv e, 
        Object/*[]*/ a, String c) {
        throw new Error("dummy");
    }
}

public class EObject_$_Deflector
implements EObject_$_Intf
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EObject_$_Deflector class");
    }

    protected RtTether target_$_;

    public EObject_$_Deflector(RtTether t, Object k) {
        throw new Error("dummy");
    }

    public void when$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void order$async(RtEnvelope x, EResult_$_Intf y) {
        throw new Error("dummy");
    }

    public void respond$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void messageWithCause$async(RtSealer s, RtExceptionEnv e, 
        Object/*[]*/ a, String c) {
        throw new Error("dummy");
    }
}

public class EObject_$_Impl
implements EObject_$_Intf, RtTether
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EObject_$_Impl class");
    }

    private long identity;

    public void when$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void order$async(RtEnvelope x, EResult_$_Intf y) {
        throw new Error("dummy");
    }

    public void respond$async(EResult_$_Intf x) {
        throw new Error("dummy");
    }

    public void messageWithCause$async(RtSealer s, RtExceptionEnv e, 
        Object/*[]*/ a, String c) {
        throw new Error("dummy");
    }

    public final long getIdentity() {
        throw new Error("dummy");
    }

    public Object value() {
        throw new Error("dummy");
    }

    protected void deliver(RtEnvelope e) {
        throw new Error("dummy");
    }

    public void invoke(RtSealer s, Object[] a, RtExceptionEnv e) {
        throw new Error("dummy");
    }

    public void invokeNow(RtSealer s, Object[] a, RtExceptionEnv e) {
        throw new Error("dummy");
    }
}

public interface EObject_$_Intf 
extends E$Interface
{
    public void when$async(EResult_$_Intf x);
    public void order$async(RtEnvelope x, EResult_$_Intf y);
    public void respond$async(EResult_$_Intf x);
    public void messageWithCause$async(RtSealer s, RtExceptionEnv e, 
        Object/*[]*/ a, String c);
}

public class EObject_$_Sealer
extends RtSealer
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EObject_$_Sealer class");
    }

    public static EObject_$_Sealer sealer_$_respond$$async$OLec$De$Drun$DEResult_$$_Intf$S;
    public static EObject_$_Sealer sealer_$_order$$async$OLec$De$Drun$DRtEnvelope$SLec$De$Drun$DEResult_$$_Intf$S;
    public static EObject_$_Sealer sealer_$_when$$async$OLec$De$Drun$DEResult_$$_Intf$S;
    public static EObject_$_Sealer sealer_$_messageWithCause$$async$OLec$De$Drun$DRtSealer$SLec$De$Drun$DRtExceptionEnv$SLjava$Dlang$DObject$SLjava$Dlang$DString$S;

    public EObject_$_Sealer(int i, String s) {
        super(i, s);
        throw new Error("dummy");
    }

    public void invoke(Object x, Object[] y) throws Exception {
        throw new Error("dummy");
    }
}

// ----------------------------------------
// EResult

public interface EResult_$_Intf
{
}

// ----------------------------------------
// RtCodeable -- encoding for comm

public interface RtCodeable
{
}

// ----------------------------------------
// RtEnvelope

public class RtEnvelope
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy RtEnvelope class");
    }

    static public String messageToString(
        RtTether t, RtSealer s, Object[] a, RtExceptionEnv e) {
        throw new Error("dummy");
    }
}

// ----------------------------------------
// RtExceptionEnv

public final class RtExceptionEnv 
implements RtCodeable 
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy RtExceptionEnv class");
    }

    public void doEThrow(Throwable t) {
        throw new Error("dummy");
    }

    static public void sendException(RtExceptionEnv e, Throwable t) {
        throw new Error("dummy");
    }
}

// ----------------------------------------
// RtInvocation

public class RtInvocation
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy RtInvocation class");
    }
}

// ----------------------------------------
// RtSealer

public abstract class RtSealer
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy RtSealer class");
    }

    public RtSealer(int x, String s) {
        throw new Error("dummy");
    }
}

// ----------------------------------------
// RtTether

public interface RtTether
{
    public void invoke(RtSealer s, Object[] a, RtExceptionEnv e);
    public void invokeNow(RtSealer s, Object[] a, RtExceptionEnv e);
}

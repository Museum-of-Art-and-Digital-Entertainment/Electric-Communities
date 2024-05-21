package ec.e.net.steward;

import ec.vcache.ClassCache;
import ec.e.net.EConnection;
import ec.e.net.ESender;
import ec.e.net.ProxyDeathHandler;
import ec.e.net.ProxyInterest;

public class Proxy
implements RtTether, RtWeakling, RtFinalizer
{
    static public Trace tr = new Trace("ec.e.net.Proxy");

    /**
     * The key to use for all proxies. This *should* vary per
     * connection, but the current design of the comm system
     * (especially 3-party handoff) and of the system in general
     * (e.g., the remnants of the old style where anyone could cast to
     * an EProxy_$_Impl and play with proxy guts) prevents it, so we
     * instead use a universal key for all of 'em. This preserves the
     * pre-existing patterns of use (and makes it more obvious how bad
     * it is to boot). Note that we use a class object to be resilient
     * wrt persistence.
     */
    static private final Object TheProxyKey = Proxy.class;

    private EConnection myConnection = null;
    private ESender mySender;
    private long myID; // in the import table
    private int myReferenceCount = 0;
    private EObject myPrimeDeflector = null;

    // Establish a pointer to the connection that this proxy represents.
    // Check for null before setting, so as to thwart tampering.
    /* XXX This is only public because we're not in ec.e.net as we should be */
    public void setConnection(EConnection connection, long id) {
        if (myConnection == null) {
            myConnection = connection;
            mySender = connection.sender();
        }
        myID = id;
    }

    // Get the assigned connection for this proxy.
    /* XXX This is only public because we're not in ec.e.net as we should be */
    public EConnection getConnection() {
        return myConnection;
    }

    private void setPrimeDeflector(EObject defl) {
        myPrimeDeflector = defl;
    }

    public EObject getPrimeDeflector() {
        return myPrimeDeflector;
    }

    public void setReferenceCount(int referenceCount) {
        if (myReferenceCount != 0) {
            tr.errorm("setReferenceCount(" + referenceCount + ") when myReferenceCount == " + myReferenceCount);
        }
        else {
            myReferenceCount = referenceCount;
        }
    }

    public int getReferenceCount() {
        return myReferenceCount;
    }

    // Route this message through the Comm system to its destination.
    public void invokeNow(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        RtEnvelope envelope = new RtEnvelope(seal, args, ee);
        if (tr.verbose && Trace.ON) tr.$("deliver(" + envelope + ")");
        try {
            if (myConnection != null) {
                mySender.sendEnvelope(myPrimeDeflector, myID, envelope);
            } else {
                tr.errorm("deliver to proxy failed: no connection");
            }
        } catch (Exception e) {
            // Couldn't deliver envelope
            if (tr.debug && Trace.ON) tr.errorm("deliver to proxy failed: " +
                "caught exception, envelope=" + envelope, e);
            ethrow new ConnectionDeadEException(
                "Problem delivering message to " + myConnection, e);
        }
    }

    public boolean encodeMeForDeflector() {
        return false;
    }

    public void invoke(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        RtEnqueue.enq(this, seal, ee, args);
    }

    public void addedToWeakCell(RtWeakCell cell) {
        // I don't care one whit!
    }

    public void removedFromWeakCell(RtWeakCell cell) {
        // Nor do I care about this...
    }

    public void touch() {
        myReferenceCount++;
    }

    // This means that the proxy got GCd locally so we need to clean up
    public void finalize()  {
        RtRun.queueReallyFinalize(this);
    }

    public void reallyFinalize() {
        if (myConnection != null) {
            if (tr.debug && Trace.ON)
                tr.debugm("Proxy " + this +
                    " in finalize, got disconnected/dereferenced");
            myConnection.dgcSuspectTrash(myID, myReferenceCount);
        }
    }

    /* XXX This is only public because we're not in ec.e.net as we should be */
    public long getIdForConnection(EConnection connection) {
        if (connection == myConnection) {
            return myID;
        } else {
            return 0L;
        }
    }

    /**
     * Registers handler as interested in the Proxy's death (which occurs
     * when the connection the Proxy is on goes away).
     * Data is optional context.
     */
    public ProxyInterest registerInterestInProxy (ProxyDeathHandler handler, Object data) {
        ProxyInterest interest = new ProxyInterest(this, handler, data);
        myConnection.registerInterestInProxy(interest, myID);
        return interest;
    }

    /**
     * Unregisters the interest in the Proxy.
     */
    public void unregisterInterestInProxy (ProxyInterest interest) {
        myConnection.unregisterInterestInProxy(interest);
    }

    /**
     * Construct a Proxy deflected to by the given deflector class
     * and on the given connection with the given importID.
     */
    static public Proxy construct(Class deflClass,
            EConnection connection, long importID) {
        // XXX--DEBUG/upgrade code
        String className = deflClass.getName();
        if (className.endsWith("_$_Proxy")) {
            tr.errorReportException(new Throwable(),
                "Proxy.construct called with a _$_Proxy class: " + className);
            int space = className.indexOf(' ');
            if (space != -1) {
                className = className.substring(space + 1);
            }
            className = className.substring(0, className.length() - 5) +
                "Deflector";
            tr.errorm("Transforming to: " + className);
            try {
                deflClass = ClassCache.forName(className);
            } catch (ClassNotFoundException e) {
                tr.errorReportException(e, "exception in transformation");
            }
        }
        // XXX--end DEBUG/upgrade code
        Proxy result = new Proxy();
        EObject defl = (EObject) (RtTether) RtDeflector.construct(
            deflClass, result, TheProxyKey);
        result.setConnection(connection, importID);
        result.setPrimeDeflector(defl);
        return result;
    }

    /**
     * Cosntruct a Proxy deflected to by the given (unmunged) class
     * name and on the given connection with the given importID.
     */
    static public Proxy construct(String className,
            EConnection connection, long importID) {
        Class theClass;

        try {
            theClass = ClassCache.forName(className + "_$_Deflector");
        } catch (ClassNotFoundException e) {
            throw new RtRuntimeException("no such deflector for " + className);
        }

        return Proxy.construct(theClass, connection, importID);
    }

    /**
     * Get at the Proxy target of an object, but only if the given object
     * is in fact a deflector to a proxy. Otherwise, return null.
     */
    static public Proxy getProxyTarget(Object o) {
        return (Proxy) RtDeflector.getTarget(o, TheProxyKey);
    }

    // this stuff is needed to keep Java from deadlocking in
    // the middle of finalization
    static {
        //RtRun.beReadyToFinalize();
        new Proxy().finalize();
    }
}


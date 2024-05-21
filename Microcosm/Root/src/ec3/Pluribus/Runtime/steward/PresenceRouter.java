/*
  PresenceRouter.java -- The presence router.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

import ec.e.file.EStdio;
import ec.e.net.EConnection;
import ec.e.net.ProxyDeathHandler;
import ec.e.net.ProxyInterest;
import ec.e.net.steward.Proxy;
import ec.util.EThreadGroup;
import ec.util.NestedException;
import ec.vcache.ClassCache;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Interface to be implemented by entities that want to register themselves to
 * be notified when new una are received.
 */
public interface jiReceiveInterest {
    /**
     * Notification that an unum was received.
     *
     * @param theUnum  the unum which arrived on the presence's receivetarget
     * @param sessionKey  the session key of that unum, passed in uReceiveUnum
     * @param unumKey  the unum key likewise (i.e. root jUnumKey), passed in
     *  uReceiveUnum
     * @param arrivingUnumSession  the session of the unum on which the object
     *  arrived
     */
    void unumReceived(Unum theUnum, Object sessionKey, Object unumKey,
                      Object arrivingUnumSession);
}

/**
 * The class which represents a presence instance.
 */
public class PresenceRouter extends EDelegator implements ProxyDeathHandler {
    /* Collection of presences which have been sent out (i.e., encoded) but
       not yet heard from. */
    private Vector /* ClientPresenceTracker */ myTrackers = null;

    private Vector /* jiReceiveInterest */ myReceiveInterests;

    /* The session key for which this presence was originally created. Null if
       prime presence. This gets used so we only kill the entire presence when
       revoking its associated session unum */
    private Object myMasterPresenceKey = null;
    
    transient private UnumSoul myUnumSoul;
    private PresenceEnvironment myEnvironment;
    private PresenceState myState;
    private UnumState myUnumState;
    private UnumRouter myUnumRouter;
    private Object myUnumKey = null;
    private EPresence myEPresence;
    
    public static Trace tr = new Trace("ec.pl.runtime.PresenceRouter");
    private static long HackyFakeSwissNumber = 0; // XXX Shouldn't be faked!
    
    /**
     * Create a new, default initialized presence router.
     */
    public PresenceRouter() {
    }
    
    /**
     * Private constructor for use by the clone() method
     */
    private PresenceRouter(Vector trackers, Vector receiveInterests,
                           Object presenceKey, UnumSoul unumSoul,
                           PresenceEnvironment env, PresenceState pState,
                           UnumState uState, UnumRouter unumRouter,
                           Object unumKey, EPresence ePresence) {
        myUnumKey = unumKey;
        myEPresence = ePresence;
        super.initialize(pState.sealers, pState.targets,
                         (RtTether) myEPresence, true);
        myTrackers = trackers;
        myReceiveInterests = receiveInterests;
        myMasterPresenceKey = presenceKey;
        myUnumSoul = unumSoul;
        myEnvironment = env;
        myState = pState;
        myUnumState = uState;
        myUnumRouter = unumRouter;
        RtDeflector.construct(pState.kindName, this, unumKey);
    }
  
    /**
     * Clone this presence router
     */
    protected Object clone() {
        return new PresenceRouter
            (myTrackers, myReceiveInterests,
             myMasterPresenceKey, null,
             myEnvironment, myState, myUnumState,
             myUnumRouter, myUnumKey, myEPresence);
    }

    /**
     * Return the "creation key" of this presence. This is the session key
     * with which this presence was originally made. Only when we kill the unum
     * for this presence, or when we detect that our host has died, do we
     * inform all ingredients of unum death.
     */
    public Object creationKey() {
        return myMasterPresenceKey;
    }
    
    /**
     * Decode a presence sent to us by somebody else.
     *
     * Normally invoked via UnumRouter.decode().
     *
     * @param decoder  The decoder to use
     * @param theUnum  The unum whose presence we are decoding
     */
    UnumRouter decodePresence(RtDecoder decoder, UnumRouter theUnum)
    throws IOException {
        Object sessionKey = null;
        Object unumKey = null;
        Object[] stateBundles = null;
        PresenceHost hostDeflector = null;
        String unumImplClassName = null;
        String presenceToMakeName = null;
        String presInitRoutine = "initPresenceRouter";
        long swissNumber;
        
        try {
            unumImplClassName = (String)decoder.decodeObject();
            presenceToMakeName = (String)decoder.decodeObject();
            sessionKey = decoder.decodeObject();
            unumKey = decoder.decodeObject();
            theUnum.setSessionKey(sessionKey);
            myMasterPresenceKey = sessionKey;
            hostDeflector = (PresenceHost) decoder.decodeObject();
            swissNumber = decoder.readLong();
            myEnvironment = new PresenceEnvironment();
            myEnvironment.hostPresenceDeflector = hostDeflector;
            
            try {
                Class tempUImpl = ClassCache.forName(unumImplClassName);
                Class[] envClassArray = new Class[4];
                envClassArray[0] = String.class;
                envClassArray[1] = ec.pl.runtime.PresenceRouter.class;
                envClassArray[2] = Object.class;
                envClassArray[3] = ec.pl.runtime.PresenceEnvironment.class;
                Method method =
                    tempUImpl.getMethod(presInitRoutine, envClassArray);
                if (method != null) {
                    Object[] envArray = new Object[4];
                    envArray[0] = presenceToMakeName;
                    envArray[1] = this;
                    envArray[2] = unumKey;
                    envArray[3] = myEnvironment;
                    method.invoke(null, envArray);
                } else {
                    EStdio.out().println("PresenceRouter:decodePresence " +
                                         presInitRoutine +
                                         " method not found on " +
                                         unumImplClassName);
                }
            } catch (NoSuchMethodException exc) {
                EStdio.out().println("PresenceRouter:decodePresence "+exc);
            } catch (java.lang.reflect.InvocationTargetException exc) {
                EStdio.out().println("PresenceRouter:decodePresence "+exc);
            } catch (ClassNotFoundException exc) {
                EStdio.out().println("PresenceRouter:decodePresence "+exc);
            } catch (IllegalAccessException exc) {
                EStdio.out().println("PresenceRouter:decodePresence "+exc);
            }
            int numIngrs = myState.ingredients.length;
            stateBundles = new Object[numIngrs];
            for (int i=0; i < numIngrs; i++) {
                stateBundles[i] = decoder.decodeObject();
            }
            initUnumRouter(theUnum, unumKey);
            // When decoding a client, params for unumSoul stuff are unnecessary
            finish(theUnum, stateBundles, null, null);
        } catch (IOException ex) {
            throw new RtRuntimeException("IOException: " + ex.getMessage());
        }
        registerInterestInProxy(myEnvironment.hostPresenceDeflector);
        
        Presence newDeflector = (Presence)(getDeflector());
        
        if (myEnvironment.hostPresenceDeflector != null) {
            if (swissNumber != 0) {
                ((PresenceHost)(myEnvironment.hostPresenceDeflector)) <-
                    newOtherPresence(newDeflector, swissNumber);
            }
        } else {
            System.err.println("Client presence decoded without host: " +this);
            throw new RtRuntimeException(
                "Client presence decoded without host");
        }
        
        return theUnum;
    }

    /**
     * Encode this presence as whatever other presence it should be when
     * instantiated in another process.
     *
     * @param encoder  The encoder to use
     * @param sessionKey  Session key for session with new presence
     *
     * XXX As a public method this is a security hole
     */
    public void encodeOtherPresence(RtEncoder encoder, Object sessionKey) {
        if ((myEnvironment.flags & PresenceEnvironment.Encodeable) == 0) {
            throw new RuntimeException(
               "Exception: Attempt to encode Presence which isn't encodeable");
        }
        try {
            encoder.encodeObject(myState.unumImplClassName);
            encoder.encodeObject(myState.presenceToMakeName);
            encoder.encodeObject(sessionKey);
            encoder.encodeObject(myUnumKey);
            
            long swissNumber = 0;
            PresenceRouter presenceClone = (PresenceRouter) clone();
            encoder.encodeObject(
                presenceClone.makeHostDeflector(myState.kindName, myUnumKey));
            
            if (myEnvironment.otherPresences != null) {
                swissNumber = ++HackyFakeSwissNumber;
                ClientPresenceTracker tracker =
                    myEnvironment.unum.makeClientPresenceTracker(
                        myState.kindName, swissNumber);
                myEnvironment.otherPresences.addElement(
                    new PresenceEntry(tracker.getChannel(), sessionKey,
                                      presenceClone));
                myTrackers.addElement(tracker);
            }
            encoder.writeLong(swissNumber);
            int numIngrs = myState.clientIngredients.length;
            for (int i = 0; i < numIngrs; i++) {
                encoder.encodeObject(
                    ((IngredientJif)myState.clientIngredients[i]).
                    jiGetClientState());
            }
        } catch (Exception e) {
            throw new NestedException("Exception encoding presence", e);
        }
    }

    /**
     * Finish the initialization of a presence after decoding or after creating
     * an unum in the generated createUnum() routine.
     *
     * @param thisUnum  The unum we are in
     * @param stateBundles  All the state bundles
     * @param soulState  soulState to save into myUnumSoul
     * @param unumClassName  for getting unum impl Class inot myUnumSoul
     */
    public void finish(UnumRouter thisUnum, Object[] stateBundles,
                       SoulState soulState, String unumClassName) {
        if (myTrackers != null) {
            throw new UnumSecurityViolationException(
                "Presence finish called more than once!");
        }
        myTrackers = new Vector(1);
        myReceiveInterests = new Vector(1);
        if (myState.isHost) {
            thisUnum.createUnumSoul();
            myUnumSoul = thisUnum.getUnumSoul();
            myEnvironment.soul = myUnumSoul;
            if (null != myUnumSoul) {
                soulState.setUnumSoul(myUnumSoul, unumClassName);
            }
        }
        myEnvironment.unum = thisUnum;
        myEnvironment.unumDeflector = (Unum)(thisUnum.getDeflector());
        myEnvironment.otherPresences = new Vector(1);
        for (int i=0; i < stateBundles.length; i++) {
            IngredientJif ingredient = (IngredientJif) myState.ingredients[i];
            ingredient.initGeneric(stateBundles[i]);
        }
    }
    
    /**
     * Initialize the presence router after creation. Called from the Pluribus
     * compiler-generated createUnum() method in the generated unum impl.
     *
     * @param unumKey  The unum key for this unum.
     * @param state  All the state bundles.
     * @param environment  The presence environment in which to live
     */
    public void initialize(Object unumKey, UnumPresenceState state,
                           PresenceEnvironment environment) {
        if (myUnumKey == null) {
            myUnumKey = unumKey;
            myEPresence = new EPresence(this);
            super.initialize(state.presence.sealers, state.presence.targets,
                             (RtTether) myEPresence, true);
            myState = state.presence;
            myUnumState = state.unum;
            myEnvironment = environment;
            RtDeflector.construct(state.presence.kindName, this, myUnumKey);
            setDefaultTarget((RtTether) myEPresence);
            myEnvironment.flags |= (PresenceEnvironment.Encodeable |
                                    PresenceEnvironment.TrackOtherPresences);
            if (myState.isHost) {
                myEnvironment.flags |= PresenceEnvironment.IsHostPresence;
            } else {
                myEnvironment.flags |= PresenceEnvironment.IsClientPresence;
            }
            myEnvironment.presence = this;
        } else {
            throw new UnumSecurityViolationException(
                "PresenceRouter initialize called more than once!");
        }
    }

    /**
     * Initialize an unum router for this presence router
     *
     * @param router  The unum router to initialize
     * @param key  The unum key associated with it
     */
    public void initUnumRouter(UnumRouter router, Object key) {
        if (key.equals(myUnumKey)) {
            router.initialize(myUnumKey, this, myUnumState);
        } else {
            throw new UnumSecurityViolationException(
                "Attempting to make unum router with invalid unum key");
        }
    }
    
    /**
     * Kill the presence of the UnumRouter on this machine. If this is a host
     * presence it will render all of the client presences invalid as well. If
     * this is a client presence, it will only kill itself, but cause the host
     * to remove it from its list of other presences. This method works for
     * both host and client presences
     *
     * @param invalidateAll  true=>invalidate all the other presences too
     */
    final void invalidate(boolean invalidateAll) {
        Vector savedTrackers = null;
        
        if (myEnvironment == null ||
                (myEnvironment.flags & PresenceEnvironment.Invalidated) != 0) {
            System.err.println(
                "Warning: Attempt to invalidate already invalid Presence");
            return;
        }
        
        if (invalidateAll) {
            /* Invalidate the UnumRouter Router for this Presence */
            myEnvironment.unum.invalidate();
            for (int i = 0; i < myEnvironment.otherUna.size(); i++) {
                UnumEntry entry =
                    (UnumEntry) myEnvironment.otherUna.elementAt(i);
                entry.unum.invalidate();
            }
            
            long identity = getDeflector().getIdentity();
            Proxy proxy;

            if (myTrackers != null && myEnvironment.otherPresences != null) {
                int size = myEnvironment.otherPresences.size();
                for (int i = 0; i < size; i++) {
                    PresenceEntry other =
                      (PresenceEntry)myEnvironment.otherPresences.elementAt(i);
                    other.localRevokablePresence.invalidate(false);
                }
            }
            if (myEnvironment.otherPresences != null) {
                myEnvironment.otherPresences.removeAllElements();
            }
            myEnvironment.presence = null;
            /* Invalidate all of our routing information */
            if (savedTrackers != null) {
                myEnvironment.flags |= PresenceEnvironment.Invalidated;
            } else {
                myEnvironment = null;
                myTrackers = null;
            }
        }
        
        /* EDelegator invalidation */
        setInvalidated();
    }

    /**
     * Kill the presences of the UnumRouter on this and all other machines.
     */
    final void invalidate() {
        invalidate(true);
    }

    /**
     * Construct and return a deflector that represents ourselves as a
     * presence host of a particular kind.
     *
     * @param kindName  The name of the kind we are supposed to look like
     * @param key  Deflector key for the new deflector
     */
    private PresenceHost makeHostDeflector(String kindName, Object key) {
        RtDeflector deflector = getDeflector();
        if (deflector != null) {
            try {
                return (PresenceHost)RtDeflector.construct(kindName,
                                                           (RtTether)deflector,
                                                           key);
            } catch (ClassCastException e) {
                tr.errorm("Failed to make host deflector for kind '" +
                          kindName + "' from deflector " + deflector +
                          ", wasn't PresenceHost...");
                throw e;
            }
        } else {
            throw new UnumSecurityViolationException(
                "Attempting to make host deflector from null deflector, this is "+this);
        }
    }

    /**
     * Received by a host presence informing it of the existence of a new
     * other presence (typically a new client presence).
     *
     * This is basically the callback we get after having encoded a presence
     * to somebody else. The vector 'myTrackers' is a collection that tracks
     * the presences we have sent and thus the presences for which we are
     * expecting this method to be called; since these new presences don't
     * exist at the time we make entries for them in this vector, they are
     * identified by a Swiss number rather than by an object reference.
     *
     * @param otherPresence  The new other presence to note
     * @param swissNumber  The identity of the new presence
     */
    protected void newOtherPresence(Presence otherPresence, long swissNumber) {
        if (myTrackers == null) {
            /* If we aren't expecting to hear from anybody, it's an error */
            System.err.println(
                "NewOtherPresence called when Trackers null with Presence " +
                otherPresence + ", swissNumber is " + swissNumber);
            return;
        }
        try {
            if (tr.debug && Trace.ON)
                tr.$("newOtherPresence called for " + this + " with " +
                     otherPresence);

            /* Find the presence we are expecting */
            ClientPresenceTracker tracker = null;
            int i;
            int size = myTrackers.size();
            for (i = 0; i < size; i++) {
                ClientPresenceTracker temp =
                    (ClientPresenceTracker)myTrackers.elementAt(i);
                if (temp.getSwissNumber() == swissNumber) {
                    myTrackers.removeElementAt(i);
                    tracker = temp;
                    break;
                }
            }
            if (tracker == null) {
                /* If we couldn't find it, something is wrong */
                System.err.println(
                    "NewOtherPresence can't find Presence swiss Number " +
                    swissNumber + " for Presence " + otherPresence);
            } else {
                /* We found it, so deal with it */
                if ((myEnvironment.flags &
                         PresenceEnvironment.Invalidated) == 0) {
                    /* Host presence (us) is still alive, so add the client
                       to our collection of 'otherPresences' */
                    if (myEnvironment.otherPresences == null) {
                        if (tr.debug && Trace.ON)
                            tr.$("NewOtherPresence called when otherPresences"+
                                 " null with Presence " + otherPresence +
                                 ", swissNumber is " + swissNumber);
                        return;
                    }
                    if (tr.debug && Trace.ON)
                        tr.$("NewOtherPresence replacing " + tracker +
                             " with " + otherPresence);
                    PresenceEntry actualEntry = null;
                    for (i = 0; i < myEnvironment.otherPresences.size(); i++) {
                        PresenceEntry entry = (PresenceEntry)
                            myEnvironment.otherPresences.elementAt(i);
                        if (entry.presence == tracker.getChannel()) {
                            actualEntry = entry;
                            break;
                        }
                    }
                    if (actualEntry != null) {
                        actualEntry.presence = otherPresence;
                    } else {
                        if (tr.debug && Trace.ON)
                            tr.$("Couldn't find " + tracker.getChannel() +
                                 " in " + myEnvironment.otherPresences);
                    }
                    registerInterestInProxy(otherPresence);
                    
                    /* Send accumulated client updates outwards */
                    tracker.getDistributor() <- forward(otherPresence);
                } else {
                    /* Presence has been invalidated, tell the client */
                    long identity = getDeflector().getIdentity();
                    if (tr.debug && Trace.ON)
                        tr.$("NewOtherPresence invalidating " + tracker +
                             " for " + otherPresence);
                    if (myTrackers.size() == 0) {
                        myTrackers = null;
                        myEnvironment = null;
                    }
                }
            }
        } catch (Exception e) {
            throw new NestedException("Exception in newOtherPresence", e);
        }
    }

    /**
     * Presence-level version of sendUnum(). Requests presence to send itself
     * to somebody else.
     *
     * This is distinguished from sendUnum in two ways: 1) it will be invoked
     * asynchronously via a method in EPresence; and 2) the requestor is
     * always ourselves, rather than being passed as an explicit parameter.
     *
     * @param target  Unum to whom we are being sent
     * @param sessionKey  Session key for this unum
     * @param unumKey  Unum key for this unum
     */
    protected void pHostSendUnum(UnumReceiver target, Object sessionKey,
                                 Object unumKey) {
        if (tr.debug && Trace.ON)
            tr.debugm("RECEIVED pHostSendUnum FOR " + target + ", sesskey " +
                      sessionKey + ", unumkey " + unumKey);
        sendUnum(myEnvironment.unum, target, sessionKey, unumKey);
    }

    /**
     * This method implements the ProxyDeathHandlerInterface. It is called by
     * EConnection on loss of connection to inform us that a proxy is now dead.
     *
     * @param proxy  The proxy that is now dead
     * @param data  Arbitrary data required by interface (but not used here)
     */
    public final void noteProxyDeath(Object proxy, Object data) {
        if (tr.debug && Trace.ON) {
            if (proxy == null) {
                tr.$("Called with null proxy!");
                System.err.println(tr.getStackTrace());
            } else {
                tr.$("Proxy " + proxy + " died");
            }
        }
        int vectorSize = 0;
        if (myEnvironment == null) {
            /* We're invalidated, we don't care about other Presence death */
            return;
        }
        
        if (tr.debug && Trace.ON)
            tr.$("Removing presence " + proxy + " from otherPresences");
        vectorSize =
            removePresenceFromVector((Presence) proxy,
                                     myEnvironment.otherPresences);
        if (myEnvironment.hostPresenceDeflector == proxy) {
            if (tr.debug && Trace.ON)
                tr.$("Clearing host presence " + proxy);
            myEnvironment.hostPresenceDeflector = null;
            invalidate();
        }
    }
    
    /**
     * Register for interest in arriving una
     *
     * @param interest  Interest to be notified when this presence receives a
     *  new unum.
     */
    public void registerForReceivingInterest(jiReceiveInterest interest) {
        if (myReceiveInterests == null) {
            myReceiveInterests = new Vector(1);
        }
        myReceiveInterests.addElement(interest);
    }

    /**
     * Register to learn of the death of the proxy associated with a presence.
     * The presence will learn of the death by having its noteProxyDeath()
     * method called (PresenceRouter implements ProxyDeathHandler)
     *
     * @param presence  The presence whose proxy death concerns us
     */
    final ProxyInterest registerInterestInProxy(Object presence) {
        if (tr.debug && Trace.ON)
            tr.$("Presence:registerInterestInProxy()");
        Proxy proxy = Proxy.getProxyTarget(presence);
        return proxy.registerInterestInProxy(this, null);
    }

    /**
     * Remove an entry from a vector of PresenceEntry objects. It is permitted
     * for the entry to be absent or for the vector to be null, but it assumes
     * that if the entry is present it is only in the vector once.
     *
     * @param presence  The presence whose entry is to be removed
     * @param vector  The vector to remove it from
     * @return the length of the vector after removal.
     */
    private int removePresenceFromVector(Presence presence, Vector vector) {
        if (tr.debug && Trace.ON)
            tr.$("Presence:removePresenceFromVector()");
        if (vector == null)
            return 0;

        int size = 0;
        boolean removed = false;
        synchronized (vector) { // XXX AHEM: 'synchronized' ???
            int i;
            size = vector.size();
            if (size <= 0)
                return 0;
            for (i = 0; i < size; i++) {
                Presence test = ((PresenceEntry)vector.elementAt(i)).presence;
                if (test == presence) {
                    removed = true;
                    if (tr.debug && Trace.ON)
                        tr.$("Removing presence " + test);
                    vector.removeElementAt(i);
                    break;
                }
            }
        }
        if (removed) {
            return size - 1;
        } else {
            if (tr.debug && Trace.ON)
                tr.$("Couldn't find presence in vector " + presence);
            return size;
        }
    }
  
    /**
     * Send a message (contained in an envelope) to all the elements of a
     * vector of receivers.
     *
     * @param vector  The vector of receivers to send to
     * @param envelope  The message to be sent
     */
    public static void sendEnvelopeToOthers(Vector vector,
                                            RtEnvelope envelope) {
        if (tr.tracing)
            tr.$("Called with " + envelope);
        if (vector == null)
            return;
        if (tr.tracing)
            tr.$("Vector size is " + vector.size() + ", " + vector);
        int i;
        int size = vector.size();
        /* Remake envelope with null exenv so we don't relay ANY exceptions,
           since there is nothing we could do with them anyway. */
        RtEnvelope sendEnv =
            new RtEnvelope(envelope.getSealer(), envelope.cloneArgs(), null);
        for (i = 0; i < size; i++) {
            try {
                EObject obj = ((PresenceEntry)vector.elementAt(i)).presence;
                RtRun.enqueue(obj, sendEnv);
            } catch (Exception e) { // XXX - Should be more specific exception
                EThreadGroup.reportException(e);
            }
        }
    }

    /**
     * Request to presence to make and send an unum (representing itself) to
     * somebody else.
     *
     * @param requestor  Unum that is doing the sending
     * @param target  Unum to whom we are being sent
     * @param sessionKey  Session key for this unum
     * @param unumKey  Unum key for this unum
     */
    public void sendUnum(UnumRouter requestor, UnumReceiver target,
                         Object sessionKey, Object unumKey) {
        Proxy targp = Proxy.getProxyTarget(target);
        Proxy hpdp = Proxy.getProxyTarget(myEnvironment.hostPresenceDeflector);
        
        if (targp != null && hpdp != null &&
                targp.getConnection().equals(hpdp.getConnection())) {
            if (tr.debug && Trace.ON)
                tr.debugm("target " + target +
                          " is co-hosted with us, sending pHostSendUnum!");
            
            /* If target is co-hosted with myEnvironment.hostPresenceDeflector,
               go make the unum there. */
            myEnvironment.hostPresenceDeflector <-
                pHostSendUnum(target, sessionKey, unumKey);
        } else {
            if (myEnvironment.hostPresenceDeflector == null) {
                if (tr.debug && Trace.ON)
                    tr.debugm("we are prime, make presence here.");
            } else if (targp == null) {
                if (tr.debug && Trace.ON)
                    tr.debugm("target is not proxy, make presence here.");
            } else {
                if (tr.debug && Trace.ON)
                    tr.debugm("target connection " + targp.getConnection() +
                              " != " + hpdp.getConnection() +
                              ", make presence here.");
            }
            
            /* Make it locally and dispatch it right here. */
            UnumRouter newUnum = requestor.makeNewUnum(sessionKey);
            
            /* This is one of only two places we actually send an unum over the
               wire and this is the ONLY place where we send containables (or
               rather, if we send elsewhere, it's A BUG!). Do not propagate an
               exception environment to the eventual target; there is nothing
               we can do anyway if they decide not to receive us. */
            ekeep (null) {
                if (tr.event && Trace.ON)
                    tr.eventm(myState.kindName +
                              " requested to send unum to " + target +
                              " for session '" + sessionKey +
                              "' (the unum's key is '" + unumKey + "')");
                target <- uReceiveUnum(newUnum, sessionKey, unumKey);
            }
        }
    }
    
    /**
     * Notification of PresenceRouter about a new unum's creation.
     *
     * @param newUnum  The new unum which was made
     * @param sessionKey  The session key for which it was requested
     * @param unumSessionKey  The session key of the unum through which it was
     *  requested
     */
    public void unumMade(UnumRouter newUnum, Object sessionKey,
                         Object unumSessionKey) {
        if (tr.debug && Trace.ON)
            tr.debugm("sent unum for " + sessionKey + ", unumSessionKey " +
                      unumSessionKey + ", unum " + newUnum);
        myEnvironment.otherUna.addElement(new UnumEntry(newUnum, sessionKey,
                                                        unumSessionKey));
    }
    
    /**
     * Notify registered receive interests that an unum was received.
     *
     * @param theUnum  The unum received
     * @param sessionKey  The key of the session for which it was made (passed
     *  (in)
     * @param unumKey  The key of the unum, eventually secured (passed in)
     * @param unumSessionKey  The key of the unum through which this was
     *  received (wrapped by unum)
     */
    public void unumReceived(Unum theUnum, Object sessionKey, Object unumKey,
                             Object unumSessionKey) {
        for (int i = 0; i < myReceiveInterests.size(); i++) {
            ((jiReceiveInterest)myReceiveInterests.elementAt(i)).unumReceived(
                theUnum, sessionKey, unumKey, unumSessionKey);
        }
    }
    
    /**
     * Notify other una and presences that this unum has been revoked, then
     * invalidate ourselves.
     *
     * @param sessionKey  The key of the session being revoked
     */
    public void unumRevoked(Object sessionKey) {
        if (tr.debug && Trace.ON)
            tr.debugm("revoking session key " + sessionKey + " for " + this);
        /* Go down 'otherUna', repeatedly revoking all una associated with this
           sessionKey; if you find an unum created by this sessionKey, keep
           track of its session */
        Vector childSessions = new Vector(1);
        for (int i = myEnvironment.otherUna.size() - 1; i >= 0; i--) {
            UnumEntry entry = (UnumEntry) myEnvironment.otherUna.elementAt(i);
            if (entry.sessionKey.equals(sessionKey)) {
                entry.unum.invalidate();
                myEnvironment.otherUna.removeElementAt(i);
            }
            if (entry.parentSessionKey != null
                    && entry.parentSessionKey.equals(sessionKey)) {
                childSessions.addElement(entry.sessionKey);
            }
        }
        
        /* walk down otherPresences and do likewise, closing all connections */
        for (int i = myEnvironment.otherPresences.size() - 1; i >= 0; i--) {
            PresenceEntry entry =
                (PresenceEntry) myEnvironment.otherPresences.elementAt(i);
            long identity = getDeflector().getIdentity();
            if (entry.sessionKey.equals(sessionKey)) {
                entry.localRevokablePresence.invalidate(false);
                myEnvironment.otherPresences.removeElementAt(i);
            }
        }
        
        for (int i = 0; i < childSessions.size(); i++) {
            unumRevoked(childSessions.elementAt(i));
        }
        
        if ((sessionKey == null && myMasterPresenceKey == null) ||
                (sessionKey != null && myMasterPresenceKey != null &&
                 sessionKey.equals(myMasterPresenceKey))) {
            invalidate();
        }
    }
}

/*
  UnumRouter.java -- The unum router

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

import java.io.IOException;
import java.util.Vector;

/**
 * Class which represents an unum instance. Handles birth, death and message
 * routing.
 */
public class UnumRouter extends EDelegator
implements RtCodeable, Cloneable, jiSetFacetRecouper {
    transient private UnumSoul mySoul = null;
    private UnumState myState = null;
    private Object myUnumKey = null;
    private EUnum myEUnum = null;
    private Object mySessionKey = null;
    protected PresenceRouter myPresence;
    private jFacetRecouper myFacetRecouper;

    /**
     * Construct a new unum router. Initialization is performed later by a
     * call to initialize().
     */
    public UnumRouter() {
    }

    /**
     * Create a clone of this router. The new router is a shallow copy except
     * for the 'myEUnum' field. This method satisfies the 'Cloneable' interface
     */
    protected Object clone() {
        UnumRouter newRouter = new UnumRouter();
        newRouter.initialize(myUnumKey, myPresence, myState);
        newRouter.setSessionKey(mySessionKey);
        return newRouter;
    }

    /**
     * Give this unum a new soul
     */
    void createUnumSoul() {
        mySoul = new UnumSoul(this, (Unum)(getDeflector()));
    }

    /**
     * The deflector will delegate to this object for serialization.
     *
     * @return jFacetRecouper established by the containership ingredient.
     */
    public Object delegateToSerialize() {
        return myFacetRecouper;
    }

    /**
     * Return this unum's soul
     */
    UnumSoul getUnumSoul() {
        return mySoul;
    }

    /**
     * Initialize a new unum router.
     *
     * @param unumKey  The key for permitting sensitive operations on this unum
     * @param aPresence  The presence router of the local presence of this unum
     * @param state  The state of the unum (ingredients, etc.)
     *
     * This initialization is performed separately from the constructor because
     * the presence decoding logic does not have the initialization parameter
     * information at the time the constructor needs to be called.
     */
    public void initialize(Object unumKey, PresenceRouter aPresence,
                           UnumState state) {
        if (myUnumKey == null) {
            myUnumKey = unumKey;
            myEUnum = new EUnum(this);
            super.initialize(state.sealers, state.targets, (RtTether) myEUnum,
                             true);
            myState = state;
            myPresence = aPresence;
            RtDeflector.construct(myState.kindName, this, myUnumKey);
            setDefaultTarget((RtTether) myEUnum);
        } else {
            throw new UnumSecurityViolationException(
                "UnumRouter initialize called more than once!");
        }
    }

    /**
     * Invalidate this unum router (whatever that means)
     */
    void invalidate() {
        internalIndicateUnumDeath(mySessionKey);
        mySoul = null;
        revoke(myUnumKey);
    }

    /**
     * Kill this unum
     */
    void killUnum() {
        if (myPresence == null) {
            System.err.println(
                "Warning: Attempt to kill Unum that is already invalid");
            return;
        }
        // Presence.invalidate() will call back to our invalidate() method
        myPresence.invalidate();
    }

    /**
     * Create channel/distributor pair for client presence and set up a
     * ClientPresenceTracker.
     *
     * @param presenceKindName  What kind of presence it is
     * @param swissNumber  Unique id for tracker to use
     *
     * XXX CLEANUP: I don't understand this at all
     */
    public ClientPresenceTracker makeClientPresenceTracker(
            String presenceKindname, long swissNumber) {
        EUniDistributor standin_dist = null;
        Presence standin = null;
        EUniChannel temp = new EUniChannel();
        standin_dist = temp.getDistributor();
        standin = (Presence_$_Intf) RtDeflector.construct(presenceKindname,
                                                          temp, temp);
        return new ClientPresenceTracker(swissNumber, standin, standin_dist);
    }

    /**
     * Synchronously make a new incarnation of this unum, but with a different
     * session key.
     *
     * @param sessionKey  The session key for the new unum
     */
    UnumRouter makeNewUnum(Object sessionKey) {
        UnumRouter newUnum = null;
        try {
            newUnum = (UnumRouter) clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Error! Can't clone Unum Router for " + this);
            // XXX Can't happen, but we should "ignore" the ex more gracefully
        }
        newUnum.setSessionKey(sessionKey);
        myPresence.unumMade(newUnum, sessionKey, mySessionKey);
        return newUnum;
    }

    /**
     * Return the session key for this unum.
     */
    Object sessionKey() {
        return mySessionKey;
    }

    /**
     * Change or establish the session key for this unum.
     */
    void setSessionKey(Object key) {
        mySessionKey = key;
    }

    /**
     * Set the facet recouper.
     *
     * @param facetRecouper  The facet recouper to use
     *
     * This is called from the containership ingredient.
     *
     * XXX Note that this is profoundly hardcoded since it is the
     * containership ingredient that puts the unum in the capability table.
     */
    public void setFacetRecouper(jFacetRecouper facetRecouper) {
        if (myFacetRecouper == null) {              
            myFacetRecouper = facetRecouper;
        } else {
            throw new RuntimeException("Cannot set Facet Recouper twice");
        }
    }

    /**
     * Produce a pretty string-ified representation of this router (mainly for
     * debugging purposes)
     */
    public String toString() {
        return super.toString() + "[" + mySessionKey + "]";
    }

    /**
     * Return info about my unum kind. In this case we just return the name.
     */
    Object unumKindInfo() {
        return myState.kindName;
    }


    /*
     * Methods implementing the RtCodeable interface
     */

    /**
     * Return the class name to encode when sending.
     *
     * Just use our real class name -- nothing tricky here. Required as part
     * of the RtCodeable interface.
     */
    public String classNameToEncode(RtEncoder encoder) {
        return(getClass().getName());
    }

    /**
     * Decode an unum from somewhere else by decoding a new presence.
     *
     * Required as part of the RtCodeable interface.
     */
    public Object decode(RtDecoder decoder) throws IOException {
        /* decode() gets called on a non-constructed UnumRouter, so we need to
           set 'myPresence' and then call decodePresence() on *it* */
        myPresence = new PresenceRouter();
        UnumRouter unum = myPresence.decodePresence(decoder, this);
        return unum;
    }

    /**
     * Encode an unum router by encoding a new presence.
     *
     * Required as part of the RtCodeable interface.
     */
    public void encode(RtEncoder encoder) {
        myPresence.encodeOtherPresence(encoder, mySessionKey);
    }


    /*
     * Methods called from myEUnum implementing the Unum einterface
     *
     * XXX CLEANUP: I don't understand any of these
     */

    /**
     * Receive another unum from somebody else (typically from over the
     * network).
     *
     * @param theUnum  unum router for the unum being received
     * @param sessionKey  session key to associate with unum received
     * @param unumKey  our permission to do this
     */
    protected void uReceiveUnum(UnumRouter theUnum, Object sessionKey,
                                Object unumKey) {
        if (myPresence != null) {
            myPresence.unumReceived((Unum)(theUnum.getDeflector()),
                                    sessionKey, unumKey, mySessionKey);
        }
    }

    /**
     * Revoke this unum.
     *
     * @param sessionKey  session key associated with this operation
     */
    protected void uRevokeUnum(Object sessionKey) {
        if (myPresence != null) {
            myPresence.unumRevoked(sessionKey);
        }
    }

    /**
     * Send this unum to somebody else (typically over the network).
     *
     * @param sendTarget  receiver to whom we are to be sent
     * @param sessionKey  session key to associate with unum sent
     * @param unumKey  our permission to do this
     */
    protected void uSendUnum(UnumReceiver sendTarget, Object sessionKey,
                             Object unumKey) {
        if (myPresence != null) {
            myPresence.sendUnum(this, sendTarget, sessionKey, unumKey);
        }
    }


    /**
     * Internal logic for handling the death of an unum: notify all the
     * ingredients.
     *
     * @param sessionKey  session key for presence that created the unum
     */
    private void internalIndicateUnumDeath(Object sessionKey) {
        /* For a client, unum death is the real Death */
        if (myPresence == null) {
            System.err.println("Warning: Attempt to indicate unum death for " +
                               "unum that is already invalid");
            return;
        }

        /* You only want to notify the ingredients of death when the entire
           presence is dead -- i.e. when we are indicating unum death for the
           session that created the presence. */
        Object presenceKey = myPresence.creationKey();
        if (presenceKey == sessionKey ||
                (presenceKey != null && presenceKey.equals(sessionKey))) {
            for (int i = 0; i < myTargets.length; i++) {
                Object target = myTargets[i];
                if (target instanceof UnumKillHandler) {
                    ((UnumKillHandler)target).noteUnumKilled();
                }
            }
        }
    }
}

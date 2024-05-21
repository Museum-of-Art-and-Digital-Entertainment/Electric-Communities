/*
  Unum.java -- The unum itself.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Unum receiver interface for presence spread
 */
public einterface UnumReceiver {
    /**
     * Receive an unum with the given session key and object key
     *
     * @param theUnum  unum router for the unum being received
     * @param sessionKey  session key to associate with unum received
     * @param unumKey  our permission to do this
     */
    uReceiveUnum(UnumRouter unum, Object sessionKey, Object unumKey);
}

/**
 * Unum sender interface for presence spread
 */
public einterface UnumSender {
    /**
     * Send an unum to the designated receiver. Create the unum with
     * 'sessionKey', pass 'unumKey' straight through to uReceiveUnum()
     *
     * @param sendTarget  receiver to whom we are to be sent
     * @param sessionKey  session key to associate with unum sent
     * @param unumKey  our permission to do this
     */
    uSendUnum(UnumReceiver sendTarget, Object sessionKey, Object unumKey);
    
    /**
     * Revoke the Unum you created for this sessionKey
     *
     * @param sessionKey  session key associated with this operation
     */
    uRevokeUnum(Object sessionKey);
}

/**
 * Common interface implemented by all una. Includes both send and recieve.
 */
public einterface Unum extends UnumSender, UnumReceiver {
}

/**
 * This is a facet for the receiver portion of the unum interface (only). It is
 * a facet (so it gets proxy encoded) which wraps a channel (so messages can
 * queue on it).
 */
public eclass eUnumTarget implements jiUnumTarget, UnumReceiver {
    private UnumReceiver myChannel;
    private EResult myChannelDist;
    
    /**
     * Create a new channel and hang on to both ends of it.
     */
    public eUnumTarget() {
        UnumReceiver channel = (UnumReceiver) EUniChannel.construct(UnumReceiver.class);
        EUniDistributor channel_dist = EUniChannel.getDistributor(channel);
        myChannel = channel;
        myChannelDist = channel_dist;
    }
    
    /**
     * Receive an unum by passing it along the channel.
     */
    emethod uReceiveUnum(UnumRouter theUnum, Object vskey, Object unumkey) {
        myChannel <- uReceiveUnum(theUnum, vskey, unumkey);
    }
    
    /**
     * Return the distributor and then forget about it.
     */
    local EResult targetDist() {
        EResult ret = myChannelDist;
        myChannelDist = null; /* null it out so GC can kick in someday */
        return ret;
    }
}

/**
 * Java interface to eUnumTarget so it can be called upon synchronously
 */
public interface jiUnumTarget {
    /**
     * Synchronous call to extract the target's distributor (presumably so
     * somebody can forward it).
     */
    EResult targetDist();
}

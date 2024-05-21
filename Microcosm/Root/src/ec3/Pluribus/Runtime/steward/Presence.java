/*
  Presence.java -- The presence.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Common interface shared by all presences. There's nothing in it; it's just
 * so they'll all be a common type.
 */
public einterface Presence {
}

/**
 * Common interface shared by all host presences.
 */
public einterface PresenceHost {
    /**
     * Note the arrival of a new client presence
     *
     * @param otherPresence  The new presence
     * @param swissNumber  Its secret identity
     */
    newOtherPresence(Presence otherPresence, long swissNumber);

    /**
     * Send yourself (as an unum) to somebody
     *
     * @param target  To whom to send
     * @param sessionKey  The session key for this unum
     * @param unumKey  Permission to do this
     */
    pHostSendUnum(UnumReceiver target, Object sessionKey, Object unumKey);
}

/*
  EPresence.e -- E class which fronts for PresenceRouter

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * E class which fronts for PresenceRouter (which itself is an ordinary Java
 * class).
 */
eclass EPresence implements Presence, PresenceHost {
    public static Trace tr = new Trace("ec.pl.runtime.EPresence");
    private PresenceRouter myPresenceRouter; /* Router we are fronting for */

    /**
     * Create a new E front for a presence router
     *
     * @param aPresence  The presence router to front for
     */
    public EPresence(PresenceRouter aPresence) {
        myPresenceRouter = aPresence;
        if (myPresenceRouter == null) {
            throw new RtRuntimeException(
                "EPresence: constructed with null PresenceRouter");
        }
    }

    /**
     * Note the arrival of a new client presence
     *
     * @param otherPresence  The new presence
     * @param swissNumber  It's secret identity
     */
    emethod newOtherPresence(Presence otherPresence, long swissNumber) {
        myPresenceRouter.newOtherPresence(otherPresence, swissNumber);
    }

    /**
     * Send yourself (as an unum) to somebody
     *
     * @param target  To whom to send
     * @param sessionKey  The session key for this unum
     * @param unumKey  Permission to do this
     */
    emethod pHostSendUnum(UnumReceiver target, Object sessionKey,
                          Object unumKey) {
        myPresenceRouter.pHostSendUnum(target, sessionKey, unumKey);
    }
}

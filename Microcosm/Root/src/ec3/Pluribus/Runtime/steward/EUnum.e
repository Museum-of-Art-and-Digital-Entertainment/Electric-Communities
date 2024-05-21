/*
  EUnum.e -- E class which fronts for UnumRouter

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * E class which fronts for UnumRouter (which itself is an ordinary Java class)
 */
public eclass EUnum implements Unum {
    private UnumRouter myUnumRouter; /* The unum router we are fronting for */

    /**
     * Create a new E front for an unum router
     *
     * @param anUnum  The unum router to front for
     */
    public EUnum(UnumRouter anUnum) {
        myUnumRouter = anUnum;
        if (myUnumRouter == null) {
            throw new RtRuntimeException(
                "EUnum constructed with null UnumRouter");
        }
    }

    /**
     * Receive another unum from somebody else (typically from over the
     * network).
     *
     * @param theUnum  unum router for the unum being received
     * @param sessionKey  session key to associate with unum received
     * @param unumKey  our permission to do this
     */
    emethod uReceiveUnum(UnumRouter theUnum, Object sessionKey,
                         Object unumKey) {
        myUnumRouter.uReceiveUnum(theUnum, sessionKey, unumKey);
    }

    /**
     * Revoke this unum.
     *
     * @param sessionKey  session key associated with this operation
     */
    emethod uRevokeUnum(Object sessionKey) {
        myUnumRouter.uRevokeUnum(sessionKey);
    }

    /**
     * Send this unum to somebody else (typically over the network).
     *
     * @param sendTarget  receiver to whom we are to be sent
     * @param sessionKey  session key to associate with unum sent
     * @param unumKey  our permission to do this
     */
    emethod uSendUnum(UnumReceiver sendTarget, Object sessionKey,
                      Object unumKey) {
        myUnumRouter.uSendUnum(sendTarget, sessionKey, unumKey);
    }
}

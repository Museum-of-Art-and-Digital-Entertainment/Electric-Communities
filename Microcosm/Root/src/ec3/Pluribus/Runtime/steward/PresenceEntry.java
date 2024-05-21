/*
  PresenceEntry.java -- Structs to keep lists of things.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Struct to hold info in the PresenceEnvironment's 'otherPresences' vector
 */
class PresenceEntry {
    Presence presence;
    Object sessionKey;
    PresenceRouter localRevokablePresence;
    
    /**
     * Create a PresenceEntry struct
     *
     * @param p  Presence
     * @param sk  Session key
     * @param lrp  Local revokable presence router
     */
    PresenceEntry(Presence p, Object sk, PresenceRouter lrp) {
        presence = p;
        sessionKey = sk;
        localRevokablePresence = lrp;
    }
    
    /**
     * Produce a pretty string representation of entry
     */
    public String toString() {
        return "PresenceEntry[presence " + presence +
            " skey " + sessionKey +
            " localP " + localRevokablePresence +
            "]";
    }
}

/**
 * Struct to hold info in the PresenceEnvironment's 'otherUna' vector
 */
public class UnumEntry {
    public UnumRouter unum;
    public Object sessionKey;
    public Object parentSessionKey;
    
    /**
     * Create an UnumEntry struct
     *
     * @param u  Unum router
     * @param sk  Session key
     * @param psk  Parent session key
     */
    public UnumEntry(UnumRouter u, Object sk, Object psk) {
        unum = u;
        sessionKey = sk;
        parentSessionKey = psk;
    }
    
    /**
     * Produce a pretty string representation of entry
     */
    public String toString() {
        return "UnumEntry[unum " + unum +
            " sessionKey " + sessionKey +
            " parentSessionKey " + parentSessionKey +
            "]";
    }
}

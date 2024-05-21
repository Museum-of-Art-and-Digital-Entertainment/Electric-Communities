/*
  ClientPresenceTracker.java -- Utility struct to track presences.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Struct class for holding all the info needed to keep track of a client
 * presence of an unum
 */
public class ClientPresenceTracker
{
    private long mySwissNumber;
    private Presence myChannel = null;
    private EUniDistributor myDistributor = null;

    /**
     * Zero-arg constructor is private so nobody can call it.
     */
    private ClientPresenceTracker() {
    }

    /**
     * Create a new struct
     *
     * @param swissNumber  Secret identity of unum
     * @param channel  Channel to client presence
     * @param distributor  Distributor for 'channel'
     */
    public ClientPresenceTracker(long swissNumber, Presence channel,
                                 EUniDistributor distributor) {
        mySwissNumber = swissNumber;
        myChannel = channel;
        myDistributor = distributor;
    }

    /**
     * Return the channel to the client presence
     */
    public Presence getChannel() {
        return myChannel;
    }

    /**
     * Return the distributor for the channel to the client presence
     */
    public EUniDistributor getDistributor() {
        return myDistributor;
    }

    /**
     * Return the Swiss number
     */
    public long getSwissNumber() {
        return mySwissNumber;
    }

    /**
     * Print a pretty representation of what is in here
     */
    public String toString() {
        return super.toString() + ": Swiss Number " + mySwissNumber
            + ", myChannel " + myChannel + ", myDistributor " + myDistributor;
    }
}



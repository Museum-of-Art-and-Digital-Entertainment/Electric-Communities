/*
  UnumSoul.java -- Class to hold onto the true current state of an unum

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.plubar;

import ec.e.run.RtDoNotEncode;

/**
 * Class to hold onto the true current state of an unum.
 *
 * @author Arturo Bejar
 * @version 1.0
 */
public class UnumSoul implements RtDoNotEncode {
    private Unum myUnum;
//KSShack    private Unum myDeflector;
    private SoulState mySoulState;
    
    /**
     * Create a new unum soul for the given unum. Its initial state is null.
     *
     * @param unum  The "real" unum we are keeping the state for
     * @param deflector  A deflector to the unum
     */
    UnumSoul(Unum unum) {
        myUnum = unum;
    }
    
    /**
     * Return the deflector to the unum.
     */
    public Unum getDeflector() {
        return null; //KSShack
    }       
        
    /**
     * Return the current soul state.
     */
    public SoulState getSoulState() {
        return mySoulState;
    }

    /**
     * Kill the unum this is the soul for.
     *
     * XXX CLEANUP: There appears to be an overlap of function here between
     * this method and the identically named method in class 'UnumMaster'. If
     * we can get rid of this method (which looks likely), we can get rid of
     * the 'myUnum' instance variable as well as one of the two constructor
     * parameters.
     */
    public void killUnum() {
        myUnum.killUnum();
    }

    /**
     * Change the soul state.
     *
     * @param state  The new setting for the soul state
     */
    public void setSoulState(SoulState state) {
        mySoulState = state;
    }
}

/*
  UnumPresenceState.java -- Structs to hold state info during unum creation

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Struct to hold the state of an incarnation of a presence instance. Holds
 * onto an array of host ingredients; an array of client ingredients; an array
 * of tethers, which lead to the ingredients that receive presence messages;
 * an array of sealers, which are the sealers for those message; the name of
 * the presence kind this presence implements; the name of the presence kind
 * this presence should send if it spawns a new presences; the name of the unum
 * impl class required for this presence; and a boolean flag indicating
 * whether or not this presence is acting as a host.
 */
public class PresenceState {
    /** The (host) ingredients */
    public Object[] ingredients;
    /** The client ingredients */
    public Object[] clientIngredients;
    /** The ingredients which are the presence message targets */
    public RtTether[] targets;
    /** The sealers for the presence messages */
    public RtSealer[] sealers;
    /** The name of the presence kind */
    public String kindName;
    /** The name of the presence kind of new presences to be spawned */
    public String presenceToMakeName;
    /** The name of the unum impl class */
    public String unumImplClassName;
    /** True=>this is a host presence; false=>this is a client presence */
    public boolean isHost;

    /**
     * Create a default-initialized presence state (no arrays)
     */
    public PresenceState() {
    }
    
    /**
     * Create a presence state with empty arrays
     *
     * @param ingredientCount  Number of (host) ingredients
     * @param clientIngredientCount  Number of client ingredients
     * @param targetCount  Number of target tethers
     * @param sealerCount  Number of sealers
     */
    public PresenceState(int ingredientCount, int clientIngredientCount,
                         int targetCount, int sealerCount) {
        ingredients = new Object[ingredientCount];
        clientIngredients = new Object[clientIngredientCount];
        targets = new RtTether[targetCount];
        sealers = new RtSealer[sealerCount];
    }
}

/**
 * Struct to hold the state of an incarnation of an unum instance. Holds onto
 * an array of tethers, which lead to the ingredients that receive unum
 * messages; an array of sealers, which are the sealers for those messages; and
 * the name of the unum kind this unum implements.
 */
public class UnumState {
    /** The ingredients which are the unum message targets */
    public RtTether[] targets;
    /** The sealers for the unum messages */
    public RtSealer[] sealers;
    /** The name of the unum kind */
    public String kindName;
    
    /**
     * Create a default-initialized unum state (no target or sealer arrays)
     */
    public UnumState() {
    }
    
    /**
     * Create an unum state with empty target and sealer arrays
     *
     * @param targetCount  Number of target ingredients
     * @param sealerCount  Number of sealers
     */
    public UnumState(int targetCount, int sealerCount) {
        targets = new RtTether[targetCount];
        sealers = new RtSealer[sealerCount];
    }
}

/**
 * Struct used in unum initialization. Holds onto a pair of a PresenceState and
 * an UnumState.
 *
 * XXX CLEANUP: This class is only used during unum initialization; it gets
 * initialized, then the initialization is immediately replaced.  We can
 * certainly get rid of the redundant initialization and with a little thought
 * we should be able to get rid of this class entirely.
 */
public class UnumPresenceState {
    /** The unum state */
    public UnumState unum;
    /** The presence state */
    public PresenceState presence;
    
    /**
     * Create a new UnumPresenceState with default-initialized UnumState and
     * PresenceState.
     */
    public UnumPresenceState() {
        unum = new UnumState();
        presence = new PresenceState();
    }
}

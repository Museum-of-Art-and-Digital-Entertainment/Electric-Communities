/*
  Ingredient.java -- The base class of ingredients.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

/**
 * Synchronous Java interface to be implemented by all ingredients
 *
 * XXX CLEANUP: In the fullness of time, when we move to NewE and get rid of
 * eclasses, this merges with 'Ingredient' and much ugliness goes away.
 */
public interface IngredientJif {
    /**
     * Return the state bundle object which should be encoded for this
     * ingredient when we spawn a new client.
     */
    Object jiGetClientState();
    void initGeneric(Object state);
}

/** 
 * Base E class for all ingredients. Provides common services that all
 * ingredients need.
 */
public eclass Ingredient {
    /**
     * The 'environment' variable holds everything the ingredient needs to know
     */
    protected PresenceEnvironment environment;
    
    /**
     * Base ingredient constructor
     *
     * @param theEnvironment  The presence environment for this ingredient
     */
    public Ingredient(PresenceEnvironment theEnvironment) {
        environment = theEnvironment;
    }
    
    /**
     * Kill the presence on this machine.
     */
    protected final void invalidate()  {
        environment.presence.invalidate();
    }

    /**
     * Return the session key for our unum.
     */
    protected final Object sessionKey() {
        return environment.unum.sessionKey();
    }
    
    /**
     * Return info about my unum kind.
     */
    protected final Object unumKindInfo() {
        return environment.unum.unumKindInfo();
    }
    
    /**
     * Synchronously make a new incarnation of this unum.
     *
     * @param vskey  New session key for new incarnation.
     */
    protected final UnumRouter makeNewUnum(Object vskey) {
        return environment.unum.makeNewUnum(vskey);
    }
}

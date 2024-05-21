//
// Change al KSSHackEClass-commented code when changing Ingredient from an
// eclass back to a plain Java Class
//

package ec.plubar;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.tables.IntTable;

/**
 * An unum instance
 *
 * @author Chip Morningstar
 * @author Karl Schumaker
 * @version 1.0
 */
public class Unum {
    /** The definition for this Unum. */
    private UnumDefinition myDefinition;
    /** A table of the actual ingredient Objects for this instance of an
     * Unum. */
    private Ingredient[] myIngredients;
    /** A flag indicating if a (host) Presence of this Unum has been made. */
    private boolean myPresenceIssued;
    /** A flag indicating if Unum is "finished", i.e. all Ingredients have been
     * given initial values.  See finishUnum(). */
    private boolean myCreationMode;

    /**
     * Constructor callable only from within package. The only caller should
     * be the class UnumDefinition. The result is a new unum in creation mode.
     *
     * @param definition  The definition of this unum

     * @see UnumDefinition#createUnum
     */
    /* package */ Unum(UnumDefinition definition) {
        myDefinition = definition;
        myPresenceIssued = false;
        myCreationMode = true;
        if (definition != null) {
            myIngredients = new Ingredient[definition.ingredientCount()];
        } else {
            myIngredients = null;
        }
    }

    /**
     * Terminate unum creation mode. Called by the unum creator to indicate
     * that it has set all ingredients.
     */
    public void finishUnum() {
        myCreationMode = false;
    }

    /**
     * Invoke an unum-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from myDefinition.unumSend()
     *
     * @see UnumDefinition#unumSend
     */
    public void send(String message, Object[] args) throws UnumException {
        myDefinition.unumSend(myIngredients, message, args);
    }

    /**
     * Set one of the ingredients of this unum. Valid for use only when in
     * unum creation mode.  This method is called externally by unum
     * programmers, so the internal method is called with isHost=true for
     * the Ingredient; the decode() method calls the internal setIngredient()
     * directly with isHost=false for creating client presences.
     *
     * @param roleName  The ingredient role name of the ingredient to set
     * @param ingredient  An object to use as the ingredient
     *
     * @exception UnumException thrown from internal setIngredient() method
     *
     * @see Unum#decode()
     * @see Unum#setIngredient()
     */
    public void setIngredient(String roleName, Ingredient ingredient) throws UnumException {
        setIngredient(roleName, ingredient, true);
    }

    /**
     * Set one of the ingredients of this unum. Valid for use only when in
     * unum creation mode.
     *
     * @param roleName  The ingredient role name of the ingredient to set
     * @param ingredient  An object to use as the ingredient
     *
     * @exception UnumException if this unum is not in creation mode, i.e. if
     *   finishUnum() has been called
     * @exception UnumException if this unum has no ingredients, i.e. it was
     *   initialized with an UnumDefinition without ingredient roles
     */
    private void setIngredient(String roleName, Ingredient ingredient,
                              boolean isHost) throws UnumException {
        if (!myCreationMode) {
            throw new UnumException("setIngredient: unum creation closed");
        }
        if (myIngredients != null) {
            myIngredients[myDefinition.findIngredient(roleName)] = ingredient;
            //KSSHack Is there a reason for null ingredients?
            if (ingredient != null) {
                //KSSHackEClass Only cast until we can make Ingredient a plain Java Class again
                //KSSHackEClass ingredient.setUnum(this);
                ((IngredientJif)(ingredient)).setUnum(this);
                //KSSHack bogus call; see Ingredient.setName()
                //KSSHackEClass Only cast until we can make Ingredient a plain Java Class again
                //KSSHackEClass ingredient.setName(roleName);
                ((IngredientJif)(ingredient)).setName(roleName);
            } else {
                //KSSHack Just spam or should we keep this?
                System.out.println("setIngredient: setting ingredient " + roleName +
                                   " to null");
            }
        } else {
            throw new UnumException("setIngredient: no ingredients in unum");
        }
    }

    /**
     * Produce a presence of this unum for the first person to ask for one
     *
     * @param isHost  true=>create a host presence, false=>create a client
     *
     * @exception UnumException if myPresenceIssued is false, i.e. if
     * takePresence has already been called and a Presence has been issued
     */
    public Presence takePresence(boolean isHost) throws UnumException {
        if (myPresenceIssued) {
            throw new UnumException("takePresence: presence already issued");
        }
        myPresenceIssued = true;
        return new Presence(this, isHost);
    }

    /**
     * Return the object assigned to a particular ingredient role. Callable
     * only from within package; should only be called by the Ingredient base
     * class.
     *
     * @param neighbor  The name of the ingredient role of interest
     *
     * @return  The object in that role, or null if there is no such object
     *
     * @exception UnumException from myDefinition.findIngredient()
     * @exception UnumException if there are no ingredients in the unum
     *
     * @see UnumDefinition#findIngredient
     */
    /* package */ Ingredient getNeighbor(String neighbor) throws UnumException {
        if (myIngredients != null) {
            return myIngredients[myDefinition.findIngredient(neighbor)];
        } else {
            throw new UnumException("getNeighbor: no ingredients in unum");
        }
    }

    /**
     * Invoke a presence-level method. Calls UnumDefinition.presenceSend().
     * Callable only from within the package.  Should only be called by
     * the 'send' method of the Presence class.
     *
     * @param isHost  true=>deliver host message, false=>deliver client message
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from myDefinition.presenceSend()
     *
     * @see UnumDefinition#presenceSend
     * @see Presence#send
     */
    /* package */ void presenceSend(boolean isHost,
                                    String message,
                                    Object[] args)
                                        throws UnumException {
        myDefinition.presenceSend(isHost, myIngredients, message, args);
    }

    //KSS added

    /**
     * Initialize this unum.
     *
     * @param soulState  The soulState of the Unum which contians the
     * ingredients' states.
     */
    public void init(SoulState soulState) {
        if (soulState != null) {
            Hashtable ingredientRoles = myDefinition.getIngredientRoles();
            Enumeration names = ingredientRoles.keys();
            Enumeration entries = ingredientRoles.elements();
            String name = null;
            UnumIngredientEntry entry = null;
            istBase state = null;

            while (entries.hasMoreElements() && names.hasMoreElements()) {
                name = (String)names.nextElement();
                entry = (UnumIngredientEntry)entries.nextElement();
                try {
                    if (myIngredients != null) {
                        //KSSHackEClass Put this back when we make Ingredient a Java
                        //KSSHackEClass Class and get rid of IngredientJif
                        //KSSHackEClass myIngredients[entry.index].init(state);
                        ((IngredientJif)myIngredients[entry.index]).init(state);
                    } else {
                        throw new UnumException("init: no ingredients in unum");
                    }
                } catch (UnumException exc) {
                    //KSSHack This should never happen, but what if it does?
                }
            }
        } else {
            //KSSHack Just spam or should we keep this?
            System.out.println("Initializing Unum with null SOulState");
        }
    }

    /**
     * Create a SoulState based on encodeable ingredients
     *
     * @return  A SoulState containing the states of all this Unum's
     * Ingredients.
     */
    /* package */ SoulState createClientSoulState() {
        SoulState soulState = new SoulState();
        Hashtable ingredientRoles = myDefinition.getIngredientRoles();
        Enumeration names = ingredientRoles.keys();
        Enumeration entries = ingredientRoles.elements();
        String name = null;
        UnumIngredientEntry entry = null;

        while (entries.hasMoreElements() && names.hasMoreElements()) {
            name = (String)names.nextElement();
            entry = (UnumIngredientEntry)entries.nextElement();
            try {
                if (myIngredients != null) {
                    //KSSHackEClass Put this back when we make Ingredient a Java
                    //KSSHackEClass Class and get rid of IngredientJif
                    //KSSHackEClass soulState.put(name,
                    //KSSHackEClass               myIngredients[entry.index].getClientState());
                    soulState.put(name,
                                  ((IngredientJif)myIngredients[entry.index]).getClientState());
                } else {
                    throw new UnumException("createClientSoulState: no ingredients in unum to get state");
                }
            } catch (UnumException exc) {
                //KSSHack This should never happen, but what if it does?
            }
        }
        return soulState;
    }


    /**
     * Creates an array of ingredient role/class name pairs; this is a package
     * protected method that is called from encode().
     *
     * @return An array of Ingredient role name/class name pairs.
     *
     * @see Unum#encode
     */
    /* package */ IngredientRoleClassEntry[] createRoleClassList() {
        Hashtable ingredientRoles = myDefinition.getIngredientRoles();
        IngredientRoleClassEntry[] pairs =
              new IngredientRoleClassEntry[ingredientRoles.size()];
        Enumeration names = ingredientRoles.keys();
        Enumeration entries = ingredientRoles.elements();
        String name = null;
        UnumIngredientEntry entry = null;

        while (entries.hasMoreElements() && names.hasMoreElements()) {
            name = (String)names.nextElement();
            entry = (UnumIngredientEntry)entries.nextElement();
            try {
                if (myIngredients != null) {
                    //KSSHackEClass pairs[entry.index] = new IngredientRoleClassEntry(name,
                    //KSSHackEClass                     myIngredients[entry.index].getClass().toString());
                    pairs[entry.index] = new IngredientRoleClassEntry(name,
                                        ((IngredientJif)(myIngredients[entry.index])).getClassName());
                } else {
                        throw new UnumException("createRoleClassList: no ingredients in unum");
                }
            } catch (UnumException exc) {
                //KSSHack This should never happen, but what if it does?
            }
        }
        return pairs;
    }

    /**
     * Kill this unum.  This is here because an UnumSoul kills its Unum from
     * Pluribus days of yore.  This needs to be revisited to see if we still
     * need it.
     */
    //KSSHack Do we really need this?  It's here because of SoulState...
    void killUnum() {
        System.err.println("killUnum() not implemented in Plubar...");
    }

    /**
     * Encode this unum by encoding its UnumDefinition, a list of its
     * Ingredients' role and class names and a SoulState containing the
     * state bundles of its Ingredients.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an encoding error occurs
     *
     * @see UnumDefinition#encode
     * @see Unum#createRoleClassList
     * @see Unum#createClientSoulState
     */
    public void encode(RtEncoder encoder) throws UnumException {
        try {
            myDefinition.encode(encoder);
            encoder.encodeObject(createRoleClassList());
            encoder.encodeObject(createClientSoulState());
        } catch (IOException exc) {
            throw new UnumException("error encoding UnumDefinition");
        }
    }

    /**
     * Decode an unum.  First decode an UnumDefinition.  Then decode an array
     * of IngredientRoleClassEntries and use this array to fill up
     * myIngredients with role names and initialize them with new instances
     * of the named Classes.  Finally, initialize this new unum by decoding
     * a SoulState and calling init().
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an encoding error occurs; the following
     * exceptions are intercepted and rethrown as UnumExceptions with
     * appropriate messages: IOException, ClassNotFoundException,
     * IllegalAccessException, InstantiationException.
     *
     * @see UnumDefinition#decode
     * @see Unum#init
     */
    public void decode(RtDecoder decoder) throws UnumException {
        String saveName = null;
        try {
            myDefinition = (UnumDefinition)decoder.decodeObject();
            IngredientRoleClassEntry[] pairs = (IngredientRoleClassEntry[])decoder.decodeObject();
            myIngredients = new Ingredient[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                saveName = pairs[i].className;
                Ingredient ingr = (Ingredient)(Class.forName(pairs[i].className).newInstance());
                //KSSHackEClass ingr.myAmHost = false;
                ((IngredientJif)ingr).setHost(false);
                this.setIngredient(pairs[i].roleName, ingr, false);
            }
            this.init((SoulState)decoder.decodeObject());
            myPresenceIssued = false;
            // We should be finished creating the unum now, so...
            myCreationMode = false;
            this.finishUnum();
        } catch (IOException ioExc) {
            throw new UnumException("error decoding UnumDefinition");
        } catch (ClassNotFoundException cExc) {
            throw new UnumException("unknown class " + saveName +
              " when decoding UnumDefinition");
        } catch (IllegalAccessException illExc) {
            throw new UnumException("illegal access for class " + saveName +
              " when decoding UnumDefinition");
        } catch (InstantiationException instExc) {
            throw new UnumException("illegal instantiation for class " + saveName +
              " when decoding UnumDefinition");
        }
    }

    /**
     * Print out this unum.  Mostly bogus for testing purposes.
     *
     * @return Some verbage and myDefinition as a String, along with the
     * booleans myPresenceIssued and myCreationMode.
     */
    public String toString() {
        return (" Unum Printout:\n" +
                "--------------------------------------------------\n" +
                myDefinition +
                "myPresenceIssued=" + myPresenceIssued +
                " myCreationMode=" + myCreationMode + "\n" +
                "--------------------------------------------------");
    }
}

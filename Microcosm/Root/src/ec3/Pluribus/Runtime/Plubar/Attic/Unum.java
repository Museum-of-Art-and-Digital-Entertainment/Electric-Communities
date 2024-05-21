package ec.pl.runtime;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.io.IOException;
import java.util.NoSuchElementException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.tables.IntTable;

/**
 * An unum instance
 */
public class Unum {
    private UnumDefinition myDefinition;
    private Ingredient[] myIngredients;
    private boolean myPresenceIssued;
    private boolean myCreationMode;

    /**
     * Constructor callable only from within package. The only caller should
     * be the class UnumDefinition. The result is a new unum in creation mode.
     *
     * @param definition  The definition of this unum
     * @param ingredientCount  Number of ingredients that it will have
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
     * @exception UnumException from unumSend()
     */
    public void send(String message, Object[] args) throws UnumException {
        myDefinition.unumSend(myIngredients, message, args);
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
    public void setIngredient(String roleName, Ingredient ingredient) throws UnumException {
        if (!myCreationMode) {
            throw new UnumException("setIngredient: unum creation closed");
        }
        if (myIngredients != null) {
            myIngredients[myDefinition.findIngredient(roleName)] = ingredient;
            ingredient.setUnum(this);
            //KSSHack bogus call; see Ingredient.setName()
            ingredient.setName(roleName);
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
        if (myPresenceIssued)
            throw new UnumException("takePresence: presence already issued");
        myPresenceIssued = true;
        return new Presence(this, isHost);
    }

    /**
     * Return the object assigned to a particular ingredient role. Callable
     * only from within package; should only be called by the Ingredient base
     * class.
     *
     * @param neighbor  The name of the ingredient role of interest
     * @returns  The object in that role, or null if there is not such object
     *
     * @exception UnumException from myDefinition.findIngredient()
     * @exception UnumException if there are no ingredients in the unum
     * @see setIngredient
     */
    /* package */ Ingredient getNeighbor(String neighbor) throws UnumException {
        if (myIngredients != null) {
            return myIngredients[myDefinition.findIngredient(neighbor)];
        } else {
            throw new UnumException("getNeighbor: no ingredients in unum");
        }
    }

    /**
     * Invoke a presence-level method. Callable only from within the package.
     * Should only be called by the 'send' method of the Presence class.
     *
     * @param isHost  true=>deliver host message, false=>deliver client message
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from myDefinition.presenceSend()
     */
    /* package */ void presenceSend(boolean isHost,
                                    String message, Object[] args)
                                      throws UnumException {
        myDefinition.presenceSend(isHost, myIngredients, message, args);
    }

    //KSS added

    /**
     * Initialize this unum.
     *
     * @param soulState  The soulState to get the ingredients' states from
     */
    public void init(SoulState soulState) {
        UnumIngredientEntry[] ingredientRoles = myDefinition.getIngredientRoles();
        istBase state = null;
        for (int i=0; i<ingredientRoles.length; ++i) {
            if (soulState != null) {
                state = soulState.get(ingredientRoles[i].name);
            }
            try {
                int index = myDefinition.findIngredient(ingredientRoles[i].name);
                if (myIngredients != null) {
                    myIngredients[index].init(state);
                } else {
                    throw new UnumException("init: no ingredients in unum");
                }
            } catch (UnumException exc) {
                //KSSHack This should never happen, but what if it does?
            }
        }
    }

    /**
     * Create a SoulState based on encodeable ingredients
     */
    public SoulState createClientSoulState() {
        SoulState soulState = new SoulState();
        UnumIngredientEntry[] ingredientRoles = myDefinition.getIngredientRoles();

        for (int i=0; i<ingredientRoles.length; ++i) {
            if (ingredientRoles[i].encodeState) {
                try {
                    int index = myDefinition.findIngredient(ingredientRoles[i].name);
                    if (myIngredients != null) {
                        soulState.put(ingredientRoles[i].name,
                                      myIngredients[index].getClientState());
                    } else {
                        throw new UnumException("createClientSoulState: no ingredients in unum to get state");
                    }
                } catch (UnumException exc) {
                    //KSSHack This should never happen, but what if it does?
                }
            }
        }
        return soulState;
    }


    /**
     * Create an array of ingredient role/class name pairs
     */
    public IngredientRoleClassEntry[] createRoleClassList() {
        UnumIngredientEntry[] ingredientRoles = myDefinition.getIngredientRoles();
        IngredientRoleClassEntry[] pairs =
              new IngredientRoleClassEntry[ingredientRoles.length];

        for (int i=0; i<ingredientRoles.length; ++i) {
            try {
                int index = myDefinition.findIngredient(ingredientRoles[i].name);
                if (myIngredients != null) {
                    pairs[i] = new IngredientRoleClassEntry(ingredientRoles[i].name,
                                        myIngredients[index].getClass().toString());
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
     * Encode this unum.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an encoding error occurs
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
     * Decode this unum.
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an encoding error occurs
     */
    public void decode(RtDecoder decoder) throws UnumException {
        String saveName = null;
        try {
            myDefinition = (UnumDefinition)decoder.decodeObject();
            IngredientRoleClassEntry[] pairs = (IngredientRoleClassEntry[])decoder.decodeObject();
            myIngredients = new Ingredient[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                saveName = pairs[i].className;
                this.setIngredient(pairs[i].roleName,
                    ((Ingredient)(Class.forName(pairs[i].className).newInstance())));
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

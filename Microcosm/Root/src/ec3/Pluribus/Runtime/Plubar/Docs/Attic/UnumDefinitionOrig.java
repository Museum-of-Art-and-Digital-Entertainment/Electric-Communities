package ec.pl.runtime;

/**
 * The description of a particular variety of unum. Assumes a host/client
 * presence model and a fixed set of ingredient roles, but omits the actual
 * choice of implementations for the ingredients themselves.
 */
public class UnumDefinition {
    private String[] myIngredientRoles;
    private String[] myUnumMessages;
    private int[] myUnumMessageIndices;
    private String[] myHostPresenceMessages;
    private int[] myHostPresenceMessageIndices;
    private String[] myClientPresenceMessages;
    private int[] myClientPresenceMessageIndices;
    private boolean myCreationMode;

    /**
     * Construct a new unum definition. The result will still be in creation
     * mode.
     *
     * @param ingredientRoleCount  The number of ingredient roles it will have
     * @param unumMessageCount  The number of messages in the unum interface
     * @param hostPresenceMessageCount  The number of messages in the host-side
     *  presence interface
     * @param clientPresenceMessageCount  The number of messages in the
     *  client-side presence interface
     */
    public UnumDefinition(int ingredientRoleCount,
                          int unumMessageCount,
                          int hostPresenceMessageCount,
                          int clientPresenceMessageCount) {
        myIngredientRoles = new String[ingredientRoleCount];
        myUnumMessages = new String[unumMessageCount];
        myUnumMessageIndices = new int[unumMessageCount];
        myHostPresenceMessages = new String[hostPresenceMessageCount];
        myHostPresenceMessageIndices = new int[hostPresenceMessageCount];
        myClientPresenceMessages = new String[clientPresenceMessageCount];
        myClientPresenceMessageIndices = new int[clientPresenceMessageCount];
        myCreationMode = true;
    }

    /**
     * Declare a message in the client-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addClientPresenceMessage(String message, String ingredient) {
        addMessage(message, ingredient,
                  myClientPresenceMessages, myClientPresenceMessageIndices);
    }

    /**
     * Declare a message in the host-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addHostPresenceMessage(String message, String ingredient) {
        addMessage(message, ingredient,
                  myHostPresenceMessages, myHostPresenceMessageIndices);
    }

    /**
     * Declare the role name of one of the ingredients
     *
     * @param ingredientRole  The role name of the ingredient to add
     */
    public void addIngredientRole(String ingredientRole) {
        ensureOpenDefinition();
        myIngredientRoles[findAvailable(myIngredientRoles)] = ingredientRole;
    }

    /**
     * Declare a message in the unum presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addUnumMessage(String message, String ingredient) {
        addMessage(message, ingredient,
                  myUnumMessages, myUnumMessageIndices);
    }

    /**
     * Create a new unum that matches this definition. The newly created
     * unum will itself be returned in create mode and will not have any
     * ingredients associated with it.
     */
    public Unum createUnum() {
        return new Unum(this, myIngredientRoles.length);
    }

    /**
     * Terminate creation mode. Called by the unum definition creator to
     * indicate that it has added all the ingredient roles and messages.
     */
    public void finishDefinition() {
        myCreationMode = false;
    }

    /**
     * Package-use method to determine the integer index of a particular
     * ingredient, indentified by role name.
     *
     * @param ingredient  The role name of the ingredient desired
     * @returns  The index of said ingredient in the ingredient roles array
     */
    /* package */ int findIngredient(String ingredient) {
        for (int i=0; i<myIngredientRoles.length; ++i) {
            if (ingredient == myIngredientRoles[i]) {
                return i;
            }
        }
        throw new UnumException("no ingredient " + ingredient);
    }

    /**
     * Deliver a message to an ingredient via one of the presence interfaces.
     * Callable only from within the package. The only caller should be the
     * 'send' method of the Presence class.
     *
     * @param isHost  true=>deliver a host message, false=>a client message
     * @param ingredients  An array of the actual ingredients
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    /* package */ void presenceSend(boolean isHost, Ingredient[] ingredients,
                                    String message, Object[] args) {
        if (isHost) {
            for (int i=0; i<myHostPresenceMessages.length; ++i) {
                if (myHostPresenceMessages[i] == message) {
                    ERun.deliver(ingredients[myHostPresenceMessageIndices[i]],
                                 message, args);
                    return;
                }
            }
        } else {
            for (int i=0; i<myClientPresenceMessages.length; ++i) {
                if (myClientPresenceMessages[i] == message) {
                    ERun.deliver(ingredients[myClientPresenceMessageIndices[i]],
                                 message, args);
                    return;
                }
            }
        }
        throw new UnumException("message not understood");
    }

    /**
     * Deliver a message to an ingredient via the unum interface. Callable only
     * from within the package. The only caller should be the 'send' method
     * of the Unum class
     *
     * @param ingredients  An array of the actual ingredients
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    /* package */ void unumSend(Ingredient[] ingredients,
                                String message, Object[] args) {
        for (int i=0; i<myUnumMessages.length; ++i) {
            if (myUnumMessages[i] == message) {
                ERun.deliver(ingredients[myUnumMessageIndices[i]],
                             message, args);
                return;
            }
        }
        throw new UnumException("message not understood");
    }
        
    /**
     * Add a new message to one of the several message arrays
     *
     * @param message  The message to add
     * @param ingredient  The role name of the ingredient to deliver it to
     * @param messagesArray  The message array to add the message to
     * @param indicesArray  The indices array for mapping the message to an
     *  ingredient
     */
    private void addMessage(String message, String ingredient,
                           String[] messagesArray, int[] indicesArray) {
        ensureOpenDefinition();
        int i = findAvailable(messagesArray);
        int ingredientNumber = findIngredient(ingredient);
        messagesArray[i] = message;
        indicesArray[i] = ingredientNumber;
    }

    /**
     * Throw an exception if we are not in creation mode
     */
    private void ensureOpenDefinition() {
        if (!myCreationMode) {
            throw new UnumException("unum definition closed");
        }
    }

    /**
     * Search an array for the first null element
     *
     * @param array  The array to search
     * @returns  The index of the first element of array whose value is null
     */
    private int findAvailable(Object[] array) {
        for (int i=0; i<array.length; ++i) {
            if (array[i] == null) {
                return i;
            }
        }
        throw new UnumException("unum definition full");
    }
}

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
    /* package */ Unum(UnumDefinition definition, int ingredientCount) {
        myDefinition = definition;
        myIngredients = new Ingredient[ingredientCount];
        myPresenceIssued = false;
        myCreationMode = true;
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
     */
    public void send(String message, Object[] args) {
        myDefinition.unumSend(myIngredients, message, args);
    }

    /**
     * Set one of the ingredients of this unum. Valid for use only when in
     * unum creation mode.
     *
     * @param roleName  The ingredient role name of the ingredient to set
     * @param ingredient  An object to use as the ingredient
     */
    public void setIngredient(String roleName, Ingredient ingredient) {
        if (!myCreationMode)
            throw new UnumException("unum creation closed");
        myIngredients[myDefinition.findIngredient(roleName)] = ingredient;
        ingredient.setUnum(this);
    }

    /**
     * Produce a presence of this unum for the first person to ask for one
     *
     * @param isHost  true=>create a host presence, false=>create a client
     */
    public Presence takePresence(boolean isHost) {
        if (myPresenceIssued)
            throw new UnumException("presence already issued");
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
     */
    /* package */ Ingredient getNeighbor(String neighbor) {
        return myIngredients[myDefinition.findIngredient(neighbor)];
    }

    /**
     * Invoke a presence-level method. Callable only from within the package.
     * Should only be called by the 'send' method of the Presence class.
     *
     * @param isHost  true=>deliver host message, false=>deliver client message
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    /* package */ void presenceSend(boolean isHost,
                                    String message, Object[] args) {
        myDefinition.presenceSend(isHost, myIngredients, message, args);
    }
}

/**
 * A presence of an unum
 */
public class Presence {
    private Unum myUnum;
    private boolean amHost;

    /**
     * Constructor callable only from within package. The only caller should be
     * the class Unum. The result is a new presence of a given unum.
     *
     * @param unum  The unum we are to be a presence of
     * @param isHost  true=>create a host presence, false=>create a client
     */
    /* package */ Presence(Unum unum, boolean isHost) {
        myUnum = unum;
        amHost = isHost;
    }

    /**
     * Invoke a presence-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    public void send(String message, Object[] args) {
        myUnum.presenceSend(amHost, message, args);
    }
}

/**
 * The base class for all ingredients
 */
abstract public class Ingredient {
    protected Unum myUnum = null;

    /**
     * Tell this ingredient what unum it is a part of. Callable only from
     * within the package. The only caller should be the 'setIngredient'
     * method of class Unum.
     */
    /* package */ void setUnum(Unum unum) {
        if (myUnum != null)
            throw new UnumException("multiple setUnum calls");
        myUnum = unum;
    }

    /**
     * Return a neighbor ingredient
     *
     * @param neighbor  The name of the ingredient role of interest
     * @returns  The object in that role, or null if there is not such object
     */
    protected Ingredient getNeighbor(String neighbor) {
        return myUnum.getNeighbor(neighbor);
    }

    /**
     * Test if a particular neighbor exists
     *
     * @param neighbor  The name of the ingredient role of the neighbor
     * @returns  true iff an ingredient has been assigned to that role
     */
    protected boolean haveNeighbor(String neighbor) {
        return myUnum.getNeighbor(neighbor) != null;
    }

    /**
     * Deliver a message to one of the ingredient's neighbors.
     *
     * @param neighbor  Role name of neighbor to deliver message to
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    protected void neighborSend(String neighbor,
                                String message, Object[] args) {
        ERun.send(myUnum.getNeighbor(neighbor), message, args);
    }
}


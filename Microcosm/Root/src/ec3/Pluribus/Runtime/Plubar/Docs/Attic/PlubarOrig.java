/**
 * The description of a particular variety of unum. Assumes a host/client
 * presence model and a fixed set of ingredient roles, but omits the actual
 * choice of implementations for the ingredients themselves.
 */
public class UnumDefinition {
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
                          int clientPresenceMessageCount);
    /**
     * Declare a message in the client-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addClientPresenceMessage(String message, String ingredient);

    /**
     * Declare a message in the host-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addHostPresenceMessage(String message, String ingredient);

    /**
     * Declare the role name of one of the ingredients
     *
     * @param ingredientRole  The role name of the ingredient to add
     */
    public void addIngredientRole(String ingredientRole);

    /**
     * Declare a message in the unum presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     */
    public void addUnumMessage(String message, String ingredient);

    /**
     * Create a new unum that matches this definition. The newly created
     * unum will itself be returned in create mode and will not have any
     * ingredients associated with it.
     */
    public Unum createUnum();

    /**
     * Terminate creation mode. Called by the unum definition creator to
     * indicate that it has added all the ingredient roles and messages.
     */
    public void finishDefinition();
}

/**
 * An unum instance
 */
public class Unum {
    /**
     * Terminate unum creation mode. Called by the unum creator to indicate
     * that it has set all ingredients.
     */
    public void finishUnum();

    /**
     * Invoke an unum-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    public void send(String message, Object[] args);

    /**
     * Set one of the ingredients of this unum. Valid for use only when in
     * unum creation mode.
     *
     * @param roleName  The ingredient role name of the ingredient to set
     * @param ingredient  An object to use as the ingredient
     */
    public void setIngredient(String roleName, Ingredient ingredient);

    /**
     * Produce a presence of this unum for the first person to ask for one
     *
     * @param isHost  true=>create a host presence, false=>create a client
     */
    public Presence takePresence(boolean isHost);
}

/**
 * A presence of an unum
 */
public class Presence {
    /**
     * Invoke a presence-level method
     *
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    public void send(String message, Object[] args);
}

/**
 * The base class for all ingredients
 */
abstract public class Ingredient {
    protected Unum myUnum = null;

    /**
     * Return a neighbor ingredient
     *
     * @param neighbor  The name of the ingredient role of interest
     * @returns  The object in that role, or null if there is not such object
     */
    protected Ingredient getNeighbor(String neighbor);

    /**
     * Test if a particular neighbor exists
     *
     * @param neighbor  The name of the ingredient role of the neighbor
     * @returns  true iff an ingredient has been assigned to that role
     */
    protected boolean haveNeighbor(String neighbor);

    /**
     * Deliver a message to one of the ingredient's neighbors.
     *
     * @param neighbor  Role name of neighbor to deliver message to
     * @param message  Message to deliver
     * @param args  Message arguments
     */
    protected void neighborSend(String neighbor,
                                String message, Object[] args);
}


class Example {
    static void surfaceFloor3DUnumExample() {
        UnumDefinition def = new UnumDefinition(12, 12, 10, 5);
        
        def.addIngredientRole("behaviorManager");
        def.addIngredientRole("cloneable");
        def.addIngredientRole("compositable");
        def.addIngredientRole("containership");
        def.addIngredientRole("describer");
        def.addIngredientRole("destination");
        def.addIngredientRole("interface");
        def.addIngredientRole("modifier");
        def.addIngredientRole("portable");
        def.addIngredientRole("property");
        def.addIngredientRole("putable");
        def.addIngredientRole("verbManager");
        
        def.addUnumMessage("uShortDescribe",                  "describer");
        def.addUnumMessage("uRequestTransfer",                "containership");
        def.addUnumMessage("uAddUnum",                        "containership");
        def.addUnumMessage("uOpen",                           "containership");
        def.addUnumMessage("uClose",                          "containership");
        def.addUnumMessage("uLocalSetCompositorPresenter",    "compositable");
        def.addUnumMessage("uLocalQueryUnumUIUpdater",        "compositable");
        def.addUnumMessage("uLocalChildrenChanged",           "compositable");
        def.addUnumMessage("uLocalParentChanged",             "compositable");
        def.addUnumMessage("uRequestMoveTo",                  "compositable");
        def.addUnumMessage("uGetIcon",                        "compositable");
        def.addUnumMessage("uGetDescription",                 "compositable");
        def.addUnumMessage("uPropertySheet",                  "property");
        
        def.addHostPresenceMessage("pHostSendToContainer",    "containership");
        def.addHostPresenceMessage("pHostSendToContainerUnum","containership");
        def.addHostPresenceMessage("pHostFwdToContainerUnum", "containership");
        def.addHostPresenceMessage("pHostSendToRootSupCb",    "containership");
        def.addHostPresenceMessage("pHostRequestTransfer",    "containership");
        def.addHostPresenceMessage("pHostSendToContainedUnum","containership");
        def.addHostPresenceMessage("pHostFwdToContainedUnum", "containership");
        def.addHostPresenceMessage("pHostAddUnum",            "containership");
        def.addHostPresenceMessage("pHostOpen",               "containership");
        def.addHostPresenceMessage("pHostClose",              "containership");
        
        def.addClientPresenceMessage("pClientPerformGesture", "compositable");
        def.addClientPresenceMessage("pClientSetMood",        "compositable");
        def.addClientPresenceMessage("pClientSetOrientation", "compositable");
        def.addClientPresenceMessage("pClientSetScale",       "compositable");
        def.addClientPresenceMessage("pClientTeleportFade",   "compositable");
        
        def.finishDefinition();

        Unum unum = def.createUnum();
        
        unum.setIngredient("behaviorManager", new iiECBehaviorManager(...));
        unum.setIngredient("cloneable",       new iiCloneable(...));
        unum.setIngredient("compositable",    new iiECCompositable(...));
        unum.setIngredient("containership",   new iiECContainership(...));
        unum.setIngredient("describer",       new iiDescribeWithLink(...));
        unum.setIngredient("destination",     new iiWalkToFloor3D(...));
        unum.setIngredient("interface",       new iiPropInterface(...));
        unum.setIngredient("modifier",        new iiSimpleModifier(...));
        unum.setIngredient("portable",        new iiNotPortable(...));
        unum.setIngredient("property",        new iiECProperty(...));
        unum.setIngredient("putable",         new iiFloor3DPutAt(...));
        unum.setIngredient("verbManager",     new iiECVerbManager(...));
        
        unum.finishUnum();
    }
}


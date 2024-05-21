public class UnumDefinition {
    /** Define an unum
     *  @param primePresenceName    name of the prime/host presence
     *  @param presenceMade         name of the presence the prime makes; can be null
     *  @param ingredientRoleCount  how many ingredients in the unum
     */
    public UnumDefinition(String primePresenceName, String presenceMade,
                          int ingredientRoleCount);
    /** Add a presence role to an unum
     *  @param roleName     name of the new presence
     *  @param presenceMade name of the presence this one makes; can be null
     *  @exception UnumException when role already exists
     */
    public void addPresenceRole(String roleName, String presenceMade);
    /** Add a message to an unum for a specific presence interface
     *  @param presenceRole name of the intended presence
     *  @param message      name of the message
     *  @param ingredient   name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if presenceRole does not exist
     *  @exception UnumException if ingredient does not exist
     */
    public void addPresenceMessage(String presenceRole, String message,
                                   String ingredient);
    /** Add a message to an unum (for all presence interfaces)
     *  @param message      name of the message
     *  @param ingredient   name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if ingredient does not exist
     */
    public void addPresenceMessage(String message, String ingredient);
    /** Add an interface to an unum (for all presence interfaces)
     *  @param interfaceName the interface to deliver
     *  @param ingredient    name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if ingredient does not exist
     */
    public void addPresenceInterface(Class interfaceName, String ingredient);
    /** Add an ingredient role to an unum
     *  @param ingredientRole name of the ingredient role
     *  @param toEncode       encode this ingredient when encoding the unum
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if there's not room for another ingredient
     */
    public void addIngredientRole(String ingredientRole, boolean toEncode);
    /** Add a message to an unum (for all unum interfaces)
     *  @param message      name of the message
     *  @param ingredient   name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if presenceRole does not exist
     *  @exception UnumException if ingredient does not exist
     */
    public void addUnumMessage(String presenceRole, String message,
                               String ingredient);
    /** Add an interface to an unum
     *  @param interfaceName the interface to deliver
     *  @param ingredient    name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if ingredient does not exist
     */
    public void addUnumInterface(Class interfaceName, String ingredient);
    /** Add a message to an unum for a specific role; this allows the
     * creation of una with different interfaces, i.e. a "host" unum can
     * create a "client" unum with a different/restricted interface--Instant
     * facets :)
     *  @param presenceRole name of the intended presence
     *  @param message      name of the message
     *  @param ingredient   name of the target ingredient
     *  @exception UnumException if unum definition is finished
     *  @exception UnumException if ingredient does not exist
     */
    public void addUnumMessage(String message, String ingredient);
    /**
     * Create a new unum that matches this definition. The newly created
     * unum will itself be returned in create mode and will not have any
     * ingredients associated with it.
     *  @return the created Unum
     */
    public Unum createUnum();
    /**
     * Terminate creation mode. Called by the unum definition creator to
     * indicate that it has added all the ingredient roles and messages.
     * Also checks to see that all "made" presences are defined (and any
     * other semantic checks we come up with).
     */
    public void finishDefinition();
}

public class ExampleUnum {
    static Unum createUnum() {
        // The basic "host" Unum creates a "client" when encoded
        UnumDefinition def = new UnumDefinition("host", "client", 2);
        Unum unum = null;
                
        try {
            def.addPresenceRole("client", "client");
            def.addIngredientRole("color", true);
            def.addIngredientRole("shape", true);
            // All ExampleUna can receive this message
            def.addUnumMessage("uSetColor", "color");
            // You can't change the shape of a "client" ExampleUnum
            def.addUnumMessage("host", "uSetShape", "shape");
            def.addPresenceMessage("host", "pHostSetColor", "color");
            def.addPresenceMessage("host", "pHostSetShape", "shape");        
            def.addPresenceMessage("client", "pClientSetColor", "color");
            def.addPresenceMessage("client", "pClientSetShape", "shape");
            def.finishDefinition();

            unum = def.createUnum();
            // The new PresenceEnvironment should be cleaner than the current
            PresenceEnvironment env = new PresenceEnvironment();
            unum.setIngredient("color", new ColorIngredient(env));
            unum.setIngredient("shape", new ShapeIngredient(env));
            unum.finishUnum();
            // The unum.init() can take a null, providing your ingredients'
            // constructors can, or create a SoulState and pass it in.
            unum.init(null);
        } catch (UnumException exc) {
            System.out.println(" You screwed up the unum, idiot: " + exc);
        }
        return unum;
    }
}
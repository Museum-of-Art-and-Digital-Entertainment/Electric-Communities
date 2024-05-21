/**
 * The description of a particular variety of unum. Assumes a host/client
 * presence model and a fixed set of ingredient roles, but omits the actual
 * choice of implementations for the ingredients themselves.
 */
public class UnumDefinition {
    /**
     * All done with Hashtbles, so no counts are needed.
     */
    public UnumDefinition();

    /**
     * The same as Chip's.
     */
    public void addClientPresenceMessage(String message, String ingredient
       throws UnumException;
    public void addHostPresenceMessage(String message, String ingredient)
       throws UnumException;
    public void addIngredientRole(String ingredientRole) throws UnumException;
    public void addUnumMessage(String message, String ingredient)
       throws UnumException;
    public Unum createUnum();
    public void finishDefinition();

    //KSS added

    /**
     * Set the role name of this unum.
     *
     * @param presenceRole  The name of the unum's role (i.e. prime presence role)
     *
     * @exception UnumException if myRoleName is already set
     */
    public void setUnumRole(String presenceRole) throws UnumException;
    /**
     * Declare what role another role makes.
     *
     * @param presenceRole  The name of a role
     * @param makesRole  The name of the role rolename makes
     *
     * @exception UnumException if rolename already makes something else
     */
    public void addRoleMakes(String presenceRole, String makesRole) throws UnumException;

    /**
     * Add an ingredient role to a presence role's list
     *
     * @param presenceRole  The name of a role
     * @param ingredientRole  The name of the ingredient
     *
     * @exception UnumException if ingredientRole not in myIngredientRoles[]
     */
    public void addIngredientRole(String presenceRole, String ingredientRole) throws UnumException;

}

/**
 * An unum instance
 */
public class Unum {
    /**
     * The same as Chip's.
     */
    public void finishUnum();
    public void send(String message, Object[] args) throws UnumException;
    public void setIngredient(String roleName, Ingredient ingredient)
       throws UnumException;
    public Presence takePresence(boolean isHost) throws UnumException;

    //KSS added
    /**
     * Initialize this unum.
     *
     * @param soulState  The soulState to get the ingredients' states from
     */
    void init(SoulState soulState);
    /**
     * Encode this unum.
     *
     * @param encoder  The encoder to encode into
     */
    public void encode(RtEncoder encoder);
    /**
     * Decode this unum.
     *
     * @param decoder  The decoder to decode from
     */
    public void decode(RtDecoder decoder);
}

/**
 * A presence of an unum
 */
public class Presence {
    /**
     * The same as Chip's.
     */
    public void send(String message, Object[] args) throws UnumException;
}

/**
 * The base class for all ingredients
 */
abstract public class Ingredient {
    protected Unum myUnum = null;

    /**
     * The same as Chip's.
     */
    protected Ingredient getNeighbor(String neighbor);
    protected boolean haveNeighbor(String neighbor);
    protected void neighborSend(String neighbor,
                                String message, Object[] args)
                                   throws UnumException;

    //KSS added

    /** Used to get the states of ingredients and put them in a
     * Hashtable for encoding purposes. */
    /* package */ abstract Object getClientState();

    /** For initialization. */
    /* package */ abstract void init(Object state);
}

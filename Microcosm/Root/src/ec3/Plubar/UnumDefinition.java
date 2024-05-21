//
// Change al KSSHackEClass-commented code when changing Ingredient from an
// eclass back to a plain Java Class
//

package ec.plubar;

import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.tables.IntTable;

/**
 * The description of a particular variety of unum. Assumes a host/client
 * presence model and a fixed set of ingredient roles, but omits the actual
 * choice of implementations for the ingredients themselves.
 *
 * @author Chip Morningstar
 * @author Karl Schumaker
 * @version 1.0
 */
public class UnumDefinition {
    /** A Hashtable of UnumIngredientEntries keyed by role name.
     * @see ec.tables.IntTable#IntTable(Class) */
    private Hashtable myIngredientRoles;
    /** An IntTable of Unum message names keyed by Ingredient index (from
     * myIngredientRoles). */
    private IntTable myUnumMessages;
    /** An IntTable of host Presence message names keyed by Ingredient index
     * (from myIngredientRoles). */
    private IntTable myHostPresenceMessages;
    /** An IntTable of client Presence message names keyed by Ingredient index
     * (from myIngredientRoles). */
    private IntTable myClientPresenceMessages;
    /** A flag indicating if this definition is finished or not. */
    private boolean myCreationMode;
    /** A String used for labeling and debug purposes as we can no longer
     * distinguish between una by Class type. */
    private String myName;
    /** Boolean used for determining replacement/non-replacement of message
       routing entries in the various tables */
    private final static boolean REPLACE = true;
    /** Boolean used for determining replacement/non-replacement of message
       routing entries in the various tables */
    private final static boolean NOREPLACE = false;

    /**
     * Construct a new unum definition. The result will still be in creation
     * mode.
     *
     * @param unumName  The name for this definition; a placeholder at the
     * moment (3/4/98), but something to have as a handle or label
     * to una, as there is no way to distinguish them by Class any more...
     */
    public UnumDefinition(String unumName) {
        myName = unumName;
        myIngredientRoles = new Hashtable();
        myUnumMessages = new IntTable(String.class);
        myHostPresenceMessages = new IntTable(String.class);
        myClientPresenceMessages = new IntTable(String.class);
        myCreationMode = true;
    }

    /**
     * Declare a message in the client-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     *
     * @exception UnumException from addMessage()
     *
     * @see UnumDefinition#addMessage
     */
    public void addClientPresenceMessage(String message, String ingredient)
      throws UnumException {
        addMessage(message, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare a message in the host-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     *
     * @exception UnumException from addMessage()
     *
     * @see UnumDefinition#addMessage
     */
    public void addHostPresenceMessage(String message, String ingredient)
      throws UnumException {
        addMessage(message, ingredient, myHostPresenceMessages, NOREPLACE);
    }

    /**
     * Declare a message in all presence interfaces.
     *
     * @param message      name of the message
     * @param ingredient   name of the target ingredient
     *
     * @exception UnumException from addMessage()
     *
     * @see UnumDefinition#addMessage
     */
    public void addPresenceMessage(String message, String ingredient)
      throws UnumException {
        addMessage(message, ingredient, myHostPresenceMessages, NOREPLACE);
        addMessage(message, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the client-side presence interface.
     *
     * @param interfaceName the name of the interface to deliver
     * @param ingredient    name of the target ingredient
     *
     * @exception UnumException from addInterface()
     *
     * @see UnumDefinition#addInterface
     */
    public void addClientPresenceInterface(String interfaceName, String ingredient)
      throws UnumException {
        addInterface(interfaceName, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the client-side presence interface.
     *
     * @param intf The Class of the interface to deliver
     * @param ingredient The name of the target ingredient
     *
     * @exception UnumException from addInterface()
     *
     * @see UnumDefinition#addInterface
     */
    public void addClientPresenceInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the host-side presence interface.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
     *
     * @see UnumDefinition#addInterface
     */
    public void addHostPresenceInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myHostPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in all presence interfaces.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
     *
     * @see UnumDefinition#addInterface
     */
    public void addPresenceInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myHostPresenceMessages, NOREPLACE);
        addInterface(intf, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare the role name of one of the ingredients
     *
     * @param ingredientRole  The role name of the ingredient to add
     *
     * @exception UnumException from ensureOpenDefinition()
     *
     * @see UnumDefinition#ensureOpenDefinition
     */
    public void addIngredientRole(String ingredientRole, boolean toEncode) throws UnumException {
        ensureOpenDefinition();
        int index = myIngredientRoles.size();
        myIngredientRoles.put(ingredientRole,
                              new UnumIngredientEntry (index, toEncode));
    }

    /**
     * Declare a message in the unum presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     *
     * @exception UnumException from addMessage()
     *
     * @see UnumDefinition#addMessage
     */
    public void addUnumMessage(String message, String ingredient) throws UnumException {
        addMessage(message, ingredient, myUnumMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the unum presence interface.
     *
     * @param intf The Class of the interface to deliver
     * @param ingredient The name of the target ingredient
     *
     * @exception UnumException from addInterface()
     *
     * @see UnumDefinition#addInterface
     */
    public void addUnumInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myUnumMessages, NOREPLACE);
    }

    /**
     * Create a new unum that matches this definition. The newly created
     * unum will itself be returned in create mode and will not have any
     * ingredients associated with it.
     *
     * @return A new Unum constructed with this UnumDefinition.
     */
    public Unum createUnum() {
        return new Unum(this);
    }

    /**
     * Terminate creation mode. Called by the unum definition creator to
     * indicate that it has added all the ingredient roles and messages.
     *
     * @exception UnumException Thrown when there is an error in the definition
     */
    public void finishDefinition() throws UnumException {
        myCreationMode = false;
        System.out.println("\n *** finishDefinition() needs to do checking... ***\n");
//KSSHack For when I actually do some checking. :)
//KSSHack        if (!okay) {
//KSSHack            throw new UnumException("Error in finishDefinition()");
//KSSHack        }
    }

    /**
     * Package-use method to determine the integer index of a particular
     * ingredient, indentified by role name.
     *
     * @param ingredient  The role name of the ingredient desired
     *
     * @return  The index of said ingredient in the ingredient roles array
     *
     * @exception UnumException if 'ingredient' is null]
     * @exception UnumException if ingredient is not in myIngredientRoles[]
     */
    /* package */ int findIngredient(String ingredient) throws UnumException {
        try {
            UnumIngredientEntry entry = (UnumIngredientEntry)myIngredientRoles.get(ingredient);
            if (entry != null) {
                return entry.index;
            } else {
                throw new UnumException("findIngredient: no ingredient " + ingredient);
            }
        } catch  (NullPointerException exc) {
            throw new UnumException("findIngredient: trying to find null ingredient ");
        }
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
     *
     * @exception UnumException if message is not in myHostPresenceMessages
     *   or myClientPresenceMessages
     *
     * @see Presence#send
     */
    /* package */ void presenceSend(boolean isHost,
                                    Ingredient[] ingredients,
                                    String message,
                                    Object[] args)
                                        throws UnumException {
        if (isHost) {
            try {
                int i = myHostPresenceMessages.getInt(message);
                //KSSHack Temporary output for debugging
                System.out.println(" Sending presence host message " + message);
                if (ingredients != null && i < ingredients.length) {
                    //KSSHackEClass Only cast until we can make Ingredient a plain Java Class again
                    //KSSHackEClass ingredients[i].send(message, args);
                    ((IngredientJif)(ingredients[i])).send(message, args);
                } else {
                    throw new UnumException("presenceSend: no ingredient target");
                }
            } catch (NoSuchElementException exc) {
                throw new UnumException("presenceSend: no message " + message);
            }
        } else {
            try {
                int i = myClientPresenceMessages.getInt(message);
                //KSSHack Temporary output for debugging
                System.out.println(" Sending presence client message " + message);
                if (ingredients != null && i < ingredients.length) {
                    //KSSHackEClass Only cast until we can make Ingredient a plain Java Class again
                    //KSSHackEClass ingredients[i].send(message, args);
                    ((IngredientJif)(ingredients[i])).send(message, args);
                } else {
                    throw new UnumException("presenceSend: no ingredient target");
                }
            } catch (NoSuchElementException exc) {
                throw new UnumException("presenceSend: no message " + message);
            }
        }
        throw new UnumException("presenceSend: message not understood");
    }

    /**
     * Deliver a message to an ingredient via the unum interface. Callable only
     * from within the package. The only caller should be the 'send' method
     * of the Unum class
     *
     * @param ingredients  An array of the actual ingredients
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException if message is not in myUnumMessages
     *
     * @see Unum#send
     */
    /* package */ void unumSend(Ingredient[] ingredients,
                                String message, Object[] args)
                                  throws UnumException {
        try {
            int i = myUnumMessages.getInt(message);
            //KSSHack Temporary output for debugging
            System.out.println("Sending unum message " + message);
            if (ingredients != null && i < ingredients.length) {
                //KSSHackEClass Only cast until we can make Ingredient a plain Java Class again
                //KSSHackEClass ingredients[i].send(message, args);
                ((IngredientJif)(ingredients[i])).send(message, args);
            } else {
                throw new UnumException("unumSend: no ingredient target");
            }
        } catch (NoSuchElementException exc) {
            throw new UnumException("unumSend: no message " + message);
        }
    }
        
    /**
     * Add a new message to one of the several message arrays
     *
     * @param message  The message to add
     * @param ingredient  The role name of the ingredient to deliver it to
     * @param messagesTable  The message IntTable to add the message to
     * @param replace       Boolean to determine whether or not to replace a
     *                      message in the table if it is already routed
     *
     * @exception UnumException from ensureOpenDefinition()
     * @exception UnumException from findIngredient()
     *
     * @see UnumDefinition#ensureOpenDefinition
     * @see UnumDefinition#findIngredient
     */
    private void addMessage(String message, String ingredient,
                            IntTable messagesTable, boolean replace) throws UnumException {
        ensureOpenDefinition();
        int ingredientNumber = findIngredient(ingredient);
        try {
            int i = messagesTable.getInt(message);
            if (replace) {
                messagesTable.putInt(message, ingredientNumber);
            } else {
                //KSSHack Temporary output for debugging
                System.out.println("   Message " + message + " already routed");
            }
        } catch (NoSuchElementException exc) {
            messagesTable.putInt(message, ingredientNumber);
        }
    }

    /**
     * Add a new interface to one of the several message arrays.  First,
     * the super class of intf, if any, is added as an interface, then any
     * interfaces which intf implements are added, and finally all public
     * methods on intf itself are added.  Note that interfaces or classes
     * may be passed into this procedure.
     *
     * @param intf          The Class of the interface to add
     * @param ingredient    The role name of the ingredient to deliver it to
     * @param messagesTable The message IntTable to add the message to
     * @param replace       Boolean to determine whether or not to replace a
     *                      message in the table if it is already routed
     *
     * @exception UnumException from ensureOpenDefinition()
     * @exception UnumException from addMessage()
     */
    public void addInterface(Class intf, String ingredient,
                             IntTable messagesTable, boolean replace) throws UnumException {
        int i = 0, mods = 0;
        ensureOpenDefinition();
        Class superClass = intf.getSuperclass();
        if (superClass != null) {
            addInterface(superClass, ingredient, messagesTable, replace);
        }
        Class interfaces[] = intf.getInterfaces();
        for (i = 0; i < interfaces.length; i++) {
            addInterface(interfaces[i], ingredient, messagesTable, replace);
        }
        Method methods[] = intf.getDeclaredMethods();
        for (i = 0; i < methods.length; i++) {
            mods = methods[i].getModifiers();
            if (Modifier.isPublic(mods)) {
                //KSSHack Temporary output for debugging
                System.out.println("   Adding public message " +
                  methods[i].getName() + " to " + ingredient);
                addMessage(methods[i].getName(), ingredient, messagesTable, replace);
            } else {
                //KSSHack Temporary output for debugging
                System.out.println("   Message " + methods[i].getName() +
                  " not public for " + ingredient + "!");
            }
        }
    }

    /**
     * Throw an exception if we are not in creation mode
     *
     * @exception UnumException if myCreationMode is not true
     */
    private void ensureOpenDefinition() throws UnumException {
        if (!myCreationMode) {
            throw new UnumException("unum definition closed");
        }
    }

    //KSS added

    /**
     *  Return the number of ingredient roles for this definition.  This is
     * used in Unum.java's constructor to initialize the size of its own
     * ingredient array without the user having to indicated in 2 places the
     * number of ingredients in an unum.
     *
     * @return the size of Hashtable myIngredientRoles.
     *
     * @see Unum#Unum
     */
    int ingredientCount() {
        return myIngredientRoles.size();
    }

    /**
     *  Return the ingredient roles for this definition.  This is used, for
     * example, by Unum.java's init() routine, which needs to get each of
     * the ingredient states by role name from its soulState and then init()
     * each item in its own table of actual ingredients.
     *
     * @return The Hashtable myIngredientRoles.
     *
     * @see Unum#init
     */
    Hashtable getIngredientRoles() {
        return myIngredientRoles;
    }

    /**
     * Encode this definition by encoding myIngredientRoles, myUnumMessages,
     * myHostPresenceMessages, myClientPresenceMessages and myCreationMode.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an IOException encoding error occurs
     * in encoder.encodeObject() or encoder.writeBoolean().
     *
     * @see ec.e.run.RtEncoder#encodeObject
     * @see ec.e.run.RtEncoder#writeBoolean
     */
    public void encode(RtEncoder encoder) throws UnumException {
        try {
            encoder.encodeObject(myIngredientRoles);
            encoder.encodeObject(myUnumMessages);
            encoder.encodeObject(myHostPresenceMessages);
            encoder.encodeObject(myClientPresenceMessages);
            encoder.writeBoolean(myCreationMode);
        } catch (IOException exc) {
            throw new UnumException("error encoding UnumDefinition");
        }
    }

    /**
     * Decode this definition by encoding myIngredientRoles, myUnumMessages,
     * myHostPresenceMessages, myClientPresenceMessages and myCreationMode.
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an IOException decoding error occurs in
     * decoder.decodeObject() or decoder.readBoolean().
     *
     * @see ec.e.run.RtDecoder#decodeObject
     * @see ec.e.run.RtDecoder#readBoolean
     */
    public void decode(RtDecoder decoder) throws UnumException {
        try {
            myIngredientRoles = (Hashtable)decoder.decodeObject();
            myUnumMessages = (IntTable)decoder.decodeObject();
            myHostPresenceMessages = (IntTable)decoder.decodeObject();
            myClientPresenceMessages = (IntTable)decoder.decodeObject();
            myCreationMode = decoder.readBoolean();
        } catch (IOException exc) {
            throw new UnumException("error decoding UnumDefinition");
        }
    }

    /**
     * A possibly bogus method for outputing myIngredientRoles; called only from
     * toString().
     *
     * @return A list of the Ingredients' indices, names and encode status.
     *
     * @see UnumDefinition#toString
     */
    /* package */ String ingrsToString() {
        String data = " Ingredients[" + myIngredientRoles.size() + "]:\n";
        Enumeration names = myIngredientRoles.keys();
        Enumeration entries = myIngredientRoles.elements();
        String name = null;
        UnumIngredientEntry entry = null;
        while (entries.hasMoreElements() && names.hasMoreElements()) {
            name = (String)names.nextElement();
            entry = (UnumIngredientEntry)entries.nextElement();
            data = data + "  " + entry.index + " " + name + " " +
                   entry.encodeState + "\n";
        }
        return data;
    }

    /**
     * A possibly bogus method for outputing IntTables; called only from
     * toString().
     *
     * @return A passed in 'title', the size of the IntTable and a list
     * of index/message name pairs.
     *
     * @see UnumDefinition#toString
     */
    /* package */ String tableToString(String title, IntTable table) {
        Enumeration elems = table.elements();
        Enumeration keys = table.keys();
        String data = title + "[" + table.size() + "]\n";

        if (keys.hasMoreElements() && elems.hasMoreElements()) {
            data = data + "  ";
        }
        while (keys.hasMoreElements() && elems.hasMoreElements()) {
            data = data + keys.nextElement() +
                   " " + elems.nextElement();
            if (keys.hasMoreElements() && elems.hasMoreElements()) {
                data = data + ", ";
            } else {
                data = data + "\n";
            }
        }
        return data;
    }

    /**
     * A possibly bogus method for outputing an UnumDefinition as a String;
     * calls ingrsToString() and tehn tableToString() for myUnumMessages,
     * myHostPresenceMessages and myClientPresenceMessages.
     *
     * @return This UnumDefinition expressed as a String, listing Ingredients
     * and message routing information along with myCreationMode's value.
     *
     * @see UnumDefinition#ingrsToString
     * @see UnumDefinition#tableToString
     */
    public String toString() {
        return (ingrsToString() +
                tableToString(" Unum Messages", myUnumMessages) +
                tableToString(" Host Presence Messages", myHostPresenceMessages) +
                tableToString(" Client Presence Messages", myClientPresenceMessages) +
                "myCreationMode=" + myCreationMode + "\n");
    }
}


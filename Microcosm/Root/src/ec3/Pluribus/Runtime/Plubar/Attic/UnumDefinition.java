package ec.pl.runtime;

import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.tables.IntTable;

/**
 * The description of a particular variety of unum. Assumes a host/client
 * presence model and a fixed set of ingredient roles, but omits the actual
 * choice of implementations for the ingredients themselves.
 */
public class UnumDefinition {
    private UnumIngredientEntry[] myIngredientRoles;
    private IntTable myUnumMessages;
    private IntTable myHostPresenceMessages;
    private IntTable myClientPresenceMessages;
    private boolean myCreationMode;

    /* Boolean used for determining replacement/non-replacement of message
       routing entries in the various tables */
    private final static boolean REPLACE = true;
    private final static boolean NOREPLACE = false;

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
    public UnumDefinition(int ingredientRoleCount) {
        myIngredientRoles = new UnumIngredientEntry[ingredientRoleCount];
        myUnumMessages = new IntTable(new String().getClass());
        myHostPresenceMessages = new IntTable(new String().getClass());
        myClientPresenceMessages = new IntTable(new String().getClass());
        myCreationMode = true;
    }

    /**
     * Declare a message in the client-side presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     *
     * @exception UnumException from addMessage()
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
     */
    public void addPresenceMessage(String message, String ingredient)
      throws UnumException {
        addMessage(message, ingredient, myHostPresenceMessages, NOREPLACE);
        addMessage(message, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare a message in the client-side presence interface.
     *
     * @param interfaceName the name of the interface to deliver
     * @param ingredient    name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addClientPresenceInterface(String interfaceName, String ingredient)
      throws UnumException {
        addInterface(interfaceName, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the client-side presence interface.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addClientPresenceInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare a message in the host-side presence interface.
     *
     * @param interfaceName the name of the interface to deliver
     * @param ingredient    name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addHostPresenceInterface(String interfaceName, String ingredient)
      throws UnumException {
        addInterface(interfaceName, ingredient, myHostPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the host-side presence interface.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addHostPresenceInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myHostPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in all presence interfaces.
     *
     * @param interfaceName the interface to deliver
     * @param ingredient    name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addPresenceInterface(String interfaceName, String ingredient)
      throws UnumException {
        addInterface(interfaceName, ingredient, myHostPresenceMessages, NOREPLACE);
        addInterface(interfaceName, ingredient, myClientPresenceMessages, NOREPLACE);
    }

    /**
     * Declare an interface in all presence interfaces.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
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
     */
    public void addIngredientRole(String ingredientRole, boolean toEncode) throws UnumException {
        ensureOpenDefinition();
        int i = findAvailable(myIngredientRoles);
        myIngredientRoles[i] = new UnumIngredientEntry (ingredientRole, toEncode);
    }

    /**
     * Declare a message in the unum presence interface.
     *
     * @param message  The name of the message
     * @param ingredient  The role name of the ingredient to deliver it to
     *
     * @exception UnumException from addmessage()
     */
    public void addUnumMessage(String message, String ingredient) throws UnumException {
        addMessage(message, ingredient, myUnumMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the unum presence interface.
     *
     * @param interfaceName the name of the interface to deliver
     * @param ingredient    name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addUnumInterface(String interfaceName, String ingredient)
      throws UnumException {
        addInterface(interfaceName, ingredient, myUnumMessages, NOREPLACE);
    }

    /**
     * Declare an interface in the unum presence interface.
     *
     * @param intf        the Class of the interface to deliver
     * @param ingredient  name of the target ingredient
     *
     * @exception UnumException from addInterface()
     */
    public void addUnumInterface(Class intf, String ingredient)
      throws UnumException {
        addInterface(intf, ingredient, myUnumMessages, NOREPLACE);
    }

    /**
     * Create a new unum that matches this definition. The newly created
     * unum will itself be returned in create mode and will not have any
     * ingredients associated with it.
     */
    public Unum createUnum() {
        return new Unum(this);
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
     *
     * @exception UnumException if ingredient is not in myIngredientRoles[]
     */
    /* package */ int findIngredient(String ingredient) throws UnumException {
        for (int i=0; i<myIngredientRoles.length; ++i) {
            if (myIngredientRoles[i].name.equals(ingredient)) {
                return i;
            }
        }
        throw new UnumException("findIngredient: no ingredient " + ingredient);
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
     */
    /* package */ void presenceSend(boolean isHost, Ingredient[] ingredients,
                                    String message, Object[] args)
                                      throws UnumException {
        if (isHost) {
            try {
                int i = myHostPresenceMessages.getInt(message);
                //KSSHack Temporary output for debugging
                System.out.println(" Sending presence host message " + message);
                if (ingredients != null && i < ingredients.length) {
                    ingredients[i].send(message, args);
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
                    ingredients[i].send(message, args);
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
     */
    /* package */ void unumSend(Ingredient[] ingredients,
                                String message, Object[] args)
                                  throws UnumException {
        try {
            int i = myUnumMessages.getInt(message);
            //KSSHack Temporary output for debugging
            System.out.println("Sending unum message " + message);
            if (ingredients != null && i < ingredients.length) {
                ingredients[i].send(message, args);
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
     * Add a new interface to one of the several message arrays by adding
     * each message individually.
     *
     * @param interfaceName The name of the interface to add
     * @param ingredient    The role name of the ingredient to deliver it to
     * @param messagesTable The message IntTable to add the message to
     * @param replace       Boolean to determine whether or not to replace a
     *                      message in the table if it is already routed
     *
     * @exception UnumException if interfaceName does not exist)
     * @exception UnumException from addInterface(Class, String, IntTable, boolean)
     */
    private void addInterface(String interfaceName, String ingredient,
                              IntTable messagesTable, boolean replace) throws UnumException {
        try {
            Class intf = Class.forName(interfaceName);
            addInterface(intf, ingredient, messagesTable, replace);
        } catch (ClassNotFoundException exc) {
            throw new UnumException("addInterface: interface " + interfaceName +
                                    " does not exist");
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

    /**
     * Search an array for the first null element
     *
     * @param array  The array to search
     * @returns  The index of the first element of array whose value is null
     *
     * @exception UnumException if there's no available slot in array
     */
    private int findAvailable(Object[] array) throws UnumException {
        for (int i=0; i<array.length; ++i) {
            if (array[i] == null) {
                return i;
            }
        }
        throw new UnumException("unum definition full");
    }


    //KSS added

    /**
     *  Return the number of ingredient roles for this definition.  This is
     * used in Unum.java's constructor to initialize the size of its own
     * ingredient array without the user having to indicated in 2 places the
     * number of ingredients in an unum.
     * @see ec.pl.runtime.Unum#Unum
     */
    int ingredientCount() {
        return myIngredientRoles.length;
    }

    /**
     *  Return the ingredient roles for this definition.  This is used, for
     * example, by Unum.java's init() routine, which needs to get each of
     * the ingredient states by role name from its soulState and then init()
     * each item in its own table of actual ingredients.
     * @see ec.pl.runtime.Unum#init
     */
    UnumIngredientEntry[] getIngredientRoles() {
        return myIngredientRoles;
    }

    /**
     * Encode this definition.
     *
     * @param encoder  The encoder to encode into
     *
     * @exception UnumException when an encoding error occurs
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
     * Decode this definition.
     *
     * @param decoder  The decoder to decode from
     *
     * @exception UnumException when an decoding error occurs
     */
    public void decode(RtDecoder decoder) throws UnumException {
        try {
            myIngredientRoles = (UnumIngredientEntry[])decoder.decodeObject();
            myUnumMessages = (IntTable)decoder.decodeObject();
            myHostPresenceMessages = (IntTable)decoder.decodeObject();
            myClientPresenceMessages = (IntTable)decoder.decodeObject();
            myCreationMode = decoder.readBoolean();
        } catch (IOException exc) {
            throw new UnumException("error decoding UnumDefinition");
        }
    }

    String ingrsToString() {
        String data = " Ingredients[" + myIngredientRoles.length + "]:\n";
        for (int i = 0; i < myIngredientRoles.length; i++) {
            data = data + "  " + i + " " + myIngredientRoles[i].name + " " +
                   myIngredientRoles[i].encodeState + "\n";
        }
        return data;
    }

    String tableToString(String title, IntTable table) {
        Enumeration elems = table.elements();
        Enumeration keys = table.keys();
        String data = title + "[" + table.size() + "]\n";

        while (keys.hasMoreElements() && elems.hasMoreElements()) {
            data = data + "  " + keys.nextElement() +
                   " " + elems.nextElement() + ", ";
        }
        return data;
    }

    public String toString() {
        return (ingrsToString() +
                tableToString(" Unum Messages", myUnumMessages) +
                tableToString(" Host Presence Messages", myHostPresenceMessages) +
                tableToString(" Client Presence Messages", myClientPresenceMessages) +
                "myCreationMode=" + myCreationMode + "\n");
    }
}


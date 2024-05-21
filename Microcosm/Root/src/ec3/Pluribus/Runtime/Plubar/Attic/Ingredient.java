package ec.pl.runtime;

//KSSHack Used in bogus send() method
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//KSSHack Used in bogus send() method
 
/**
 * The base class for all ingredients
 */
abstract public class Ingredient {
    protected PresenceEnvironment myEnvironment = null;
    protected String myName = null;
    protected Unum myUnum = null;

    /** Constructor
     *
     * @param env  The PresenceEnvironment for this Ingredient
     */
    public Ingredient(PresenceEnvironment env) {
        myEnvironment = env;
    }

    /**
     * Tell this ingredient what unum it is a part of. Callable only from
     * within the package. The only caller should be the 'setIngredient'
     * method of class Unum.
     *
     * @exception UnumException if the Ingredient's unum is already set
     */
    /* package */ void setUnum(Unum unum) throws UnumException {
        if (myUnum != null)
            throw new UnumException("multiple Ingredient.setUnum calls");
        myUnum = unum;
    }

    /**
     * Return a neighbor ingredient
     *
     * @param neighbor  The name of the ingredient role of interest
     * @returns  The object in that role, or null if there is not such object
     */
    protected Ingredient getNeighbor(String neighbor) {
        try {
            return myUnum.getNeighbor(neighbor);
        } catch (UnumException ue) {
            return null;
        }
    }

    /**
     * Test if a particular neighbor exists
     *
     * @param neighbor  The name of the ingredient role of the neighbor
     * @returns  true iff an ingredient has been assigned to that role
     */
    protected boolean haveNeighbor(String neighbor) {
        try {
            return (myUnum.getNeighbor(neighbor) != null);
        } catch (UnumException ue) {
            return false;
        }
    }

    /**
     * Deliver a message to one of the ingredient's neighbors.
     *
     * @param neighbor  Role name of neighbor to deliver message to
     * @param message  Message to deliver
     * @param args  Message arguments
     *
     * @exception UnumException from Unum.getNeighbor()
     */
    protected void neighborSend(String neighbor,
                                String message, Object[] args)
                                  throws UnumException {
        //KSSHack debug message; delete later
        System.out.println(" Sent message " + message + " to neighbor " +
                           neighbor);
        myUnum.getNeighbor(neighbor).send(message, args);
    }

    //KSS Add

    /** Set the PresenceEnvironment of this ingredient
     *
     * @exception UnumException if the Ingredient's PresenceEnvironment
     * is already set
     */
    public void setEnvironment(PresenceEnvironment env) throws UnumException {
        if (myEnvironment == null) {
            myEnvironment = env;
        } else {
            throw new UnumException("multiple Ingredient.setEnvironment calls");
        }
    }

    /** Used to get the states of ingredients and put them in a
     * Hashtable for encoding purposes. */
    public abstract istBase getClientState();

    /** For initialization. */
    public abstract void init(istBase state);

   //KSSHack This is a (possibly) bogus method to set the Ingredient name
   //KSSHack Currently the name is only used in the exception handler for
   //KSSHack send().
    /* package */ void setName(String name) throws UnumException {
        if (myName != null)
            throw new UnumException("multiple Ingredient.setName calls");
        myName = name;
    }

    //KSSHack This is a totally bogus method to simulate Erun.deliver
    //KSSHack In any event, do we want all ERun.deliver localized here
    //KSSHack and use Ingredient.send elsewhere in the runtime?  Extra
    //KSSHack method calls from somewhere like UnumDefinition#presenceSend()
    //KSSHack but all actual messaging/delivering could be done from one point.
    public void send(String message, Object[] args) {
        Class[] classArray = null;
        if (args != null) {
            classArray = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                classArray[i] = args[i].getClass();
            }
        }
        try {
            Method method = this.getClass().getMethod(message, classArray);
            if (method != null) {
                method.invoke(this, args);
            } else {
                System.out.println("Ingredient(" + myName +"):send - method " +
                                   message +" not found");
            }
        } catch (NoSuchMethodException exc) {
            System.out.println("Ingredient:send "+exc);
        } catch (InvocationTargetException exc) {
            System.out.println("Ingredient:send "+exc+"\n  "+exc.getTargetException());
        } catch (IllegalAccessException exc) {
            System.out.println("Ingredient:send "+exc);
        }

 
    }

}
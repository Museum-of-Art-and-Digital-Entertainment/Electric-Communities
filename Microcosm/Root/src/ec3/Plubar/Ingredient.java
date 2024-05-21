package ec.plubar;

//KSSHack Used in bogus send() method
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//KSSHack Used in bogus send() method
 
/**
 * The base class for all ingredients in the new Plubar model.  The Unum class
 * deals with tables of these Ingredients.  Note that sublcasses of Ingredient
 * must either:
 * 1) Not have any constructors with arguments, OR
 * 2) A nil argument constructor in addition to any other constructors
 * This requirement stems from the fact that instances of an Unum's Ingredients
 * will be created upon decode by a newInstance() call, which requires a nil
 * argument constructor.
 *
 * @author Chip Morningstar
 * @author Karl Schumaker
 * @version 1.0
 */
abstract public class Ingredient {
    /** A flag indicating if this Ingredient resides in a host Unum.  Note that
     * it defaults to true; client Ingredients created upon decoding of an unum
     * will have their flags set to fals by the Unum.decode() method. */
    protected boolean myAmHost = true;
    /** The state bundle for this Ingredient; set by public method
    * init(istBase) and retrieved by getClientState().  While children of
    * Ingredient may have their own instance variables for children of
    * istBase, this variable insures that the istBase variable sent into
    * the init() method is what gets returned by getClientState(). */
    protected istBase myBaseState = null;
    /** The name of this kind of Ingredient; used for labeling and debug
     * purposes only at this time (3/9/98). */
    protected String myName = null;
    /** The Unum in which this instance of Ingredient resides; set by package
     * method setUnum(), called by Unum#setIngredient(). */
    protected Unum myUnum = null;

    /**
     * The basic constructor for an Ingredient: does nothing special at this
     * time (3/9/98) except that client Ingredients are created, after decode,
     * by a newInstance() call, which requires a constructor with no arguments.
     * See the comments about the Ingredient class.
     */
    public Ingredient() {
    }

    /**
     * Tell this ingredient what unum it is a part of. Callable only from
     * within the package. The only caller should be the setIngredient()
     * method of class Unum.
     *
     * @param unum The Unum to initialize instance variable myUnum with.
     *
     * @exception UnumException if the Ingredient's unum is already set or
     * if parameter unum is null
     *
     * @see Unum#setIngredient
     */
    /* package */ void setUnum(Unum unum) throws UnumException {
        if (unum == null) {
            throw new UnumException("Ingredient.setUnum called with null");
        }
        if (myUnum != null) {
            throw new UnumException("multiple Ingredient.setUnum calls");
        }
        myUnum = unum;
    }

    /**
     * Calls getNeighbor() on this Ingredient's myUnum variable and returns the
     * result, or catches the UnumExeption (meaning there was no ingredient
     * named 'neighbor' in the Unum) and returns a null.
     *
     * @param neighbor The name of the ingredient role of interest
     *
     * @return  The object in that role, or null if there is no such object
     *
     * @see Unum#getNeighbor
     */
    protected Ingredient getNeighbor(String neighbor) {
        try {
            return myUnum.getNeighbor(neighbor);
        } catch (UnumException ue) {
            return null;
        }
    }

    //KSS Add

    /**
     * Used to get the states of ingredients and put them in a
     * Hashtable for encoding purposes.   Called from
     * Unum.createClientSoulState()
     *
     * @see Unum#createClientSoulState
     *
     * @return  The instance variable myBaseState
     */
    /* package */ final istBase getClientState() {
        return myBaseState;
    }


    /**
     * For programmer-provided initialization, this method is automatically
     * called by init(istBase).
     *
     * @see Ingredient#init(istBase)
     */
    protected abstract void init();

    /**
     * For standard initialization, called from Unum#init().  This init
     * routine guarantees that the myBaseState variable returned in
     * getClientState() gets initialized with
     * an istBase object, and then calls the user-defined abstract init().
     *
     * @param state The initial state for this ingredient.
     *
     * @exception UnumException If this Ingredient has already been initialized
     *
     * @see Ingredient#getClientState
     * @see Unum#init
     */
    /* package */ final void init(istBase state) throws UnumException {
        if (myBaseState != null) {
            throw new UnumException("multiple Ingredient.init(istBase) calls");
        }
        myBaseState = state;
        this.init();
    }

    /**
     * A bogus method used for labeling and debugging only.
     *
     * @param name The name for this ingredient.
     *
     * @exception UnumException If this Ingredient has already been named
     */
    //KSSHack This is a (probably) bogus method to set the Ingredient name
    //KSSHack Currently the name is only used in the exception handler for
    //KSSHack send().
    protected void setName(String name) throws UnumException {
        if (myName != null) {
            throw new UnumException("multiple Ingredient.setName calls");
        }
        myName = name;
    }

    /**
     * A bogus method used for sending messages until we get ERun.deliver.
     *
     * @param message The name of the method to send.
     * @param args The arguments for message.
     */
    //KSSHack This is a totally bogus method to simulate Erun.deliver
    //KSSHack In any event, do we want all ERun.deliver localized here
    //KSSHack and use Ingredient.send elsewhere in the runtime?  Extra
    //KSSHack method calls from somewhere like UnumDefinition#presenceSend()
    //KSSHack but all actual messaging/delivering could be done from one point.
    /* package */ void send(String message, Object[] args) {
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


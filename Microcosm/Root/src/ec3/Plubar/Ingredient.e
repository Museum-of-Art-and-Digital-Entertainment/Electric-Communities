//
// Change al KSSHackEClass-commented code when changing Ingredient from an
// eclass back to a plain Java Class
//

package ec.plubar;

//KSSHack Used in bogus send() method
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//KSSHack Used in bogus send() method
 
//KSSHackEClass This is a bogus interface so we can call synchronous methods
//KSSHackEClass on an Ingredient until we can get rid eclasses for good and change
//KSSHackEClass Ingredient back to a simple Class.
public interface IngredientJif {
    void send(String message, Object[] args);
    void setUnum(Unum unum) throws UnumException;
    void setHost(boolean isHost) throws UnumException;
    void setName(String name) throws UnumException;

    istBase getClientState();
    void init(istBase state);
    String getClassName();
}

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
//KSSHackEClass abstract public class Ingredient {
public eclass Ingredient implements IngredientJif {
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
    //KSSHackEClass /* package */ void setUnum(Unum unum) throws UnumException {
    public void setUnum(Unum unum) throws UnumException {
        if (myUnum != null)
            throw new UnumException("multiple Ingredient.setUnum calls");
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
    //KSSHackEClass Change back when E goes away.
    //KSSHackEClass /* package */ final istBase getClientState() {
    public istBase getClientState() {
        return myBaseState;
    }


    /**
     * For programmer-provided initialization, this method is automatically
     * called by init(istBase).
     *
     * @see Ingredient#init(istBase)
     */
    //KSSHackEClass Put this back when we make Ingredient a Java Class and get rid
    //KSSHackEClass of IngredientJif
    //KSSHackEClass protected abstract void init();
    protected void init() {
        System.out.println("Using abstract Ingredient.init()");
        System.exit(0);
    }

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
    //KSSHackEClass Change back when E goes away.
    //KSSHackEClass /* package */ final void init(istBase state) {
    public void init(istBase state) {
        myBaseState = state;
        this.init();
    }

    //KSSHackEClass This *&#!$ thing is only here for Unum.java to call because
    //KSSHackEClass I can't call getClass() on a @(#$! eclass...
    //KSSHackEClass protected void setName(String name) throws UnumException {
    public String getClassName() {
        return "ec.plubar.Ingredient";
    }

    //KSSHackEClass This is totally bogus, only here because you can't
    //KSSHackEClass access the myAmHost variable from outside an eclass.
    public void setHost(boolean isHost) throws UnumException {
        myAmHost = isHost;
    }

    /**
     * A bogus method used for labeling and debugging only.
     *
     * @param name The name for this ingredient.
     *
     * @exception UnumException If this Ingredient has already been named
     */
    //KSSHack This is a (possibly) bogus method to set the Ingredient name
    //KSSHack Currently the name is only used in the exception handler for
    //KSSHack send().
    //KSSHackEClass /* package */ void setName(String name) throws UnumException {
    public void setName(String name) throws UnumException {
        if (myName != null)
            throw new UnumException("multiple Ingredient.setName calls");
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
    //KSSHackEClass /* package */ void send(String message, Object[] args) {
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

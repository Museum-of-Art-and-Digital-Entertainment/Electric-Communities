package ec.e.openers;

import ec.e.openers.OpenerID;
import ec.e.openers.Recipe;
import ec.e.openers.ObjOpener;
import java.util.Hashtable;

/**
 * A Recipe of no parameters for encoding and decoding a particular
 * object.
 */
/*package*/ class SingletonRecipe extends Recipe {

    static private Object[][] NoParams = {};

    private Object myObj;

    /**
     * The resulting Recipe will insist that 'obj' is what it encodes,
     * and will decode to 'obj'.
     */
    /*package*/ SingletonRecipe(Object obj) {
        super(NoParams, NoParams, obj.getClass());
        myObj = obj;
    }


    /**
     * The are no preface args.
     */
    public Object[] prefaceArgs(Object obj) {
    if (obj != myObj) {
        throw new Error("internal: obj isn't my Obj");
    }
        return NO_ARGS;
    }

    /**
     * There are no body args
     */
    public Object[] bodyArgs(Object obj) {
    if (obj != myObj) {
        throw new Error("internal: obj isn't my Obj");
    }
        return NO_ARGS;
    }

    /**
     * The returned argument is already fully cooked.
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
    return myObj;
    }

    /**
     * Should never be called
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        throw new Error("internal: should not be called");
    }

    /**
     * Does nothing, the result of halfBakedInstance is already fully
     * cooked.
     */
    public void cook(Object halfBaked, Object[] bodyArgs) {}
}


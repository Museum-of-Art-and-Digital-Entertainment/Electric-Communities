package ec.e.openers.guest;

import ec.e.openers.Recipe;
import ec.e.openers.RefOpener;
import ec.util.PEHashtable;
import java.util.Hashtable;

/**
 * The Recipe for encoding a ParamRecipe.
 * 
 * @see ec.e.opener.guest.ParamOpenerRecipe
 */
/*package*/ class ParamRecipeRecipe implements Recipe {

    private ParamRecipe myParamRecipe;

    /**
     * 
     */
    /*package*/ ParamRecipeRecipe(ParamRecipe paramRecipe) {
    myParamRecipe = paramRecipe;
    }

    /**
     * returns 'ParamRecipe.class', as that is the only kind of object
     * this is a Recipe for.
     */
    public Class type() {
        return myParamRecipe.getClass();
    }

    /**
     * Ignoring cruft, returns "(())ParamRecipe", since this Recipe
     * takes no arguments and cooks up a ParamRecipe
     */
    public String openerID() {
        return "(())" + RefOpener.signatureOf(type());
    }

    /**
     * Returns an empty array since there are no arguments
     */
    public Object commentary() {
        return NO_ARGS;
    }

    /**
     * The are no preface args.
     */
    public Object[] prefaceArgs(Object obj) {
    if (obj != myParamRecipe) {
        throw new Error("internal: obj isn't my ParamRecipe");
    }
        return NO_ARGS;
    }

    /**
     * There are no body args
     */
    public Object[] bodyArgs(Object obj) {
    if (obj != myParamRecipe) {
        throw new Error("internal: obj isn't my ParamRecipe");
    }
        return NO_ARGS;
    }

    /**
     * The returned argument is already fully cooked.
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
    return myParamRecipe;
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


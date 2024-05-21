package ec.e.openers.guest;

import ec.e.openers.OpenerID;
import ec.e.openers.Recipe;
import ec.e.openers.ObjOpener;
import ec.e.openers.JavaUtil;
import ec.tables.PEHashtable;
import java.util.Hashtable;

/**
 * The recipe for encoding a perimeter-crossing object reference as a
 * param object, and decoding a param as a corresponding
 * argument. <p>
 *
 * We're actually telling a white lie about the types, ARG_TYPE
 * is a pseudo type made up to represent the arg object,
 * as it could actually be any type. Typing it to Object is too
 * wide and could result in the ParamRecipe being used by default
 * for all Objects without explicit Recipes or Openers.
 *
 * @see ec.e.opener.guest.ParamOpenerRecipe
 */
public class ParamRecipe extends Recipe {

    static final Object[][] PrefaceParams = {
        { JavaUtil.OBJECT_TYPE,     "param" },
    };

    static final Object[][] BodyParams = {};

    /**
     * maps from a perimeter-crossing object reference to a
     * corresponding param object.
     */
    private PEHashtable myEncodingParams;

    /**
     * maps from a param object to a corresponding argument.
     */
    private Hashtable myDecodingArgs;


    /**
     * these tables are shared
     */
    public ParamRecipe(PEHashtable encodingParams,
                       Hashtable decodingArgs) {
        super(PrefaceParams, BodyParams, ArgType.ARG_TYPE);
        myEncodingParams = encodingParams;
        myDecodingArgs = decodingArgs;
    }


    /**
     * Given an object reference that crosses the perimeter, returns
     * a singleton array of the corresponding param.
     */
    public Object[] prefaceArgs(Object obj) {
        Object[] result = { myEncodingParams.get(obj) };
        if (result[0] == null) {
            throw new Error("internal: should have just been found");
        }
        return result;
    }

    /**
     * There are no body args
     */
    public Object[] bodyArgs(Object obj) {
        return NO_ARGS;
    }

    /**
     * Given a param object, returns the corresponding argument.
     * The returned argument is immediately fully cooked.
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        Object result;

        if (myDecodingArgs == null) {
            // allow an empty table, but it is an error if we get here with one
            result = null;
        } else {
            result = myDecodingArgs.get(prefaceArgs[0]);
            // this exception means that the encoding and the decoding
            // tables did not match
        }
        if (result == null) {
            throw new IndexOutOfBoundsException("" + prefaceArgs[0]);
        }
        return result;
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

/**
 * ArgType serves as a unique type identifier for
 * the ParamRecipe so that registering it with its Maker
 * (OpenerRecipe) won't cause Objects of some innocent type
 * to get decoded by a Chef built on a ParamRecipe.
 */

/* package */ class ArgType {

    static /*package*/ final Class ARG_TYPE = new ArgType().getClass();

    /* package */ ArgType() {}
}





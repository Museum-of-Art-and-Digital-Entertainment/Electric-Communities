package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import ec.tables.ArgsHolder;
import ec.tables.IntTable;
import ec.util.NestedException;


/**
 * The class representing the recipe for RtSealers.
 */
public class RtSealerRecipe 
extends Recipe
{
    /**
     * An array of pairs of a class and a field name
     */
    static private Object[][] ThePrefaceParams = 
        { { String.class, "name" } };

    /**
     * An array of pairs of a class and a field name
     */
    static private Object[][] TheBodyParams = {};

    /**
     * The unique RtSealerRecipe.
     */
    static public final RtSealerRecipe TheOne = new RtSealerRecipe();

    /**
     * Construct the (unique) RtSealerRecipe.
     */
    private RtSealerRecipe() {
        super(ThePrefaceParams, TheBodyParams, RtSealer.class);
    }

    /**
     * Given an RtSealer, return the preface args for reconsituting
     * it.
     *
     * @param obj != null;
     */
    public Object[] prefaceArgs(Object obj) {
        // guaranteed to work, as invoked by the serialization system
        RtSealer seal = (RtSealer) obj;

        return new Object[] { seal.getSignature() };
    }

    /**
     * Given a (sub)class of RtSealer and an array of preface args for
     * an RtSealer, return the "half baked" RtSealer object, which, in
     * this case, is actually cooked enough.
     *
     * @return ; the cooked-enough RtSealer
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        RtSealer result = RtSealer.getSealerOrUnknownSealer(
            sub, (String) prefaceArgs[0]);

        if (result == null) {
            throw new RuntimeException("Unexpectedly got a null sealer");
        }

        return result;
    }

    /**
     * We like mooshy RtSealers. They're cooked enough already.
     */
    public void cook(Object halfBaked, Object[] bodyArgs) {
    }

    /**
     * Get the unique encoder for RtSealers.
     */
    static public Recipe makeEncoder() {
        return TheOne;
    }

    /**
     * Get the unique decoder for RtSealers, if given the appropriate
     * sealer id.
     */
    static public Recipe makeDecoder(OpenerID id) {
        if (TheOne.openerID().equals(id)) {
            return TheOne;
        } else {
            throw new IllegalArgumentException(
                "RtSealerRecipe got bad OpenerID " + id);
        }
    }
}

package ec.e.openers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import ec.tables.ArgsHolder;
import ec.tables.IntTable;
import ec.util.NestedException;


/**
 * The interface to allow code outside this module to extend and alter
 * how various classes are inspected/encoded/decoded.
 */
public abstract class Recipe {

    /**
     * An array of no Objects.  This is returned by implementations
     * of prefaceArgs() or bodyArgs() when there aren't any.
     * Zero-length arrays are the only kind that are immutable, and
     * hence the only kind that may be statically shared.
     */
    static public final Object[] NO_ARGS = new Object[0];

    /**
     * An array of pairs of a class and a field name
     */
    private Object[][] myPrefaceParams;

    /**
     * @see ec.e.openers.ArgsHolder
     */
    private IntTable myPrefaceGuide;

    /**
     * An array of pairs of a class and a field name
     */
    private Object[][] myBodyParams;

    /**
     * @see ec.e.openers.ArgsHolder
     */
    private IntTable myBodyGuide;

    /**
     * @see ec.e.openers.Recipe#type
     */
    private Class myResultType;

    /**
     * @see ec.e.openers.Recipe#openerID
     */
    private NormalOpenerID myOpenerID;


    /**
     * Given an array of pairs, where the second element of each is a
     * String (presumed to be a name), makes a Guide giving the index
     * of each name.
     */
    private IntTable makeGuide(Object[][] params) {
        IntTable result = new IntTable(JavaUtil.STRING_TYPE);
        for (int i = 0; i < params.length; i++) {
            result.putInt(params[i][1], i, true);
        }
        return result;
    }


    /**
     *
     * @param prefaceParams An array of pairs, one for each "field" of
     * the Recipe.  The first column of each pair is a Class object
     * representing the type of the field (primitive Classes for scalar
     * fields), and the second column is the name of the field.
     *
     * @param bodyParams An array of pairs in the same format as
     * prefaceParams. If this Recipe represents the upgrade of a
     * Surgeon, the field names should mirror the original's instance
     * variable names.
     *
     * @param resultType A non-primitive non-array Class representing
     * the type of object cooked up by this Recipe
     */
    protected Recipe(Object[][] prefaceParams,
                     Object[][] bodyParams,
                     Class resultType) {
        myPrefaceParams = prefaceParams;
        myPrefaceGuide = makeGuide(myPrefaceParams);
        myBodyParams = bodyParams;
        myBodyGuide = makeGuide(myBodyParams);

        myResultType = resultType;
        myOpenerID = NormalOpenerID.make(myPrefaceParams,
                                         myBodyParams,
                                         myResultType);
    }

    /**
     * What is the actual type of objects that this recipe will
     * encode, or that will be returned by this recipe on decode?  On
     * encode, the recipe is typically looked up by this type.
     */
    public Class type() {
        return myResultType;
    }

    /**
     * The openerID under which this Recipe will be registered.  The
     * openerID is used by the corresponding Chef to encode
     * itself, and is how the recipe is looked up on decode.
     *
     * @see ec.e.openers.ObjOpener#openerID
     */
    public OpenerID openerID() {
        return myOpenerID;
    }


    /**
     * @see ec.e.openers.ObjOpener#commentary
     * @return nullOk, suspect;
     */
    public Object commentary() {
        String[] result = new String[myPrefaceParams.length
                                     + myBodyParams.length];
        int i;
        for (i = 0; i < myPrefaceParams.length; i++) {
            result[i] = (String)myPrefaceParams[i][1];
        }
        for (int j = 0; j < myBodyParams.length; j++) {
            result[i + j] = (String)myBodyParams[j][1];
        }
        return result;
    }


    /**
     * Given an object of the type() this recipe encodes, return an
     * array of objects that can be encoded according to the preface
     * part of the recipe's openerID(). <p>
     *
     * This defaults to returning an empty array, which will fail on
     * encode (and therefore should be overridden) if prefaceParams is
     * not empty.
     *
     * @param obj nullOk, suspect;
     */
    public Object[] prefaceArgs(Object obj) {
        return NO_ARGS;
    }


    /**
     * Given an object of the type() this recipe encodes, return an
     * array of objects that can be encoded according to the body
     * part of the recipe's openerID(). <p>
     *
     * This defaults to returning an empty array, which will fail on
     * encode (and therefore should be overridden) if bodyParams is
     * not empty.
     *
     * @param obj nullOk, suspect;
     */
    public Object[] bodyArgs(Object obj) {
        return NO_ARGS;
    }


    /**
     * Given an array of objects that corresponds to the preface part
     * of the recipe's openerID, make and return a new object
     * partially initialized (halfBaked) using this preface data. <p>
     *
     * Should usually be overridden.  If not, defaults to calling
     * halfBakedInstanceOf() using this Recipe's type().
     *
     * @return nullOk, suspect;
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        return halfBakedInstanceOf(type(), prefaceArgs);
    }


    /**
     * Like 'halfBakedInstance', but will return an instance of 'sub',
     * where type() is assignable from 'sub'.  This lets a Recipe for
     * a class 'sup' be used to initialize the sup part of a class sub
     * where sub is a subclass of sup.  sup's Recipe should therefore
     * ignore prefaceArgs beyond those it expects.  These might be
     * used by our caller to initialize the sub part. <p>
     *
     * Defaults to calling the constructor of class sub that is
     * declared as taking an ArgHolder parameter.  If class sub has no
     * such constructor, this default will fail (and should be
     * overridden).
     *
     * @return nullOk, suspect;
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        try {
            Constructor ctor = sub.getConstructor(ArgsHolder.MARKING_PARAMS);
            ArgsHolder holder = new ArgsHolder(myPrefaceGuide, prefaceArgs);
            Object[] args = { holder };
            return ctor.newInstance(args);

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            //if I understand Java exception handling correctly, ex is
            //not a RuntimeException
            throw new NestedException
              ("halfBakedInstanceOf should probably have been overridden", ex);
        }
    }

    /**
     * Given an object, 'halfBaked', returned by this recipe's
     * halfBakedInstance method, and an array of objects that corresponds
     * to the body part of the recipe's openerID, complete the
     * initialization of the object using this body data.  This Recipe
     * should ignore bodyArgs beyond what it expects.  This might be
     * used by our caller when the actual object is a subclass of what
     * this Recipe expects. <p>
     *
     * If the halfBaked object responds to a public method
     * 'prevat$init(ArgsHolder)', cook() defaults to invoking it.
     * Otherwise, cook() should be overridden.
     *
     * @param halfBaked nullOk, suspect;
     */
    public void cook(Object halfBaked, Object[] bodyArgs) {
        try {
            Method meth = halfBaked.getClass().getMethod("prevat$init",
                                                    ArgsHolder.MARKING_PARAMS);
            ArgsHolder holder = new ArgsHolder(myBodyGuide, bodyArgs);
            Object[] args = { holder };
            meth.invoke(halfBaked, args);

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            //if I understand Java exception handling correctly, ex is
            //not a RuntimeException
            throw new NestedException
              ("cook should probably have been overridden", ex);
        }
    }
}


/**
 * On decoding, a Chef uses a Recipe to cook up an object.  On
 * encoding, a Chef uses an object, and a Recipe for cooking such
 * objects, to explain to another Chef--in another time and place--how
 * to cook, ie, decode, an object like this one. <p>
 *
 * A Chef using an FooRecipe is an "Foo-chef", ie, an ObjOpener for
 * cooking up Foo's.  Hence "ObjOpener-chef" and "Class-chef".  The
 * ObjOpener-chef is the key object for bootstrapping encode/decode as
 * implemented by ec.e.serial.
 */
public final class Chef extends ObjOpener {

    Recipe myRecipe;
    VarOpener[] myPreface;
    VarOpener[] myBody;

    public Chef(OpenerRecipe maker, Recipe recipe) {

        super(maker, recipe.type(), recipe.openerID());
        myRecipe = recipe;
        NormalOpenerID openerID = (NormalOpenerID)openerID();
        myPreface = openerID.preface();
        myBody = openerID.body();
    }

    public String toString() {
        return "Recipe for " + myRecipe.toString();
    }

    public Object commentary() {
        return myRecipe.commentary();
    }

    static void encodeArgs(VarOpener[] params,
                           Object[] args,
                           RtEncoder encoder) throws IOException
    {
        if (args.length != params.length) {
            throw new IllegalArgumentException("Lengths don't match");
        }
        for (int i = 0; i < args.length; i++) {
            params[i].encodeItTo(args[i], encoder);
        }
    }

    public void encodePreface(Object obj, RtEncoder encoder)
         throws IOException {
        encodeArgs(myPreface, myRecipe.prefaceArgs(obj), encoder);
    }

    public void encodeBody(Object obj, RtEncoder encoder)
         throws IOException {
        encodeArgs(myBody, myRecipe.bodyArgs(obj), encoder);
    }

    static Object[] decodeArgs(VarOpener[] params, RtDecoder decoder)
         throws IOException {

        Object[] args = new Object[params.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = params[i].decodeFrom(decoder);
        }
        return args;
    }

    public Object decodePreface(RtDecoder decoder) throws IOException {
        return myRecipe.halfBakedInstance(decodeArgs(myPreface, decoder));
    }

    Object decodePrefaceOf(Class sub, RtDecoder decoder) throws IOException {
        return myRecipe.halfBakedInstanceOf(sub,
                                            decodeArgs(myPreface, decoder));
    }

    public void decodeBody(Object halfBaked, RtDecoder decoder)
         throws IOException
    {
        decoder.delay(RtDecoder.RECIPE_DELAY,
                      new Oven(myRecipe, halfBaked,
                               decodeArgs(myBody, decoder)));
    }
}


/**
 *
 */
/*package*/class Oven implements Runnable {

    private Recipe myRecipe;
    private Object myHalfBaked;
    private Object[] myBodyArgs;

    /*package*/Oven(Recipe recipe, Object halfBaked, Object[] bodyArgs) {
        myRecipe = recipe;
        myHalfBaked = halfBaked;
        myBodyArgs = bodyArgs;
    }

    public void run() {
        myRecipe.cook(myHalfBaked, myBodyArgs);
    }
}



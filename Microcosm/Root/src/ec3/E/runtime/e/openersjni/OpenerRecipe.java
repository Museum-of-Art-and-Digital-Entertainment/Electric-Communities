package ec.e.openers;

import ec.vcache.ClassCache;
import ec.tables.Table;
import ec.util.NestedException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


/**
 * An OpenerRecipe will make, encode, and decode RefOpeners.  It is a
 * Recipe that knows how to cook RefOpeners.
 */
public final class OpenerRecipe extends Recipe {
    static private final Trace tr = new Trace("ec.e.openers.OpenerRecipe");

    static final Class[] NO_CLASSES = {};

    static private final Object[][] PrefaceParams = {
        { JavaUtil.STRING_TYPE,     "openerID" },
    };

    static private final Object[][] BodyParams = {
        { JavaUtil.OBJECT_TYPE,     "commentary" },
    };

    /**
     * Maps fully qualified class names of classes whose instances may
     * be specially decoded to fully qualified class names of classes
     * that should have a method <p>
     * <pre>
     *     static public Recipe makeDecoder(OpenerID openerID)
     * </pre>
     * whose result is either null or a Recipe for encoding with
     * that openerID.
     * @see ec.e.openers.guest.SerializableMarker#DECODER_MAKERS
     */
    private Table myDecoderMakers;

    /**
     * Maps OpenerIDs to RefOpeners for decoding an object encoded
     * according to that ID.
     */
    private Table myDecoders;

    /**
     * Maps fully qualified class names of classes whose instances are
     * to be specially encoded to fully qualified class names of
     * classes that should have a method <p>
     * <pre>
     *     static public Recipe makeEncoder()
     * </pre>
     * @see ec.e.openers.guest.SerializableMarker#ENCODER_MAKERS
     */
    private Table myEncoderMakers;

    /**
     * Maps Classes to RefOpeners for encoding instances of that
     * Class.
     */
    private Table myEncoders;

    /**
     * Maps fully qualified class names to Classes.
     */
    private ClassRecipe myClassRecipe;

    private Chef myTurtle;

    /**
     * To decode an object, we first must decode its ObjOpener.  To
     * decode an ObjOpener, we have to map from an OpenerID to an
     * ObjOpener.  The decoderMakers lets us make decoding Recipes for
     * openerIDs that need special cooking instructions.  If we find a
     * matching Recipe, we return a Chef that cooks with that Recipe.
     * Failing that, we see if classRecipe can find the Class from its
     * fully qualified name, and if so, we see if we can make a
     * Surgeon for operating on instances of that Class according to
     * that OpenerID. <p>
     *
     * Similarly, to encode an object, we must first encode its
     * ObjOpener.  The encoderMakers lets us make encoding Recipes for
     * Classes whose instance need special encoding instructions.  If
     * we find a matching Recipe, we return a Chef that cooks with
     * that Recipe.  Failing that, we see if classRecipe allows the
     * Class, and if so, we make a Surgeon for operating on instances
     * of that Class.
     *
     * @param decoderMakers An array of pairs of fully-qualified class
     * names interpreted as documented for
     * SerializableMarker.DECODER_MAKERS.
     *
     * @param encoderMakers An array of pairs of fully-qualified class
     * names interpreted as documented for
     * SerializableMarker.ENCODER_MAKERS.
     *
     * @param classRecipe If the openerID isn't in the cookbook, see
     * if the classRecipe will cook up a Class from the fully
     * qualified name of the openerID's result type.
     *
     * @see ec.e.openers.guest.SerializableMarker#DECODER_MAKERS
     * @see ec.e.openers.guest.SerializableMarker#ENCODER_MAKERS
     */
    public OpenerRecipe(String[][] decoderMakers,
                        String[][] encoderMakers,
                        ClassRecipe classRecipe) {
        super(PrefaceParams, BodyParams, ObjOpener.OBJ_OPENER_TYPE);

        if (classRecipe == null)  {
            throw new IllegalArgumentException
              ("Must supply ClassRecipe to OpenerRecipe constructor");
        }

        myDecoderMakers = new Table(JavaUtil.STRING_TYPE,
                                    JavaUtil.STRING_TYPE);
        myDecoderMakers.putPairs(decoderMakers, true);
        myDecoders = new Table(OpenerID.OPENER_ID_TYPE,
                               ObjOpener.OBJ_OPENER_TYPE);

        myEncoderMakers = new Table(JavaUtil.STRING_TYPE,
                                    JavaUtil.STRING_TYPE);
        myEncoderMakers.putPairs(encoderMakers, true);
        myEncoders = new Table(true,
                               JavaUtil.CLASS_TYPE,
                               ObjOpener.OBJ_OPENER_TYPE);

        myClassRecipe = classRecipe;

        ObjOpener strOpener = new StringOpener(this);
        registerDecoder(strOpener);
        registerEncoder(strOpener);

        ObjOpener classOpener = new Chef(this, classRecipe);
        registerDecoder(classOpener);
        registerEncoder(classOpener);

        //not registered
        myTurtle = new Chef(this, this);
    }

    /**
     * Returns the ClassRecipe from which our authority flows--the
     * same one provided as a construction argument.
     */
    public ClassRecipe classRecipe() {
        return myClassRecipe;
    }


    /**
     * Package scope only!  The callback from a ObjOpener to register
     * itself in its OpenerRecipe's table of decoding ObjOpeners.
     * Returns its argument.
     */
    /*package*/ ObjOpener registerDecoder(ObjOpener opener) {
        myDecoders.put(opener.openerID(), opener, true);
        return opener;
    }


    /**
     * Package scope only!  The callback from a ObjOpener to register
     * itself in its OpenerRecipe's table of encoding ObjOpeners.
     * Returns its argument.
     */
    /*package*/ ObjOpener registerEncoder(ObjOpener opener) {
        myEncoders.put(opener.type(), opener, true);
        return opener;
    }


    /************* Recipe methods for cooking up RefOpeners ***********/

    /**
     * Returns the preface args to be encoded.  This is an array of
     * one string--the opener's openerID's description string.
     */
    public Object[] prefaceArgs(Object opener) {
        String[] result = { (((ObjOpener)opener).openerID()).descString() };
        return result;
    }

    /**
     * Returns the body args to be encoded.  This is an array of one
     * Object to be taken as a semantics-free commentary.
     *
     * @see ec.e.openers.ObjOpener#commentary
     */
    public Object[] bodyArgs(Object opener) {
        Object[] result = { ((ObjOpener)opener).commentary() };
        return result;
    }

    /**
     * Returns the opener gotten by interpreting prefaceArgs[0] as an
     * openerID.
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        try {
            return forDecodingBy(OpenerID.make((String)prefaceArgs[0]));

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new NestedException("in making an ObjOpener", ex);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new NestedException("in making an ObjOpener", ex);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new NestedException("in making an ObjOpener", ex);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new NestedException("in making an ObjOpener", ex);
        }
    }

    /**
     * OpenerRecipes don't support halfBakedInstanceOf()
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        throw new RuntimeException("should not implement " + sub);
    }

    /**
     * Currently does nothing with the decoded commentary
     */
    public void cook(Object halfBaked, Object[] bodyArgs) {}


    /******************* end recipe methods ***********************/

    /**
     * Returns a ObjOpener capable of opening 'obj', and therefore, of
     * opening objects like 'obj'. <p>
     *
     * Special case: if 'obj' is a ObjOpener whose maker() is this
     * object, then a Chef on this object is the ObjOpener for it.
     * This is done as a special case, and not through the recipe-
     * registration mechanism, because security would be violated if
     * we didn't check the maker() as well.
     *
     * @param obj suspect;
     */
    public ObjOpener forEncoding(Object obj) {
        if ((obj instanceof ObjOpener) && ((ObjOpener)obj).maker() == this) {

            return myTurtle;
        }
        try {
            return forEncodingAn(obj.getClass());

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new NestedException("finding an encoding ObjOpener for object " + obj, ex);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw new NestedException("finding an encoding ObjOpener for object " + obj, ex);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw new NestedException("finding an encoding ObjOpener for object " + obj, ex);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new NestedException("finding an encoding ObjOpener for object " + obj, ex);
        }
    }


    /**
     * Returns a ObjOpener for opening objects of type 'clazz'.
     */
    public ObjOpener forEncodingAn(Class clazz)
         throws ClassNotFoundException,
           InvocationTargetException,
           NoSuchMethodException,
           IllegalAccessException
    {
        ObjOpener result = (ObjOpener)myEncoders.get(clazz, null);
        if (result != null) {
            return result;
        }
        if (clazz.isArray()) {
            result = new ArrayOpener(this, clazz.getComponentType());
            registerEncoder(result);
            registerDecoder(result);
            return result;
        }
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException
              (clazz + " should not be a scalar type");
        }
        //in the non-array, non-primitive case, Class.getName()
        //returns the fully qualified class name (according to Steele.
        //This isn't documented in the javadoc-umentation.)
        String fqn = clazz.getName();

        //see if there's a special encoding behavior registered
        String encMakerName = (String)myEncoderMakers.get(fqn, null);
        if (encMakerName != null) {
            //yes, use CRAPI to call the Recipe making method

            //get the class that will provide the special encoding Recipe

            // If you get an ExceptionInInitializer in the statement
            // below, make sure you have ordered the static
            // initializers in your recipe class so as not to use
            // forward references - Kari
            Class encMaker = ClassCache.forName(encMakerName);
            //call its static 'makeEncoder()' method to get the Recipe
            Method maker = encMaker.getMethod("makeEncoder", NO_CLASSES);
            // If you get an IllegalAccessException in the statement below,
            // Make sure your Recipe class is public - Kari
            // Hmmm. maybe we should catch those errors here so we can tell user
            // the name of the failing recipe since that's hard to figure out - Kari
            Recipe encoder;
            try {
                encoder = (Recipe)maker.invoke(null, NO_ARGS);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
                throw new NestedException("InvocationTargetException wrapped ",
                    ex.getTargetException());
            }
            //wrap
            return registerEncoder(new Chef(this, encoder));
        } else {
            myClassRecipe.ensureAllowed(clazz);
            //there is no special encoding, default to using a Surgeon
            return registerEncoder(new Surgeon(this, clazz));
        }
    }


    /**
     * Returns an ObjOpener usable to decode an object encoded
     * according to 'opid'.
     *
     * @see ec.e.openers.ObjOpener#openerID
     */
    public ObjOpener forDecodingBy(OpenerID opid)
         throws ClassNotFoundException,
           NoSuchMethodException,
           InvocationTargetException,
           IllegalAccessException
    {
        ObjOpener result = (ObjOpener)myDecoders.get(opid, null);
        if (result != null) {
            return result;
        }
        if (opid instanceof NormalOpenerID) {
            //is there a registered special decoding behavior?
            String fqn = ((NormalOpenerID)opid).resultName();
            String decMakerName = (String)myDecoderMakers.get(fqn, null);
            if (decMakerName != null) {
                //yes, use CRAPI to call the Recipe making method
                Class[] params = { OpenerID.OPENER_ID_TYPE };
                Object[] args = { opid };

                Class decMaker = ClassCache.forName(decMakerName);
                Method maker = decMaker.getMethod("makeDecoder", params);
                Recipe decoder = (Recipe)maker.invoke(null, args);
                if (decoder != null) {
                    //makeDecoder(opid) returned a decoding Recipe
                    result = new Chef(this, decoder);
                    //without this check we'd have a security hole
                    if (! opid.equals(result.openerID())) {
                        throw new IllegalArgumentException
                          (opid + " must match " + result.openerID());
                    }
                    return registerDecoder(result);
                }
                //if the returned decoding Recipe is null, fall through to
                //attempting a Surgeon
            }
            Class clazz = myClassRecipe.forName(fqn);
            result = new Surgeon(this, clazz /* XXX , opid*/);
            if (opid.equals(result.openerID())) {
                return registerDecoder(result);

                //XXX If Surgeon creation is still expensive, we
                //should see if we can validly registerEncoder(result)
                //as well, and do so if legal.  This will be hard.
                //Lets hope Surgeon creation is no longer expensive
            }
            throw new IllegalArgumentException
              ("Not found in current system: " + opid);

        }
        if (opid instanceof ArrayOpenerID) {
            result = new ArrayOpener(this,
                                     ((ArrayOpenerID)opid).elementType());
            registerDecoder(result);
            registerEncoder(result);
            return result;
        }
        if (opid instanceof StringOpenerID) {
            throw new IllegalArgumentException
              ("Internal: StringOpener should already be registered");
        }
        throw new IllegalArgumentException
          ("unrecognized OpenerID type: " + opid);
    }
}

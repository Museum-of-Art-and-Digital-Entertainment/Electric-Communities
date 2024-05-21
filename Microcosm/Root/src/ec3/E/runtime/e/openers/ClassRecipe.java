package ec.e.openers;

import ec.vcache.ClassCache;
import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import ec.e.util.EmptyEnumeration;


/**
 * A ClassRecipe cooks up a Class object given its fully qualified
 * name, and decides which FieldKnives to approve to a Surgeon that
 * would operate on instances of that Class.  The authority and "point
 * of view" of an OpenerRecipe--and all the Openers it makes--flows
 * from the ClassRecipe it is parameterized with (as well as any
 * authority that may be present in Recipes in the cookbook, but these
 * rarely possess any). <p>
 *
 * The RootClassRecipe grants full authority: it looks up Classes
 * using the moral equivalent of Class.forName(), and it approves all
 * FieldKnives.  Other ClassRecipes can present less authority by
 * wrapping a more powerful ClassRecipe but filtering requests: only
 * allowing a subset of Classes to be looked up (eg, GuestClasses),
 * and only enabling a subset of the FieldKnives (eg, only for
 * non-transient instance variables).
 *
 * @see ec.e.openers.guest.AllowingClassRecipe
 */
public abstract class ClassRecipe extends Recipe {

    static private final Object[][] PrefaceParams = {
        { JavaUtil.STRING_TYPE,    "signature" },
    };

    static private final Object[][] BodyParams = {};

    static private final Class ResultType = JavaUtil.CLASS_TYPE;

    protected ClassRecipe() {
        super(PrefaceParams, BodyParams, ResultType);
    }


    /**
     * If fqn is the fully qualified name for a Class this
     * ClassRecipe is willing to see instantiated raw and operated on,
     * it returns this Class.  Otherwise it throws an exception (to be
     * documented XXX).  Only the RootClassLoader can truly get the
     * Class from nothing but a String.  <p>
     *
     * Wrapping ClassRecipes enforce their policy by choosing whether
     * to forward to their wrapped ClassRecipe or throw an exception.
     * With this protocol, a wrapping ClassLoader cannot provide any
     * Classes its wrapped ClassLoader does not provide.
     */
    public abstract Class forName(String fqn);


    /**
     * Returns successfully iff this ClassRecipe allows this class.
     */
    public void ensureAllowed(Class clazz) {
        if (clazz != forName(clazz.getName())) {
            throw new IllegalArgumentException("may not load: " + clazz);
        }
    }


    /**
     * This happens during initialization of a Surgeon in order to
     * determine which instance variables it may operate on.  Only the
     * RootClassRecipe can actually approve a FieldKnife, and it will
     * approve any.  <p>
     *
     * Wrapping ClassRecipes attempt to approve a knife by asking
     * their wrapped ClassRecipe to approve them.  A wrapping
     * ClassRecipe declines to approve simply by silently not doing
     * anything.  With this protocol, a wrapping ClassRecipe cannot
     * approve any knife that its wrapped ClassRecipe is unwilling to
     * approve.
     */
    public abstract void approve(FieldKnife knife);


    /*********** implementing the Recipe interface ***********/

    /** returns one arg: 'clazz's signature string */
    public Object[] prefaceArgs(Object clazz) {
        String[] result = { JavaUtil.signature(((Class)clazz).getName()) };
        return result;
    }

    /** no body */
    public Object[] bodyArgs(Object clazz) {
        return NO_ARGS;
    }

    public Object halfBakedInstance(Object[] prefaceArgs) {
        return forName(JavaUtil.fullyQualifiedName((String)prefaceArgs[0]));
    }

    /**
     * ClassRecipes don't support halfBakedInstanceOf()
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        throw new RuntimeException("should not implement " + sub);
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {
        //already cooked, do nothing
    }


    /**
     * Returns the value of the named 'static public' field of the
     * clazz, or one inherited from a super class/interface
     *
     * @return nullOk;
     */
    static public Object staticValue(Class clazz, String fieldName)
         throws IllegalAccessException, NoSuchFieldException
    {
        Field field = clazz.getField(fieldName);
        int mods = field.getModifiers();
        if (! Modifier.isPublic(mods)) {
            /*
             * We have to do this check ourselves to avoid a Confused
             * Deputy attack.  Field.get() checks whether the field is
             * accessible to its caller, not whether it is public.
             */
            throw new IllegalAccessException(clazz + "." + fieldName
                                             + " must be public");
        }
        if (! Modifier.isStatic(mods)) {
            /*
             * We do this redundant check just to yield a more
             * informative error than NullPointerException
             */
            throw new IllegalAccessException(clazz + "." + fieldName
                                             + " must be static");
        }
        return field.get(null);
    }
}


/**
 * The RootClassRecipe simply uses the moral equivalent of
 * Class.forName(), and so is using who knows what ClassLoader.  (The
 * code below makes some attempt to use the same ClassLoader that
 * loaded this RootClassRecipe.)  Come EVM, this will probably be
 * parameterized with a ClassLoader.
 */
public final class RootClassRecipe extends ClassRecipe {

    /** myLoader nullOk; */
    private ClassLoader myLoader;

    /**
     * XXX SECURITY BUG.  This must be exported, but in a closely held
     * fashion.  This 'static public' stuff's gotta go.
     */
    static public final ClassRecipe THE_ONE
        = new RootClassRecipe(null);

    static public final String[][] NO_PAIRS = {};

    /**
     * XXX SECURITY BUG.  This should be exported, but in a closely
     * held fashion.  This 'static public' stuff's gotta go.
     */
    static public final OpenerRecipe ROOT_OPENER_RECIPE
        = new OpenerRecipe(NO_PAIRS, NO_PAIRS, RootClassRecipe.THE_ONE);

    /**
     * XXX SECURITY BUG.  This should be exported, but in a closely
     * held fashion.  This 'static public' stuff's gotta go.
     */
    static public final Chef ROOT_OPENER_CHEF
        = new Chef(ROOT_OPENER_RECIPE, ROOT_OPENER_RECIPE);

    private RootClassRecipe(ClassLoader loader) {
        super();
        myLoader = loader;
    }


    /**
     * fqn is a name according to Steele's Class.getName() documentation.
     */
    public Class forName(String fqn) {
        Class result = (Class)JavaUtil.PrimTypes.get(fqn, null);
        if (result != null) {
            return result;
        }
        try {
            if (myLoader == null) {
                /*
                 * XXX The intention is that if myLoader == null, we
                 * use the "null" ClassLoader, but the following code
                 * instead probably uses the same ClassLoader that
                 * this RootClassRecipe was loaded by
                 */
                return ClassCache.forName(fqn);
            } else {
                return myLoader.loadClass(fqn);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Not found " + fqn + " " + ex);
        }
     }

    /**
     *
     */
    public void approve(FieldKnife knife) {
        knife.enable();
    }
}


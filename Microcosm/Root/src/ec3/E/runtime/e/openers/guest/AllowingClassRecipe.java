package ec.e.openers;

import ec.vcache.ClassCache;
import ec.tables.TableEditor;
import ec.tables.TableEditorImpl;
import ec.util.NestedException;
import ec.e.openers.ClassRecipe;
import ec.e.openers.FieldKnife;
import ec.e.openers.JavaUtil;
import java.util.Enumeration;


/**
 * Wraps another ClassRecipe, subsetting its authority to allow only a
 * certain set of classes.
 */
public class AllowingClassRecipe extends ClassRecipe {

    static private final Trace tr
        = new Trace("ec.e.openers.AllowingClassRecipe");

    private ClassRecipe myWrapped;
    private Class myAllowedMarker;
    private String myName;
    private TableEditor myAllowedClasses;
    private TableEditor myNotAllowed;
    private int myElseMode;

    /**
     * The new Recipe subsets the authority of 'wrapped' by
     * allowing only classes whose fully-qualified name is in
     * 'allowedClasses' and not in 'notAllowed', or which are a subtype of
     * 'allowedMarker' (typically, a marker interface).
     *
     * @param wrapped; Where we delegate requests we approve
     * @param allowedMarker; A marker interface which extends
     * SerializableMarker.  All classes which implement the
     * allowedMarker are allowed.
     * @param allowedClasses; A LitTree of the fully-qualified
     * names of the classes that should be allowed, even if they don't
     * implement allowedMarker.
     * @param notAllowed; Classes that should cause an exception to be
     * thrown when an attempt is made to serialize them, independent of
     * whether they are allowed or what the elseMode is.
     * @param elseMode; One of IGNORE, WARNING, VERBOSE_WARNING, or
     * ERROR, which determines how we handle a class that "should" not
     * be serialized, ie, that doesn't implement allowedMarker, and
     * doesn't appear in either allowedClasses or notAllowed.
     *
     * @see ec.e.opener.guest.SerializableMarker
     * @see ec.tables.Table#putAll
     */
    public AllowingClassRecipe(ClassRecipe wrapped,
                               Class allowedMarker,
                               Object[] allowedClasses,
                               Object[] notAllowed,
                               int elseMode) {
        myWrapped = wrapped;

        myAllowedMarker = allowedMarker;
        myName = allowedMarker.getName();
        //last identifier
        myName = myName.substring(myName.lastIndexOf('.') +1);

        myAllowedClasses = new TableEditorImpl(JavaUtil.STRING_TYPE, Void.TYPE);
        myAllowedClasses.putAll(allowedClasses, null, true);
        myAllowedClasses.removeAll(notAllowed, false);

        myNotAllowed = new TableEditorImpl(JavaUtil.STRING_TYPE, Void.TYPE);
        myNotAllowed.putAll(notAllowed, null, true);

        myElseMode = elseMode;

    }


    /**
     * Supports the convenient pattern of having the marker interface
     * also declare 'static public final' variables ALLOWED_CLASSES,
     * ALLOWED_ANYWAY, and ELSE_MODE corresponding to
     * AllowingClassRecipe's other constructor arguments, and
     * MODIFIER_MASK and DISALLOWED_FIELDS corresponding to
     * SharpeningClassRecipe's construction arguments.
     */
    static public ClassRecipe make(ClassRecipe wrapped, Class allowedMarker)
         throws IllegalAccessException, NoSuchFieldException
    {
        int mask = ((Integer)staticValue(allowedMarker,
                                         "MODIFIER_MASK")).intValue();
        String[][] disallowedFields
          = (String[][])staticValue(allowedMarker,
                                    "DISALLOWED_FIELDS");

        if (mask != 0 || disallowedFields.length > 0) {
            wrapped = new SharpeningClassRecipe(wrapped,
                                                mask,
                                                disallowedFields);
        }

        return new AllowingClassRecipe
          (wrapped,
           allowedMarker,
           (Object[])staticValue(allowedMarker, "ALLOWED_CLASSES"),
           (Object[])staticValue(allowedMarker, "NOT_ALLOWED"),
           ((Integer)staticValue(allowedMarker, "ELSE_MODE")).intValue());
    }


    /**
     * A convenience, especially for use in static initializers, where
     * it's awkward to use things that throw non-Runtime Exceptions.
     */
    static public ClassRecipe make(ClassRecipe wrapped,
                                   String allowedMarkerClass) {
        try {
            return make(wrapped, ClassCache.forName(allowedMarkerClass));
        } catch (Exception ex) {
            tr.errorm("making a ClassRecipe", ex);
            throw new NestedException("making a ClassRecipe", ex);
        }
    }


    /**
     * If "Foo" is allowed, then so is "[Foo", "[[Foo", etc..
     */
    public Class forName(String fqn) {
        /*
         * This is definitely too tricky to do without a comment.  If
         * there are no '['s, i will be -1 and i+1 will be 0.  Thought
         * of another way, lastIndexOf fails more harmoniously than
         * indexOf, in that it always returns the position immediately
         * beyond the last character that didn't match.
         */
        Class result = myWrapped.forName(fqn);

        int i = fqn.lastIndexOf('[');
        String base = fqn.substring(i+1);

        if (base.charAt(base.length() -1) == ';') {
            if (base.charAt(0) != 'L') {
                throw new IllegalArgumentException
                  (base + " isn't a valid fully qualified class name");
            }
            base = base.substring(1, base.length() -1);
        }

        if (myAllowedClasses.containsKey(base)) {
            //We don't need to check myNotAllowed, because its
            //contents have already been removed from myAllowedClasses.
            return result;
        }
        if (myNotAllowed.containsKey(base)) {
            throw new IllegalArgumentException(myName + " forbidden: " + base);
        }
        if (myAllowedMarker.isAssignableFrom(result)) {
            return result;
        }
        switch (myElseMode) {
            case SerializableMarker.IGNORE: {
                return result;
            }
            case SerializableMarker.WARNING: {
                if (tr.warning && Trace.ON) {
                    tr.warningm("Warning, " + myName + " opening " + base);
                }
                return result;
            }
            case SerializableMarker.VERBOSE_WARNING: {
                if (tr.warning && Trace.ON) {
                    tr.warningm("Warning: " + myName + " opening " + base,
                                new Exception());
                }
                return result;
            }
            case SerializableMarker.ERROR: {
                throw new IllegalArgumentException(myName +
                                                   " may not open " + base);
            }
            default: {
                throw new Error("unrecognized: " + myElseMode);
            }
        }

    }

    /**
     *
     */
    public void approve(FieldKnife knife) {
        myWrapped.approve(knife);
    }
}



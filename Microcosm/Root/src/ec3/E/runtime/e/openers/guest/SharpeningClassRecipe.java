package ec.e.openers;

import ec.e.openers.ClassRecipe;
import ec.e.openers.FieldKnife;
import ec.e.openers.JavaUtil;
import ec.tables.TableEditor;
import ec.tables.TableEditorImpl;
import java.util.Enumeration;


/**
 * Disallows (by failing to approve) certain fields of certain
 * classes. <p>
 *
 * XXX Should this class disappear, and have it's functionality
 * absorbed by AllowingClassRecipe?
 */
public class SharpeningClassRecipe extends ClassRecipe {

    static public final String[][] STD_DISALLOWED_FIELDS = {
        { "java.lang.Throwable",            "backtrace" },
        { "ec.regexp.RegularExpression",    "raw_bits", "raw_offsets" },
        { "ec.e.start.Tether",              "myHeld" },
        { "ec.e.hold.DataHolderSteward",    "myDataRequestor" },
    };

    private ClassRecipe myWrapped;
    private int myModifierMask;
    private TableEditor myDisallowedFields;

    /**
     * The new Recipe subsets the authority of 'wrapped' by disallowing
     * any fields whose modifiers intersect with those of
     * 'modifierMask'.
     *
     * @param wrapped Where we delegate requests we approve
     * @param modifierMask Each one bit in here represents a forbidden
     * Field modifier.  For example, if the Modifier.TRANSIENT bit is
     * on, we will not ask myWrapped to approve FieldKnives
     * representing transient instance variables.
     * @param disallowedFields nullOk; An array of disallowed field
     * descriptions, where each description is an array whose zeroth
     * member is the fully qualified name of a class, and the rest of
     * whose members are names of fields of that class.  These fields
     * are disallowed, meaning they won't get opened/encoded, and will
     * be "decoded" as null (or zero, for scalar fields).  If null,
     * then no fields are disallowed.
     *
     * @see ec.tables.Table#putMulti
     */
    public SharpeningClassRecipe(ClassRecipe wrapped,
                                 int modifierMask,
                                 String[][] disallowedFields) {
        myWrapped = wrapped;
        myModifierMask = modifierMask;
        myDisallowedFields = new TableEditorImpl(JavaUtil.STRING_TYPE, 
                                       JavaUtil.TABLE_TYPE);
        if (disallowedFields != null) {
            myDisallowedFields.putMulti(disallowedFields, false);
        }
    }

    /**
     *
     */
    public Class forName(String fqn) {
        return myWrapped.forName(fqn);
    }

    /**
     *
     */
    public void approve(FieldKnife knife) {
        if ((myModifierMask & knife.modifier()) == 0) {
            TableEditor fields
              = (TableEditor)myDisallowedFields.get(knife.baseType().getName(),
                                              null);
            if (fields != null && fields.containsKey(knife.name())) {
                return;
            }
            myWrapped.approve(knife);
        }
    }
}



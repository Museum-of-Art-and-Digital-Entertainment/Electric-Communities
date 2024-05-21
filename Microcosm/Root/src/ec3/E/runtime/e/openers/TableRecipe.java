package ec.e.openers;

import java.lang.reflect.Array;
import java.util.Enumeration;
import ec.tables.Table;
import ec.tables.TableEditor;


/**
 * Table is also hash-based, so it needs a Recipe
 *
 * @see ec.e.openers.HashtableRecipe
 */
public class TableRecipe extends Recipe {

    /**
     * "Object" is the only way to say "array" generically in Java
     */
    static final Class ARRAY_TYPE = JavaUtil.OBJECT_TYPE;

    static private final Object[][] PrefaceParams = {
        { Boolean.TYPE,             "isIdentity" },
        { JavaUtil.CLASS_TYPE,      "keyType" },
        { JavaUtil.CLASS_TYPE,      "valueType" },
        { Integer.TYPE,             "size" },
    };

    static private final Object[][] BodyParams = {
        { ARRAY_TYPE,       "keys" },
        { ARRAY_TYPE,       "values" },
    };

    static private final TableRecipe THE_ONE = new TableRecipe();

    private TableRecipe() {
        super(PrefaceParams, BodyParams, JavaUtil.TABLE_TYPE);
    }

    static public Recipe makeEncoder() {
        return THE_ONE;
    }

    static public Recipe makeDecoder(OpenerID opid) {
        if (THE_ONE.openerID().equals(opid)) {
            return THE_ONE;
        } else {
            return null;
        }
    }

    public Object[] prefaceArgs(Object obj) {
        Table tab = (Table)obj;
        /* XXX removed in translation from ec3 to ec4 -emm
        if (tab.weakness() != Table.STRONG) {
            throw new RuntimeException
              ("not yet implemented: serializing weak tables");
        }
        */
        Object[] result = {
            (tab.isIdentity() ? Boolean.TRUE : Boolean.FALSE),
            tab.keyType(),
            tab.valueType(),
            new Integer(tab.size()),
        };
        return result;
    }

    public Object[] bodyArgs(Object obj) {
        Table tab = (Table)obj;
        int size = tab.size();
        Object keys = Array.newInstance(tab.keyType(), size);
        Object vals = Array.newInstance(tab.valueType(), size);
        Object[] result = { keys, vals };

        Enumeration iter = tab.keys();
        for (int i = 0; i < size; i++) {
            Object key = iter.nextElement();
            Array.set(keys, i, key);
            Array.set(vals, i, tab.get(key));
        }
        if (iter.hasMoreElements()) {
            throw new RuntimeException
                ("size() " + size + " disagrees with keys()");
        }
        return result;
    }

    /* XXX my guess is that this will need to make Table's which are
       read only.  It's got to write the elements into them, so it has
       to be given a TableEditor.  There has to be some way to turn
       that into a Table where appropriate.  I'm ignoring this in
       converting from ec3 to ec4 -emm
     */
    public void cook(Object halfBaked, Object[] bodyArgs) {
        TableEditor tab = (TableEditor)halfBaked;
        Object keys = bodyArgs[0];
        Object vals = bodyArgs[1];
        int len = Array.getLength(keys);
        int vLen = Array.getLength(vals);
        if (len != vLen) {
            throw new IllegalArgumentException
                ("not matched " + len + ", " + vLen);
        }
        for (int i = 0; i < len; i++) {
            tab.put(Array.get(keys, i), Array.get(vals, i), true);
        }
    }
}


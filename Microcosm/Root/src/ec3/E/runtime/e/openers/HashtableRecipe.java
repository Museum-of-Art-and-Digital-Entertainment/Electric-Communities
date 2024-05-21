package ec.e.openers;

import java.util.Hashtable;
import java.util.Dictionary;
import java.util.Enumeration;
import ec.tables.PEHashtable;
import ec.tables.ObjKeyTable;


/**
 * Abstracts the issues in common among hash-based Dictionaries.
 *
 * @see ec.e.openers.HashtableRecipe
 */
public abstract class DictionaryRecipe extends Recipe {

    static final Class OBJ_ARRAY_TYPE = new Object[0].getClass();

    static private final Object[][] PrefaceParams = {
        { Integer.TYPE,     "size" },
    };

    static private final Object[][] BodyParams = {
        { OBJ_ARRAY_TYPE,   "keys" },
        { OBJ_ARRAY_TYPE,   "values" },
    };

    public DictionaryRecipe(Class resultType) {
        super(PrefaceParams, BodyParams, resultType);
    }

    public Object[] prefaceArgs(Object obj) {
        Object[] result = { new Integer(((Dictionary)obj).size()) };
        return result;
    }

    public Object[] bodyArgs(Object obj) {
        Dictionary tab = (Dictionary)obj;
        int size = tab.size();
        Object[] keys = new Object[size];
        Object[] vals = new Object[size];
        Object[][] result = { keys, vals };

        Enumeration iter = tab.keys();
        for (int i = 0; i < size; i++) {
            Object key = iter.nextElement();
            keys[i] = key;
            vals[i] = tab.get(key);
        }
        if (iter.hasMoreElements()) {
            throw new RuntimeException
                ("size() " + size + " disagrees with keys()");
        }
        return result;
    }

    /**
     * Defaults to calling halfBakedInstanceOf using type().
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        return halfBakedInstanceOf(type(), prefaceArgs);
    }

    /**
     * Defaults to ignoring the prefaceArgs and just returns a
     * sub.newInstance()
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        try {
            return sub.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("can't instantiate " + sub);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("may not instantiate " + sub);
        }
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {
        Dictionary tab = (Dictionary)halfBaked;
        Object[] keys = (Object[])bodyArgs[0];
        Object[] vals = (Object[])bodyArgs[1];
        if (keys.length != vals.length) {
            throw new IllegalArgumentException
                ("not matched " + keys.length + ", " + vals.length);
        }
        for (int i = 0; i < keys.length; i++) {
            if (tab.put(keys[i], vals[i]) != null) {
                throw new IllegalArgumentException
                    ("keys collided " + keys[i]);
            }
        }
    }
}


/**
 * Since an object's hash may not be preserved by encode/decode,
 * copying a Hashtable structurally may result in an incorrect
 * Hashtable because the key's new hashes no longer correspond to
 * their place in the structure.  To address this we use
 * a HashtableRecipe which will encode/decode the Hashtable as if it
 * had two instance variables: an array of keys and an array of
 * values.  In order to decode quickly, we also encode the Hashtable's
 * size() as a prefaceArg.
 */
public class HashtableRecipe extends DictionaryRecipe {

    static public final Class HASHTABLE_TYPE = new Hashtable().getClass();

    static private final HashtableRecipe THE_ONE = new HashtableRecipe();

    private HashtableRecipe() {
        super(HASHTABLE_TYPE);
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

    public Object halfBakedInstance(Object[] prefaceArgs) {
        int size = ((Integer)prefaceArgs[0]).intValue();
        if (size == 0) {
            //even if there are only to be zero elements, Hashtable
            //doesn't like it.
            size = 1;
        }
        return new Hashtable(size);
    }
}

/**
 *
 * @see ec.e.openers.HashtableRecipe
 */
public class PEHashtableRecipe extends DictionaryRecipe {

    static public final Class PEHASHTABLE_TYPE = new PEHashtable().getClass();

    static private final PEHashtableRecipe THE_ONE = new PEHashtableRecipe();

    private PEHashtableRecipe() {
        super(PEHASHTABLE_TYPE);
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

    public Object halfBakedInstance(Object[] prefaceArgs) {
        int size = ((Integer)prefaceArgs[0]).intValue();
        if (size == 0) {
            //even if there are only to be zero elements, PEHashtable
            //doesn't like it.
            size = 1;
        }
        return new PEHashtable(size);
    }
}


/**
 * ObjKeyTable is also hash-based, so it needs a Recipe
 *
 * @see ec.e.openers.HashtableRecipe
 */
public class ObjKeyTableRecipe extends Recipe {

    static final Class OBJ_ARRAY_TYPE = new Object[0].getClass();
    static final Class INT_ARRAY_TYPE = new int[0].getClass();

    static private final Object[][] PrefaceParams = {
        { Integer.TYPE,     "noSuchKey" },
        { Integer.TYPE,     "size" },
        { Boolean.TYPE,     "useEquals" },
    };

    static private final Object[][] BodyParams = {
        { OBJ_ARRAY_TYPE,   "keys" },
        { INT_ARRAY_TYPE,   "values" },
    };

    static public final Class OBJKEYTABLE_TYPE 
        = new ObjKeyTable(-1).getClass();

    static private final ObjKeyTableRecipe THE_ONE = new ObjKeyTableRecipe();

    private ObjKeyTableRecipe() {
        super(PrefaceParams, BodyParams, OBJKEYTABLE_TYPE);
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
        ObjKeyTable tab = (ObjKeyTable)obj;
        Object[] result = {
            new Integer(tab.noSuchKey()),
            new Integer(tab.size()),
            (tab.usingEquals() ? Boolean.TRUE : Boolean.FALSE)
        };
        return result;
    }

    public Object[] bodyArgs(Object obj) {
        ObjKeyTable tab = (ObjKeyTable)obj;
        int size = tab.size();
        Object[] keys = new Object[size];
        int[] vals = new int[size];
        Object[] result = { keys, vals };

        Enumeration iter = tab.keys();
        for (int i = 0; i < size; i++) {
            Object key = iter.nextElement();
            keys[i] = key;
            vals[i] = tab.get(key);
        }
        if (iter.hasMoreElements()) {
            throw new RuntimeException
                ("size() " + size + " disagrees with keys()");
        }
        return result;
    }

    /**
     *
     */
    public Object halfBakedInstance(Object[] prefaceArgs) {
        return new ObjKeyTable(((Integer)prefaceArgs[0]).intValue(),
                               ((Integer)prefaceArgs[1]).intValue(),
                               ((Boolean)prefaceArgs[2]).booleanValue());
    }

    /**
     * Defaults to ignoring the prefaceArgs and just returns a
     * sub.newInstance()
     */
    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        try {
            return sub.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("can't instantiate " + sub);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("may not instantiate " + sub);
        }
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {
        ObjKeyTable tab = (ObjKeyTable)halfBaked;
        int noSuchKey = tab.noSuchKey();
        Object[] keys = (Object[])bodyArgs[0];
        int[] vals = (int[])bodyArgs[1];
        if (keys.length != vals.length) {
            throw new IllegalArgumentException
                ("not matched " + keys.length + ", " + vals.length);
        }
        for (int i = 0; i < keys.length; i++) {
            if (tab.put(keys[i], vals[i]) != noSuchKey) {
                throw new IllegalArgumentException
                    ("keys collided " + keys[i]);
            }
        }
    }
}


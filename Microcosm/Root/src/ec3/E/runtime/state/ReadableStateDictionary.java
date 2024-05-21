package ec.state;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ec.util.NestedException;

/**
 * Read only wrapper around a WriteableStateDictionary that is passed to a Stateful
 * Object being decoded. A ReadableStateDictionary only contains public methods to
 * get values of mappings in the WriteableStateDictionary.
 *
 * @see ec.state.StateDictionary
 * @see ec.state.Stateful
 * @see ec.state.WriteableStateDictionary
 */
public class ReadableStateDictionary
{
    private WriteableStateDictionary dictionary;

    /**
     * Get the value of a String mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public String getStringMapping (String key)  {
        Object object = null;
        String s = null;
        try {
            object = dictionary.getMapping(key);
            s = (String)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get String mapping for " + object.getClass(), e);
        }
        if (s == null) {
            return null; // Valid mapping for String
        }
        return s.substring(1, s.length()-1);
    }

    /**
     * Get the value of a boolean mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public boolean getBooleanMapping (String key)  {
        Object object = null;
        String s = null;
        try {
            object = dictionary.getMapping(key);
            s = (String)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get boolean mapping for " + object.getClass(), e);
        }
        if (s == null) {
            // Squawk?
            return false;
        }
        return (StateDictionary.TrueValue.equals(s)) ? true : false;
    }

    /**
     * Get the value of a double mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public double getDoubleMapping (String key)  {
        Object object = null;
        String s = null;
        try {
            object = dictionary.getMapping(key);
            s = (String)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get double mapping for " + object.getClass(), e);
        }
        if (s == null) {
            // Squawk?
            return 0.0;
        }
        return (new Double(s)).doubleValue();
    }

    /**
     * Get the value of an int mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public int getIntMapping (String key)  {
        Object object = null;
        String s = null;
        try {
            object = dictionary.getMapping(key);
            s = (String)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get int mapping for " + object.getClass(), e);
        }
        if (s == null) {
            // Squawk...
            return 0;
        }
        return (new Integer(s)).intValue();
    }

    /**
     * Get the value of a Hashtable mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public Hashtable getHashtableMapping (String key)  {
        Object object = null;
        HashtableState hs = null;
        try {
            object = dictionary.getMapping(key);
            hs = (HashtableState)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get Hashtable mapping for " + object.getClass(), e);
        }
        if (hs == null) {
            // Squawk?
            return null;
        }
        return hs.getHashtable();
    }

    /**
     * Get the value of a Vector mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public Vector getVectorMapping (String key)  {
        Object object = null;
        VectorState vs = null;
        try {
            object = dictionary.getMapping(key);
            vs = (VectorState)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get Vector mapping for " + object.getClass(), e);
        }
        if (vs == null) {
            // Squawk?
            return null;
        }
        return vs.getVector();
    }

    /**
     * Get the value of a Stateful Object mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public Stateful getStatefulMapping (String key)  {
        Object object = null;
        Stateful s = null;
        try {
            object = dictionary.getMapping(key);
            s = (Stateful)object;
        } catch (ClassCastException e) {
            throw new NestedException("Attempt to get Stateful mapping for " + object.getClass(), e);
        }
        return s;
    }

    /**
     * Get the value of an Object mapping.
     *
     * @param key The key for the mapping.
     * @return The value of the mapping.
     */
    public Object getObjectMapping (String key)  {
        Object object = dictionary.getMapping(key);
        if (object instanceof String) {
            String s = (String) object;
            object = s.substring(1, s.length()-1);
        }
        return object;
    }

    /**
     * Constructor to wrap a WriteableStateDictionary
     *
     * @param dictionary The Writeable State Dictionary to be wrapped
     */
    /* package */ ReadableStateDictionary (WriteableStateDictionary dictionary)  {
        this.dictionary = dictionary;
    }
}

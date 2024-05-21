package ec.vcache;

import java.util.Hashtable;

/**
 *
 */
public class ClassCache {

    /**
     * Using java.util.Hashtables instead of ec.tables.Tables in order
     * to avoid circular dependencies.  ClassCache wants to come
     * before those that were using Class.forName(), and in the ec3
     * hierarchy, ec.tables builds on the E runtime.
     */
    static private final Hashtable OurCache = new Hashtable();

    /**
     *
     */
    static private final boolean ON = true;

    /** prevent instantiation */
    private ClassCache() {}

    /**
     * Intended to be synonymous with Class.forName(name), except for
     * performance.  If the static private ON variable is set to true,
     * then this caches the class by its name.  This should be
     * redundant since Class.forName() already does this.  However,
     * there's suspicion it does this badly.  For testing, or if
     * there's no performance win, set ON to false and this becomes
     * identical to Class.forName(), modulo the overhead of an extra call.
     */
    static public Class forName(String name) throws ClassNotFoundException {
        if (ON) {
            Class result = (Class)OurCache.get(name);
            if (result == null) {
                result = Class.forName(name);
                OurCache.put(name, result);
            }
            return result;
        } else {
            return Class.forName(name);
        }
    }
}



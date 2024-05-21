package dom.session;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Custom class loader for multipoint View.
 *
 * @see View#loadDObject
 *
 * @author Scott Lewis
 */
class ViewClassLoader extends ClassLoader
{
    View myView;
    Hashtable myCache;

    //static final boolean debug = true;
    static final boolean debug = false;

    /**
     * Constructor.  Protected so only classes within package can call.
     *
     * @param view the view this class loader is associated with.
     */
    protected ViewClassLoader(View view)
    {
        this.myView = view;
        myCache = new Hashtable();
    }

    /**
     * Read the raw bytes for the given class name.
     * Note that use of WebObject synchronizes multiple concurrent attempts
     * to fetch the same data; it will only be fetched once, and this copy
     * returned to all callers.
     *
     * @param className the name of the class to load (e.g., java.util.Vector).
     * @return the bytes defining the class, or null.
     */
    private byte[] readBytes(String className)
    {
        try {
            String classPath = className.replace('.','/') + ".class";
            // XXX This is where the required bytes should be read (from disk, net or wherever)
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     */
    public Class loadClass(URL codeBase, String className)
        throws Exception
    {
        // TODO
        return null;
    }
    
    /**
     * Load a class given its name.  Fully resolve the class before
     * returning.
     *
     * @param className the name of the class to load (e.g., java.util.Vector).
     * @return the class, or null.
     */
    public Class loadClass(String className)
    {
        return loadClass(className, true);
    }

    /**
     * Load a class given its name.  Optionally resolve the class before
     * returning.
     *
     * @param className the name of the class to load (e.g., java.util.Vector).
     * @return the class, or null.
     */

    public Class loadClass(String className, boolean resolve)
    {
        if (debug) System.out.println("Attempting to load: "+className);

        if (isSystemClass(className)) {
            if (debug) System.out.println("\tNo system classes: "+className);
            try {
                return findSystemClass(className);
            }
            catch (ClassNotFoundException e) {
                if (debug) System.out.println("Unable to find system class "
                                                + className + " exception:" + e);
                return null;
            }
        }

        Class cls = (Class) myCache.get(className);

        if (cls == null) {
            byte data[] = readBytes(className);
            if (data != null) {
                cls = defineClass(className, data);
            }
            if (cls == null) {
                if (debug) System.out.println("\tFailed to load: "+className);
                return null;
            }
        } else {        // found in cache
            if (debug) System.out.println("\tFound in cache: "+className);
        }

        /*
         * If need be, fully resolve the class before returning.
         */

        if (resolve) {
            if (debug) System.out.println("\tResolving: "+className);
            resolveClass(cls);
            if (debug) System.out.println("\tResolved: "+className);
        }
        if (debug) System.out.println(((cls != null) ? "\tLoaded: " : "\tFailed to load: ")+className);
        return cls;
    }

    /**
     * Define the class.
     *
     * @param className the name of the class.
     * @param data the bytes comprising the class definition.
     * @return the new class definition (or the old one if race has
     * already defined one); returns
     * null if fail to define the class.
     */

    protected synchronized Class defineClass(String className, byte[] data)
    {
        Class cls = (Class) myCache.get(className);
        if (cls != null) return cls;

        cls = defineClass(data, 0, data.length);
        if (cls != null) {
            myCache.put(className, cls);
        }
        return cls;
    }

    protected boolean isSystemClass(String className)
    {
        if (className.startsWith("java.") || className.startsWith("dom.")) {
            return true;
        }

        return false;
    }

    /**
     * Flush the cache of this class loader instance.
     */
    protected synchronized void flushCache()
    {
        myCache.clear();
    }

    protected synchronized void flushCache(String className)
    {
        myCache.remove(className);
    }

}

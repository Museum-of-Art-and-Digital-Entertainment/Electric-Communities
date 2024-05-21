package ec.e.cap;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * A container for named objects.  Passed to an ELaunchable when
 * launched.
 * <pre>
 * public interface:
 * 
 *     EEnvironment();
 * 
 *     deprecated:
 *      EEnvironment copy();
 *      void restrict (String name, String kind, String value);
 * 
 *     void setProperties(Properties p);
 *     Properties getProperties();
 *     void setArgs(String a[]);
 *     String[] getArgs();
 * 
 *     String getProperty(String key);
 *     String getProperty(String key, String def);
 *     Integer getPropertyAsInteger(String key);
 *     int getPropertyAsInt(String key);
 *     Integer getPropertyAsInteger(String key, Integer def);
 *     int getPropertyAsInt(String key, int def);
 *     boolean getPropertyAsBoolean (String key);
 * 
 *     Object get(String name);
 *     void put(String name, Object value);
 * </pre>
 * Anyone can create a blank EEnvironment.  It will have no objects. <p>
 * 
 * Deprecated: To copy an EEnvironment, use copy().  Once you've copy()'d
 * an EEnvironment, you can restrict() the copy without affecting the
 * original. <p>
 * 
 * Deprecated: To restrict the capablities in an EEnvironment, call
 * restrict(name, kind, value).  restrict(kind, value) will be called on
 * the named capability.  Two special kind's are handled by EEnvironment
 * itself: copy and remove.  restrict("oldname", "copy", "newname")
 * creates a new copy of a capability "oldname" and makes it available as
 * "newname".  restrict(name, "remove") removes the named capability from
 * this EEnvironment. <p>
 * 
 * An EEnvironment also contains a set of properties and an array of
 * argument strings.  These can be set and retrieved as a whole by
 * setProperties, getProperties, setArgs and getArgs. <p>
 * 
 * Individual properties can also be retrieved by name (with optional
 * default values) by the various getProperty...() functions. <p>
 * 
 * Objects can be stored in and retrieved from the EEnvironment by name
 * using get() and put().
 */

public class EEnvironment extends Hashtable {
    private Properties properties = null;
    private String args[];
    
    /**
     * XXX BUG: This ugly hack doesn't always work.  What about when
     * the provided value happens to be the string
     * "DefaultPropertyValue"? 
     */
    public final static String DefaultPropertyValue = "DefaultPropertyValue";
    
    /**
     * A new empty EEnvironment
     */
    public EEnvironment() {}
    
    public void setProperties(Properties p) {
        properties = p ;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setArgs(String a[]) {
        args = a ;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public String getProperty(String key) {
        if (properties == null) return null;
        return (String)properties.getProperty(key);
    }
    
    public String getProperty(String key, String def) {
        if (properties == null) return def;
        return (String)properties.getProperty(key, def);
    }
    
    public Integer getPropertyAsInteger(String key) {
        String value = getProperty(key);
        if ((value != null) && (value != EEnvironment.DefaultPropertyValue)) {
            return new Integer(value);
        }
        else {
            return null;
        }
    }
    
    public int getPropertyAsInt(String key) {
        String value = getProperty(key);
        if ((value != null) && (value != EEnvironment.DefaultPropertyValue)) {
            return Integer.parseInt(value);
        }
        else {
            return 0;
        }
    }
    
    public Integer getPropertyAsInteger(String key, Integer def) {
        String value = getProperty(key, DefaultPropertyValue);
        if ((value != null) && (value != EEnvironment.DefaultPropertyValue)) {
            return new Integer(value);
        }
        else {
            return def;
        }
    }
    
    public int getPropertyAsInt(String key, int def) {
        String value = getProperty(key, DefaultPropertyValue);
        if ((value != null) && (value != EEnvironment.DefaultPropertyValue)) {
            return Integer.parseInt(value);
        }
        else {
            return def;
        }
    }
    
    public boolean getPropertyAsBoolean (String key) {
        String value = getProperty(key, "false");
        return value.equals("true") ;
    }
}

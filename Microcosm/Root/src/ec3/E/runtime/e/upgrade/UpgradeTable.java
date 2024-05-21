package ec.e.upgrade;

import java.util.Hashtable;

/**
 * An UpgradeTable contains a table mapping Classes to UpgradeConverters.
 * Whenever two systems with different versions connect, the system
 * with the higher (hence newer) version number is responsible for
 * "talking down" to the lower numbered (hence older) system. It does
 * this by Wrapping objects that are both proxied and copied over the
 * wire.
 * 
 * There is a separate UpgradeTable instance for every back version of
 * the system supported. If a Version 99 system supports versions 97
 * and 98, there will be two UpgradeTable instances in system 99. 
 *
 * @see ec.e.upgrade.UpgradeConverter
 */
 
// XXX - Create these and then stuff them into the Repository? 
public class UpgradeTable
{
    private Hashtable /* Class, UpgradeConverter */ myWrappers; /** Table of Converters */
    private String myDescription; /** Descriptive name for this table */
    
    /**
     * Constructor. Takes descriptive name for this table
     */ 
    public UpgradeTable (String description)  {
        myDescription = description;
        myWrappers = new Hashtable(); // XXX - tune this?
    }   
    
    /** 
     * Add an entry to the table. A Class instance is the key,
     * and an UpgradeConverter is the value returned on lookup.
     */ 
    public void addConverter (Class clazz, UpgradeConverter converter)  {
        myWrappers.put(clazz, converter);
    } 
    
    /** 
     * Lookup an UpgradeConverter Object based on a Class instance
     */ 
    public UpgradeConverter getConverter (Class clazz)  {
        return (UpgradeConverter)myWrappers.get(clazz);
    }
        
    /**
     * Return the descriptive name for this table
     */ 
    public String description ()  {
        return myDescription;
    }   
}


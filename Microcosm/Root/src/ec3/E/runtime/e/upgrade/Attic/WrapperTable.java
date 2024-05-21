package ec.e.upgrade;

import java.util.Hashtable;

/**
 * An WrapperTable contains a table mapping Classes to UpgradeWrapperMakers.
 * Whenever two systems with different versions connect, the system
 * with the higher (hence newer) version number is responsible for
 * "talking down" to the lower numbered (hence older) system. It does
 * this by Wrapping objects that are both proxied and copied over the
 * wire.
 * 
 * There is a separate WrapperTable instance for every back version of
 * the system supported. If a Version 99 system supports versions 97
 * and 98, there will be two WrapperTable instances in system 99. 
 *
 * @see ec.e.upgrade.UpgradeWrapperMaker
 */
 
// XXX - Create these and then stuff them into the Repository? 
public class WrapperTable
{
    private Hashtable /* Class, UpgradeWrapperMaker */ myWrappers; /** Table of WrapperMakers */
    private String myDescription; /** Descriptive name for this table */
    
    /**
     * Constructor. Takes descriptive name for this table
     */ 
    public WrapperTable (String description)  {
        myDescription = description;
        myWrappers = new Hashtable(); // XXX - tune this?
    }   
    
    /** 
     * Add an entry to the table. A Class instance is the key,
     * and an UpgradeWrapper is the value returned on lookup.
     */ 
    public void addWrapperMaker (Class clazz, UpgradeWrapperMaker wrapperMaker)  {
        myWrappers.put(clazz, wrapperMaker);
    } 
    
    /** 
     * Lookup an UpgradeWrapper Object based on a Class instance
     */ 
    public UpgradeWrapperMaker getWrapperMaker (Class clazz)  {
        return (UpgradeWrapperMaker)myWrappers.get(clazz);
    }
    
    /**
     * Return the descriptive name for this table
     */ 
    public String description ()  {
        return myDescription;
    }   
}


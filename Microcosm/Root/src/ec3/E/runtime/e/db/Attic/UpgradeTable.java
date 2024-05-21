package ec.e.db;

import java.util.Hashtable;
import java.io.IOException;

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
 * @see ec.e.db.UpgradeWrapperMaker
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

/**
 * UpgradeWrapperMaker is an "abstract" interface marking all WrapperMakers.
 * When an Object is to be sent or Proxied over the wire, the
 * class for the Object is looked up in the WrapperTable used 
 * for the connection, and if a WrapperMaker is found, it is used (in 
 * the correct context of a "concrete" WrapperMaker Interface) to match
 * versions of the Object between systems.
 *
 * @see ec.e.db.WrapperTable
 *
 * @see ec.e.db.StateWrapperMaker
 * @see ec.e.db.InterfaceWrapperMaker
 */
public interface UpgradeWrapperMaker {
}   

/**
 * InterfaceWrapperMaker is an UpgradeWrapperMaker interface that wrappers
 * for Objects that are Proxied over the wire by reference must implement.
 * When an Object is about to be Proxied and an InterfaceWrapperMaker is 
 * present for it, the InterfaceWrapper is asked to return an Object
 * wrapping the outgoing Object. This wrapper Object is what is actually
 * proxied over the wire.
 *
 * Conversely, when a Proxy is received over the wire and its class
 * maps to an InterfaceWrapperMaker, the InterfaceWrapperMaker is asked
 * to wrap the incoming Object in another Object that can be returned
 * from decoding.
 *
 * @see ec.e.db.UpgradeWrapperMaker
 */
public interface InterfaceWrapperMaker extends UpgradeWrapperMaker
{
    /**
     * Return an Object to encode over the wire.
     */
    Object wrapOut (Object object);
    
    /**
     * Return an Object to wrap a prior version of the
     * Object with.
     */ 
    Object wrapIn (Object object);
}
        
/**
 * StateWrapperMaker is an UpgradeWrapperMaker interface that wrappers
 * for Objects that are sent over the wire by copying their state must
 * implement. When an Object is about to be copied over the wire
 * and a StateWrapperMaker is present for it, the StateWrapperMaker is
 * used to encode the Object onto an Encoder stream such that a prior 
 * version of the class can decode the object on the other side of
 * the wire. 
 *
 * Conversely, when an object is to be decoded and its class is
 * maps to a StateWrapperMaker, the StateWrapperMaker is used to
 * decode the Object.
 *
 * @see ec.e.db.UpgradeWrapperMaker
 */
public interface StateWrapperMaker extends UpgradeWrapperMaker
{
    /**
     * Encode something over the wire which can be read by 
     * a prior version of the class on the other side.
     */
    void encode (Object object, RtEncoder encoder) throws IOException;
    
    /**
     * Decode into the Object (or return another) from a decoder
     * which contains the encoded contents of a prior version of
     * the class.
     */
    Object decode (Object object, RtDecoder decoder) throws IOException;
}       

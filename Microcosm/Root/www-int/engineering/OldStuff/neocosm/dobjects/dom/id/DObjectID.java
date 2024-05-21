/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/id/DObjectID.java $
    $Revision: 1 $
    $Date: 12/27/97 4:09p $
    $Author: Sbl $

    Todo:

****************************************************************************/

package dom.id;

import java.io.*;
import java.net.InetAddress;

/**
 * Representation of an id (or <b>dead reference</b>) for a distributed object.  
 * This presents an opaque id, that can be tested for equality (equals),
 * serialized/deserialized.  Once its state is set on construction,
 * it cannot be changed.
 * <p>
 * NOTE:  This id is opaque, so it could easily be modified to use, say,
 * a SturdyRef underneath.
 * <p>
 * TODO:  Perhaps define an interface that is implemented by this class that
 * declares equals() and hashCode().  Might make for simpler exstension.
 *
 * @version $Revision: 18 $ $Date: 12/17/97 9:43p $
 * @author Scott Lewis
 * @see dom.session.DObject
 * @see dom.session.View#createDObject
 */
public class DObjectID implements Serializable
{
    /**
     * Class variables and initialization
     */
    static InetAddress myHost = null;
    static long myCurrentSerialNumber = 0;
    static long myStartupTime = 0;

    static {
        initializeStaticState();
    }

    private static void initializeStaticState()
    {
        try {
            myStartupTime = System.currentTimeMillis();
            myHost = InetAddress.getLocalHost();
        } catch (Exception e) {
            System.out.println("ERROR: Can't get localhost.  Exiting");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Generate unique serial number (unique in this process for a given clock value).
     *
     * @return unique serial number
     */
    private static synchronized long getNextSerial()
    {
        return myCurrentSerialNumber++;
    }
    
    /**
     * Instance construction.  All instance construction will go through
     * this method, as this is the id factory.  In this way, we can be sure 
     * that things are being done properly/securely, and make strong security 
     * guarantees about the validity of this id in the context provided by
     * the dom code.  This also allows us the flexibility to have (trusted) subclasses 
     * redefine this static method, so that they know about other 'kinds' of 
     * identity.
     * 
     * Likely to be very cool.  Allows identity to be flexibly defined by the
     * specific app, simply by subclassing this class.  In this way, trusted
     * code can introduce new kinds of identity, and still use the rest of
     * the dom.* classes *unmodified*.
     *
     * @return DObjectID new DObjectID instance
     * @exception Exception thrown if for some reason an id cannot be made
     */
    public static DObjectID makeNewID() throws Exception
    {
        return new DObjectID();
    }
    
    /**
     * Instance variables
     */
    protected byte myAddress[];
    protected long myTime;
    protected long mySerial;

    /**
     * Test for equality with another object id.
     *
     * @param other the Object to compare with
     * @return true if object is equal to other object, false otherwise
     */
    public boolean equals(Object otherObject)
    {
        if (otherObject == null) return false;
        try {
            return internalEquals((DObjectID) otherObject);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Internal equality test.  Protected method used in equals.
     *
     * @param the DObjectID to compare to ourselves
     * @return true if equal to this DObjectID, false otherwise.
     */
    protected boolean internalEquals(DObjectID other)
    {
        return ((myAddress[0] == other.myAddress[0]) &&
        (myAddress[1] == other.myAddress[1]) &&
        (myAddress[2] == other.myAddress[2]) &&
        (myAddress[3] == other.myAddress[3]) &&
        (myTime == other.myTime) &&
        (mySerial == other.mySerial));
    }

    /**
     * Generate a hash code for this object.
     *
     * @return hash value.  In the case of this c
     */
    public int hashCode()
    {
        return internalHashCode();
    }

    protected int internalHashCode()
    {
        return (int) (myTime+mySerial);
    }

    /**
     * Make the set of internal values unique.
     *
     * @param unique flag to indicate whether values for id should be
     * made unique (true) or null (false)
     */
    protected void createUniqueID(boolean unique)
    {
        if (unique)
        {
            myAddress = myHost.getAddress();
            myTime = myStartupTime;
            mySerial = getNextSerial();
        } else
        {
            myAddress = new byte[4];
            myTime = 0;
            mySerial = 0;
        }
    }

    /**
     * Determine if one DObjectID preceeds another.
     *
     * @param other an DObjectID.
     * @return true if this Object should preceed other in an ordering.
     */
    public boolean preceeds(DObjectID other)
    {
        if (myTime < other.myTime) return true;
        if (myTime > other.myTime) return false;
        return (mySerial < other.mySerial);
    }

    /**
     * Generate a String representation of this object.
     * @return a text representation of this object
     */
    public String toString()
    {
        return "DObjectID"+"("+shortString()+")";
    }

    public String shortString()
    {
        return     (myAddress[0] & 0xff)
              +"."+(myAddress[1] & 0xff)
              +"."+(myAddress[2] & 0xff)
              +"."+(myAddress[3] & 0xff)
              +":"+myTime+":"+mySerial;
    }

    public String minimalValueString()
    {
        return     (myAddress[0] & 0xff)
              +""+(myAddress[1] & 0xff)
              +""+(myAddress[2] & 0xff)
              +""+(myAddress[3] & 0xff)
              +""+myTime+""+mySerial;
    }

    /**
     * Make a new DObjectID with unique values.
     */
    protected DObjectID()
    {
        createUniqueID(true);
    }

    /**
     * Constructor to create either unique DObjectID (true) or a null
     * id (false).
     *
     * @param makeUnique if true, initialize with new (unique) values.
     * Initialize with null values if false.
     */
    protected DObjectID(boolean makeUnique)
    {
        createUniqueID(makeUnique);
    }

    /**
     * Constructor to make a copy of an DObjectID
     *
     * @param id existing DObjectID to make a copy of.
     */
     protected DObjectID(DObjectID id)
     {
        this(false);
        myAddress = new byte[4];
        for(int i=0; i < 4; i++) myAddress[i] = id.myAddress[i];
        myTime = id.myTime;
        mySerial = id.mySerial;
     }

}


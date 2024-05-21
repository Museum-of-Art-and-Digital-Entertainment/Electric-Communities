/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/DObjectPacket.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import java.io.Serializable;

import dom.serial.Data;
import dom.id.*;

/**
 * A wrapper class to hold data that a DObject can send to its other presences.  
 * Instances of this class are created on behalf
 * of a DObject that wishes to send some message to other presences of itself.
 * Instances are created in the ViewDObjectFacet class, the sendDataToPresences
 * and sendDataToPresenceAtView methods.  These methods are available to the
 * root DObject class, and are therefore available to all DObjects.
 * <p>
 * The ViewDObjectFacet methods turn around and call the analogous methods
 * on the View class with the created DObjectPacket as the parameter.
 * <p>
 * Instances of this class represent on 'unit of message' or 'packet' for
 * serialization and asynchronous delivery (thus this class implements the
 * Serializable interface).  Basically, all it does is provide a wrapper
 * for two data elements:  the DObjectID of the sending DObject, and the Data
 * that is provided by the DObject.  Both of these must be provided for 
 * construction of instances of this class.
 *
 * @author Scott Lewis
 * @see dom.serial.Data
 * @see dom.session.View#sendDataToPresences
 * @see dom.session.View#sendDataToPresenceAtView
 * @see dom.session.ViewDObjectFacet#sendDataToPresences
 * @see dom.session.ViewDObjectFacet#sendDataToPresenceAtView
 * @see dom.session.DObject#sendDataToRemote
 * @see dom.session.DObject#sendClosureToRemote
 * @see dom.session.DObject#receiveDataFromRemote
 * @see dom.session.DObject#receiveClosureFromRemote
 */
public class DObjectPacket implements Serializable {

    private DObjectID fromID;
    private Data myData;
    
    /**
     * Constructor.  The two data elements that this class provides a wrapper
     * for are passed in the constructor.
     *
     * @param id the DObjectID of the sender DObject
     * @param data the arbitrary Data sent by the requesting DObject
     * @see dom.session.ViewDObjectFacet#sendDataToPresences
     * @see dom.session.ViewDObjectFacet#sendDataToPresenceAtView
     */
    public DObjectPacket(DObjectID id, Data data)
    {
        fromID = id;
        myData = data;
    }
    
    /**
     * Get the DObjectID that created this DObjectPacket.
     *
     * @return DObjectID that created this DObjectPacket
     */
    public DObjectID getFromID()
    {
        return fromID;
    }
    
    /**
     * Get the data that this DObjectPacket contains.  May return null if no
     * data was sent.
     *
     * @return Data from the DObject that created this packet
     */
    public Data getData() 
    {
        return myData;
    }
}


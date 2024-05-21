/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/CapabilityReceipt.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;

/**
 * Wrapper class to hold data for a DObject<->DObject presence capability request.  The class protects access
 * to the contained data by a) not letting the grantor change any of
 * the fields except the myFacet field...which it can set only *once* through
 * setFacet(aFacet) call.  This is the only thing the grantor can do
 * to this object other than get access to the info the receipt wraps.
 *
 * The class is declared final so no subclasses can be created.
 * The constructor is declared protected so no untrusted code can make
 * spurious instances of this class (only made by the View).
 *
 * @see View#requestDObjectCapability
 * @author Scott Lewis
 */
final class CapabilityReceipt {

    private View myView;
    private DObjectID myRequestor;
    private DObjectID myGrantor;
    private String myRequestName;
    private Object myData;

    private DObjectFacet myFacet;

    /**
     * Constructor declared protected so that untrusted code cannot
     * make instances of these objects.  Instances *only* made by
     * the View object.
     *
     * @param aView the View that is making us
     * @param requestor the DObjectID of the DObject making the request
     * @param grantor the DObjectID of the DObject being sent this receipt
     * @param requestName the name of the request
     * @param data any Object data sent along with the request by the requestor
     * @exception NullPointerException thrown if any of the first 4 parameters
     * are null (they should not be).
     */
    protected CapabilityReceipt(View aView,
                                DObjectID requestor,
                                DObjectID grantor,
                                String requestName,
                                Object data)
    {
        if (aView == null ||
            requestor == null ||
            grantor == null ||
            requestName == null) throw new NullPointerException();
        myView = aView;
        myRequestor = requestor;
        myGrantor = grantor;
        myRequestName = requestName;
        myData = data;
    }

    /**
     * Get the DObjectID of the DObject that is making the request for a capability
     * (i.e. the request initiator).
     *
     * @return DObjectID of the request initiator
     */
    public DObjectID getRequestorID()
    {
        return myRequestor;
    }

    /**
     * Get the DObjectID of the request grantor
     *
     * @return DObjectID of the capability request grantor (the target of the
     * the capability request)
     */
    public DObjectID getGrantorID()
    {
        return myGrantor;
    }

    /**
     * Get the String name of the request.  This returns the name of the
     * request passed in by the requestor, in the form of a String
     *
     * @return String that represents request name
     */
    public String getRequestName()
    {
        return myRequestName;
    }

    /**
     * Get the data associated with the request.  This is data that is passed
     * in by the requestor as part of the request.  The grantor can look at
     * this data and do with it as it wishes (i.e. depending upon the request,
     * this data might be some 'magic bits' that the grantor uses to decide
     * whether or not to grant the request
     *
     * @return Object that is the data sent by the requestor as part of the request
     */
    public Object getData()
    {
        return myData;
    }
    
    /**
     * This method allows the grantor to give the requestor a facet.  It
     * takes any subclass of DObjectFacet.
     *
     * @param newFacet the DObjectFacet that the grantor is providing
     * access to
     */
    public void setFacet(DObjectFacet aFacet)
    {
        // Only allow this to be used once
        if (myFacet == null) myFacet = aFacet;
    }
    
    /**
     * Get the DObjectFacet that the grantor has stamped this receipt with.
     * This is only used by the View.giveDObjectCapability method, hence its
     * status as protected.  This means that untrusted DObject code can never
     * call this method, and must depend upon the View to get the facet out
     * and provide it to the DObject (via the DObject.giveCapability message).
     *
     * @return DObjectFacet that has been given to this receipt (via the setFacet
     * method above) by the capability request grantor
     */
    protected DObjectFacet getFacet()
    {
        return myFacet;
    }
    
    /**
     * Represent this capability receipt as a String.  Mostly used for debugging.
     *
     * @return String that represents this capability receipt
     */
    public String toString()
    {
        return "CapabilityReceipt;From:"+getRequestorID()+
                ";To:"+getGrantorID()+
                ";Request:"+getRequestName()+
                ";Data:"+getData()+
                ";Facet:"+getFacet();
    }
}


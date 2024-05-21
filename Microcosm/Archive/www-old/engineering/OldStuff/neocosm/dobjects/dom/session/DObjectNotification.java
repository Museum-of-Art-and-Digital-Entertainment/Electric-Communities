/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DObjectNotification.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.DObjectID;

/**
 * Representation of the View->DObject notification messages for allowing the
 * ViewListManager to notify DObjects about <b>other</b> DObjects arrival and
 * departure.
 * 
 * @see ViewListManager
 * @author Scott Lewis
 */
final class DObjectNotification
{
    DObjectID myID;
    DObjectViewFacet myDObjectFacet;
    
    protected DObjectNotification(DObject object) throws NullFacetException
    {
        myID = object.getID();
        myDObjectFacet = object.getAccessFacet();
    }
    
    boolean equalsID(DObjectID id)
    {
        return myID.equals(id);
    }
    
    void otherActivated(DObjectID id) 
    {
        try {
            if (!equalsID(id)) myDObjectFacet.otherActivated(id);
        } catch (Exception e) {
            // Just ignore...if this message can't be delivered then
            // it's tough luck for the receiver
        }
    }
    
    void otherDeactivated(DObjectID id)
    {
        try {
            if (!equalsID(id)) myDObjectFacet.otherDeactivated(id);
        } catch (Exception e) {
            // Just ignore...if this message can't be delivered then
            // it's tough luck for the receiver
        }
    }    
}
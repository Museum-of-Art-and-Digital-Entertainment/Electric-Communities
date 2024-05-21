/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DObjectNotFoundException.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.util.DOMException;

import dom.id.*;

/**
 * Exception thrown by ViewListManager when DObject not found.
 *
 * @see View
 * @see ViewListManager
 *
 * @author Scott Lewis
 */
public class DObjectNotFoundException extends DOMException
{
    DObjectID myID=null;
    
    public DObjectNotFoundException(DObjectID id)
    {
        myID = id;
    }
    
    public DObjectID getID()
    {
        return myID;
    }
}
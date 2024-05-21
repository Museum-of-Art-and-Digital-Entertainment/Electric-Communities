/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/CreateClientInfo.java $
    $Revision: 1 $
    $Date: 1/26/98 5:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;


// TODO
// This will contain information relevant to allowing a DObject to
// create a client of itself on a remote machine.  Minimally, this will
// have to provide a SessionViewID object to refer to the remote View
/**
 * A wrapper class for encapsulating information about remote DObject
 * presence construction.  An instance of this class is provided by the
 * local ViewListManager to the local presence of a DObject when a new
 * View is added to the multipoint group.  The DObject typically will
 * respond to this information by requesting the construction of a remote
 * client presence.
 *
 * @see DObject#createClient
 * @see ViewListManager#notifyClientCreation
 *
 * @author Scott Lewis
 */
public class CreateClientInfo
{
    private SessionViewID myTarget;

    public CreateClientInfo(SessionViewID targetID)
    {
        myTarget = targetID;
    }

    public SessionViewID getTargetViewID()
    {
        return myTarget;
    }
    
}

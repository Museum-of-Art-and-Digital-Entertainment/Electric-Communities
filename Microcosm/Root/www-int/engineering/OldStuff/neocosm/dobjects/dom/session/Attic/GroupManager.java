/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/View.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;

/**
 * This is the interface that must be implemented by the local View of
 * an Object Group.  The group manager must provide the facilities to
 * allow the joining to another session view (to form a multipoint 
 * session), leave the group, and get some information about the contents
 * of the group itself.  This interface defines those interfaces
 *
 * @see View
 * @author Scott Lewis
 */
public interface GroupManager {
    /**
     * Primary access to joining an existing multipoint group.  This will
     * block until a) the connection has been formed to the group (still not
     * *admitted* but well on the way...modulo authentication); b) an IO
     * exception (i.e. connection could not be created) is thrown.
     * If access to the group is granted, a "joinOK" message from the 
     * group manager will be received...and that is the final word that
     * we (and potentially all our objects) will be allowed to join the group.
     * If this is called while this view is already a member of a group
     * (before leave is called) then it will throw an exception.
     *
     * @param requester the DObject that wishes to initiate a join
     * @param viewToJoin the SessionViewID that represents the group
     * we wish to join
     * @exception Exception thrown if there is some problem joining group
     * (group unreachable, security disallows join request, already a
     * member of a group, etc.)
     */
    public abstract void joinGroup(DObject requester, SessionViewID viewToJoin)
        throws Exception;
    
    /**
     * Primary method to leave a group.  Will throw an exception if not
     * already in a group, or group cannot be left (should never happen).
     *
     * @param requester the DObject that wishes to initiate the group departure
     * @exception Exception thrown if some problem with leaving the group
     */
    public abstract void leaveGroup(DObject requester)
        throws Exception;
        
}
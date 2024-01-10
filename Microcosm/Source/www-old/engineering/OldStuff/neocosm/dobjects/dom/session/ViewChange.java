/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ViewChange.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.net.*;
import dom.util.*;

import java.io.*;

/**
 * A wrapper class for the data sent in a view change
 * (a group membership message).  All this does is encapsulate all of the
 * information needed for the group manager (server in star topology) to
 * send group membership change information to all group members.
 * <p>
 * Package protected constructor so that only the View class can create
 * instances (in the View.getRequestorAdmittedViewChange method).
 *
 * @see View#getRequestorAdmittedViewChange
 * @author Scott Lewis
 */
class ViewChange implements Serializable {

    boolean myIsSuccessful;
    boolean myIsJoining;
    SessionViewID myChangeID;
    VectorEx myIds;
    int myChangeRank;
    SessionViewData myData;
    
    /**
     * Create an instance of this wrapper class.  See the View.getRequestorAdmittedViewChange
     * method.
     *
     * @param changeID the SessionViewID of the view in question
     * @param joining a boolean indicating if the view in question is joining
     * (true) or leaving (false)
     * @param successful a boolean indicating whether the view change was
     * successful (true) or not successful (false)
     * @param ids the VectorEx (basically a Vector) that holds all of the
     * current SessionViewIDs for this group.  This is only used so that new
     * group members can receive the list of current group members upon successful
     * addition to a group.  Otherwise it is null.
     * @param rank the newly added view's *rank* in the group.  This is for
     * future use, as it allows the definition of some algorithm for group
     * manager change over time (currently we have no need to change the group
     * manager at all dynamically, so this isn't needed).
     * @param data the SessionViewData associated with this view change information
     * Currently this is never used (null), but is present so that some arbitrary
     * data could be associated with the ViewChange
     *
     * @see View#getRequestorAdmittedViewChange
     */
    protected ViewChange(SessionViewID changeID,
                         boolean joining, 
                         boolean successful, 
                         VectorEx ids, 
                         int rank, 
                         SessionViewData data)
    {
        myChangeID = changeID;
        myIsJoining = joining;
        myIsSuccessful = successful;
        myIds = ids;
        myChangeRank = rank;
        myData = data;
    }

    /**
     * Another version of the constructor.  See above.
     *
     * @param changeID the SessionViewID of the view in question
     * @param joining a boolean indicating if the view in question is joining
     * (true) or leaving (false)
     * @param successful a boolean indicating whether the view change was
     * successful (true) or not successful (false)
     * @param ids the VectorEx (basically a Vector) that holds all of the
     * current SessionViewIDs for this group.  This is only used so that new
     * group members can receive the list of current group members upon successful
     * addition to a group.  Otherwise it is null.
     * @param rank the newly added view's *rank* in the group.  This is for
     * future use, as it allows the definition of some algorithm for group
     * manager change over time (currently we have no need to change the group
     * manager at all dynamically, so this isn't needed).
     */
    protected ViewChange(SessionViewID changeID,
                         boolean joining, 
                         boolean successful, 
                         VectorEx ids, 
                         int rank)
    {
        this(changeID, joining, successful, ids, rank, null);
    }
    
    /**
     * Get the SessionViewID in question
     *
     * @return SessionViewID of the view that is being added/being removed
     * from the group
     */
    SessionViewID getChangeID()
    {
        return myChangeID;
    }

    /**
     * Determine whether this is a join (true) or leave (false) change message
     *
     * @return true if this is a join change message, false if it is a leave
     * change message
     */
    boolean isJoining()
    {
        return myIsJoining;
    }
    
    /**
     * Indicate whether this the join/leave was successful (true) or not (false).
     *
     * @return true if view change was successful, false otherwise
     */
    boolean isSuccessful()
    {
        return myIsSuccessful;
    }
    /**
     * Get the rank assigned to the newly added view.  This field is
     * irrelevant if this is not a successful join view change, or if the
     * view change is directed to a 3rd party.
     *
     * @return int that indicates this new View's rank within the group.  
     * Not currently used, but could be used to allow dynamic group manager
     * changes
     */
    int getRank()
    {
        return myChangeRank;
    }
    
    /**
     * Get vector of SessionViewIDs that indicate the current group membership.
     * This is used so that the new group member can be updated by the current
     * group manager as to what the current group membership is.  This is only
     * valid (non-null) when a group manager is sending a successful join ok
     * message back to a putative new group member.
     *
     * @return VectorEx that is a Vector of SessionViewIDs that define the
     * entire current group membership for the newly added group member
     */
    VectorEx getIDs()
    {
        return myIds;
    }
    
    /**
     * Get the SessionViewData assocated with this view change.  Currently
     * not used, but potentially useful in the future for associating some
     * data with a view change that I haven't thought of as useful yet.
     *
     * @return SessionViewData associated with this view change.  Only the
     * current group manager defines what this means.
     */
    SessionViewData getMsgData()
    {
        return myData;
    }
    
    /**
     * Represent this view change object as a String (mostly for debugging)
     */
    public String toString()
    {
        return "ViewChange(id:"+myChangeID+";join:"+myIsJoining+";suc:"+
            myIsSuccessful+";rank:"+myChangeRank+";ids:"+myIds+";data:"+myData+")";
    }
}
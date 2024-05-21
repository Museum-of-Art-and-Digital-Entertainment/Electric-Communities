/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/GroupAdmissionCheck.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.*;
import dom.net.SessionViewData;

/**
 * A check of a group admission request.  Instances of this class are created
 * by the View to pass around all of the information compactly that is needed
 * to accomplish a group admission check.
 * 
 * @see ServerView#checkForAdmission
 * @see GroupConnectionHandler#handleLeaveMsg
 *
 * @author Scott Lewis
 */
class GroupAdmissionCheck {

    // The id of the requestor
    private SessionViewID myForID;
    // Whether or not the requestor has been admitted
    private boolean myIsAdmitted;
    // Any data associated with decision...can be null, but can also contain 
    // arbitrary information relevant to the decision (mostly relevant for
    // failure cases)
    private SessionViewData myResultData;

    /**
     * Constructor for this result object.  
     *
     * @param id the SessionViewID of the view this result is relevant for
     * @param admitted whether this id is admitted (true) or not (false)
     * @param data any data associated with result, that should be propogated
     * back to the requester
     */
    protected GroupAdmissionCheck(SessionViewID id, boolean admitted, SessionViewData data)
    {
        myForID = id;
        myIsAdmitted = admitted;
        myResultData = data;
    }
    
    /**
     * Return whether our check actually resulted in the requester being
     * admitted
     *
     * @return true if admitted according to this check, false otherwise
     */
    protected boolean isAdmitted()
    {
        return myIsAdmitted;
    }
    
    /**
     * Get the SessionViewID this result is relevant for
     *
     * @return SessionViewID this result is relevant for
     */
    protected SessionViewID getViewID()
    {
        return myForID;
    }
    
    /**
     * Get the SessionViewData (can be null) for this result
     *
     * @return SessionViewData that contains relevant result information
     */
    protected SessionViewData getResultData()
    {
        return myResultData;
    }

}
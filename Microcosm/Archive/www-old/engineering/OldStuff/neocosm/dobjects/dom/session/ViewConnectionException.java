/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/ViewConnectionException.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.util.DOMException;

/**
 * Exception thrown when a joinGroup or leaveGroup is attempted and the View
 * is in an inappropriate state.
 *
 * @see View#joinGroup
 * @see View#leaveGroup
 *
 * @author Scott Lewis
 */
public class ViewConnectionException extends DOMException {
    
    public ViewConnectionException() {
        super();
    }
    
    public ViewConnectionException(String message)
    {
        super(message);
    }
}

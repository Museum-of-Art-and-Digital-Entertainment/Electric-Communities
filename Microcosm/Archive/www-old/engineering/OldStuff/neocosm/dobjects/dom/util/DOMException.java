/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/util/DOMException.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.util;

/**
 * Root exception class for all Distributed Object Model (DOM) exceptions.
 *
 * @author Scott Lewis
 */
public class DOMException extends Exception {
    
    public DOMException()
    {
        super();
    }
    
    public DOMException(String message)
    {
        super(message);
    }
    
}

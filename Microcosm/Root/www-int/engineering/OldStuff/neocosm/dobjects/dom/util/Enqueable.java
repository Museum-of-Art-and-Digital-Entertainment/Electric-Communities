/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/util/Queue.java $
    $Revision: 1 $
    $Date: 12/27/97 4:09p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.util;

public interface Enqueable {

    public boolean enqueue(Object obj);
    public void close();
}
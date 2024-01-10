/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/net/SessionViewData.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.net;

import dom.id.*;
import dom.serial.*;

import java.io.Serializable;

/**
 * Class for the data in a SessionViewPacket.  This is a simple
 * dictionary subclass of dom.serial.Data.  A more specialized version
 * of the dom.serial.Data class.
 *
 * @see SessionViewPacket
 * @see dom.serial.Data
 *
 * @author Scott Lewis
 */
public class SessionViewData extends Data implements Serializable {

    /**
     * Create an instance of this class with the given Serializable object
     * for its actual data
     *
     * @param data the Serializable that is the data this class wraps
     */
    public SessionViewData(Serializable data)
    {
        super();
        put(0, data);
    }
    
    /**
     * Get the Serializable instance that this object wraps
     *
     * @return Serializable that this dictionary contains
     */
    public Serializable getData()
    {
        return (Serializable) get(0);
    }
    
    /**
     * Return String representation of this object.  Mostly used for
     * debugging
     *
     * @return String that represents this object
     */
    public String toString()
    {
        return "SessionViewData("+minimalString()+")";
    }
    
    
}

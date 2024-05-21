/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/util/VectorEx.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.util;

import java.util.Enumeration;
import java.util.Vector;

import java.io.Serializable;

/**
 * An extended Vector class. Adds a few utility interfaces to Vector by 
 * extending it.
 *
 * @see java.util.Vector
 * @version $Revision: 3 $ $Date: 1/27/98 10:32a $
 * @author  Scott Lewis
 */
public class VectorEx extends Vector implements Serializable
{
    /**
     * Normal constructor.  Just calls superclass constructor.
     *
     */
    public VectorEx()
    {
        super();
    }
    
    /**
     * Add all objects from an enumeration to the collection.
     *
     * @param e an enumeration of objects
     * @return the collection (for caller convenience)
     */
    public VectorEx addAll(Enumeration e)
    {
        while (e.hasMoreElements()) {
            addElement(e.nextElement());
        }
        return this;
    }
    
    public VectorEx addAll(Object arr[])
    {
        for(int i=0; i < arr.length; i++) addElement(arr[i]);
        return this;
    }
    

    /**
     * Create a VectorEx of Objects that initially holds all the
     * objects in an enumeration.
     *
     * @param e an enumeration of Objects
     */
    public VectorEx(Enumeration e)
    {
        addAll(e);
    }
    
    public VectorEx(Object arr[])
    {
        addAll(arr);
    }
}

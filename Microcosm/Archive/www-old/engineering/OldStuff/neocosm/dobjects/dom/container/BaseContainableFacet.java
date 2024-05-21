/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/container/BaseContainableFacet.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.container;

import dom.session.DObjectFacet;
import dom.session.ComponentDObject;
import dom.session.Closure;

import dom.session.NullFacetException;
import dom.id.DObjectID;

import java.lang.reflect.Method;

/**
 * Facet to BaseContainable component.
 *
 * @see BaseContainable
 * @see BaseContainerFacet
 * @see ComponentDObject
 *
 * @author Scott Lewis
 */
public class BaseContainableFacet extends DObjectFacet {

    public static String ADDED_METHOD_NAME = "asynchAdded";
    public static String REMOVED_METHOD_NAME = "asynchRemoved";
    public static String OTHER_ADDED_METHOD_NAME = "asynchOtherAdded";
    public static String OTHER_REMOVED_METHOD_NAME = "asynchOtherRemoved";
    
    BaseContainable myContainable;
    
    public BaseContainableFacet(ComponentDObject owner, BaseContainable containable) throws NullFacetException
    {
        super(owner, (Method []) null);
        if (containable == null) throw new NullFacetException();
        myContainable = containable;
    }

    public void added(Exception error, DObjectFacet resultFacet)
        throws NullFacetException
    {
        exceptionIfRevoked();
        // Give appropriate Closure to the ComponentDObject owner for actual processing
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainable.getIdentifier(),
            makeNewClosure(ADDED_METHOD_NAME, Closure.getObjectArrayFromParams(getReceiverID(), error, resultFacet)));
    }

    public void removed(Exception error, DObjectFacet resultFacet)
        throws NullFacetException
    {
        exceptionIfRevoked();
        // Give appropriate Closure to the ComponentDObject that is our owner
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainable.getIdentifier(),
            makeNewClosure(REMOVED_METHOD_NAME, Closure.getObjectArrayFromParams(getReceiverID(), error, resultFacet)));
    }

    public void otherAdded(DObjectID otherID)
        throws NullFacetException
    {
        exceptionIfRevoked();
        // Give appropriate Closure to the ComponentDObject owner for actual processing
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainable.getIdentifier(),
            makeNewClosure(OTHER_ADDED_METHOD_NAME, Closure.getObjectArrayFromParams(getReceiverID(), otherID)));
    }

    public void otherRemoved(DObjectID otherID)
        throws NullFacetException
    {
        exceptionIfRevoked();
        // Give appropriate Closure to the ComponentDObject that is our owner
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainable.getIdentifier(),
            makeNewClosure(OTHER_REMOVED_METHOD_NAME, Closure.getObjectArrayFromParams(getReceiverID(), otherID)));
    }
    
    private Closure makeNewClosure(String methodName, Object [] arr)
    {
        return createNewClosure(myContainable.getClass(), methodName, arr);
    }
    
    protected boolean overrideableRevoke()
    {
        // Set myContainable to null, so it can be garbage collected
        myContainable = null;
        return true;
    }
    
}

/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/container/BaseContainerFacet.java $
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

import java.io.Serializable;

/**
 * Facet for BaseContainer component.
 *
 * @see BaseContainer
 * @see BaseContainableFacet
 * @see ComponentDObject
 *
 * @author Scott Lewis
 */
public class BaseContainerFacet extends DObjectFacet {

    public static String ADD_CONTAINABLE_METHOD_NAME = "asynchAdd";
    public static String REMOVE_CONTAINABLE_METHOD_NAME = "asynchRemove";
    
    // The component that this facet references
    BaseContainer myContainer;
    
    public BaseContainerFacet(ComponentDObject owner, BaseContainer container) throws NullFacetException
    {
        super(owner, (java.lang.reflect.Method []) null);
        if (container == null) throw new NullFacetException();
        myContainer = container;
    }

    public void add(BaseContainableFacet containable, Serializable data)
        throws NullFacetException
    {
        exceptionIfRevoked();
        if (containable == null) throw new NullFacetException();
        // Give appropriate Closure to the ComponentDObject owner for actual processing
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainer.getIdentifier(),
            makeNewClosure(ADD_CONTAINABLE_METHOD_NAME, Closure.getObjectArrayFromParams(containable, data)));
    }
    
    public void remove(BaseContainableFacet containable)
        throws NullFacetException
    {
        exceptionIfRevoked();
        if (containable == null) throw new NullFacetException();
        ((ComponentDObject) getOwner()).asynchDeliverToComponent(getReceiverID(), myContainer.getIdentifier(),
            makeNewClosure(REMOVE_CONTAINABLE_METHOD_NAME, Closure.getObjectArrayFromParam(containable)));
    }

    private Closure makeNewClosure(String methodName, Object [] arr)
    {
        return createNewClosure(myContainer.getClass(), methodName, arr);
    }
    
    protected boolean overrideableRevoke()
    {
        // Set myContainable to null, so it can be garbage collected
        myContainer = null;
        return true;
    }

}


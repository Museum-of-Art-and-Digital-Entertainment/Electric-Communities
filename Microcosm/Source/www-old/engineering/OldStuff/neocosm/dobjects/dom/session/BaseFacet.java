/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/BaseFacet.java $
    $Revision: 1 $
    $Date: 1/8/98 4:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.DObjectID;
import java.util.Vector;

import java.lang.reflect.*;

/**
 * Root facet class to provide basic revokation semantics.  It provides an abstract superclass for
 * all kinds of facets...i.e. DObjectFacets (for facets between mutually untrusting
 * DObjects) as well as the DObjectView facet (the facet the DObject gives the
 * View)
 *
 * @author Scott Lewis
 */
public abstract class BaseFacet {
    // This is the well-protected owner of this facet.  This is guaranteed
    // to be protected by a) java's type safety (no untrusted piece of code
    // can get to the myOwner ref; b) this class will only allow
    // access to this ref to other subclasses (protected)
    private DObject myOwner = null;

    /**
     * Given a Class object, a string identifying the method, and an array of
     * Objects representing parameters, this will return a Closure Object to use
     * for subsequently passing onto a receiver.
     *
     * @param aClass the Class that will be expected of the receiver.  If this 
     * first parameter is null, it means to lookup directly upon the class of the
     * receiving object for the given method
     * @param methodName the String identifying which method to invoke on the given Class
     * @param params the Object [] containing the params to pass on as part of Closure
     * returned
     * @return Closure that will represent the given
     */
    public static Closure createNewClosure(Class aClass, String methodName,
                                            Object [] params)
    {
        return new Closure(aClass, methodName, params);
    }


    /**
     * Constructor for this class.  All subclasses must provide
     * a non-null reference to the DObject that is to be the owner
     * of this facet.
     *
     * @param owner the owner of this newly-constructed facet
     * @exception NullFacetException thrown if owner is null
     */
    public BaseFacet(DObject owner) throws NullFacetException
    {
        if (owner == null) throw new NullFacetException();
        this.myOwner = owner;
    }

    /**
     * Get the id of the DObject that owns this facet.  This method
     * cannot be overridden.
     *
     */
    public final DObjectID getID()
    {
        // Null facet exception never thrown.  This method will always be
        // available for the receiver of this facet
        return myOwner.getID();
    }

    /**
     * Revoke this facet.  This method cannot be overridden.
     *
     * @param theOwner the DObject that owns this facet
     * @return true if successfully revoked, false if DObject key
     * provided does not match one for this facet.
     * @exception NullFacetException thrown if already revoked
     */
    public final boolean revoke(DObject theOwner) throws NullFacetException
    {
        exceptionIfRevoked();
        if (this.myOwner != theOwner) return false;
        myOwner = null;
        // Call subclass overrideable code
        return overrideableRevoke();
    }

    /**
     * Subclass overrideable code that gets called when a facet is revoked.
     *
     */
    protected boolean overrideableRevoke()
    {
        // Do nothing by default
        // Subclasses may override to put in subclass-specific clean up code.
        return true;
    }
    
    /**
     * Check to see if this facet is revoked (without throwing exception)
     *
     * @return true if this facet has been revoked, false otherwise.
     */
    public final boolean isRevoked()
    {
        if (myOwner != null) return false;
        return true;
    }

    /**
     * Utility method for subclasses to throw exception if this facet has
     * previously been revoked.
     *
     * @exception NullFacetException thrown if this facet has previously been revoked.
     */
    protected final void exceptionIfRevoked() throws NullFacetException
    {
        if (isRevoked()) throw new NullFacetException();
    }

    /**
     * Utility method for subclasses to get access to the owner of this facet.
     *
     * @return DObject reference to our owner.  Should not be exposed via public method.
     */
    protected final DObject getOwner()
    {
        return myOwner;
    }

}
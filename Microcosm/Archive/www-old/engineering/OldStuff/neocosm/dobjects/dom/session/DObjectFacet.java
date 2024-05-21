/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DObjectFacet.java $
    $Revision: 1 $
    $Date: 1/22/98 10:00a $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.id.DObjectID;
import dom.session.DObject;

import dom.util.VectorEx;

import java.lang.reflect.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Root class facet for all DObject facets.  This class defines
 * behavior that will be common to *all* interactions between DObjects within
 * the context defined by a View.  This class must guarantee that a) the View
 * that provides the context for this interaction between DObjects has the
 * right and ability to revoke the usage of this facet at any time; b) that
 * revoking this facet (and any subclass) null's out any refences to the 
 * underlying DObject that delivers this facet.  This functionality is
 * provided below by the (final) methods:  setView, setReceiverID, and 
 * revoke(View).  setView is called once by View.giveDObjectCapability to
 * immutably set the View as the trusted third-party 'co-owner' of the facet 
 * (along with the facet grantor and facet receiver).  This allows the View to
 * later call 'revoke(this)' on this facet, and know that it will do the
 * correct thing WRT nulling out the underlying reference.
 * <p>
 * All facets exchanged between DObjects *must* inherit from this class.
 * <p>
 * TODO:  Need to think about adding some mechanism for doing runtime checking
 * of messages and params defined by subclasses
 *
 * @author Scott Lewis
 */
public class DObjectFacet extends BaseFacet {

    // The View can stamp this facet so it has the right to revoke
    // This variable is set by the View in View.giveDObjectCapability
    private View myView;
    
    // The receiver of this facet object.  This id is set by the View
    // within View.giveDObjectCapability so the the original *grantor*
    // will know who it originally granted this facet to.  If this facet
    // is passed along to another DObject, then the original grantor can
    // know which DObject it provided this facet to originally
    protected DObjectID myUserID;
    // The Vector of Method objects that this facet exposes
    protected VectorEx myMethods;
    
    /**
     * Constructor.  This constructor allows the grantor of this facet to
     * provide an array of Method objects that will be the interfaces this
     * facet exposes.  The receiver of this facet may then 
     * query this facet on what interfaces it exposes (via the hasMatchinMethod
     * interfaces below, and then send messages (asynchronously) to the 
     * original grantor via these interfaces.
     *
     * @param owner the owner of this newly-constructed facet
     * @param methodArray the array of Method objects that this facet will
     * provide access to.
     * @exception NullFacetException thrown if owner is null
     */
    public DObjectFacet(DObject owner, Method methodArray[])
        throws NullFacetException
    {
        super(owner);
        myView = null;
        myUserID = null;
        myMethods = new VectorEx();
        if (methodArray != null) myMethods.addAll(methodArray);
        // Include public methods that this class and subclasses expose
        addMethods(this);
    }

    /**
     * Constructor.  This constructor allows the grantor of this facet to
     * provide a Vector of objects that will be the interfaces this
     * facet exposes.  The contents of this Vector must be Method objects,
     * or they will be ignored (eventually other representations of Methods
     * may be added).  
     * <p>
     * The receiver of this facet may then 
     * query this facet on what interfaces it exposes (via the hasMatchinMethod
     * interfaces below, and then send messages (asynchronously) to the 
     * original grantor via these interfaces.
     *
     * @param owner the owner of this newly-constructed facet
     * @param methodVector the Vector of Method objects that this facet will
     * provide access to.
     * @exception NullFacetException thrown if owner is null
     */
    public DObjectFacet(DObject owner, Vector methodVector)
        throws NullFacetException
    {
        super(owner);
        myView = null;
        myUserID = null;
        myMethods = new VectorEx();
        if (methodVector != null) myMethods.addAll(methodVector.elements());
        // Include public methods that this class and subclasses expose
        addMethods(this);
    }

    
    
    /**
     * Constructor for this class.  All subclasses must provide
     * a non-null reference to the DObject that is to be the owner
     * of this facet.  This will automatically get (using CRAPI)
     * all public methods exposed by the class of the 'owner' object,
     * and include them as methods exposed by this facet. 
     *
     * @param owner the owner of this newly-constructed facet
     * @exception NullFacetException thrown if owner is null
     */
    public DObjectFacet(DObject owner) throws NullFacetException
    {
        this(owner, (Vector) null);
        myMethods.addAll(filterClassMethods(owner).elements());
        // Include public methods that this class and subclasses expose
        addMethods(this);
    }
    
    /**
     * Add the methods defined as public on the given object to the set exposed
     * by this facet.
     *
     * @param obj the Object that exposes some public methods.  Public methods
     * that are exposed by the class Object are filtered out.
     */
    protected void addMethods(Object obj)
    {
        myMethods.addAll(filterClassMethods(obj).elements());
    }
    
    /**
     * Private method used in DObjectFacet constructor to filter out methods.
     * This method returns a Vector of Method objects, removing all public methods
     * that are exposed by the Object class, so that only public methods exposed
     * by the DObject class or subclasses is included in Vector returned.
     * 
     * @param owner the Object owner to filter
     * @return Vector representing filtered Methods
     */
    private Vector filterClassMethods(Object owner)
    {
        Vector stuff = new Vector();
        Method methodArr[] = owner.getClass().getMethods();
        if (methodArr == null) return stuff;
        for(int i=0; i < methodArr.length; i++) {
            if (includeClassMethod(methodArr[i])) stuff.addElement(methodArr[i]);
        }
        return stuff;
    }
    
    /**
     * Determine whether given Method should be included.  If the given Method
     * is found on class 'Object' then it returns false, if not, returns true.
     *
     * @param aMethod the Method to look for
     * @return true if Method *not* found on Object class.  False if found
     */
    private boolean includeClassMethod(Method aMethod)
    {
        try {
            // Filter out all methods exposed by class Object
            return (!aMethod.equals(Object.class.getMethod(aMethod.getName(), aMethod.getParameterTypes())));
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get the id of the 'client' of this facet.
     *
     * @return DObjectID of the DObject that is the receiver of this facet
     */
    public final DObjectID getReceiverID()
    {
        return myUserID;
    }
    
    /**
     * Determine whether this facet exposes the given Method.  This comparison
     * is done *not* with an 'equals' test between Method objects, but rather
     * via comparing the method names and arity of the two method objects.  This
     * is done because the Method objects actually *could* be from classes
     * loaded via separate classloaders, which would presumably fail an equals
     * test between Method objects, but for which we want to *succeed*.
     *
     * @param aMethod the method to look for
     * @return true if a match is found, false otherwise
     */
    public final boolean hasMatchingMethod(String methodName, Class argTypes[])
    {
       if (methodName == null) return false;
       // Make sure that argTypes is non-null so searchForTarget doesn't barf
       if (argTypes == null) argTypes = new Class[0];
       Method methodArr[] = new Method[myMethods.size()];
       myMethods.copyInto(methodArr);
       if (Closure.searchForTarget(methodArr, methodName, argTypes) != null) return true;
       return false;
    }
     
    /**
     * Same as above, but allows passing of Method object.
     *
     * @param aMethod the method to look for
     * @return true if a match is found, false otherwise
     */
    protected final boolean hasMatchingMethod(Method aMethod)
    {
       return hasMatchingMethod(aMethod.getName(), aMethod.getParameterTypes());
    }
     
    /**
     * Same as above, but allows use of Closure object.
     *
     * @param aMethod the method to look for
     * @return true if a match is found, false otherwise
     */
    protected final boolean hasMatchingMethod(Closure aClosure)
    {
       return hasMatchingMethod(aClosure.getMethodName(), Closure.getArgTypesFromArgs(aClosure.getArgs()));
    }
        
    /**
     * Deliver the provided Closure asynchronously to the owner of this facet.
     * This is the message delivery point for all inter-DObject messaging via
     * the DObjectFacet class.
     *
     * @param aClosure the Closure to deliver to the owner of this facet
     * @exception Exception thrown if some problem with delivery (possibly
     * a NullFacetException or NoSuchMethodException if this facet does not
     * expose the given capability.
     */
     protected final void deliverClosure(Closure aClosure)
        throws Exception
     {
        // If this facet does not expose the desired interface, throw so that
        // sender knows about it.
        if (!hasMatchingMethod(aClosure)) throw new NoSuchMethodException();
        // Actually do it
        realAsynchInvokeMethod(getReceiverID(), aClosure);
     }
        
     
    // Protected methods that are a) to be accessed by the view only (e.g.
    // for stamping this facet
    /**
     * Allow the View to set itself as the co-owner of this facet.  This
     * is allowed only once, and the View must do this if it wishes to
     * be able to revoke this DObjectFacet (which it does, if, e.g., their
     * is a partition which requires this facet to make it's DObject
     * inaccessible.  This is only called on initial facet exchange, and 
     * is called by the View in View.giveDObjectCapability.
     *
     * Declared final so that no subclass facets of this facet can change
     * its behavior.  Critical for security.
     *
     * @param theView a View that will set itself as co-owner of this
     * facet
     * @see View#giveDObjectCapability
     */
    protected final void setView(View theView)
    {
        // Only allow this stamping to occur once
        if (myView == null) myView = theView;
    }
    
    /**
     * Set the id of the receiver of this facet.  This is called by View.
     * giveDObjectCapability method to unchangeably stamp this facet with
     * the id of the DObject that is the 'client' or 'user' of this facet.
     *
     * Declared final so that the stamp is unchangeable by subclasses.
     * Can be called successfully only once, and is called by the View on
     * initial delivery.
     *
     * @param id the DObjectID that is the 'client' of this facet
     */
    protected final void setReceiverID(DObjectID id)
    {
        if (myUserID == null) myUserID = id;
    }
    
    /**
     * Allow the co-owner View of this facet to revoke it.  This will typically
     * be done by the View when a DObject becomes inaccessible (leaves or
     * network partition).  Requires View to identify itself by passing in
     * reference to itself.  Revokation is only done if View key matches.  Of
     * course, receivers of this facet can also do this, but they only hurt
     * themselves.  ;-)
     *
     * Declared final so that subclasses may not redefine behavior, and View
     * can count on doing this correctly.
     *
     * @param aView the View that is doing the revokation.  Must match View
     * specified by setView to have revokation occur.
     * @exception NullFacetException thrown if this DObjectFacet has already
     * been revoked.
     * @see #setView
     */
    protected final void revoke(View aView)
        throws NullFacetException
    {
        exceptionIfRevoked();
        if (myView == aView) revoke(getOwner());
    }
    
    /**
     * Utility for subclasses to use to give Closure to our owner.  Owner can decide
     * how to handle asynch invocation from DObjectID.
     *
     * @param closure the Closure to pass to our owner.
     */
    protected void realAsynchInvokeMethod(DObjectID fromID, Closure closure)
    {
        getOwner().receiveAsynchDObjectMsg(fromID, closure);
    }
    
    /**
     * Method to print out the names and arities of each of the methods this
     * facet surrounds, separated by commas
     *
     * @return String representation of methods
     */
    private String methodsAsString()
    {
        String aString = "";
        for(Enumeration e=myMethods.elements(); e.hasMoreElements(); ) {
            Method aMethod = (Method) e.nextElement();
            aString = aString + aMethod.getName()+"/"+aMethod.getParameterTypes().length;
            if (e.hasMoreElements()) aString = aString + ",";
        }
        return aString;
    }
    
    /**
     * Debugging support
     */
    public String toString()
    {
        return this.getClass().getName()+"["+methodsAsString()+"]";
    }

}

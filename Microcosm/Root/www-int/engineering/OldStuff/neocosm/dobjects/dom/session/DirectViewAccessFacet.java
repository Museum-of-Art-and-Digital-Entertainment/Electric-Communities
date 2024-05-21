/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/DirectViewAccessFacet.java $
    $Revision: 1 $
    $Date: 1/26/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

/**
 * A facet class that allows direct access to the underlying View instance.
 * Use this class with care, as it exposes all the publicly available methods
 * on a given View class (provided in constructor).  It should not be made
 * available to just anyone.
 *
 * @author Scott Lewis
 */
public class DirectViewAccessFacet extends BaseFacet
{
    View myView;

    /**
     * Normal constructor.  Takes the owner View for this facet.  Note
     * that this facet allows the receiving code to get at the underlying
     * View reference directly, so does not protect it at all.  Use with
     * care.
     *
     * @param aView the View that this facet provides access to
     */
    public DirectViewAccessFacet(DObject receiver, View aView)
        throws NullFacetException
    {
        super(receiver);
        myView = aView;
    }

    /**
     * Method that client code can use to get directly at that underlying
     * View.  Note that any receiver of this facet type will be able to
     * get directly at the underlying View class instance.
     *
     * @return View that this facet wraps
     */
    public View getView()
    {
        return myView;
    }

}
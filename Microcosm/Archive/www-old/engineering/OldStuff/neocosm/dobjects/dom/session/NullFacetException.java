/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/NullFacetException.java $
    $Revision: 1 $
    $Date: 1/8/98 4:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import dom.util.DOMException;

/**
 * Exception thrown when a facet class is accessed, and it has
 * previously been revoked.
 *
 * @see dom.session.BaseFacet
 * @see dom.session.DObjectFacet
 *
 * @author Scott Lewis
 */
public class NullFacetException extends DOMException {}
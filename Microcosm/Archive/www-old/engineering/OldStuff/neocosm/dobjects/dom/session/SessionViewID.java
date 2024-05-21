/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/session/SessionViewID.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.session;

import java.net.*;
import java.io.*;
import dom.id.DObjectID;

/**
 * A Views's (local View of distributed group) unique identification.  Includes 
 * a) a unique number for a particular local 'view' of a session; b) An URL, which 
 * can be used as an external reference to the session view this id represents 
 * (for access to entering/leaving group)
 * <p>
 * NOTE:  This id is opaque, so it could be replaced by, say, a SturdyRef.
 * @see View
 * @author Scott Lewis
 */
public class SessionViewID extends DObjectID implements Serializable {

    private URL myURL;

    /**
     * Static method to create new instance of SessionViewIDs.  This is a
     * factory for making these ids, and the central point for all SessionViewID
     * creation.
     *
     * @param theURL the URL that will be used to point at this view
     * @return SessionViewID that is new id
     * @exception Exception thrown if some problem making id
     */
    public static SessionViewID makeNewID(URL theURL) throws Exception
    {
        if (theURL == null) return new SessionViewID((URL) null);
        else return new SessionViewID(theURL);
    }
    
    /**
     * Another version of above
     *
     * @param theURL the string representing the URL that will be used to point 
     * at this view
     * @return SessionViewID that is new id
     * @exception Exception thrown if some problem making id
     */
    public static SessionViewID makeNewID(String theURL) throws Exception
    {
        if (theURL == null) return new SessionViewID((URL) null);
        else return new SessionViewID(new URL(theURL));
    }

    /**
     * Another version of above.
     *
     * @param base an URL that identifies the session view
     * @param name the name of the session view
     * @return SessionViewID that is new id
     * @exception Exception thrown if some problem making id
     */
    public static SessionViewID makeNewID(URL base, String name) throws Exception
    {
        return new SessionViewID(new URL(base, name));
    }
    
    /**
     * Instance constructor.  Package protected so untrusted code cannot make these.
     *
     * @param an URL that identifies this session view.
     */
    protected SessionViewID(URL theURL)
    {
        super();
        myURL = theURL;
    }

    /**
     * Make a copy of an existing SessionViewID
     *
     * @param id the SessionViewID object that will be copied.
     */
     protected SessionViewID(SessionViewID id)
     {
        super(id);
     }

     /**
      * Make a non-descript SessionViewID.
      *
      */
     protected SessionViewID(boolean makeUnique) {
        super(makeUnique);
        myURL = null;
     }

     /**
      * Get the URL for this SessionViewID.
      *
      * @return URL for this SessionViewID
      */
     public URL getURL()
     {
        return myURL;
     }

     /**
      * Get string representation of this object.  Violates opaqueness,
      * but this will be removed eventually.
      *
      */
     public String toString()
     {
        String urlString = (myURL==null)?"null":myURL.toExternalForm();
        return "SessionViewID("+shortString()+","+urlString+")";
        
     }
}
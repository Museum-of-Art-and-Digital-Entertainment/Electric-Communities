package dom.lilbuddies;

import dom.session.*;
import dom.id.*;

import java.awt.Frame;

/**
 * ChatServer type of view.  Extends ServerView, and exposes a method to allow
 * new instances of MulticastTextChat (or any other DObject type) to be created
 * on this server.
 */
public class ChatServerView extends ServerView
{
    /**
     * Constructor for ChatServerView.  Takes the new id and calls super(id).
     *
     * @param id the SessionViewID to use for this view.
     */
    public ChatServerView(SessionViewID id)
    {
        super(id);
    }

    /**
     * Get the SessionViewID for this view instance.
     *
     * @return SessionViewID that is the id for this instance
     */
    public SessionViewID getMyID()
    {
        return super.getID();
    }

    /**
     * Create an instance of the given classname, with the given DObjectID.
     * This creates a new DObject that is hosted on this View.
     *
     * @param id the DObjectID to create the new instance
     * @param className the String classname to use to construct the instance
     * @exception Exception thrown if one of several possible things go wrong
     */
    protected DObjectID createMyDObject(DObjectID id, String className)
        throws Exception
    {
        return createDObject(null, id, getID(), null, className, null, true);
    }

    // XXX TESTING.  This method is being used to test the java package security
    // mechanisms.  If declared public, this method is callable from any of the
    // dobjects.* code (from the MultipointTextChat instance constructor).  If declared
    // protected, the call from MultipointTextChat will fail with an exception because
    // of the package security structure java enforces.
    /*
    protected void bogusCall()
    {
        System.out.println("ChatServerView.bogusCall successfully called");
    }
    */


}
package dom.lilbuddies;

import dom.session.*;
import dom.id.*;
import dom.dobjects.*;

import java.awt.Frame;

/**
 * ChatClient type of view.  Extends ClientView, and exposes a method
 * getUserInterface to MulticastTextChat DObject's that exist on this View 
 * (client or host presences) so that they have some way to interact with
 * the user interface.
 */
public class ChatClientView extends ClientView

{
    LilBuddiesFrame myFrame;
    String myLocalName;
    
    /**
     * Constructor for ChatClientView.  Takes the new id and calls super(id).
     *
     * @param id the SessionViewID to use for this view.
     * @param aFrame the LilBuddiesFrame user interface
     */
    public ChatClientView(SessionViewID id, LilBuddiesFrame aFrame, String name)
    {
        super(id);
        myFrame = aFrame;
        myLocalName = name;
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

    /**
     * Join the group identified by the given SessionViewID (if possible).
     * If not possible, an exception is thrown.
     *
     * @param id the SessionViewID of the view to join
     */
    protected void joinMyGroup(SessionViewID id) throws Exception
    {
        joinGroup(id);
    }

    /**
     * Access method for DObject code to query and get the GUI for them
     * to show output to, and receive text input from.
     *
     * @param owner the MultipointTextChat instance that will be using the
     * MulticastChatTextIO interface for display, and to receive input
     */
    public MulticastChatTextIO getUserInterface(MultipointTextChat owner)
    {
        if (owner == null) return null;
        return new TextInputOutput(myFrame, myFrame.getTextOutput(), owner);
    }
    
    /**
     * Get the local name to be used.
     *
     * @return String that locally identifies this participant
     */
    public String getLocalName()
    {
        return myLocalName;
    }
    
    protected BaseFacet getViewFacet(DObject requestor, String request, Object data)
    {
        System.out.println("ChatClientView.getViewFacet.  Request "+request+" data "+data);
        try {
            if ("viewFacet".equals(request)) {
                return new DirectViewAccessFacet(requestor, this);
            }
            return null;
        } catch (Exception e) {
          return null;
        }
    }
    
}
/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/lilbuddies/MultipointTextChat.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.dobjects;

import dom.session.*;
import dom.id.*;
import dom.lilbuddies.*;

import java.io.Serializable;
import java.net.URL;
import java.io.IOException;

/**
 * DObject for multi-person chat.  This DObject subclass provides basic text chat support
 * to any client code.  All it does is given an Utterance object, it forwards
 * that object to all other presences of itself, where a given method is
 * called to actually present the utterance using whatever gui is appropriate.
 *
 * @author Scott Lewis
 * @see DObject
 */
public class MultipointTextChat extends DObject {

   public static String receiveMethod = "asynchReceiveUtterance";

   MulticastChatTextIO myTextOutput;
   String myLocalName;

   /**
    * Basic constructor.  This just overrides the DObject constructor, adding
    * on some (optional) GUI interface class that will call into this code
    * to send to itself on remotes, and provide a callback method to call
    * upon reception of an Utterance.
    *
    * @param viewFacet a ViewDObjectFacet object that defines our local runtime context.
    * If this parameter is null, an InstantiationException is thrown.
    * @param newID a DObjectID that is the new id for this DObject.
    * If this parameter is null, an InstantiationException is thrown.
    * @param homeID a SessionViewID that is the home view id for this DObject
    * If this parameter is null, an InstantiationException is thrown.
    * @param codeBase an URL that identifies the (local or remote) codebase
    * for this DObject.  This parameter may be null.
    * @param params an Object that is used to initialize any subclasses.  For
    * this subclass, is always null.
    *
    * @exception InstantiationException is thrown if any of the critical
    * parameters are null.
    */
    public MultipointTextChat(ViewDObjectFacet viewFacet,
                                 DObjectID newID,
                                 SessionViewID homeID,
                                 URL codeBase,
                                 Serializable init) throws InstantiationException {

        super(viewFacet, newID, homeID, codeBase, init);
        ChatClientView theView = null;
        try {
            DirectViewAccessFacet viewAccessFacet = (DirectViewAccessFacet) viewFacet.getViewFacet("viewFacet", this);
            if (viewAccessFacet != null) {
                // Get the view directly (since the DirectViewAccessFacet let's us get it
                // and cast it to a type of ChatClientView...if this doesn't work, then
                // we have no UI, but that's OK (in the case of a server)
                theView = (ChatClientView) viewAccessFacet.getView();
                // Then get the user interface to use
                myTextOutput = theView.getUserInterface(this);
                // Set local name, provided by view on initialization
                myLocalName = theView.getLocalName();
            } else {
                System.out.println("MultipointTextChat.<init>.  View did not give valid facet to us...no interface available");
            }       
        } catch (ClassCastException e) {
            // If this happens then no IO is available.
            // That's OK, but our code will have to deal with it
            System.out.println("MultipointTextChat.<init>. Failed to get valid text output interface...OK if the local View is a server");

        }
    }

    /**
     * Handle text input.  This method is called synchronously by the user interface
     * when new text is available (i.e. when the user hits enter in the text
     * field
     *
     * @param aString the String that was entered
     */
    public void handleText(String newText)
    {
        // Get name...for now, just use constant
        String senderName = getSenderName();
        // Send utterance to all remotes
        Utterance anUtterance = new Utterance(getSenderName(), newText);
        sendUtteranceToRemotes(anUtterance);
        // Also show it locally
        handleReceiveUtterance(new Utterance(newText, true));
    }

    private String getSenderName()
    {
        return myLocalName;
    }

    /**
     * Send a given utterance to all of our remotes.  This just turns around
     * and calls sendClosureToRemotes with the name of the receiveMethod
     * (see receiveMethod static variable and method of same name)
     *
     * @parma anUtterance an instance of an Utterance to send.  If null,
     * nothing will be sent.
     */
    protected void sendUtteranceToRemotes(Utterance anUtterance)
    {
        try {
            // Make sure we have a valid utterance instance
            if (anUtterance != null) {
                sendClosureToRemotes(
                    BaseFacet.createNewClosure(null,
                                               receiveMethod,
                                               Closure.getObjectArrayFromParam(anUtterance)));
            }
        } catch (Exception e) {
            // Ignore if it can't be sent for any reason
            // Just spam for now
            debug("MultipointTextChat.sendUtteranceToRemotes.  Failed to send utterance "+anUtterance+" with exception "+e);
        }
    }

    /**
     * Method called on asynch reception of an utterance delivered by remote.
     * This method will be called when an utterance is received by one of our
     * remotes.  NOTE:  The name of this method is bound to the static variable
     * <b>receiveMethod</b>, if the name of this method is changed, or the
     * value do the static method above is changed, then the other should be
     * changed to match, otherwise message delivery (sent by sendUtteranceToRemotes
     * will not occur properly.
     *
     * @param anUtterance an instance of an Utterance that we have received
     */
    public void asynchReceiveUtterance(Utterance anUtterance)
    {
        // This is called when we receive an Utterance from our remote
        // For now we'll just turn around and have a handler method deal with
        // this
        handleReceiveUtterance(anUtterance);
    }

    /**
     * Actual utterance handler.  This method is called by the asynchReceiveUtterance
     * method when an utterance is received from a remote.
     *
     * @param anUtterance the Utterance delivered
     */
    public void handleReceiveUtterance(Utterance anUtterance)
    {
        // If we have something to render this with, render it by asking to
        // show itself on the given MulticastTextIO instance
        if (myTextOutput != null) {
            anUtterance.showUtterance(getID(), myTextOutput);
        }
    }

    /**
     * Message that is received when we have been activated.  In this case, we
     * try to show the MultipointTextIO window, so that our output can be
     * displayed when we receive it.
     *
     * @param info the ViewNotifyInfo that accompanies this notification
     */
    protected void activated(ViewNotifyInfo info)
    {
        if (myTextOutput != null) {
            // Show it, now that we're activated
            myTextOutput.showChatWindow();
        }
    }

    /**
     * Message that is received when we have been deactivated.  This is used
     * to do clean up...basically to remove the chat window if it is still there.
     * This is called automatically in the event of our presence going away
     * (i.e. partition).
     *
     * @param info the ViewNotifyInfo associated with this deactivation
     */
    protected void deactivated(ViewNotifyInfo info)
    {
        if (myTextOutput != null) {
            // Hide our output window
            myTextOutput.hideChatWindow();
        }
    }

}


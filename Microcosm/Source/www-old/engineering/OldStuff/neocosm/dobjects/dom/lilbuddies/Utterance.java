/****************************************************************************

    Copyright (c) 1997 Scott B. Lewis
    All Rights Reserved

    This software may not be copied or disclosed.

    $Archive: /dom/lilbuddies/Utterance.java $
    $Revision: 1 $
    $Date: 1/6/98 10:00p $
    $Author: Sbl $

    Todo:

****************************************************************************/
package dom.lilbuddies;

import dom.session.*;
import dom.id.*;

import java.io.Serializable;

/**
 * Representation of a textual utterance.  Contains instance variables to hold
 * the String 'name' of the sending DObject (optional), and the
 * String utterance itself.  This could be enhanced if necessary.  Declared
 * Serializable because it is sent over the wire.
 *
 * @author Scott Lewis
 * @see MultipointTextChat
 */
public class Utterance implements Serializable {

    private String mySenderNickName;
    private String myMessage;
    private boolean mySender = false;

    /**
     * Minimal constructor.  This is the minimal information.  The String msg
     * may be null if desired (but what would be the point of that?).
     *
     * @param msg the String message to send
     */
    public Utterance(String msg)
    {
        myMessage = msg;
    }

    /**
     * Alternative that provides a text nickname to show with the utterance
     *
     * @param name the String name to show with this message
     * @param msg the String message to show
     */
    public Utterance(String name, String msg)
    {
        this(msg);
        mySenderNickName = name;
    }
    
    /**
     * Constructor that allows the sender to specify that the utterance is
     * from here (i.e. that the text should say:  "You say: " when the
     * utterance is rendered.
     *
     * @param msg the String message to show
     * @param meSender flag to indicate whether the utterance is local or not
     */
    public Utterance(String msg, boolean meSender)
    {
        this(msg);
        mySender = true;
    }

    /**
     * Debugging support.
     *
     * @return String representation of this object
     */
    public String toString()
    {
        return "Utterance from "+mySenderNickName+": "+myMessage;
    }
    
    /**
     * Methed called to actually 'render' this utterance to an
     * implementer of the MultipointTextChat interface.  This object
     * can determine right here how it want to render itself.
     *
     * @param id the DObjectID of the DObject responsible for this
     * utterance
     * @param anInterface the MultipointTextChat interface where
     * this utterance will be shown
     */
    public void showUtterance(DObjectID id, MulticastChatTextIO ui)
    {
        if (mySender) {
            ui.showString("You say: \""+myMessage+"\"\n");
        } else {
            if (mySenderNickName != null) {
                ui.showString(mySenderNickName+" says: \""+myMessage+"\"\n");
            } else {
            ui.showString(id.toString()+" says: \""+myMessage+"\"\n");
            }
        }
    }

}
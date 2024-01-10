package dom.lilbuddies;

import dom.dobjects.*;

import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;


/**
 * Text input output interface class.  This class implements the 
 * MulticastChatTextIO interface, and are created to pass to the instances
 * of MultipointTextChat, which are considered 'owners' of this interface.
 * This allows the MultipointTextChat instances to show text output in
 * a Text area passed into this object by construction.
 */
public class TextInputOutput implements MulticastChatTextIO
{
    LilBuddiesFrame myWindow;
    TextArea myTextOutputWindow;
    MultipointTextChat myOwner;

    public TextInputOutput(LilBuddiesFrame window, TextArea output, MultipointTextChat owner)
    {
        myWindow = window;
        myTextOutputWindow = output;
        myOwner = owner;
        myWindow.setTextInputHandler(this);
    }

    /**
     * Show given string.
     *
     * @param aString the String to show
     */
    public void showString(String aString)
    {
        myTextOutputWindow.append(aString);
    }

    /**
     * Show the text chat window.
     *
     */
    public void showChatWindow()
    {
        myWindow.show();
    }

    /**
     * Hide the text chat window.
     *
     */
    public void hideChatWindow()
    {
        myWindow.hide();
    }

    /**
     * Handle input event from textfield
     *
     * @param aTextField the TextField that generated the event
     * @param anEvent the Event that was generated (should be enter hit)
     */
    public void handleInputEvent(TextField aTextField, ActionEvent anEvent)
    {
        // Pass the text onto the owner MultipointTextChat instance
        myOwner.handleText(aTextField.getText());
    }

}
package dom.lilbuddies;

import java.awt.TextField;
import java.awt.event.ActionEvent;

/**
 * Interface that describes the text io that can be done by
 * the receiving instance of the MulticastTextChat DObject.
 */
public interface MulticastChatTextIO {
    /**
     * Show given string.
     *
     * @param aString the String to show
     */
    public void showString(String aString);
    
    /**
     * Show the text chat window.
     *
     */
    public void showChatWindow();
    
    /**
     * Hide the text chat window.
     *
     */
    public void hideChatWindow();
    
    /**
     * Handle input event from textfield
     *
     * @param aTextField the TextField that generated the event
     * @param anEvent the Event that was generated (should be enter hit)
     */
    public void handleInputEvent(TextField aTextField, ActionEvent anEvent);
}
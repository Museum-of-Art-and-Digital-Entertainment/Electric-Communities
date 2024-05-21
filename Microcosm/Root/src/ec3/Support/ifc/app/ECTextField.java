/**
 * ECTextField class
 *
 * Copyright 1997-1998 Electric Communities. All rights reserved worldwide.
 * John Sullivan, Alex McKale
 *
 *  980129  agm     Swallow page up, pagedown and insert keys (bug #588).
 *
 */

package ec.ifc.app;

import netscape.application.Border;
import netscape.application.KeyEvent;
import netscape.application.TextField;

public class ECTextField extends TextField implements ECKeyFilter,
                                                      ECTabKeyDispenser
{
    // key codes
    private static final int CONTROL_A_KEYCODE = 1;
    private static final int CONTROL_C_KEYCODE = 3;
    private static final int CONTROL_V_KEYCODE = 22;
    private static final int CONTROL_X_KEYCODE = 24;

    private static final int INSERT_KEYCODE = 1025;
    private static final int PAGEUP_KEYCODE = 1002;
    private static final int PAGEDOWN_KEYCODE = 1003;
 
    // instance variables
    private ECTabKeyHandler myTabKeyHandler = null;
    
    //
    // constructors
    //

    public ECTextField() {
        super();
    }
    
    public ECTextField (int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    //
    // public methods
    //
    
    /** Overridden to handle control keys for cut/copy/paste */
    public void keyDown(KeyEvent event)  {
        // we currently swallow these keys in text fields.
        if ((event.key == INSERT_KEYCODE) || (event.key == PAGEUP_KEYCODE) || (event.key == PAGEDOWN_KEYCODE)) {
            return; 
        }
        
        // we're only interested in control key events
        if (!event.isControlKeyDown()) {
            super.keyDown(event);
            return;
        }
        switch (event.key) {
            case CONTROL_C_KEYCODE:
                if (!selectedRange().isEmpty()) {               
                    copy();
                }
                break;
            case CONTROL_X_KEYCODE:
                if (isEditable() && !selectedRange().isEmpty()) {               
                    cut();
                }
                break;
            case CONTROL_V_KEYCODE:
                if (isEditable()) {
                    paste();
                }
                break;
            case CONTROL_A_KEYCODE:
                if (isSelectable()) {
                    selectText();
                }
                break;
                
            // all other control-key combinations get dropped
        }
    }


    /**
     * Overridden to fix bug in superclass that would result in
     * the text slightly scrolled off the left edge. Note that the
     * fix calls stopFocus() on this field, so you should call
     * setDrawableCharacter before setting the focus to this view.
     */
    public void setDrawableCharacter(char aChar) {
        super.setDrawableCharacter(aChar);
        
        // there's a bug in TextField._computeScrollOffset that ends
        // up with a negative scroll offset if the text field thinner
        // than its border's left margin. Work around this here by
        // calling stopFocus(), which resets the scroll offset even
        // if this textfield didn't have the focus.
        Border border = border();
        int width = width();
        if (width == 0 || (border != null && border.leftMargin() > width)) {
            stopFocus();    
        }
    }
    
    /**
     * Responsibility from ECTabKeyDispenser. Sets object that will
     * handle tab key presses.
     */
    public void setTabKeyHandler(ECTabKeyHandler newHandler) {
        myTabKeyHandler = newHandler;
    }

    /**
     * Responsibility from ECTabKeyDispenser. Returns object that will
     * handle tab key presses.
     */
    public ECTabKeyHandler tabKeyHandler() {
        return myTabKeyHandler;
    }

    /**
     * Responsibility from ECKeyFilter. Returns false, so the escape key can
     * be used to activate the cancel button.
     */
     public boolean wantsEscapeKey() {
        return false;
    }

    /**
     * Responsibility from ECKeyFilter. Returns false. This allows
     * ECRootView to convert Return key presses into Tabs when there's
     * no default button, which is the desired UI behavior.
     */
     public boolean wantsReturnKey() {
        return false;
    }
}


/**
 * ECTextView class
 *
 * Copyright 1997-1998 Electric Communities. All rights reserved worldwide.
 * John Sullivan, Alex McKale
 *
 *  980129  agm     Swallow insert keys (bug #588).
 *
 */

package ec.ifc.app;

import netscape.application.KeyEvent;
import netscape.application.Range;
import netscape.application.Rect;
import netscape.application.TextParagraphFormat;
import netscape.application.TextView;
import netscape.util.Vector;

public class ECTextView extends TextView implements ECKeyFilter,
                                                    ECTabKeyDispenser
{
    // key codes
    private static final int CONTROL_A_KEYCODE = 1;
    private static final int CONTROL_C_KEYCODE = 3;
    private static final int CONTROL_V_KEYCODE = 22;
    private static final int CONTROL_X_KEYCODE = 24;

    private static final int INSERT_KEYCODE = 1025;

    // instance variables
    private ECTabKeyHandler myTabKeyHandler = null;

    //
    // constructors
    //

    /** Returns a new ECTextView with bounds (0, 0, 0, 0) */
    public ECTextView() {
        super();
    }
    
    /** Returns a new ECTextView with bounds (x, y, width, height) */
    public ECTextView (int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    //
    // public methods
    //

    /** Overridden to handle control keys for cut/copy/paste, swallow insert key */
    public void keyDown(KeyEvent event)  {
        
        // we currently swallow these keys in textviews.
        if (event.key == INSERT_KEYCODE) {
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
                    selectRange(new Range(0, length()));
                }
                break;
            // all other control-key combinations get dropped
        }
    }

    /**
     * Returns whether or not this textView has exactly one line.
     * No text at all counts as one line.
     */
    public boolean isOneLiner() {
        // Since there's no TextView call that returns the number of lines,
        // I compute whether this is a one-liner by checking the y position
        // and height of the rectangle bounding the first character with the
        // y position and height of the rectangle bounding the last character.
        // You'd think there'd be an easy way to determine this...
        Vector wholeRangeRects = 
            rectsForRange(new Range(0, length()));
        Vector firstCharRects = 
            rectsForRange(new Range(0, 1));

        // check for degenerate no-text-at-all case
        if (wholeRangeRects.size() == 0)
            return true;

        Rect firstCharRect = (Rect)firstCharRects.elementAt(0);
        Rect lastCharRect = (Rect)wholeRangeRects.lastElement();

        return (firstCharRect.y == lastCharRect.y) 
                && (firstCharRect.height == lastCharRect.height);
    }

    /** 
     * Sets the default justification of this textView to the specified
     * value. The value must be Graphics.LEFT_JUSTIFIED, Graphics.CENTERED,
     * or Graphics.RIGHT_JUSTIFIED.
     */
    public void setDefaultJustification(int justification) {
        // The IFC document for TextParagraphFormat claims that the best way
        // to do this sort of thing is to get the current TextParagraphFormat
        // and then change the parts you want to change, so that's what I did
        netscape.util.Hashtable attributes = defaultAttributes();
        TextParagraphFormat format = 
            (TextParagraphFormat)attributes.get(PARAGRAPH_FORMAT_KEY);
        if (format != null) {
            TextParagraphFormat newFormat = (TextParagraphFormat)format.clone();
            newFormat.setJustification(justification);
            addDefaultAttribute(PARAGRAPH_FORMAT_KEY, newFormat);
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
     * Responsibility from ECKeyFilter. Returns false if the text view
     * is not editable, so the return key can be used to activate the
     * default button.
     */
     public boolean wantsReturnKey() {
        return isEditable();
    }
}

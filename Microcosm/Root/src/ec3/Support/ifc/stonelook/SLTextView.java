package ec.ifc.stonelook;

import netscape.application.*;
import netscape.util.Vector;
import ec.ifc.app.ECTextView;

/**
 * Subclass of TextView that applies the stone-texture look.
 */
public class SLTextView extends ECTextView {

    // used to remember whether this view is currently in focus
    private boolean myIsBeingEdited = false;
    // used in drawView trick to draw selection in different color
    private boolean myDontDirtyNow = false;
    
    //
    // Constructors
    //
    public SLTextView() {
        this(0, 0, 0, 0);
    }

    public SLTextView (int x, int y, int width, int height) {
        super(x, y, width, height);
        setSelectionColor(StoneLook.editableTextSelectionColor());
        setCaretColor(StoneLook.editableTextSelectionColor());
        setFont(StoneLook.standardFontBold());
        updateColors();
    }

    //
    // Public Instance Methods
    //

    /** Overridden to do nothing at certain key internal-use only
     * times that you don't need to know about.
     */
    public void addDirtyRect(Rect dirtyRect) {
        if (myDontDirtyNow) {
            return;
        }
        super.addDirtyRect(dirtyRect);
    }

    /** Overridden to draw selected text in different color */
    public void drawView(Graphics g) {
        
        // first draw the normal way
        super.drawView(g);
        
        // if we aren't the active field, or we don't have
        // a selection, we're done
        if (!myIsBeingEdited || !hasSelection()) {
            return;
        }

        Range selection = selectedRange();

        // Bail out if the selected range is empty (such as if the insertion
        // point is blinking). Without this test here, the
        // setTextColorWithoutDirtying() calls below mysteriously prevent the
        // insertion point from displaying properly. I don't know why.
        if (selection.isEmpty()) {
            return;
        }
        
        // Clip out the text before and after the selected range, then redraw
        // with different font. This assumes the selected range is all on a single line.
        Color oldTextColor = textColor();
        setTextColorWithoutDirtying(StoneLook.selectedTextColor());
        
        Vector selectionRects = rectsForRange(selection);
        int count = selectionRects.count();
        for (int i = 0; i < count; i+=1) {
            g.pushState();
            g.setClipRect((Rect)selectionRects.elementAt(i));
            super.drawView(g);
            g.popState();
        }
        
        setTextColorWithoutDirtying(oldTextColor);
    }
    
    /*
    public void mouseDragged(MouseEvent event) {
        if (containsPoint(event.x, event.y)) {
            super.mouseDragged(event);
        }
        else {
            DragSession session = new DragSession(
                this, StoneLook.draggedTextImage(), event.x, event.y, event.x, event.y, 
                MY_DATA_TYPE, string());
        }
    }
    */
    
    /** Overridden to do nothing at certain key internal-use only
     * times that you don't need to know about.
     */
    public void setDirty(boolean newValue) {
        if (myDontDirtyNow) {
            return;
        }
        super.setDirty(newValue);
    }

    /** Overridden to set the border colors appropriately */
    public void setEditable(boolean newValue) {
        super.setEditable(newValue);
        updateColors();
    }
    
    /** Overridden to update text color if necessary */
    public void setTransparent(boolean newValue) {
        super.setTransparent(newValue);
        updateColors();
    }
    
    /** Overridden to remember that we are in focus */
    public void startFocus() {
        super.startFocus();
        myIsBeingEdited = true;
    }
    
    /** Overridden to remember that we are not in focus */
    public void stopFocus() {
        super.stopFocus();
        myIsBeingEdited = false;
    }
    
    //
    // Private Methods
    //

    /**
     * Workaround that enables me to change the text color while drawing
     * without dirtying the view -- dirtying the view while drawing
     * is an IFC no-no.
     */
    private void setTextColorWithoutDirtying(Color newColor) {
        myDontDirtyNow = true;
        setTextColor(newColor);
        myDontDirtyNow = false;
    }
    

    private void updateColors() {
        Color fillColor;
        Color textColor;
        
        // text labels (transparent) use editable text colors
        if (isEditable() || isTransparent()) {
            fillColor = StoneLook.editableTextFillColor();
            textColor = StoneLook.editableTextColor();
        } else {
            fillColor = StoneLook.disabledEditableTextFillColor();
            textColor = StoneLook.disabledEditableTextColor();
        }
        setBackgroundColor(fillColor);
        setTextColor(textColor);
        setDirty(true);
    }
}

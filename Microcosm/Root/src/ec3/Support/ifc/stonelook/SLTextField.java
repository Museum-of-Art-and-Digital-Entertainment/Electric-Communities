package ec.ifc.stonelook;

import netscape.application.*;
import ec.ifc.app.ECTextField;

/**
 * Subclass of TextField that applies the stone-texture look.
 */
public class SLTextField extends ECTextField {

    // used in drawView trick to draw selection in different color
    private boolean myDontDirtyNow = false;

    //
    // constructors
    //
    /** creates a stone-look text field with empty bounds */
    public SLTextField() {
        this(0, 0, 0, 0);
    }

    /** creates a stone-look text field with specified bounds */    
    public SLTextField (int x, int y, int width, int height) {
        super(x, y, width, height);

        setSelectionColor(StoneLook.editableTextSelectionColor());
        setCaretColor(StoneLook.editableTextSelectionColor());
        setFont(StoneLook.standardFontBold());
        setBorder(new SLBezelBorder(BezelBorder.LOWERED));
        updateColors();
    }
    
    //
    // static methods
    //
    /** use the standard stone look font 
     *  not overriden because we want to change the return type
     */
    public static TextField createLabel(String string) {
        FontMetrics metrics = StoneLook.standardFontBold().fontMetrics();
        int width = metrics.stringWidth(string),
            height = metrics.stringHeight();
        SLTextField label = new SLTextField(0, 0, width, height);

        label.setBorder(null);
        label.setStringValue(string);
        label.setFont(StoneLook.standardFontBold());
        label.setTransparent(true);
        label.setEditable(false);
        label.setSelectable(false);
        return label;
    }
    
    //
    // public methods
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
        if (!isBeingEdited() || !hasSelection()) {
            return;
        }
        
        // Clip out the text before and after the selected range, then redraw
        // with different font. This assumes the selected range is all on a single line.
        g.pushState();
        Color oldTextColor = textColor();
        setTextColorWithoutDirtying(StoneLook.selectedTextColor());
        Range selection = selectedRange();
        int x1 = xPositionOfCharacter(selection.index());
        int x2 = xPositionOfCharacter(selection.index() + selection.length());
        g.setClipRect(new Rect(x1, 0, x2 - x1, bounds.height));
        super.drawView(g);
        setTextColorWithoutDirtying(oldTextColor);
        g.popState();       
    }
    
    /** overridden to set the border's inside color as well */
    public void setBackgroundColor(Color newColor) {
        super.setBackgroundColor(newColor);
        Border border = border();
        if (border instanceof SLBezelBorder) {
            ((SLBezelBorder)border).setInsideColor(newColor);
        }
    }

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
    
    //
    // private methods
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
        Color borderColor;
        Color textColor;
        
        // text labels (transparent) use editable text colors
        if (isEditable()|| isTransparent()) {
            fillColor = StoneLook.editableTextFillColor();
            borderColor = StoneLook.editableBorderColor();
            textColor = StoneLook.editableTextColor();
        } else {
            fillColor = StoneLook.disabledEditableTextFillColor();
            borderColor = StoneLook.disabledBorderColor();
            textColor = StoneLook.disabledEditableTextColor();
        }
        setBackgroundColor(fillColor);
        setTextColor(textColor);
        Border border = border();
        if (border instanceof SLBezelBorder) {
            ((SLBezelBorder)border).setLineColor(borderColor);
        }
        setDirty(true);
    }
}

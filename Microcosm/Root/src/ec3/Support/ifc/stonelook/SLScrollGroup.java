package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Subclass of ScrollGroup that applies the stone-texture look.
 */
public class SLScrollGroup extends ScrollGroup {
    
    /** space between scroll bar and scrolled contents */
    private static final int SCROLL_BAR_MARGIN = 5;
    
    /** used to prevent recursion in layoutView */
    private boolean layingOutView = false;
    
    /**
     * Returns a new scroll group with the stone-texture look and
     * empty bounds.
     */
    public SLScrollGroup() {
        // don't make the bounds completely empty, because
        // the scroll bars are sometimes positioned wrong
        // if they are created with 0 height/width (AWT/IFC bug)
        this(0, 0, 4, 4);
    }
    
    /**
     * Returns a new scroll group with the stone-texture look and the
     * specified bounds.
     */
    public SLScrollGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
        
        setBorder(new SLBezelBorder(BezelBorder.LOWERED));
        setBackgroundColor(StoneLook.dataBackgroundColor());
        setCornerColor(null);       
    }

    /** overridden to create an SLScrollBar */
    protected ScrollBar createScrollBar(boolean horizontal) {
        SLScrollBar aBar;

        if (horizontal) {
            aBar = new SLScrollBar(0, 0, bounds.width, 1, Scrollable.HORIZONTAL);
        } else {
            aBar = new SLScrollBar(0, 0, 1, bounds.height, Scrollable.VERTICAL);
        }

        return aBar;
    }

    /** overridden to create an SLScrollView */
    protected ScrollView createScrollView() {
        return new SLScrollView(0, 0, bounds.width, bounds.height);
    }
    
    /** overridden to leave space between scroll bar and scrolled content */
    public void layoutView(int x, int y) {
        // Changing the size of the scroll bars can change their active state,
        // which makes the superclass call layoutView again. If the size of
        // the scroll bar is right on the border between active and inactive,
        // this can recurse until stack overflow. To avoid this, don't layout
        // the view again if we're already in the middle of laying it out.
        // IFC's ScrollBar uses a similar trick with a private variable
        // named ignoreScrollBars
        if (layingOutView) {
            return;
        }   
    
        layingOutView = true;
        super.layoutView(x, y);
        
        if (hasVertScrollBar()) {
            // leave space on right side of content
            scrollView().sizeBy(-SCROLL_BAR_MARGIN, 0);
            if (hasHorizScrollBar()) {
                horizScrollBar().sizeBy(-SCROLL_BAR_MARGIN, 0);
            }
        }
        if (hasHorizScrollBar()) {
            // leave space on bottom of content
            scrollView().sizeBy(0, -SCROLL_BAR_MARGIN);
            if (hasVertScrollBar()) {
                vertScrollBar().sizeBy(0, -SCROLL_BAR_MARGIN);
            }
        }
        layingOutView = false;
    }

    /** overridden to set the border's inside color as well */
    public void setBackgroundColor(Color newColor) {
        super.setBackgroundColor(newColor);
        Border border = border();
        if (border instanceof SLBezelBorder) {
            ((SLBezelBorder)border).setInsideColor(newColor);
        }
    }
}
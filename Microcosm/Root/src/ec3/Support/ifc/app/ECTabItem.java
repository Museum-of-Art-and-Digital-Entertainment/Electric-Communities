package ec.ifc.app;

import netscape.application.*;

/** 
 * Class representing one tab in an ECTabView.
 * @see ECTabView
 */
public class ECTabItem implements Cloneable {
    
    //
    // constants
    //
    /** longest supported character count for a label */
    protected static final int MAX_LABEL_LENGTH = 256;
    
    /** desirable minimum extra white space around horizontal title */  
    protected static final int HORIZ_TITLE_PADDING = 8;
    /** desirable minimum extra white space around vertical title */    
    protected static final int VERT_TITLE_PADDING = 8;

    //
    // state
    //  
    protected ECTabView myTabView;
    protected String myCommand;
    protected String myTitle;
    protected String myTip;
    protected Font myFont;
    protected Color myBackgroundColor = Color.lightGray;
    protected Color myEdgeColor = Color.black;
    protected Color mySelectedColor = Color.white;
    protected Color mySelectedEdgeColor = Color.black;
    protected Color myTitleColor = Color.black;
    protected Color mySelectedTitleColor = Color.black;
    protected boolean mySelected = false;
    protected boolean myHilited = false;

    //
    // cached calculations and data
    //
    private char[] myLabelChars = new char[MAX_LABEL_LENGTH];

    //
    // constructors
    //
    /**
     * Returns a new ECTabItem with background color light gray and
     * selected color white.
     */
    public ECTabItem() {
    }
    
    //
    // public methods
    //
    
    /**
     * Returns the color the ECTabItem uses to draw its background when
     * not selected. By default this is Color.lightGray
     */
    public Color backgroundColor() {
        return myBackgroundColor;
    }

    /**
     * Clones the ECTabItem. ECTabView adds additional tabs by cloning its
     * prototype ECTabItem.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            return null;
        }
    }
        
    /** 
     * Called by ECTabView to draw the tab. The horizontal space between
     * tabs has already been taken into account, so the tab should extend to
     * all edges of <b>boundsRect</b>. This just calls drawBackground() and
     * drawStringInRect() in turn, so subclasses might want to override one
     * or the other of those methods rather than this one.
     */
    public void drawInRect(Graphics g, Rect boundsRect, boolean toDrawString) {
        drawBackground(g, boundsRect);
        if (toDrawString) {
            drawStringInRect(g, myTitle, boundsRect,
                mySelected ? mySelectedTitleColor : myTitleColor);
        }
    }
    
    /**
     * Returns the command associated with this ECTabItem.
     * @see #setCommand
     */
    public String command() {
        return myCommand;
    }

    /**
     * Returns the Font the ECTabItem uses to render its title. If no font has
     * been set, returns <b>Font.defaultFont()</b>.
     */
    public Font font() {
        if (myFont == null) {
            myFont = Font.defaultFont();
        }

        return myFont;
    }

    /**
     * Returns <b>true</b> if the ECTabItem is selected.
     * @see #setSelected
     */
    public boolean isHilited() {
        return myHilited;
    }

    /**
     * Returns <b>true</b> if the ECTabItem is selected.
     * @see #setSelected
     */
    public boolean isSelected() {
        return mySelected;
    }
    
    /**
     * Returns the minimum length (long dimension) of this tab. This
     * length is enough to hold the title with a small margin.
     */
    public int minLength() {
        FontMetrics metrics = font().fontMetrics();
        if (myTabView.isHorizontal()) {
            return metrics.stringWidth(myTitle) + HORIZ_TITLE_PADDING;
        } else {
            return metrics.ascent() * myTitle.length() + VERT_TITLE_PADDING;        
        }
    }   

    /**
     * Returns the color the ECTabItem uses to draw its background when
     * selected. By default this is Color.white
     */
    public Color selectedColor() {
        return mySelectedColor;
    }

    /**
     * Sets the color the ECTabItem uses to draw its background when
     * not selected.
     * @see #getBackgroundColor
     */
    public void setBackgroundColor(Color newBackgroundColor) {
        myBackgroundColor = newBackgroundColor;
    }

    /**
     * Sets the command associated with this ECTabItem.  ECTabView sends
     * ECTabItem's command when the ECTabItem is clicked.
     */
    public void setCommand(String newCommand) {
        myCommand = newCommand;
    }

    /** Sets the font the ECTabItem uses to draw its title. */
    public void setFont(Font newFont) {
        myFont = newFont;
    }

    /**
     * Called by ECTabView when a ECTabItem is going to be subjected to drop.
     */
    public void setHilited(boolean flag) {
        myHilited = flag;
    }

    /**
     * Called by ECTabView when a ECTabItem is selected. By default, selected
     * ECTabItems highlight by painting a white background.
     */
    public void setSelected(boolean flag) {
        mySelected = flag;
    }

    /**
     * Sets the color the ECTabItem uses to draw its background when
     * selected.
     * @see #getSelectedColor
     */
    public void setSelectedColor(Color newSelectedColor) {
        mySelectedColor = newSelectedColor;
    }

    /** Sets the tip the ECTabItem displays. */
    public void setTip(String newTip) {
        myTip= newTip;
    }
    
    /** Sets the title the ECTabItem displays. */
    public void setTitle(String newTitle) {
        myTitle = newTitle;
    }
    
    /**
     * Sets the color the ECTabItem uses to draw its title when
     * not selected.
     * @see #titleColor
     */
    public void setTitleColor(Color newTitleColor) {
        myTitleColor = newTitleColor;
    }

    /**
     * Sets the color the ECTabItem uses to draw its title when
     * selected.
     * @see #selectedTitleColor
     */
    public void setSelectedTitleColor(Color newColor) {
        mySelectedTitleColor = newColor;
    }

    /** Returns the ECTabView associated with the ECTabItem. Returns
      * <b>null</b> if the ECTabItem is not in an ECTabView.
      */
    public ECTabView tabView() {
        return myTabView;
    }

    /**
     * Returns the title the ECTabItem displays.
     * @see #setTip
     */
    public String tip() {
        return myTip;
    }

    /**
     * Returns the title the ECTabItem displays.
     * @see #setTitle
     */
    public String title() {
        return myTitle;
    }
    
    /**
     * Returns the color the ECTabItem uses to draw its title
     * when not selected. By default this is Color.black.
     */
    public Color titleColor() {
        return myTitleColor;
    }

    /**
     * Returns the color the ECTabItem uses to draw its title
     * when selected. By default this is Color.black.
     */
    public Color selectedTitleColor() {
        return mySelectedTitleColor;
    }

    //
    // package and protected methods
    //
    
    /**
     * Called from <b>drawInRect()</b> to draw the ECTabItem's background
     * (everything other than the title). Subclasses can override this method
     * to draw the background in a special way.
     */
    protected void drawBackground(Graphics g, Rect boundsRect) {
        g.setColor(mySelected ? mySelectedColor : myBackgroundColor);
        g.fillRect(boundsRect);
        g.setColor(mySelected ? mySelectedEdgeColor : myEdgeColor);
        g.drawRect(boundsRect);     
    }
    
    /**
     * Called from <b>drawInRect()</b> to draw the ECTabItem's title.
     * Subclasses can override this method to draw the title string in a
     * special way.
     */
    protected void drawStringInRect(Graphics g, String title,
                                 Rect textBounds, Color titleColor) {
        int startIndex = 0, endIndex = title.length();
        g.pushState();
        
        g.setColor(titleColor);
        Font titleFont = font();        
        g.setFont(titleFont);
        FontMetrics metrics = titleFont.fontMetrics();

        if (myTabView.isHorizontal()) {
            // center text horizontally in tab; clips ends if it doesn't fit
            g.setClipRect(textBounds);
            g.drawStringInRect(title.substring(startIndex, endIndex), 
                               textBounds, Graphics.CENTERED);
        }
        else {
            // draw text one character at a time vertically
            // Clip bottom if it doesn't fit
            int spacing = metrics.ascent();
            int charCount = endIndex - startIndex;
            int totalHeight = charCount*spacing;
            int midX = textBounds.midX();
            
            g.setClipRect(textBounds);
            
            title.getChars(0, charCount, myLabelChars, 0);
            
            int firstCharTop = (textBounds.height < totalHeight) ? 
                                textBounds.y : textBounds.midY() - totalHeight/2; 
            int baseline = firstCharTop + metrics.ascent();
            for (int i = startIndex; i < endIndex; ++i) {
                myLabelChars[i] = Character.toUpperCase(myLabelChars[i]);
                g.drawChars(myLabelChars, i, 1,
                            midX - metrics.charWidth(myLabelChars[i])/2, baseline);
                baseline += spacing;
            }
        }
        
        // restore clip, etc.
        g.popState();
    }   

// These were part of an abandoned experiment, but we might want to reuse this code some day    
//    private static int getStringStartIndex(String title, int offset, 
//                                           boolean horizontal, FontMetrics metrics) {
//        int strlen = title.length();
//        int startIndex = 0;
//
//        if (horizontal) {
//            int width = 0;
//
//            while(startIndex < strlen) {
//                width += metrics.charWidth(title.charAt(startIndex++));
//                if (width >= offset) {
//                    break;
//                }
//            }
//
//        }
//        else {
//            startIndex = offset / metrics.charHeight();
//        }
//
//        return startIndex;
//    }
//
//    private static int getStringEndIndex(String title, int offset, 
//                                         boolean horizontal, FontMetrics metrics) {
//        int endIndex = title.length();
//
//        if (horizontal) {
//            int width = 0;
//
//            while(endIndex > 0) {
//                width += metrics.charWidth(title.charAt(--endIndex));
//                if (width >= offset) {
//                    break;
//                }
//            }
//
//        }
//        else {
//            endIndex = offset / metrics.charHeight();
//        }
//
//        return endIndex;
//    }

    /** sets which set of index tabs this item is a part of */
    protected void setTabView(ECTabView newTabView) {
        myTabView = newTabView;
    }
}

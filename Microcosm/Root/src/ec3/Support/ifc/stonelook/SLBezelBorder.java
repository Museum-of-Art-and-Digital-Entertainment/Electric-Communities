package ec.ifc.stonelook;

import netscape.application.*;

/**
 * A tan bezel that's part of the stone-texture look. This is used for
 * several of the other SL classes, and can be used by itself.
 */
public class SLBezelBorder extends Border {
    //
    // constants
    //

    /** diameter that gives us the amount of roundedness we want */
    // used by SLButton, so not private
    static final int ROUNDED_CORNER_DIAMETER = 6;   

    /** border width (all four sides) of all SLBezelBorders */
    public static final int BORDER_THICKNESS = 2;
    
    private int myType = BezelBorder.LOWERED;
    
    private Color myInsideColor;
    private Color myLineColor;
    private Color myHighlightColor;

    //
    // constructors
    //
    
    /** Creates a lowered SLBezelBorder */
    public SLBezelBorder() {
        this(BezelBorder.LOWERED);
    }
    
    /**
     * Creates an SLBezelBorder of the specified type.
     * The type must be either BezelBorder.LOWERED or BezelBorder.RAISED.
     * The colors are set to defaults that can be changed later.
     */
    public SLBezelBorder (int type) {
        if (type != BezelBorder.LOWERED && type != BezelBorder.RAISED)
            throw new IllegalArgumentException("invalid border type " + type);
        myType = type;
        
        myInsideColor = StoneLook.lightBackgroundColor();
        myLineColor = StoneLook.standardBorderColor();
        myHighlightColor = StoneLook.standardBorderHighlightColor();
    }
    
    //
    // static methods
    //
    
    /**
     * Convenience method for drawing a stone-looking bezel without having
     * to create a bezel object.
     */
    public static void drawBezel(Graphics g, int x, int y, 
                                 int width, int height,
                                 boolean raised,
                                 Color lineColor,
                                 Color insideColor,
                                 Color highlightColor) {
                                 
        // fill the inner rect with the inside color
        if (insideColor != null) {
            g.setColor(insideColor);
            g.drawRect(x + 1, y + 1, width - BORDER_THICKNESS, height - BORDER_THICKNESS);
        }
        
        // draw the outer border with the line color, optionally rounded
            
        // first draw entire rounded rect in line color
        g.setColor(lineColor);
        g.drawRoundedRect(x, y, width, height, ROUNDED_CORNER_DIAMETER, ROUNDED_CORNER_DIAMETER);
        
        // if there's a highlight color, use it to over-draw the two highlighted
        // edges that give this border a 3-D effect. If there's no highlight
        // color, the border will be a single color
        if (highlightColor != null) {
            g.setColor(highlightColor);
            Rect edge = new Rect();
            // come back to oldClip each time to avoid slow pushState/popState
            Rect oldClip = g.clipRect();
            if (raised) {
                // top edge
                edge.setBounds(x, y, width - BORDER_THICKNESS, BORDER_THICKNESS);
                g.setClipRect(oldClip, false);
                g.setClipRect(edge, true);
                g.drawRoundedRect(
                    x, y, width, height, ROUNDED_CORNER_DIAMETER, ROUNDED_CORNER_DIAMETER);       
                
                // left edge
                edge.setBounds(x, y, BORDER_THICKNESS, height - BORDER_THICKNESS);
                g.setClipRect(oldClip, false);
                g.setClipRect(edge, true);
                g.drawRoundedRect(
                    x, y, width, height, ROUNDED_CORNER_DIAMETER, ROUNDED_CORNER_DIAMETER);       
            } else {
                // bottom edge
                edge.setBounds(x + BORDER_THICKNESS, y + height - BORDER_THICKNESS,
                              width - BORDER_THICKNESS, BORDER_THICKNESS);
                g.setClipRect(oldClip, false);
                g.setClipRect(edge, true);
                g.drawRoundedRect(
                    x, y, width, height, ROUNDED_CORNER_DIAMETER, ROUNDED_CORNER_DIAMETER);       
                
                // right edge
                edge.setBounds(x + width - BORDER_THICKNESS, y + BORDER_THICKNESS,
                              BORDER_THICKNESS, height - BORDER_THICKNESS);
                g.setClipRect(oldClip, false);
                g.setClipRect(edge, true);
                g.drawRoundedRect(
                    x, y, width, height, ROUNDED_CORNER_DIAMETER, ROUNDED_CORNER_DIAMETER);       
            }
            g.setClipRect(oldClip, false);
        }
    }
    
    //
    // public methods
    //
    
    /** Overridden to draw custom border. */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        drawBezel(g, x, y, width, height, isRaised(),
            myLineColor, myInsideColor, myHighlightColor);
    }

    public int bottomMargin() {
        return BORDER_THICKNESS;
    }
    
    /** Returns the color used for the light edges of the 3-Dish border */
    public Color highlightColor() {
        return myHighlightColor;
    }
    
    /** Returns the color used for filling inside the border line */
    public Color insideColor() {
        return myInsideColor;
    }

    public int leftMargin() {
        return BORDER_THICKNESS;
    }
    
    /** Returns the color used for drawing the border line */
    public Color lineColor() {
        return myLineColor;
    }

    public int rightMargin() {
        return BORDER_THICKNESS;
    }
    
    /**
     * Sets the highlight color.
     * @see #highlightColor
     */
    public void setHighlightColor(Color newColor) {
        myHighlightColor = newColor;
    }

    /**
     * Sets the inside color.
     * @see #insideColor
     */
    public void setInsideColor(Color newColor) {
        myInsideColor = newColor;
    }

    /**
     * Sets the line color.
     * @see #lineColor
     */
    public void setLineColor(Color newColor) {
        myLineColor = newColor;
    }

    public int topMargin() {
        return BORDER_THICKNESS;
    }

    //
    // private methods
    //

    /** simple convenience function to check for raised type */
    private boolean isRaised() {
        return myType == BezelBorder.RAISED;
    }
}

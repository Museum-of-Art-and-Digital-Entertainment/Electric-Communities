package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Thickish border used for large areas resting on window background, such
 * as vertical tabs.
 */
public class SLSlabBorder extends Border {

    /** Creates a new SLSlabBorder. */
    public SLSlabBorder() {
    }
    
    //
    // public static methods
    //
    
    /**
     * Returns the thickness of this border. The thickness is the same for
     * all four edges.
     */
    public static int borderThickness() {
        // assumes that all color sets have same length
        return StoneLook.slabBorderHighlightColors().length; ;
    }
    
    /** draws a slab border in the given rectangle */
    public static void drawBorder(Graphics g, int x, int y,
                                   int width, int height) {
        Color colors[];
        int maxInset = 2;

        colors = StoneLook.slabBorderHighlightColors();
        drawTopEdge(g, x, y, width, height, colors, maxInset);
        drawLeftEdge(g, x, y, width, height, colors, maxInset);

        colors = StoneLook.slabBorderShadowColors();
        drawBottomEdge(g, x, y, width, height, colors, maxInset);
        drawRightEdge(g, x, y, width, height, colors, maxInset);            
    }

    
    //
    // private static methods
    //

    /**
     * Draws colored shading on top of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected static void drawTopEdge(Graphics g, 
                               int x, int y, int width, int height,
                               Color colors[], int maxInset) {
        
        for (int i = 0; i < colors.length; i += 1) {
            int inset = Math.max(maxInset - i, 0);
            g.setColor(colors[i]);
            g.drawLine(x + inset, y + i, x + width - inset - 1, y + i);
        }
    }

    /**
     * Draws colored shading on left of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected static void drawLeftEdge(Graphics g,
                                int x, int y, int width, int height,
                                Color colors[], int maxInset) {

        for (int i = 0; i < colors.length; i += 1) {
            g.setColor(colors[i]);
            g.drawLine(x + i, y + maxInset,
                       x + i, y + height - 1 - Math.max(maxInset - i, 0));
        }
                                
    }

    /**
     * Draws colored shading on right of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected static void drawRightEdge(Graphics g,
                                 int x, int y, int width, int height,
                                 Color colors[], int maxInset) {
                                
            for (int i = 0; i < colors.length; i += 1) {
            int inset = maxInset + Math.max(i, 1);
            g.setColor(colors[i]);
            g.drawLine(x + width - i - 1, y + Math.max(maxInset - i, 0),
                       x + width - i - 1, y + height - inset);
        }
    }

    /**
     * Draws colored shading on bottom of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected static void drawBottomEdge(Graphics g,
                                  int x, int y, int width, int height,
                                  Color colors[], int maxInset) {
                                
        for (int i = 0; i < colors.length; i += 1) {
            int inset = Math.max(maxInset - i, 0);
            g.setColor(colors[i]);
            g.drawLine(x + inset, y + height - i - 1,
                       x + width - inset - 1, y + height - i - 1);
        }
    }
    
    //
    // public instance methods
    //

    /** Overridden to draw the customized look */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        drawBorder(g, x, y, width, height);
    }

    /** Overridden to compute border size properly */
    public int bottomMargin() {
        return borderThickness();
    }

    /** Overridden to compute border size properly */
    public int leftMargin() {
        return borderThickness();
    }

    /** Overridden to compute border size properly */
    public int rightMargin() {
        return borderThickness();
    }

    /** Overridden to compute border size properly */
    public int topMargin() {
        return borderThickness();
    }

}

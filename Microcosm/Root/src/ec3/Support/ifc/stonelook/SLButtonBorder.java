package ec.ifc.stonelook;

import netscape.application.*;

/**
 * Button border used for our custom look. SLButton.createPushButton uses
 * this automatically, so this class shouldn't need to be made public.
 * @see SLButton#createPushButton
 */
class SLButtonBorder extends Border {

    /** Standard button height. */
    public static final int BUTTON_HEIGHT = StoneLook.standardButtonHeight();

    /** button that this border is drawn around */  
    private SLButton owningButton = null;

    /**
     * Creates a new SLButtonBorder. The border appears raised or lowered
     * or disabled depending on the state of <b>button</b>. Normal use is to
     * create one border per button, using it for both the raised and lowered
     * borders of the button.
     */
    public SLButtonBorder(SLButton button) {
        owningButton = button;
    }
    
    //
    // public static methods
    //
    
    /**
     * Returns the width of this border. The width is the same for
     * all four edges.
     */
    public static int borderWidth() {
        return SLBezelBorder.BORDER_THICKNESS;
    }
    
    /** draws a raised standard-color border in the given rect */
    public static void drawBorder(Graphics g, int x, int y,
                                  int width, int height) {
        drawBorder(g, x, y, width, height, true, true, false);
    }
    
    //
    // private static methods
    //

    /**
     * Draws all the different variations. This is the bottleneck for
     * both the instance methods and the statics.
     */
     // We could make it public if a need ever arises, but for simplicity
     // of API I've left it private for now.
    private static void drawBorder(Graphics g, int x, int y,
                                   int width, int height,
                                   boolean isRaised, boolean isEnabled,
                                   boolean isDefault) {
                                   
        Color lineColor, insideColor, highlightColor;
        
        if (isEnabled) {
            lineColor = StoneLook.standardBorderColor();
            insideColor = StoneLook.lightBackgroundColor();
            highlightColor = StoneLook.standardBorderHighlightColor();
        } else {
            lineColor = StoneLook.disabledButtonColor();
            insideColor = lineColor;
            highlightColor = null;
        }
                                  
        SLBezelBorder.drawBezel(g, x, y, width, height, isRaised,
                                lineColor, insideColor, highlightColor);

    }


    //
    // public instance methods
    //

    /** Overridden to draw the customized look */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        drawBorder(g, x, y, width, height, !owningButton.state(),
                   owningButton.isEnabled(), owningButton.isDefault());
    }

    /** Overridden to compute border size properly */
    public int bottomMargin() {
        return borderWidth();
    }

    /** Overridden to compute border size properly */
    public int leftMargin() {
        return borderWidth();
    }

    /** Overridden to compute border size properly */
    public int rightMargin() {
        return borderWidth();
    }

    /** Overridden to compute border size properly */
    public int topMargin() {
        return borderWidth();
    }

}

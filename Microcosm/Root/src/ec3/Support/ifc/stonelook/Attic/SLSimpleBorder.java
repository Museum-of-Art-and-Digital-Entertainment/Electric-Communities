/**
 * SLSimpleBorder.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */
package ec.ifc.stonelook;

import netscape.application.Border;
import netscape.application.Color;
import netscape.application.Graphics;

/**
 * Simple Button border used for some of the stone-texture looks. 
 * SLButton.createPushButton uses this with the fifth argument set to true
 * so this class shouldn't need to be made public.
 * @see SLButton#createPushButton
 */
class SLSimpleBorder extends Border {

    /** Only height at which this border looks right. */
    private SLButton owningButton = null;

    /**
     * Creates a new SLButtonBorder. The border appears raised or lowered
     * or disabled depending on the state of <b>button</b>. Normal use is to
     * create one border per button, using it for both the raised and lowered
     * borders of the button.
     */
    public SLSimpleBorder(SLButton button) {
        owningButton = button;
    }

    /** Overridden to draw the marbleized look */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        Color colors[];

        // order of drawing the edges changes when button is pushed
        // so that dark edges cover light edges at the corners
        if (!owningButton.isEnabled()) {
            // draw disabled look
            colors = StoneLook.disabledButtonLightColors();
            drawTopEdge(g, x, y, width, height, colors);
            drawLeftEdge(g, x, y, width, height, colors);

            colors = StoneLook.disabledButtonDarkColors();
            drawBottomEdge(g, x, y, width, height, colors);
            drawRightEdge(g, x, y, width, height, colors);                        
        }
        else if (!owningButton.state()) {
            // draw raised look
            colors = buttonLightColors();
            drawTopEdge(g, x, y, width, height, colors);
            drawLeftEdge(g, x, y, width, height, colors);

            colors = buttonDarkColors();
            drawBottomEdge(g, x, y, width, height, colors);
            drawRightEdge(g, x, y, width, height, colors);            
        }
        else {
            // draw pressed look
            colors = buttonLightColors();
            drawBottomEdge(g, x, y, width, height, colors);
            drawRightEdge(g, x, y, width, height, colors);            
            
            colors = buttonDarkColors();
            drawTopEdge(g, x, y, width, height, colors);
            drawLeftEdge(g, x, y, width, height, colors);

        }
    }

    /**
     * Draws colored shading on top of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected void drawTopEdge(Graphics g, 
        int x, int y, int width, int height, Color colors[]) {
        
        for (int i = 0; i < colors.length; i += 1) {
            g.setColor(colors[i]);
            g.drawLine(x, y + i, x + width - 1, y + i);
        }
    }

    /**
     * Draws colored shading on left of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected void drawLeftEdge(Graphics g,
        int x, int y, int width, int height, Color colors[]) {

        for (int i = 0; i < colors.length; i += 1) {
            g.setColor(colors[i]);
            g.drawLine(x + i, y, x + i, y + height - 1);
        }
                                
    }

    /**
     * Draws colored shading on right of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected void drawRightEdge(Graphics g,
        int x, int y, int width, int height, Color colors[]) {
                                
            for (int i = 0; i < colors.length; i += 1) {
            int inset = Math.max(i, 1);
            g.setColor(colors[i]);
            g.drawLine(x + width - i - 1, y,
                       x + width - i - 1, y + height - inset);
        }
    }

    /**
     * Draws colored shading on bottom of button. Called by drawInRect.
     * @see #drawInRect
     */
    protected void drawBottomEdge(Graphics g,
        int x, int y, int width, int height, Color colors[]) {
                                
        for (int i = 0; i < colors.length; i += 1) {
            g.setColor(colors[i]);
            g.drawLine(x, y + height - i - 1,
                x + width - 1, y + height - i - 1);
        }
    }

    /** Overridden to compute border size properly */
    public int bottomMargin() {
        return 3;//buttonDarkColors().length + StoneLook.buttonBackgroundInset();
    }

    /** Overridden to compute border size properly */
    public int leftMargin() {
        return 3;//buttonLightColors().length + StoneLook.buttonBackgroundInset();
    }

    /** Overridden to compute border size properly */
    public int rightMargin() {
        return 3;//buttonDarkColors().length + StoneLook.buttonBackgroundInset();
    }

    /** Overridden to compute border size properly */
    public int topMargin() {
        return 3;//buttonLightColors().length + StoneLook.buttonBackgroundInset();
    }

    //
    // private methods
    //

    /** 
     * returns array of colors used to draw dark sides of button
     */
    private Color[] buttonDarkColors() {
        return owningButton.isDefault()
            ? StoneLook.defaultButtonDarkColors()
            : StoneLook.buttonDarkColors();
    }

    /** 
     * returns array of colors used to light sides of button
     */
    private Color[] buttonLightColors() {
        return owningButton.isDefault()
            ? StoneLook.defaultButtonLightColors()
            : StoneLook.buttonLightColors();
    }
}

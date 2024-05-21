/**
 * SLSlider.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */
package ec.ifc.stonelook;

import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.Rect;
import netscape.application.Scrollable;
import netscape.application.Slider;

import ec.ifc.app.ECSlider;

/**
 * Subclass of ECSlider that applies the stone-texture look.
 */
public class SLSlider extends ECSlider {

    /** default groove length (height if horizontal, width if vertical) */
    private static final int GROOVE_DEFAULT_SIZE = 20;

    /** distance from button to beginning of track's groove */
    private static final int GROOVE_INSET = 9;

    /** thickness of groove drawn in center of scroll track, inc. shading */
    private static final int GROOVE_THICKNESS = 5;

    /** default width of the knob */
    private static final int  SLIDER_KNOB_SIZE = GROOVE_DEFAULT_SIZE - 2;

    /** width (if vertical) or height (if horizontal) of the knob */
    private int myKnobSize = SLIDER_KNOB_SIZE;

    public SLSlider(int x, int y, int length, int orientation) {
        super(x, y, 
            orientation == Scrollable.HORIZONTAL ? length : GROOVE_DEFAULT_SIZE, 
            orientation == Scrollable.VERTICAL ? length : GROOVE_DEFAULT_SIZE,
            orientation);
        setGrooveHeight(GROOVE_DEFAULT_SIZE);
    }

    /**
     * Returns default dimension of the slider 
     */
    public static final int defaultSliderDimension() {
        return GROOVE_DEFAULT_SIZE;
    }

    /** 
     * Overridden to draw the groove differently. 
     */
    public void drawViewGroove(Graphics g) {
        Rect    areaRect;
        int     grooveSize = grooveHeight();

        if (myIsVertical) {
            areaRect = new Rect((bounds.width - grooveSize) / 2, 0,
                                grooveSize, bounds.height);
        }
        else {
            areaRect = new Rect(0, (bounds.height - grooveSize) / 2,
                                bounds.width, grooveSize);
        }

        boolean isEnabled = isEnabled();
        
        // frame slider background
        Color lineColor, insideColor, highlightColor;
        if (isEnabled) {
            lineColor = StoneLook.scrollBarBorderLineColor();
            insideColor = StoneLook.scrollBarBorderInsideColor();
            highlightColor = StoneLook.scrollBarBorderHighlightColor();
        } else {
            lineColor = StoneLook.disabledScrollBarColor();
            insideColor = lineColor;
            highlightColor = null;
        }
        SLBezelBorder.drawBezel(g, areaRect.x, areaRect.y, areaRect.width,
                                areaRect.height, false,
                                lineColor, insideColor, highlightColor);
        
        // fill slider background
        int bezelThickness = StoneLook.bezelThickness();
        areaRect.growBy(-bezelThickness, -bezelThickness);
        if (isEnabled) {
            StoneLook.scrollTrackTexture(!myIsVertical).drawTiled(g, areaRect);
        } else {
            g.setColor(StoneLook.disabledScrollBarColor());
            g.fillRect(areaRect);
        }
    }

    /** 
     * Overridden to draw the knob differently. 
     */
    public void drawViewKnob(Graphics g) {
        Rect  knobRect = knobRect();

        if (isEnabled()) {
            SLScrollBar.drawKnobInRect(g, knobRect, !myIsVertical);
        }
        // don't draw knob at all if it's disabled
    }

    /** 
     * Overridden to get the stonelook version (larger than original) of the
     * knob bounds
     */
    public Rect knobRect() {
        Rect knobRect = super.knobRect();
        int  growX, growY;

        if (myIsVertical) {
            growX = (bounds.width - knobRect.width) / 2;
            growY = 0;
        }
        else {
            growX = 0;
            growY = (bounds.height - knobRect.height) / 2;

        }

        knobRect.growBy(growX, growY);
        return knobRect;
    }

    public int knobHeight() {
        return myKnobSize;
    }

    public int knobWidth() {
        return myKnobSize;
    }
}

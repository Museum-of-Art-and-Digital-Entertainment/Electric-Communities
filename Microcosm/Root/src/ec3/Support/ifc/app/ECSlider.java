/**
 * ECSlider.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Dima Nasledov
 *
 */
package ec.ifc.app;

import netscape.application.Border;
import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Scrollable;
import netscape.application.Slider;

/**
 * Subclass of Slider that supports both horizontal and vertical sliders
 */
public class ECSlider extends Slider {
    //
    // protected instance variables
    //
    protected boolean myIsVertical = false;
    protected int     mySliderPosition = 0;

    //
    // private instance variables
    //
    private int     myClickOffset = 0;

    //
    // Constructors
    //
    public ECSlider(int x, int y, int width, int height, int orientation) {
        super(x, y, width, height);
        myIsVertical = orientation == Scrollable.HORIZONTAL ? false : true;
    }

    //
    // public instance methods
    //

    /** Overridden to reposition the Slider's knob when resized. */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        recomputePosition();
        super.didSizeBy(deltaWidth, deltaHeight);
    }


    /** 
     * Override of Slider's method, implementing vertical drawing
     * Draws the Slider's groove, which includes its Border and contents.
     * You never call this method directly, but should override it to
     * implement custom groove drawing.
     */
    public void drawViewGroove(Graphics g) {
        Rect  tmpRect;
        int   grooveSize = grooveHeight();

        if (myIsVertical) {
            tmpRect = new Rect((bounds.width - grooveSize) / 2, 0, grooveSize, bounds.height);
        }
        else {
            tmpRect = new Rect(0, (bounds.height - grooveSize) / 2, bounds.width, grooveSize);
        }

        Border border = border();
        border.drawInRect(g, tmpRect);
        border.computeInteriorRect(tmpRect, tmpRect);

        Image backgroundImage = image();
        if (backgroundImage != null) {
            g.pushState();
            g.setClipRect(tmpRect);
            backgroundImage.drawWithStyle(g, tmpRect, imageDisplayStyle());
            g.popState();
        } 
        else {
            if (!isEnabled()) {
                g.setColor(Color.lightGray);
            } else {
                g.setColor(backgroundColor());
            }

            g.fillRect(tmpRect);
        }

    }

    /** 
     * Overwrite of Slider's method, implementing vertical slider support.
     * Returns a Rect containing the Slider's knob.
     */
    public Rect knobRect() {
        Image   knobImage = knobImage();
        int     x, y;

        if (myIsVertical) {
            if (knobImage != null) {
                x = (bounds.width - knobImage.width()) / 2;
            } 
            else {
                x = (bounds.width - grooveHeight()) / 2 -
                    (knobWidth() - grooveHeight()) / 2;
            }
            
            y = mySliderPosition;
        }
        else {
            if (knobImage != null) {
                y = (bounds.height - knobImage.height()) / 2;
            } 
            else {
                y = (bounds.height - grooveHeight()) / 2 -
                    (knobHeight() - grooveHeight()) / 2;
            }

            x = mySliderPosition;
        }

        return new Rect(x, y, knobWidth(), knobHeight());
    }

    /** 
     * Overridden to implement knob dragging and send for vertical slider
     */
    public boolean mouseDown(MouseEvent event) {
        if (!isEnabled()) {
            return false;
        }

        Rect  knobRect = knobRect();

        if (myIsVertical) {
            if (knobRect.contains(event.x, event.y)) {
                myClickOffset = positionFromPoint(event.y) - mySliderPosition;
            } else {
                myClickOffset = 0;
                moveSliderTo(event.y);
            }
        }
        else {
            if (knobRect.contains(event.x, event.y)) {
                myClickOffset = positionFromPoint(event.x) - mySliderPosition;
            } 
            else {
                myClickOffset = 0;
                moveSliderTo(event.x);
            }
        }
        
        sendCommand();
        return true;
    }

    /** 
     * Overridden to implement knob dragging and send for horizontal slider
     */
    public void mouseDragged(MouseEvent event) {
        if (myIsVertical) {
            moveSliderTo(event.y - myClickOffset);
        }
        else {
            moveSliderTo(event.x - myClickOffset);
        }

        sendCommand();
    }

    /** 
     * Overwritten to implement setting the value of vertical slider
     */
    public void setValue(int aValue) {
        if (aValue < minValue() || aValue > maxValue()) {
            return;
        }
        
        if (value() != aValue) {
            super.setValue(aValue);

            int oldSliderPosition = mySliderPosition;
            recomputePosition();            
            redrawSlider(oldSliderPosition);
        }
    }

    //
    // private instance methods
    //
    private void moveSliderTo(int point) {
        int oldSliderPosition = mySliderPosition;

        mySliderPosition = positionFromPoint(point);

        if (myIsVertical) {
            setValue((int)((((maxValue() - minValue()) * 1.0) /
                (float)(bounds.height - knobHeight())) * mySliderPosition) + minValue());
        }
        else {
            setValue((int)((((maxValue() - minValue()) * 1.0) /
                (float)(bounds.width - knobWidth())) * mySliderPosition) + minValue());
        }

        redrawSlider(oldSliderPosition);
    }

    private int positionFromPoint(int point) {
        if (myIsVertical) {
            int     knobHeight = knobHeight();

            point -= knobHeight / 2;

            if (point < 1) {
                point = 1;
            } 
            else if (point > bounds.height - knobHeight - 1) {
                point = bounds.height - knobHeight - 1;
            }
        }
        else {
            int     knobWidth = knobWidth();

            point -= knobWidth / 2;

            if (point < 1) {
                point = 1;
            } 
            else if (point > bounds.width - knobWidth - 1) {
                point = bounds.width - knobWidth - 1;
            }
        }

        return point;
    }

    /** Computes the normalized position of the slider relative to its
      * physical size and its numerical value.  Called by setValue() and
      * didSizeBy().
      */
    private void recomputePosition() {
        int normalizedValue = value() - minValue();
        float normalizedMaxValue = (maxValue() - minValue()) * 1.0f;

        if (myIsVertical) {
            mySliderPosition = (int)((normalizedValue / normalizedMaxValue) *
                (bounds.height - knobHeight()));
        }
        else {
            mySliderPosition = (int)((normalizedValue / normalizedMaxValue) *
                (bounds.width - knobWidth()));
        }
    }

    private void redrawSlider(int oldSliderPosition) {
        Rect    tmpRect;

        if (mySliderPosition == oldSliderPosition) {
            return;
        }

        if (myIsVertical) {
            if (mySliderPosition < oldSliderPosition) {
                tmpRect = new Rect(0, mySliderPosition,
                    bounds.width, oldSliderPosition - mySliderPosition + knobHeight());
            } 
            else {
                tmpRect = new Rect(0, oldSliderPosition,
                    bounds.width, mySliderPosition - oldSliderPosition + knobHeight());
            }
        }
        else {
            if (mySliderPosition < oldSliderPosition) {
                tmpRect = new Rect(mySliderPosition, 0,
                    oldSliderPosition - mySliderPosition + knobWidth(), bounds.height);
            } else {
                tmpRect = new Rect(oldSliderPosition, 0,
                    mySliderPosition - oldSliderPosition + knobWidth(), bounds.height);
            }
        }

        draw(tmpRect);
    }
}

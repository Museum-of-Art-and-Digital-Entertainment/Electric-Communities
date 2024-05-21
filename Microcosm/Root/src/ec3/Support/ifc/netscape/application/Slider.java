// Slider.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;


/** View subclass that implements a "slider," an analog control device that
  * allows the user to drag a knob to select the Slider's value. As the
  * Slider changes its value, it sends a command to its Target.
  * @note 1.0 removed Component
  * @note 1.0 added keyboard ui, archiving changed
  * @note 1.0 didSizeBy() will redraw knob appropriately positioned
  * @note 1.0 Added FormElement interface for browser needs
  */
public class Slider extends View implements Target, FormElement {
    Target      target;
    Border      border = BezelBorder.loweredBezel();
    Image       backgroundImage, knobImage;
    Color       backgroundColor = Color.gray;
    String      command;
    int         value, minValue, maxValue, sliderX, knobHeight,
                grooveHeight, clickOffset, imageDisplayStyle;
    boolean     enabled = true;
    int         incrementResolution;
    static Vector    _fieldDescription;

    private static final int  SLIDER_KNOB_WIDTH = 6;

    static final String         TARGET_KEY = "target",
                                BACKGROUND_IMAGE_KEY = "backgroundImage",
                                KNOB_IMAGE_KEY = "knobImage",
                                BACKGROUNDC_KEY = "backgroundColor",
                                COMMAND_KEY = "command",
                                VALUE_KEY = "value",
                                MINVALUE_KEY = "minValue",
                                MAXVALUE_KEY = "maxValue",
                                KNOB_HEIGHT_KEY = "knobHeight",
                                GROOVE_HEIGHT_KEY = "grooveHeight",
                                BORDER_KEY = "border",
                                ENABLED_KEY = "enabled",
                                IMAGEDISP_KEY = "imageDisplayStyle",
                                INC_RES_KEY = "incrementResolution";

    /** Increase the slider value
      *
      */
    public static final String INCREASE_VALUE = "increaseValue";

    /** Decrease the slider value
      *
      */
    public static final String DECREASE_VALUE = "decreaseValue";


    /* constructors */

    /** Constructs a Slider with origin (<b>0</b>, <b>0</b>) and zero
      * width and height.
      */
    public Slider() {
        this(0, 0, 0, 0);
    }

    /** Constructs a Slider with bounds <B>rect</B>.
      */
    public Slider(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a Slider with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public Slider(int x, int y, int width, int height) {
        super(x, y, width, height);

        knobHeight = 13;
        grooveHeight = 8;

        minValue = 0;
        maxValue = 255;

        setValue(0);
        _setupKeyboard();
        incrementResolution = 20;
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

/* attributes/actions */

    /** Overridden to return the Slider's minimum size.
      */
    public Size minSize() {
        if (_minSize != null) {
            return new Size(_minSize);
        }

        return new Size(knobWidth() + 2,
                (knobHeight > grooveHeight ? knobHeight : grooveHeight));
    }

    /** Sets the Slider's minimum and maximum values.  Modifies the Slider's
      * current value to fit within this new range (if outside of the range).
      */
    public void setLimits(int minValue, int maxValue) {
        int     realValue;

        if (minValue >= maxValue) {
            return;
        }

        this.minValue = minValue;
        this.maxValue = maxValue;

        /* clip */
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }

        /* position knob and redraw */
        realValue = value;
        if (value > 0) {
            value--;
        } else {
            value++;
        }
        setValue(realValue);
    }

    /** Returns the Slider's minimum value.
      * @see #setLimits
      */
    public int minValue() {
        return minValue;
    }

    /** Returns the Slider's maximum value.
      * @see #setLimits
      */
    public int maxValue() {
        return maxValue;
    }

    /** Sets the Image displayed by the Slider as its "knob."  If set,
      * the Slider will display this Image rather than draw a bezeled
      * knob.
      */
    public void setKnobImage(Image anImage) {
        if (anImage == knobImage) {
            return;
        }

        knobImage = anImage;
        if (anImage != null) {
            knobHeight = anImage.height();
        }

        /* reposition knob and redraw */
        setValue(value);
    }

    /** Returns the Image displayed by the Slider as its "knob."
      * @see #setKnobImage
      */
    public Image knobImage() {
        return knobImage;
    }

    /** Sets the Color the Slider displays within its groove if it has no
      * image.
      */
    public void setBackgroundColor(Color aColor) {
        if (backgroundColor != null) {
            backgroundColor = aColor;
        }
    }

    /** Returns the Color the Slider displays within its groove.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return backgroundColor;
    }

    /** Sets the Image the Slider displays within its groove.
      */
    public void setImage(Image anImage) {
        backgroundImage = anImage;
    }

    /** Returns the Image the Slider displays within its groove.
      * @see #setImage
      */
    public Image image() {
        return backgroundImage;
    }

    /** Tells the Slider how to display its Image (<b>Image.CENTERED</b>,
      * <b>Image.TILED</b>, or <b>Image.SCALED</b>).
      * @see #setImage
      */
    public void setImageDisplayStyle(int aStyle) {
        if (aStyle != Image.CENTERED && aStyle != Image.TILED &&
            aStyle != Image.SCALED) {
            throw new InconsistencyException("Unknown image display style: " +
                aStyle);
        }

        imageDisplayStyle = aStyle;
        draw();
    }

    /** Returns the style the Slider uses to display its Image.
      * @see #setImageDisplayStyle
      */
    public int imageDisplayStyle() {
        return imageDisplayStyle;
    }

    /** Sets the Slider's Target, the object the Slider notifies when the
      * user changes its value.
      * @see Target
      */
    public void setTarget(Target aTarget) {
        target = aTarget;
    }

    /** Returns the Slider's Target.
      * @see #setTarget
      */
    public Target target() {
        return target;
    }

    /** Sets the command the Slider sends to its Target.
      * @see #setTarget
      */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Returns the command the Slider sends to its Target.
      * @see #setCommand
      */
    public String command() {
        return command;
    }

    /** Called by the Slider to send its command to its Target.
      * @see #setTarget
      */
    public void sendCommand() {
        if (target != null) {
            target.performCommand(command, this);
        }
    }

    /** Sets the Slider's "enabled" state and then calls the Slider's
      * <B>draw()</B> method to redraw it.  A disabled Slider does not
      * respond to mouse clicks or drags.
      */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;

            draw();
        }
    }

    /** Returns <B>true</b> is the Slider is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
    }

    /** Returns the width of the Slider's knob.  Override when subclassing
      * if you want to draw a special knob.
      */
    public int knobWidth() {
        if (knobImage != null) {
            return knobImage.width();
        }

        return SLIDER_KNOB_WIDTH;
    }

    /** Sets the Slider's knob height.
      */
    public void setKnobHeight(int anInt) {
        if (anInt > 0) {
            knobHeight = anInt;
        }
    }

    /** Returns the Slider's knob height.
      * @see #setKnobHeight
      */
    public int knobHeight() {
        return knobHeight;
    }

    /** Sets the Slider's groove height.
      */
    public void setGrooveHeight(int anInt) {
        if (anInt > 0) {
            grooveHeight = anInt;
        }
    }

    /** Returns the Slider's groove height.
      * @see #setKnobHeight
      */
    public int grooveHeight() {
        return grooveHeight;
    }

    /** Sets the Border the Slider draws around its groove.
      */
    public void setBorder(Border newBorder) {
        if (newBorder == null)
            newBorder = EmptyBorder.emptyBorder();

        border = newBorder;
        setValue(value);
    }

    /** Returns the Border the Slider draws around its groove.
      * @see #setBorder
      */
    public Border border() {
        return border;
    }

    void redrawView(int oldSliderX) {
        Rect    tmpRect;

        if (sliderX == oldSliderX) {
            return;
        }

        if (sliderX < oldSliderX) {
            tmpRect = Rect.newRect(sliderX, 0,
                                   oldSliderX - sliderX + knobWidth(),
                                   bounds.height);
        } else {
            tmpRect = Rect.newRect(oldSliderX, 0,
                                   sliderX - oldSliderX + knobWidth(),
                                   bounds.height);
        }

        draw(tmpRect);

        Rect.returnRect(tmpRect);
    }

    /** Computes the normalized position of the slider relative to its
      * physical size and its numerical value.  Called by setValue() and
      * didSizeBy().
      */
    void recomputeSliderPosition() {
        float   normalizedMaxValue;
        int     normalizedValue;

        normalizedValue = value - minValue;
        normalizedMaxValue = (maxValue - minValue) * 1.0f;
        sliderX = (int)((normalizedValue/ normalizedMaxValue) *
                                          (bounds.width - knobWidth()));
    }

    /** Sets the Slider's value and redraws the Slider.  If less than
      * <b>minValue()</b> or greater than <b>maxValue()</b>, does nothing.
      */
    public void setValue(int aValue) {
        Rect    updateRect;
        float   normalizedMaxValue;
        int     oldX, normalizedValue;

        if (aValue < minValue || aValue > maxValue) {
            return;
        }

        if (value != aValue) {
            value = aValue;
            oldX = sliderX;
            recomputeSliderPosition();
            redrawView(oldX);
        }
    }

    /** Returns the Slider's value.
      * @see #setValue
      */
    public int value() {
        return value;
    }

    /** Draws the Slider's groove, which includes its Border and contents.
      * You never call this method directly, but should override it to
      * implement custom groove drawing.
      */
    public void drawViewGroove(Graphics g) {
        Rect    tmpRect;
        int     yOffset;

        yOffset = (bounds.height - grooveHeight) / 2;
        tmpRect = Rect.newRect(0, yOffset, bounds.width, grooveHeight);

        border.drawInRect(g, tmpRect);
        border.computeInteriorRect(tmpRect, tmpRect);

        if (backgroundImage != null) {
            g.pushState();
            g.setClipRect(tmpRect);
            backgroundImage.drawWithStyle(g, tmpRect, imageDisplayStyle);
            g.popState();
        } else {
            if (!enabled) {
                g.setColor(Color.lightGray);
            } else {
                g.setColor(backgroundColor);
            }

            g.fillRect(tmpRect);
        }

        Rect.returnRect(tmpRect);
    }

    /** Returns a Rect containing the Slider's knob.
      */
    public Rect knobRect() {
        int     y;

        if (knobImage != null) {
            y = (bounds.height - knobImage.height()) / 2;
        } else {
            y = (bounds.height - grooveHeight) / 2 -
                (knobHeight - grooveHeight) / 2;
        }
        return Rect.newRect(sliderX, y, knobWidth(), knobHeight);
    }

    /** Draws the Slider's knob.  You never call this method directly, but
      * should override it to implement custom knob drawing.
      */
    public void drawViewKnob(Graphics g) {
        Rect    knobRect;

        knobRect = knobRect();
        if (knobImage != null) {
            knobImage.drawAt(g, knobRect.x, knobRect.y);
        } else {
            BezelBorder.raisedButtonBezel().drawInRect(g, knobRect);
            g.setColor(Color.lightGray);
            g.fillRect(knobRect.x + 2, knobRect.y + 2, knobRect.width - 4,
                       knobRect.height - 4);
        }
        Rect.returnRect(knobRect);
    }

    /** Draws the Slider.  Calls <b>drawViewGroove()</b> and
      * <b>drawViewKnob()</b>.
      * @see #drawViewGroove
      * @see #drawViewKnob
      */
    public void drawView(Graphics g) {
        drawViewGroove(g);
        drawViewKnob(g);
    }

    int _positionFromPoint(int point) {
        int     knobWidth;

        knobWidth = knobWidth();
        point -= knobWidth / 2;

        if (point < 0) {
            point = 0;
        } else if (point > bounds.width - knobWidth) {
            point = bounds.width - knobWidth;
        }

        return point;
    }

    void _moveSliderTo(int point) {
        int     oldX;

        oldX = sliderX;

        sliderX = _positionFromPoint(point);
        value = (int)((((maxValue - minValue) * 1.0) /
                            (float)(bounds.width - knobWidth())) * sliderX) +
                                                                    minValue;

        redrawView(oldX);
    }

    /** Overridden to reposition the Slider's knob when resized.
      */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        recomputeSliderPosition();
        super.didSizeBy(deltaWidth, deltaHeight);
    }

    /** Overridden to implement knob dragging and send the Slider's command
      * to its Target.
      * @see #sendCommand
      */
    public boolean mouseDown(MouseEvent event) {
        Rect    knobRect;

        if (!enabled) {
            return false;
        }

        knobRect = knobRect();
        if (knobRect.contains(event.x, event.y)) {
            clickOffset = _positionFromPoint(event.x) - sliderX;
        } else {
            clickOffset = 0;
            _moveSliderTo(event.x);
        }

        sendCommand();

        return true;
    }

    /** Overridden to implement knob dragging and send the Slider's command
      * to its Target.
      * @see #sendCommand
      */
    public void mouseDragged(MouseEvent event) {
        _moveSliderTo(event.x - clickOffset);

        sendCommand();
    }



/* archiving */


    /** Describes the Slider class' information.
     * @see Codable#describeClassInfo
     */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.Slider", 2);
        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(BACKGROUND_IMAGE_KEY, OBJECT_TYPE);
        info.addField(KNOB_IMAGE_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDC_KEY, OBJECT_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(COMMAND_KEY, STRING_TYPE);
        info.addField(VALUE_KEY, INT_TYPE);
        info.addField(MINVALUE_KEY, INT_TYPE);
        info.addField(MAXVALUE_KEY, INT_TYPE);
        info.addField(KNOB_HEIGHT_KEY, INT_TYPE);
        info.addField(GROOVE_HEIGHT_KEY, INT_TYPE);
        info.addField(IMAGEDISP_KEY, INT_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(INC_RES_KEY,INT_TYPE);
    }

    /** Encodes the Slider instance.
     * @see Codable#encode
     */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(TARGET_KEY, (Codable)target);
        encoder.encodeObject(BACKGROUND_IMAGE_KEY, backgroundImage);
        encoder.encodeObject(KNOB_IMAGE_KEY, knobImage);
        encoder.encodeObject(BACKGROUNDC_KEY, backgroundColor);

        encoder.encodeString(COMMAND_KEY, command);

        encoder.encodeInt(VALUE_KEY, value);
        encoder.encodeInt(MINVALUE_KEY, minValue);
        encoder.encodeInt(MAXVALUE_KEY, maxValue);
        encoder.encodeInt(KNOB_HEIGHT_KEY, knobHeight);
        encoder.encodeInt(GROOVE_HEIGHT_KEY, grooveHeight);

        if (border instanceof EmptyBorder)
            encoder.encodeObject(BORDER_KEY, null);
        else
            encoder.encodeObject(BORDER_KEY, border);

        encoder.encodeBoolean(ENABLED_KEY, enabled);
        encoder.encodeInt(IMAGEDISP_KEY, imageDisplayStyle);
        encoder.encodeInt(INC_RES_KEY,incrementResolution);
    }

    /** Decodes the Slider instance.
     * @see Codable#decode
     */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.Slider");
        super.decode(decoder);

        target = (Target)decoder.decodeObject(TARGET_KEY);
        backgroundImage = (Image)decoder.decodeObject(BACKGROUND_IMAGE_KEY);
        knobImage = (Image)decoder.decodeObject(KNOB_IMAGE_KEY);
        backgroundColor = (Color)decoder.decodeObject(BACKGROUNDC_KEY);

        command = decoder.decodeString(COMMAND_KEY);

        value = decoder.decodeInt(VALUE_KEY);
        minValue = decoder.decodeInt(MINVALUE_KEY);
        maxValue = decoder.decodeInt(MAXVALUE_KEY);
        knobHeight = decoder.decodeInt(KNOB_HEIGHT_KEY);
        grooveHeight = decoder.decodeInt(GROOVE_HEIGHT_KEY);

        setBorder((Border)decoder.decodeObject(BORDER_KEY));

        enabled = decoder.decodeBoolean(ENABLED_KEY);
        imageDisplayStyle = decoder.decodeInt(IMAGEDISP_KEY);
        if(version > 1)
            incrementResolution = decoder.decodeInt(INC_RES_KEY);
        else
            incrementResolution = 20;
    }

    void _setupKeyboard() {
        removeAllCommandsForKeys();
        setCommandForKey(INCREASE_VALUE,KeyEvent.RIGHT_ARROW_KEY,View.WHEN_SELECTED);
        setCommandForKey(DECREASE_VALUE,KeyEvent.LEFT_ARROW_KEY,View.WHEN_SELECTED);
        setCommandForKey(INCREASE_VALUE,'+',View.WHEN_SELECTED);
        setCommandForKey(DECREASE_VALUE,'-',View.WHEN_SELECTED);
    }

    /** Overriden to return <b>true</b>.
      *
      */
    public boolean canBecomeSelectedView() {
        return true;
    }

    /** Overriden to increase / decrease the value **/
    public void performCommand(String command, Object data) {
      int newValue;
      if(INCREASE_VALUE.equals(command)) {
          newValue = value() + (int)Math.rint(((double)(maxValue - minValue) / (double)incrementResolution));
          if(newValue > maxValue)
              newValue = maxValue;
          setValue(newValue);
          sendCommand();
      } else if(DECREASE_VALUE.equals(command)) {
          newValue = value() - (int)Math.rint(((double)(maxValue - minValue) / (double)incrementResolution));
          if(newValue < minValue)
              newValue = minValue;
          setValue(newValue);
          sendCommand();
      }
    }

    /** Return the increment used by keyboard UI
      * The default increment is 20. This means that the user will have
      * to press a key 20 times to go from the minValue to the maxValue.
      * @see #setIncrementResolution
      *
      */
    public int incrementResolution() {
        return incrementResolution;
    }

    /** Set the value increment
      *
      */
    public void setIncrementResolution(int aValue) {
        incrementResolution = aValue;
    }

    /** Implementation of the FormElement interface
      *
      */
    public String formElementText() {
        return Integer.toString(value());
    }
}

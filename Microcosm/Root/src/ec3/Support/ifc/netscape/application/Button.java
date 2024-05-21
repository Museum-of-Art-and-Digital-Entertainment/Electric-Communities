// Button.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass that implementing a "button" control, an object with two
  * states: "on" and "off."  The user changes a Button's state by clicking
  * it, which may also cause the Button to draw itself differently.  For
  * example, when clicked most Buttons draw themselves to appear "pressed" in.
  * The Button class supports several types of Button, from PUSH_TYPE to
  * TOGGLE_TYPE to RADIO_TYPE to CONTINUOUS_TYPE.  See the <b>setType()</b>
  * method documentation for more information.
  * @see #setType
  * @note 1.0 removed implementation of Component interface
  * @note 1.0 Changes for Keyboard UI
  * @note 1.0 double click() radio button will no longer deselect them
  * @note 1.0 archiving raised/lowered bezel
  * @note 1.0 mouseDown now calls containsPoint() before processing Event
  * @note 1.0 setHighlighted() and isHighlighted() now protected methods
  * @note 1.0 Added FormElement interface for browser needs
  */
public class Button extends View implements Target, DrawingSequenceOwner,
                                                FormElement {
    Font                _titleFont;
    Color               _titleColor, _disabledTitleColor, _raisedColor,
                        _loweredColor;
    Image               _image, _altImage;
    Border              _raisedBorder, _loweredBorder;
    Sound               _downSound, _upSound;
    Timer               _actionTimer;
    Target              _target;
    String              _title = "", _command, _altTitle = "";
    int                 _type, _imagePosition, _repeatDelay = 75;
    boolean             _state, _enabled = true, _bordered = true,
                        _highlighted, _oldState, transparent = false;
    private int         _clickCount;
    private boolean     _performingAction;

    static Vector    _fieldDescription;

    /** Push button type. */
    public final static int     PUSH_TYPE = 0;
    /** Toggle button type. */
    public final static int     TOGGLE_TYPE = 1;
    /** Radio button type. */
    public final static int     RADIO_TYPE = 2;
    /** Continuous button type. */
    public final static int     CONTINUOUS_TYPE = 3;

    /** Position Image to the title's left. */
    public final static int     IMAGE_ON_LEFT = 0;
    /** Position Image to the title's right. */
    public final static int     IMAGE_ON_RIGHT = 1;
    /** Position Image above the title. */
    public final static int     IMAGE_ABOVE = 2;
    /** Position Image below the title. */
    public final static int     IMAGE_BELOW = 3;
    /** Draw title on top of Image. */
    public final static int     IMAGE_BENEATH = 4;

    /** Command causing the Button to send its command to its Target. */
    public final static String SEND_COMMAND = "sendCommand";

    /** Command causing the Button to visually "click," as if pressed, as well
      * as send its command to its Target.
      */
    public final static String CLICK = "click";

    /** Command causing a radio button to select the next button.
      *
      */
    public final static String SELECT_NEXT_RADIO_BUTTON = "selectNextRadioButton";

    /** Command causing a radio button to select the previous button
      *
      */
    public final static String SELECT_PREVIOUS_RADIO_BUTTON = "selectPreviousRadioButton";

    // Codable information

    final static String TITLE_KEY = "title";
    final static String ALT_TITLE_KEY = "altTitle";
    final static String TITLE_FONT_KEY = "titleFont";
    final static String TITLE_COLOR_KEY = "titleColor";
    final static String DISABLED_TITLE_COLOR_KEY = "disabledTitleColor";
    final static String RAISED_COLOR_KEY = "raisedColor";
    final static String LOWERED_COLOR_KEY = "loweredColor";
    final static String IMAGE_KEY = "image";
    final static String ALT_IMAGE_KEY = "altImage";
    final static String DOWN_SOUND_KEY = "downSound";
    final static String UP_SOUND_KEY = "upSound";
    final static String TARGET_KEY = "target";
    final static String COMMAND_KEY = "command";
    final static String TYPE_KEY = "type";
    final static String IMAGE_POSITION_KEY = "imagePosition";
    final static String REPEAT_DELAY_KEY = "repeatDelay";
    final static String STATE_KEY = "state";
    final static String ENABLED_KEY = "enabled";
    final static String BORDERED_KEY = "bordered";
    final static String TRANSPARENT_KEY = "transparent";

    final static String RAISED_BORDER_KEY = "raisedBorder";
    final static String LOWERED_BORDER_KEY = "loweredBorder";


/* static methods */

    /** Returns a new Button instance configured to look and behave like a
      * "push" button, a Button of type PUSH_TYPE.
      */
    public static Button createPushButton(int x, int y,
                                          int width, int height) {
        Button  pushButton;

        pushButton = new Button(x, y, width, height);
        pushButton.setType(Button.PUSH_TYPE);

        return pushButton;
    }

    /** Returns a new Button instance configured to look and behave like a
      * "check" button, a Button of type TOGGLE_TYPE that displays a check mark
      * when on.
      */
    public static Button createCheckButton(int x, int y,
                                           int width, int height) {
        Button  checkButton;

        checkButton = new Button(x, y, width, height);

        checkButton.setType(Button.TOGGLE_TYPE);
        checkButton.setTransparent(true);

        checkButton.setImage(new CheckButtonImage(false));
        checkButton.setAltImage(new CheckButtonImage(true));
        checkButton.setImagePosition(Button.IMAGE_ON_LEFT);

        return checkButton;
    }

    /** Returns a new Button instance configured to look and behave like a
      * "radio" button, a Button of type RADIO_TYPE that can only be on if the
      * others in its group are off.
      */
    public static Button createRadioButton(int x, int y,
                                           int width, int height) {
        Button  radioButton;

        radioButton = new Button(x, y, width, height);

        radioButton.setType(Button.RADIO_TYPE);
        radioButton.setImage(Bitmap.bitmapNamed(
                            "netscape/application/RadioButtonOff.gif"));
        radioButton.setAltImage(Bitmap.bitmapNamed(
                            "netscape/application/RadioButtonOn.gif"));
        radioButton.setImagePosition(Button.IMAGE_ON_LEFT);
        radioButton.setTransparent(true);

        return radioButton;
    }

    /* constructors */

    /** Constructs a Button with origin (0, 0) and zero width and height.
      */
    public Button() {
        this(0, 0, 0, 0);
    }

    /** Constructs a Button with bounds <B>rect</B>.
      */
    public Button(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a Button with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public Button(int x, int y, int width, int height) {
        super(x, y, width, height);

        _titleColor = Color.black;
        _disabledTitleColor = Color.gray;
        _titleFont = Font.defaultFont();
        _raisedBorder = BezelBorder.raisedButtonBezel();
        _loweredBorder = BezelBorder.loweredButtonBezel();
        _raisedColor = Color.lightGray;
        _loweredColor = Color.lightGray;

        _setupKeyboard();
    }

    /* attributes */

    /** Sets the Button's title and then calls the Button's
      * <B>draw()</B> method to redraw it.  If <b>aString</b> contains
      * newlines, the title will break and wrap to the next line at those
      * points.  If you don't want to immediately
      * redraw the Button, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setTitle(String aString) {
        if (aString == null) {
            _title = "";
        } else {
            _title = aString;
        }
        draw();
    }

    /** Returns the Button's title.
      * @see #setTitle
      */
    public String title() {
        return _title;
    }

    /** Sets the Button's alternate title, the title the Button displays
      * when it's in its alternate state, and then calls the Button's
      * <B>draw()</B> method to redraw it.  If <b>aString</b> contains
      * newlines, the title will break and wrap to the next line at those
      * points.  If you don't want to immediately redraw the Button, you
      * should first call its <B>disableDrawing()</B> method.
      * @see #setTitle
      * @see View#disableDrawing
      */
    public void setAltTitle(String aString) {
        if (aString == null) {
            _altTitle = "";
        } else {
            _altTitle = aString;
        }
        setDirty(true);
    }

    /** Returns the Button's alternate title.
      * @see #setAltTitle
      */
    public String altTitle() {
        return _altTitle;
    }

    /** Sets the Button's "enabled" state and then calls the Button's
      * <b>draw()</b> method to redraw it.  A disabled Button does not
      * react to mouse clicks.  If you do not want to immediately
      * redraw the Button, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setEnabled(boolean enabled) {
        if (enabled != _enabled) {
            _enabled = enabled;

            setDirty(true);
        }
    }

    /** Returns <b>true</b> if the Button is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return _enabled;
    }

    void _setState(boolean newState) {
        if (newState != _state) {
            _state = newState;
            draw();
//            setDirty(true);
        }
    }

    void selectNextRadioButton(boolean forward) {
        View sv = superview();
        View peer;

        if(sv != null) {
            Vector peers = sv.subviews();
            int index = peers.indexOfIdentical(this);
            int count = peers.count();
            do {
                if(forward){
                    index++;
                    if(index == count)
                        index = 0;
                } else {
                    index--;
                    if(index<0)
                        index = count-1;
                }

                peer = (View)peers.elementAt(index);
                if(peer instanceof Button && ((Button)peer).type() == RADIO_TYPE) {
                    ((Button)peer).setState(true);
                    rootView().selectView(peer,true);
                    break;
                }
            } while(peer != this);
        }
    }

    Button _otherActive() {
        Button          nextPeer;
        View            nextView;
        Vector          peers;
        int             i;

        if (superview() == null) {
            return null;
        }

        peers = superview().peersForSubview(this);
        i = peers.count();
        while (i-- > 0) {
            nextView = (View)peers.elementAt(i);

            if (!(nextView instanceof Button) || nextView == this) {
                continue;
            }

            nextPeer = (Button)nextView;
            if (nextPeer.type() != RADIO_TYPE) {
                continue;
            } else if (nextPeer.isEnabled() && nextPeer.state()) {
                return nextPeer;
            }
        }

        return null;
    }

    /** Sets the Button's state (a boolean value where <b>false</b> is the
      * normal, "up" state and <b>true</b> is the down or "pressed" state)
      * and then calls the Button's
      * <b>draw()</b> method to redraw it.  If you do not want to immediately
      * redraw the Button, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setState(boolean newState) {
        Button     otherActive;
        RootView   rv = rootView();

        if (_type == RADIO_TYPE) {
            if (newState == state()) {
                return;
            }

            if (newState) {
                otherActive = _otherActive();
                if (otherActive != null) {
                    otherActive._setState(false);
                }
            }
        }

        _setState(newState);
    }

    /** Returns the Button's state.
      * @see #setState
      */
    public boolean state() {
        return _state;
    }

    /** Sets the Button's Target, the object that will receive the a
      * <b>performCommand()</b> message when the user clicks the Button.
      */
    public void setTarget(Target aTarget) {
        _target = aTarget;
    }

    /** Returns the Button's Target.
      * @see #setTarget
      */
    public Target target() {
        return _target;
    }

    /** Sets the Button's command.
      * @see #setTarget
      */
    public void setCommand(String command) {
        _command = command;
    }

    /** Returns the Button's command.
      * @see #setCommand
      */
    public String command() {
        return _command;
    }

    /** Returns the Size required to display the Button's Images.
      * @see #setImage
      */
    public Size imageAreaSize() {
        Size    theSize;

        if (_image != null) {
            theSize = new Size(_image.width(), _image.height());
        } else {
            theSize = new Size();
        }
        if (_altImage != null) {
            if (_altImage.width() > theSize.width) {
                theSize.width = _altImage.width();
            }
            if (_altImage.height() > theSize.height) {
                theSize.height = _altImage.height();
            }
        }

/*        if (_upSequence != null) {
            if (_upSequence.width() > theSize.width) {
                theSize.width = _upSequence.width();
            }
            if (_upSequence.height() > theSize.height) {
                theSize.height = _upSequence.height();
            }
        }

        if (_downSequence != null) {
            if (_downSequence.width() > theSize.width) {
                theSize.width = _downSequence.width();
            }
            if (_downSequence.height() > theSize.height) {
                theSize.height = _downSequence.height();
            }
        }*/

        return theSize;
    }

    Size minStringSize(String string) {
        Size    theSize;
        String  titleRemainder;
        int     lineCount, newline, substringWidth;

        newline = string.indexOf('\n');
        if (newline == -1) {
            return _titleFont.fontMetrics().stringSize(string);
        }

        theSize = new Size(0, _titleFont.fontMetrics().stringHeight());
        lineCount = 1;
        titleRemainder = string;
        while (newline != -1) {
            substringWidth = _titleFont.fontMetrics().stringWidth(
                                        titleRemainder.substring(0, newline));
            if (substringWidth > theSize.width) {
                theSize.width = substringWidth;
            }
            lineCount++;
            titleRemainder = titleRemainder.substring(newline + 1);
            newline = titleRemainder.indexOf('\n');
        }
        substringWidth = _titleFont.fontMetrics().stringWidth(titleRemainder);
        if (substringWidth > theSize.width) {
            theSize.width = substringWidth;
        }

        theSize.height *= lineCount;

        return theSize;
    }

    /** Returns the minimum Size required to fully display the Button's
      * Border, title, and Images.
      */
    public Size minSize() {
        Size            theSize = null, altSize = null, imageAreaSize;
        boolean         hasTitle = false, hasImage;

        if (_minSize != null) {
            return new Size(_minSize);
        }

        if (_title != null && _title.length() > 0) {
            theSize = minStringSize(_title);
            hasTitle = true;
        }
        if (_altTitle != null && _altTitle.length() > 0) {
            altSize = minStringSize(_altTitle);
            hasTitle = true;
        }

        if (theSize == null) {
            theSize = altSize;
        } else if (altSize != null) {
            if (theSize.width < altSize.width) {
                theSize.width = altSize.width;
            }
            if (theSize.height < altSize.height) {
                theSize.height = altSize.height;
            }
        }

        if (theSize != null) {
            if (theSize.width > 0) {
                theSize.sizeBy(3, 0);
            }
        } else {
            theSize = new Size();
        }

        imageAreaSize = imageAreaSize();
        hasImage = (imageAreaSize.width > 0 || imageAreaSize.height > 0);

        if (hasImage) {
            if (_imagePosition == IMAGE_ABOVE ||
                _imagePosition == IMAGE_BELOW) {
                if (theSize.width < imageAreaSize.width) {
                    theSize.width = imageAreaSize.width;
                }
                theSize.height += imageAreaSize.height + 2;
            } else if (hasTitle && _imagePosition != IMAGE_BENEATH) {
                theSize.sizeBy(imageAreaSize.width + 2, 0);

                if (imageAreaSize.height > theSize.height) {
                    theSize.height = imageAreaSize.height;
                }
            } else {
                if (imageAreaSize.width > theSize.width) {
                    theSize.width = imageAreaSize.width;
                }
                if (imageAreaSize.height > theSize.height) {
                    theSize.height = imageAreaSize.height;
                }
            }
        }

        if (_bordered) {
            theSize.sizeBy(3, 3);
        }

        return theSize;
    }

    /** Sets the Font used to display the Button's title.
      * @see #setTitle
      */
    public void setFont(Font aFont) {
        if (aFont == null) {
            _titleFont = Font.defaultFont();
        } else {
            _titleFont = aFont;
        }
    }

    /** Returns the Font used to display the Button's title.
      * @see #setFont
      */
    public Font font() {
        return _titleFont;
    }

    /** Sets the Color used to draw the Button's title.
      * @see #setTitle
      */
    public void setTitleColor(Color aColor) {
        if (aColor == null) {
            _titleColor = Color.black;
        } else {
            _titleColor = aColor;
        }
    }

    /** Returns the Color used to draw the Button's title.
      * @see #setTitleColor
      */
    public Color titleColor() {
        return _titleColor;
    }

    /** Sets the Color used to draw the Button's title when disabled.
      * @see #setTitle
      */
    public void setDisabledTitleColor(Color aColor) {
        if (aColor == null) {
            _disabledTitleColor = Color.gray;
        } else {
            _disabledTitleColor = aColor;
        }
    }

    /** Returns the Color used to draw the Button's title when disabled.
      * @see #setDisabledTitleColor
      */
    public Color disabledTitleColor() {
        return _disabledTitleColor;
    }

    /** Sets the background Color used to draw the Button in its raised state.
      */
    public void setRaisedColor(Color aColor) {
        if (aColor == null) {
            _raisedColor = Color.lightGray;
        } else {
            _raisedColor = aColor;
        }
    }

    /** Returns the background Color used to draw the Button in its raised
      * state.
      * @see #setRaisedColor
      */
    public Color raisedColor() {
        return _raisedColor;
    }

    /** Sets the background Color used to draw the Button in its lowered state.
      */
    public void setLoweredColor(Color aColor) {
        if (aColor == null) {
            _loweredColor = Color.lightGray;
        } else {
            _loweredColor = aColor;
        }
    }

    /** Returns the background Color used to draw the Button in its lowered
      * state.
      * @see #setLoweredColor
      */
    public Color loweredColor() {
        return _loweredColor;
    }

    /** Sets the Image displayed by the Button.  If <b>anImage</b> is a
      * DrawingSequence, sets the Button to be the DrawingSequence's owner and
      * starts the sequence when the user releases the Button (after a press).
      * @see DrawingSequence
      * @see #setAltImage
      */
    public void setImage(Image anImage) {
        DrawingSequence         sequence;

        _image = anImage;

        if (anImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)anImage;
            sequence.setOwner(this);
        }
    }

    /** Returns the Image displayed by the Button.
      * @see #setImage
      */
    public Image image() {
        return _image;
    }

    /** Sets the Image the button displays when pressed. If <b>anImage</b>
      * is a DrawingSequence, sets the Button to be the DrawingSequence's owner
      * and starts the sequence when the user presses the Button.
      * @see DrawingSequence
      */
    public void setAltImage(Image anImage) {
        DrawingSequence         sequence;

        _altImage = anImage;

        if (anImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)anImage;
            sequence.setOwner(this);
        }
    }

    /** Returns the Image the Button displays when pressed.
      * @see #setAltImage
      */
    public Image altImage() {
        return _altImage;
    }

    /** Sets the raised Border, the Border the Button draws around its
      * perimeter when in the "up" state.
      */
    public void setRaisedBorder(Border border) {
        _raisedBorder = border;
    }

    /** Returns the Button's raised Border.
      * @see #setRaisedBorder
      */
    public Border raisedBorder() {
        return _raisedBorder;
    }

    /** Sets the lowered Border, the Border the Button draws around its
      * perimeter when in the "down" state.
      */
    public void setLoweredBorder(Border border) {
        _loweredBorder = border;
    }

    /** Returns the Button's lowered Border.
      * @see #setLoweredBorder
      */
    public Border loweredBorder() {
        return _loweredBorder;
    }

    /** Sets the Sound the Button plays when pressed.
      */
    public void setMouseDownSound(Sound aSound) {
        _downSound = aSound;
        if (_type == CONTINUOUS_TYPE && _downSound != null) {
            _downSound.setLoops(true);
        }
    }

    /** Returns the Sound the Button plays when pressed.
      * @see #setMouseDownSound
      */
    public Sound mouseDownSound() {
        return _downSound;
    }

    /** Sets the Sound the Button plays when released, after being pressed.
      */
    public void setMouseUpSound(Sound aSound) {
        _upSound = aSound;
    }

    /** Returns the Sound the Button plays when released.
      * @see #setMouseUpSound
      */
    public Sound mouseUpSound() {
        return _upSound;
    }

    /** Sets whether the Button draws its Border.
      */
    public void setBordered(boolean flag) {
        _bordered = flag;
        if (_bordered) {
            setTransparent(false);
        }
    }

    /** Returns <b>true</b> if the Button draws its Border.
      * @see #setBordered
      */
    public boolean isBordered() {
        return _bordered;
    }

/*    public void setBackgroundCanChange(boolean flag) {
        _backgroundCanChange = flag;
    }

    public boolean backgroundCanChange() {
        return _backgroundCanChange;
    }*/

    /** Sets the Button's type, which can be one of the following:
      * <OL><LI>A PUSH_TYPE Button appears to press in when clicked and sends
      * its command to its Target when released, returning to its original
      * "up" state.  <LI>A TOGGLE_TYPE Button
      * changes its state between off and on and sends its command to its
      * Target on each click.  <LI>A RADIO_TYPE button behaves like a
      * TOGGLE_TYPE button, except when clicked it turns off any other
      * RADIO_TYPE buttons with the same superview.
      * <LI>A CONTINUOUS_TYPE Button behaves like a PUSH_TYPE
      * Button, except that it sends its command to its Target while pressed,
      * every <b>repeatDelay()</b> milliseconds. </OL>
      * @see #setRepeatDelay
      */
    public void setType(int buttonType) {
        if (buttonType < 0 || buttonType > CONTINUOUS_TYPE) {
            throw new InconsistencyException(
                                    "Invalid Button type: " + buttonType);
        }

        _type = buttonType;

        setState(false);
        if (_type == CONTINUOUS_TYPE) {
            if (_downSound != null) {
                _downSound.setLoops(true);
            }
        } else {
            if (_downSound != null) {
                _downSound.setLoops(false);
            }
        }
        _setupKeyboard();
    }

    /** Returns the Button's type.
      * @see #setType
      */
    public int type() {
        return _type;
    }

    /** Sets the Button's repeat delay, the time delay a CONTINUOUS_TYPE Button
      * uses when sending its command to its Target while being pressed.
      * @see #setType
      */
    public void setRepeatDelay(int milliseconds) {
        if (milliseconds > 0) {
            _repeatDelay = milliseconds;
            if (_actionTimer != null) {
                _actionTimer.setDelay(_repeatDelay);
            }
        }
    }

    /** Returns the Button's repeat delay.
      * @see #setRepeatDelay
      */
    public int repeatDelay() {
        return _repeatDelay;
    }

    /** Sets the relationship between the Button's Images and its title.
      * Options are IMAGE_ON_LEFT, IMAGE_ON_RIGHT, IMAGE_ABOVE,
      * IMAGE_BELOW, and IMAGE_BENEATH.
      * @see #setImage
      * @see #setTitle
      */
    public void setImagePosition(int aPosition) {
        if (aPosition < 0 || aPosition > IMAGE_BENEATH) {
            return;
        }
        _imagePosition = aPosition;
    }

    /** Returns the integer describing the relationship between the Button's
      * Images and title.
      * @see #setImagePosition
      */
    public int imagePosition() {
        return _imagePosition;
    }

    /** Overridden to configure the Button not to draw its Border, if
      * configured to be transparent.  A transparent Button does not display a
      * Border (unless subsequently configured to do so) or a background color,
      * only its title and Images, if any.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
        if (transparent) {
            _bordered = false;
        }
    }

    /** Overridden to return <b>true</b> if the Button is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Overridden to support RADIO_TYPE buttons.
      */
    protected void ancestorWasAddedToViewHierarchy(View aView) {
        super.ancestorWasAddedToViewHierarchy(aView);

        if ((_type == RADIO_TYPE) && _state) {
            _state = false;
            setState(true);
        }
    }


/* drawing */

    /** Sets the appropriate Color (according to whether or not the Button's
      * enabled) and calls Graphics' <b>drawStringInRect()</b> to draw the
      * Button's title within <b>textBounds</b> using the justification
      * <b>justification</b>.  You never call this method directly, but should
      * override to produce any custom title drawing.
      */
    public void drawViewTitleInRect(Graphics g, String title,
                                    Font titleFont, Rect textBounds,
                                    int justification) {
        Rect    tmpRect;
        String  titleRemainder;
        int     newline;

        if (title == null || title.length() == 0) {
            return;
        }

        if (_enabled) {
            g.setColor(_titleColor);
        } else {
            g.setColor(_disabledTitleColor);
        }

        g.setFont(titleFont);
//      g.drawStringInRect(title, textBounds, justification);

        /* this code allows Buttons to draw multiple lines, separated by
         * newlines
         */
        newline = title.indexOf('\n');
        if (newline == -1) {
            g.drawStringInRect(title, textBounds, justification);
            return;
        }

        tmpRect = new Rect(textBounds);
        tmpRect.height = _titleFont.fontMetrics().stringHeight();
        titleRemainder = title;
        while (newline != -1) {
            g.drawStringInRect(titleRemainder.substring(0, newline),
                               tmpRect, justification);
            tmpRect.y += tmpRect.height;
            titleRemainder = titleRemainder.substring(newline + 1);
            newline = titleRemainder.indexOf('\n');
        }
        g.drawStringInRect(titleRemainder, tmpRect, justification);
    }

    /** Draws the Button's interior. <b>title</b> is the appropriate String
      * and <b>image</b> is the appropriate Image to be drawn in the Button's
      * current state. <b>interiorRect</b> is the Rect defining the Button's
      * interior (the entire Button without its border). Calls
      * <b>drawViewTitleInRect()</b> to draw the Button's title.  You never
      * call this method directly, but should override to implement any custom
      * drawing.
      */
    public void drawViewInterior(Graphics g, String title, Image image,
                                 Rect interiorRect) {
        Size            imageAreaSize, stringSize;
        int             x, charHeight, justification;
        boolean         center;

        imageAreaSize = imageAreaSize();

        if (_imagePosition == IMAGE_ON_LEFT) {
            if (title == null || title.length() == 0) {
                x = interiorRect.x + 1 +
                    (interiorRect.width - imageAreaSize.width - 2) / 2;
            } else {
                x = interiorRect.x + 1;
            }

            if (image != null) {
                image.drawAt(g, x, interiorRect.y + (interiorRect.height -
                    imageAreaSize.height) / 2);
            }

            /* put some distance between the image and the text */
            if (imageAreaSize.width > 0) {
                interiorRect.moveBy(imageAreaSize.width + 3, 0);
                interiorRect.sizeBy(-(imageAreaSize.width + 4), 0);
                justification = Graphics.LEFT_JUSTIFIED;
            } else {
                interiorRect.moveBy(1, 0);
                interiorRect.sizeBy(-2, 0);
                justification = Graphics.CENTERED;
            }

            drawViewTitleInRect(g, title, _titleFont, interiorRect,
                                justification);
        } else if (_imagePosition == IMAGE_ABOVE) {
            if (image != null) {
                image.drawAt(g, interiorRect.x + (interiorRect.width -
                    imageAreaSize.width) / 2, interiorRect.y + 2);
            }

            charHeight = _titleFont.fontMetrics().charHeight();
            interiorRect.setBounds(interiorRect.x + 1,
                                   interiorRect.maxY() - charHeight - 1,
                                   interiorRect.width - 2, charHeight);
            drawViewTitleInRect(g, title, _titleFont, interiorRect,
                                Graphics.CENTERED);
        } else if (_imagePosition == IMAGE_BELOW) {
            if (image != null) {
                image.drawAt(g, interiorRect.x + (interiorRect.width -
                    imageAreaSize.width) / 2,  interiorRect.maxY() -
                    imageAreaSize.height - 2);
            }

            interiorRect.setBounds(interiorRect.x + 1, interiorRect.y + 1,
                                   interiorRect.width - 2,
                                   _titleFont.fontMetrics().charHeight());
            drawViewTitleInRect(g, title, _titleFont, interiorRect,
                                Graphics.CENTERED);
        } else {
            if (image != null && _imagePosition == IMAGE_BENEATH) {
                image.drawAt(g, interiorRect.x +
                                (interiorRect.width - imageAreaSize.width) / 2,
                             interiorRect.y +
                               (interiorRect.height - imageAreaSize.height)/2);
                justification = Graphics.CENTERED;
            } else if (imageAreaSize.width == 0) {
                interiorRect.moveBy(2, 0);
                justification = Graphics.CENTERED;
            } else {
                justification = Graphics.LEFT_JUSTIFIED;
            }

            if (title == null || title.length() == 0) {
                x = interiorRect.x + 1 +
                    (interiorRect.width - imageAreaSize.width - 2) / 2;
            } else {
                x = interiorRect.maxX() - imageAreaSize.width - 1;
            }

            if (image != null && _imagePosition == IMAGE_ON_RIGHT) {
                image.drawAt(g, x, interiorRect.y + (interiorRect.height -
                    imageAreaSize.height) / 2);
            }

            drawViewTitleInRect(g, title, _titleFont, interiorRect,
                                justification);
        }
    }

    /** Draws the Button's background (a Border, or solid rectangle if no
      * Border and opaque).  You never call this method directly, but should
      * override to perform any custom background drawing.  You must modify
      * <b>interiorRect</b> to define the
      * area available to draw the Button's title and/or Image.
      */
    public void drawViewBackground(Graphics g, Rect interiorRect,
                                   boolean drawDownState) {
        if (_bordered) {
            interiorRect.sizeBy(-3, -3);
            if (drawDownState) {
                _loweredBorder.drawInRect(g, 0, 0, bounds.width,
                                          bounds.height);
                g.setColor(_loweredColor);
                g.fillRect(_loweredBorder.leftMargin(),
                           _loweredBorder.topMargin(),
                           bounds.width - _loweredBorder.widthMargin(),
                           bounds.height - _loweredBorder.heightMargin());
                interiorRect.moveBy(2, 2);
            } else {
                _raisedBorder.drawInRect(g, 0, 0, bounds.width,
                                         bounds.height);
                g.setColor(_raisedColor);
                g.fillRect(_raisedBorder.leftMargin(),
                           _raisedBorder.topMargin(),
                           bounds.width - _raisedBorder.widthMargin(),
                           bounds.height - _raisedBorder.heightMargin());
                interiorRect.moveBy(1, 1);
            }
        } else {
            if (!isTransparent()) {
                if (drawDownState) {
                    g.setColor(_loweredColor);
                } else {
                    g.setColor(_raisedColor);
                }
                g.fillRect(0, 0, bounds.width, bounds.height);
            }
        }
    }

    /** Draws the Button.  Calls <b>drawViewBackground()</b> and
      * <b>drawViewInterior()</b>.  You never call this method directly, but
      * may override to implement custom Button drawing.  Use the <b>draw()</b>
      * method to draw the Button.
      * @see View#draw
      */
    public void drawView(Graphics g) {
        Image                   theImage = null;
        DrawingSequence         sequence;
        String                  theTitle;
        Rect                    interiorRect;
        boolean                 drawDownState;

        drawDownState = _highlighted ? !_state : _state;
        //        if ((_image != null /*|| _upSequence != null*/) &&
        //           (_altImage != null /*|| _downSequence != null*/)) {
        //           backgroundCanChange = false;
        //        } else {
        //            backgroundCanChange = true;
        //        }

        /* bezel */
        interiorRect = Rect.newRect(0, 0, bounds.width, bounds.height);
        drawViewBackground(g, interiorRect, drawDownState);

        /* which image should appear - first, should be any running sequence */
        if (_image instanceof DrawingSequence) {
            sequence = (DrawingSequence)_image;
            if (sequence.isAnimating()) {
                theImage = _image;
            } else if (_altImage instanceof DrawingSequence) {
                /* trust me - this is what you want */
                theImage = _altImage;
            }
        } else if (_altImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)_altImage;
            if (sequence.isAnimating()) {
                theImage = _altImage;
            }
        }

        /* no sequences, or running sequences, select an image */
        if (theImage == null) {
            theImage = _image;
            if (drawDownState && _altImage != null) {
                theImage = _altImage;
            }
        }

/*        if (_upSequence != null && _upSequence.isAnimating()) {
            theSequence = _upSequence;
        } else {
            theSequence = _downSequence;
        }*/

        if (drawDownState && _altTitle != null && _altTitle.length() != 0) {
            theTitle = _altTitle;
        } else {
            theTitle = _title;
        }

        /* nothing more to draw? */
        if (theImage == null &&
            (theTitle == null || theTitle.length() == 0)) {
            Rect.returnRect(interiorRect);
            return;
        }

//        if (theSequence != null) {
//            drawViewInterior(g, theSequence, interiorRect);
//        } else {
            drawViewInterior(g, theTitle, theImage, interiorRect);
//        }

        Rect.returnRect(interiorRect);
    }


  /* events */

    Button _activeForPoint(int x, int y) {
        Button          nextPeer;
        View            nextView;
        Point           point;
        Vector          peers;
        int             i;

        if (superview() == null) {
            return null;
        }

        peers = superview().peersForSubview(this);

        point = Point.newPoint();
        i = peers.count();
        while (i-- > 0) {
            nextView = (View)peers.elementAt(i);

            if (!(nextView instanceof Button) || nextView == this) {
                continue;
            }

            nextPeer = (Button)nextView;
            if (nextPeer.type() != RADIO_TYPE) {
                continue;
            }

            convertToView(nextView, x, y, point);

            if (nextPeer.containsPoint(point.x, point.y)) {
                Point.returnPoint(point);
                return nextPeer;
            }
        }

        Point.returnPoint(point);

        return null;
    }

    /** Overridden to interpret Button clicks.  For CONTINUOUS_TYPE
      * Buttons, starts a Timer that sends the Button's command to its
      * Target every <b>repeatDelay()</b> milliseconds.
      * @see #setRepeatDelay
      */
    public boolean mouseDown(MouseEvent event) {
        Button                  otherActive;
        DrawingSequence         sequence;

        if (!_enabled) {
            return false;
        }

        if (!containsPoint(event.x, event.y)) {
            return false;
        }

        if (_type == RADIO_TYPE) {
            otherActive = _otherActive();
            if (otherActive != null) {
                otherActive._setState(false);
            }
            _oldState = _state;
            _state = false;
        }

        _clickCount = event.clickCount;

        if (_type == TOGGLE_TYPE || _type == RADIO_TYPE)
            setHighlighted(true);
        else
            setState(true);

        if (_downSound != null) {
            _downSound.play();
        }

        if (_altImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)_altImage;
            sequence.start();
        }

        if (_type == CONTINUOUS_TYPE && _actionTimer == null) {
            sendCommand();
// ALERT! - Buttons shouldn't set the initial delay behind your back!
            _actionTimer = new Timer(this, "sendCommand", _repeatDelay);
            _actionTimer.setInitialDelay(300);
            _actionTimer.start();
        }

        return true;
    }

    void _buttonDown() {
        DrawingSequence         sequence;

        if (_image instanceof DrawingSequence) {
            sequence = (DrawingSequence)_image;
            if (sequence.doesLoop()) {
                sequence.stop();
            } else {
               /* wait for it to finish */
                while (sequence.isAnimating()) {
// ALERT!!!
//                  Thread.yield();
//                  Application.sleep(5);
                    sequence.stop();
                }
            }
        }

        if (_altImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)_altImage;
            sequence.start();
        }

        if (_type == CONTINUOUS_TYPE) {
            if (_upSound != null) {
                _upSound.stop();
            }
        }
        if (_downSound != null) {
            _downSound.play();
        }
    }

    void _buttonUp() {
        DrawingSequence         sequence;

        if (_altImage instanceof DrawingSequence) {
            sequence = (DrawingSequence)_altImage;
            if (sequence.doesLoop()) {
                sequence.stop();
            } else {
               /* wait for it to finish */
                while (sequence.isAnimating()) {
// ALERT!!!
//                  Thread.yield();
//                  Application.sleep(5);
                    sequence.stop();
                }
            }
        }

        if (_image instanceof DrawingSequence) {
            sequence = (DrawingSequence)_image;
            sequence.start();
        }

        if (_type == CONTINUOUS_TYPE) {
            if (_downSound != null) {
                _downSound.stop();
            }
        }
        if (_upSound != null) {
            _upSound.play();
        }
    }

    /** Overridden to support Button clicks. */
    public void mouseDragged(MouseEvent event) {
        Button          newButton;
        boolean         newState;

        if (!_enabled) {
            return;
        }

        if (_type == RADIO_TYPE && !containsPoint(event.x, event.y)) {
            newButton = _activeForPoint(event.x, event.y);
            if (newButton != null) {
                setHighlighted(false);
                newButton.setHighlighted(true);

                rootView().setMouseView(newButton);
            }
            return;
        }

      /* did the user move the mouse away from the button */
        if (containsPoint(event.x, event.y)) {
            if (!(_state || _highlighted)) {
                _buttonDown();
                if (_type == TOGGLE_TYPE || _type == RADIO_TYPE)
                    setHighlighted(true);
                else
                    setState(true);
                if (_type == CONTINUOUS_TYPE) {
                    sendCommand();
                    _actionTimer = new Timer(this, "sendCommand", 100);
                    _actionTimer.start();
                }
            }
        } else {
            if (_state || _highlighted) {
                _buttonUp();
                if (_type == CONTINUOUS_TYPE) {
                    if (_actionTimer != null) {
                        _actionTimer.stop();
                        _actionTimer = null;
                    }
                }
                if (_type == TOGGLE_TYPE || _type == RADIO_TYPE)
                    setHighlighted(false);
                else
                    setState(false);
            }
        }
    }

    /** Overridden to support Button clicks. */
    public void mouseUp(MouseEvent event) {
        boolean         mouseOverButton;

        if (!_enabled) {
            return;
        }

        if (_type == RADIO_TYPE) {
            if (_highlighted) {
                _highlighted = false;
                setState(true);
            }

            if (_state != _oldState) {
                sendCommand();
            }
            _oldState = false;

            if(canBecomeSelectedView() && rootView() != null)
                rootView().selectView(this,true);

            return;
        }

        // First of all stop the timed event
        if (_actionTimer != null) {
            _actionTimer.stop();
            _actionTimer = null;
        }

        // If we're still in the rect raise the button
        mouseOverButton = containsPoint(event.x, event.y);
        if (mouseOverButton) {
            _buttonUp();
        }

        if (_type == CONTINUOUS_TYPE) {
            _state = false;
            if (mouseOverButton) {
                setDirty(true);
            }
            if(canBecomeSelectedView() && rootView() != null)
                rootView().selectView(this,true);
            return;
        }

        // Only toggle buttons retain their state
        if (_type == TOGGLE_TYPE) {
            if (mouseOverButton) {
                _highlighted = false;
                _state = !_state;
            } else
                _highlighted = false;

            if (mouseOverButton)
                setDirty(true);

            if (mouseOverButton) {
                sendCommand();
            }
        } else { /* Simple push button change the state after the action */
            if (_type != CONTINUOUS_TYPE && mouseOverButton) {
                sendCommand();
            }
            _state = false;
            if (mouseOverButton)
               setDirty(true);
        }
        if(canBecomeSelectedView() && rootView() != null)
            rootView().selectView(this,true);
    }

    /** If the Button is currently pressed, returns the current mouse click
      * count (single-click, double-click, and so on).
      */
    public int clickCount() {
        if (_performingAction)
            return _clickCount;
        else
            return 0;
    }

    /** Presses the Button, as if the user clicked it with the mouse. */
    public void click() {
        if (!_enabled) {
            return;
        }

        // If we are already ON and we are a radio, do nothing
        if(_type == RADIO_TYPE && _state)   {
            return;
        }

        if (_type == TOGGLE_TYPE || _type == RADIO_TYPE) {
            setState(!_state);
            application().syncGraphics();

            sendCommand();
        } else {
            setState(true);
            application().syncGraphics();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            _clickCount = 1;
            sendCommand();

            setState(false);
        }
    }

    /** Implements the Button's commands:
      * <ul>
      * <li>SEND_COMMAND - calls the Button's <b>sendCommand()</b> method,
      * causing the Button to send its command to its Target.
      * <li>CLICK - Calls the Button's <b>click()</b> method, causing the
      * Button to change its state (based on its type), redraw, and send its
      * command to its Target.
      * </ul>
      */
    public void performCommand(String command, Object data) {
        int type = type();

        if (SEND_COMMAND.equals(command))
            sendCommand();
        else if (CLICK.equals(command))
            click();
        else if(type == RADIO_TYPE && SELECT_PREVIOUS_RADIO_BUTTON.equals(command))
                selectNextRadioButton(false);
        else if(type == RADIO_TYPE && SELECT_NEXT_RADIO_BUTTON.equals(command))
                selectNextRadioButton(true);
        else
            throw new NoSuchMethodError("unknown command: " + command);
    }

    /** DrawingSequenceOwner method implemented to handle DrawingSequences
      * assigned to the Button as Images.
      * @see DrawingSequenceOwner#drawingSequenceFrameChanged
      */
    public void drawingSequenceFrameChanged(DrawingSequence aSequence) {
        setDirty(true);
    }

    /** DrawingSequenceOwner method implemented to handle DrawingSequences
      * assigned to the Button as Images.
      * @see DrawingSequenceOwner#drawingSequenceCompleted
      */
    public void drawingSequenceCompleted(DrawingSequence aSequence) {
    }

    /** Called by the Button to send its command to its Target.
      * @see #setTarget
      */
    public void sendCommand() {
        _performingAction = true;

        if (_target != null) {
            _target.performCommand(_command, this);
        }

        _performingAction = false;
    }

    /** Sets the Button to draw itself highlighted.
      * @see isHighlighted
      *
      */
    protected void setHighlighted(boolean highlighted) {
        if (_highlighted != highlighted) {
            _highlighted = highlighted;
            setDirty(true);
        }
    }

    /** Returns whether or not the Button has received a MOUSE_DOWN MouseEvent
      * but has yet to receive a MOUSE_UP.
      *
      */
    protected boolean isHighlighted() {
        return _highlighted;
    }

    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * Button implementation returns true for a regular button and
      * false for unselected radio buttons
      *
      */
    public boolean canBecomeSelectedView() {
        if(isEnabled() && hasKeyboardBindings()) {
            if(type() == RADIO_TYPE) {
                if(state() == true)
                    return true;
                else
                    return false;
            } else
                return true;
        } else
            return false;
    }

/* archiving */


    /** Describes the Button class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.Button", 2);
        info.addField(TITLE_KEY, STRING_TYPE);
        info.addField(ALT_TITLE_KEY, STRING_TYPE);
        info.addField(TITLE_FONT_KEY, OBJECT_TYPE);
        info.addField(TITLE_COLOR_KEY, OBJECT_TYPE);
        info.addField(DISABLED_TITLE_COLOR_KEY, OBJECT_TYPE);
        info.addField(RAISED_COLOR_KEY, OBJECT_TYPE);
        info.addField(LOWERED_COLOR_KEY, OBJECT_TYPE);

        info.addField(IMAGE_KEY, OBJECT_TYPE);
        info.addField(ALT_IMAGE_KEY, OBJECT_TYPE);

        info.addField(DOWN_SOUND_KEY, OBJECT_TYPE);
        info.addField(UP_SOUND_KEY, OBJECT_TYPE);

        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(COMMAND_KEY, STRING_TYPE);

        info.addField(TYPE_KEY, INT_TYPE);
        info.addField(IMAGE_POSITION_KEY, INT_TYPE);
        info.addField(REPEAT_DELAY_KEY, INT_TYPE);

        info.addField(STATE_KEY, BOOLEAN_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(BORDERED_KEY, BOOLEAN_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);

        info.addField(RAISED_BORDER_KEY, OBJECT_TYPE);
        info.addField(LOWERED_BORDER_KEY, OBJECT_TYPE);
    }

    /** Encodes the Button instance.
     * @see Codable#encode
     */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeString(TITLE_KEY, _title);
        encoder.encodeString(ALT_TITLE_KEY, _altTitle);
        encoder.encodeObject(TITLE_FONT_KEY, _titleFont);

        encoder.encodeObject(TITLE_COLOR_KEY, _titleColor);
        encoder.encodeObject(DISABLED_TITLE_COLOR_KEY, _disabledTitleColor);

        encoder.encodeObject(RAISED_COLOR_KEY, _raisedColor);
        encoder.encodeObject(LOWERED_COLOR_KEY, _loweredColor);

        encoder.encodeObject(IMAGE_KEY, _image);
        encoder.encodeObject(ALT_IMAGE_KEY, _altImage);

        encoder.encodeObject(TARGET_KEY, (Codable)_target);
        encoder.encodeString(COMMAND_KEY, _command);

        encoder.encodeInt(TYPE_KEY, _type);
        encoder.encodeInt(IMAGE_POSITION_KEY, _imagePosition);
        encoder.encodeInt(REPEAT_DELAY_KEY, _repeatDelay);

        encoder.encodeBoolean(STATE_KEY, _state);
        encoder.encodeBoolean(ENABLED_KEY, _enabled);
        encoder.encodeBoolean(BORDERED_KEY, _bordered);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);

        encoder.encodeObject(RAISED_BORDER_KEY, _raisedBorder);
        encoder.encodeObject(LOWERED_BORDER_KEY, _loweredBorder);
    }

    /** Decodes the Button instance.
     * @see Codable#encode
     */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        _title = decoder.decodeString(TITLE_KEY);
        _altTitle = decoder.decodeString(ALT_TITLE_KEY);
        _titleFont = (Font)decoder.decodeObject(TITLE_FONT_KEY);

        _titleColor = (Color)decoder.decodeObject(TITLE_COLOR_KEY);
        _disabledTitleColor =
            (Color)decoder.decodeObject(DISABLED_TITLE_COLOR_KEY);

        _raisedColor = (Color)decoder.decodeObject(RAISED_COLOR_KEY);
        _loweredColor = (Color)decoder.decodeObject(LOWERED_COLOR_KEY);

        _image = (Image)decoder.decodeObject(IMAGE_KEY);
        _altImage = (Image)decoder.decodeObject(ALT_IMAGE_KEY);

        _target = (Target)decoder.decodeObject(TARGET_KEY);
        _command = decoder.decodeString(COMMAND_KEY);

        _type = decoder.decodeInt(TYPE_KEY);
        _imagePosition = decoder.decodeInt(IMAGE_POSITION_KEY);
        _repeatDelay = decoder.decodeInt(REPEAT_DELAY_KEY);

        _state = decoder.decodeBoolean(STATE_KEY);
        _enabled = decoder.decodeBoolean(ENABLED_KEY);
        _bordered = decoder.decodeBoolean(BORDERED_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);

        if (decoder.versionForClassName("netscape.application.Button") > 1) {
            _raisedBorder = (Border)decoder.decodeObject(RAISED_BORDER_KEY);
            _loweredBorder = (Border)decoder.decodeObject(LOWERED_BORDER_KEY);
        }
    }

    void _setupKeyboard() {
        removeAllCommandsForKeys();
        if(type() == RADIO_TYPE) {
            setCommandForKey(SELECT_NEXT_RADIO_BUTTON,KeyEvent.RIGHT_ARROW_KEY,View.WHEN_SELECTED);
            setCommandForKey(SELECT_NEXT_RADIO_BUTTON,KeyEvent.DOWN_ARROW_KEY,View.WHEN_SELECTED);
            setCommandForKey(SELECT_PREVIOUS_RADIO_BUTTON,KeyEvent.LEFT_ARROW_KEY,View.WHEN_SELECTED);
            setCommandForKey(SELECT_PREVIOUS_RADIO_BUTTON,KeyEvent.UP_ARROW_KEY,View.WHEN_SELECTED);
        } else {
            setCommandForKey(CLICK,KeyEvent.RETURN_KEY,View.WHEN_SELECTED);
        }
    }

    /** Implementation of the FormElement interface
      * If the button type is TOGGLE_TYPE this method
      * will returns "true" or "false" depending on it's
      * state. Otherwise, it will return it's title()
      *
      */
    public String formElementText() {
        if (_type == TOGGLE_TYPE) {
            if (_state) {
                return "true";
            } else {
                return "false";
            }
        } else {
            return title();
        }
    }
}

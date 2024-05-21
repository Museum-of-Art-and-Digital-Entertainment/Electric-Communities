// ColorChooser.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass creating a View containing controls for selecting a
  * Color. A selected Color can be dragged from the ColorChooser into an
  * accepting object within the Application. The ColorChooser can be added
  * to the View hierarchy, or given a Window to display itself in, using
  * the <b>setWindow()</b> method.
  * @see RootView#showColorChooser
  * @note 1.0 Changes to allow driving with Keyboard
  */

public class ColorChooser implements Target, TextFieldOwner {
    ColorWell    _colorWell;
    Slider       _redSlider, _greenSlider, _blueSlider;
    TextField    _redTextField, _greenTextField, _blueTextField;
    private ContainerView contentView;
    private Window window;
    private static final String COLOR_CHANGED = "colorChanged";

/* constructors */

    /** Constructs a ColorChooser. */
    public ColorChooser() {
        super();

        Font fieldFont;

        contentView = new ContainerView(0, 0, 157, 113);
        contentView.setBackgroundColor(Color.lightGray);
        contentView.setBorder(null);
        contentView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        contentView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        _colorWell = new ColorWell(3, 3, 152, 40);
        _colorWell.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        _colorWell.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        _colorWell.setTarget(this);
        contentView.addSubview(_colorWell);

      /* sliders */
        _redSlider = new Slider(3, 50, 120, 13);
        _redSlider.setBuffered(true);
        _redSlider.setImageDisplayStyle(Image.SCALED);
        _redSlider.setImage(Bitmap.bitmapNamed(
                                "netscape/application/RedGrad.gif"));
        _redSlider.setTarget(this);
        _redSlider.setCommand(COLOR_CHANGED);
        _redSlider.setValue(255);
        _redSlider.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        _redSlider.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_redSlider);

        _greenSlider = new Slider(3, 68, 120, 13);
        _greenSlider.setBuffered(true);
        _greenSlider.setImageDisplayStyle(Image.SCALED);
        _greenSlider.setImage(Bitmap.bitmapNamed(
                                "netscape/application/GreenGrad.gif"));
        _greenSlider.setTarget(this);
        _greenSlider.setCommand(COLOR_CHANGED);
        _greenSlider.setValue(255);
        _greenSlider.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        _greenSlider.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_greenSlider);

        _blueSlider = new Slider(3, 86, 120, 13);
        _blueSlider.setBuffered(true);
        _blueSlider.setImageDisplayStyle(Image.SCALED);
        _blueSlider.setImage(Bitmap.bitmapNamed(
                                "netscape/application/BlueGrad.gif"));
        _blueSlider.setTarget(this);
        _blueSlider.setCommand(COLOR_CHANGED);
        _blueSlider.setValue(255);
        _blueSlider.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        _blueSlider.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_blueSlider);

        fieldFont = Font.fontNamed("Helvetica", Font.PLAIN, 10);

        /* value text fields */
        _redTextField = new TextField(125, 49, 30, 16);
        _redTextField.setContentsChangedCommandAndTarget("", this);
        _redTextField.setFont(fieldFont);
        _redTextField.setTextColor(Color.darkGray);
        _redTextField.setBackgroundColor(Color.white);
        _redTextField.setIntValue(_redSlider.value());
        _redTextField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _redTextField.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_redTextField);

        _greenTextField = new TextField(125, 67, 30, 16);
        _greenTextField.setContentsChangedCommandAndTarget("", this);
        _greenTextField.setFont(fieldFont);
        _greenTextField.setTextColor(Color.darkGray);
        _greenTextField.setBackgroundColor(Color.white);
        _greenTextField.setIntValue(_greenSlider.value());
        _greenTextField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _greenTextField.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_greenTextField);

        _blueTextField = new TextField(125, 85, 30, 16);
        _blueTextField.setIntValue(_blueSlider.value());
        _blueTextField.setTextColor(Color.darkGray);
        _blueTextField.setBackgroundColor(Color.white);
        _blueTextField.setContentsChangedCommandAndTarget("", this);
        _blueTextField.setFont(fieldFont);
        _blueTextField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _blueTextField.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_blueTextField);

        _redTextField.setOwner(this);
        _blueTextField.setOwner(this);

        _updateColorWell();
    }

    /** If the ColorChooser is in a Window, makes the Window visible.  You must
      * have first set the ColorChooser's Window using its <b>setWindow()</b>
      * method.
      * @see RootView#showColorChooser
      */
    public void show() {
        if (window != null)
            window.show();
    }

    /** If the ColorChooser is in a Window, hides the Window.
      * @see #show
      */
    public void hide() {
        if (window != null)
            window.hide();
    }

    private void _updateColorWell() {
        _colorWell.setColor(
                new Color(_redSlider.value(), _greenSlider.value(),
                                                        _blueSlider.value()));
    }

    /** Sets the ColorChooser's current color. */
    public void setColor(Color aColor) {
        if (aColor != null) {
            _redSlider.setValue(aColor.red());
            _redTextField.setIntValue(aColor.red());
            _greenSlider.setValue(aColor.green());
            _greenTextField.setIntValue(aColor.green());
            _blueSlider.setValue(aColor.blue());
            _blueTextField.setIntValue(aColor.blue());

            _updateColorWell();
        }
    }

    /** Returns the ColorChooser's current color.
      * @see #setColor
      */
    public Color color() {
        return _colorWell.color();
    }

    /** Performs the commands necessary for the ColorChooser to operate. You
      * should never call this method.
      */
    public void performCommand(String command, Object data) {
        if (data == _redTextField) {
            _redSlider.setValue(_redTextField.intValue());
            _redTextField.setIntValue(_redSlider.value());
        } else if (data == _greenTextField) {
            _greenSlider.setValue(_greenTextField.intValue());
            _greenTextField.setIntValue(_greenSlider.value());
        } else if (data == _blueTextField) {
            _blueSlider.setValue(_blueTextField.intValue());
            _blueTextField.setIntValue(_blueSlider.value());
        } else if (data == _redSlider) {
            _redTextField.setIntValue(_redSlider.value());
        } else if (data == _greenSlider) {
            _greenTextField.setIntValue(_greenSlider.value());
        } else if (data == _blueSlider) {
            _blueTextField.setIntValue(_blueSlider.value());
        } else if (data == _colorWell) {
            setColor(_colorWell.color());
            return;
        }

        _updateColorWell();
    }

    /** Called when the user clicks in one of the ColorChooser's TextFields.
      * You should never call this method.
      * @see TextFieldOwner
      */
    public void textEditingDidBegin(TextField textField) {
    }

    /** Called when the user modifies one of the ColorChooser's TextFields.
      * You should never call this method.
      * @see TextFieldOwner
      */
    public void textWasModified(TextField textField) {
    }

    /** Called when the user starts editing one of the ColorChooser's
      * TextFields. You should never call this method.
      * @see TextFieldOwner
      */
    public boolean textEditingWillEnd(TextField textField, int endCondition,
                                      boolean contentsChanged) {
        return true;
    }

    /** Called when the user edits one of the ColorChooser's TextFields.
      * You should never call this method.
      * @see TextFieldOwner
      */
    public void textEditingDidEnd(TextField textField, int endCondition,
                                  boolean contentsChanged) {
    }

    /** Places the ColorChooser in <b>aWindow</b>. This method sizes the
      * <b>aWindow</b> to the minimum size necessary to display the
      * ColorChooser, as well as makes <b>aWindow</b> closable and sets its
      * title.
      */
    public void setWindow(Window aWindow) {
        Size    windowSize;

        window = aWindow;
        windowSize = window.windowSizeForContentSize(contentView.width(),
                                                     contentView.height());
        window.sizeTo(windowSize.width, windowSize.height);
        window.setTitle("Color Chooser");
        if (window instanceof InternalWindow) {
            InternalWindow iWindow = (InternalWindow) window;
            iWindow.setCloseable(true);
        }
        window.setContainsDocument(false);
        window.addSubview(contentView);
    }

    /** Returns the ColorChooser's Window.
      * @see #setWindow
      */
    public Window window() {
        return window;
    }

    /** Returns the ColorChooser's content View. */
    public View contentView() {
        return contentView;
    }
}

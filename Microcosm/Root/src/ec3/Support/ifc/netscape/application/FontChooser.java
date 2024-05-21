// FontChooser.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

// ALERT!!! - jla - need to think about where choosers appear onscreen

/** Object subclass creating a View with controls for selecting a Font.
  * The Set button sends the command ExtendedTarget.SET_FONT to the first
  * target in the Target Chain that can perform the command. The FontChooser
  * can be added to the View hierarchy, or given a Window to display itself
  * in, using the <b>setWindow()</b> method.
  * @see RootView#showFontChooser
  * @see ExtendedTarget
  * @note 1.0 changes for keyboard UI
  */

public class FontChooser implements Target {
    ListView               _nameList;
    Popup                  _sizePopup, _stylePopup;
    TextField              _sizeTextField, _messageTextField;
    Button                 _setButton;
    Font                   _currentFont;
    private ContainerView  contentView;
    private Window         window;

    /* constructors */

    /** Constructs a FontChooser. */
    public FontChooser() {
        super();

        ListView        tmpList;
        TextField       textField;
        ScrollGroup     scrollGroup;
        ContainerView   ruleView;
        FontItem        newItem, proto;
        Rect            listRect;
        String          styleArray[] = new String[4];
        int             styleTagArray[] = new int[4];
        int             i, count, sizeArray[] = new int[6];

        contentView = new ContainerView(0, 0, 178, 120);

        contentView.setBackgroundColor(Color.lightGray);
        contentView.setBorder(null);
        contentView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        contentView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        scrollGroup = new ScrollGroup(4, 19, 90, 61);
        scrollGroup.setHasVertScrollBar(true);
        scrollGroup.setBorder(BezelBorder.loweredBezel());
        scrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        listRect = scrollGroup.scrollView().bounds;
        _nameList = new ListView(0, 0, listRect.width, listRect.width);
        _nameList.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        _nameList.setPrototypeItem(new FontItem());
        _nameList.prototypeItem().setFont(Font.fontNamed("Default"));
        _loadNameList();

        scrollGroup.setContentView(_nameList);
        contentView.addSubview(scrollGroup);

        textField = new TextField(28, 1, 90 - 20, 18);
        textField.setEditable(false);
        textField.setTextColor(Color.black);
        textField.setFont(Font.fontNamed("Helvetica",
                                                            Font.BOLD, 12));
        textField.setBackgroundColor(Color.lightGray);
        textField.setStringValue("Name");
        textField.setJustification(Graphics.LEFT_JUSTIFIED);
        textField.setBorder(null);
        textField.setHorizResizeInstruction(View.RIGHT_MARGIN_CAN_CHANGE);
        textField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(textField);

        /* size popup */
        _sizePopup = new Popup(99, 19, 45, 20);
        proto = new FontItem();
        proto.setPopup(_sizePopup);
        proto.setFont(Font.fontNamed("Default"));
        _sizePopup.setPrototypeItem(proto);

        sizeArray[0] = 8;
        sizeArray[1] = 10;
        sizeArray[2] = 12;
        sizeArray[3] = 14;
        sizeArray[4] = 24;
        sizeArray[5] = 36;
        count = sizeArray.length;
        tmpList = _sizePopup.popupList();
        for (i = 0; i < count; i++) {
            newItem = (FontItem)tmpList.addItem();
            newItem.setTitle(Integer.toString(sizeArray[i]));
            newItem.setTag(sizeArray[i]);
        }
        newItem = (FontItem)tmpList.addItem();
        newItem.setTitle("Other");
        newItem.setTag(-1);
        _sizePopup.setTarget(this);
        _sizePopup.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _sizePopup.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(_sizePopup);

        _sizeTextField = new TextField(99 + 47, 19, 25, 20);
        _sizeTextField.setEditable(true);
        _sizeTextField.setContentsChangedCommandAndTarget("", this);
        _sizeTextField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _sizeTextField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(_sizeTextField);

        textField = new TextField(100, 1, 45 - 15, 18);
        textField.setEditable(false);
        textField.setTextColor(Color.black);
        textField.setBackgroundColor(Color.lightGray);
        textField.setFont(Font.fontNamed("Helvetica", Font.BOLD, 12));
        textField.setStringValue("Size");
        textField.setJustification(Graphics.LEFT_JUSTIFIED);
        textField.setBorder(null);
        textField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        textField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(textField);

        /* style popup */
        _stylePopup = new Popup(99, 61, 75, 21);
        proto = new FontItem();
        proto.setPopup(_stylePopup);
        proto.setFont(Font.fontNamed("Default"));
        _stylePopup.setPrototypeItem(proto);

        styleArray[0] = "Plain";
        styleArray[1] = "Bold";
        styleArray[2] = "Italic";
        styleArray[3] = "Bold Italic";
        styleTagArray[0] = Font.PLAIN;
        styleTagArray[1] = Font.BOLD;
        styleTagArray[2] = Font.ITALIC;
        styleTagArray[3] = Font.BOLD | Font.ITALIC;
        count = styleArray.length;
        tmpList = _stylePopup.popupList();
        for (i = 0; i < count; i++) {
            newItem = (FontItem)tmpList.addItem();
            newItem.setTitle(styleArray[i]);
            newItem.setTag(styleTagArray[i]);
        }
        _stylePopup.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _stylePopup.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(_stylePopup);

        textField = new TextField(100, 43, 45 - 15, 18);
        textField.setEditable(false);
        textField.setTextColor(Color.black);
        textField.setBackgroundColor(Color.lightGray);
        textField.setFont(Font.fontNamed("Helvetica",
                                                            Font.BOLD, 12));
        textField.setStringValue("Style");
        textField.setJustification(Graphics.LEFT_JUSTIFIED);
        textField.setBorder(null);
        textField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        textField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        contentView.addSubview(textField);

        /* rule */
        ruleView = new ContainerView(-2, 87, 184, 2);
        ruleView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        ruleView.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(ruleView);

        _setButton = new Button(124, 95, 50, 21);
        _setButton.setTitle("Set");
        _setButton.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        _setButton.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        _setButton.setCommand(ExtendedTarget.SET_FONT);
        _setButton.setTarget(this);
        contentView.addSubview(_setButton);

        _messageTextField = new TextField(4, 95, 100, 21);
        _messageTextField.setEditable(false);
        _messageTextField.setBorder(null);
        _messageTextField.setTextColor(Color.gray);
        _messageTextField.setBackgroundColor(Color.lightGray);
        _messageTextField.setFont(
                    Font.fontNamed("Helvetica",
                                                        Font.PLAIN, 10));
        _messageTextField.setHorizResizeInstruction(
                                            View.RIGHT_MARGIN_CAN_CHANGE);
        _messageTextField.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        contentView.addSubview(_messageTextField);

        setFont(Font.defaultFont());
    }

    private void _loadNameList() {
        FontItem        newItem;
        String          nameArray[] = new String[6],
                        fontNameArray[] = new String[6];
        int             i, count;

        nameArray[0] = "Courier";
        nameArray[1] = "Dialog";
        nameArray[2] = "Dialog Input";
        nameArray[3] = "Helvetica";
        nameArray[4] = "Times Roman";
        nameArray[5] = "Zapf Dingbats";

        fontNameArray[0] = "Courier";
        fontNameArray[1] = "Dialog";
        fontNameArray[2] = "DialogInput";
        fontNameArray[3] = "Helvetica";
        fontNameArray[4] = "TimesRoman";
        fontNameArray[5] = "ZapfDingbats";
        count = nameArray.length;
        for (i = 0; i < count; i++) {
            newItem = (FontItem)_nameList.addItem();
            newItem.setTitle(nameArray[i]);
            newItem.setFontName(fontNameArray[i]);
        }

        _nameList.setRowHeight(_nameList.minItemHeight());
        _nameList.sizeToMinSize();
    }

    /** If the FontChooser is in a Window, makes the Window visible.  You must
      * have first set the FontChooser's Window using its <b>setWindow()</b>
      * method.
      * @see #setWindow
      * @see RootView#showFontChooser
      */
    public void show() {
        if (window != null)
            window.show();
    }

    /** Hides the FontChooser's Window.
      * @see #show
      */
    public void hide() {
        if (window != null)
            window.hide();
    }

    private void _setSizePopupToSize(int aSize) {
        FontItem    nextItem;
        int     i;

        i = _sizePopup.count();
        while (i-- > 0) {
            nextItem =(FontItem)_sizePopup.popupList().itemAt(i);
            if (nextItem.tag() == aSize) {
                _sizePopup.selectItemAt(i);
                return;
            }
        }
        if (i == -1) {
            _sizePopup.selectItemAt(_sizePopup.count() - 1);
        }
    }

    /** Makes <b>aFont</b> the FontChooser's current font.
      */
    public void setFont(Font aFont) {
        FontItem        nextFontItem;
        FontItem            nextItem;
        int             i;

        if (aFont == null) {
            return;
        }

        _currentFont = aFont;

        /* name */
        i = _nameList.count();
        while (i-- > 0) {
            nextFontItem = (FontItem)_nameList.itemAt(i);
            if (nextFontItem.hasFontName(aFont.family())) {
                _nameList.selectItemAt(i);
                _nameList.scrollItemAtToVisible(i);
                break;
            }
        }
        if (i == -1) {
            _nameList.selectItemAt(0);
        }

        /* style */
        if (aFont.isBold()) {
            if (aFont.isItalic()) {
                _stylePopup.selectItemAt(3);
            } else {
                _stylePopup.selectItemAt(1);
            }
        } else if (aFont.isItalic()) {
            _stylePopup.selectItemAt(2);
        } else {
            _stylePopup.selectItemAt(0);
        }

        /* size */
        _setSizePopupToSize(aFont.size());
        _sizeTextField.setIntValue(aFont.size());
    }

    /** Returns the FontChooser's current font.
      * @see #setFont
      */
    public Font font() {
        Font            newFont;
        FontItem        Item;
        String          name;

        if (_nameList.selectedItem() == null) {
            name = "";
        } else {
            Item = (FontItem)_nameList.selectedItem();
            name = Item.fontName();
        }

        newFont = Font.fontNamed(name,
                                 ((FontItem)_stylePopup.selectedItem()).tag(),
                                 _sizeTextField.intValue());

        return newFont;
    }

    /** Performs the commands necessary for the FontChooser to operate. You
      * should never call this method.
      */
    public void performCommand(String command, Object data) {
        int             fontSize;

        if (data == _sizeTextField) {
            fontSize = _sizeTextField.intValue();
            if (fontSize > 0) {
                _setSizePopupToSize(fontSize);
            } else {
                _setSizePopupToSize(8);
                _sizeTextField.setIntValue(8);
            }
        } else if (data == _sizePopup) {
            fontSize = ((FontItem)(_sizePopup.selectedItem())).tag();
            if (fontSize > 0) {
                _sizeTextField.setIntValue(fontSize);
            }
        } else if (data == _setButton) {
            Target target = TargetChain.applicationChain();

            target.performCommand(command, font());
        }
    }

    /** Places the FontChooser in the Window <b>aWindow</b>. This method sizes
      * <b>aWindow</b> to the minimum size necessary to display the
      * FontChooser, as well as makes <b>aWindow</b> closable and sets its
      * title.
      */
    public void setWindow(Window aWindow) {
        Size    windowSize;
        Rect    bounds;

        this.window = aWindow;
        windowSize
            = this.window.windowSizeForContentSize(contentView.width(),
                                                   contentView.height());
        this.window.sizeTo(windowSize.width, windowSize.height);
        this.window.addSubview(contentView);
        this.window.setTitle("Font Chooser");
        bounds = this.window.bounds();
        this.window.setMinSize(bounds.width, bounds.width);
        if (this.window instanceof InternalWindow) {
            InternalWindow iWindow = (InternalWindow) this.window;
            iWindow.setCloseable(true);
            iWindow.setBuffered(true);
        }
        this.window.setContainsDocument(false);
    }

    /** Returns the FontChooser's Window.
      * @see #setWindow
      */
    public Window window() {
        return window;
    }

    /** Returns the FontChooser's content View. */
    public View contentView() {
        return contentView;
    }
}

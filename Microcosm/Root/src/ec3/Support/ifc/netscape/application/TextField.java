// TextField.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;


/** View subclass that displays and, optionally, lets the user edit a mono-
  * font string.
  * TextFields have owners that are notified of important events within the
  * TextField, such as the completion of an editing session. They also have
  * filters that can filter or process each key event received by the
  * TextField. On certain events, such as receiving a tab key, the TextField
  * can send a command to a Target interested in reacting to that event.
  * @note 1.0 added drawableCharacter for password purpose.
  * @note 1.0 added isScrollable and all draws to dirtRects
  * @note 1.0 major changes
  * @note 1.0 Added FormElement interface for browser needs
  */
public class TextField extends View implements ExtendedTarget, FormElement {
    TextFieldOwner      _owner;
    TextFilter          _filter;
    Target              _tabTarget, _backtabTarget, _contentsChangedTarget,
                        _commitTarget;
    Vector              _keyVector;
    Border              border = BezelBorder.loweredBezel();
    Font                _font;
    Color               _textColor, _backgroundColor, _selectionColor,
                        _caretColor;
    String              _tabCommand, _backtabCommand, _contentsChangedCommand,
                        _commitCommand;
    FastStringBuffer    _contents, _oldContents;
    Timer               blinkTimer;
    char                _drawableCharacter;
    int                 _selectionAnchorChar = -1, _selectionEndChar = -1,
                        _justification, _scrollOffset, _fontHeight,
                        _initialAnchorChar = -1,_clickCount;
    boolean             _editing, _caretShowing, _canBlink, _editable,
                        _selectable, _mouseDragging, _shadowed,
                        _textChanged = false, _canWrap, transparent = false,
                        wantsAutoscrollEvents = false, isScrollable = true,
                        hasFocus = false, _ignoreWillBecomeSelected;

    /** Constant to indicate that any character can be displayed
     *  @see setDrawableCharacter()
     */
    public static char  ANY_CHARACTER = (char)-1;

    /** Command to select the TextField's entire contents. */
    final public static String  SELECT_TEXT = "selectText";
    final static String         BLINK_CARET = "blinkCaret";

    final static String OWNER_KEY = "owner",
                        FILTER_KEY = "filter",
                        TABREC_KEY = "tabTarget",
                        BACKTABREC_KEY = "backtabTarget",
                        CONTENTSCHANGEDREC_KEY = "contentsChangedTarget",
                        COMMITREC_KEY = "commitTarget",
                        BORDER_KEY = "border",
                        FONT_KEY = "font",
                        TEXTC_KEY = "textColor",
                        BACKGROUNDC_KEY = "backgroundColor",
                        SELECTIONC_KEY = "selectionColor",
                        CARETC_KEY = "caretColor",
                        TABCOM_KEY = "tabCommand",
                        BACKTABCOM_KEY = "backtabCommand",
                        CONTENTSCHANGEDCOM_KEY = "contentsChangedCommand",
                        COMMITCOM_KEY = "commitCommand",
                        CONTENTS_KEY = "contents",
                        SELECTIONANCH_KEY = "selectionAnchorChar",
                        SELECTIONEND_KEY = "selectionEndChar",
                        JUSTIFICATION_KEY = "justification",
                        SCROLLOFFSET_KEY = "scrollOffset",
                        EDITABLE_KEY = "editable",
                        SELECTABLE_KEY = "selectable",
                        SHADOWED_KEY = "shadowed",
                        CANWRAP_KEY = "canWrap",
                        AUTOSCROLLEVENT_KEY = "wantsAutoscrollEvents",
                        TRANSPARENT_KEY = "transparent",
                        DRAWABLE_CHAR_KEY = "drawableCharacter",
                        DRAWABLE_CHAR_STRING_KEY = "drawable char",
                        SCROLLABLE_KEY = "isScrollable";



    /* constructors */

    /** Constructs a TextField with origin (<b>0</b>, <b>0</b>) and zero
      * width and height.
      */
    public TextField() {
        this(0, 0, 0, 0);
    }

    /** Constructs a TextField with bounds <B>rect</B>.
      */
    public TextField(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a TextField with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public TextField(int x, int y, int width, int height) {
        super(x, y, width, height);

        _keyVector = new Vector();

        _contents = new FastStringBuffer();
        _drawableCharacter = ANY_CHARACTER;
        _textColor = Color.black;
        _backgroundColor = Color.white;
        _selectionColor = Color.lightGray;
        _caretColor = Color.black;

        setEditable(true);
        setFont(Font.defaultFont());
    }

    /** Creates a new TextField that is suitable for use as a "label": it has
      * no Border, is transparent, and is not editable or selectable. The
      * label displays <b>string</b> using <b>font</b>.
      * This method is obsolete. Use the <b>Label</b> class instead.
      * @see Label
      * @deprecated
      */
    public static TextField createLabel(String string, Font font) {
        FontMetrics metrics = font.fontMetrics();
        int width = metrics.stringWidth(string),
            height = metrics.stringHeight();
        TextField label = new TextField(0, 0, width, height);

        label.setBorder(null);
        label.setStringValue(string);
        label.setFont(font);
        label.setTransparent(true);
        label.setEditable(false);
        label.setSelectable(false);
        return label;
    }

    /** Creates a new TextField that is suitable for use as a "label": it has
      * no Border, is transparent, and is not editable or selectable. The label
      * displays <b>string</b> using the default font.
      */
    public static TextField createLabel(String string) {
        return createLabel(string, Font.defaultFont());
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

/* dimensional attributes */

   /** Returns the distance between the interior edge of the TextField's
    * left border and the first character. By default, this method returns
    * the left margin of the TextField's Border.
    */
    public int leftIndent() {
        int leftIndent;

        // ALERT!
        leftIndent = border.leftMargin();
        if (leftIndent > 2)
            leftIndent = 2;

        return leftIndent;
    }

    /** Returns the distance between the interior edge of the TextField's right
      * border and the last character. By default, returns the right margin
      * plus 1 of the TextField's Border.
      */
    public int rightIndent() {
        int rightIndent;

        // ALERT!
        rightIndent = border.rightMargin() + 1;
        if (rightIndent > 3)
            rightIndent = 3;

        return rightIndent;
    }

    /** Returns the distance between the interior edge of the TextField's left
      * border and the first character, and the right border and the last
      * character. Equivalent to the code:
      * <pre>
      *     leftIndent() + rightIndent()
      * </pre>
      */
    private int widthIndent() {
        return leftIndent() + rightIndent();
    }

    /** Returns the TextField's minimum size.
     */
    public Size minSize() {
        Vector  stringVector;
        Rect    interiorRect;
        Size    theSize;

        if (_minSize != null) {
            return new Size(_minSize);
        }

        theSize = _font.fontMetrics().stringSize(drawableString());
        interiorRect = interiorRect();

        /* if text can wrap and width is fixed, compute min height */
        if ((horizResizeInstruction() != WIDTH_CAN_CHANGE) && _canWrap &&
            !isEditable() && theSize.width > interiorRect.width) {
            stringVector = stringVectorForContents(interiorRect.width);
            theSize.sizeTo(interiorRect.width,
                           theSize.height * stringVector.count());
        }
        Rect.returnRect(interiorRect);

        theSize.sizeBy(border.widthMargin() + widthIndent(),
                       border.heightMargin());

        return theSize;
    }



/* appearance attributes */


    /** Set the character used to display the contents.
      * the default is <B>ANY_CHARACTER</B>.
      *
      */
    public void setDrawableCharacter(char aChar) {
        _drawableCharacter = aChar;
        _scrollOffset = 0;
        _computeScrollOffset();
        setDirty(true);
    }

    /** Return the character used to display the contents **/
    public char drawableCharacter() {
        return _drawableCharacter;
    }

    /** Sets the TextField's Font and redraws the TextField.  The default Font
      * is Font.defaultFont().
      * @see Font#defaultFont
      */
    public void setFont(Font aFont) {
        Size    stringSize;

        if (aFont == null) {
            _font = Font.defaultFont();
        } else {
            _font = aFont;
        }

        stringSize = _font.fontMetrics().stringSize(null);
        _fontHeight = stringSize.height;

        setDirty(true);
    }

    /** Returns the TextField's font.
      * @see #setFont
      */
    public Font font() {
        return _font;
    }

    /** Sets the Color of the TextField's text and redraws the TextField.
      * Default text color is Color.black.
      */
    public void setTextColor(Color aColor) {
        _textColor = aColor;
        if (_textColor == null) {
            _textColor = Color.black;
        }
        setDirty(true);
    }

    /** Returns the TextField's text Color.
      * @see #setTextColor
      */
    public Color textColor() {
        return _textColor;
    }

    /** Sets the TextField's background Color and redraws the TextField.
      * Default background Color is Color.white.
      */
    public void setBackgroundColor(Color aColor) {
        _backgroundColor = aColor;
        if (_backgroundColor == null) {
            _backgroundColor = Color.white;
        }
        setDirty(true);
    }

    /** Returns the TextField's background Color.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return _backgroundColor;
    }

    /** Sets the Color the TextField uses to indicate selection and redraws the
      * TextField.  Default selection Color is Color.lightGray.
      */
    public void setSelectionColor(Color aColor) {
        _selectionColor = aColor;
        if (_selectionColor == null) {
            _selectionColor = Color.lightGray;
        }
        setDirty(true);
    }

    /** Returns the TextField's selection Color.
      * @see #setSelectionColor
      */
    public Color selectionColor() {
        return _selectionColor;
    }

    /** Sets the Color of the TextField's caret.  Default is Color.black.
      */
    public void setCaretColor(Color aColor) {
        _caretColor = aColor;
        if (_caretColor == null) {
            _caretColor = Color.black;
        }
    }

    /** Returns the TextField's caret Color.
      * @see #setCaretColor
      */
    public Color caretColor() {
        return _caretColor;
    }

    /** Sets the Border the TextField draws around its perimeter.
      * @see Border
      */
    public void setBorder(Border newBorder) {
        if (newBorder == null)
            newBorder = EmptyBorder.emptyBorder();

        border = newBorder;
    }

    /** Returns the TextField's Border.
      * @see #setBorder
      */
    public Border border() {
        return border;
    }

    /** Configures the TextField to draw its text with a drop shadow.
      */
    public void setDrawsDropShadow(boolean flag) {
        _shadowed = flag;
        setDirty(true);
    }

    /** Returns <b>true</b> if the TextField draws its text with a drop shadow.
      * @see #setDrawsDropShadow
      */
    public boolean drawsDropShadow() {
        return _shadowed;
    }

    /** Sets the justification (Graphics.LEFT_JUSTIFIED,
      * Graphics.CENTERED, Graphics.RIGHT_JUSTIFIED) the
      * TextField uses to draw its text.
      */
    public void setJustification(int aJustification) {
        if (aJustification < Graphics.LEFT_JUSTIFIED ||
            aJustification > Graphics.RIGHT_JUSTIFIED) {
            return;
        }

        if( aJustification != _justification ) {
           _justification = aJustification;
           _scrollOffset  = 0;
        }
    }

    /** Returns the justification the TextField uses to draw its text.
      */
    public int justification() {
        return _justification;
    }

    /** Overridden to automatically remove the TextField's Border if
      * <b>flag</b> is <b>true</b>.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
        if (transparent) {
            setBorder(null);
        }
    }

    /** Overridden to return <b>true</b> if the TextField is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }


/* behavioral attributes */

    /** Configures the TextField to allow the user to select its text.
      */
    public void setSelectable(boolean flag) {
        RootView        rootView;

        if (_selectable != flag) {
            _selectable = flag;
            wantsAutoscrollEvents = flag;
            if (!_selectable && _scrollOffset != 0) {
                _scrollOffset = 0;
                drawInterior();
            }

            rootView = rootView();
            if (rootView != null) {
                rootView.updateCursor();
            }
        }
    }

    /** Returns <b>true</b> if the TextField allows the user to select its
      * text.
      * @see #setSelectable
      */
    public boolean isSelectable() {
        return _selectable;
    }

    /** Overridden to return <B>true</B> if the TextField wants to
      * automatically receive mouse dragged Events when the user clicks and
      * drags outside of its bounds.
      */
    public boolean wantsAutoscrollEvents() {
        return wantsAutoscrollEvents;
    }

    /** Configures the TextField to wrap its contents if too long to fit
      * on a single line.
      */
    public void setWrapsContents(boolean flag) {
        _canWrap = flag;
        if( flag && isEditable())
            setEditable(false);
        drawInterior();
    }

    /** Returns <b>true</b> if the TextField wraps its contents if too long to
      * fit on a single line.
      * @see #setWrapsContents
      */
    public boolean wrapsContents() {
        return _canWrap;
    }

   /** Configures the TextField to allow the user to edit its text. */
    public void setEditable(boolean aFlag) {
        if (_editable != aFlag) {
            _editable = aFlag;
            setSelectable(aFlag);
            if( aFlag && wrapsContents() )
                setWrapsContents(false);
        }
    }

    /** Returns <b>true</b> if the TextField is editable.
      * @see #setEditable
      */
    public boolean isEditable() {
        return _editable;
    }

    /** Returns <b>true</b> if the TextField's contents are currently being
      * edited.
      */
    public boolean isBeingEdited() {
        return _editing;
    }

    /** Overridden to return TEXT_CURSOR when the TextField is selectable or
      * editable.
      */
    public int cursorForPoint(int x, int y) {
        if (isEditable() || isSelectable()) {
            return TEXT_CURSOR;
        }
        return ARROW_CURSOR;
    }



/* other attributes */

    /** Sets the TextField's owner, the object that it notifies of important
      * events such as receiving a new key Event.
      * @see TextFieldOwner
      */
    public void setOwner(TextFieldOwner owner) {
        _owner = owner;
    }

    /** Returns the TextField's owner.
      * @see #setOwner
      */
    public TextFieldOwner owner() {
        return _owner;
    }

    /** Sets the TextField's filter, the object that inspects each key
      * Event received by the TextField.
      * @see TextFilter
      */
    public void setFilter(TextFilter aFilter) {
        _filter = aFilter;
    }

    /** Returns the TextField's text filter.
      * @see #setFilter
      */
    public TextFilter filter() {
        return _filter;
    }

    /** Sets the Target and the command it should receive when the TextField's
      * contents change. This command is sent each time the user changes the
      * text inside the TextField.
      */
    public void setContentsChangedCommandAndTarget(String aCommand,
                                                   Target aTarget) {
        _contentsChangedCommand = aCommand;
        _contentsChangedTarget = aTarget;
    }

    /** Returns the contents changed Target.
      * @see #setContentsChangedCommandAndTarget
      */
    public Target contentsChangedTarget() {
        return _contentsChangedTarget;
    }

    /** Returns the contents changed command.
      * @see #setContentsChangedCommandAndTarget
      */
    public String contentsChangedCommand() {
        return _contentsChangedCommand;
    }

    /** Sets the TextField whose text is selected when the TextField
      * receives a Return or Tab key Event.
      */
    public void setTabField(TextField aTextField) {
        if (aTextField == null) {
            _tabTarget = null;
            _tabCommand = null;
        } else {
            _tabTarget = aTextField;
            _tabCommand = SELECT_TEXT;
        }
        invalidateKeyboardSelectionOrder();
    }

    /** Returns the TextField whose text is selected when the TextField
      * receives a Return or Tab key Event.
      * @see #setTabField
      */
    public TextField tabField() {
        if (_tabTarget instanceof TextField) {
            return (TextField)_tabTarget;
        }

        return null;
    }

    /** Sets the TextField whose text is selected when the TextField
      * receives a Backtab key Event.
      */
    public void setBacktabField(TextField aTextField) {
        if (aTextField == null) {
            _backtabTarget = null;
            _backtabCommand = null;
        } else if (aTextField != this) {
            _backtabTarget = aTextField;
            _backtabCommand = SELECT_TEXT;
        }
        invalidateKeyboardSelectionOrder();
    }

    /** Returns the TextField whose text is selected when the TextField
      * receives a Backtab key Event.
      * @see #setBacktabField
      */
    public TextField backtabField() {
        if (_backtabTarget instanceof TextField) {
            return (TextField)_backtabTarget;
        }

        return null;
    }

    /** Sets the Target that should receive a command when the textfield
     *  commit to a new value. The textfield commit to a new value in the
     *  following situations:
     *  <OL>
     *  <LI> The user types Return.
     *  <LI> Contents has changed and the user press tab or backtab
     *  <LI> Contents has changed and the textfield loses focus
     *  </OL>
     *  If a more precise understanding of why the textfield is ending
     *  the editing session is required, you need to use TextFieldOwner.
     */
    public void setTarget(Target aTarget) {
        _commitTarget = aTarget;
    }

    /** Sets the command the Target should receive when the
     *  editing ends. The editing ends when the user types return,
     *  tab, backtab or select something else.
     */
    public void setCommand(String aCommand) {
        _commitCommand = aCommand;
    }

    /** Returns the Return Target.
      * @see #setTarget
      */
    public Target target() {
        return _commitTarget;
    }

    /** Returns the Return command.
      * @see #setCommand
      */
    public String command() {
        return _commitCommand;
    }


/* content */

    /** Sets the contents of the TextField to <b>aString</b> */
    public void setStringValue(String aString) {
        if (aString != null && aString.equals(stringValue())) {
            if (isBeingEdited()) {
                cancelEditing();
            }
            return;
        }

        if(aString == null)
            aString = "";

        replaceRangeWithString(new Range(0,charCount()),aString);

        _oldContents = null;

        if (isBeingEdited()) {
            cancelEditing();
        } else {
            setDirty(true);
        }
    }

    /** Returns the TextField's contents.
      * @see #setStringValue
      */
    public String stringValue() {
        if (_contents == null) {
            return "";
        }
        return _contents.toString();
    }

    /** Replaces the string enclosed by <b>aRange</b> with <b>aString</b>.  If
      * <b>aRange</b> is a null range, this method inserts <b>aString</b> into
      * the text.  If <b>aString</b> is <b>null</b> or the empty string, this
      * method removes the text defined by <b>aRange</b>.
      */
    public void replaceRangeWithString(Range aRange,String aString) {
        String contents = stringValue();
        Range  r = new Range();
        String before,after;

        r.index  = aRange.index;
        r.length = aRange.length;
        r.intersectWith(new Range(0,contents.length()));

        if( r.isNullRange()) {
            r.index = contents.length();
            r.length= 0;
        }

        before = contents.substring(0,r.index);
        after  = contents.substring(r.index+r.length);

        if( aString != null )
            _contents = new FastStringBuffer(before + aString + after );
        else
            _contents = new FastStringBuffer(before + after);

        if( isBeingEdited() ) {
            Range selectedRange;

            r.index = 0;
            r.length = _contents.length();
            selectedRange = selectedRange();
            selectedRange.intersectWith(r);
            if( selectedRange.isNullRange())
                selectRange(new Range(_contents.length,0));
            else
                selectRange(selectedRange);
        } else
            setDirty(true);
    }

    /** Returns the string included in <b>aRange</b>. */
    public String stringForRange(Range aRange) {
        String contents = stringValue();
        Range r = new Range();
        r.index = aRange.index;
        r.length = aRange.length;
        r.intersectWith( new Range(0,contents.length()));
        if( r.isNullRange())
            return "";
        return contents.substring(r.index,r.index+r.length());
    }

    /** Returns the selected portion of the TextField's contents.  If none,
      * returns the empty string.
      * @see #stringValue
      */
    public String selectedStringValue() {
        int     start, stop;

        if (hasInsertionPoint()) {
            return "";
        }

        start = selectionStart();
        stop = selectionStop();

        if (start == -1 || stop == -1) {
            return "";
        }

        if (stop == _contents.length()) {
            return _contents.toString().substring(start);
        }

        return _contents.toString().substring(start, stop);
    }

    /** Sets the contents of the TextField to the string version of
      * <b>anInt</b>.
      */
    public void setIntValue(int anInt) {
        setStringValue(Integer.toString(anInt));
    }

    /** Returns the integer value of the TextField's contents, or <b>0</b> if
      * not a number.
      */
    public int intValue() {
        return parseInt(_contents.toString());
    }

    /** Returns <b>true</b> if the TextField contains no text.
      */
    public boolean isEmpty() {
        return (charCount() == 0);
    }

    /** Returns the number of characters the TextField contains. */
    public int charCount() {
        return _contents.length();
    }

    /** Returns the baseline of the first line of text.
      */
    public int baseline() {
        String  contentString;
        Rect    interiorRect;
        Size    stringSize;
        int     y;

        contentString = drawableString();
        stringSize = _font.fontMetrics().stringSize(contentString);

        interiorRect = Rect.newRect();
        border.computeInteriorRect(0, 0, bounds.width, bounds.height,
                                   interiorRect);
        y = interiorRect.maxY() -
                        (interiorRect.height - stringSize.height) / 2 -
                        _font.fontMetrics().descent();

        Rect.returnRect(interiorRect);

        return y;
    }



/* selection */

    /** Selects characters in the Range <b>aRange</b>, unless the TextField is
      * not selectable.
      */
    public void selectRange(Range aRange) {
        if( aRange.length < 0 || aRange.index < 0 )
            throw new InconsistencyException("TextField - invalid range: " +
                                                                    aRange);

        selectRange(aRange.index,aRange.index + aRange.length);
    }

    /** Selects characters <b>start</b> through <b>stop</b> of the
      * TextField's contents, unless the TextField is not selectable.
      * Override this method if you need to know when the selection changed.
      *
      */
    protected void selectRange(int start, int stop) {
        if (!isSelectable()) {
            return;
        }

        if (start < 0) {
            start = 0;
        } else if (start > _contents.length()) {
            start = _contents.length();
        }
        if (stop < 0) {
            stop = 0;
        } else if (stop > _contents.length()) {
            stop = _contents.length();
        }

        _selectionAnchorChar = start;
        _selectionEndChar = stop;

        if (isEditable() && !isBeingEdited()) {
            _startEditing(true);
        }

        // Need to refresh here since selection not visible
        // when textfield does not have the focus
        drawInterior();
    }

    /** Selects the TextField's contents, unless it is not selectable.
      * @see #selectRange
      */
    public void selectText() {
        selectRange(0, charCount());
    }

    /** Places the TextField's insertion point at <b>position</b>, unless
      * the TextField is not selectable.
      */
    public void setInsertionPoint(int position) {
        selectRange(position, position);
    }

    int selectionAnchorPoint() {
        return _selectionAnchorChar;
    }

    int selectionEndPoint() {
        return _selectionEndChar;
    }

    /** Returns a Range containing the current selection. If no selection
      * exists, this method returns a null range.
      */
    public Range selectedRange() {
        if( hasInsertionPoint() ) {
            return new Range(_selectionAnchorChar,0);
        }
        if( _selectionAnchorChar == -1 ||
            _selectionEndChar == -1 )
            return new Range();
        else
            return Range.rangeFromIndices(selectionStart(),selectionStop());
    }

    /** Returns the location of the first selected character, or <b>-1</b> if no
      * selection or insertion point.
      */
    int selectionStart() {
        return (_selectionAnchorChar < _selectionEndChar) ?
                                    _selectionAnchorChar : _selectionEndChar;
    }

    /** Returns the location of the last selected character, or <b>-1</b> if no
      * selection or insertion point.
      */
    int selectionStop() {
        return (_selectionAnchorChar < _selectionEndChar) ?
                                    _selectionEndChar : _selectionAnchorChar;
    }

    /** Returns <b>true</b> if the TextField has a selection. */
    public boolean hasSelection() {
        return (_selectionAnchorChar != _selectionEndChar);
    }

    /** Returns <b>true</b> if the TextField has an insertion point. */
    public boolean hasInsertionPoint() {
        return (_selectionAnchorChar == _selectionEndChar &&
                _selectionAnchorChar != -1);
    }

    Rect caretRect() {
        FontMetrics  fontMetrics;
        Rect            interiorRect;
        int             y1, y2, cutoff;

        fontMetrics = _font.fontMetrics();
        if (fontMetrics == null) {
            return null;
        }

        interiorRect = interiorRect();
        y2 = interiorRect.maxY() - (interiorRect.height - _fontHeight) / 2;

        y1 = y2 - fontMetrics.charHeight();
        cutoff = border.topMargin();
        if (y1 < cutoff) {
            y1 = cutoff;
        }

        Rect.returnRect(interiorRect);

        /* we need to take endChar to make scroll offset computation work */
        return Rect.newRect(xPositionOfCharacter(_selectionEndChar), y1, 1,
                            y2 - y1);
    }

    Rect interiorRect() {
        Rect interiorRect = Rect.newRect();

        border.computeInteriorRect(0, 0, width(), height(), interiorRect);
        return interiorRect;
    }

    Rect rectForRange(int start, int stop) {
        Rect    tmpRect;
        int     startX;

        startX = xPositionOfCharacter(start);
        tmpRect = interiorRect();
        tmpRect.setBounds(startX, tmpRect.y,
                          xPositionOfCharacter(stop) - startX + 1,
                          tmpRect.height);

        return tmpRect;
    }



/* drawing */

    /** Returns the X-coordinate of character number <b>charNumber</b>. */
    public int xPositionOfCharacter(int charNumber) {
        FontMetrics  fontMetrics;
        String       contentString;
        int          startX;
        int          stringWidth;

        fontMetrics = _font.fontMetrics();
        contentString = drawableString();
        stringWidth   = fontMetrics.stringWidth(contentString);

        startX = absoluteXOriginForStringWithWidth(stringWidth);

        if( charNumber <= 0 )
            return startX;
        else
            return startX + fontMetrics.stringWidth(
                             contentString.substring(0, charNumber));
    }

    /** Returns the character number for X-coordinate <b>x</b>. */
    public int charNumberForPoint(int x) {
        FontMetrics     fontMetrics;
        String          contentString;
        int             contentLength, stringWidth, i, width,oldWidth,delta;
        int             startX;

        contentLength = _contents.length();
        if (contentLength == 0) {
            return 0;
        }

        fontMetrics = _font.fontMetrics();
        contentString = drawableString();

        stringWidth = fontMetrics.stringWidth(contentString);

        startX = absoluteXOriginForStringWithWidth(stringWidth);
        if (x < startX) {
            return 0;
        } else if (x > (startX + stringWidth)) {
            return contentLength;
        }

        oldWidth = 0;
        for (i = 1; i < contentLength; i++) {
            width = fontMetrics.stringWidth(contentString.substring(0, i));
            delta = width - oldWidth;
            if (x <= (startX + width)) {
                if (x > (startX + oldWidth + (delta / 2))) {
                    return i;
                } else {
                    return i - 1;
                }
            }

            oldWidth = width;
        }

        return contentLength;
    }

    void drawViewCaret(Graphics g) {
        Rect    caretRect;

        if (!_caretShowing || _selectionAnchorChar == -1 ||
            _selectionAnchorChar != _selectionEndChar ||
            !hasFocus ) {
            return;
        }

        g.setColor(_caretColor);
        caretRect = caretRect();
        g.drawLine(caretRect.x, caretRect.y, caretRect.x, caretRect.maxY()-1);
        Rect.returnRect(caretRect);
    }

    Vector stringVectorForContents(int maxWidth) {
        int charWidths[];
        FontMetrics fm;
        int currentWidth;
        Vector result = new Vector();
        String contents = drawableString();
        int    index,firstIndex,length,lastSpace;
        int    size;
        char   buf[] = new char[1];
        char   ch;

        fm = font().fontMetrics();
        charWidths = fm.widthsArray();

        index = 0;
        length = contents.length();
        firstIndex = index;
        currentWidth = 0;
        lastSpace = -1;

        while( index < length ) {
            ch = contents.charAt(index);
            if( ch == ' ' || ch == '\t' )
                lastSpace = index;
            if( ch == '\n' ) {
                result.addElement(contents.substring(firstIndex,index));
                index++;
                firstIndex = index;
                currentWidth = 0;
                lastSpace = -1;
            } else {
                if( ch < 256 )
                    currentWidth += charWidths[ch];
                else {
                    buf[0] = ch;
                    currentWidth += fm.stringWidth(new String(buf));
                }
                if( currentWidth > maxWidth ) {
                    if( index == firstIndex ) {/* One char per line minimum */
                        result.addElement(contents.substring(firstIndex,firstIndex+1));
                        index++;
                        firstIndex = index;
                    } if( lastSpace == -1 ) {
                        result.addElement(contents.substring(firstIndex,index));
                        firstIndex = index;
                    } else {
                        result.addElement(contents.substring(firstIndex,lastSpace));
                        firstIndex = lastSpace+1;
                        index = firstIndex;
                    }
                    currentWidth = 0;
                    lastSpace = -1;
                } else
                    index++;
            }
        }

        if( firstIndex < length) {
            result.addElement(contents.substring(firstIndex));
        }
        return result;
    }


    /** Draws the String at position (<b>x</b>, <b>y</b>).  You never call this
      * method directly, but should override to produce custom string drawing.
      */
    public void drawViewStringAt(Graphics g, String aString, int x, int y) {
        if (_shadowed) {
            g.setColor(Color.black);
            g.drawString(aString, x + 2, y + 2);
        }
        g.setColor(_textColor);
        g.drawString(aString, x, y);
    }

    int absoluteXOriginForStringWithWidth(int stringWidth) {
        int x;
        if (_justification == Graphics.RIGHT_JUSTIFIED)
            x = width() - border.rightMargin() - rightIndent()
                - stringWidth - _scrollOffset;
        else if (_justification == Graphics.CENTERED)
            x = border.leftMargin() + leftIndent() +
                ((width() - (border.widthMargin() + widthIndent())
                  - stringWidth) / 2) - _scrollOffset;
        else
            x = border.leftMargin() + leftIndent() - _scrollOffset;
        return x;
    }


    void drawViewLine(Graphics g, String aString, Size stringSize, int y) {
        int     x, offset;

        if (stringSize == null) {
            stringSize = _font.fontMetrics().stringSize(aString);
        }

        x = absoluteXOriginForStringWithWidth(stringSize.width);

        drawViewStringAt(g, aString, x, y);
    }

    /** Draws the portion of the TextField's interior specified by
      * <b>interiorRect</b>.  Calls <b>drawViewStringAt()</b> to draw the text.
      * You never call this method directly, but should override to produce
      * custom TextField drawing.
      */
    public void drawViewInterior(Graphics g, Rect interiorRect) {
        Vector          stringVector;
        String          contentString;
        Size            stringSize;
        Rect            caretRect;
        int             y, x1, x2, i, count, delta;

        /* get the baseline of the first line of text */
        y = baseline();

        /* clip to within bezel */
        g.pushState();
        g.setClipRect(interiorRect);

        /* draw the background */
        if (!isTransparent()) {
            /* we want different color for non-editable, but no way to
                * enforce it here and get the right behavior for people who
                * don't want to follow the "standard" look; developer should
                * explicitly set the background non-editable and selection
                * colors
                */
            g.setColor(_backgroundColor);
            g.fillRect(interiorRect);
        }

      /* draw the selection rectangle */
        if (_selectionAnchorChar != _selectionEndChar &&
            hasFocus && isSelectable()) {
            x1 = xPositionOfCharacter(selectionStart());
            x2 = xPositionOfCharacter(selectionStop());
            caretRect = caretRect();

            g.setColor(_selectionColor);
            g.fillRect(x1, caretRect.y, x2 - x1, caretRect.height);

            Rect.returnRect(caretRect);
        }

        /* draw the contents */
        g.setFont(_font);

        contentString = drawableString();

        stringSize = _font.fontMetrics().stringSize(contentString);

        if (!_canWrap || isEditable()) {
            drawViewLine(g, contentString, stringSize, y);
        } else {
            stringVector = stringVectorForContents(interiorRect.width);
            count = stringVector.count();

            if (count > 1) {
                y += (interiorRect.height - stringSize.height) / 2;
                delta = (interiorRect.height -
                                (int)(stringSize.height * count)) / 2;
                y -= delta + (count - 1) * stringSize.height;
            }
            for (i = 0; i < count; i++) {
                drawViewLine(g, (String)stringVector.elementAt(i), null, y);
                y += stringSize.height;
            }
        }

        if (isBeingEdited() && _caretShowing) {
            drawViewCaret(g);
        }
        g.popState();
    }

    /** Draws the TextField's border using its Border.  You never call this
      * method directly, but should override to produce custom border drawing.
      */
    public void drawViewBorder(Graphics g) {
        if (border != null) {
            border.drawInRect(g, 0, 0, width(), height());
            return;
        }

        // ALERT why drawing the background from here?

        if (!isTransparent() && _backgroundColor != null) {
            g.setColor(_backgroundColor);
            g.fillRect(0, 0, width(), height());
        }
    }

    /** Draws the TextField.  Calls <b>drawViewBorder()</b> and
      * <b>drawViewInterior()</b>.  You never call this method directly - call
      * the TextField's <b>draw()</b> method to draw the TextField.
      */
    public void drawView(Graphics g) {
        Rect    interiorRect;

        drawViewBorder(g);

        interiorRect = interiorRect();
        drawViewInterior(g, interiorRect);
        Rect.returnRect(interiorRect);
    }

    /* Redraws the area occupied by the caret. */
    void drawCaret() {
        Rect    caretRect;

        caretRect = caretRect();
        addDirtyRect(caretRect);
        Rect.returnRect(caretRect);
    }

    /* Hides the caret. */
    void hideCaret() {
        _caretShowing = false;
        drawCaret();
    }

    /* Shows the caret. */
    void showCaret() {
        _caretShowing = true;
        drawCaret();
    }

    /** Redraws the TextField's interior. */
    public void drawInterior() {
        Rect    interiorRect;

        interiorRect = interiorRect();
        addDirtyRect(interiorRect);
        Rect.returnRect(interiorRect);
    }



/* mouse events */
    /** Overriden to return <b>true</b>.
    public boolean wantsMouseTrackingEvents() {
        return true;
    }

    /** Overridden to process mouse Events within the TextField. */
    public boolean mouseDown(MouseEvent event) {
        boolean shouldHideCursor = true;
        Rect            redrawRect = null;
        boolean         caretWasShowing;
        Range r,wasSelected;

        _clickCount = event.clickCount();
        if( _clickCount > 3 )
            return true;

        if (!isSelectable()) {
            return false;
        }

        if (!isBeingEdited()) {
            if(isEditable())
                _startEditing(true);
            else if(isSelectable())   /** Should grab focus when only selectable
                                        * for selection display exclusion
                                        */
                _startEditing(false);
        }

        /** Calling start editing might change the focus.
         *  changing the focus might popup another window
         *  in this case we'll never see a mouse up.
         *  If the mouse is up we should not hide the
         *  cursor, we should just change the selection.
         */
        if(!rootView().mouseStillDown()) {
            int charNumber = charNumberForPoint(event.x);
            _clickCount = 0;
            selectRange(new Range(charNumber,0));
            if (!hasSelection() && isEditable()) {
                _caretShowing = _canBlink = true;
                drawCaret();
                _startBlinkTimer();
            }
            return true;
        }


        /** This should be performed after _startEditing to
         *  take in account this last change.
         */
        caretWasShowing = _caretShowing;

        _canBlink = _caretShowing = false;
        _mouseDragging = true;

        if (hasSelection()) {
            redrawRect = rectForRange(selectionStart(), selectionStop());
        } else if (caretWasShowing) {
            hideCaret();
        }

        wasSelected = selectedRange();

        if(event.isShiftKeyDown() && _clickCount == 1) {
            _selectionEndChar = charNumberForPoint(event.x);
            if( redrawRect != null )
                redrawRect.unionWith(rectForRange(selectionStart(),selectionStop()));
            else
                redrawRect = rectForRange(selectionStart(),selectionStop());
        } else {
            _selectionAnchorChar = _selectionEndChar = _initialAnchorChar =
              charNumberForPoint(event.x);
        }

        switch( _clickCount ) {
        case 2:
            r = groupForIndex(_selectionAnchorChar);
            if(!r.isNullRange()) {
              if(event.isShiftKeyDown()) {
                  Range otherRange = new Range(wasSelected);
                  otherRange.unionWith(r);
                  selectRange(otherRange);
              } else
                  selectRange(r);
            }
            redrawRect = null;
            break;
        case 3:
            selectRange(new Range(0,charCount()));
            redrawRect = null;
            break;
        default:
            break;
        }

        if (redrawRect != null) {
            addDirtyRect(redrawRect);
            Rect.returnRect(redrawRect);
        }

        return true;
    }

    void _computeScrollOffset() {
        Rect    caretRect,interiorRect;
        int     leftOffset, rightOffset;
        String  contentString;

        if (!isScrollable)  {
            _scrollOffset = 0;
            return;
        }

        leftOffset = border.leftMargin() + leftIndent();
        rightOffset = border.rightMargin() + rightIndent();

        caretRect = caretRect();
        interiorRect = interiorRect();

        contentString = drawableString();

        /* Always cancel any scrolling if the text now fit */
        if( (interiorRect.width - (leftIndent() + rightIndent())) >
            _font.fontMetrics().stringWidth(contentString)) {
            _scrollOffset = 0;
            return;
        }

        if (caretRect.x >= leftOffset &&
            caretRect.x < bounds.width - rightOffset) {
            Rect.returnRect(caretRect);

            return;
        }

        if (caretRect.x <  leftOffset) {
            _scrollOffset += caretRect.x - leftOffset;
        } else {
            _scrollOffset += caretRect.x - (bounds.width - rightOffset);
        }

        Rect.returnRect(caretRect);
    }

    /** Overridden to process mouse Events within the TextField. */
    public void mouseDragged(MouseEvent event) {
        Rect    redrawRect;
        Size    stringSize;
        int     oldOffset, offset, minOffset, maxOffset, sideWidths,
                stringWidth, oldEndChar, left, right, delta = 0;
        boolean shouldRefresh = true;

        if (!isSelectable())
            return;

        /* If clickCount > 2, we have selected everything. Nothing
         * should happend during mouseDrag
         */
        if( _clickCount > 2 )
            return;

        oldEndChar = _selectionEndChar;
        _selectionEndChar = charNumberForPoint(event.x);

        if (_clickCount == 2 ) { /* Per word selection dragging */
            Range startRange  = groupForIndex(_initialAnchorChar);
            Range endRange    = groupForIndex(_selectionEndChar);
            Range unionRange  = Range.rangeFromUnion(startRange,endRange);
            if(!unionRange.isNullRange()) {
                if( endRange.index > startRange.index ) {
                    _selectionAnchorChar = startRange.index;
                    _selectionEndChar    = endRange.index + endRange.length;

                } else {
                    _selectionAnchorChar = startRange.index +
                                           startRange.length;
                    _selectionEndChar    = endRange.index;
                }
            }
        }

        if (!containsPointInVisibleRect(event.x, 1)) {
            oldOffset = _scrollOffset;
            _computeScrollOffset();
            if(_scrollOffset != oldOffset ) {
                drawInterior();
                shouldRefresh = false;
            }
        }

        if (shouldRefresh && _selectionEndChar != oldEndChar) {
            if (_selectionEndChar < _selectionAnchorChar &&
                oldEndChar > _selectionAnchorChar ||
                _selectionEndChar > _selectionAnchorChar &&
                oldEndChar < _selectionAnchorChar) {
                left = selectionStart();
                right = selectionStop();
                if (oldEndChar < left) {
                    left = oldEndChar;
                }
                if (oldEndChar > right) {
                    right = oldEndChar;
                }
            } else {
                if (_selectionEndChar > oldEndChar) {
                    left = oldEndChar;
                    right = _selectionEndChar;
                } else {
                    left = _selectionEndChar;
                    right = oldEndChar;
                }
            }

            redrawRect = rectForRange(left, right);
            addDirtyRect(redrawRect);
            Rect.returnRect(redrawRect);
        }
    }

    /** Overridden to process mouse Events within the TextField. */
    public void mouseUp(MouseEvent event) {
        _mouseDragging = false;

        if (!hasSelection() && isEditable()) {
            _caretShowing = _canBlink = true;
            drawCaret();
        }

        _initialAnchorChar = -1;
        _clickCount = 0;
    }



/* key events */

    void _keyDown(KeyEvent event) {
        String          contentString;
        int             oldAnchor, oldOffset, start, condition;
        boolean         didChange, wasASelection;

        if (event.isReturnKey() || event.isTabKey() || event.isBackTabKey()) {
            if (event.isReturnKey()) {
                condition = TextFieldOwner.RETURN_KEY;
            } else if (event.isTabKey()) {
                condition = TextFieldOwner.TAB_KEY;
            } else {
                condition = TextFieldOwner.BACKTAB_KEY;
            }
            if (_owner != null &&
                !_owner.textEditingWillEnd(this, condition, _textChanged)) {
                return;
            }

            didChange = _textChanged;
            _completeEditing();

            if (_owner != null) {
                _owner.textEditingDidEnd(this, condition, didChange);
            }

            if (event.isBackTabKey()) {
                sendBacktabCommand();
                if( didChange )
                    sendCommitCommand();
            } else if (event.isTabKey()) {
                sendTabCommand();
                if( didChange )
                    sendCommitCommand();
            } else {
                sendCommitCommand();
            }

            return;
        } else if (event.isLeftArrowKey()) {
            if( event.isShiftKeyDown() ) {
              int oldScrollOffset = _scrollOffset;
              selectRange(_selectionAnchorChar,_selectionEndChar - 1);
              _computeScrollOffset();
              if( _scrollOffset != oldScrollOffset ) {
                  drawInterior();
              }
            } else {
                oldAnchor = _selectionAnchorChar;
                wasASelection = false;
                if (_selectionAnchorChar != _selectionEndChar) {
                    wasASelection = true;
                    _selectionAnchorChar = selectionStart();
                    oldAnchor = -1;
                } else if (_selectionAnchorChar > 0) {
                    hideCaret();
                    _selectionAnchorChar--;
                }
                _selectionEndChar = _selectionAnchorChar;

                if (oldAnchor != _selectionAnchorChar) {
                    oldOffset = _scrollOffset;
                    _computeScrollOffset();
                    if (oldOffset != _scrollOffset || wasASelection) {
                        _caretShowing = true;
                        drawInterior();
                    } else {
                        showCaret();
                    }
                }
            }
            return;
        } else if (event.isRightArrowKey()) {
            if( event.isShiftKeyDown() ) {
              int oldScrollOffset = _scrollOffset;
              selectRange(_selectionAnchorChar,_selectionEndChar + 1);
              _computeScrollOffset();
              if( _scrollOffset != oldScrollOffset ) {
                  drawInterior();
              }
            } else {
                oldAnchor = _selectionAnchorChar;
                wasASelection = false;
                if (_selectionAnchorChar != _selectionEndChar) {
                  wasASelection = true;
                  _selectionAnchorChar = selectionStop();
                  oldAnchor = -1;
                } else if (_selectionAnchorChar < _contents.length()) {
                  hideCaret();
                  _selectionAnchorChar++;
                  if (_selectionAnchorChar > _contents.length()) {
                    _selectionAnchorChar = _contents.length();
                  }
                }
                _selectionEndChar = _selectionAnchorChar;

                if (oldAnchor != _selectionAnchorChar) {
                  oldOffset = _scrollOffset;
                  _computeScrollOffset();
                  if (oldOffset != _scrollOffset || wasASelection) {
                    _caretShowing = true;
                    drawInterior();
                  } else {
                    showCaret();
                  }
                }
            }
            return;
        } else if(event.isHomeKey()) {
            Range r = selectedRange();
            int oldScrollOffset;
            if( event.isShiftKeyDown() )
              selectRange(_selectionAnchorChar,0);
            else
              selectRange(new Range(0,0));

            oldScrollOffset = _scrollOffset;
            _computeScrollOffset();
            if( _scrollOffset != oldScrollOffset )
              drawInterior();
            return;
        } else if(event.isEndKey())  {
            Range r = selectedRange();
            int oldScrollOffset;
            int lastIndex = _contents.length();

            if( event.isShiftKeyDown())
                selectRange(_selectionAnchorChar,lastIndex);
            else
              selectRange(new Range(lastIndex,0));

            oldScrollOffset = _scrollOffset;
            _computeScrollOffset();
            if( _scrollOffset != oldScrollOffset )
              drawInterior();
            return;
        } else if (!event.isBackspaceKey() &&
                   !event.isDeleteKey() &&
                   !event.isPrintableKey()) {
            return;
        }

        if (_oldContents == null) {
            _oldContents = new FastStringBuffer(_contents.toString());
        }

        hideCaret();

        /* delete the selection */
        if (_selectionAnchorChar != _selectionEndChar) {
            contentString = _contents.toString();

            start = selectionStart();

            _contents = new FastStringBuffer(
                                        contentString.substring(0, start));
            _contents.append(contentString.substring(selectionStop()));
            _selectionAnchorChar = _selectionEndChar = start;

            if (event.isBackspaceKey() || event.isDeleteKey()) {
                event = null;
            }
        }

        if (event != null) {
           if (event.isBackspaceKey()) {
                if (_contents.length() == 0 || _selectionAnchorChar == 0) {
                    showCaret();
                    return;
                }
                _contents.removeCharAt(_selectionAnchorChar - 1);
                _selectionAnchorChar--;
            } else if(event.isDeleteKey()) {
                if(_selectionAnchorChar < _contents.length())
                  _contents.removeCharAt(_selectionAnchorChar);
                else
                  showCaret();
            } else {
                _contents.insert((char)event.key, _selectionAnchorChar++);
            }

            _selectionEndChar = _selectionAnchorChar;
        }

        _computeScrollOffset();

        drawInterior();
        showCaret();
        if (_owner != null) {
            _owner.textWasModified(this);
            _textChanged = true;
        } else if (!_textChanged) {
            _textChanged = true;
        }
    }

    /** Overridden to process key Events within the TextField. If you want
      * to process or filter key Events yourself, implement the TextFilter
      * interface and set yourself as the TextField's filter.
      * @see #setFilter
      */
    public void keyDown(KeyEvent event) {
        KeyEvent        nextKey;

        if( !isEditable())
            return;

        if (_filter != null) {
            if (_filter.acceptsEvent(this, event, _keyVector)) {
                _keyVector.addElement(event);
            }
        } else {
            _keyVector.addElement(event);
        }

        while (!_keyVector.isEmpty()) {
            nextKey = (KeyEvent)_keyVector.removeFirstElement();
            _keyDown(nextKey);
        }
    }

    /** Override this method to start an editing session if necessary */
    public void setFocusedView() {
        if( _editing == false && isEditable() )
            _startEditing(true); /* Will call setFocusedView with _editing set to true*/
        else
            super.setFocusedView();
    }

    /* start editing */
    private void _startEditing(boolean sendMessage) {
        if (_superview != null) {
            if( isEditable() )
                _canBlink = _caretShowing = hasInsertionPoint();
            else
                _canBlink = _caretShowing = false;

            _editing = true;

            if(isSelectable())
                this.setFocusedView();

            if (hasInsertionPoint() && isEditable()) {
                showCaret();
            }
            if (sendMessage && _owner != null) {
                _owner.textEditingDidBegin(this);
            }
        }
    }

    /* stop all editing, saving changes */
    private void _completeEditing() {
        boolean         shouldPerformAction;

        _oldContents = null;

        shouldPerformAction = _textChanged;

        _editing = false;

        if (_superview != null) {
            _ignoreWillBecomeSelected = true;
            _superview.setFocusedView(null);
            _ignoreWillBecomeSelected = false;
        }

        if (shouldPerformAction) {
            sendContentsChangedCommand();
        }
    }

    void _startBlinkTimer() {
        if (blinkTimer == null) {
            blinkTimer = new Timer(this, BLINK_CARET, 750);
            blinkTimer.start();
        }
    }

    /** Make sure that the selection is defined */
    void _validateSelection() {
        String contents = stringValue();
        Range r;

        if( _selectionAnchorChar == -1 )
            selectRange( new Range(0,0));
        else {
            if( _selectionAnchorChar < 0 )
                _selectionAnchorChar = 0;
            else if( _selectionAnchorChar > _contents.length())
                _selectionAnchorChar = _contents.length();

            if( _selectionEndChar < 0 )
                _selectionEndChar = 0;
            else if( _selectionEndChar > _contents.length())
                _selectionEndChar = _contents.length();
        }
    }

    /** Overridden to notify the TextField it has become the focus of
      * KeyEvents.
      */
    public void startFocus() {
        _validateSelection();
        hasFocus = true;
        if( isEditable())
            _startBlinkTimer();
        setDirty(true);
    }

    /** Overridden to notify the TextField it has ceased being the focus of
      * KeyEvents.
      */
    public void stopFocus() {
        if (blinkTimer != null) {
            blinkTimer.stop();
            blinkTimer = null;
        }

        hasFocus = false;
        _scrollOffset = 0;

        /* if editing, we didn't cause the loss of focus */
        if (_editing && _owner != null && isEditable()) {
            _owner.textEditingWillEnd(this, TextFieldOwner.LOST_FOCUS,
                                      _textChanged);
        }

        _caretShowing = _canBlink = false;

        if (_editing && isEditable()) {
            if (_owner != null) {
                _owner.textEditingDidEnd(this, TextFieldOwner.LOST_FOCUS,
                                         _textChanged);
            }

            if (_textChanged) {
                sendCommitCommand();
            }
        }
        _editing = _textChanged = false;

        drawInterior();
    }


    /** Overridden to notify the TextField that it has ceased being the focus
      * of KeyEvents, but that it will regain focus when the user clicks on
      * its InternalWindow.
      */
    public void pauseFocus() {
        if (blinkTimer != null) {
            blinkTimer.stop();
            blinkTimer = null;
            hideCaret();
        }
    }

    /** Overridden to notify the TextField it has become the focus of
      * KeyEvents, because the user clicked on its InternalWindow.
      */
    public void resumeFocus() {
      if(isEditable())
        _startBlinkTimer();
    }

   /** Causes the TextField to cancel editing by discarding any changes
     * that the user made.
     */
    public void cancelEditing() {
        if (!isBeingEdited()) {
            return;
        }

        if (_oldContents != null) {
            _contents = _oldContents;
            _oldContents = null;
        }

        /* cause stopFocus() to get called */
        _editing = false;
        if (_superview != null) {
            _ignoreWillBecomeSelected = true;
            _superview.setFocusedView(null);
            _ignoreWillBecomeSelected = false;
        }
    }

    /** Causes the TextField to complete any editing by retaining all
      * changes the user made to the TextField's text.
      * @see #cancelEditing
      */
    public void completeEditing() {
        boolean textDidChange = _textChanged;

        if (!isBeingEdited()) {
            return;
        }

        if (_owner != null &&
            !_owner.textEditingWillEnd(this, TextFieldOwner.RESIGNED_FOCUS,
                                       textDidChange)) {
            return;
        }

        _completeEditing();

        if (_owner != null) {
            _owner.textEditingDidEnd(this, TextFieldOwner.RESIGNED_FOCUS,
                                     textDidChange);
        }
    }



/* commands */


    void sendCommand(String command, Target aTarget) {
        if (aTarget != null) {
            /*if (!(aTarget instanceof TextField) ||
                !command.equals(SELECT_TEXT) ||
                (rootView() != null && rootView().focusedView() == null)) */
            aTarget.performCommand(command, this);
        }
    }

    void sendTabCommand() {
        if(_tabCommand != null && _tabTarget != null)
            sendCommand(_tabCommand, _tabTarget);
        else if(rootView() != null)
            rootView().selectViewAfter(this);
    }

    void sendBacktabCommand() {
        if(_backtabCommand != null && _backtabCommand != null)
            sendCommand(_backtabCommand, _backtabTarget);
        else if(rootView() != null)
            rootView().selectViewBefore(this);
    }

    void sendContentsChangedCommand() {
        sendCommand(_contentsChangedCommand, _contentsChangedTarget);
    }

    void sendCommitCommand() {
        if (_commitCommand == null && _commitTarget == null &&
            _tabCommand != null && _tabTarget != null) {
            sendTabCommand();
        }
        sendCommand(_commitCommand, _commitTarget);
    }

    /** Implements the TextField's ExtendedTarget interface */
    public boolean canPerformCommand(String command) {
        return (BLINK_CARET.equals(command) ||
                SELECT_TEXT.equals(command) ||
                (isEditable() && CUT.equals(command)) ||
                COPY.equals(command) ||
                (isEditable() && PASTE.equals(command)));
    }

    /** Implements the TextField's commands:
      * <ul>
      * <li>SELECT_TEXT - calls the <b>selectText()</b> method.
      * </ul>
      * @see #selectText
      */
    public void performCommand(String command, Object data) {
        if (BLINK_CARET.equals(command)) {
            blinkCaret();
        } else if (SELECT_TEXT.equals(command)) {
            selectText();
        } else if (CUT.equals(command)) {
            cut();
        } else if (COPY.equals(command)) {
            copy();
        } else if (PASTE.equals(command)) {
            paste();
        } else {
            throw new NoSuchMethodError("unknown command: " + command);
        }
    }

    private void blinkCaret() {
        if (_canBlink) {
            _caretShowing = !_caretShowing;
            drawCaret();
        } else if (!_mouseDragging && hasInsertionPoint()) {
            _canBlink = true;
        }
    }

/* archiving */


    /** Describes the TextField class' information.
     * @see Codable#describeClassInfo
     */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.TextField", 3);
        info.addField(CONTENTS_KEY, STRING_TYPE);
        info.addField(OWNER_KEY, OBJECT_TYPE);
        info.addField(FILTER_KEY, OBJECT_TYPE);
        info.addField(TABREC_KEY, OBJECT_TYPE);
        info.addField(BACKTABREC_KEY, OBJECT_TYPE);
        info.addField(CONTENTSCHANGEDREC_KEY, OBJECT_TYPE);
        info.addField(COMMITREC_KEY, OBJECT_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(FONT_KEY, OBJECT_TYPE);
        info.addField(TEXTC_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDC_KEY, OBJECT_TYPE);
        info.addField(SELECTIONC_KEY, OBJECT_TYPE);
        info.addField(CARETC_KEY, OBJECT_TYPE);
        info.addField(TABCOM_KEY, STRING_TYPE);
        info.addField(BACKTABCOM_KEY, STRING_TYPE);
        info.addField(CONTENTSCHANGEDCOM_KEY, STRING_TYPE);
        info.addField(COMMITCOM_KEY, STRING_TYPE);
        info.addField(SELECTIONANCH_KEY, INT_TYPE);
        info.addField(SELECTIONEND_KEY, INT_TYPE);
        info.addField(JUSTIFICATION_KEY, INT_TYPE);
        info.addField(SCROLLOFFSET_KEY, INT_TYPE);
        info.addField(EDITABLE_KEY, BOOLEAN_TYPE);
        info.addField(SELECTABLE_KEY, BOOLEAN_TYPE);
        info.addField(SHADOWED_KEY, BOOLEAN_TYPE);
        info.addField(CANWRAP_KEY, BOOLEAN_TYPE);
        info.addField(AUTOSCROLLEVENT_KEY, BOOLEAN_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
        info.addField(DRAWABLE_CHAR_KEY,CHAR_TYPE);
        info.addField(SCROLLABLE_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the TextField instance.
     * @see Codable#decode
     */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeString(CONTENTS_KEY, _contents.toString());

        encoder.encodeObject(OWNER_KEY, (Codable)_owner);
        encoder.encodeObject(FILTER_KEY, (Codable)_filter);

        encoder.encodeObject(TABREC_KEY, (Codable)_tabTarget);
        encoder.encodeObject(BACKTABREC_KEY, (Codable)_backtabTarget);
        encoder.encodeObject(CONTENTSCHANGEDREC_KEY,
                               (Codable)_contentsChangedTarget);
        encoder.encodeObject(COMMITREC_KEY, (Codable)_commitTarget);

        if (border instanceof EmptyBorder) {
            encoder.encodeObject(BORDER_KEY, null);
        } else {
            encoder.encodeObject(BORDER_KEY, border);
        }

        encoder.encodeObject(FONT_KEY, _font);

        encoder.encodeObject(TEXTC_KEY, _textColor);
        encoder.encodeObject(BACKGROUNDC_KEY, _backgroundColor);
        encoder.encodeObject(SELECTIONC_KEY, _selectionColor);
        encoder.encodeObject(CARETC_KEY, _caretColor);

        encoder.encodeString(TABCOM_KEY, _tabCommand);
        encoder.encodeString(BACKTABCOM_KEY, _backtabCommand);
        encoder.encodeString(CONTENTSCHANGEDCOM_KEY,
                               _contentsChangedCommand);
        encoder.encodeString(COMMITCOM_KEY, _commitCommand);

        encoder.encodeInt(SELECTIONANCH_KEY, _selectionAnchorChar);
        encoder.encodeInt(SELECTIONEND_KEY, _selectionEndChar);
        encoder.encodeInt(JUSTIFICATION_KEY, _justification);
        encoder.encodeInt(SCROLLOFFSET_KEY, _scrollOffset);

        encoder.encodeBoolean(EDITABLE_KEY, _editable);
        encoder.encodeBoolean(SELECTABLE_KEY, _selectable);
        encoder.encodeBoolean(SHADOWED_KEY, _shadowed);
        encoder.encodeBoolean(CANWRAP_KEY, _canWrap);
        encoder.encodeBoolean(AUTOSCROLLEVENT_KEY, wantsAutoscrollEvents);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);

        encoder.encodeChar(DRAWABLE_CHAR_KEY,_drawableCharacter);
        encoder.encodeBoolean(SCROLLABLE_KEY, isScrollable);
    }

    /** Decodes the TextField instance.
     * @see Codable#decode
     */
    public void decode(Decoder decoder) throws CodingException {
        String          contentString;
        int version = decoder.versionForClassName("netscape.application.TextField");

        super.decode(decoder);

        contentString = decoder.decodeString(CONTENTS_KEY);
        if (contentString != null) {
            _contents = new FastStringBuffer(contentString);
        }

        _owner = (TextFieldOwner)decoder.decodeObject(OWNER_KEY);
        _filter = (TextFilter)decoder.decodeObject(FILTER_KEY);

        _tabTarget = (Target)decoder.decodeObject(TABREC_KEY);
        _backtabTarget = (Target)decoder.decodeObject(BACKTABREC_KEY);
        _contentsChangedTarget =
                    (Target)decoder.decodeObject(CONTENTSCHANGEDREC_KEY);
        _commitTarget = (Target)decoder.decodeObject(COMMITREC_KEY);

        setBorder((Border)decoder.decodeObject(BORDER_KEY));

        _font = (Font)decoder.decodeObject(FONT_KEY);

        _textColor = (Color)decoder.decodeObject(TEXTC_KEY);
        _backgroundColor = (Color)decoder.decodeObject(BACKGROUNDC_KEY);
        _selectionColor = (Color)decoder.decodeObject(SELECTIONC_KEY);
        _caretColor = (Color)decoder.decodeObject(CARETC_KEY);

        _tabCommand = decoder.decodeString(TABCOM_KEY);
        _backtabCommand = decoder.decodeString(BACKTABCOM_KEY);
        _contentsChangedCommand = decoder.decodeString(CONTENTSCHANGEDCOM_KEY);
        _commitCommand = decoder.decodeString(COMMITCOM_KEY);

        _selectionAnchorChar = decoder.decodeInt(SELECTIONANCH_KEY);
        _selectionEndChar = decoder.decodeInt(SELECTIONEND_KEY);
        _justification = decoder.decodeInt(JUSTIFICATION_KEY);
        _scrollOffset = decoder.decodeInt(SCROLLOFFSET_KEY);

        _editable = decoder.decodeBoolean(EDITABLE_KEY);
        _selectable = decoder.decodeBoolean(SELECTABLE_KEY);
        _shadowed = decoder.decodeBoolean(SHADOWED_KEY);
        _canWrap = decoder.decodeBoolean(CANWRAP_KEY);
        wantsAutoscrollEvents = decoder.decodeBoolean(AUTOSCROLLEVENT_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);

        if( version >= 2 ) {
            _drawableCharacter = decoder.decodeChar(DRAWABLE_CHAR_KEY);
        } else
            _drawableCharacter = ANY_CHARACTER;

        if( version >= 3 ) {
            isScrollable = decoder.decodeBoolean(SCROLLABLE_KEY);
        } else
            isScrollable = true;

    }

    /** Finishes the TextField instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();

        setFont(_font);
    }

    boolean isWordCharacter(char c) {
        if( c >= '0' && c <= '9' ||
            c >= 'A' && c <= 'Z' ||
            c >= 'a' && c <= 'z' )
            return true;
        else
            return false;
    }


    Range groupForIndex(int index) {
        int length = charCount();
        int relFirstIndex,relLastIndex;
        int i,l;
        char c;

        if( length == 0 )
            return new Range();

        if( _drawableCharacter != ANY_CHARACTER )
            return new Range(0,charCount());

        if( index < 0 )
            index = 0;
        else if( index >= length )
            index = length-1;

        i = index;
        c = _contents.charAt(i);

        if( c == '\n') {
            return new Range(i,1);
        }

        if( c == ' ' || c == '\t' ) {
            while(i>0) {
                c = _contents.charAt(i);
                if( c == ' ' || c == '\t' )
                    i--;
                else
                    break;
            }
            relFirstIndex = i+1;

            i = index;

            while(i<length) {
                c = _contents.charAt(i);
                if( c == ' ' || c == '\t')
                    i++;
                else
                    break;
            }
            relLastIndex = i-1;
            return new Range(relFirstIndex,relLastIndex - relFirstIndex +1);
        }

        if( !isWordCharacter(c) ) {
            return new Range( index, 1);
        }

        relFirstIndex = i;
        while( relFirstIndex > 0 ) {
            c = _contents.charAt(relFirstIndex-1);
            if(!isWordCharacter(c))
                break;
            relFirstIndex--;
        }


        relLastIndex = i;
        while( relLastIndex < (length-1)) {
            c = _contents.charAt(relLastIndex+1);
            if(!isWordCharacter(c))
                break;
            relLastIndex++;
        }

        return new Range(relFirstIndex, relLastIndex - relFirstIndex + 1);
    }

    /** Return the string that should be displayed for a given
     *  password */
    private String drawableString() {
        if( _drawableCharacter == ANY_CHARACTER ) {
            if(_contents != null)
                return _contents.toString();
            else
                return "";
        }

        if( _contents != null && _contents.length() > 0 ) {
            char buf[] = new char[_contents.length()];
            int i,c;

            for(i=0,c=buf.length ; i < c ; i++)
                buf[i] = _drawableCharacter;

            return new String(buf);
        } else
            return "";
    }

    /** Inform the view that it is about to become the selected view
      *
      */
    public void willBecomeSelected() {
        if(!_ignoreWillBecomeSelected) {
            selectText();
        }
    }

    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * Returns <b>true</b>
      *
      */
    public boolean canBecomeSelectedView() {
        if(isEditable())
            return true;
        else
            return false;
    }

    /** Return the View that should become selected when the user
      * press the tab key. If the result is null, the keyboard UI
      * system will select the next available view.
      * The default implementation returns the tabField.
      *
      */
    public View nextSelectableView() {
        if(_tabTarget != null && (_tabTarget instanceof View)) /* Paranoia */
            return (View)_tabTarget;
        else
            return null;
    }

    /** Return the View that should become selected when the user
      * press the backtab key. If the result is null, the keyboard UI
      * system will select the next available view.
      * The default implementation return the backtabField.
      *
      */
    public View previousSelectableView() {
        if(_backtabTarget != null && (_backtabTarget instanceof View)) /* Paranoia */
            return (View)_backtabTarget;
        else
            return null;
    }

    /** Copys the current selection.
      *
      */
    public void copy() {
        Application.setClipboardText(stringForRange(selectedRange()));
    }

    /** Cuts the current selection.
      *
      */
    public void cut() {
        if (isEditable()) {
            Range range = selectedRange();

            if(range == null || range.index < 0)
                return;

            Application.setClipboardText(stringForRange(range));
            replaceRangeWithString(range, "");
            selectRange(new Range(range.index(), 0));
        }
    }

    /** Replaces the current selection.
      *
      */
    public void paste() {
        if (isEditable()) {
            Range range = selectedRange();
            String text = Application.clipboardText();

            if(range == null || text == null)
                return;

            replaceRangeWithString(range, text);
            selectRange(new Range(range.index() + text.length(), 0));
        }
    }

    /** By default TextFields will scroll as the user types, so they can see
      * what they are typing. This flag indicates if this scrolling should
      * occur. If <b>isScrollable</b> returns false, the content area that
      * the user is typing in will not be scrolled. Additionally, if user
      * drags a selection, the selection will not cause the text to scroll.
      *
      */
    public boolean isScrollable()  {
        return isScrollable;
    }

    /** By default TextFields will scroll as the user types, so they can see
      * what they are typing. This flag indicates if this scrolling should
      * occur.
      *
      */
    public void setScrollable(boolean value) {
        isScrollable = value;
    }

    /** Implementation of the FormElement interface
      *
      */
    public String formElementText() {
        return stringValue();
    }

}

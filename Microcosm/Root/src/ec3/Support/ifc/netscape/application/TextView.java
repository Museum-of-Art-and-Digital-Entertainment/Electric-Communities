// TextView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;
import java.io.*;
import java.net.*;

/* drawtext variable needs mutex protection */

/** View subclass that displays zero or more paragraphs of multi-font text
  * or embedded Images.  TextView stores the displayed text as a collection
  * of Strings and a Hashtable that describes the attributes associated with
  * each String.  Each Hashtable applies to a character range. A character
  * range containing the same attributes is called a "run." Paragraphs
  * are defined as collections of runs ending with a carriage return.<p>
  * Any attribute can be associated with a character range, however
  * TextView defines some special attributes that are interpreted by the
  * layout engine when formatting and displaying the text.  When one or more
  * characters are inserted into the TextView by typing or by using the
  * insertion methods, the following rules apply to determine how attributes
  * apply to the new ranges:<OL>
  * <LI>If there is a run after the inserted range, the inserted characters'
  * attributes will be <b>defaultAttributes()</b> UNION the following run's
  * attributes.
  * <LI>If a run appears before the inserted range, the inserted characters'
  * attributes will be <b>defaultAttributes()</b> UNION the previous run's
  * attributes.
  * <LI>If the previous two conditions don't apply, the inserted characters'
  * attributes will be <b>defaultAttributes()</b>.
  * </OL>
  * Finally, if some typingAttributes have been set, typing attributes are
  * applied to what have been typed. Typing attributes are used only once,
  * after the normal attributes propagation mechanism applies.
  * @note 1.0 It is now possible to use views as TextAttachments.
  *           TextAttachment provides some new notifications so the
  *           attachment subclass can know when to add a view to view
  *           hierarchy, update it's bounds and remove it from the view
  *           hierarchy.
  * @note 1.0 It is now possible to extend HTML parsing so TextView supports
  *           new tags when parsing HTML.
  * @note 1.0 Some performance improvements.
  * @note 1.0 Added FormElement interface for browser needs
  * @note 1.0 filterEvents bug fixed.
  */
public class TextView extends View implements ExtendedTarget, EventFilter, DragDestination, FormElement {

    /** String used to store a TextAttachment instance in the text.
     * @see TextAttachment
     */
    public final static String TEXT_ATTACHMENT_STRING = "@";

    /** ParagraphFormat attribute. Value is a ParagraphFormat instance. */
    public final static String PARAGRAPH_FORMAT_KEY = "ParagraphFormatKey";

    /** Font attribute.  Value is a Font instance. */
    public final static String FONT_KEY = "FontKey";

    /** TextColor attribute. Value is a Color instance. */
    public final static String TEXT_COLOR_KEY = "TextColorKey";

    /** TextAttachment attribute. A TextAttachment is an attachment, and this
      * attribute is generally set on a single character. The character will be
      * replaced by the TextAttachment
      * This attributes is static. It is not propagated while typing and
      * cannot be used as a typing attribute.
      */
    public final static String TEXT_ATTACHMENT_KEY = "TextAttachmentKey";

    /** TextAttachment baseline offset attribute. The value of this attribute
      * is the distance between the bottom of the attachment and the text
      * baseline. This attribute makes sense only when used with
      * <B>TEXT_ATTACHMENT_KEY</B>.  The distance is stored as an
      * <B>Integer</B> instance and is a number of pixels.
      *  The default value of this attribute is 0.
      * This attributes is static. It is not propagated while typing and
      * cannot be used as a typing attribute.
      */
    public final static String TEXT_ATTACHMENT_BASELINE_OFFSET_KEY =
                                        "TextAttachmentBaselineOffsetKey";


    /** Caret Color attribute. */
    public final static String CARET_COLOR_KEY = "CaretColorKey";

    /** "Link" attribute. A range that has this attribute will tell the
      * <b>TextViewOwner</b>
      * to follow the link when the users clicks the region. The value is a
      * string, containing an URL.
      * This attributes is static. It is not propagated while typing and
      * cannot be used as a typing attribute.
     */
    public final static String LINK_KEY = "LinkKey";

    /** Local link destination attribute. A range that has this attribute
      * will be marked as the destination a link. The value is the link's
      * name.
      * This attributes is static. It is not propagated while typing and
      * cannot be used as a typing attribute.
      */
    public final static String LINK_DESTINATION_KEY = "LinkDestinationKey";

    /** Link color attribute. Usually, you use this attribute
      * only on the default attribute. You can however use it on a single link.
      */
    public final static String LINK_COLOR_KEY = "LinkColorKey";

    /** Link pressed attribute. */
    public final static String PRESSED_LINK_COLOR_KEY = "PressedLinkColorKey";

    /** Flag to indicate that the link has been pressed
      * @private
      */
    final static String LINK_IS_PRESSED_KEY = "_IFCLinkPressedKey";

    /** Vector of attributes changing text formatting when changed */
    static Vector attributesChangingFormatting;

    static {
        attributesChangingFormatting = new Vector();
        attributesChangingFormatting.addElement(TEXT_ATTACHMENT_KEY);
        attributesChangingFormatting.addElement(TEXT_ATTACHMENT_BASELINE_OFFSET_KEY);
        attributesChangingFormatting.addElement(FONT_KEY);
        attributesChangingFormatting.addElement(PARAGRAPH_FORMAT_KEY);
    }

    Vector              _paragraphVector;
    Color               _backgroundColor, _selectionColor;
    TextParagraph       _updateParagraph;
    TextPositionInfo    _anchorInfo, _upInfo;
    TextSelection       _selection;
    TextFilter          _filter;
    TextViewOwner       _owner;
    Hashtable           _defaultAttributes;
    Hashtable           _typingAttributes;
    Timer               _updateTimer;
    Vector              _eventVector;
    int                 _charCount, _paragraphSpacing, _updateLine, _downY,
                        _clickCount, _resizeDisabled,_formattingDisabled;
    boolean             _drawText = true, _editing,
                        _useSingleFont = false, _editable = true,
                        _selectable = true, _drawNextParagraph,
                        _resizing = false, insertionPointVisible = false,
                        transparent = false,selectLineBreak;

    private Range       _selectedRange; /* This is a cache and is not always up to date. use selectedRange()
                                           the selected range */
    private Range       _wasSelectedRange;

    private TextAttachment    _mouseDownTextAttachment;   /* Used only during a mouseDown->mouseDrag->mouseUp session
                                                             the text item under the mouse */
    private Point       _mouseDownTextAttachmentOrigin; /* the text item origin in the textview */
    private FontMetrics _defaultFontMetricsCache; /* Cache default font font metrics */

    private URL         _baseURL;                 /* Base URL. Used only while parsing HTML */
    private Range       _clickedRange;            /* Range of the clicked link */
    private Range       _firstRange;              /* Use while selecting per words/line/paragraph*/

    private HTMLParsingRules  _htmlParsingRules;              /* HTML parsing rules */
    private int notifyAttachmentDisabled = 0;       /* If > 0 do not notify attachment. Store changed range into: */
    private Range invalidAttachmentRange = null;

    static private Vector    _rectCache = new Vector();
    static private Vector    _vectorCache = new Vector();
    static private boolean      _shouldCache = false, _cacheVectors = false;
    static         ObjectPool hashtablePool = new ObjectPool("netscape.util.Hashtable",32);
    static         ObjectPool rangePool     = new ObjectPool("netscape.application.Range",32);

    static final String         PARAGRAPHVECTOR_KEY = "paragraphVector";
    static final String         BACKGROUNDCOLOR_KEY = "backgroundColor";
    static final String         SELECTIONCOLOR_KEY = "selectionColor";
    static final String         FILTER_KEY = "filter";
    static final String         DEFAULTATTRIBUTES_KEY = "defaultAttributes";
    static final String         PARASPACING_KEY = "paragraphSpacing";
    static final String         USESINGLEFONT_KEY = "useSingleFont";
    static final String         EDITABLE_KEY = "editable";
    static final String         SELECTABLE_KEY = "selectable";
    static final String         TRANSPARENT_KEY = "transparent";
    static final String         HTML_PARSING_RULES_KEY = "htmlParsingRules";
    static final String         OWNER_KEY = "owner";

    /* constructors */

    /** Constructs a TextView. */
    public TextView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a TextView with bounds <B>rect</B>.
      */
    public TextView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a TextView with bounds
      * (<b>x</b>, <b>y</b>, <b>width</b>, <b>height</b>).
      */
    public TextView(int x, int y, int width, int height) {

        super(x, y, width, height);

        TextParagraph   newParagraph;
        TextParagraphFormat defaultFormat;
        int i;

        _eventVector = new Vector();
        _selection = new TextSelection(this);

        _paragraphVector = new Vector();
        _paragraphSpacing = 0;
        _backgroundColor = Color.white;
        _selectionColor = Color.lightGray;

        _defaultAttributes = new Hashtable();
        _defaultAttributes.put(FONT_KEY,Font.defaultFont());
        _defaultAttributes.put(TEXT_COLOR_KEY,Color.black);
        _defaultAttributes.put(LINK_COLOR_KEY,Color.blue);
        _defaultAttributes.put(CARET_COLOR_KEY,Color.black);
        _defaultAttributes.put(PRESSED_LINK_COLOR_KEY,Color.red);
        defaultFormat = new TextParagraphFormat();
        defaultFormat.setLeftMargin(3);
        defaultFormat.setRightMargin(3);
        defaultFormat.setJustification(Graphics.LEFT_JUSTIFIED);

        int tabPos = 30;
        for(i=0;i < 20 ; i++ ) {
            defaultFormat.addTabPosition(tabPos);
            tabPos += 30;
        }

        _defaultAttributes.put(PARAGRAPH_FORMAT_KEY, defaultFormat);
        _wasSelectedRange = new Range(selectedRange());
        newParagraph = new TextParagraph(this);
        newParagraph.addRun(new TextStyleRun(newParagraph, "", null));
        addParagraph(newParagraph);
        reformatAll();

        _typingAttributes = new Hashtable();
    }

    /** Overridden to take special action when the TextView moves. */
    public void didMoveBy(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0 && _updateTimer != null) {
            _updateTimer.stop();
            _updateTimer = null;
            _updateParagraph = null;
        }

        super.didMoveBy(deltaX, deltaY);
    }

    /** Overridden to catch size changes. */
    public void sizeBy(int deltaWidth, int deltaHeight) {
        int origWidth;
        if (!isResizingEnabled()) {
            return;
        }
        origWidth = bounds.width;
        _resizing = true;
        super.sizeBy(deltaWidth, deltaHeight);
        _resizing = false;

        if( bounds.width != (origWidth + deltaWidth )) {
          /* This can happen if the scrollbar has been added */
            disableResizing();
            reformatAll();
            enableResizing();
            setDirty(true);
        } else if( deltaWidth != 0 || deltaHeight != 0 ){
            setDirty(true);
        }
    }

    /** Overridden to catch size changes. */
    public void didSizeBy(int dw, int dh) {
        if (!_resizing) {
            reformatAll();
        }
        super.didSizeBy(dw, dh);
    }

    /** Sets the TextView to be transparent or opaque.
      */
    public void setTransparent(boolean flag) {
        transparent = flag;
    }

    /** Overridden to return <b>true</b> if the TextView is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Overridden to return <b>true</b> - TextViews can be autoscrolled. */
    public boolean wantsAutoscrollEvents() {
        return true;
    }

    /** Draws the TextView's contents. */
    public void drawView(Graphics g) {
        TextParagraph   nextParagraph, paragraph;
        Rect            insertionRect, rect;
        int             minY, maxY, count, i;

        if (!_drawText) {
            if (_updateParagraph != null) {
                _updateParagraph.drawLine(g, _updateLine);
            }
        } else {
            count = _paragraphVector.count();
            minY = g.clipRect().y;
            maxY = g.clipRect().maxY();
            rect = Rect.newRect(0, 0, width(), height());
            for (i = 0; i < count; i++) {
                nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
                if (nextParagraph._y > maxY ||
                    (nextParagraph._y + nextParagraph._height) < minY) {
                    continue;
                }
                nextParagraph.drawView(g, rect);
            }
            Rect.returnRect(rect);
        }

        if( _selection._insertionPointShowing ) {
            Hashtable attr;
            TextStyleRun run;
            TextPositionInfo insertionPointInfo;
            Rect r = _selection.insertionPointRect();
            Color caretColor = null;

            if( g.clipRect().intersects( r )) {
                insertionPointInfo = _selection.insertionPointInfo();
                run = _runForIndex( insertionPointInfo._absPosition);
                if( (attr = run.attributes()) != null)
                    caretColor = (Color) attr.get(CARET_COLOR_KEY);


                if( caretColor == null )
                    caretColor = (Color) _defaultAttributes.get(
                                                            CARET_COLOR_KEY);

                if( caretColor == null )
                    caretColor = Color.black;

                g.setColor( caretColor );
                g.fillRect(r);
            }
        }
    }

    /** Implemented to allow TextViews to remove KeyEvents from the
      * EventLoop.
      */
    public Object filterEvents(Vector events) {
        int i;

        for(i = 0; i < events.count(); i++) {
            Event event = (Event)events.elementAt(i);

            if ((event instanceof KeyEvent) &&
                (event.type() == KeyEvent.KEY_DOWN)) {
                if (_filter != null) {
                    if (_filter.acceptsEvent(this, (KeyEvent)event,
                                             _eventVector)) {
                        _eventVector.addElement(event);
                    }
                } else {
                    _eventVector.addElement(event);
                }
                events.removeElementAt(i);
                i--;
            }
        }
        return null;
    }

    /** Overridden to handle mouse clicks within the TextView. */
    public boolean mouseDown(MouseEvent event) {
        TextParagraph           paragraph;
        TextPositionInfo        anchorInfo;
        TextPositionInfo        absoluteAnchorInfo;
        int                     anchorPosition, endPosition;

        _mouseDownTextAttachment = null;
        _clickedRange=null;

        if(isEditable()|| isSelectable()) /** Should grab focus when only selectable
                                           *  for selection display exclusion
                                           */
            setFocusedView();

        if(!rootView().mouseStillDown())
            return true;

        // set focused view should take care of
        // setting editing to true
        //_setEditing(true);

        _selection.hideInsertionPoint();

        _clickCount = event.clickCount();

        _anchorInfo = positionForPoint(event.x, event.y,false);
        absoluteAnchorInfo = positionForPoint(event.x,event.y,true);

        if( _anchorInfo != null && _anchorInfo._endOfLine ) {
            Rect r = new Rect(_anchorInfo._x,_anchorInfo._y,bounds.width,_anchorInfo._lineHeight);
            if(!r.contains(event.x,event.y))
                selectLineBreak=false;
            else
                selectLineBreak=true;
        } else
            selectLineBreak = false;

        if( absoluteAnchorInfo != null ) {
            TextStyleRun run;

            run = _runForIndex( absoluteAnchorInfo._absPosition );
            if( run != null ) {
                Hashtable attr;
                TextAttachment item;
                boolean result;

                attr = run.attributes();
                if( attr != null ) {
                    if( (item = (TextAttachment)attr.get(TEXT_ATTACHMENT_KEY)) != null && /* End of line ? */
                        run.rangeIndex() == absoluteAnchorInfo._absPosition) {
                        /** Cannot use rectForRange since the height is equal to lineHeight */
                        Rect rect = run.textAttachmentBoundsForOrigin(absoluteAnchorInfo._x,
                                        absoluteAnchorInfo._y,
                                        run._paragraph._baselines[absoluteAnchorInfo._lineNumber]);
                        if( rect != null && rect.contains(event.x,event.y)) {

                            result = item.mouseDown(new MouseEvent( event.timeStamp,
                                                                    event.type,
                                                                    event.x - rect.x,
                                                                    event.y - rect.y,
                                                                    event.modifiers));
                            if( result ) {
                                _mouseDownTextAttachment = item;
                                _mouseDownTextAttachmentOrigin =
                                    new Point(rect.x,rect.y);
                                return true;
                            }
                        }
                    }
                    if(!isEditable() && attr.get(LINK_KEY) != null
                        && runUnderMouse(run,event.x,event.y)
                        && _clickCount == 1) {
                        _clickedRange  = linkRangeForPosition( absoluteAnchorInfo._absPosition);
                        highlightLinkWithRange(_clickedRange,true);
                    }
                }
            }
        }

        if( !isSelectable() && _clickedRange == null)
            return false;

        _firstRange = null;
        if( _clickCount > 1 ) {
            /* Multiple click at a line break should do nothing */
            if(!selectLineBreak) {
                switch(_clickCount) {
                case 2:
                    _firstRange = groupForIndex(_anchorInfo._absPosition);
                    break;
                default:
                    _firstRange = paragraphForIndex(_anchorInfo._absPosition);
                    break;
                }

                if( _firstRange != null && !_firstRange.isNullRange()) {
                    if( event.isShiftKeyDown()) {
                        Range r = new Range(selectedRange());
                        r.unionWith(_firstRange.index,_firstRange.length);
                        _selection.setRange(r.index,r.index+r.length,null,false);
                    } else
                        _selection.setRange(_firstRange.index,_firstRange.lastIndex()+1,
                                          null,false);
                    _selectionChanged();
                }
                return true;
            } else
                _firstRange = new Range(_anchorInfo._absPosition,0);
        }

        if( event.isShiftKeyDown() ) {
            _selection.setRange(_selection.orderedSelectionStart(),
                                _anchorInfo._absPosition,null,false);
        } else
            _selection.setInsertionPoint(_anchorInfo);
        _selectionChanged();
        _upInfo = null;

        _downY = _anchorInfo._y + _anchorInfo._lineHeight;

        return true;
    }

    /** Overridden to handle mouse drags within the TextView. */
    public void mouseDragged(MouseEvent event) {
        TextParagraph          paragraph;
        TextPositionInfo        newUp;
        Rect                    tmpRect;
        int                     wordPosition, anchorPosition;
        Point                   realPoint;
        boolean                 selectionChanged;

        if( _mouseDownTextAttachment != null ) {
            _mouseDownTextAttachment.mouseDragged( new MouseEvent( event.timeStamp,
                                                                   event.type,
                                                                   event.x - _mouseDownTextAttachmentOrigin.x,
                                                                   event.y - _mouseDownTextAttachmentOrigin.y,
                                                                   event.modifiers));
            return;
        }

        realPoint = new Point(event.x,event.y);
        if( realPoint.x >= bounds.width )
            realPoint.x = bounds.width-1;
        else if( realPoint.x < 0 )
            realPoint.x = 0;

        if( realPoint.y >= bounds.height )
            realPoint.y = bounds.height-1;
        else if( realPoint.y < 0 )
            realPoint.y = 0;

        newUp = positionForPoint(realPoint.x, realPoint.y,false);

        if( _clickedRange != null ) {
            TextStyleRun runForMouse = _runForIndex( newUp._absPosition);
            Hashtable runAttr;
            if( (runAttr = runForMouse.attributes()) != null &&
                (runAttr.get(LINK_KEY) != null ) && runUnderMouse(runForMouse,event.x,event.y)) {
                Range newClickedRange = linkRangeForPosition(newUp._absPosition);
                if(! newClickedRange.equals(_clickedRange)) {
                    highlightLinkWithRange(_clickedRange,false);
                    _clickedRange = newClickedRange;
                    highlightLinkWithRange(_clickedRange,true);
                }
                return;
            } else {
                highlightLinkWithRange(_clickedRange,false);
                _clickedRange = null;
            }
        }

        if(!isSelectable())
            return;


        if (newUp == null) {
            // THE MOUSE DRAGGED GOT A NULL POINT
            return;
        }

        if (!containsPointInVisibleRect(event.x, event.y)) {
            tmpRect = TextView.newRect(newUp._x, newUp._y, 1,
                                       newUp._lineHeight);
            scrollRectToVisible(tmpRect);
            TextView.returnRect(tmpRect);
        }

        anchorPosition = _anchorInfo._absPosition;
        if( _upInfo != null && newUp._absPosition != _upInfo._absPosition )
            selectionChanged = true;
        else
            selectionChanged = false;
        _upInfo = newUp;


        if(selectionChanged) {
            Range first,last;

            switch( _clickCount ) {
            case 0:
            case 1:
                _selection.setRange(anchorPosition, _upInfo._absPosition, _upInfo,
                                    selectLineBreak);
                _selectionChanged();
                return;
            case 2:
                last  = groupForIndex(_upInfo._absPosition);
                break;
            default:
                last  = paragraphForIndex(_upInfo._absPosition);
                break;
            }
            if(_firstRange != null && !_firstRange.isNullRange() && !last.isNullRange() ) {
                last.unionWith(_firstRange);
                if(!last.equals(selectedRange())) {
                    _selection.setRange(last.index,last.lastIndex()+1,null,selectLineBreak);
                    _selectionChanged();
                }
            }
        }
    }

    /** Overridden to handle mouse up Events within the TextView. */
    public void mouseUp(MouseEvent event) {
        TextPositionInfo upPosition;

        if( _mouseDownTextAttachment != null ) {
            _mouseDownTextAttachment.mouseUp( new MouseEvent( event.timeStamp,
                                                              event.type,
                                                              event.x - _mouseDownTextAttachmentOrigin.x,
                                                              event.y - _mouseDownTextAttachmentOrigin.y,
                                                              event.modifiers));
            _mouseDownTextAttachment = null;
            _mouseDownTextAttachmentOrigin = null;
            return;
        }

        if( _clickedRange != null ) {
            Range newClickedRange;
            upPosition = positionForPoint(event.x,event.y,true);
            newClickedRange = linkRangeForPosition(upPosition._absPosition);
            highlightLinkWithRange(_clickedRange,false);

            if( newClickedRange != null && newClickedRange.equals(_clickedRange)) {
                if( _owner != null ) {
                    TextStyleRun run = _runForIndex( _clickedRange.index );
                    Hashtable runAttr = run.attributes();
                    String urlStr = null;

                    if( runAttr != null && (urlStr = (String) runAttr.get(LINK_KEY)) != null
                        && runsUnderMouse(runsForRange(_clickedRange),event.x,event.y))
                        _owner.linkWasSelected(this,_clickedRange,urlStr);
                }
            }
            _clickedRange=null;
        }

        if(!isSelectable())
            return;

        if (_upInfo == null ||
            _upInfo._absPosition == _anchorInfo._absPosition) {
            _selection.showInsertionPoint();
        }

        _firstRange = null;
    }

    /** Overridden to return TEXT_CURSOR when the TextView is
      * selectable or editable.
      */
    public int cursorForPoint(int x, int y) {
        if( isEditable() )
            return TEXT_CURSOR;
        else {
            TextPositionInfo info = positionForPoint(x, y, true);
            if (info != null) {
              TextStyleRun run = _runForIndex(info._absPosition);

              if (run != null) {
                Hashtable attr = run.attributes();

                if (attr != null) {
                  if (attr.get(LINK_KEY) != null && runUnderMouse(run,x,y)) {
                    return HAND_CURSOR;
                  }
                }
              }
            }

            if (isSelectable()) {
              return TEXT_CURSOR;
            } else {
              return ARROW_CURSOR;
            }
        }
    }

    /** Implements TextView's commands.  You should never
      * call this method.
      */
    public void performCommand(String command, Object data) {
        Rect    updateRect;
        int     index;

        if( command.equals("refreshBitmap")) {
            refreshBitmap(data);
        } else if (command != null && command.equals(SET_FONT)) {
            processSetFont((Font)data);
            return;
        } else if (command.equals(CUT)) {
            cut();
        } else if (command.equals(COPY)) {
            copy();
        } else if (command.equals(PASTE)) {
            paste();
        } else if (!(data instanceof Timer)) {
            return;
        }

        /* more to redraw? */
        if (_updateParagraph == null) {
            if (_updateTimer != null) {
                _updateTimer.stop();
                _updateTimer = null;
            }
            return;
        }

        _drawText = false;
        updateRect = _updateParagraph.rectForLine(_updateLine);
        draw(updateRect);
        TextView.returnRect(updateRect);
        _drawText = true;

        /* more to redraw? */
        _updateLine++;
        if (_updateLine >= _updateParagraph._breakCount) {
            if (!_drawNextParagraph) {
                _updateParagraph = null;
            } else {
                index = _paragraphVector.indexOfIdentical(_updateParagraph) + 1;
                if (index == 0 || index >= _paragraphVector.count()) {
                    _updateParagraph = null;
                } else {
                    _updateParagraph =
                        (TextParagraph)_paragraphVector.elementAt(index);
                    _updateLine = 0;
                }
            }
        }

        /* if not, stop */
        if (_updateParagraph == null && _updateTimer != null) {
            _updateTimer.stop();
            _updateTimer = null;
        }
    }

    /** Implemented so that TextView can participate in the TargetChain.
      * @see ExtendedTarget
      */
    public boolean canPerformCommand(String command) {
        if (command.equals(SET_FONT)) {
            if( usesSingleFont() || !isEditable())
                return false;
            else
                return true;
        } else if (command.equals("refreshBitmap") ||
                   command.equals(COPY) ||
                   (isEditable() && command.equals(CUT)) ||
                   (isEditable() && command.equals(PASTE))) {
            return true;
        } else {
            return false;
        }
    }

    /** Overridden so that TextView can receive KeyEvents. */
    public void keyDown(KeyEvent event) {
        if( event.isPageUpKey()) {
            int newY;
            Rect visibleRect = new Rect();
            TextPositionInfo newPosition;

            computeVisibleRect(visibleRect);
            visibleRect.y -= (visibleRect.height - 1);
            if( visibleRect.y < 0 )
                visibleRect.y = 0;


            newPosition = positionForPoint(visibleRect.x,visibleRect.y,true);
            if( newPosition != null)
                visibleRect.y = newPosition._y;
            scrollRectToVisible(visibleRect);
            return;
        } else if( event.isPageDownKey()) {
            int newY;
            Rect visibleRect = new Rect();
            TextPositionInfo currentPosition;
            TextPositionInfo newPosition;



            computeVisibleRect(visibleRect);
            currentPosition = positionForPoint(visibleRect.x,visibleRect.y,true);

            visibleRect.y += (visibleRect.height - 1);
            if( visibleRect.y > (bounds.height - visibleRect.height))
                visibleRect.y = (bounds.height - visibleRect.height);



            newPosition = positionForPoint(visibleRect.x,visibleRect.y,true);
            if( newPosition != null) {
                visibleRect.y = newPosition._y;
                if( currentPosition != null &&
                    currentPosition._absPosition == newPosition._absPosition)
                    visibleRect.y += newPosition._lineHeight;
            }
            scrollRectToVisible(visibleRect);
            return;
        }

        if(! isEditable() ) {
            //            Application.application().beep();
            return;
        }

        if(! hasSelection()) {
            //            Application.application().beep();
            return;
        }

        if (_filter != null) {
            if (_filter.acceptsEvent(this, event, _eventVector)) {
                _eventVector.addElement(event);
            }
        } else {
            _eventVector.addElement(event);
        }

        application().eventLoop().filterEvents(this);
        while (!_eventVector.isEmpty()) {
            _keyDown();
        }
    }

    /** Overridden to let TextViews accepts drags of Colors and Images.
     */
    public DragDestination acceptsDrag(DragSession session, int x, int y) {
        String type = session.dataType();

        if (isEditable() && hasSelection() &&
            (Color.COLOR_TYPE.equals(type) ||
             Image.IMAGE_TYPE.equals(type))) {
            return this;
        } else {
            return null;
        }
    }

    /** DragDestination support method.
      * @see DragDestination#dragEntered
      */
    public boolean dragEntered(DragSession session) {
        return true;
    }

    /** DragDestination support method.
      * @see DragDestination#dragMoved
      */
    public boolean dragMoved(DragSession session) {
        return true;
    }

    /** DragDestination support method.
      * @see DragDestination#dragExited
      */
    public void dragExited(DragSession session) {
    }

    /** Drag and drop destination support method, called when the user
      * releases a DragSession's Image over the TextView. If the DragSession
      * represents a Color, changes the currently selected text's Color to that
      * Color. If the DragSession represents an Image, replaces the selected
      * text with the Image.
      * @see DragDestination#dragExited
      */
    public boolean dragDropped(DragSession session) {
        TextParagraph           insertionParagraph;
        TextPositionInfo        insertionPoint;
        TextStyleRun            newRun;
        Object                  dragItem;
        int                     index;
        Range                   r;


        if (!isEditable() || !hasSelection()) {
            return false;
        }

        dragItem = session.data();
        if (dragItem == null)
            return false;

        if (dragItem instanceof Color) {
            r = selectedRange();
            if( r.length > 0 )
                addAttributeForRange( TEXT_COLOR_KEY, dragItem , r);
            else
                addTypingAttribute(TEXT_COLOR_KEY, dragItem);
            return true;
        } else if(dragItem instanceof Image) {
            replaceRangeWithTextAttachment(selectedRange(),
                                           new ImageAttachment((Image)dragItem));
            return true;
        }
        return false;
    }

    /** Focus management support method. */
    public void startFocus() {
        _setEditing(true);
        showInsertionPoint();
        _selection._startFlashing();
        if( isEditable() && _owner != null )
            _owner.textEditingDidBegin(this);
        if( hasSelection() ) {
            Range r = selectedRange();
            if( r.length > 0 )
                dirtyRange(r);
        } else
            selectRange(new Range(0,0));
    }

    /** Focus management support method. */
    public void stopFocus() {
        _selection._stopFlashing();
        hideInsertionPoint();
        _setEditing(false);
        if( isEditable() && _owner != null )
            _owner.textEditingDidEnd(this);
        if( hasSelection() ) {
            Range r = selectedRange();
            if( r.length > 0 )
                dirtyRange(r);
        }
    }

    /** Focus management support method. */
    public void pauseFocus() {
        _selection._stopFlashing();
        hideInsertionPoint();
    }

    /** Focus management support method. */
    public void resumeFocus() {
        showInsertionPoint();
        _selection._startFlashing();
    }


    /** Converts the TextView's contents to a String containing both the text
      * and its attributes.
      */
    public String toString() {
        int i,c;
        StringBuffer sb = new StringBuffer();

        for(i=0,c=_paragraphVector.count() ; i < c ; i++ )
            sb.append(_paragraphVector.elementAt(i).toString());

        return sb.toString();
    }

    /** Sets the TextView's filter, the object that examines each KeyEvent
      * the TextView receives.
      */
    public void setFilter(TextFilter aFilter) {
        _filter = aFilter;
    }

    /** Returns the TextView's text filter.
      * @see #setFilter
      */
    public TextFilter filter() {
        return _filter;
    }

    /** Sets the TextField's owner, the object that it notifies of important
      * events such as the selection changing.
      * @see TextViewOwner
      */
    public void setOwner(TextViewOwner owner) {
        _owner = owner;
    }

    /** Returns the TextField's owner.
      * @see #setOwner
      */
    public TextViewOwner owner() {
        return _owner;
    }

    /** Disables TextView resizing.  If you are about to perform several
      * changes to the TextView, call this method to disable the auto-resizing,
      * to avoid recomputing the TextView's size.
      * @see #enableResizing
      */
    public void disableResizing() {
        _resizeDisabled++;
    }

    /** Reenables TextView resizing.  You should call <b>sizeToMinSize()</b>
      * to get the TextView to relayout its contents.
      *  @see #disableResizing
      */
    public void enableResizing() {
        _resizeDisabled--;
        if (_resizeDisabled < 0) {
            _resizeDisabled = 0;

        }
    }

    /** Returns <b>true</b> if resizing is enabled.
      * @see #enableResizing
      * @see #disableResizing
      */
    public boolean isResizingEnabled() {
        return _resizeDisabled == 0;
    }

    /** Calculates the minimum size needed to display the text and
      * resizes the TextView to this minimum size.
      */
    public void sizeToMinSize() {
        sizeBy(0, adjustCharCountsAndSpacing() - bounds.height);
    }


    /** Scrolls the TextView so that <b>aRange</b> is visible. */
    public void scrollRangeToVisible(Range aRange) {
        if(aRange.index == 0 && aRange.length == 0) {  /* top ?*/
            scrollRectToVisible(new Rect(0,0,1,1));
        } else if(aRange.index >= length()) {
            scrollRectToVisible(new Rect(width()-1,height()-1,1,1));
        } else if(aRange.length == 0) {
            TextPositionInfo top = positionInfoForIndex(aRange.index);
            scrollRectToVisible(new Rect(top._x,top._y+top._lineHeight,1,top._lineHeight));
        } else {
            TextPositionInfo top = positionInfoForIndex(aRange.index);
            TextPositionInfo bottom = positionInfoForIndex(aRange.index + aRange.length);
            int y1,y2;
            y1 = top._y;
            y2 = bottom._y + bottom._lineHeight;

            if(top._y == bottom._y) {
                scrollRectToVisible(new Rect(top._x,y1,bottom._x - top._x,y2-y1));
            } else {
                scrollRectToVisible(new Rect(0,y1,width(),y2 - y1));
            }
        }
    }

    /** If <b>flag</b> is <b>true</b>, TextView ignores all the font
      * attributes but the default one, which it uses to display the
      * text.
      */
    public void setUseSingleFont(boolean flag) {
        _useSingleFont = flag;
    }

    /** Returns <b>true</b> if the receiver uses a single font.
      * @see #setUseSingleFont
      */
    public boolean usesSingleFont() {
        return _useSingleFont;
    }

    /** Sets the default attributes used by the TextView when new text is
      * inserted.
      */
    public void setDefaultAttributes(Hashtable attributes) {
        int i,c;
        Range r;

        _defaultAttributes = attributes;
        if( attributes.get(FONT_KEY) != null )
            _defaultFontMetricsCache = null;

        if( attributes.get(PARAGRAPH_FORMAT_KEY) == null ) {
            TextParagraphFormat defaultFormat;

            defaultFormat = new TextParagraphFormat();
            defaultFormat.setLeftMargin(3);
            defaultFormat.setRightMargin(3);
            defaultFormat.setJustification(Graphics.LEFT_JUSTIFIED);
            _defaultAttributes.put(PARAGRAPH_FORMAT_KEY, defaultFormat);
        }

        reformatAll();

        r = allocateRange(0,length());
        dirtyRange( r );
        recycleRange( r );
    }

    /** Returns the default attributes.
      * @see #setDefaultAttributes
      */
    public Hashtable defaultAttributes() {
        return _defaultAttributes;
    }

    /** Add an attribute into typingAttributes.
     *  When the user types some character, attributes into typingAttributes
     *  are added.
     */
    public void addTypingAttribute(String key,Object value) {
        Hashtable tmp = new Hashtable();
        Enumeration keys;
        String k;

        tmp.put(key,value);
        tmp = TextView.attributesByRemovingStaticAttributes(tmp);
        keys = tmp.keys();

        while(keys.hasMoreElements()){
            k = (String)keys.nextElement();
            _typingAttributes.put(k,tmp.get(k));
        }
    }

    /** Return the current typing attributes
      *
      */
    public Hashtable typingAttributes() {
        return _typingAttributes;
    }

    /** Set the typing attributes.
      *
      */
    public void setTypingAttributes(Hashtable attr) {
        if( attr == null )
            _typingAttributes = new Hashtable();
        else
            _typingAttributes = TextView.attributesByRemovingStaticAttributes(attr);
    }

    /** Sets the TextView's background Color. */
    public void setBackgroundColor(Color aColor) {
        if (aColor != null) {
            _backgroundColor = aColor;
        }
    }

    /** Returns the TextView's background Color.
      * @see #setBackgroundColor
      */
    public Color backgroundColor() {
        return _backgroundColor;
    }

    /** Sets the TextView's selection Color. */
    public void setSelectionColor(Color aColor) {
        if (aColor != null) {
            _selectionColor = aColor;
        }
    }

    /** Returns the TextView's selection Color.
      * @see #setSelectionColor
      */
    public Color selectionColor() {
        return _selectionColor;
    }

    /** Sets whether or not the TextView is editable. */
    public void setEditable(boolean flag) {
        if (_editable != flag) {
            _editable = flag;
            setSelectable(true);
        }
    }

    /** Returns <b>true</b> if the TextView is editable.
      * @see #setEditable
      */
    public boolean isEditable() {
        return _editable;
    }

    /** Sets whether or not the TextView is selectable. */
    public void setSelectable(boolean flag) {
        RootView        rootView;

        if (_selectable != flag) {
            _selectable = flag;
            rootView = rootView();
            if (rootView != null) {
                rootView.updateCursor();
            }
        }
    }

    /** Returns <b>true</b> if the TextView is selectable.
      * @see #setSelectable
      */
    public boolean isSelectable() {
        return _selectable;
    }

    /** Sets the font associated with the default attributes. */
    public void setFont(Font aFont) {
        if (aFont == null) {
            return;
        }
        addDefaultAttribute( FONT_KEY, aFont);
    }

    /** Returns the font associated with the default attributes.
      * @see #setFont
      */
    public Font font() {
        return (Font) _defaultAttributes.get(FONT_KEY);
    }


    /** Sets the TextView's default text Color. */
    public void setTextColor(Color aColor) {
        if (aColor != null) {
            addDefaultAttribute( TEXT_COLOR_KEY , aColor );
        }
    }


    /** Returns the TextView's default text Color.
      * @see #setTextColor
      */
    public Color textColor() {
        Color res = (Color) _defaultAttributes.get(TEXT_COLOR_KEY);
        if( res == null )
            return Color.black;
        else
            return res;
    }

    /** Sets the TextView's caret Color.  Default is Color.black.
     */
    public void setCaretColor(Color aColor) {
        if( aColor != null ) {
            addDefaultAttribute( CARET_COLOR_KEY , aColor);
        }

    }

    /** Returns the TextView's caret Color.
      * @see #setCaretColor
      */
    public Color caretColor() {
        Color res = (Color) _defaultAttributes.get(CARET_COLOR_KEY);
        if( res == null )
            return Color.black;
        else
            return res;
    }

    /** Replaces the string enclosed by <b>r</b> with <b>aString</b>.  If
      * <b>r</b> is a null range, this method inserts <b>aString</b> into
      * the text.  If <b>aString</b> is
      * <b>null</b> or "", this method removes the text defined by <b>r</b>.
      * The inserted text will have the attributes set for the first
      * index of the range. If the index is 0, default attributes will be used.
      */
    public void replaceRangeWithString(Range r,String aString) {
        String                          subString;
        int                             insertionPoint, newHeight, start, end,
                                        lastIndex;
        boolean                         insertReturn = true;
        int                             paragraphIndex;

        if( _owner != null )
            _owner.textWillChange(this,r);

        if( r.equals(new Range(0,length()))) {
          replaceContentWithString(aString);
          if( _owner != null )
              _owner.textDidChange(this,new Range(0,length()));
          return;
        }

        disableResizing();
        paragraphIndex = _paragraphIndexForIndex(r.index);

        deleteRange(r,null);

        if( aString == null || aString.equals("") ) {
            enableResizing();
            if(paragraphIndex > 0)
              sizeBy(0,adjustCharCountsAndSpacing(paragraphIndex-1) - bounds.height);
            else
              sizeToMinSize();
            if( _owner != null )
                _owner.textDidChange(this,new Range(r.index,0));
            return;
        }

        disableAttachmentNotification();
        insertionPoint = r.index;
        start = 0;
        end   = aString.indexOf('\n');
        if (end == -1) {
            insertString(aString,insertionPoint);
            enableResizing();
            if(paragraphIndex > 0)
              sizeBy(0,adjustCharCountsAndSpacing(paragraphIndex-1) - bounds.height);
            else
              sizeToMinSize();
            enableAttachmentNotification();
            if( _owner != null )
                _owner.textDidChange(this,new Range(r.index,aString.length()));
            return;
        }

        insertString(aString.substring(start, end),insertionPoint);
        insertReturn(insertionPoint + (end-start));
        insertionPoint += ((end - start) + 1); /* Plus one for the carriage return */
        lastIndex = aString.length()-1;
        while(end < lastIndex) {
            start = end + 1;
            end   = aString.indexOf('\n', start);
            if( end == -1 ) {
                end = lastIndex+1;
                insertReturn = false;
            }

            if( end > start ) {
                subString = aString.substring(start,end);
                insertString(subString,insertionPoint);
                if( insertReturn )
                    insertReturn(insertionPoint + (end - start));
                insertionPoint += ((end - start)+1);
            } else {
                insertReturn(insertionPoint);
                insertionPoint++;
            }
        }

        enableResizing();
        if(paragraphIndex > 0)
          sizeBy(0,adjustCharCountsAndSpacing(paragraphIndex-1) - bounds.height);
        else
          sizeToMinSize();
        enableAttachmentNotification();
        if( _owner != null )
            _owner.textDidChange(this,new Range(r.index,aString.length()));
    }

    /** Returns the string included in <b>r</b>. */
    public String stringForRange(Range r) {
        TextParagraph leftP = _paragraphForIndex(r.index);
        TextParagraph rightP = _paragraphForIndex(r.index + r.length);

        if( leftP == null )
            return null;

        if( rightP == null )
            rightP = lastParagraph();

        if( leftP == rightP )
            return leftP.stringForRange(r);
        else {
            TextParagraph p;
            StringBuffer sb = new StringBuffer();
            Range  intersection = allocateRange();
            String s;
            int i,c;

            for( i = _paragraphVector.indexOfIdentical(leftP),
                     c = _paragraphVector.indexOfIdentical(rightP) ; i <= c ; i++ ) {
                intersection.index  = r.index;
                intersection.length = r.length;
                p = (TextParagraph) _paragraphVector.elementAt(i);
                intersection.intersectWith( p.range() );
                sb.append( p.stringForRange( intersection ));
            }

            recycleRange( intersection );
            return sb.toString();
        }
    }


    /** Sets the attributes for the Range <b>r</b>. <b>attributes</b> is a
      * Hashtable containing any attributes. TextView defines some special
      * attributes that it interprets when laying out and displaying the text.
      */
    public void setAttributesForRange(Hashtable attributes,Range r) {
        Range  paragraphsRange;
        Vector runsVector;
        TextStyleRun run;
        int i,c;

        paragraphsRange = paragraphsRangeForRange(r);
        for(i=paragraphsRange.index,c=paragraphsRange.index + paragraphsRange.length ;
            i < c ; i++ ) {
            ((TextParagraph)_paragraphVector.elementAt(i)).setFormat(null);
        }

        runsVector = createAndReturnRunsForRange(r);
        for(i=0,c=runsVector.count() ; i < c ; i++ ) {
            run = (TextStyleRun) runsVector.elementAt(i);
            run.setAttributes(null);
        }

        addAttributesForRange(attributes,r);
    }


    /** Returns the attributes for the character at <b>anIndex</b>.
      * To determine the scope of the attributes, use
      * <b>runRangeForCharacterAtIndex()</b>
      * and <b>paragraphRangeForCharacterAtIndex()</b>.
      */
    public Hashtable attributesAtIndex(int anIndex) {
        TextStyleRun r = _runForIndex(anIndex);
        TextParagraph p = _paragraphForIndex(anIndex);

        if( r != null && p != null ) {
            Hashtable attr = r.attributes();
            if( attr == null ) {
                TextParagraphFormat f = p.format();
                if( f == null )
                    return _defaultAttributes;
                else {
                    Hashtable h = (Hashtable) _defaultAttributes.clone();
                    h.put(PARAGRAPH_FORMAT_KEY,f);
                    return h;
                }
            } else {
                TextParagraphFormat f = p.format();
                Hashtable h = (Hashtable)attr.clone();

                if( f != null )
                    h.put(PARAGRAPH_FORMAT_KEY,f);
                else
                    h.put(PARAGRAPH_FORMAT_KEY,_defaultAttributes.get(PARAGRAPH_FORMAT_KEY));
                return h;
            }
        } else
            return _defaultAttributes;
    }

    /** Returns the number of characters the TextView contains. */
    public int length() {
        /* A paragraph has always a \n. The last one does not exists */
        return _charCount - 1;
    }

    /** Returns the TextView's contents as a String. */
    public String string() {
        String result;
        Range r = allocateRange(0,length());
        result = stringForRange( r );
        recycleRange( r );
        return result;
    }

    /** Replaces all the text in the TextView with <b>textString</b>. */
    public void setString(String textString) {
        Range r = allocateRange(0,length());
        replaceRangeWithString(r,textString);
        recycleRange( r );
    }



    /** Appends <b>aString</b> to the end of the TextView. Returns the range
      * of the inserted string.
      */
    public Range appendString(String aString) {
        Range r = allocateRange( length(), 0 );
        replaceRangeWithString(r , aString);
        r.length = aString.length();
        return r;
    }


    /** Replaces the string enclosed by <b>r</b> with a <b>aTextAttachment</b>.
      * Inserts an attachment character.
      */
    public void replaceRangeWithTextAttachment(Range r, TextAttachment
                                                            aTextAttachment) {
        Hashtable h;
        Range r2;
        replaceRangeWithString(r,TEXT_ATTACHMENT_STRING);
        h = new Hashtable();
        h.put(TextView.TEXT_ATTACHMENT_KEY,aTextAttachment);
        r2 = allocateRange(r.index,1);
        addAttributesForRange(h, r2);
        recycleRange( r2 );
    }


    /** Adds attributes for the text in the Range <b>range</b>.
      * Merges the contents of <b>attributes</b> with the current attributes
      * of the range.
      */
    public void addAttributesForRange(Hashtable attributes, Range range) {
        if( _owner != null )
            _owner.attributesWillChange(this,range);

        addAttributesForRangeWithoutNotification(attributes,range);

        if( _owner != null )
            _owner.attributesDidChange(this,range);
    }

    /** Adds the attribute <b>attribute</b> with value <b>value</b> to the
      * Range <b>range</b>.
      */
    public void addAttributeForRange(String attribute, Object value,
                                     Range range) {
        Hashtable h = new Hashtable();

        h.put(attribute, value);
        addAttributesForRange(h ,range);
    }

    /** Removes the attribute <b>attribute</b> from the Range <b>r</b>. */
    public void removeAttributeForRange(String attribute, Range r) {
        Vector runVector = runsForRange(r);
        int i,c;
        Range rr,effectiveRange;
        TextStyleRun run;
        Hashtable attr,newAttr;

        effectiveRange = allocateRange();
        for(i=0,c=runVector.count();i<c;i++) {
            rr = (Range)runVector.elementAt(i);
            run = _runForIndex( rr.index);
            attr = run.attributes();
            if( attr != null && attr.get(attribute) != null) {
                newAttr = (Hashtable)attr.clone();
                newAttr.remove(attribute);
                effectiveRange.index  = r.index;
                effectiveRange.length = r.length;
                effectiveRange.intersectWith(rr);
                setAttributesForRange( newAttr, effectiveRange);
            }
        }
    }

    /** Adds the attribute <b>attribute</b> with value <b>value</b> to the
      * default attributes.
      */
    public void addDefaultAttribute(String attribute, Object value) {
        Hashtable defAttr = defaultAttributes();

        defAttr.put(attribute, value);
        setDefaultAttributes(defAttr);
    }

    /** Decomposes <b>aRange</b> into a Vector of ranges. Each Range represents
      * a run. The Vector includes the left-most and right-most Ranges, even
      * if they are not fully included in <b>aRange</b>.
      */
    public Vector runsForRange(Range aRange) {
        TextStyleRun startRun,endRun,run;
        Vector result = new Vector();
        Vector runsAfter = null;
        Vector runs;
        int i,c,j,d;
        boolean sameParagraph = false;

        if( aRange.length == 0 ) {
            Range r = runForIndex(aRange.index);
            if( !r.isNullRange() )
                result.addElement(r);
            return result;
        }
        startRun = _runForIndex(aRange.index);
        if( startRun == null )
            startRun = _runForIndex(0);

        endRun   = _runForIndex(aRange.index + aRange.length() -1);
        if( endRun  == null )
            endRun = _runForIndex(length()-1);

        runs = startRun.paragraph().runVector();

        if( startRun.paragraph() == endRun.paragraph() ) {
            c  = runs.indexOfIdentical(endRun) + 1;
            sameParagraph=true;
        } else {
            c  = runs.count();
            sameParagraph=false;
        }

        for(i=runs.indexOfIdentical(startRun) ; i < c ; i++ )
            result.addElement( ((TextStyleRun)runs.elementAt(i)).range());

        if(!  sameParagraph ) {
            for( i = _paragraphVector.indexOfIdentical( startRun.paragraph() ) + 1 ,
                     c = _paragraphVector.indexOfIdentical( endRun.paragraph() ) ; i < c ; i++ ) {
                runs = ((TextParagraph)_paragraphVector.elementAt( i )).runVector();
                for(j=0, d = runs.count() ;  j < d ; j++ )
                    result.addElement( ((TextStyleRun)runs.elementAt(j)).range() );
            }

            runs = endRun.paragraph().runVector();
            for( i = 0 , c = runs.indexOfIdentical(endRun) ; i <= c ; i++ )
                result.addElement(((TextStyleRun)runs.elementAt(i)).range());
        }
        return result;
    }

    /** Decomposes <b>aRange</b> into a Vector of Ranges, with each Range
      * representing a paragraph.  The Vector includes the left-most and
      * right-most Ranges, even if they are not fully included in
      * <b>aRange</b>.
      */
    public Vector paragraphsForRange(Range aRange) {
        Range pr = paragraphsRangeForRange(aRange);
        int i,c;
        Vector result = new Vector();
        Range r;

        for(i=pr.index,c=pr.index+pr.length ; i < c ; i++ ) {
            r = ((TextParagraph)_paragraphVector.elementAt(i)).range();
            if( i == (c - 1)) { /* Last paragraph */
                r.length--; /* Remove the last \n that does not exist */
            }
            result.addElement(r);
        }
        return result;
    }

    /** Returns the run Range to which the character at <b>anIndex</b> belongs.
      */
    public Range runForIndex(int anIndex) {
        TextStyleRun run = _runForIndex(anIndex);
        if( run == null )
            return allocateRange();
        else
            return run.range();
    }

    /** Returns the paragraph Range to which the character at <b>anIndex</b>
      * belongs.
      */
    public Range paragraphForIndex(int anIndex) {
        Vector v;
        Range pr = allocateRange(anIndex,0);
        v = paragraphsForRange(pr);
        recycleRange(pr);
        if( v.count() > 0 )
            return (Range)v.elementAt(0);
        else
            return allocateRange();
    }

    /** Returns the Range of the paragraph containing the point
      * (<b>x</b>, <b>y</b>).
      */
    public Range paragraphForPoint(int x,int y) {
        TextParagraph p = _paragraphForPoint(x,y);
        if( p != null )
            return p.range();
        else
            return allocateRange(); /* Return a nullRange() */
    }

    /** Returns the Range of the run containing the point
      * (<b>x</b>, <b>y</b>).
      */
    public Range runForPoint(int x,int y) {
        TextPositionInfo info = positionForPoint( x,y,true );
        TextStyleRun run;

        if( info != null ) {
            run = _runForIndex( info._absPosition );
            if( run != null )
                return run.range();
        }
        return allocateRange(); /* return a nullRange() */
    }

    /** Returns the index of the character under the point
      * (<b>x</b>, <b>y</b>).
      *  Returns -1 if there is no character under (<b>x</b>, <b>y</b>).
      */
    public int   indexForPoint(int x,int y) {
        TextPositionInfo info = positionForPoint(x,y,true);
        if( info != null )
            return info._absPosition;
        else
            return -1;
    }

    /** Returns the Vector of Rects enclosing <b>aRange</b>.
      * Rectangles are ordered from top to bottom.
      * If the Range length is null or the Range is completely out of bounds,
      * this method returns an empty Vector.  If the Range is partially out of
      * bounds, this method returns the Rects for the intersection
      * of the given Range and the Range of the text.
      */
    public Vector rectsForRange(Range aRange) {
        return rectsForRange(aRange,null);
    }


    /*
     *  Selection
     */

    /** Returns the selected Range. Returns Range.nullRange() if no selection
      * exists.
      */
    public Range selectedRange() {

        if( _selectedRange == null )
            _selectedRange = allocateRange();

        _selectedRange.index = _selection.selectionStart();
        if( _selectedRange.index < 0 )
            return allocateRange();
        _selectedRange.length = _selection.selectionEnd() -
                                _selection.selectionStart();
        return _selectedRange;
    }

    /** Sets the selected Range. If <b>aRange</b> is Range.nullRange(),
      * removes the TextView's selection.
      */
    public void selectRange(Range aRange) {
        /* No need to refresh since _selection.setRange does it */
        if( aRange.isNullRange() ) {
            _selection.clearRange();
        } else {
            _selection.setRange(aRange.index(), aRange.lastIndex()+1);
        }

        _selectionChanged();
    }

    /** Returns <b>true</b> if the TextView has a selection. */
    public boolean hasSelection() {
        Range r = selectedRange();
        if(r.isNullRange())
            return false;
        else
            return true;
    }


    /* Import */

    /** Primitive to insert some HTML elements into a range. Vector should
      * contain some TextViewHTMLElement. This method is useful when
      * extending HTML parsing with some containers producing TextAttachment.
      * Use importHTMLInRange() or importHTMLFromURLString() in any other
      * <b>attributes</b> is the attributes that should be used as base to
      * parse HTML. For example if you set FONT_KEY to be helvetica 18,
      * all HTML text will be helvetica 18, but if HTML changes the font.
      *
      */
    public void insertHTMLElementsInRange(Vector components, Range aRange,Hashtable attributes) {
        String s;
        int i,c;
        int lengths[] = new int[components.count()];
        int offset=0,length=0;
        Hashtable context = new Hashtable();
        FastStringBuffer fb = new FastStringBuffer();
        Hashtable initialAttributes;

        if( attributes == null )
            attributes = defaultAttributes();

        initialAttributes = TextView.attributesByRemovingStaticAttributes(attributes);

        setDirty(true); /* Set dirty now to avoid computing dirty rects during parsing */
        disableResizing();
        fb.setDoublesCapacityWhenGrowing(true);

        for(i=0,c = components.count() ; i < c  ; i++) {
            ((TextViewHTMLElement)components.elementAt(i)).appendString(context,fb);
            if( i == 0 )
                length = lengths[0] = fb.length();
            else {
                lengths[i] = fb.length() - length;
                length += lengths[i];
            }
        }

        this.replaceRangeWithString(aRange, fb.toString());

        disableFormatting();

        for(i=0,c = components.count() ; i < c ; i++ ) {
            ((TextViewHTMLElement)components.elementAt(i)).setAttributesStartingAt(
                aRange.index + offset , initialAttributes,this,context);
            offset += lengths[i];
        }

        enableFormatting();
        reformatAll();
        enableResizing();
        sizeToMinSize();
    }

    /** Convenience to import HTML from the stream <b>inputStream</b>,
      * replacing the text defined by <b>aRange</b>.  If <b>baseURL</b> is
      * <b>null</b>, only absolute HTTP references will work.<p>
      * Default attributes will be used as a base to parse HTML.
      * Supports HTML v1.0.
      */
    public void importHTMLInRange(InputStream inputStream, Range aRange,
                                  URL baseURL) throws IOException, HTMLParsingException {
        importHTMLInRange(inputStream,aRange,baseURL,defaultAttributes());
    }

    /** Imports HTML from the stream <b>inputStream</b>, replacing the text
      * defined by <b>aRange</b>.  If <b>baseURL</b> is <b>null</b>, only
      * absolute HTTP references.<p>
      * <b>attributes</b> is the attributes that should be used as base to
      * parse HTML. For example if you set FONT_KEY to be helvetica 18,
      * all HTML text will be helvetica 18, but if HTML changes the font.
      * Supports HTML v1.0.
      */
    public void importHTMLInRange(InputStream inputStream, Range aRange,
                                  URL baseURL,Hashtable attributes)
        throws IOException, HTMLParsingException {
        Vector components = new Vector();
        HTMLParser parser;
        HTMLElement component;
        int i,c;


        validateHTMLParsingRules();
        parser = new HTMLParser(inputStream,_htmlParsingRules);


        try {
            while((component = parser.nextHTMLElement()) != null)
                components.addElement(component);
        } catch( java.lang.InstantiationException e) {
            throw new InconsistencyException("Cannot intantiate HTML storage");
        } catch (java.lang.IllegalAccessException e) {
            throw new InconsistencyException("Cannot access HTML storage classes");
        }

        setBaseURL( baseURL );
        insertHTMLElementsInRange(components, aRange,attributes);
    }

    /** Imports HTML from the URL <b>urlString</b>.
      * Scrolls the TextView to the top and sets the selection to (0, 0).
     */
    public void importHTMLFromURLString(String urlString) {
        Range r;
        URL url;
        InputStream in;
        r = allocateRange(0,length());
        try {
            url = new URL(urlString);
            in  = url.openStream();
            this.importHTMLInRange( in , r , url,defaultAttributes());
            r.index = 0;
            r.length = 0;
            this.selectRange(r);
            this.scrollRangeToVisible(r);
        } catch (MalformedURLException e) {
            System.err.println("Bad URL " + urlString );
        } catch (IOException e) {
            System.err.println("IOException while reading " + urlString);
        } catch (HTMLParsingException e ) {
            System.err.println("At line " + e.lineNumber() + ":" + e );
        }
        recycleRange( r );
    }

    /** Set the HTML parsing rules
      *
      */
    public void setHTMLParsingRules(HTMLParsingRules newRules) {
        _htmlParsingRules = newRules;
    }

    /** Return the HTML parsing rules
      *
      */
    public HTMLParsingRules htmlParsingRules() {
        validateHTMLParsingRules();
        return _htmlParsingRules;
    }

    /** Returns the Range of the run with the attribute
      * <b>LINK_DESTINATION_KEY</b> set to <b>aName</b>.
      * Returns a null Range if not found. Use <B>scrollRangeToVisible()</B>
      * to scroll the TextView to make the link destination visible.
      */
    public Range runWithLinkDestinationNamed(String aName) {
        int i,c;
        int j,d;
        TextParagraph paragraph;
        TextStyleRun run=null;
        String name;
        Range result = null;

        for(i = 0, c = _paragraphVector.count() ; i < c ; i++ ) {
            paragraph = (TextParagraph) _paragraphVector.elementAt(i);
            for(j=0,d = paragraph._runVector.count() ; j < d ; j++ ) {
                run = (TextStyleRun) paragraph._runVector.elementAt(j);
                if( run._attributes != null &&
                    (name = (String)run._attributes.get(LINK_DESTINATION_KEY)) != null ) {
                    if( name.equals(aName)) {
                        result = run.range();
                        break;
                    }
                }
            }
            if(result != null)
                break;
        }

        if(result == null)
            return new Range();
        else if(run != null) {
            Range initialResult = result;
            while(result.length == 0) {
                run = runAfter(run);
                if(run != null)
                    result = run.range();
                else {
                    result = initialResult;
                    break;
                }
            }
        }
        return result;
    }

    /* archiving */


    /** Describes the TextView class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.TextView", 2);
        info.addField(PARAGRAPHVECTOR_KEY, OBJECT_TYPE);
        info.addField(BACKGROUNDCOLOR_KEY, OBJECT_TYPE);
        info.addField(SELECTIONCOLOR_KEY, OBJECT_TYPE);
        info.addField(FILTER_KEY, OBJECT_TYPE);
        info.addField(DEFAULTATTRIBUTES_KEY, OBJECT_TYPE);
        info.addField(PARASPACING_KEY, INT_TYPE);
        info.addField(USESINGLEFONT_KEY, BOOLEAN_TYPE);
        info.addField(EDITABLE_KEY, BOOLEAN_TYPE);
        info.addField(SELECTABLE_KEY, BOOLEAN_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
        info.addField(HTML_PARSING_RULES_KEY,OBJECT_TYPE);
        info.addField(OWNER_KEY,OBJECT_TYPE);
    }

    /** Encodes the TextView instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(PARAGRAPHVECTOR_KEY, _paragraphVector);
        encoder.encodeObject(BACKGROUNDCOLOR_KEY, _backgroundColor);
        encoder.encodeObject(SELECTIONCOLOR_KEY, _selectionColor);
        encoder.encodeObject(FILTER_KEY, (Codable)_filter);
        encoder.encodeObject(DEFAULTATTRIBUTES_KEY, _defaultAttributes);
        encoder.encodeInt(PARASPACING_KEY, _paragraphSpacing);
        encoder.encodeBoolean(USESINGLEFONT_KEY, _useSingleFont);
        encoder.encodeBoolean(EDITABLE_KEY, _editable);
        encoder.encodeBoolean(SELECTABLE_KEY, _selectable);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
        encoder.encodeObject(HTML_PARSING_RULES_KEY,_htmlParsingRules);
        encoder.encodeObject(OWNER_KEY,_owner);
    }

    /** Decodes the TextView instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.TextView");

        super.decode(decoder);

        _paragraphVector = (Vector)decoder.decodeObject(PARAGRAPHVECTOR_KEY);
        _backgroundColor = (Color)decoder.decodeObject(BACKGROUNDCOLOR_KEY);
        _selectionColor = (Color)decoder.decodeObject(SELECTIONCOLOR_KEY);
        _filter = (TextFilter)decoder.decodeObject(FILTER_KEY);
        _defaultAttributes = (Hashtable)decoder.decodeObject(DEFAULTATTRIBUTES_KEY);
        _paragraphSpacing = decoder.decodeInt(PARASPACING_KEY);
        _useSingleFont = decoder.decodeBoolean(USESINGLEFONT_KEY);
        _editable = decoder.decodeBoolean(EDITABLE_KEY);
        _selectable = decoder.decodeBoolean(SELECTABLE_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);

        if(version >= 2) {
            _htmlParsingRules = (HTMLParsingRules) decoder.decodeObject(HTML_PARSING_RULES_KEY);
            _owner = (TextViewOwner) decoder.decodeObject(OWNER_KEY);
        }
    }

    /** Finishes the TextView instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        TextParagraph   nextParagraph;
        int             i;

        super.finishDecoding();

        // Drop the font metrics cache
        _defaultFontMetricsCache = null;

        i = _paragraphVector.count();
        while (i-- > 0) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
            nextParagraph.setOwner(this);
        }

        reformatAll();
    }

    /** Return the number of lines currently in TextView.
      *
      */
    public int lineCount() {
        int i,c,result = 0;
        TextParagraph p;

        for(i=0,c=_paragraphVector.count(); i < c ; i++ ) {
            p = (TextParagraph)_paragraphVector.elementAt(i);
            result += p.lineCount();
        }

        return result;
    }

    /** Return the baseURL for the document. The return value is defined
      * only while parsing some HTML.
      *
      */
    public URL baseURL() {
        return _baseURL;
    }


    /** Return whether this view can become the selected view
      * when the user is moving from view to views with the keyboard
      * The default implementation returns <b>true</b> if the TextView
      * is selectable.
      *
     */
    public boolean canBecomeSelectedView() {
        if( isEditable() || isSelectable())
            return true;
        else
            return false;
    }

    /** Inform the view that it is about to become the selected view.
      *
      */
    public void willBecomeSelected() {
        setFocusedView();
    }

    /** Converts CR/LF to CR. */
    public static String stringWithoutCarriageReturns(String aString) {
        FastStringBuffer sb = new FastStringBuffer();
        int i,c;
        char ch;

        for(i=0,c=aString.length(); i < c ; i++ ) {
            ch = aString.charAt(i);
            if( ch == '\r' && (i+1) < c && aString.charAt(i+1) == '\n')
                continue;
            else
                sb.append(ch);
        }
        return sb.toString();
    }


    /**
         * Return and cache the fontMetrics for the default font
         */
    FontMetrics defaultFontMetrics() {
        if( _defaultFontMetricsCache == null )
            _defaultFontMetricsCache = font().fontMetrics();
        return _defaultFontMetricsCache;
    }


    /* Static conveniences */
    static Hashtable attributesByRemovingStaticAttributes(Hashtable attributes) {
        if( attributes == null )
            return null;
        else {
            Hashtable h = (Hashtable) attributes.clone();
            h.remove(TEXT_ATTACHMENT_KEY);
            h.remove(TEXT_ATTACHMENT_BASELINE_OFFSET_KEY);
            return h;
        }
    }

    /** Adds <b>aParagraph</b> to the TextView.  You must call
         * <b>reformatAll()</b> * to get the TextView to properly reformat itself.
         * @see #reformat
         */
    private void addParagraph(TextParagraph aParagraph) {
        if (aParagraph == null) {
            return;
        }

        aParagraph.setOwner(this);
        _paragraphVector.addElement(aParagraph);
    }

    /* Set whether this text view can edit. The textview can edit if
     * it has the focus and a selection
     */
    synchronized void _setEditing(boolean flag) {
        _editing = flag;
    }

    synchronized boolean isEditing() {
        return _editing;
    }

    private void reformatAll() {
        TextParagraph   nextParagraph;
        int             currentHeight, i, count;
        Range wasSelectedRange = selectedRange();
        currentHeight = 0;
        _charCount = 0;
        count = _paragraphVector.count();

        if( ! formattingEnabled() )
            return;
        for (i = 0; i < count; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
            nextParagraph._y = currentHeight;
            nextParagraph._startChar = _charCount;
            nextParagraph.computeLineBreaksAndHeights(bounds.width);
            currentHeight += nextParagraph._height + _paragraphSpacing;
            _charCount += nextParagraph._charCount;
        }

        sizeBy(0, currentHeight - bounds.height);
        notifyAttachmentsForRange(new Range(0,length()),true);
        selectRange( wasSelectedRange );
    }

    /** Disables TextView paragraph formatting.  If you are about to perform several
         * changes to the TextView attributes, call this method to disable the auto-formatting,
         * to avoid recomputing the paragraph layout.
         * @see #enableFormatting()
         */
    void disableFormatting() {
        _formattingDisabled++;
    }

    /** Reenables TextView formatting.  You should call <b>reformat()</b>
         * to get the TextView to relayout its contents.
         *  @see #disableFormatting
         */
    void enableFormatting() {
        _formattingDisabled--;
        if (_formattingDisabled < 0) {
            _formattingDisabled = 0;
        }
    }

    /** Returns <b>true</b> if formatting is enabled.
         * @see #disableFormatting
         */
    boolean formattingEnabled() {
        return _formattingDisabled == 0;
    }


    private void formatParagraphAtIndex(int pIndex) {
      TextParagraph   nextParagraph,aParagraph;
        int             index, count, i, currentHeight;
        Range changedRange = new Range();

        if( ! formattingEnabled() )
            return;

        if(pIndex == -1)
          return;
        aParagraph = (TextParagraph) _paragraphVector.elementAt(pIndex);
        index = pIndex - 1;
        if (index < -1) {
            return;
        } else if (index == -1) {
            _charCount = 0;
            currentHeight = 0;
        } else {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(index);
            _charCount = nextParagraph._startChar + nextParagraph._charCount;
            currentHeight = nextParagraph._y + nextParagraph._height +
                _paragraphSpacing;
        }


        aParagraph.setY(currentHeight);
        aParagraph.setStartChar(_charCount);
        aParagraph.computeLineBreaksAndHeights(bounds.width);

        _charCount += aParagraph._charCount;
        currentHeight += aParagraph._height + _paragraphSpacing;

        /* reposition paragraphs below this one */
        count = _paragraphVector.count();
        for (i = pIndex + 1; i < count; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
            nextParagraph.setY(currentHeight);
            nextParagraph.setStartChar(_charCount);
            currentHeight += nextParagraph._height + _paragraphSpacing;
            _charCount += nextParagraph._charCount;
        }

        changedRange.index = aParagraph._startChar;
        changedRange.unionWith(lastParagraph().range());
        sizeBy(0, currentHeight - bounds.height);
        notifyAttachmentsForRange(changedRange,true);
    }

    private void formatParagraph(TextParagraph aParagraph) {
      formatParagraphAtIndex(_paragraphVector.indexOfIdentical(aParagraph));
    }

    private int adjustCharCountsAndSpacing(int pIndex) {
        TextParagraph   nextParagraph;
        int             currentHeight, i, count;
        currentHeight = _charCount = 0;

        if(pIndex > 0) {
          nextParagraph = (TextParagraph)_paragraphVector.elementAt(pIndex - 1);
          currentHeight = nextParagraph._y + nextParagraph._height + _paragraphSpacing;
          _charCount    = nextParagraph._startChar + nextParagraph._charCount;
        }

        count = _paragraphVector.count();
        for (i = pIndex; i < count; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
            nextParagraph._y = currentHeight;
            nextParagraph._startChar = _charCount;
            currentHeight += nextParagraph._height + _paragraphSpacing;
            _charCount += nextParagraph._charCount;
        }

        return currentHeight;
    }

    private int adjustCharCountsAndSpacing() {
      return adjustCharCountsAndSpacing(0);
    }

    int _paragraphIndexForIndex(int absPosition) {
        TextParagraph   nextParagraph;
        int             count, i;

        count = _paragraphVector.count();
        if(absPosition > (length() / 2)) {
            for (i = count-1; i >= 0; i--) {
                nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);

                if (absPosition >= nextParagraph._startChar &&
                    absPosition < nextParagraph._startChar +
                    nextParagraph._charCount) {
                  return i;
                }
            }
        } else {
            for (i = 0; i < count; i++) {
                nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);

                if (absPosition >= nextParagraph._startChar &&
                    absPosition < nextParagraph._startChar +
                    nextParagraph._charCount) {
                  return i;
                }
            }
        }

    return -1;
  }

    TextParagraph _paragraphForIndex(int absPosition) {
      int p = _paragraphIndexForIndex(absPosition);
      if(p!=-1)
        return (TextParagraph)_paragraphVector.elementAt(p);
      else
        return null;
    }

    private TextParagraph _paragraphForPoint(int x, int y) {
        TextParagraph   nextParagraph;
        int             count, i, minY, maxY;

        if (y < 0) {
            y = 0;
            x = 0;
        }

        count = _paragraphVector.count();
        minY = 0;
        for (i = 0; i < count; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);

            maxY = minY + nextParagraph._height;
            if (y >= minY && y < maxY) {
                return nextParagraph;
            }
            minY = maxY;
        }

        return null;
    }

    TextPositionInfo positionInfoForIndex( int index ) {
        TextParagraph p = _paragraphForIndex(index);
        if( p == null )
            p = lastParagraph();
        return p._infoForPosition( index );
    }

    /* If absolute is false, this method will return the next character
         * if the hit point is on the right of the glyph center. Otherwise
         * it will return the character for the glyph.
         * Example:  Netscape
         * flag is false, (x,y) is on the 't' a little after the center of the glyph 't'.
         *     the returned position will be the position of s.
         * flag is true , (x,y) is on the 't' a little after the center of the glyph 't'.
         *     the returned position will be the position of t
         */
    private TextPositionInfo positionForPoint(int x, int y, boolean absolute) {
        TextParagraph   theParagraph;

        if (y < 0) {
            y = 0;
            x = 0;
        }

        theParagraph = _paragraphForPoint(x, y);
        if (theParagraph == null) {
            if (_charCount == 0) {
                return lastParagraph().infoForPosition(_charCount, y);
            }
            return lastParagraph().infoForPosition(_charCount - 1, y);
        }

        return theParagraph.positionForPoint(x, y,absolute);
    }


    void drawInsertionPoint() {
        Rect    tmpRect;
        tmpRect = _selection.insertionPointRect();
        addDirtyRect(tmpRect);
        TextView.returnRect(tmpRect);
    }

    private void insertString(String aString,int insertionPoint) {
        TextParagraph           editParagraph, lastParagraph, oldUpdate;
        TextPositionInfo        newPosition, insertionPointInfo;
        Rect                    updateRect;
        int                     oldHeight, newHeight, oldLine,
                                insertCount = 0;
        Range r;
        TextParagraphFormat     format;
        int paragraphIndex;

        if (aString == null) {
            return;
        }

        paragraphIndex = _paragraphIndexForIndex(insertionPoint);
        if(paragraphIndex == -1)
          return;
        editParagraph = (TextParagraph) _paragraphVector.elementAt(paragraphIndex);

        if (editParagraph == null) {
            return;
        }

        lastParagraph = lastParagraph();
        if (lastParagraph != null) {
            oldHeight = lastParagraph._y + lastParagraph._height;
        } else {
            oldHeight = 0;
        }

        insertionPointInfo = positionInfoForIndex( insertionPoint );
        if (insertionPointInfo == null) {
            insertionPointInfo = editParagraph.infoForPosition(
                                                               insertionPoint, -1);
        }

        newPosition = editParagraph.insertCharOrStringAt('\0', aString, insertionPoint);
        insertCount = aString.length();
        _charCount += insertCount;

        if (newPosition == null) { /** Nothing actually happened **/
            _selection.showInsertionPoint();
            return;
        }

        if( isDirty() && dirtyRect == null ) {
            r = new Range(editParagraph._startChar, length() - editParagraph._startChar);
            notifyAttachmentsForRange(r,true);
            return;
        }

        newPosition.setAbsPosition(insertionPoint + insertCount);
        format = editParagraph.currentParagraphFormat();

        if (newPosition._redrawCurrentLineOnly && format._justification == Graphics.LEFT_JUSTIFIED) {
            //System.out.println("Redraw current line only");
            oldUpdate = _updateParagraph;
            oldLine = _updateLine;

            _updateParagraph = editParagraph;
            _updateLine = newPosition._updateLine;
            _drawText = false;
            updateRect = editParagraph.rectForLine(newPosition._lineNumber);
            updateRect.setBounds(insertionPointInfo._x, updateRect.y,
                                 updateRect.width - (insertionPointInfo._x - updateRect.x),
                                 updateRect.height);
            this.addDirtyRect(updateRect);
            TextView.returnRect(updateRect);
            _drawText = true;
            _updateParagraph = oldUpdate;
            _updateLine = oldLine;
            notifyAttachmentsForRange(newPosition.lineRange(),true);
        } else if (newPosition._redrawCurrentParagraphOnly ||
                   newPosition._redrawCurrentLineOnly ) {
            //System.out.println("Redraw paragraph");
            r = editParagraph.range();
            notifyAttachmentsForRange(r,true);
            dirtyRange(r);
        } else {
            //System.out.println("Redraw until the end");
            r = new Range(editParagraph._startChar, length() - editParagraph._startChar);
            notifyAttachmentsForRange(r,true);
            dirtyRange(r);
        }
    }

    /** ALERT: This is one of the only remaining method that does not use
      * replaceRangeWithString() the reason is that replaceRangeWithString does
      * not know how to refresh exactly one line when possible.
      */
    private void fastDeleteChar(boolean after) {
        TextParagraph           editParagraph, lastParagraph, nextParagraph,
            previousParagraph, oldUpdate;
        TextPositionInfo        newPosition, insertionPointInfo;
        KeyEvent                nextKey;
        Rect                    updateRect;
        String                  theString;
        int                     i, startLine, oldHeight, newHeight, index,
            insertionPoint, oldLine, insertCount = 0;
        TextParagraphFormat     format;

        if (!isEditing()) {
            //                application().beep();
            return;
        }


        insertionPoint = _selection.insertionPoint();
        if (!after && insertionPoint == 0 )
            return;

        if( after && insertionPoint == length())
            return;

        if( after )
            insertionPoint++; /* We bump insertion point to delete the char after */

        editParagraph = _paragraphForIndex(insertionPoint);

        if (editParagraph == null) {
            return;
        }


        notifyAttachmentsForRange(new Range(insertionPoint,1),false);
        lastParagraph = lastParagraph();
        if (lastParagraph != null) {
            oldHeight = lastParagraph._y + lastParagraph._height;
        } else {
            oldHeight = 0;
        }

        if (editParagraph._startChar == insertionPoint &&
               !isOnlyParagraph(editParagraph)) {
            index = _paragraphVector.indexOfIdentical(editParagraph);
            previousParagraph = (TextParagraph)_paragraphVector.elementAt(
                                                                index - 1);
            if (previousParagraph != null) {
                previousParagraph.subsumeParagraph(editParagraph);
                _paragraphVector.removeElement(editParagraph);
                editParagraph = previousParagraph;
                formatParagraph(editParagraph);
            }

            if (insertionPoint == 0) {
                newPosition = editParagraph.infoForPosition(0, -1);
            } else {
                newPosition = editParagraph.infoForPosition(insertionPoint - 1, -1);
            }
        } else {
            newPosition = editParagraph.removeCharAt(insertionPoint);
            _charCount--;
        }

        insertionPointInfo = newPosition;

        newHeight = adjustCharCountsAndSpacing();

        sizeBy(0, newHeight - bounds.height);

        if (newPosition == null) {
            _selection.showInsertionPoint();
            return;
        }

        notifyAttachmentsForRange(newPosition._textRun._paragraph.range(),true);

        if(! after ) {
            if (insertionPoint == 0) {
                newPosition.setAbsPosition(0);
            } else {
                newPosition.setAbsPosition(insertionPoint - 1);
            }
            _selection.setInsertionPoint(newPosition);
            _selectionChanged();
        } else {
            /** Selection did not change but we have to do
             *  this to make sure the caret has the rigth
             *  size
             */
            _selection.setInsertionPoint(newPosition);
        }


        format = editParagraph.currentParagraphFormat();

        if (newPosition._redrawCurrentLineOnly && format._justification == Graphics.LEFT_JUSTIFIED) {
            oldUpdate = _updateParagraph;
            oldLine = _updateLine;

            _updateParagraph = editParagraph;
            _updateLine = newPosition._updateLine;
            _drawText = false;
            updateRect = editParagraph.rectForLine(newPosition._lineNumber);
            updateRect.setBounds(insertionPointInfo._x, updateRect.y,
                                 updateRect.width -
                                 (insertionPointInfo._x - updateRect.x),
                                 updateRect.height);
            addDirtyRect(updateRect);
            TextView.returnRect(updateRect);
            _drawText = true;

            _updateParagraph = oldUpdate;
            _updateLine = oldLine;
        } else if (newPosition._redrawCurrentParagraphOnly ||
                   newPosition._redrawCurrentLineOnly ) {
            dirtyRange( editParagraph.range());
        } else {
            Range dirtyRange = allocateRange(editParagraph._startChar, length() - editParagraph._startChar);
            dirtyRange( dirtyRange );
            recycleRange( dirtyRange );
        }

    }

    private Rect insertReturn() {
        Rect result;
        int insertionPoint;
        if (_selection.isARange()) {
            deleteSelection();
        }
        insertionPoint = _selection.insertionPoint();
        result = insertReturn( insertionPoint );
        _selection.setRange(insertionPoint + 1, insertionPoint + 1);
        _selectionChanged();
        return result;
    }

    private Rect insertReturn(int insertionPoint) {
        TextParagraph           editParagraph, oldPara;
        int                     index;

        index = _paragraphIndexForIndex(insertionPoint);
        if(index == -1)
          index = _paragraphVector.count() - 1;
        editParagraph = (TextParagraph)_paragraphVector.elementAt(index);

        oldPara = editParagraph;
        editParagraph = editParagraph.createNewParagraphAt(insertionPoint);
        formatParagraphAtIndex(index);
        _charCount++;
        _paragraphVector.insertElementAt(editParagraph, index + 1);
        formatParagraphAtIndex(index + 1);
        return TextView.newRect(0, oldPara._y, bounds.width,
                                bounds.height - oldPara._y);
    }


    private TextStyleRun _runForIndex( int index ) {
        int length = length();
        if( length > 0 && index >= length )
            return _runForIndex( length - 1);

        if( index >= 0 ) {
            TextParagraph p = _paragraphForIndex(index);
            if( p != null )
                return p.runForCharPosition(index);
            else {
                return null;
            }
        } else
            return null;
    }


    private boolean equalsAttributesHint(Hashtable a1,Hashtable a2) {
        if( a1 == a2 )
            return true;
        else if( a1 == null || a2 == null )
            return false;
        else {
            return false; /* This is just a hint. May want to implement more for performance purpose */
        }
    }

    private void _keyDown() {
        TextParagraph           editParagraph, oldPara;
        TextPositionInfo        newPosition, currentPosition;
        Rect                    updateRect, tmpRect;
        KeyEvent                event;
        Event                   revEvent;
        int                     insertionPoint, index,newPos;
        boolean                 scrollToVisible;

        revEvent = (Event)_eventVector.removeFirstElement();

        if (!(revEvent instanceof KeyEvent)) {
            return;
        }
        event = (KeyEvent)revEvent;

        _selection.disableInsertionPoint();

        insertionPoint = _selection.insertionPoint();
        editParagraph = _paragraphForIndex(insertionPoint);

        scrollToVisible = true;
        if (event.isReturnKey()) {
            Range r = new Range(selectedRange());
            if( _owner != null )
                _owner.textWillChange(this,r);

            updateRect = insertReturn();

            if( _owner != null ) {
                r.length = 1;
                _owner.textDidChange(this,r);
            }
            draw(updateRect);
            TextView.returnRect(updateRect);
        } else if (event.isLeftArrowKey()) {
            if( event.isShiftKeyDown()) {
              newPos = _selection.orderedSelectionEnd() - 1;
              if( newPos < 0 )
                  newPos = 0;
              _selection.setRange(_selection.orderedSelectionStart(),
                                  newPos ,true);
            } else {
              if (insertionPoint == -1)
                  _selection.setRange(_selection.selectionStart(),
                                      _selection.selectionStart(),true);
              else {
                  newPos = insertionPoint - 1;
                  if( newPos < 0 )
                      newPos = 0;
                  _selection.setRange(newPos,newPos,true);
              }
            }
            _selectionChanged();
        } else if (event.isRightArrowKey()) {
            if( event.isShiftKeyDown()) {
                _selection.setRange(_selection.orderedSelectionStart(),
                                    _selection.orderedSelectionEnd() + 1,false);
            } else {
                if (insertionPoint == -1) {
                    _selection.setRange(_selection.selectionEnd(),
                                        _selection.selectionEnd());
                } else {
                    _selection.setRange(insertionPoint + 1,
                                        insertionPoint + 1,false);
                }
            }
            _selectionChanged();
        } else if (event.isUpArrowKey()) {
            currentPosition = _selection.orderedSelectionEndInfo();

            newPosition = positionForPoint(currentPosition._x,
                                           currentPosition._y - 1,false);
            if (newPosition != null) {
                newPosition.representCharacterBeforeEndOfLine();
                if( event.isShiftKeyDown()) {
                    if( newPosition._absPosition == currentPosition._absPosition &&
                        newPosition._absPosition > 0 )
                        _selection.setRange(_selection.orderedSelectionStart(),
                                            newPosition._absPosition-1,null,false,true);
                    else
                        _selection.setRange(_selection.orderedSelectionStart(),
                                            newPosition._absPosition,newPosition,false,true);
                } else {
                    if(!(currentPosition._lineNumber == 0 &&
                         currentPosition._textRun._paragraph == _paragraphVector.elementAt(0))) {
                        _selection.setInsertionPoint(newPosition);
                    }
                }
                _selectionChanged();
            }
        } else if (event.isDownArrowKey()) {
            currentPosition = _selection.orderedSelectionEndInfo();
            currentPosition.representCharacterBeforeEndOfLine();
            newPosition = positionForPoint(currentPosition._x,
                                           currentPosition._y +
                                           currentPosition._lineHeight + 1,false);

            if (newPosition != null) {
                if( event.isShiftKeyDown())
                    _selection.setRange(_selection.orderedSelectionStart(),
                                        newPosition._absPosition,newPosition,false,false);
                else {
                    if( newPosition._textRun._paragraph != currentPosition._textRun._paragraph ||
                        newPosition._y != currentPosition._y)
                        _selection.setInsertionPoint(newPosition);
                }
                _selectionChanged();
            }
        } else if( event.isHomeKey()) {
            Range r = selectedRange();
            TextPositionInfo lineInfo = _selection.orderedSelectionEndInfo();
            Range selectedLine = lineForPosition(lineInfo);
            TextPositionInfo homeInfo;

            homeInfo = positionInfoForIndex(selectedLine.index);
            if( homeInfo != null ) {
                if( homeInfo._y != lineInfo._y )
                    homeInfo.representCharacterAfterEndOfLine();

                if( event.isShiftKeyDown()) {
                    if( homeInfo._absPosition != lineInfo._absPosition )
                        _selection.setRange(_selection.orderedSelectionStart(),
                                            homeInfo._absPosition,homeInfo,
                                            false,false);
                } else
                    _selection.setInsertionPoint(homeInfo);
            }
            _selectionChanged();
        } else if( event.isEndKey())  {
            Range r = selectedRange();
            TextPositionInfo lineInfo = _selection.orderedSelectionEndInfo();
            Range selectedLine = lineForPosition(lineInfo);
            TextPositionInfo endInfo;

            endInfo = positionInfoForIndex(selectedLine.index + selectedLine.length);
            if( endInfo != null ) {
                if( endInfo._y != lineInfo._y )
                    endInfo.representCharacterAfterEndOfLine();

                if( event.isShiftKeyDown()) {
                    if(endInfo._absPosition  != lineInfo._absPosition)
                        _selection.setRange(_selection.orderedSelectionStart(),
                                            endInfo._absPosition,endInfo,
                                            false,false);
                } else
                    _selection.setInsertionPoint(endInfo);
            }
            _selectionChanged();
        } else if (event.isBackspaceKey()) {
            Range r = new Range(selectedRange());

            if (_selection.isARange()) {

                 if( _owner != null )
                     _owner.textWillChange(this,r);

                deleteSelection();

                if( _owner != null ) {
                    r.length = 0;
                    _owner.textDidChange(this,r);
                }
            } else {
                if( _owner != null ) {
                    r.index--;
                    r.length = 1;
                    _owner.textWillChange(this,r);
                }

                fastDeleteChar(false);

                if( _owner != null ) {
                    r.length = 0;
                    _owner.textDidChange(this,r);
                }
            }
        } else if (event.isDeleteKey()) {
            Range r = selectedRange();

            if(_selection.isARange()) {
                if( _owner != null )
                    _owner.textWillChange(this,r);

                deleteSelection();

                if( _owner != null ) {
                    r.length = 0;
                    _owner.textDidChange(this,r);
                }
            } else {
              if(r.index < length()) {
                  r.length = 1;
                  if( _owner != null )
                      _owner.textWillChange(this,r);

                  fastDeleteChar(true);

                  r.length = 0;
                  if( _owner != null )
                      _owner.textDidChange(this,r);
              }
            }
        } else if (event.isPrintableKey()) {
            Range r = selectedRange();
            Range newSelection = allocateRange();
            replaceRangeWithString( r , "" + (char)event.key );
            if(_typingAttributes.count() > 0) {
                newSelection.index  = r.index;
                newSelection.length = 1;
                addAttributesForRangeWithoutNotification(_typingAttributes,newSelection);
                clearTypingAttributes();
            }
            newSelection.index  = r.index + 1;
            newSelection.length = 0;
            selectRange(newSelection);
            recycleRange(newSelection);
        } else if( event.isTabKey()) {
            Range r = selectedRange();
            Range newSelection = allocateRange();
            replaceRangeWithString( r, "\t");
            newSelection.index = r.index + 1;
            newSelection.length=0;
            selectRange( newSelection );
            recycleRange( newSelection );
        } else {
            scrollToVisible = false;
        }

        if (scrollToVisible) {
            Range r = new Range(_selection.orderedSelectionEnd(),0);
            /* We do this for an edge case:
             * TextView contains an attachment that has the same size than the visible
             * rect and the user types before the textview */
            if( r.index > 0 ){
                r.index--;
                r.length++;
            }
            scrollRangeToVisible(r);
        }

        _selection.enableInsertionPoint();
    }

    private Range paragraphsRangeForRange(Range r) {
        TextParagraph   nextParagraph;
        int             count, i,lastLocation;
        Range result = allocateRange();
        boolean hasIndex=false;
        count = _paragraphVector.count();

        lastLocation = r.index + r.length;
        for (i = 0; i < count; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);

            if( !hasIndex ) {
                if (r.index >= nextParagraph._startChar &&
                    r.index < (nextParagraph._startChar + nextParagraph._charCount)) {
                    result.index = i;
                    hasIndex=true;
                }
            }
            if( hasIndex ) {
                if( lastLocation >= nextParagraph._startChar &&
                    lastLocation < (nextParagraph._startChar + nextParagraph._charCount)) {
                    result.length = i - result.index + 1;
                    break;
                }
            }
        }
        return result;
    }




    /**
         * Returns the TextStyleRuns that comprise the current selection.  Forms new
         * TextStyleRuns if a selection endpoint falls within a single run.  If
         * an empty selection, returns (creates if necessary) a TextStyleRun at the
         * insertion point containing no characters.
         */
    private Vector createAndReturnRunsForRange(Range aRange) {
        TextParagraph           startParagraph, endParagraph, nextParagraph;
        TextStyleRun            startRun, endRun;
        Vector               runVector, tmpVector;
        int                     start, end, i;
        boolean                 sameParagraph;
        int absPosition,endAbsPosition;

        absPosition = aRange.index;
        endAbsPosition = aRange.index+aRange.length;
        startParagraph = _paragraphForIndex(absPosition);
        endParagraph   = _paragraphForIndex(endAbsPosition);
        sameParagraph = (startParagraph == endParagraph);

        if (aRange.length == 0 ) {
            /* if an insertion point, get an empty run */
            startRun = startParagraph.createNewRunAt(absPosition);
            if (startRun.charCount() > 0) {
                startRun = startParagraph.createNewRunAt( absPosition );
            }
            runVector = TextView.newVector();
            runVector.addElement(startRun);
            return runVector;
        }

        /* cleave the last part of the first selected paragraph */
        startRun = startParagraph.runForCharPosition(absPosition);
        if( startRun.rangeIndex() != absPosition )
            startRun = startParagraph.createNewRunAt(absPosition);


        /* cleave the first part of the last selected paragraph */
        endRun = endParagraph.runForCharPosition(endAbsPosition);
        if( endAbsPosition <= (endRun.rangeIndex() + endRun.charCount()-1)) {
            endRun = endParagraph.createNewRunAt(endAbsPosition);
            endRun = endParagraph.runBefore(endRun);
        }

        if (sameParagraph) {
            Vector result;
            if( startRun == endRun ) {
                result = new Vector();
                result.addElement(startRun);
                return result;
            } else {
                result = startParagraph.runsFromTo(startRun, endRun);
                return result;
            }
        }

        runVector = TextView.newVector();

        runVector.addElement(startRun);
        tmpVector = startParagraph.runsAfter(startRun);
        runVector.addElementsIfAbsent(tmpVector);
        TextView.returnVector(tmpVector);

        /* grab the paragraphs in between */
        start = i = _paragraphVector.indexOfIdentical(startParagraph) + 1;
        end = _paragraphVector.indexOfIdentical(endParagraph);
        for (; i < end; i++) {
            nextParagraph = (TextParagraph)_paragraphVector.elementAt(i);
            runVector.addElementsIfAbsent(nextParagraph.runVector());
        }

        /* add the runs in the final paragraph */
        tmpVector = endParagraph.runsBefore(endRun);
        tmpVector.addElement( endRun );
        runVector.addElementsIfAbsent(tmpVector);

        /* This should not happen when setting
           some attribute accross several para */
        TextView.returnVector(tmpVector);

        return runVector;
    }


    /**
         * Changes the font of the currently-selected text.
         */
    private void processSetFont(Font aFont) {
        Range s = selectedRange();
        if( s.length > 0 ) {
            Hashtable newAttr = new Hashtable();
            newAttr.put( FONT_KEY , aFont );
            addAttributesForRange( newAttr , s);
        } else
            addTypingAttribute(FONT_KEY,aFont);
    }



    /**
         * Removes the current selection from the TextView and places the paragraphs
         * containing the text in the Vector <b>paragraphVector</b>.  If
         * <b>paragraphVector</b> is <b>null</b>, this method just throws away
         * the paragraphs.  This method resizes and redisplays the TextView.
         *
         * ALERT: This one the only remaining method not calling replaceRangeWithString()
         */
    private void deleteSelection(Vector paragraphVector) {
        Range r = selectedRange();
        deleteRange(r,paragraphVector);
        _selection.setRange(r.index,r.index);
        _selectionChanged();
    }

    private void deleteRange(Range aRange, Vector paragraphVector) {
        TextParagraph           newParagraph = null, nextParagraph,
            startParagraph, endParagraph;
        TextPositionInfo        rangeStartInfo, rangeEndInfo;
        TextStyleRun            startRun, endRun;
        Vector                  runVector;
        int                     i, start, end, currentHeight,index;
        boolean                 sameParagraph;
        Range dirtyRange;

        if( aRange.length == 0 )
            return;

        dirtyRange = allocateRange(aRange.index , length() - aRange.index);
        dirtyRange( dirtyRange );
        recycleRange( dirtyRange );

        notifyAttachmentsForRange(aRange,false);

        rangeStartInfo = positionInfoForIndex( aRange.index );
        rangeEndInfo   = positionInfoForIndex( aRange.index + aRange.length);
        startParagraph = rangeStartInfo._textRun._paragraph;
        endParagraph = rangeEndInfo._textRun._paragraph;
        sameParagraph = (startParagraph == endParagraph);

        /* grab the last part of the first selected paragraph */
        startRun = rangeStartInfo._textRun;
        if( startRun.rangeIndex() != rangeStartInfo._absPosition )
            startRun = startParagraph.createNewRunAt(rangeStartInfo._absPosition);
        if (!sameParagraph) {
            runVector = startParagraph.runsAfter(startRun);
            startParagraph.removeRun(startRun);
            startParagraph.removeRuns(runVector);

            if (paragraphVector != null) {
                newParagraph = new TextParagraph(this);
                newParagraph.setFormat(startParagraph._format);
                newParagraph.addRun(startRun);
                newParagraph.addRuns(runVector);
                paragraphVector.addElement(newParagraph);
            }

            runVector.removeAllElements();
            TextView.returnVector(runVector);
        }

        /* grab the first part of the last selected paragraph */
        endRun = rangeEndInfo._textRun;
        if(endRun.rangeIndex() != rangeEndInfo._absPosition)
            endRun = endParagraph.createNewRunAt(rangeEndInfo._absPosition);
        if (sameParagraph) {
            runVector = startParagraph.runsFromTo(startRun, endRun);
            runVector.removeElement(endRun);
            startParagraph.removeRuns(runVector);

            if (paragraphVector != null) {
                newParagraph = new TextParagraph(this);
                newParagraph.setFormat(startParagraph._format);
                newParagraph.addRuns(runVector);
                paragraphVector.addElement(newParagraph);
            }

            runVector.removeAllElements();
            TextView.returnVector(runVector);
        } else {
            /* grab the paragraphs in between */
            start = i = _paragraphVector.indexOfIdentical(startParagraph) + 1;
            end = _paragraphVector.indexOfIdentical(endParagraph);
            for (; i < end; i++) {
                nextParagraph =
                    (TextParagraph)_paragraphVector.removeElementAt(start);
                if (paragraphVector != null) {
                    paragraphVector.addElement(nextParagraph);
                }
            }

            /* add the runs in the final paragraph */
            runVector = endParagraph.runsBefore(endRun);
            endParagraph.removeRuns(runVector);

            if (paragraphVector != null) {
                newParagraph = new TextParagraph(this);
                newParagraph.setFormat(startParagraph._format);
                newParagraph.addRuns(runVector);
                paragraphVector.addElement(newParagraph);
            }

            runVector.removeAllElements();
            TextView.returnVector(runVector);

            /* combine last paragraph with first */
            startParagraph.subsumeParagraph(endParagraph);
            _paragraphVector.removeElement(endParagraph);
        }

        if (startParagraph.isEmpty() && !isOnlyParagraph(startParagraph)) {
            index = _paragraphVector.indexOfIdentical(startParagraph);
            _paragraphVector.removeElement(startParagraph);
            currentHeight = adjustCharCountsAndSpacing();
            sizeBy(0, currentHeight - bounds.height);
        } else {
          formatParagraph(startParagraph);
        }
    }

    private void deleteSelection() {
        deleteSelection(null);
    }


    boolean isOnlyParagraph(TextParagraph aParagraph) {
        if (_paragraphVector.count() == 1 &&
            _paragraphVector.contains(aParagraph)) {
            return true;
        }
        return false;
    }

    int selectionStart() {
        return _selection.selectionStart();
    }

    TextPositionInfo selectionStartInfo() {
        return _selection.selectionStartInfo();
    }

    int selectionEnd() {
        return _selection.selectionEnd();
    }

    TextPositionInfo selectionEndInfo() {
        return _selection.selectionEndInfo();
    }

    boolean hasSelectionRange() {
        return _selection.isARange();
    }

    TextParagraph lastParagraph() {
        return (TextParagraph)_paragraphVector.lastElement();
    }

    char characterAt(int absPosition) {
        TextParagraph   theParagraph;

        if (absPosition < 0 || absPosition > _charCount) {
            return '\0';
        }

        theParagraph = _paragraphForIndex(absPosition);
        if (theParagraph == null) {
            theParagraph = lastParagraph();
        }

        return theParagraph.characterAt(absPosition);
    }

    int _positionOfPreviousWord(int absPosition) {
        char            previousChar;
        boolean         done = false;

        if (absPosition == 0) {
            return 0;
        }

        previousChar = characterAt(absPosition--);
        if (previousChar == '\n') {
            return absPosition + 1;
        }

        if (previousChar == ' ' || previousChar == '\t') {
            do {
                previousChar = characterAt(absPosition--);
                done = (previousChar != ' ' && previousChar != '\t') ||
                    (previousChar == '\n');
            } while (absPosition > -1 && !done);
        } else {
            do {
                previousChar = characterAt(absPosition--);
                done = (previousChar == ' ' || previousChar == '\t' ||
                        (previousChar >= '!' && previousChar <= '/') ||
                        (previousChar >= ':' && previousChar <= '@') ||
                        (previousChar >= '[' && previousChar <= '\'') ||
                        (previousChar >= '{' && previousChar <= '~') ||
                        previousChar == '\n');
            } while (absPosition > -1 && !done);
        }

        if (done) {
            return absPosition + 2;
        }

        return 0;
    }

    int _positionOfNextWord(int absPosition) {
        char            previousChar, nextChar;
        boolean         done = false;

        if (absPosition >= _charCount) {
            return _charCount;
        }

        if (absPosition > 0) {
            previousChar = characterAt(absPosition - 1);
            if (previousChar == '\n') {
                return absPosition - 1;
            }
        }

        nextChar = characterAt(absPosition++);
        if (nextChar == ' ' || nextChar == '\t') {
            do {
                previousChar = nextChar;
                nextChar = characterAt(absPosition++);
                done = (nextChar != ' ' && nextChar != '\t') ||
                    (nextChar == '\n');
            } while (absPosition < _charCount && !done);
        } else {
            do {
                previousChar = nextChar;
                nextChar = characterAt(absPosition++);
                done = (nextChar == ' ' || nextChar == '\t' ||
                        (nextChar >= '!' && nextChar <= '/') ||
                        (nextChar >= ':' && nextChar <= '@') ||
                        (nextChar >= '[' && nextChar <= '\'') ||
                        (nextChar >= '{' && nextChar <= '~') ||
                        nextChar == '\n');
            } while (absPosition < _charCount && !done);
        }

        if (done) {
            return absPosition - 1;
        }

        return _charCount;
    }

    private void hideInsertionPoint() {
        if( insertionPointVisible ) {
            insertionPointVisible=false;
        }
    }

    private void showInsertionPoint() {
        if(! insertionPointVisible ) {
            insertionPointVisible = true;
        }
    }




    /* rect cache */

    static Rect newRect(int x, int y, int width, int height) {
        Rect    theRect;

        synchronized(_rectCache) {
            if (!_shouldCache || _rectCache.isEmpty()) {
                return new Rect(x, y, width, height);
            }

            theRect = (Rect)_rectCache.removeLastElement();
        }
        theRect.setBounds(x, y, width, height);

        return theRect;
    }

    /**
         * Returns a Rect from the Rect cache whose origin and size match
         * templateRect.  Creates a new Rect if the cache is empty.
         */
    static Rect newRect(Rect templateRect) {
        Rect    theRect;

        synchronized(_rectCache) {
            if (!_shouldCache || _rectCache.isEmpty()) {
                return new Rect(templateRect);
            }

            theRect = (Rect)_rectCache.removeLastElement();
        }
        theRect.setBounds(templateRect);

        return theRect;
    }

    /**
         * Returns a Rect from the Rect cache with origin (0, 0) and zero size.
         * Equivalent to
         * <pre>
         *  r = TextView.newRect(0, 0, 0, 0);
         * </pre>
         * Creates a new Rect if the cache is empty.
         */
    static Rect newRect() {
        return TextView.newRect(0, 0, 0, 0);
    }


    /**
         * Places r back in the Rect cache (if the cache isn't full).
         */
    static void returnRect(Rect r) {
        if( r == null )
            return;
        if (!_shouldCache) {
            return;
        }

        synchronized(_rectCache) {
            if (_rectCache.count() < 50) {
                _rectCache.addElement(r);
            }
        }
    }

    /**
         * Places the Rects contained in rectVector back in the Rect cache
         * (if the cache isn't full) and empties the Vector.
         */
    static void returnRects(Vector rectVector) {
        int     i;

        if (rectVector == null || !_shouldCache) {
            return;
        }

        i = rectVector.count();
        while (i-- > 0) {
            TextView.returnRect((Rect)rectVector.elementAt(i));
        }

        rectVector.removeAllElements();
    }

    /**
         * Enables and disables Rect caching.  With setShouldCacheRects(false),
         * TextView.newRect() methods create new Rects and TextView.returnRect()
         * methods do
         * nothing with the Rects they're given.  Disabling Rect caching can help
         * you track down problems in your code of returning Rects to the cache
         * while accidentally continuing to maintain a reference to them.
         */
    static void setShouldCacheRects(boolean flag) {
        synchronized(_rectCache) {
            _shouldCache = flag;
            if (!_shouldCache) {
                _rectCache.removeAllElements();
            }
        }
    }

    static Vector newVector() {
        Vector       theVector;

        synchronized(_vectorCache) {
            if (!_shouldCache || _vectorCache.isEmpty()) {
                return new Vector();
            }

            theVector = (Vector)_vectorCache.removeLastElement();
        }

        return theVector;
    }

    static void returnVector(Vector aVector) {
        if (!_shouldCache) {
            return;
        }

        synchronized(_vectorCache) {
            if (aVector != null && _vectorCache.count() < 15) {
                aVector.removeAllElements();
                _vectorCache.addElement(aVector);
            }
        }
    }

    static void setShouldCacheVectors(boolean flag) {
        synchronized(_vectorCache) {
            _shouldCache = flag;

            if (!_cacheVectors) {
                _vectorCache.removeAllElements();
            }
        }
    }

    private void _selectionChanged() {
        /* Make sure we do not call selectionChanged
         * or reset typing attributes when the selection
         * did not change
         */
        int start = _selection.selectionStart();
        int  end  = _selection.selectionEnd();
        if( start == _wasSelectedRange.index &&
            (end - start) == _wasSelectedRange.length )
            return;

        _wasSelectedRange.index = start;
        _wasSelectedRange.length = end - start;
        clearTypingAttributes();
        if( _owner != null ) {
            _owner.selectionDidChange(this);
        }
    }

    void dirtyRange(Range aRange) {
        /* If we are dirty don't spend some time finding the dirty rect */

        if( isDirty() && dirtyRect == null )
            return;
        else {
            Range r = allocateRange(aRange);
            Rect visibleRect;
            visibleRect = new Rect();
            computeVisibleRect( visibleRect );
            r.intersectWith(0,length());
            if( _superview != null && r != null && !r.isNullRange() && r.length > 0 ) {
                Rect rect;
                Vector dirtyRects = rectsForRange(r,visibleRect);
                int i,c;
                for(i=0,c=dirtyRects.count() ; i < c ; i++ ) {
                    rect = (Rect) dirtyRects.elementAt(i);
                    rect.x = 0;
                    rect.width = bounds.width;
                    if( rect.width > 0 && rect.height > 0 ) {
                        this.addDirtyRect((Rect)rect);
                    }
                }
            }
            recycleRange( r );
        }
    }


    private TextParagraphFormat _formatForTextPositionInfo(TextPositionInfo info) {
        TextParagraphFormat f = info._textRun.paragraph().format();
        if( f == null )
            f = (TextParagraphFormat) _defaultAttributes.get(PARAGRAPH_FORMAT_KEY);
        return f;
    }

    private TextPositionInfo positionInfoForNextLine(TextPositionInfo info ) {
        TextPositionInfo result;

        int nextLineFirstChar = info._textRun.paragraph().characterStartingLine( info._lineNumber + 1);
        if( nextLineFirstChar == -1 ) {

            int nextParagraphIndex = _paragraphVector.indexOfIdentical((Object)info._textRun.paragraph() ) + 1;
            TextParagraph nextParagraph;

            if( nextParagraphIndex < _paragraphVector.count()) {
                nextParagraph = (TextParagraph) _paragraphVector.elementAt(nextParagraphIndex);
                return positionInfoForIndex( nextParagraph._startChar );
            } else
                return null;
        }

        result = positionInfoForIndex( nextLineFirstChar );
        return result;
    }

    void setBaseURL(URL baseURL) {
        _baseURL = baseURL;
    }





    private TextStyleRun runBefore(TextStyleRun aRun) {
        TextStyleRun run = aRun.paragraph().runBefore(aRun);
        if( run == null ) {
            int index = _paragraphVector.indexOfIdentical( aRun.paragraph());
            if( index > 0 )
                return ((TextParagraph)_paragraphVector.elementAt(index - 1)).lastRun();
        }
        return run;
    }

    private TextStyleRun runAfter(TextStyleRun aRun) {
        TextStyleRun run = aRun.paragraph().runAfter(aRun);
        if( run == null ) {
            int index = _paragraphVector.indexOfIdentical( aRun.paragraph());
            if( index < (_paragraphVector.count()-1))
                return ((TextParagraph)_paragraphVector.elementAt(index + 1)).firstRun();
        }
        return run;
    }

    private Range linkRangeForPosition(int index) {
        TextStyleRun run,orig,firstRun,lastRun;
        Hashtable attr;
        String url;
        Range result;

        orig = _runForIndex(index);
        if( (attr = orig.attributes()) != null &&
            (url = (String) attr.get(LINK_KEY)) != null) {

            run = firstRun = orig;
            while( true ) {
                run = runBefore(run);
                if( run == null ||
                    (attr = run.attributes()) == null ||
                    (!url.equals((String)attr.get(LINK_KEY))))
                    break;
                firstRun = run;
            }


            run = lastRun = orig;
            while( true ) {
                run = runAfter( run );
                if( run == null ||
                    (attr = run.attributes()) == null ||
                    (!url.equals((String)attr.get(LINK_KEY))))
                    break;
                lastRun = run;
            }

            result = firstRun.range();
            result.unionWith(lastRun.range());
            return result;
        }
        return null;
    }

    private void highlightLinkWithRange(Range aRange,boolean flag) {
        Range r;
        int i,c;

        if( flag )
            addAttributeForRange( LINK_IS_PRESSED_KEY , "", aRange);
        else
            removeAttributeForRange( LINK_IS_PRESSED_KEY,aRange);

        /** Collect empty runs created during the change of attributes */
        r = paragraphsRangeForRange(aRange);
        for(i=r.index,c=r.index+r.length ; i < c ; i++)
            ((TextParagraph)_paragraphVector.elementAt(i)).collectEmptyRuns();
    }

    private boolean runUnderMouse(TextStyleRun run,int x,int y) {
        int i,c;
        Vector rects = rectsForRange(run.range());
        for(i=0,c=rects.count(); i < c ; i++ )
            if( ((Rect)rects.elementAt(i)).contains(x,y))
                return true;

        return false;
    }

    private boolean runsUnderMouse(Vector runs,int x,int y) {
        int i,c;
        Range r;
        TextStyleRun run;

        for(i=0,c=runs.count() ; i < c ; i++ ) {
            r = (Range)runs.elementAt(i);
            run = _runForIndex(r.index);
            if(runUnderMouse(run,x,y))
                return true;
        }
        return false;
    }

    boolean lastParagraphIsEmpty() {
        return (lastParagraph()._charCount == 0);
    }


    char charAt(int index) {
        String s = stringForRange( new Range( index , 1));
        if( s != null && s.length() > 0 )
            return s.charAt(0);
        else
            return '\0';
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
        int length = length();
        int relFirstIndex,relLastIndex;
        int i,l;
        char c;

        i = index;
        c = charAt(i);

        if( c == '\n') {
            return new Range(i,1);
        }

        if( c == ' ' || c == '\t' ) {
            while(i>0) {
                c = charAt(i);
                if( c == ' ' || c == '\t' )
                    i--;
                else
                    break;
            }
            relFirstIndex = i+1;

            i = index;

            while(i<length()) {
                c = charAt(i);
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
            c = charAt(relFirstIndex-1);
            if(!isWordCharacter(c))
                break;
            relFirstIndex--;
        }


        relLastIndex = i;
        while( relLastIndex < (length-1)) {
            c = charAt(relLastIndex+1);
            if(!isWordCharacter(c))
                break;
            relLastIndex++;
        }

        return new Range(relFirstIndex, relLastIndex - relFirstIndex + 1);
    }

    Range lineForPosition(TextPositionInfo info) {
        TextParagraph p = info._textRun._paragraph;
        int lineBegin,lineEnd;

        if( p != null ) {
            if( info._lineNumber == 0 )
                lineBegin = p._startChar;
            else
                lineBegin = p._startChar + p._lineBreaks[ info._lineNumber - 1 ];

            lineEnd = p._startChar + p._lineBreaks[info._lineNumber] - 1;
            return new Range( lineBegin , lineEnd - lineBegin + 1);
        }
        return new Range();/** null range */
    }

    void replaceContentWithString(String aString) {
        TextParagraph p;
        TextStyleRun  r;
        int i,c,nextBreak;
        int firstIndex,lastIndex;

        notifyAttachmentsForRange(new Range(0,length()),false);
        _paragraphVector.removeAllElements();

        i = 0;
        c = aString.length();
        while( i < c ) {
            nextBreak = aString.indexOf('\n',i);
            if( nextBreak == -1 ) {
                firstIndex = i;
                lastIndex  = c;
                i = c;
            } else {
                firstIndex = i;
                lastIndex  = nextBreak;
                i = nextBreak + 1;
            }
            p = new TextParagraph(this);
            r = new TextStyleRun(p,aString,firstIndex,lastIndex,null);
            p.addRun( r );
            _paragraphVector.addElement(p);
        }

        /* Never leave a textview without any paragraph
         * or add the last \n
         */
        if( _paragraphVector.count() == 0 || aString.charAt(c-1) == '\n') {
            p = new TextParagraph(this);
            r = new TextStyleRun(p,"",null);
            p.addRun(r);
            _paragraphVector.addElement(p);
        }
        this.setDirty(true);
        reformatAll();
    }

   Vector rectsForRange(Range aRange,Rect maxRect) {
       Range r = allocateRange(aRange.index,aRange.length);
       Vector result = new Vector();
       TextPositionInfo start,end,info;
       TextParagraphFormat f;
       Rect rect,lastRect;
       int i,c,lastY;
       boolean done;
       TextParagraph p;
       int lineRemainder;

       r.intersectWith(0,length());

       if( r.length == 0 || r.isNullRange()) {
           recycleRange(r);
           return result;
       }

       start = positionInfoForIndex( r.index );
       if( start._endOfLine && !start._endOfParagraph )
           start.representCharacterAfterEndOfLine();
       end   = positionInfoForIndex( r.index + r.length());
       //System.out.println("Start is " + start);
       //System.out.println("End is " + end);
       if( start == null || end == null ){
           recycleRange(r);
           return result;
       }

       if( start._textRun.paragraph() == end._textRun.paragraph() &&
           start._lineNumber == end._lineNumber ) {

           f = _formatForTextPositionInfo(start);
           if( end._endOfLine ) {
               p = start._textRun.paragraph();
               lineRemainder = p._lineRemainders[start._lineNumber];
               switch( f._justification ) {
               case Graphics.CENTERED:
                   result.addElement(new Rect(start._x,start._y,
                                              bounds.width - f._rightMargin - start._x -
                                              (lineRemainder / 2),
                                              start._lineHeight));
                   break;
               case Graphics.RIGHT_JUSTIFIED:
                   result.addElement(new Rect(start._x,start._y,
                                              bounds.width - f._rightMargin - start._x,
                                              start._lineHeight));
                   break;
               case Graphics.LEFT_JUSTIFIED:
               default:
                   result.addElement(new Rect(start._x,start._y,
                                              bounds.width - f._rightMargin - start._x -
                                              lineRemainder,
                                              start._lineHeight));
                   break;

               }
           } else
               result.addElement(new Rect(start._x,start._y,end._x - start._x , start._lineHeight));
           recycleRange(r);
           return result;
       }

       f = _formatForTextPositionInfo(start);
       p = start._textRun.paragraph();
       lineRemainder = p._lineRemainders[start._lineNumber];
       switch( f._justification ) {
       case Graphics.CENTERED:
           rect = new Rect(start._x ,start._y, bounds.width - f._rightMargin - start._x -
                           (lineRemainder/2), start._lineHeight);
           break;
       case Graphics.RIGHT_JUSTIFIED:
           rect = new Rect(start._x ,start._y, bounds.width - f._rightMargin - start._x ,
                           start._lineHeight);
           break;
       case Graphics.LEFT_JUSTIFIED:
       default:
           rect = new Rect(start._x ,start._y, bounds.width - f._rightMargin - start._x -
                           lineRemainder, start._lineHeight);
           break;
       }
       /** If width == 0 add the rect anyway to refresh empty lines
        *  while selecting
        */
       if( rect.height > 0 )
           result.addElement(rect);

       rect = new Rect(0,0,0,0);
       info = start;

       done = false;
       lastY = -1;
       while (!done)  {
           info = positionInfoForNextLine( info );
           if( info == null )
               break;

           if( info._endOfLine && !info._endOfParagraph)
               info.representCharacterAfterEndOfLine();

           if(info._y <= lastY) { /** No more progress. Layout is not done yet
                                   *  new text will be layout later.
                                   */
               break;
           } else
               lastY = info._y;

           if( maxRect != null ) {
               if( info._y  <  maxRect.y )
                   continue;

               if( info._y > (maxRect.y + maxRect.height)) {
                   done = true;
                   break;
               }
           }


           f = _formatForTextPositionInfo( info );
           if( info._textRun.paragraph() != end._textRun.paragraph() ||
               info._lineNumber < end._lineNumber ) {
               rect.x = info._x;
               rect.y = info._y;
               p = info._textRun.paragraph();
               lineRemainder = p._lineRemainders[info._lineNumber];
               switch( f._justification) {
               case Graphics.CENTERED:
                   rect.width  = bounds.width - f._rightMargin - rect.x -
                       (lineRemainder / 2);
                   break;
               case Graphics.RIGHT_JUSTIFIED:
                   rect.width  = bounds.width - f._rightMargin - rect.x;
                   break;
               case Graphics.LEFT_JUSTIFIED:
               default:
                   rect.width  = bounds.width - f._rightMargin - rect.x -
                       lineRemainder;
                   break;
               }
               rect.height = info._lineHeight;
           } else {
               rect.x = info._x;
               rect.y = info._y;
               rect.width  = end._x - rect.x;
               rect.height = info._lineHeight;
               done = true;
           }

           if( rect.height > 0 ) {
               lastRect = (Rect) result.lastElement();
               if( lastRect != null && lastRect.x == rect.x && lastRect.width == rect.width ) {
                   lastRect.height = (rect.y + rect.height) - lastRect.y;
               } else {
                   result.addElement(new Rect(rect));
               }
           }
       }
       recycleRange(r);
       return result;
   }

    Vector rangesOfVisibleAttachmentsWithBitmap(Bitmap aBitmap) {
        Vector result = new Vector();
        TextPositionInfo info;
        TextStyleRun     run;
        int firstIndex,lastIndex;
        Rect r = new Rect();
        Hashtable attributes;
        TextAttachment attachment;

        computeVisibleRect(r);


        info = positionForPoint(r.x,r.y,true);
        if( info == null )
            firstIndex = 0;
        else if( info._absPosition > 0 )
            firstIndex = info._absPosition - 1;
        else
            firstIndex = 0;

        info = positionForPoint(r.x + r.width , r.y + r.height,true);
        if( info == null )
            lastIndex = length() - 1;
        else if( info._absPosition < (length()-1))
            lastIndex = info._absPosition + 1;
        else
            lastIndex = length() - 1;

        run = _runForIndex(firstIndex);
        while( run != null ) {
            attributes = run.attributes();
            if(attributes != null && (attachment = (TextAttachment)
                                      attributes.get(TEXT_ATTACHMENT_KEY)) != null ) {
                if( attachment instanceof ImageAttachment ) {
                    if( ((ImageAttachment)attachment).image() == (Image) aBitmap)
                        result.addElement(run.range());
                }
            }
            run = runAfter( run );
            if( run.rangeIndex() > lastIndex )
                break;
        }

        return result;
    }

    void refreshBitmap(Object data) {
        Vector ranges;
        Bitmap bm = (Bitmap) data;
        Range attachmentRange;
        Rect imageRect;
        Rect r;
        int i,c;
        int j,d;

        ranges = rangesOfVisibleAttachmentsWithBitmap(bm);

        for(j=0,d=ranges.count() ; j < d ; j++ ) {
          attachmentRange = (Range) ranges.elementAt(j);
          if( !attachmentRange.isNullRange()) {
              Vector v = rectsForRange(attachmentRange);
              imageRect = bm.updateRect();

              if (v.count() > 0 ) {
                  r = (Rect) v.elementAt(0);
                  for(i=1,c=v.count() ; i < c ; i++ )
                      r.unionWith((Rect)v.elementAt(i));
                  r.x = 0;
                  r.width = bounds.width;
                  r.y += imageRect.y;
                  r.height= imageRect.height;
                  addDirtyRect(r);
              }
          }
        }
    }

    boolean attributesChangingFormatting(Hashtable attr) {
        int i,c;
        Vector keysVector;

        if( attr != null ) {
            keysVector = attr.keysVector();
            for(i=0,c=keysVector.count() ; i < c ; i++ )
                if(attributesChangingFormatting.indexOf(keysVector.elementAt(i)) != -1 ) {
                    return true;
                }
        }

        return false;
    }

    void clearTypingAttributes() {
        if( _typingAttributes != null )
            _typingAttributes.clear();
    }

    void addAttributesForRangeWithoutNotification(Hashtable attributes, Range range) {
        Range  paragraphsRange;
        Vector runsVector;
        TextStyleRun run;
        TextParagraphFormat format;
        TextParagraph  paragraph;
        int i,c;
        Range runRange,paragraphRange;
        Range selectedRange = selectedRange();
        Range dirtyRange    = allocateRange();
        Vector toBeFormatted = new Vector();
        TextAttachment ti;

        if( attributes == null ) {
            recycleRange( dirtyRange);
            return;
        }

        if((ti = (TextAttachment) attributes.get(TEXT_ATTACHMENT_KEY)) != null )
            ti.setOwner(this);

        if( (format = (TextParagraphFormat)attributes.get(PARAGRAPH_FORMAT_KEY)) != null ) {
            paragraphsRange = paragraphsRangeForRange(range);

            for(i=paragraphsRange.index,c=paragraphsRange.index + paragraphsRange.length ;
                i < c ; i++ ) {
                paragraph = (TextParagraph)_paragraphVector.elementAt(i);
                paragraph.setFormat(format);
                toBeFormatted.addElementIfAbsent(paragraph);
                dirtyRange.unionWith(paragraph.range());
            }

            if( attributes.count() == 1 ) {
                for(i=0,c=toBeFormatted.count() ; i < c ; i++ )
                    formatParagraph((TextParagraph)toBeFormatted.elementAt(i));
                dirtyRange( dirtyRange );
                if( formattingEnabled() )
                    _selection.setRange(selectedRange.index,selectedRange.index+selectedRange.length);
                recycleRange( dirtyRange );
                recycleRange( paragraphsRange );
                return;
            }
            recycleRange( paragraphsRange );
        }

        run = _runForIndex(range.index);

        if( run != null ) {
            runRange = run.range();

            if( range.equals(runRange)) {
                run.appendAttributes(attributes);
                dirtyRange.unionWith(run.range());
                if(attributesChangingFormatting(attributes))
                    toBeFormatted.addElementIfAbsent( _paragraphForIndex(range.index));
                for(i=0,c=toBeFormatted.count() ; i < c ; i++ ) {
                    paragraph = (TextParagraph) toBeFormatted.elementAt(i);
                    formatParagraph(paragraph);
                    dirtyRange.unionWith(paragraph.range());
                }
                dirtyRange( dirtyRange );
                if( formattingEnabled() )
                    _selection.setRange(selectedRange.index,selectedRange.index+selectedRange.length);
                recycleRange( dirtyRange );
                recycleRange(runRange);
                return;
            } else if( range.index >= runRange.index &&
                       (range.index + range.length) <= (runRange.index+runRange.length) &&
                       equalsAttributesHint(attributes,run.attributes())) {
                recycleRange( dirtyRange );
                recycleRange( runRange );
                return;
            }
        }

        runsVector = createAndReturnRunsForRange(range);

        for(i=0,c=runsVector.count() ; i < c ; i++ ) {
            run = (TextStyleRun) runsVector.elementAt(i);
            run.appendAttributes(attributes);
            runRange = run.range();
            dirtyRange.unionWith(runRange);
            recycleRange(runRange);
        }

        if(attributesChangingFormatting(attributes)) {
            paragraphsRange = paragraphsRangeForRange(range);
            for(i=paragraphsRange.index, c = paragraphsRange.index+paragraphsRange.length ;
                i < c ; i++ )
                toBeFormatted.addElementIfAbsent(_paragraphVector.elementAt(i));
            recycleRange( paragraphsRange );
        }

        for(i = 0 , c = toBeFormatted.count() ; i < c ; i++ ) {
            Range pRange;
            paragraph = (TextParagraph) toBeFormatted.elementAt(i);
            formatParagraph(paragraph);
            pRange = paragraph.range();
            dirtyRange.unionWith( pRange );
            recycleRange( pRange );
        }

        dirtyRange( dirtyRange );
        if( formattingEnabled() )
            _selection.setRange(selectedRange.index,selectedRange.index+selectedRange.length);
        recycleRange( dirtyRange );
    }

    void validateHTMLParsingRules() {
        if( _htmlParsingRules == null ) {
            int i,c;

            /* Warning: If you add some markers, make sure in finishDecoding
               that you add the marker class in the html rules if
               it does not exist.
               */
            String supportedContainers[] = {
                "BODY","H1","H2","H3","H4","H5","H6","B","STRONG",
                "CENTER","EM","I","PRE","A","OL","UL","LI",
                "ADDRESS","BLOCKQUOTE","DIR","MENU","TT","SAMP",
                "CODE","KBD","VAR","CITE","DL","DT","DD","TITLE","P"};

            String supportedMarkers[]    = { "BR" , "HR", "IMG" };

            _htmlParsingRules = new HTMLParsingRules();

            for(i=0,c=supportedContainers.length ; i < c ; i++ )
                _htmlParsingRules.setClassNameForMarker(
                "netscape.application.TextViewHTMLContainerImp", supportedContainers[i]);

            for(i=0,c=supportedMarkers.length ; i < c ; i++ )
                _htmlParsingRules.setClassNameForMarker(
                "netscape.application.TextViewHTMLMarkerImp", supportedMarkers[i]);

            _htmlParsingRules.setStringClassName("netscape.application.TextViewHTMLString");
        }
    }

    void disableAttachmentNotification() {
      notifyAttachmentDisabled++;
    }

    void enableAttachmentNotification() {
      notifyAttachmentDisabled--;
      if(notifyAttachmentDisabled<0)
        notifyAttachmentDisabled = 0;
      if(notifyAttachmentDisabled == 0 && invalidAttachmentRange!=null) {
        notifyAttachmentsForRange(invalidAttachmentRange,true);
        invalidAttachmentRange = null;
      }
    }

   void _notifyAttachmentsForRange(Range aRange,boolean added) {
        int paragraphIndex = _paragraphIndexForIndex(aRange.index);
        int runIndex;
        TextParagraph p;
        TextStyleRun run;
        int index = aRange.index;
        int lastIndex = aRange.index + aRange.length;
        Hashtable attributes;
        TextAttachment attachment;

        if(paragraphIndex == -1)
          return;
        p = (TextParagraph) _paragraphVector.elementAt(paragraphIndex);
        runIndex = p.runIndexForCharPosition(index);
        if(runIndex == -1)
          return;

        run = (TextStyleRun) p._runVector.elementAt(runIndex);
        index = run.rangeIndex();
        while(index < lastIndex) {
            attributes = run.attributes();
            if(attributes != null &&
               (attachment = (TextAttachment) attributes.get(TEXT_ATTACHMENT_KEY)) != null) {
              if(added) {
                TextPositionInfo info = run._paragraph._infoForPosition(run.rangeIndex());
                if( info != null ) {
                  Rect r;

                  info.representCharacterAfterEndOfLine();
                  r = run.textAttachmentBoundsForOrigin(info._x,info._y,
                                                        run._paragraph._baselines[info._lineNumber]);
                  attachment._willShowWithBounds(r);
                }
              } else
                  attachment._willHide();
            }

            index += run.charCount();
            runIndex++;
            if(runIndex < (p._runVector.count()))
              run = (TextStyleRun) p._runVector.elementAt(runIndex);
            else {
              paragraphIndex++;
              index++; /** Return char **/
              if(paragraphIndex < (_paragraphVector.count())) {
                p = (TextParagraph) _paragraphVector.elementAt(paragraphIndex);
                runIndex = 0;
                if(p._runVector.count() > 0)
                  run = (TextStyleRun) p._runVector.elementAt(runIndex);
                else
                  break;
              } else
                break;
            }
        }
    }

    void notifyAttachmentsForRange(Range aRange,boolean added) {
      if(added == false) {
        _notifyAttachmentsForRange(aRange,false);
      } else {
        if(notifyAttachmentDisabled > 0) {
          if(invalidAttachmentRange !=  null)
            invalidAttachmentRange.unionWith(aRange);
          else
            invalidAttachmentRange = new Range(aRange);
        } else {
          _notifyAttachmentsForRange(aRange,true);
        }
      }
    }

    boolean isLeftHalfOfCharacter(int x,int y) {
        TextPositionInfo info,absoluteInfo;

        info = positionForPoint(x,y,false);
        absoluteInfo = positionForPoint(x,y,true);
        if(info == null || absoluteInfo == null)
            return true;

        if(info._absPosition == absoluteInfo._absPosition)
            return true;
        else
            return false;
    }




    static Range allocateRange() {
        return allocateRange(Range.nullRange().index,Range.nullRange().length);
    }

    static Range allocateRange(Range template) {
        return allocateRange(template.index,template.length);
    }

    static Range allocateRange(int index,int length) {
        Range result = (Range) rangePool.allocateObject();
        result.index = index;
        result.length = length;
        return result;
    }

    static void recycleRange(Range aRange) {
        rangePool.recycleObject(aRange);
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

            _selection.disableInsertionPoint();
            replaceRangeWithString(range, text);

            range = new Range(range.index() + text.length(), 0);
            selectRange(range);
            scrollRangeToVisible(range);
            _selection.enableInsertionPoint();
        }
    }

    /** Implementation of the FormElement interface
      *
      */
    public String formElementText() {
        return string();
    }

}

// TextPositionInfo.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that contains information about a single character position
  * within the TextView (position, line number, height of the line,
  * etc.).<br><br>
  * @note 1.0 changes
  */
class TextPositionInfo {
    public TextStyleRun _textRun;
    public int          _x, _y, _lineNumber, _lineHeight, _absPosition,
                        _positionInRun, _updateLine;
    public boolean      _redrawCurrentLineOnly, _redrawCurrentParagraphOnly,
                        _nextLine, _endOfLine, _endOfParagraph;



/* constructors */

     TextPositionInfo() {
        super();
    }

     TextPositionInfo(TextStyleRun theRun, int x, int y, int lineNumber,
                            int lineHeight, int runPosition, int absPosition) {
        this();

        init(theRun, x, y, lineNumber, lineHeight, runPosition, absPosition);
    }

     TextPositionInfo(TextPositionInfo aPosition) {
        this();

        init(aPosition._textRun, aPosition._x, aPosition._y,
             aPosition._lineNumber, aPosition._lineHeight,
             aPosition._positionInRun, aPosition._absPosition);
    }


    public String toString() {
        return "run is " + _textRun + " x is " + _x + " y is " + _y + " lineNumber is " + _lineNumber +
            "line height is: " + _lineHeight + "positionInRun is " + _positionInRun + "position is:" +
             _absPosition + "endOfLine is " + _endOfLine + "_endOfParagraph is " + _endOfParagraph;
    }

/* initializers */


     void init(TextStyleRun theRun, int x, int y, int lineNumber,
                     int lineHeight, int runPosition, int absPosition) {

        _textRun = theRun;
        _x = x;
        _y = y;
        _lineNumber = lineNumber;
        _lineHeight = lineHeight;
        _positionInRun = runPosition;
        _absPosition = absPosition;

        _updateLine = _lineNumber;
    }

     void init(TextPositionInfo aPosition) {
        init(aPosition._textRun, aPosition._x, aPosition._y,
             aPosition._lineNumber, aPosition._lineHeight,
             aPosition._positionInRun, aPosition._absPosition);
    }

    /** ALERT!, it would be better to make sure that the rest of the code is never accessing _x,_y and
     * line number directly so we can perform the following conversion on the fly
      */
     void representCharacterAfterEndOfLine() {
        if( _endOfLine ) {
            TextParagraphFormat f = _textRun.paragraph().currentParagraphFormat();
            _x = f._leftMargin + f._leftIndent;
            if( f.wrapsUnderFirstCharacter() ) {
                _x = f._leftMargin +
                    (_textRun.paragraph().addWidthOfInitialTabs(
                                                            f._leftMargin+f._leftIndent)
                     - f._leftIndent);
            }
            _y += _lineHeight;
            _lineNumber++;
            _lineHeight = _textRun.paragraph()._lineHeights[_lineNumber];
            _endOfLine = false;

        }
    }

     void representCharacterBeforeEndOfLine() {
         TextPositionInfo info = _textRun._paragraph._owner.positionInfoForIndex(_absPosition);
         if( info._endOfLine ) {
             _textRun = info._textRun;
             _x = info._x;
             _y = info._y;
             _absPosition = info._absPosition;
             _lineNumber = info._lineNumber;
             _lineHeight = info._lineHeight;
             _positionInRun = info._positionInRun;
             _updateLine = info._updateLine;
             _redrawCurrentLineOnly = info._redrawCurrentLineOnly;
             _redrawCurrentParagraphOnly = info._redrawCurrentParagraphOnly;
             _nextLine = info._nextLine;
             _endOfLine = info._endOfLine;
             _endOfParagraph = info._endOfParagraph;
         }
     }

     void setUpdateLine(int lineNumber) {
        _updateLine = lineNumber;
    }

     void setRedrawCurrentLineOnly(boolean flag) {
        _redrawCurrentLineOnly = flag;
    }

     void setRedrawCurrentParagraphOnly(boolean flag) {
        _redrawCurrentParagraphOnly = flag;
    }

     void setX(int anInt) {
        _x = anInt;
    }

     void setAbsPosition(int absPosition) {
        _absPosition = absPosition;
    }

     void setPositionInRun(int anInt) {
        _positionInRun = anInt;
    }

     void moveBy(int deltaX, int deltaY) {
        _x += deltaX;
        _y += deltaY;
    }

     int maxY() {
        return _y + _lineHeight;
    }

     Rect lineBounds() {
        return _textRun._paragraph.rectForLine(_lineNumber);
    }

     Range lineRange() {
        return _textRun._paragraph.rangeForLine(_lineNumber);
    }
     void setNextLine(boolean flag) {
        _nextLine = flag;
    }

     void setAtEndOfLine(boolean flag) {
        _endOfLine = flag;
    }

     void setAtEndOfParagraph(boolean flag) {
        _endOfParagraph = flag;
    }
 }


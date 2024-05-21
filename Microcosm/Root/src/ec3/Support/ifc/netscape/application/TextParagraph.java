// TextParagraph.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Object representing a paragraph (a sequence of TextStyleRuns) formatted
  * according to a TextParagraphFormat and ending with a return
  * character.<br><br>
  * <i><b>Note:</b> This class mostly works, but its API has not been refined
  * or reviewed, and remains mostly undocumented for now.</i>
  * @private
  * @note 1.0 changes
  */
public class TextParagraph implements Cloneable, Codable {
    TextView            _owner;
    TextParagraphFormat _format;
    Vector              _runVector;
    int                 _y, _height, _lineBreaks[], _breakCount,
                        _lineHeights[], _heightCount, _baselines[],
                        _baselineCount, _lineRemainders[], _remainderCount,
                        _charCount, _startChar;

    static final String         FORMAT_KEY = "format";
    static final String         RUNVECTOR_KEY = "runVector";



/* constructors */

     public TextParagraph() {
        super();
    }

     TextParagraph(TextView owner) {
        this();
        init(owner);
    }

     TextParagraph(TextView owner, TextParagraphFormat format) {
        this();

        init(owner, format);
    }



/* initializers */


     void init(TextView owner, TextParagraphFormat format) {
        _owner = owner;

        _runVector = new Vector();

        setFormat(format);
    }

     void init(TextView owner) {
        init(owner, null);
    }

    Object objectAt(Vector vector, int index) {
        return (index < 0 || index >= vector.count()) ? null :
                                                       vector.elementAt(index);
    }

    public Object clone() {
        TextParagraph   cloneParagraph;
        TextStyleRun    nextRun;
        Object          clone = null;
        int             i, count;

        collectEmptyRuns();

        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InconsistencyException(this + ": clone() not supported :" + e);
        }

        if (clone != null) {
            cloneParagraph = (TextParagraph)clone;
            cloneParagraph._owner = null;
            if( _format != null )
                cloneParagraph._format = (TextParagraphFormat)_format.clone();
            else
                cloneParagraph._format = null;
            cloneParagraph._runVector = new Vector();
            count = _runVector.count();
            for (i = 0; i < count; i++) {
                nextRun = (TextStyleRun)_runVector.elementAt(i);
                cloneParagraph.addRun(nextRun.createEmptyRun());
            }
            cloneParagraph._lineBreaks = null;
            cloneParagraph._lineHeights = null;
            cloneParagraph._baselines = null;
            cloneParagraph._lineRemainders = null;
        }

        return clone;
    }

     void setOwner(TextView owner) {
        _owner = owner;
    }

     TextView owner() {
        return _owner;
    }

    void setY(int y) {
        _y = y;
    }

    void setStartChar(int absPosition) {
        _startChar = absPosition;
    }

    void setFormat(TextParagraphFormat format) {
        if( format == null && _format == null )
            return;
        if (format != null)
            _format = (TextParagraphFormat)format.clone();
        else
            _format = null;
        if( _charCount > 0 )
            computeLineBreaksAndHeights(_owner.bounds.width);
    }

   TextParagraphFormat format() {
     return _format;
   }

   TextParagraphFormat currentParagraphFormat() {
       if( _format != null )
           return _format;
       else {
           TextParagraphFormat f = null;
           if(_owner != null)
               f = (TextParagraphFormat)_owner.defaultAttributes().get(
                                                                                TextView.PARAGRAPH_FORMAT_KEY);
           if( f != null )
               return f;
           else {
               return new TextParagraphFormat();
           }
       }
   }

  /**
   * Returns the TextParagraph's Vector of TextStyleRuns.
   */
     Vector runVector() {
        return _runVector;
    }

     TextStyleRun firstRun() {
         return (TextStyleRun) _runVector.firstElement();
     }

     TextStyleRun lastRun() {
         return (TextStyleRun) _runVector.lastElement();
     }

  /**
   * Makes the TextStyleRun <b>aRun</b> as the last TextStyleRun in the
   * TextParagraph.
   */
     void addRun(TextStyleRun aRun) {
        if (aRun != null) {
            aRun.setParagraph(this);
            _runVector.addElement(aRun);
        }
    }

    /* Remove all empty runs but the first one */
    void collectEmptyRuns() {
        int i,c;
        TextStyleRun run;
        Hashtable attr;

        for(i=1,c=_runVector.count() ; i < c ; i++ ) {
            run = (TextStyleRun) _runVector.elementAt(i);
            if( run.charCount() == 0) {
                if(!( run._attributes != null &&
                      run._attributes.get(TextView.LINK_DESTINATION_KEY) != null)) {
                    _runVector.removeElementAt(i);
                    i--;
                    c--;
                }
            }
        }
    }

  /**
   * Adds the TextStyleRuns contained in <b>runVector</b> to the TextParagraph
   * by
   * calling <b>addRun()</b> for each.
   */
     void addRuns(Vector runVector) {
        int     count, i;

        if (runVector != null) {
            count = runVector.count();
            for (i = 0; i < count; i++) {
                addRun((TextStyleRun)runVector.elementAt(i));
            }
        }
    }

  /**
   * Adds the TextStyleRun <b>aRun</b> to the TextParagraph, inserting it at
   * <b>index</b> within the TextParagraph's runVector.
   */
     void insertRunAt(TextStyleRun aRun, int index) {
        TextStyleRun    fontRun;

        if (aRun != null && index >= 0) {
            aRun.setParagraph(this);
            _runVector.insertElementAt(aRun, index);
        } else {
//            System.out.println("Cannot insert run " + aRun.toString() + " at index " + index);
        }
    }

  /**
   * Returns the TextStyleRun preceeding <b>aRun</b> in the TextParagraph's
   * runVector.
   */
     TextStyleRun runBefore(TextStyleRun aRun) {
        int     index;

        if (aRun == null) {
            return null;
        }

        index = _runVector.indexOfIdentical(aRun);
        if (index < 1) {
            return null;
        }

        return (TextStyleRun)_runVector.elementAt(index - 1);
    }

  /**
   * Returns the TextStyleRun after <b>aRun</b> in the TextParagraph's
   * runVector.
   */
     TextStyleRun runAfter(TextStyleRun aRun) {
        int     index;

        if (aRun == null) {
            return null;
        }

        index = _runVector.indexOfIdentical(aRun);
        if (index == (_runVector.count() - 1)) {
            return null;
        }

        return (TextStyleRun)_runVector.elementAt(index + 1);
    }


  /**
   * Returns a Vector containing all TextStyleRuns preceeding <b>aRun</b>
   * in the TextParagraph's runVector.
   */
     Vector runsBefore(TextStyleRun aRun) {
        Vector       runVector;
        int             i, index;

        runVector = TextView.newVector();

        if (aRun == null) {
            return runVector;
        }

        index = _runVector.indexOfIdentical(aRun);
        if (index == -1) {
            return runVector;
        }

        for (i = 0; i < index; i++) {
            runVector.addElement(_runVector.elementAt(i));
        }

        return runVector;
    }

  /**
   * Returns a Vector containing all TextStyleRuns following <b>aRun</b> in
   * the
   * TextParagraph's runVector.
   */
     Vector runsAfter(TextStyleRun aRun) {
        Vector       runVector;
        int             i, count;

        runVector = TextView.newVector();

        if (aRun == null) {
            return runVector;
        }

        i = _runVector.indexOfIdentical(aRun);
        if (i == -1) {
            return runVector;
        }

        count = _runVector.count();
        for (; i < count; i++) {
            runVector.addElement(_runVector.elementAt(i));
        }

        return runVector;
    }

  /**
   * Returns a Vector containing all TextStyleRuns between (and including)
   * <b>startRun</b> and <b>endRun</b> in the TextParagraph's runVector.
   * If <b>startRun</b> is <b>null</b>, this method starts at the first
   * run in the runVector.  If <b>endRun</b> is <b>null</b>, this method
   * ends with the last run in the runVector.
   */
     Vector runsFromTo(TextStyleRun startRun, TextStyleRun endRun) {
        Vector       runVector;
        int             i, end;

        runVector = TextView.newVector();

        if (startRun == endRun && startRun != null) {
            runVector.addElement(startRun);

            return runVector;
        }

        if (startRun == null) {
            i = 0;
        } else {
            i = _runVector.indexOfIdentical(startRun);
        }

        if (endRun == null) {
            end = _runVector.count() - 1;
        } else {
            end = _runVector.indexOfIdentical(endRun);
        }

        if (i < 0 || end < 0) {
            return runVector;
        }

        for (; i <= end; i++) {
            runVector.addElement(_runVector.elementAt(i));
        }

        return runVector;
    }

  /**
   * Removes the TextStyleRun <b>aRun</b> from the TextParagraph.
   */
     void removeRun(TextStyleRun aRun) {
        if (aRun != null) {
            _runVector.removeElement(aRun);
        }
    }

  /**
   * Removes the TextStyleRuns contained in <b>runVector</b> from the
   * TextParagraph.
   */
     void removeRuns(Vector runVector) {
        int     i;

        if (runVector == null) {
            return;
        }

        i = runVector.count();
        while (i-- > 0) {
            _runVector.removeElement(runVector.elementAt(i));
        }
    }

  /**
   * Removes the TextStyleRun located at <b>index</b> within the TextParagraph's
   * runVector from the TextParagraph.
   */
     void removeRunAt(int index) {
        _runVector.removeElementAt(index);
    }

  /**
   * Returns <b>true</b> if the TextParagraph contains no TextStyleRuns.
   */
     boolean isEmpty() {
        TextStyleRun    nextRun;
        int             i, count = 0;

        i = _runVector.count();
        while (i-- > 0 && count == 0) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);
            count += nextRun.charCount();
        }

        return (count == 0);
    }


    int[] _growArrayTo(int anArray[], int newSize) {
        int     i, oldArray[];

        if (newSize < 1) {
            return anArray;
        }

        if (anArray != null && anArray.length >= newSize) {
            return anArray;
        }

        oldArray = anArray;
        if (anArray != null) {
            i = anArray.length;
            while (i < newSize) {
                i *= 2;
            }
        } else {
            i = 20;
        }

        anArray = new int[i];

        if (oldArray == null) {
            return anArray;
        }

        System.arraycopy(oldArray, 0, anArray, 0, oldArray.length);

        return anArray;
    }

    void _addLineBreak(int position) {
        int     i, count;

        if (position < 0) {
            return;
        }

        _lineBreaks = _growArrayTo(_lineBreaks, _breakCount + 1);
        _lineBreaks[_breakCount] = position;
        _breakCount++;
    }

    void _addLineHeightAndBaseline(int height, int baseline) {
        int     i, count;

        if (height < 0 || baseline < 0) {
            return;
        }

        _lineHeights = _growArrayTo(_lineHeights, _heightCount + 1);
        _lineHeights[_heightCount] = height;
        _heightCount++;

        _baselines = _growArrayTo(_baselines, _baselineCount + 1);
        _baselines[_baselineCount] = baseline;
        _baselineCount++;
    }

    void _addLineRemainder(int width) {
        int     i, count;

        if (width < 0) {
            width = 0;
        }

        _lineRemainders = _growArrayTo(_lineRemainders, _remainderCount + 1);
        _lineRemainders[_remainderCount] = width;
        _remainderCount++;
    }

    int addWidthOfInitialTabs(int offsetX) {
        TextStyleRun run;
        int i,c,j,d;
        FastStringBuffer rString;
        int tabCount = 0;
        int tabPositions[] = currentParagraphFormat().tabPositions();

        for(i=0,c=_runVector.count() ; i < c ; i++ ) {
            run = (TextStyleRun) _runVector.elementAt(i);
            rString = run._contents;
            if( rString == null || rString.length() == 0 )
                break;
            j=0;
            d=rString.length();
            while(j < d && rString.charAt(j) == '\t') {
                tabCount++;
                j++;
            }
            if( j < d )
                break;
        }

        if( tabCount == 0 )
            return offsetX;
        else {
            for(i=0,c=tabPositions.length ; i < c ; i++ )
                if( tabPositions[i] >= offsetX )
                    break;
            if( i == c )
                return offsetX;
            else {
                i += tabCount;
                if( i >= tabPositions.length )
                    return tabPositions[tabPositions.length-1];
                else
                    return tabPositions[i-1];
            }
        }
    }

    void computeLineBreaksAndHeights(int maxWidth) {
      computeLineBreaksAndHeights(maxWidth,0);
    }

    void computeLineBreaksAndHeights(int maxWidth,int fromLine) {
        int debugOrigMaxWidth = maxWidth;
        TextStyleRun    nextRun;
        int             count, i, runCharsUsed, totalCharsInRun,
                        charactersAddedToLine,
                        remainingWidth, currentCharNumber, currentLineHeight,
                        currentLineBaseline, startingWidth, ascenderHeight,
                        descenderHeight, newAscenderHeight, newDescenderHeight,
                        currentX,initialCurrentX;
        TextParagraphFormat format = currentParagraphFormat();

        if(fromLine > 0)
          fromLine--;

      /* clear line breaks */
        _breakCount = fromLine;

      /* clear line heights and baselines */
        _heightCount = fromLine;
        _baselineCount = fromLine;

      /* clear line remainders */
        _remainderCount = fromLine;
        if (format._justification == Graphics.LEFT_JUSTIFIED && fromLine == 0) {
            _lineRemainders = null;
        }

        maxWidth -= format._leftMargin + format._rightMargin;
        if (maxWidth < 1) {
            maxWidth = 1;
        }

      /*
       * remainingWidth is the remaining space on the current line;
       * at the start, it's just the total available space; normally, it's
       * just maxWidth, but the leftIndent changes it for the first line
       */
        if(fromLine == 0) {
          remainingWidth = maxWidth - format._leftIndent;
          if (remainingWidth < 1) {
            remainingWidth = 1;
          }
          startingWidth = remainingWidth;
          _height = 0;
          _charCount = 0;
        } else {
          remainingWidth = maxWidth;
          startingWidth = maxWidth;
          _height = 0;
          for(i=0; i < fromLine ; i++)
            _height += _lineHeights[i];
          _charCount = _lineBreaks[fromLine-1];
        }


      /* start laying out each run */
        count = _runVector.count();
        currentCharNumber = _charCount;
        currentLineHeight = 0;
        currentLineBaseline = 0;
        ascenderHeight = descenderHeight = 0;
        currentX = format._leftMargin;
        initialCurrentX = currentX;
        if(fromLine == 0)
          currentX += format._leftIndent;
        if(format.wrapsUnderFirstCharacter() ) {
            initialCurrentX = addWidthOfInitialTabs(format._leftMargin + format._leftIndent);
            maxWidth -= (initialCurrentX - currentX);
            remainingWidth = maxWidth;
        }
        if(fromLine == 0) {
          i = 0;
          nextRun = null;
          totalCharsInRun = 0;
          runCharsUsed = 0;
        } else {
          nextRun = runForCharPosition(_startChar + _charCount);
          i = _runVector.indexOfIdentical(nextRun) + 1;
          totalCharsInRun = nextRun.charCount();
          runCharsUsed = _startChar + _charCount - nextRun.rangeIndex();
          _charCount += (totalCharsInRun - _charCount);
        }
        while(true)  {
            if(nextRun == null || runCharsUsed >= totalCharsInRun) {
            if(i == _runVector.count())
                break;
              nextRun = (TextStyleRun)_runVector.elementAt(i++);
              if( nextRun.charCount() == 0 )
                continue;

              runCharsUsed = 0;
              totalCharsInRun = nextRun.charCount();
              _charCount += totalCharsInRun;
            }

          /* lay out characters until we run out */
            while (runCharsUsed < totalCharsInRun) {
              /* how many chars can fit in the available space? */
                charactersAddedToLine = nextRun.charsForWidth(runCharsUsed,
                                                              currentX,
                                                              remainingWidth,
                                                              startingWidth,
                                                            format._tabStops);

              /* if characters added, adjust markers */
                if (charactersAddedToLine > 0) {
                    runCharsUsed += charactersAddedToLine;
                    currentCharNumber += charactersAddedToLine;

                    newAscenderHeight = nextRun.baseline();
                    newDescenderHeight = nextRun.height() - nextRun.baseline();

                    if (ascenderHeight < newAscenderHeight) {
                        ascenderHeight = newAscenderHeight;
                    }
                    if (descenderHeight < newDescenderHeight) {
                        descenderHeight = newDescenderHeight;
                    }

                    if (ascenderHeight + descenderHeight > currentLineHeight) {
                        currentLineHeight = ascenderHeight + descenderHeight;
                    }
                    if (ascenderHeight > currentLineBaseline) {
                        currentLineBaseline = ascenderHeight;
                    }

                    currentX += remainingWidth - nextRun._remainder;
                    remainingWidth = nextRun._remainder;
                }

              /* wrap remaining characters to next line */
                if (runCharsUsed < totalCharsInRun) {
                  /* place them on the next line */
                    _addLineBreak(currentCharNumber);
                    _addLineHeightAndBaseline(
                                currentLineHeight + format._lineSpacing,
                                currentLineBaseline);
                    _height += currentLineHeight + format._lineSpacing;
                    _addLineRemainder(remainingWidth);

                    remainingWidth = startingWidth = maxWidth;
                    currentLineHeight = currentLineBaseline = 0;
                    ascenderHeight = descenderHeight = 0;
                    currentX = initialCurrentX;
                    continue;
                }
            }
        }

      /* break at the end of the paragraph */
        _addLineBreak(currentCharNumber);
        if (currentLineHeight == 0) {
            nextRun = (TextStyleRun)_runVector.firstElement();
            currentLineHeight = nextRun.height() + format._lineSpacing;
            currentLineBaseline = nextRun.baseline();
        } else {
            currentLineHeight += format._lineSpacing;
        }
        _addLineHeightAndBaseline(currentLineHeight, currentLineBaseline);
        _height += currentLineHeight;
        _addLineRemainder(remainingWidth);

        /* return char */
        _charCount++;

        /*      if(fromLine>0) {
             dumpMe();
             computeLineBreaksAndHeights(debugOrigMaxWidth,0);
             dumpMe();
        } */
    }

/***
  void dumpMe() {
        int i;
        System.out.println("RunVector is " + _runVector +
                             " _y=" + _y +
                             " _height=" + _height +
                             " _breakCount=" + _breakCount +
                             " _charCount=" + _charCount);

        System.out.println("line breaks:");
        for(i=0;i<_breakCount;i++)
            System.out.print("" + _lineBreaks[i] + " ");
        System.out.println("");

        System.out.println("line heights:");
        for(i=0;i<_breakCount;i++)
            System.out.print("" + _lineHeights[i] + " ");
        System.out.println("");
     }
*****/

     int characterStartingLine(int lineNumber) {
        if( lineNumber == 0 )
            return _startChar;
        else if( lineNumber < _breakCount )
            return _startChar + _lineBreaks[lineNumber-1];
        else
            return -1;
    }

     Rect rectForLine(int lineNumber) {
        int     currentX, currentY, i;
        TextParagraphFormat f = currentParagraphFormat();

        if (lineNumber >= _breakCount) {
            return null;
        }

        currentY = _y;
        for (i = 0; i < lineNumber; i++) {
            currentY += _lineHeights[i];
        }

        return TextView.newRect(f._leftMargin, currentY,
                                _owner.bounds.width - f._rightMargin, _lineHeights[i]);
    }

    Range rangeForLine(int lineNumber) {
        int start,end;
        if( lineNumber >= _breakCount )
            return new Range(_startChar + _charCount , 0);

        if( lineNumber == 0 )
            return new Range(_startChar,_lineBreaks[lineNumber]/*(+_startChar - _startChar)*/);
        else
            return new Range(_startChar + _lineBreaks[lineNumber-1],
                             _lineBreaks[lineNumber]-_lineBreaks[lineNumber-1]);
    }

  int runIndexForCharPosition(int absPosition) {
    TextStyleRun    nextRun;
    int             position, count, i;

    position = absPosition - _startChar;
    count = _runVector.count();

    for (i = 0; i < count; i++) {
      nextRun = (TextStyleRun)_runVector.elementAt(i);

      if (nextRun.charCount() <= position) {
        position -= nextRun.charCount();
        continue;
      }

      return i;
    }

    return _runVector.count() - 1;
  }

  TextStyleRun runForCharPosition(int absPosition) {
    int index = runIndexForCharPosition(absPosition);
    if(index >= 0)
      return (TextStyleRun) _runVector.elementAt(index);
    else
      return null;
  }

     char characterAt(int absPosition) {
        TextStyleRun    nextRun;
        char            theChar;
        int             position, count, i;

        if (_charCount < 2) {
            return '\n';
        }

        position = absPosition - _startChar;

        count = _runVector.count();
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);

            if (nextRun.charCount() <= position) {
                position -= nextRun.charCount();
                continue;
            }

            theChar = nextRun.charAt(position);

            return theChar;
        }

      /* this is the return character */
        if (position < 2) {
            return '\n';
        }

        return '\0';
    }

     int lineForPosition(int absPosition) {
        int     position, i;

        position = absPosition - _startChar;

      /* if return character, return the last line */
        if (_breakCount > 0 && position == _lineBreaks[_breakCount - 1]) {
            return _breakCount - 1;
        }

        for (i = 0; i < _breakCount && position >= _lineBreaks[i]; i++) {
        }

        if (i >= _breakCount) {
            return -1;
        }

        return i;
    }

    /* If absolute is false, this method will return the next character
     * if the hit point is on the right of the glyph center. Otherwise
     * it will return the character for the glyph.
     * Example:  Netscape
     * flag is false, (x,y) is on the 't' a little after the center.
     *     the returned position will be the position of s.
     * flag is true , (x,y) is on the 't' a little after the center.
     *     the returned position will be the position of t
     */
     TextPositionInfo positionForPoint(int x, int y,boolean absolute) {
        TextStyleRun            nextRun = null;
        TextPositionInfo        currentInfo, previousInfo = null,
                                endPosition;
        int                     i, currentY, firstChar, lastChar, charCount,
                                currentPosition, previousPosition, remainder,
                                currentX, availableWidth, lineNumber,
                                runIndex, deltaX, runStartChar = 0, count;
        TextParagraphFormat format = currentParagraphFormat();

      /* which line is it on? */


        currentY = _y;
        for (i = 0; i < _breakCount; i++) {
            if (y >= currentY && y <= currentY + _lineHeights[i]) {
                break;
            }
            currentY += _lineHeights[i];
        }

        lineNumber = i;
        if (lineNumber == 0) {
            firstChar = _startChar;
        } else {
            firstChar = _startChar + _lineBreaks[lineNumber - 1];
        }
        lastChar = _startChar + _lineBreaks[lineNumber];

      /* find the run that begins the line */
        count = _runVector.count();
        charCount = _startChar;
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);
            if (charCount + nextRun.charCount() > firstChar) {
                runStartChar = firstChar - charCount;
                break;
            }
            charCount += nextRun.charCount();
        }
        if (nextRun == null) {
            return null;
        }
        runIndex = i;

        if (format._justification == Graphics.LEFT_JUSTIFIED) {
            currentX = format._leftMargin;
        } else if (format._justification ==
                                    Graphics.RIGHT_JUSTIFIED) {
            currentX = format._leftMargin +
                       _lineRemainders[lineNumber];
        } else {
            currentX = format._leftMargin +
                       _lineRemainders[lineNumber] / 2;
        }
        availableWidth = _owner.bounds.width - format._leftMargin -
                         format._rightMargin;
        if (lineNumber == 0) {
            currentX += format._leftIndent;
            availableWidth -= format._leftIndent;
        } else if( format.wrapsUnderFirstCharacter()) {
            int oldCurrentX = currentX;
            currentX = addWidthOfInitialTabs( currentX + format._leftIndent);
            availableWidth -= (currentX - oldCurrentX);
        }
        if (availableWidth < 1) {
            availableWidth = 1;
        }

      /* beyond ends of line? */
        if (x > (currentX + availableWidth - _lineRemainders[lineNumber])) {
            endPosition = infoForPosition(lastChar, -1);
            endPosition.setAtEndOfLine(true);
            return endPosition;
        } else if (x <= currentX) {
            return infoForPosition(firstChar, y);
        }

        runIndex++;
        while(nextRun != null && nextRun.charCount() == 0 ) {
            nextRun = runAfter(nextRun);
            runIndex++;
        }
        if( nextRun == null ) {
            return infoForPosition(firstChar + charCount,y);
        }
        while (firstChar <= lastChar) {
            deltaX = nextRun.widthOfContents(runStartChar, 1, currentX,
                                             format._tabStops);
            if (x >= currentX && x <= (currentX + deltaX)) {
                if( absolute )
                    return infoForPosition(firstChar,y);
                else {
                    deltaX /= 2;
                    if (x >= currentX + deltaX) {
                        return infoForPosition(firstChar + 1, y);
                    }
                    return infoForPosition(firstChar, y);
                }
            }

            runStartChar++;
            if (runStartChar >= nextRun.charCount()) {
                nextRun = (TextStyleRun)objectAt(_runVector, runIndex++);
                while(nextRun != null && nextRun.charCount() == 0 ) {
                    nextRun = runAfter(nextRun);
                    runIndex++;
                }
                if (nextRun == null) {
                    return infoForPosition(firstChar + charCount, y);
                }
                runStartChar = 0;
            }

            firstChar++;
            currentX += deltaX;
        }

        return null;
    }

    TextPositionInfo _infoForPosition(int absPosition) {
        TextStyleRun    nextRun;
        int             position, i=0, charCount = 0, runNumber = 1, currentX=0,
                        currentY, runChars, deltaX = 0, runStartChar,initialPosition,
                        maxWidth, availableWidth;
        TextParagraphFormat format = currentParagraphFormat();
        position = absPosition - _startChar;
        initialPosition = position;

        maxWidth = _owner.bounds.width - format._leftMargin -
                   format._rightMargin;
        currentY = _y;

        for(i=0;i<(_breakCount-1);i++) {
            if(_lineBreaks[i] < position) {
              currentY += _lineHeights[i];
            } else
              break;
        }

        if(i > 0) {
          nextRun = (TextStyleRun)runForCharPosition(_startChar + _lineBreaks[i-1]);
          runStartChar = _lineBreaks[i-1] + _startChar - nextRun.rangeIndex();
          position -= _lineBreaks[i-1];
        } else {
          nextRun = (TextStyleRun)_runVector.firstElement();
          runStartChar = 0;
        }

        for (; i < _breakCount; i++) {
            if (i == 0) {
                charCount = _lineBreaks[i];
            } else {
                charCount = _lineBreaks[i] - _lineBreaks[i - 1];
            }

            if (charCount > position) {
                charCount = position;
            }

            if (format._justification == Graphics.LEFT_JUSTIFIED) {
                currentX = format._leftMargin;
            } else if (format._justification ==
                                        Graphics.RIGHT_JUSTIFIED) {
                currentX = format._leftMargin +
                           _lineRemainders[i];
            } else {
                currentX = format._leftMargin +
                           _lineRemainders[i] / 2;
            }

            availableWidth = maxWidth;
            if (i == 0) {
                currentX += format._leftIndent;
                availableWidth -= format._leftIndent;
            }  else if( format.wrapsUnderFirstCharacter()) {
                int newCurrentX = addWidthOfInitialTabs( currentX + format._leftIndent);
                availableWidth -= (newCurrentX - currentX);
                currentX = newCurrentX;
            }
            if (availableWidth < 1) {
                availableWidth = 1;
            }

            if (charCount == 0) {
                return new TextPositionInfo(nextRun, currentX, currentY, i,
                                            _lineHeights[i], runStartChar,
                                            absPosition);
            }

            while (charCount > 0) {
                runChars = nextRun.charCount() - runStartChar;

                if (charCount >= runChars) {
                    if (charCount <= position) {
                        deltaX = nextRun.widthOfContents(runStartChar,
                                                         charCount, currentX,
                                                         format._tabStops);
                    }
                    charCount -= runChars;
                    position -= runChars;

                    nextRun = (TextStyleRun)objectAt(_runVector, runNumber++);
                    while (nextRun != null && nextRun.charCount() == 0) {
                        nextRun = (TextStyleRun)objectAt(_runVector,
                                                                runNumber++);
                    }
                    runStartChar = 0;
                } else {
                    if (charCount <= position) {
                        deltaX = nextRun.widthOfContents(runStartChar,
                                                         charCount, currentX,
                                                         format._tabStops);
                    }

                    runStartChar += charCount;
                    position -= charCount;
                    charCount = 0;
                }
                currentX += deltaX;
                availableWidth -= deltaX;

                if (position == 0 ||
                    (nextRun == null && position == 1)) {
                    TextPositionInfo result;
                    if (nextRun == null) {
                        nextRun = (TextStyleRun)_runVector.lastElement();
                        runStartChar = nextRun.charCount();
                    }
                    result = new TextPositionInfo(nextRun, currentX, currentY, i,
                                                _lineHeights[i], runStartChar,
                                                absPosition);
                    if( initialPosition == _lineBreaks[i] ) {
                        result.setAtEndOfLine(true);
                        if( i == (_breakCount-1))
                          result.setAtEndOfParagraph(true);
                    }
                    return result;

                }
            }

            currentY += _lineHeights[i];
        }
        return null;
    }

  /* yCoord == -1 ignores special fudging */
    TextPositionInfo infoForPosition(int absPosition, int yCoord) {
        TextPositionInfo        positionInfo, nextInfo;
        int                     lineNumber, currentX;
        TextParagraphFormat format = currentParagraphFormat();

        positionInfo = _infoForPosition(absPosition);

        /* If positionInfo out of bounds, return the last position */
        if( positionInfo == null ) {
            positionInfo = _infoForPosition(_startChar + _charCount);
            return positionInfo;
        }

        if (yCoord < positionInfo.maxY()) {
          return positionInfo;
        }

      /* if not at end of line, we're done */
        nextInfo = _infoForPosition(absPosition + 1);
        if (nextInfo == null ||
            nextInfo._lineNumber == positionInfo._lineNumber) {
            return positionInfo;
        }

        lineNumber = nextInfo._lineNumber;
        if (format._justification == Graphics.LEFT_JUSTIFIED) {
            currentX = format._leftMargin;
        } else if (format._justification ==
                                    Graphics.RIGHT_JUSTIFIED) {
            currentX = format._leftMargin +
                       _lineRemainders[lineNumber];
        } else {
            currentX = format._leftMargin +
                       _lineRemainders[lineNumber] / 2;
        }

      /*
       * create a TextInfo that represents the absPosition at the start of the
       * next line, rather than the end of the one previous
       */
        positionInfo = new TextPositionInfo(positionInfo._textRun, currentX,
                                            nextInfo._y, nextInfo._lineNumber,
                                            nextInfo._lineHeight,
                                            positionInfo._positionInRun,
                                            positionInfo._absPosition);
        positionInfo.setNextLine(true);

        return positionInfo;
    }

     TextPositionInfo insertCharOrStringAt(char aChar, String aString,
                                                 int absPosition) {
        TextPositionInfo        lastCharPosition, newPosition,
                                newLastCharPosition;
        TextStyleRun            nextRun = null, newRun;
        int                     i, count, position, currentLine, charCount,
                                lastCharNumber, oldHeight;

        if (aString == null && aChar == '\0') {
            return null;
        }

        /* find the run that has this character */
        position = absPosition - _startChar;
        count = _runVector.count();
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)objectAt(_runVector, i);

            if (nextRun == null) {
                break;
            }

            if (nextRun.charCount() < position) {
                position -= nextRun.charCount();
                continue;
            }

            if (nextRun.containsATextAttachment()) {
                if (position == 0) {
                    /* ALERT! Should create a run with the attributes of the previous run */
                    newRun = nextRun.createEmptyRun();
                    insertRunAt(newRun, _runVector.indexOfIdentical(nextRun));
                    nextRun = newRun;
                } else {
                    TextStyleRun previousRun = null;
                    if( i > 0 ) {
                        previousRun = (TextStyleRun) objectAt(_runVector,i-1);
                        newRun = previousRun.createEmptyRun(
                              TextView.attributesByRemovingStaticAttributes(previousRun.attributes()));
                        insertRunAt(newRun,_runVector.indexOfIdentical(nextRun) + 1);
                        nextRun = newRun;
                    } else {
                        /** We should filter static attributes here too */
                        nextRun = (TextStyleRun)objectAt(_runVector, 0);
                        newRun = nextRun.createEmptyRun(
                              TextView.attributesByRemovingStaticAttributes(nextRun.attributes()));
                        insertRunAt(newRun,1);
                        nextRun = newRun;
                    }
                    if (nextRun == null) {
                        /* ALERT! Should create a run with the attributes of the previous run */
                        nextRun = new TextStyleRun(this, "",null);
                        addRun(nextRun);
                    }
                }
                position = 0;
            }

            break;
        }

        if ((i >= count || nextRun == null) && position == 1) {
          /* return character */
            nextRun = (TextStyleRun)_runVector.lastElement();
            position = nextRun.charCount();
        } else if (nextRun == null) {
            return null;
        }

        currentLine = lineForPosition(absPosition - 1);
        lastCharNumber = _lineBreaks[currentLine] + _startChar;
        lastCharPosition = infoForPosition(lastCharNumber, -1);
        oldHeight = _height;

        if (aString != null) {
            nextRun.insertStringAt(aString, position);
            charCount = aString.length();
        } else {
            nextRun.insertCharAt(aChar, position);
            charCount = 1;
        }

        computeLineBreaksAndHeights(_owner.bounds.width,currentLine);

        /* information about new insertion point position */
        newPosition = infoForPosition(absPosition + charCount, -1);

        if( oldHeight != _height ) {
            newPosition.setRedrawCurrentParagraphOnly(false);
            newPosition.setRedrawCurrentLineOnly(false);
            return newPosition;
        }

        newPosition.setRedrawCurrentParagraphOnly(true);

      /* did we jump to a different line? */
        if (newPosition._lineNumber != lastCharPosition._lineNumber) {
            newPosition.setRedrawCurrentParagraphOnly(false);
            if (lastCharPosition._lineNumber < newPosition._lineNumber) {
                newPosition.setUpdateLine(lastCharPosition._lineNumber);
            } else {
                newPosition.setUpdateLine(newPosition._lineNumber);
            }

            return newPosition;
        } else
            newPosition.setRedrawCurrentParagraphOnly(true);

      /* we're on the same line, but did the character at the end change? */
        newLastCharPosition = infoForPosition(lastCharNumber + 1, -1);
        if (newLastCharPosition != null && lastCharPosition != null &&
            newLastCharPosition._lineNumber == lastCharPosition._lineNumber) {
            if( currentParagraphFormat().wrapsUnderFirstCharacter() )
                newPosition.setRedrawCurrentLineOnly(false);
            else
                newPosition.setRedrawCurrentLineOnly(true);
        } else
            newPosition.setRedrawCurrentLineOnly(false);

        return newPosition;
    }

     TextPositionInfo removeCharAt(int absPosition) {
        TextPositionInfo        firstCharPosition, firstNextLineCharPosition,
                                newPosition, newFirstCharPosition,
                                newNextLineFirstCharPosition;
        TextStyleRun            nextRun = null;
        int                     i, count, position, currentLine,
                                firstCharNumber, firstNextLineCharNumber,
                                oldHeight;

        if (absPosition <= _startChar) {
            return null;
        }
        //ALERT! - THIS BUG WAS A JIT BUG!!
//        absPosition--;
        absPosition = absPosition - 1;

      /* find the run that has this character */
        position = absPosition - _startChar;
        count = _runVector.count();
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);

            if (nextRun.charCount() <= position) {
                position -= nextRun.charCount();
                continue;
            }

            break;
        }

        if ((i >= count || nextRun == null) && position == 0) {
          /* return character */
            nextRun = (TextStyleRun)_runVector.lastElement();
            position = nextRun.charCount() - 1;
        } else if (nextRun == null) {
            return null;
        }

        currentLine = lineForPosition(absPosition - 1);
        if (currentLine == 0) {
            firstCharNumber = _startChar + 1;
        } else {
            firstCharNumber = _lineBreaks[currentLine - 1] + _startChar + 1;
        }
        firstNextLineCharNumber = _lineBreaks[currentLine] + _startChar + 1;
        if (firstNextLineCharNumber > _startChar + _charCount) {
            firstNextLineCharNumber = _startChar + _charCount;
        }
        firstNextLineCharPosition = infoForPosition(firstNextLineCharNumber,
                                                    -1);
        firstCharPosition = infoForPosition(firstCharNumber, -1);
        oldHeight = _height;

        nextRun.removeCharAt(position);
        if (nextRun.charCount() == 0 && _runVector.count() > 1) {
            _runVector.removeElement(nextRun);
        }
        computeLineBreaksAndHeights(_owner.bounds.width);

      /* information about new insertion point position */
        newPosition = infoForPosition(absPosition, -1);

        if (oldHeight == _height) {
            newPosition.setRedrawCurrentParagraphOnly(true);
        }

      /* did we jump down a line? */
        if (newPosition._lineNumber != firstCharPosition._lineNumber) {
            if (firstCharPosition._lineNumber < newPosition._lineNumber) {
                newPosition.setUpdateLine(firstCharPosition._lineNumber);
            } else {
                newPosition.setUpdateLine(newPosition._lineNumber);
            }

            return newPosition;
        }

      /* did part of the next line come up? */
        newNextLineFirstCharPosition = infoForPosition(
                                            firstNextLineCharNumber - 1, -1);

        if (firstNextLineCharPosition._lineNumber !=
            newNextLineFirstCharPosition._lineNumber) {
            if (firstNextLineCharPosition._lineNumber <
                newNextLineFirstCharPosition._lineNumber) {
                newPosition.setUpdateLine(
                                        firstNextLineCharPosition._lineNumber);
            } else {
                newPosition.setUpdateLine(
                                    newNextLineFirstCharPosition._lineNumber);
            }

            return newPosition;
        }

      /* we're on the same line, but did the character at the end change? */
        newFirstCharPosition = infoForPosition(firstCharNumber, -1);

        if (oldHeight == _height &&
            newFirstCharPosition != null && firstCharPosition != null &&
           newFirstCharPosition._lineNumber == firstCharPosition._lineNumber) {
            if( currentParagraphFormat().wrapsUnderFirstCharacter())
                newPosition.setRedrawCurrentLineOnly(false);
            else
                newPosition.setRedrawCurrentLineOnly(true);
        }

        return newPosition;
    }

    TextStyleRun createNewRunAt(int absPosition) {
        TextPositionInfo        breakPosition;
        TextStyleRun            newRun;
        int                     position;

        breakPosition = infoForPosition(absPosition, -1);
        if (breakPosition == null) {
            return null;
        }

        position = _runVector.indexOfIdentical(breakPosition._textRun);
        newRun = breakPosition._textRun.breakAt(breakPosition._positionInRun);
        insertRunAt(newRun, position + 1);
        return newRun;
    }


  TextParagraph createNewParagraphAt(int absPosition) {
        TextParagraph           newParagraph;
        TextStyleRun            newRun = null, firstRun, previousRun;
        int                     position, count, i, runCount;
        TextParagraphFormat format = currentParagraphFormat();
        int positionInRun;
        TextStyleRun run;

        collectEmptyRuns();

        run = runForCharPosition( absPosition );
        positionInRun = absPosition - run.rangeIndex();

        newParagraph = new TextParagraph(_owner, format);

        if (positionInRun == 0) {
            Hashtable attr;
            i = _runVector.indexOfIdentical(run);
            attr = TextView.attributesByRemovingStaticAttributes(run.attributes());
            if (i == 0) {
                newRun = run.createEmptyRun(attr);
                _runVector.insertElementAt(newRun, 0);
            } else {
                i--;
            }
            newParagraph.addRun( new TextStyleRun(this,"",attr));

        } else if (run.containsATextAttachment()) {
            int k;
            i = _runVector.indexOfIdentical(run);
            k = i-1;
            previousRun=null;
            while(k > 0) {
                previousRun = (TextStyleRun)objectAt(_runVector, k);
                if( previousRun == null )
                    break;
                else if(! previousRun.containsATextAttachment())
                    break;
                else
                    k--;
            }

            if (previousRun == null) {
                newRun = new TextStyleRun(this,"",null);
                newParagraph.addRun(newRun);
            } else {
                newRun = new TextStyleRun(this, "", TextView.attributesByRemovingStaticAttributes(
                                                previousRun.attributes()));
                newParagraph.addRun(newRun);
            }
        } else {
            newRun = firstRun = run.breakAt(positionInRun);
            newParagraph.addRun(firstRun);
            i = _runVector.indexOfIdentical(run);
        }

        if (i < 0) {
            return newParagraph;
        }
        i++;

        count = _runVector.count();
        runCount = 0;
        for (; i < count; i++, runCount++) {
            newParagraph.addRun((TextStyleRun)_runVector.elementAt(i));
        }
        while (runCount-- > 0) {
            _runVector.removeLastElement();
        }

        return newParagraph;
    }

    void drawBackgroundForLine(Graphics g, int lineNumber, int currentX,
                              int currentY) {
        TextPositionInfo        startInfo, endInfo;
        Rect                    selectionRect = null, nonSelectionRect,
                                nonSelectionRect2;
        int                     selectionStart, selectionEnd, maxWidth,defaultMaxWidth,
                                lineStartCharNumber, lineEndCharNumber;

        TextParagraphFormat format = currentParagraphFormat();
        TextParagraphFormat defaultFormat = (TextParagraphFormat)
            _owner.defaultAttributes().get(TextView.PARAGRAPH_FORMAT_KEY);


        maxWidth = _owner.bounds.width - format._leftMargin -
                   format._rightMargin;
        defaultMaxWidth = _owner.bounds.width - defaultFormat._leftMargin -
                   defaultFormat._rightMargin;

        if (lineNumber == 0) {
            maxWidth -= format._leftIndent;
        }

        if (lineNumber < 0 || lineNumber >= _breakCount ||
            (!_owner.hasSelectionRange() && !_owner.insertionPointVisible)) {
            if (!_owner.isTransparent()) {
                g.setColor(_owner._backgroundColor);
                g.fillRect(defaultFormat._leftMargin, currentY, defaultMaxWidth,
                           _lineHeights[lineNumber]);
            }
            return;
        }

        selectionStart = _owner.selectionStart();
        selectionEnd = _owner.selectionEnd();
        startInfo = _owner.selectionStartInfo();
        endInfo = _owner.selectionEndInfo();

        if (lineNumber == 0) {
            lineStartCharNumber = _startChar;
        } else {
            lineStartCharNumber = _startChar + _lineBreaks[lineNumber - 1];
        }
        lineEndCharNumber = _startChar + _lineBreaks[lineNumber];
        if (lineNumber == _breakCount - 1) {
            lineEndCharNumber++;
        }

        if (selectionStart == selectionEnd || selectionStart > lineEndCharNumber ||
            selectionEnd < lineStartCharNumber) {
            if (!_owner.isTransparent()) {
                g.setColor(_owner._backgroundColor);
                g.fillRect(defaultFormat._leftMargin, currentY, defaultMaxWidth,
                           _lineHeights[lineNumber]);
            }
            return;
        }

        if (lineStartCharNumber >= selectionStart &&
            lineEndCharNumber <= selectionEnd) {
          /* all characters on this line selected */
            selectionRect = TextView.newRect(defaultFormat._leftMargin,currentY,defaultMaxWidth,
                                             _lineHeights[lineNumber]);
            nonSelectionRect = nonSelectionRect2 = null;
        } else if (lineStartCharNumber >= selectionStart &&
                   lineEndCharNumber > selectionEnd &&
                   lineStartCharNumber < selectionEnd) {
          /* first through some # selected on this line */
            if( selectionStart == lineStartCharNumber ) {
                selectionRect = TextView.newRect(currentX, currentY,
                                                 endInfo._x - currentX,
                                                 _lineHeights[lineNumber]);
            } else {
                selectionRect = TextView.newRect(defaultFormat._leftMargin,currentY,
                                                 endInfo._x - defaultFormat._leftMargin,
                                                 _lineHeights[lineNumber]);
            }

            nonSelectionRect = TextView.newRect(selectionRect.maxX(), currentY,
                                                defaultMaxWidth - selectionRect.width,
                                                _lineHeights[lineNumber]);

            nonSelectionRect2 = null;
        } else if (lineStartCharNumber < selectionStart && selectionStart < lineEndCharNumber &&
                   lineEndCharNumber <= selectionEnd) {
          /* some # through last selected on this line */
            if (startInfo._textRun._paragraph != this ||
                startInfo._lineNumber > lineNumber) {
                nonSelectionRect = TextView.newRect(defaultFormat._leftMargin, currentY,
                                                    defaultMaxWidth,
                                                    _lineHeights[lineNumber]);
            } else {
            selectionRect = TextView.newRect(startInfo._x, currentY,
                                             _owner.bounds.width - defaultFormat._rightMargin -
                                             startInfo._x, _lineHeights[lineNumber]);
            nonSelectionRect = TextView.newRect(defaultFormat._leftMargin, currentY,
                                            selectionRect.x - defaultFormat._leftMargin,
                                            _lineHeights[lineNumber]);
            }
            nonSelectionRect2 = null;
        } else if (lineStartCharNumber < selectionStart &&
                   lineEndCharNumber > selectionEnd) {
          /* entire selection is a subrange of characters on this line */
            selectionRect = TextView.newRect(startInfo._x, currentY,
                                         endInfo._x - startInfo._x,
                                         _lineHeights[lineNumber]);
            nonSelectionRect = TextView.newRect(selectionRect.maxX(), currentY,
                                            defaultMaxWidth - selectionRect.width,
                                            _lineHeights[lineNumber]);
            nonSelectionRect2 = TextView.newRect(defaultFormat._leftMargin, currentY,
                                             selectionRect.x - defaultFormat._leftMargin,
                                             _lineHeights[lineNumber]);
        } else {
            nonSelectionRect = TextView.newRect(defaultFormat._leftMargin, currentY, defaultMaxWidth,
                                                _lineHeights[lineNumber]);
            nonSelectionRect2 = null;
        }

        if (!_owner.isTransparent()) {
            g.setColor(_owner._backgroundColor);
            if (nonSelectionRect != null) {
                g.fillRect(nonSelectionRect);
                TextView.returnRect(nonSelectionRect);
            }
            if (nonSelectionRect2 != null) {
                g.fillRect(nonSelectionRect2);
                TextView.returnRect(nonSelectionRect2);
            }
        }

        if (_owner.hasSelectionRange() && selectionRect != null && _owner.isSelectable()) {

            if (selectionRect.width == 0 && selectionRect.x == currentX) {
                selectionRect.sizeTo(4, selectionRect.height);
            }

            if(!_owner.isEditing()) {
                if(!_owner.isTransparent()) {
                    g.setColor(_owner._backgroundColor);
                    g.fillRect(selectionRect);
                }
            } else {
                g.setColor(_owner._selectionColor);
                g.fillRect(selectionRect);
            }

            TextView.returnRect(selectionRect);
        }
    }

    void drawLine(Graphics g, int lineNumber) {
        TextStyleRun    nextRun;
        int             i, charCount = 0, runNumber = 1, currentX,
                        currentY, runChars, runStartChar, deltaX = 0,
                        maxWidth, availableWidth;
        TextParagraphFormat format = currentParagraphFormat();

        if (lineNumber >= _breakCount) {
            return;
        }

        maxWidth = _owner.bounds.width - format._leftMargin -
                   format._rightMargin;
        currentY = _y;

        if (!_owner.isTransparent()) {
            g.setColor(_owner._backgroundColor);
            g.fillRect(0, currentY, format._leftMargin,
                       _height);
            g.fillRect(_owner.bounds.width - format._rightMargin,
                       currentY, format._rightMargin + 1, _height);
        }

        nextRun = (TextStyleRun)_runVector.firstElement();
        runStartChar = 0;
        for (i = 0; i <= lineNumber; i++) {
            if (format._justification == Graphics.LEFT_JUSTIFIED) {
                currentX = format._leftMargin;
            } else if (format._justification ==
                                    Graphics.RIGHT_JUSTIFIED) {
                currentX = format._leftMargin +
                           _lineRemainders[i];
            } else {
                currentX = format._leftMargin +
                           _lineRemainders[i] / 2;
            }
            availableWidth = maxWidth;
            if (i == 0) {
                currentX += format._leftIndent;
                availableWidth -= format._leftIndent;
            } else if( format.wrapsUnderFirstCharacter()) {
                int newCurrentX = addWidthOfInitialTabs( currentX + format._leftIndent);
                availableWidth -= (newCurrentX - currentX);
                currentX = newCurrentX;
            }

            if (i == 0) {
                charCount = _lineBreaks[i];
            } else {
                charCount = _lineBreaks[i] - _lineBreaks[i - 1];
            }

            if (i == lineNumber) {
                drawBackgroundForLine(g, i, currentX, currentY);
            }

            while (charCount > 0) {
                runChars = nextRun.charCount() - runStartChar;
                if (charCount >= runChars) {
                    if (i == lineNumber) {
                        deltaX = nextRun.drawCharacters(g, runStartChar,
                                                        runChars, currentX,
                                                        currentY +
                                                                _baselines[i],
                                                       format._tabStops);
                    }
                    charCount -= runChars;
                    nextRun = (TextStyleRun)objectAt(_runVector, runNumber++);
                    while (nextRun != null && nextRun.charCount() == 0) {
                        nextRun = (TextStyleRun)objectAt(_runVector,
                                                                runNumber++);
                    }
                    runStartChar = 0;
                } else {
                    if (i == lineNumber) {
                        deltaX = nextRun.drawCharacters(g, runStartChar,
                                                        charCount, currentX,
                                                        currentY +
                                                                _baselines[i],
                                                        format._tabStops);
                    }
                    runStartChar += charCount;
                    charCount = 0;
                }
                currentX += deltaX;
                availableWidth -= deltaX;
            }

            currentY += _lineHeights[i];
        }
    }

    void drawView(Graphics g, Rect textBounds) {
        TextStyleRun    nextRun;
        Rect            lineRect, clipRect;
        int             i, charCount = 0, runNumber = 1, currentX, currentY,
                        runChars, runStartChar, deltaX, availableWidth,
                        maxWidth,defaultMaxWith;
        boolean         canDraw;
        TextParagraphFormat format = currentParagraphFormat();
        TextParagraphFormat defaultFormat = (TextParagraphFormat)
            _owner.defaultAttributes().get(TextView.PARAGRAPH_FORMAT_KEY);

        maxWidth = _owner.bounds.width - format._leftMargin -
                   format._rightMargin;


        currentY = _y;

        if (!_owner.isTransparent()) {
            g.setColor(_owner._backgroundColor);
            g.fillRect(textBounds.maxX() - defaultFormat._rightMargin,
                       currentY, defaultFormat._rightMargin + 1, _height);
        }

        nextRun = (TextStyleRun)_runVector.firstElement();
        runStartChar = 0;
        lineRect = TextView.newRect();
        clipRect = g.clipRect();
        for (i = 0; i < _breakCount; i++) {
            if (format._justification == Graphics.LEFT_JUSTIFIED) {
                currentX = textBounds.x + format._leftMargin;
            } else if (format._justification ==
                                        Graphics.RIGHT_JUSTIFIED) {
                currentX = textBounds.x + format._leftMargin +
                           _lineRemainders[i];
            } else {
                currentX = textBounds.x + format._leftMargin +
                           _lineRemainders[i] / 2;
            }
            availableWidth = maxWidth;
            if (i == 0) {
                currentX += format._leftIndent;
                availableWidth -= format._leftIndent;
            } else if( format.wrapsUnderFirstCharacter()) {
                int newCurrentX = addWidthOfInitialTabs( currentX + format._leftIndent);
                availableWidth -= (newCurrentX - currentX);
                currentX = newCurrentX;
            }

            if (i == 0) {
                charCount = _lineBreaks[i];
            } else {
                charCount = _lineBreaks[i] - _lineBreaks[i - 1];
            }


            lineRect.setBounds(0, currentY, _owner.bounds.width,
                               _lineHeights[i]);
            canDraw = clipRect.intersects(lineRect);

            if(!_owner.isTransparent()) {
                g.setColor(_owner.backgroundColor());
                g.fillRect(textBounds.x,currentY,currentX-textBounds.x,_lineHeights[i]);
            }
            if (canDraw) {
                drawBackgroundForLine(g, i, currentX, currentY);
            }
            while (charCount > 0) {
                runChars = nextRun.charCount() - runStartChar;
                if (charCount >= runChars) {
                    if (canDraw) {
                        deltaX = nextRun.drawCharacters(g, runStartChar,
                                                        runChars, currentX,
                                                        currentY +
                                                                _baselines[i],
                                                        format._tabStops);
                    } else {
                        deltaX = 0;
                    }
                    charCount -= runChars;
                    nextRun = (TextStyleRun)objectAt(_runVector, runNumber++);
                    while (nextRun != null && nextRun.charCount() == 0) {
                        nextRun = (TextStyleRun)objectAt(_runVector,
                                                                runNumber++);
                    }
                    runStartChar = 0;
                } else {
                    if (canDraw) {
                        deltaX = nextRun.drawCharacters(g, runStartChar,
                                                        charCount, currentX,
                                                        currentY +
                                                                _baselines[i],
                                                        format._tabStops);
                    } else {
                        deltaX = 0;
                    }
                    runStartChar += charCount;
                    charCount = 0;
                }
                currentX += deltaX;
                availableWidth -= deltaX;
            }

            currentY += _lineHeights[i];
        }

        TextView.returnRect(lineRect);
    }

    void draw() {
        Rect    tmpRect;

        if (_owner != null) {
            tmpRect = TextView.newRect(0, _y,
                                       _owner.bounds.width, _height);
            _owner.draw(tmpRect);
            TextView.returnRect(tmpRect);
        }
    }

    void subsumeParagraph(TextParagraph aParagraph) {
        TextStyleRun    nextRun;
        int             count, i;

        if (aParagraph == null) {
            return;
        }

        count = aParagraph._runVector.count();
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)aParagraph._runVector.elementAt(i);
            if (nextRun.charCount() > 0 ||
                (_runVector.isEmpty() && i + 1 == count)) {
                addRun(nextRun);
            }
        }
    }

    public String toString() {
        TextStyleRun            nextRun;
        FastStringBuffer        buffer;
        int                     i, count;

        buffer = new FastStringBuffer();
        buffer.append("[");
        count = _runVector.count();
        for (i = 0; i < count; i++) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);
            buffer.append(nextRun.toString());
        }
        buffer.append("]\n");
        return buffer.toString();
    }



/* archiving */


    /** Describes the TextParagraph class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.TextParagraph", 1);
        info.addField(FORMAT_KEY, OBJECT_TYPE);
        info.addField(RUNVECTOR_KEY, OBJECT_TYPE);
    }

    /** Encodes the TextParagraph instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(FORMAT_KEY, _format);
        encoder.encodeObject(RUNVECTOR_KEY, _runVector);
    }

    /** Decodes a TextParagraph instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        TextStyleRun    nextRun;
        int             i;

        _format = (TextParagraphFormat)decoder.decodeObject(FORMAT_KEY);
        _runVector = (Vector)decoder.decodeObject(RUNVECTOR_KEY);

        i = _runVector.count();
        while (i-- > 0) {
            nextRun = (TextStyleRun)_runVector.elementAt(i);
            nextRun.setParagraph(this);
        }
    }

    /** Finishes the TextParagraph instance decoding.  This method does
      * nothing.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }

    /* Return the contents of the paragraph */
     String stringForRange(Range absoluteRange) {
        TextStyleRun run;
        StringBuffer sb = new StringBuffer();
        int i,c;
        int runIndex = _startChar;
        Range intersection = TextView.allocateRange();
        String s;

        for( i = 0 , c = _runVector.count() ; i < c ; i++ ) {
            run = (TextStyleRun)_runVector.elementAt(i);
            intersection.index  = runIndex;
            intersection.length = run.charCount();
            intersection.intersectWith( absoluteRange );
            if( intersection.index != Range.nullRange().index ) {
                s = run.text().substring(intersection.index - runIndex,
                                               intersection.index - runIndex + intersection.length);

                sb.append(s);
            }
            runIndex += run.charCount();
        }
        /* Does the range includes \n */
        if((absoluteRange.index + absoluteRange.length - 1) == (_startChar + _charCount - 1))
            sb.append("\n");
        TextView.recycleRange( intersection );
        return sb.toString();
    }

     Range range() {
        return TextView.allocateRange( _startChar, _charCount );
    }

    int lineCount() {
        return _breakCount;
    }
}


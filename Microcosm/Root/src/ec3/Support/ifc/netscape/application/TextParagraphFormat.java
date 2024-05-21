// TextParagraphFormat.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that maintains a TextParagraph's formatting information
  * (justification, margins, tab stops and line spacing).  To apply a
  * TextParagraphFormat, use TextView's <b>addAttributeForRange()</b> method.
  * @note 1.0 added toString
  */
public class TextParagraphFormat implements Cloneable, Codable {
    int         _leftIndent, _leftMargin, _rightMargin, _lineSpacing,
                _tabStops[], _justification;
    boolean     _wrapsUnderFirstCharacter;

    static final String         LEFTINDENT_KEY = "leftIndent";
    static final String         LEFTMARGIN_KEY = "leftMargin";
    static final String         RIGHTINDENT_KEY = "rightMargin";
    static final String         LINESPACING_KEY = "lineSpacing";
    static final String         TABSTOPS_KEY = "tabStops";
    static final String         JUSTIFICATION_KEY = "justification";
    static final String         WRAPS_UNDER_FIRST_CHARACTER_KEY =
                                                "wrapsUnderFirstCharacter";

/* constructors */
    /** Constructs an empty TextParagraphFormat.  It's easier to get the
      * default paragraph format from the TextView's default attributes,
      * clone it, and modify the clone, than it is to create a
      * TextParagraphFormat yourself.
      */
    public TextParagraphFormat() {
        super();
    }



/* methods */


    /** Clones the TextParagraphFormat.
      */
    public Object clone() {
        TextParagraphFormat     cloneFormat;
        Object                  clone;

        try {
            clone = super.clone();
            cloneFormat = (TextParagraphFormat)clone;

            if (_tabStops != null) {
                cloneFormat.clearAllTabPositions();
                cloneFormat.setTabPositions(_tabStops);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InconsistencyException(this +
                                        ": clone() not supported :" + e);
        }
    }

    /** Sets the distance in pixels from the left border to the beginning of
      * the text.
      */
    public void setLeftMargin(int leftMargin) {
        _leftMargin = leftMargin;
    }

    /** Returns the distance in pixels from the left border to the beginning
      * of the text.
      * @see #setLeftMargin
      */
    public int leftMargin() {
        return _leftMargin;
    }

    /** Sets the distance in pixels from the left border to the paragraph's
      * first character.
      */
    public void setLeftIndent(int indent) {
        _leftIndent = indent;
    }

    /** Returns the distance in pixels from the left border to the paragraph's
      * first character.
      * @see #setLeftIndent
      */
    public int leftIndent() {
        return _leftIndent;
    }

    /** Sets the distance between the end of the text and the right border.
      */
    public void setRightMargin(int rightMargin) {
        _rightMargin = rightMargin;
    }

    /** Returns the distance between the end of the text and the right border.
      */
    public int rightMargin() {
        return _rightMargin;
    }

    /** Sets the minimum distance between lines of text.
      */
    public void setLineSpacing(int spacing) {
        _lineSpacing = spacing;
    }

    /** Returns the minimum distance between lines of text.
      */
    public int lineSpacing() { return _lineSpacing;}

    /** Sets the TextParagraphFormat's justification. Possibles values are
      * <b>Graphics.LEFT_JUSTIFIED</b>, <b>Graphics.CENTERED</b> and
      * <b>Graphics.RIGHT_JUSTIFIED</b>
      */
    public void setJustification(int justification) {
        if (_justification < 0 || _justification > 3) {
            return;
        }

        _justification = justification;
    }

    /** Returns the TextParagraphFormat's justification.
      */
    public int justification() { return _justification; }

    /** Sets whether line wrapping should occur under the first character of
      * the first line.  This feature works only if the text is LEFT_JUSTIFIED.
      *  @private
      */
    public void setWrapsUnderFirstCharacter(boolean flag) {
        _wrapsUnderFirstCharacter = flag;
    }

    /** Returns <b>true</b> if line wrapping should occur under the first
      * character of the first line.
      * @private
      */
    public boolean wrapsUnderFirstCharacter() {
        if( _justification == Graphics.LEFT_JUSTIFIED )
            return _wrapsUnderFirstCharacter;
        else
            return false;
    }

    /** Removes all the tab positions.
      */
    public void clearAllTabPositions() {
        _tabStops = null;
    }

    /** Adds a tab at pixel <b>position</b>.
      */
    public void addTabPosition(int position) {
        int     i, count;

        if (position < 0) {
            return;
        }

        if (_tabStops == null) {
            _growTabArrayTo(20);
            _tabStops[0] = position;
            return;
        }

        count = _tabStops.length;
        if (_tabStops[count - 1] != -1) {
            _growTabArrayTo(_tabStops.length + 10);
            count = _tabStops.length;
        }

        for (i = 0; i < count; i++) {
            if (_tabStops[i] > position) {
                return;
            } else if (_tabStops[i] == -1) {
                _tabStops[i] = position;
                break;
            }
        }
    }

    /** Replaces the current tab positions with the positions described by
      * <b>tabArray</b>.  Each tab position is the number of pixels from the
      * left border.
      */
    public void setTabPositions(int[] tabArray) {
        int     count, i;

        if (tabArray == null) {
            return;
        }

        clearAllTabPositions();
        count = tabArray.length;
        for (i = 0; i < count; i++) {
            addTabPosition(tabArray[i]);
        }
    }

    /** Returns the current tab positions.
      */
    public int[] tabPositions() {
        int tabCount = 0;
        int i,c;
        int result[];

        for(i=0,c=_tabStops.length; i < c ; i++ )
            if( _tabStops[i] == -1 )
                break;
            else
                tabCount++;

        result = new int[tabCount];
        System.arraycopy(_tabStops,0,result,0,tabCount);

        return result;
    }

    /* Returns the position of tab number <b>tabNumber</b>. */
    public int positionForTab(int tabNumber) {
        if (_tabStops == null || tabNumber < 0 ||
            tabNumber >= _tabStops.length) {
            return -1;
        }

        return _tabStops[tabNumber];
    }




/* archiving */


    /** Describes the TextParagraphFormat class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.TextParagraphFormat", 1);
        info.addField(LEFTINDENT_KEY, INT_TYPE);
        info.addField(LEFTMARGIN_KEY, INT_TYPE);
        info.addField(RIGHTINDENT_KEY, INT_TYPE);
        info.addField(LINESPACING_KEY, INT_TYPE);
        info.addField(TABSTOPS_KEY, STRING_TYPE);
        info.addField(JUSTIFICATION_KEY, INT_TYPE);
        info.addField(WRAPS_UNDER_FIRST_CHARACTER_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the TextParagraphFormat instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        FastStringBuffer        buffer;
        int                     i, count;

        encoder.encodeInt(LEFTINDENT_KEY, _leftIndent);
        encoder.encodeInt(LEFTMARGIN_KEY, _leftMargin);
        encoder.encodeInt(RIGHTINDENT_KEY, _rightMargin);
        encoder.encodeInt(LINESPACING_KEY, _lineSpacing);

        if (_tabStops != null) {
            buffer = new FastStringBuffer();
            count = _tabStops.length;
            for (i = 0; i < count; i++) {
                buffer.append(_tabStops[i] + ".");
            }
            encoder.encodeString(TABSTOPS_KEY, buffer.toString());
        } else {
            encoder.encodeString(TABSTOPS_KEY, null);
        }

        encoder.encodeInt(JUSTIFICATION_KEY, _justification);
        encoder.encodeBoolean(WRAPS_UNDER_FIRST_CHARACTER_KEY,
                              _wrapsUnderFirstCharacter);
    }

    /** Decodes the TextParagraphFormat instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        String                          tabData;
        java.util.StringTokenizer       tokenizer;

        _leftIndent = decoder.decodeInt(LEFTINDENT_KEY);
        _leftMargin = decoder.decodeInt(LEFTMARGIN_KEY);
        _rightMargin = decoder.decodeInt(RIGHTINDENT_KEY);
        _lineSpacing = decoder.decodeInt(LINESPACING_KEY);

        tabData = decoder.decodeString(TABSTOPS_KEY);
        if (tabData != null) {
            try {
                tokenizer = new java.util.StringTokenizer(tabData, ".", false);
                addTabPosition(Integer.parseInt(tokenizer.nextToken()));
            } catch (Exception e) {
                throw new CodingException("Illegal tab stop data: " +
                                                "\"" + tabData + "\"");
            }
        }

        _justification = decoder.decodeInt(JUSTIFICATION_KEY);
        _wrapsUnderFirstCharacter =
                        decoder.decodeBoolean(WRAPS_UNDER_FIRST_CHARACTER_KEY);
    }

    /** Finishes the TextParagraphFormat instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }

    private void _growTabArrayTo(int newSize) {
        int     i, count, oldArray[];

        if (newSize < 1) {
            return;
        }

        if (_tabStops != null && _tabStops.length >= newSize) {
            return;
        }

        oldArray = _tabStops;
        i = newSize + 5;
        _tabStops = new int[i];
        while (i-- > 0) {
            _tabStops[i] = -1;
        }

        if (oldArray == null) {
            return;
        }

        count = oldArray.length;
        for (i = 0; i < count; i++) {
            _tabStops[i] = oldArray[i];
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("leftIndent = " + _leftIndent + " " +
                  "leftMargin = " + _leftMargin + " " +
                  "rightMargin = " + _rightMargin + " " +
                  "lineSpacing = " + _lineSpacing + " " +
                  "justification = " + _justification );
        return sb.toString();
    }
}

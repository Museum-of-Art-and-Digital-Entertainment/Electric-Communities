// FontMetrics.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass that manages a Font instance's metrics.<p>
  * <i><b>Note:</b> FontMetrics are currently retrieve using the
  * default java.awt.Toolkit, so they contain screen metric information
  * only.</i>.
  */
public class FontMetrics {
    Font                        _font;
    java.awt.FontMetrics        _awtMetrics;

    final static String         LEADING = "Leading", ASCENT = "Ascent",
                                DESCENT = "Descent",
                                TOTAL_HEIGHT = "Total Height",
                                MAX_ASCENT = "Maximum Ascent",
                                MAX_DESCENT = "Maximum Descent",
                                MAX_ADVANCE = "Maximum Advance";

    /** Constructs a FontMetrics not associated with a Font. This method
      * is only useful when decoding.
      */
    public FontMetrics() {
        super();
    }

    /** Creates a FontMetrics associated with <b>aFont</b>.
      */
    public FontMetrics(Font aFont) {
        this();

        _font = aFont;
        if (!_font.wasDownloaded()) {
            _awtMetrics =
                AWTCompatibility.awtToolkit().getFontMetrics(_font._awtFont);
        }
    }

    FontMetrics(java.awt.FontMetrics awtMetrics) {
        this();

        _font = AWTCompatibility.fontForAWTFont(awtMetrics.getFont());
        _awtMetrics = awtMetrics;
    }

    /** Returns the Font associated with this FontMetrics.
      */
    public Font font() {
        return _font;
    }

    /** Returns the standard leading (line spacing) for the Font, the logical
      * amount of space to be reserved between the descent of one line of
      * text and the ascent of the next line. The height metric is calculated
      * to include this extra space.
      */
    public int leading() {
        if (_awtMetrics != null) {
            return _awtMetrics.getLeading();
        }

        return _font._intValueFromDescription(LEADING);
    }

    /** Returns the Font's ascent, the distance from the baseline to the
      * top of the characters.
      */
    public int ascent() {
        if (_awtMetrics != null) {
            return _awtMetrics.getAscent();
        }

        return _font._intValueFromDescription(ASCENT);
    }

    /** Returns the Font's descent, the distance from the baseline to the
      * bottom of the characters.
      */
    public int descent() {
        if (_awtMetrics != null) {
            return _awtMetrics.getDescent();
        }

        return _font._intValueFromDescription(DESCENT);
    }

    /** Returns the Font's total height, the distance between the baseline
      * of adjacent lines of text (leading + ascent + descent).
      */
    public int height() {
        if (_awtMetrics != null) {
            return _awtMetrics.getHeight();
        }

        return _font._intValueFromDescription(TOTAL_HEIGHT);
    }

    /** Returns the Font's total character height, the distance from the
      * bottom of the descent to the top of the ascent.
      */
    public int charHeight() {
        if (_awtMetrics != null) {
            return _awtMetrics.getAscent() + _awtMetrics.getDescent();
        }

        return _font._intValueFromDescription(ASCENT) +
               _font._intValueFromDescription(DESCENT);
    }

    /** Returns the maximum ascent of all characters in this Font. No
      * character extends further above the baseline than this metric.
      */
    public int maxAscent() {
        if (_awtMetrics != null) {
            return _awtMetrics.getMaxAscent();
        }

        return _font._intValueFromDescription(MAX_ASCENT);
    }

    /** Returns the maximum descent of all characters in this Font. No
      * character descends futher below the baseline than this metric.
      */
    public int maxDescent() {
        if (_awtMetrics != null) {
            return _awtMetrics.getMaxDecent();
        }

        return _font._intValueFromDescription(MAX_DESCENT);
    }

    /** Returns the maximum advance width of any character in this Font,
      * or <b>-1</b> if unknown.
      */
    public int maxAdvance() {
        if (_awtMetrics != null) {
            return _awtMetrics.getMaxAdvance();
        }

        return _font._intValueFromDescription(MAX_ADVANCE);
    }

    /** Returns <b>aChar</b>'s width in the FontMetric's Font.
      */
    public int charWidth(int aChar) {
        if (_awtMetrics != null) {
            return _awtMetrics.charWidth(aChar);
        }

        return 0;
    }

    /** Returns <b>aChar</b>'s width in the FontMetric's Font.
      */
    public int charWidth(char aChar) {
        if (_awtMetrics != null) {
            return _awtMetrics.charWidth(aChar);
        }

        return 0;
    }

    /** Returns <b>aString</b>'s width in the FontMetric's Font.
      */
    public int stringWidth(String aString) {
        int     length, i, index;

        if (aString == null) {
            return 0;
        } else if (_awtMetrics != null) {
            return _awtMetrics.stringWidth(aString);
        }

        length = 0;
        for (i = 0; i < aString.length(); i++) {
            index = (int)aString.charAt(i);
            if (index < 0 || index >= _font._widthsArray.length) {
                continue;
            }

            length += _font._widthsArray[index];
        }

        return length;
    }

    /** Returns the height of all strings in the FontMetric's Font.
      * Equivalent to the following code:
      * <pre>
      *     ascent() + descent();
      * </pre>
      */
    public int stringHeight() {
        return ascent() + descent();
    }

    /** Returns <b>aString</b>'s size in the FontMetric's Font.
      * Equivalent to the following code:
      * <pre>
      *     new Size(stringWidth(aString), stringHeight());
      * </pre>
      */
    public Size stringSize(String aString) {
        return new Size(stringWidth(aString), stringHeight());
    }

    /** Returns the width of <b>length</b> characters of the array
      * <b>data</b> in the FontMetric's Font, starting at <b>offset</b> within
      * the array.
      */
    public int charsWidth(char data[], int offset, int length) {
        if (_awtMetrics != null) {
            return _awtMetrics.charsWidth(data, offset, length);
        }

        return 0;
    }

    /** Returns the width of <b>length</b> bytes of the array <b>data</b>
      * in the FontMetric's Font, starting at <b>offset</b> within the array.
      */
    public int bytesWidth(byte data[], int offset, int length) {
        if (_awtMetrics != null) {
            return _awtMetrics.bytesWidth(data, offset, length);
        }

        return 0;
    }

    /** Returns the widths of the first 256 characters in the FontMetric's
      * Font.
      */
    public int[] widthsArray() {
        if (_awtMetrics != null) {
            return _awtMetrics.getWidths();
        }

        return _font._widthsArray;
    }

    /** Returns the index of the first glyph in a downloaded Font.
      */
    public int widthsArrayBase() {
        return _font._widthsArrayBase;
    }

    /** Returns the FontMetric's string representation.
      */
    public String toString() {
        if (_awtMetrics != null) {
            return _awtMetrics.toString();
        }

        return "";
    }
}

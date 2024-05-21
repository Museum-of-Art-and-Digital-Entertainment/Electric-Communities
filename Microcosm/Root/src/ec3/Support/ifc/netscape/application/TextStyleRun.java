// TextStyleRun.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** @private
  * @note 1.0 changes
  */
public class TextStyleRun extends Object implements Codable {
    private static final String CONTENTS_KEY   = "contents";
    private static final String ATTRIBUTES_KEY = "attributes";


    TextParagraph       _paragraph;
    FastStringBuffer    _contents;
    Hashtable           _attributes;
    FontMetrics         _fontMetricsCache;
    int                 _remainder;

/* constructors */

     public TextStyleRun() {
        super();
    }

     TextStyleRun(TextParagraph owner) {
        this();
        init(owner);
    }

     TextStyleRun(TextParagraph owner, String contents, Hashtable attributes) {
        this();
        init(owner, contents, attributes);
    }

    TextStyleRun(TextParagraph owner, String aString, int firstIndex,int lastIndex,Hashtable attributes) {
        this();
        init(owner, aString,firstIndex,lastIndex,attributes);
    }

/* initializers */


     void init(TextParagraph owner) {
        _paragraph = owner;
    }

     void init(TextParagraph owner, String contents, Hashtable attributes) {
        init(owner);
        setText(contents);
        setAttributes(attributes);
    }

     void init(TextParagraph owner, String contents,int firstIndex,
               int lastIndex,Hashtable attributes) {
         init(owner);
         setText(contents,firstIndex,lastIndex);
         setAttributes(attributes);
     }

    TextStyleRun createEmptyRun() {
        return new TextStyleRun(_paragraph, "", TextView.attributesByRemovingStaticAttributes(_attributes));
    }

    TextStyleRun createEmptyRun(Hashtable attributes) {
        return new TextStyleRun(_paragraph, "", attributes);
    }

     void setParagraph(TextParagraph aParagraph) {
        _paragraph = aParagraph;
    }

     TextParagraph paragraph() {
        return _paragraph;
    }

     void setText(String text) {
        _contents = new FastStringBuffer(text);
    }

    void setText(String text,int firstIndex,int lastIndex) {
      _contents = new FastStringBuffer(text,firstIndex,lastIndex);
    }

    void setText(StringBuffer text) {
        setText(text.toString());
    }

    int rangeIndex() {
        int result = _paragraph._startChar;
        Vector v = _paragraph.runsBefore(this);
        int i,c;

        for(i=0,c=v.count(); i < c ; i++ )
            result += ((TextStyleRun)v.elementAt(i)).charCount();
        return result;
    }

    Range range() {
        return TextView.allocateRange(rangeIndex(),charCount());
    }

    /** Accessing vital attributes */
    private Font getFont() {
        Font f = null;
        if( _paragraph.owner().usesSingleFont() ) {
          f = (Font) _paragraph.owner().defaultAttributes().get(TextView.FONT_KEY);
          return f;
        }

        if( _attributes != null )
            f = (Font) _attributes.get(TextView.FONT_KEY);
        if( f == null )
            f = (Font) _paragraph.owner().defaultAttributes().get(TextView.FONT_KEY);
        return f;
    }

    private Color getColor() {
        Color c = null;
        if( _attributes != null ) {
          if( _attributes.get(TextView.LINK_KEY) != null ) {
            if( _attributes.get(TextView.LINK_IS_PRESSED_KEY) != null)
              c = (Color) _attributes.get(TextView.PRESSED_LINK_COLOR_KEY);
            else
              c = (Color) _attributes.get(TextView.LINK_COLOR_KEY);
          } else
            c = (Color) _attributes.get(TextView.TEXT_COLOR_KEY);
        } else
            c = (Color) _paragraph.owner().defaultAttributes().get(TextView.TEXT_COLOR_KEY);
        return c;
    }

    private void validateFontMetricsCache() {
        if( _paragraph.owner().usesSingleFont() ||
            _attributes == null ||
            _attributes.get(TextView.FONT_KEY) == null )
            _fontMetricsCache = _paragraph.owner().defaultFontMetrics();
        else {
            if(_fontMetricsCache == null)
                _fontMetricsCache = getFont().fontMetrics();
        }
    }

    private void invalidateFontMetricsCache() {
        _fontMetricsCache = null;
    }

     boolean containsATextAttachment() {
        if( _attributes != null && _attributes.get(TextView.TEXT_ATTACHMENT_KEY) != null )
            return true;
        else
            return false;
    }

    Rect textAttachmentBoundsForOrigin(int x,int y,int baseline) {
        TextAttachment att;
        Rect result = new Rect();
        int baselineOffset;

        if( _attributes != null &&
            (att = (TextAttachment)_attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null ) {
            result.x = x;
            result.y = y + baseline - att.height() + attachmentBaselineOffset();
            result.width = att.width();
            result.height = att.height();
            return result;
        }
        return null;
    }

    /** Returns the character at <b>index</b>.  If <b>index</b> is invalid
      * or the TextStyleRun doesn't contain text, returns '\0';
      */
     char charAt(int index) {
        FastStringBuffer        buffer;
        int                     i;

        buffer = (FastStringBuffer)_contents;
        if (buffer.length() == 0 || index >= buffer.length()) {
            return '\0';
        }
        return buffer.charAt(index);
    }

    /** Inserts <b>aChar</b> at <b>position</b>.  If <b>index</b> is
      * greater than or equal to the length of text within the run,
      * appends <b>aChar</b>.  If this TextStyleRun doesn't contain text,
      * does nothing.
      */
     void insertCharAt(char aChar, int index) {
        FastStringBuffer        buffer;

        if (_contents == null) {
            buffer = new FastStringBuffer(aChar);
            _contents = buffer;
        } else {
            buffer = (FastStringBuffer)_contents;
            buffer.insert(aChar, index);
        }
    }

    /** Inserts <b>aString</b> at <b>index</b>.  If <b>index</b> is
      * greater than or equal to the length of text within the run,
      * appends <b>aString</b>.  If this TextStyleRun doesn't contain text,
      * does nothing.
      */
     void insertStringAt(String aString, int index) {
        FastStringBuffer        buffer;

        if ( index < 0 || aString == null) {
            return;
        }

        if (_contents == null) {
            buffer = new FastStringBuffer(aString);
            _contents = buffer;
        } else {
            buffer = (FastStringBuffer)_contents;
            buffer.insert(aString, index);
        }
    }

     void removeCharAt(int index) {
        FastStringBuffer        buffer;
        int                     i;

        buffer = (FastStringBuffer)_contents;
        if (buffer.length() == 0 || index >= buffer.length()) {
            return;
        }
        buffer.removeCharAt(index);
    }

    TextStyleRun breakAt(int index) {
        TextStyleRun            newRun;
        String                  theString;
        FastStringBuffer        buffer;

        buffer = (FastStringBuffer)_contents;
        if (buffer.length() == 0 || index >= buffer.length()) {
            return createEmptyRun(TextView.attributesByRemovingStaticAttributes(_attributes));
        }
        theString = buffer.toString();
        newRun = new TextStyleRun(_paragraph,
                                  theString.substring(index, buffer.length()),
                                  TextView.attributesByRemovingStaticAttributes(_attributes));

        buffer.truncateToLength(index);
        return newRun;
    }

    void cutBefore(int index) {
        FastStringBuffer        buffer;
        int                     i;

        buffer = (FastStringBuffer)_contents;
        if (buffer.length() == 0 || index >= buffer.length()) {
            return;
        }
        buffer.moveChars(index, 0);
    }

    void cutAfter(int index) {
        FastStringBuffer        buffer;

        buffer = (FastStringBuffer)_contents;
        if (buffer.length() == 0 || index >= buffer.length()) {
            return;
        }
        buffer.truncateToLength(index);
    }

     String text() {
        return _contents.toString();
    }

     public String toString() {
        String res = "";
        if( _attributes != null )
            res += _attributes.toString();
        else
            res += "{DefAttr}";
        res += "**(";
        res += _contents.toString();
        res += ")**";
        return res;
    }

    /** Returns the number of characters the TextStyleRun contains.  If the
      * run contains an Image or TextAttachment, returns 1.
      */
     int charCount() {
         return _contents.length();
     }

     int attachmentBaselineOffset() {
         Integer integer;
         if( _attributes != null &&
             (integer = (Integer) _attributes.get(TextView.TEXT_ATTACHMENT_BASELINE_OFFSET_KEY))
             != null ) {
             return integer.intValue();
         }
         return 0;
     }

    /** Returns the distance from the top of the TextStyleRun's contents to
      * the bottom of its contents.  In the case of text, this is just the sum
      * of the font's ascent and descent.  For an image or TextAttachment, it's the
      * height of the image or item.
      */
     int height() {
        TextAttachment     theItem;
        Image        theImage;

        if(_attributes != null && (theItem = (TextAttachment) _attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null ) {
          int baselineOffset = attachmentBaselineOffset();
          if( baselineOffset > 0 )
            return Math.max(theItem.height(),baselineOffset);
          else
            return theItem.height() + Math.abs(baselineOffset);
        } else {
            validateFontMetricsCache();
            return _fontMetricsCache.ascent() + _fontMetricsCache.descent();
        }
    }

    /** Returns the distance from the top of the TextStyleRun's contents to
      * the baseline.  In the case of text, this is just the font ascent.
      * For an image or TextAttachment, it's the height of the image or item.
      */
     int baseline() {
        TextAttachment theItem;

        if( _attributes != null &&
            (theItem = (TextAttachment)
             _attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null ) {
          int baselineOffset = attachmentBaselineOffset();
          return Math.max(theItem.height() - baselineOffset,0);
        } else {
            validateFontMetricsCache();
            return _fontMetricsCache.ascent();
        }
    }

    int _widthForTab(int position, int[] tabStops) {
        int     i;
        if (tabStops == null) {
            return 0;
        }
        for (i = 0; i < tabStops.length; i++) {
            if (position < tabStops[i]) {
                return tabStops[i] - position;
            }
        }
        return 0;
    }

    int _breakForSubstring(int startingChar, int count, int availableWidth) {
        int     width;

        width = _widthOfSubstring(startingChar, count, 0, null);
        while (width > availableWidth && count > 0) {
            count--;
            width = _widthOfSubstring(startingChar, count, 0, null);
        }
        return count;
    }

    int charsForWidth(int startingChar, int currentX,
                      int availableWidth, int maxAvailableWidth,
                      int[] tabStops) {
        TextAttachment                theItem;
        Image                   theImage;
        int                     charWidths[], count, i, nextWidth, wordWidth,
                                spaceWidth, start, tabPosition = -1, width,
                                breakPoint;
        char                 buf[];

        width = availableWidth;
        buf = new char[1];
        if (_contents == null) {
            _remainder = availableWidth;
            return 0;
        } else if( _attributes != null &&
                   (theItem = (TextAttachment)_attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null ) {
            if (theItem.width() > maxAvailableWidth) {
                if( width == maxAvailableWidth ) {
                    _remainder = 0;
                    return 1;
                } else
                    return 0;
            } else if (theItem.width() <= width) {
                _remainder = width - theItem.width();
                return 1;
            }
            _remainder = width;
            return 0;
        }

        validateFontMetricsCache();
        charWidths = _fontMetricsCache.widthsArray();

        count = _contents.length();
        i = startingChar;
        while (i < count && width > 0) {
            wordWidth = spaceWidth = 0;
            start = i;
            breakPoint = -1;

          /* find end of current word */
            while (i < count && !(_contents.buffer[i] == ' ' || _contents.buffer[i] == '\t')) {
                if( _contents.buffer[i] < 256 )
                    wordWidth += charWidths[_contents.buffer[i]];
                else {
                    buf[0] = _contents.buffer[i];
                    wordWidth += _fontMetricsCache.stringWidth(new String(buf));
                }
                i++;
                if (wordWidth > width && breakPoint == -1) {
                    breakPoint = i;
                    break;
                }
            }

          /* subsume spaces that follow */
            if (i < count && (_contents.buffer[i] == ' ' || _contents.buffer[i] == '\t')) {
                while (i < count && (_contents.buffer[i] == ' ' || _contents.buffer[i] == '\t')) {
                    if (_contents.buffer[i] == ' ') {
                        spaceWidth += charWidths[' '];
                    } else {
                        spaceWidth += _widthForTab(currentX + wordWidth +
                                                   spaceWidth, tabStops);
                        if (tabPosition == -1) {
                            tabPosition = i;
                        }
                    }
                    i++;
                }
            }

            /* do the word and spaces fit? */
            if ((wordWidth + spaceWidth) <= width) {
                width -= wordWidth + spaceWidth;
                continue;
            }

            /* they don't - if at end of line and word fits, or we're on a new
             * line, add the word and ignore the space/tab width (but do
             * include them as characters on the line)
             */
            if (width < maxAvailableWidth && wordWidth <= width) {
                width -= wordWidth;
                break;
            } else if (wordWidth > width && width >= maxAvailableWidth) {
                if (breakPoint != -1) {
                    count = _breakForSubstring(start,
                                               breakPoint - start,
                                               width);
                } else {
                    count = _breakForSubstring(start, i - start,
                                               width);
                }
                if (count > 0) {
                    i = startingChar + count;
                    width -= _widthOfSubstring(startingChar, count,
                                                        0, null);
                } else
                    i = start;
                break;
            } else
                i = start; /* We give up the end that does not fit */
            break;
        }

        if( width > 0 )
          _remainder = width;
        else
          _remainder = 0;

        if( i == startingChar && width == maxAvailableWidth ) {
            _remainder = 0;
            return 1;
        }
        return i - startingChar;
    }

    int _widthOfSubstring(int start, int count, int currentX, int[] tabStops) {
        int[]                   charWidths;
        int                     width = 0, i, endChar;
        char buf[];
        buf = new char[1];
        validateFontMetricsCache();
        charWidths = _fontMetricsCache.widthsArray();
        endChar = start + count;
        for (i = start; i < endChar; i++) {
            if (_contents.buffer[i] == '\t' && tabStops != null) {
                width += _widthForTab(currentX + width, tabStops);
                continue;
            }

            if( _contents.buffer[i] < 256 )
                width += charWidths[_contents.buffer[i]];
            else {
                buf[0] = _contents.buffer[i];
                width += _fontMetricsCache.stringWidth(new String(buf));
            }
        }

        return width;
    }

    /** Computes the width of <b>charCount</b> characters of the TextStyleRun
      * starting at <b>startingChar</b>.
      */
     int widthOfContents(int startingChar, int charCount, int currentX,
                               int[] tabStops) {
        TextAttachment                theItem;
        Image                   theImage;

        if( _attributes != null && (theItem = (TextAttachment) _attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null ) {
            return theItem.width();
        } else {
            validateFontMetricsCache();
            if( charCount == 0 )
                return 0;

            if (startingChar < 0) {
                startingChar = 0;
            }
            if ((startingChar + charCount) > _contents.length())
                charCount = _contents.length() - startingChar;

            return _widthOfSubstring(startingChar, charCount, currentX, tabStops);
        }
    }

    /** Draws <b>charCount</b> characters beginning with <b>startingChar</b>,
      * at (<b>x</b>, <b>y</b>) within <b>g</b>.  Returns the width of the
      * substring it drew.
      */
     int drawCharacters(Graphics g, int startingChar, int charCount,
                              int x, int y, int[] tabStops) {
        TextAttachment                theItem;
        Rect                    tmpRect;
        char[]                  charArray;
        int[]                   charWidths;
        int                     endChar, width, nextTab, count, totalWidth;

        if (g == null) {
            return 0;
        } else if (_attributes != null  &&
                   (theItem = (TextAttachment) _attributes.get(TextView.TEXT_ATTACHMENT_KEY)) != null) {
          int baselineOffset = attachmentBaselineOffset();
            tmpRect = Rect.newRect(x, y - theItem.height() + baselineOffset, 0, 0);
            tmpRect.width = theItem.width();
            tmpRect.height = theItem.height();
            theItem.drawInRect(g, tmpRect);
            Rect.returnRect(tmpRect);
            return theItem.width();
        }

        validateFontMetricsCache();
        if( _fontMetricsCache == null || charCount <= 0 ) {
            return 0;
        }

        /* setup */
        g.setFont(getFont());
        g.setColor(getColor());

        /* clip range */
        if (startingChar < 0) {
            startingChar = 0;
        }
        if (startingChar + charCount > _contents.length()) {
            charCount = _contents.length() - startingChar;
        }

        /* draw and compute total length */
        charArray = _contents.charArray();
        charWidths = _fontMetricsCache.widthsArray();
        endChar = startingChar + charCount;
        totalWidth = 0;
        while (startingChar < endChar) {
            nextTab = _contents.indexOf('\t', startingChar);
            if (nextTab == -1) {
                count = endChar - startingChar;
            } else {
                count = nextTab - startingChar;
            }

            /* draw everything up to the tab */
            if (count > 0) {
                g.drawChars(charArray, startingChar, count, x, y);
            }

            /* include the tabstop in the width calculation */
            if (nextTab != -1) {
                count++;
            }

            /* compute width of drawn text and tabstop */
            width = _widthOfSubstring(startingChar, count, x, tabStops);
            x += width;
            totalWidth += width;

            /* advance pointer */
            startingChar += count;
        }

        return totalWidth;
    }




/* archiving */


    /** Describes the TextStyleRun class' information.
     * @see Codable#describeClassInfo
     */
     public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.TextStyleRun", 1);
        info.addField(CONTENTS_KEY,STRING_TYPE);
        info.addField(ATTRIBUTES_KEY, OBJECT_TYPE);
    }

    /** Encodes the TextStyleRun instance.
     * @see Codable#encode
     */
     public void encode(Encoder encoder) throws CodingException {
        encoder.encodeString(CONTENTS_KEY,_contents.toString());
        encoder.encodeObject(ATTRIBUTES_KEY,_attributes);
    }

    /** Decodes a TextStyleRun instance.
     * @see Codable#decode
     */
     public void decode(Decoder decoder) throws CodingException {
        String          text;
        Object          image;

        _contents = new FastStringBuffer( decoder.decodeString(CONTENTS_KEY));
        _attributes = (Hashtable)decoder.decodeObject(ATTRIBUTES_KEY);
    }

    /** Finishes the TextStyleRun instance decoding.  This method does nothing.
     * @see Codable#finishDecoding
     */
    public  void finishDecoding() throws CodingException {
    }

     void setAttributes(Hashtable attributes) {
      if( attributes != null ) {
        invalidateFontMetricsCache();
        _attributes = (Hashtable)attributes.clone();
      } else
        _attributes = null;

      if( _attributes != null ) {
        if(_attributes.get(TextView.PARAGRAPH_FORMAT_KEY) != null)
          _attributes.remove( TextView.PARAGRAPH_FORMAT_KEY );
      }
    }

    void appendAttributes(Hashtable attributes) {
     Enumeration keys;
     String key;

     if( attributes == null )
       return;
     if(_attributes == null )
       _attributes = (Hashtable) _paragraph.owner().defaultAttributes().clone();

     keys = attributes.keys();
     while(keys.hasMoreElements()) {
       key = (String)keys.nextElement();
       if( key.equals(TextView.FONT_KEY))
           invalidateFontMetricsCache();
       _attributes.put( key, attributes.get(key));
     }
   }

   Hashtable attributes() {
       if( _attributes != null ) {
           _attributes.put(TextView.PARAGRAPH_FORMAT_KEY,_paragraph.currentParagraphFormat());
           return _attributes;
       }
       return null;
  }
}



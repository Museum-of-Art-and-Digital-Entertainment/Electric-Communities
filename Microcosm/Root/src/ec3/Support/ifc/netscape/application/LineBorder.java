// LineBorder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Border subclass that draws a 1 pixel wide colored line around the given
  * Rect.
  */
public class LineBorder extends Border {
    private static Border blackLine;
    private static Border grayLine;

    private Color color;

    /** Convenience method for getting the Color.black LineBorder.
      */
    public static Border blackLine() {
        if (blackLine == null) {
            blackLine = new LineBorder(Color.black);
        }

        return blackLine;
    }

    /** Convenience method for getting the Color.gray LineBorder.
      */
    public static Border grayLine() {
        if (grayLine == null) {
            grayLine = new LineBorder(Color.gray);
        }

        return grayLine;
    }

    /** Constructs a LineBorder.  You must set the color that it uses to draw.
      * @see #setColor
      */
    public LineBorder() {
        super();
    }

    /** Constructs a LineBorder that draws with the color <b>borderColor</b>.
      */
    public LineBorder(Color borderColor) {
        this();
        color = borderColor;
    }

    /** Sets the LineBorder's Color.
      */
    public void setColor(Color aColor) {
        color = aColor;
    }

    /** Returns the LinBorder's Color.
      */
    public Color color() {
        return color;
    }

    /** Returns the LineBorder's left margin (1 pixel).
      */
    public int leftMargin() {
        return 1;
    }

    /** Returns the LineBorder's right margin (1 pixel).
      */
    public int rightMargin() {
        return 1;
    }

    /** Returns the LineBorder's top margin (1 pixel). */
    public int topMargin() {
        return 1;
    }

    /** Returns the LineBorder's bottom margin (1 pixel).
      */
    public int bottomMargin() {
        return 1;
    }

    /** Draws the LineBorder within the given Rect.
      */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        g.setColor(color);
        g.drawRect(x, y, width, height);
    }

    static final String colorField = "color";
    private static Class lineBorderClass;

    private Class lineBorderClass() {
        if (lineBorderClass == null) {
            lineBorderClass = blackLine().getClass();
        }

        return lineBorderClass;
    }

    /** Describes the LineBorder class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.LineBorder", 1);
        info.addField(colorField, OBJECT_TYPE);
    }

    /** Encodes the LineBorder instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(colorField, color);
    }

    /** Decodes the LineBorder instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        color = (Color)decoder.decodeObject(colorField);

        if (getClass() != lineBorderClass())
            return;

        if (color == Color.black)
            decoder.replaceObject(blackLine());
        else if (color == Color.gray)
            decoder.replaceObject(grayLine());
    }

    /** Finishes the LineBorder's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
    }
}

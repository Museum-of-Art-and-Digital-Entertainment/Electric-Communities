// BezelBorder.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Border subclass capable of drawing five different kinds of 3D bezels:
  * raised, lowered, grooved, raised button, and lowered button. For the
  * raised, lowered, and grooved bezels, the 3D effect is created by
  * generating lighter and darker
  * versions of a given base color. The base color is typically the
  * background color on which the bezel appears (Color.lightGray). The button
  * bezel colors are hardcoded, and draw a more complicated bezel.
  * @note 1.0 default borders will be used during unarchiving as necessary
  */
public class BezelBorder extends Border {
    /** Raised bezel type. */
    public static final int RAISED  = 0;
    /** Lowered bezel type. */
    public static final int LOWERED = 1;
    /** Grooved bezel type. */
    public static final int GROOVED = 2;

    /** "Raised" button bezel type. */
    public static final int RAISED_BUTTON = 3;

    /** "Lowered" button bezel type. */
    public static final int LOWERED_BUTTON = 4;

    static final int RAISED_SCROLL_BUTTON = 5;
    static final int LOWERED_SCROLL_BUTTON = 6;

    private static Border raisedBezel;
    private static Border loweredBezel;
    private static Border groovedBezel;
    private static Border raisedButtonBezel;
    private static Border loweredButtonBezel;
    private static Border raisedScrollButtonBezel;
    private static Border loweredScrollButtonBezel;

    private int bezelType;
    private Color baseColor;
    private Color lighterColor;
    private Color darkerColor;

    /** Convenience method for creating a raised bezel on the standard
      * background color.
      */
    public static Border raisedBezel() {
        if (raisedBezel == null) {
            raisedBezel = new BezelBorder(RAISED, Color.lightGray, Color.white,
                Color.gray);
        }

        return raisedBezel;
    }

    /** Convenience method for creating a lowered bezel on the standard
      * background color.
      */
    public static Border loweredBezel() {
        if (loweredBezel == null) {
            loweredBezel = new BezelBorder(LOWERED, Color.lightGray,
                Color.white, Color.gray);
        }

        return loweredBezel;
    }

    /** Convenience method for creating a grooved bezel on the standard
      * background color.
      */
    public static Border groovedBezel() {
        if (groovedBezel == null) {
            groovedBezel = new BezelBorder(GROOVED, Color.lightGray,
                Color.white, Color.gray);
        }

        return groovedBezel;
    }

    /** A convenience for creating a raised bezel on the standard
      * Button.
      */
    public static Border raisedButtonBezel() {
        if (raisedButtonBezel == null) {
            raisedButtonBezel = new BezelBorder(RAISED_BUTTON, Color.lightGray,
                Color.white, Color.gray);
        }

        return raisedButtonBezel;
    }

    /** A convenience for creating a lowered bezel on the standard
      * Button.
      */
    public static Border loweredButtonBezel() {
        if (loweredButtonBezel == null) {
            loweredButtonBezel = new BezelBorder(LOWERED_BUTTON, Color.lightGray,
                Color.white, Color.gray);
        }

        return loweredButtonBezel;
    }

    /** A convenience for creating a raised bezel on the standard
      * Button which is used by ScrollBar.
      */
    static Border raisedScrollButtonBezel() {
        if (raisedScrollButtonBezel == null) {
            raisedScrollButtonBezel = new BezelBorder(RAISED_SCROLL_BUTTON);
        }

        return raisedScrollButtonBezel;
    }

    /** A convenience for creating a lowered bezel on the standard
      * Button which is used by ScrollBar.
      */
    static Border loweredScrollButtonBezel() {
        if (loweredScrollButtonBezel == null) {
            loweredScrollButtonBezel = new BezelBorder(LOWERED_SCROLL_BUTTON);
        }

        return loweredScrollButtonBezel;
    }

    /** Constructs a BezelBorder. This constructor is only useful
      * during decoding.
      */
    public BezelBorder() {
        super();
    }

    /** Constructs a BezelBorder of type <b>type</b>. Use this method when
      * creating Button borders, not the generic bezeled or grooved Border.
      */
    public BezelBorder(int type) {
        this(type, Color.lightGray, Color.white, Color.gray);
    }

    /** Constructs a BezelBorder of the given type and base color. Computes the
      * highlight colors (lighter and darker) by calling <b>lighterColor()</b>
      * and <b>darkerColor()</b> on <b>baseColor</b>.
      */
    public BezelBorder(int type, Color baseColor) {
        this();
        bezelType = type;
        this.baseColor = baseColor;
        this.lighterColor = baseColor.lighterColor();
        this.darkerColor = baseColor.darkerColor();
    }

    /** Constructs a BezelBorder of the given type, base color, and lighter
      * and darker colors (for creating highlights).
      */
    public BezelBorder(int type, Color baseColor, Color lighterColor,
        Color darkerColor) {

        this();
        bezelType = type;
        this.baseColor = baseColor;
        this.lighterColor = lighterColor;
        this.darkerColor = darkerColor;
    }

    /** Returns the BezelBorder's left margin of 2 pixels. */
    public int leftMargin() {
        if (bezelType == LOWERED_BUTTON) {
            return 3;
        }
        if (bezelType == RAISED_SCROLL_BUTTON ||
               bezelType == LOWERED_SCROLL_BUTTON) {
            return 1;
        }
        return 2;
    }

    /** Returns the BezelBorder's right margin of 2 pixels. */
    public int rightMargin() {
        if (bezelType == RAISED_BUTTON) {
            return 3;
        } else if (bezelType == RAISED_SCROLL_BUTTON ||
                   bezelType == LOWERED_SCROLL_BUTTON) {
            return 1;
        }
        return 2;
    }

    /** Returns the BezelBorder's top margin of 2 pixels. */
    public int topMargin() {
        if (bezelType == LOWERED_BUTTON) {
            return 3;
        }
        if (bezelType == RAISED_SCROLL_BUTTON ||
               bezelType == LOWERED_SCROLL_BUTTON) {
            return 1;
        }
        return 2;
    }

    /** Returns the BezelBorder's bottom margin of 2 pixels. */
    public int bottomMargin() {
        if (bezelType == RAISED_BUTTON) {
            return 3;
        } else if (bezelType == RAISED_SCROLL_BUTTON ||
                      bezelType == LOWERED_SCROLL_BUTTON) {
            return 1;
        }
        return 2;
    }

    /** Returns the Bezel's type. */
    public int type() {
        return bezelType;
    }

    /** Draws the BezelBorder in the given rectable.
      */
    public void drawInRect(Graphics g, int x, int y, int width, int height) {
        switch (bezelType) {
            case RAISED:
                drawBezel(g, x, y, width, height, baseColor, lighterColor,
                    darkerColor, Color.darkGray, true);
                break;
            case LOWERED:
                drawBezel(g, x, y, width, height, baseColor, lighterColor,
                    darkerColor, Color.darkGray, false);
                break;
            case GROOVED:
                drawGroovedBezel(g, x, y, width, height, lighterColor,
                    darkerColor);
                break;
            case RAISED_BUTTON:
                drawRaisedButtonBezel(g, x, y, width, height);
                break;
            case LOWERED_BUTTON:
                drawLoweredButtonBezel(g, x, y, width, height);
                break;
            case RAISED_SCROLL_BUTTON:
                drawRaisedScrollButtonBezel(g, x, y, width, height);
                break;
            case LOWERED_SCROLL_BUTTON:
                drawLoweredScrollButtonBezel(g, x, y, width, height);
                break;
            default:
                throw new InconsistencyException("Invalid bezelType: " +
                    bezelType);
        }
    }

    static final String bezelTypeField = "bezelType";
    static final String baseColorField = "baseColor";
    static final String lighterColorField = "lighterColor";
    static final String darkerColorField = "darkerColor";

    private static Class bezelBorderClass;

    private static Class bezelBorderClass() {
        if (bezelBorderClass == null) {
            bezelBorderClass = loweredBezel().getClass();
        }

        return bezelBorderClass;
    }

    /** Describes the BezelBorder class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.BezelBorder", 1);
        info.addField(bezelTypeField, INT_TYPE);
        info.addField(baseColorField, OBJECT_TYPE);
        info.addField(lighterColorField, OBJECT_TYPE);
        info.addField(darkerColorField, OBJECT_TYPE);
    }

    /** Encodes the BezelBorder instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeInt(bezelTypeField, bezelType);
        encoder.encodeObject(baseColorField, baseColor);
        encoder.encodeObject(lighterColorField, lighterColor);
        encoder.encodeObject(darkerColorField, darkerColor);
    }

    /** Decodes the BezelBorder instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        bezelType = decoder.decodeInt(bezelTypeField);
        baseColor = (Color)decoder.decodeObject(baseColorField);
        lighterColor = (Color)decoder.decodeObject(lighterColorField);
        darkerColor = (Color)decoder.decodeObject(darkerColorField);

        if (getClass() != bezelBorderClass())
            return;

        if (baseColor == Color.lightGray && lighterColor == Color.white &&
            darkerColor == Color.gray) {
            switch (bezelType) {
                case RAISED:
                    decoder.replaceObject(raisedBezel());
                    break;
                case LOWERED:
                    decoder.replaceObject(loweredBezel());
                    break;
                case GROOVED:
                    decoder.replaceObject(groovedBezel());
                    break;
                case RAISED_BUTTON:
                    decoder.replaceObject(raisedButtonBezel());
                    break;
                case LOWERED_BUTTON:
                    decoder.replaceObject(loweredButtonBezel());
                    break;
                case RAISED_SCROLL_BUTTON:
                    decoder.replaceObject(raisedScrollButtonBezel());
                    break;
                case LOWERED_SCROLL_BUTTON:
                    decoder.replaceObject(loweredScrollButtonBezel());
                    break;
            }
        }
    }

    /** Finishes the BezelBorder decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
    }

    /** Convenience method for drawing a bezel with the four specified colors.
      */
    public static void drawBezel(Graphics g, int x, int y, int width,
        int height, Color baseColor, Color lighterColor, Color darkerColor,
        Color blackColor, boolean raised) {

        Color outerNW, outerSE, innerNW, innerSE;

        if (width == 0 || height == 0)
            return;

        if (raised) {
            outerNW = baseColor;
            outerSE = blackColor;
            innerNW = lighterColor;
            innerSE = darkerColor;
        } else {
            outerNW = darkerColor;
            outerSE = lighterColor;
            innerNW = blackColor;
            innerSE = baseColor;
        }

        g.setColor(outerNW);
        g.fillRect(x, y, width - 1, 1);
        g.fillRect(x, y + 1, 1, height - 1);
        g.setColor(outerSE);
        g.fillRect(x, y + height - 1, width, 1);
        g.fillRect(x + width - 1, y, 1, height);

        x++;
        y++;
        width -= 2;
        height -= 2;

        g.setColor(innerNW);
        g.fillRect(x, y, width - 1, 1);
        g.fillRect(x, y + 1, 1, height - 1);
        g.setColor(innerSE);
        g.fillRect(x, y + height - 1, width, 1);
        g.fillRect(x + width - 1, y, 1, height);
    }

    /** Convenience method for drawing a grooved bezel with the two specified
      * colors.
      */
    public static void drawGroovedBezel(Graphics g, int x, int y, int width,
        int height, Color lighterColor, Color darkerColor) {

        if (width == 0 || height == 0)
            return;

        g.setColor(lighterColor);
        g.drawRect(x + 1, y + 1, width - 1, height - 1);
        g.drawPoint(x + width - 1, y);
        g.drawPoint(x, y + height - 1);

        g.setColor(darkerColor);
        g.drawRect(x, y, width - 1, height - 1);
    }

    static final Color gray218 = new Color(218, 218, 218);
    static final Color gray165 = new Color(165, 165, 165);
    static final Color gray143 = new Color(143, 143, 143);

    /** Convenience method for drawing a raised Button bezel.
      */
    public static void drawRaisedButtonBezel(Graphics g, int x, int y,
                                             int width, int height) {
        int maxX, maxY;

        maxX = x + width;
        maxY = y + height;

        g.setColor(Color.white);
        g.drawPoint(x + 1, y + 1);

        g.setColor(Color.gray231);
        g.drawLine(x, y, x, maxY - 4);
        g.drawLine(x + 1, y, maxX - 3, y);

        g.setColor(Color.lightGray);
        g.drawLine(x, maxY - 3, x, maxY - 2);
        g.drawLine(x + 1, y + 2, x + 1, maxY - 3);
        g.drawLine(x + 2, y + 1, maxX - 3, y + 1);
        g.drawLine(x + 2, maxY - 3, maxX - 4, maxY - 3);
        g.drawLine(maxX - 3, y + 2, maxX - 3, maxY - 4);
        g.drawPoint(maxX - 2, y);

        g.setColor(Color.gray153);
        g.drawLine(x + 1, maxY - 2, maxX - 3, maxY - 2);
        g.drawLine(maxX - 2, y + 1, maxX - 2, maxY - 3);
        g.drawPoint(maxX - 3, maxY - 3);
        g.drawPoint(maxX - 1, y);
        g.drawPoint(x, maxY - 1);

        g.setColor(Color.gray102);
        g.drawLine(x + 1, maxY - 1, maxX - 1, maxY - 1);
        g.drawLine(maxX - 1, y + 1, maxX - 1, maxY - 2);
        g.drawPoint(maxX - 2, maxY - 2);
    }

    /** Convenience method for drawing a lowered Button bezel.
      */
    public static void drawLoweredButtonBezel(Graphics g, int x, int y,
                                              int width, int height) {
        int maxX, maxY;

        maxX = x + width;
        maxY = y + height;

        g.setColor(Color.white);
        g.drawPoint(maxX - 2, maxY - 2);

        g.setColor(Color.gray231);
        g.drawLine(x + 2, maxY - 1, maxX - 1, maxY -1);
        g.drawLine(maxX - 1, y + 3, maxX - 1, maxY - 2);

        g.setColor(Color.lightGray);
        g.drawLine(x + 2, y + 3, x + 2, maxY - 3);
        g.drawLine(x + 3, y + 2, maxX - 2, y + 2);
        g.drawLine(maxX - 2, y + 3, maxX - 2, maxY - 3);
        g.drawLine(x + 2, maxY - 2, maxX - 3, maxY - 2);
        g.drawLine(maxX - 1, y + 1, maxX - 1, y + 2);
        g.drawPoint(x + 1, maxY - 1);

        g.setColor(Color.gray153);
        g.drawLine(x + 1, y + 2, x + 1, maxY - 2);
        g.drawLine(x + 2, y + 1, maxX - 2, y + 1);
        g.drawPoint(x, maxY - 1);
        g.drawPoint(maxX - 1, y);
        g.drawPoint(x + 2, y + 2);

        g.setColor(Color.gray102);
        g.drawLine(x, y, x, maxY - 2);
        g.drawLine(x + 1, y, maxX - 2, y);
        g.drawPoint(x + 1, y + 1);
    }

    /** Convenience method for drawing a raised ScrollBar Button bezel.
      */
    static void drawRaisedScrollButtonBezel(Graphics g, int x, int y,
                                            int width, int height) {
        int maxX, maxY;

        maxX = x + width;
        maxY = y + height;

        g.setColor(Color.white);
        g.drawLine(x, y + 1, x, maxY - 3);
        g.drawLine(x + 1, y, maxX - 3, y);

        g.setColor(Color.gray231);
        g.drawPoint(x, y);
        g.drawPoint(x, maxY - 2);
        g.drawPoint(maxX - 2, y);

        g.setColor(Color.gray153);
        g.drawPoint(maxX - 1, y);
        g.drawPoint(maxX - 1, maxY - 1);
        g.drawPoint(x, maxY - 1);

        g.setColor(Color.gray102);
        g.drawLine(maxX - 1, y + 1, maxX - 1, maxY - 2);
        g.drawLine(x + 1, maxY - 1, maxX - 2, maxY - 1);
    }

    /** Convenience method for drawing a lowered ScrollBar Button bezel.
      */
    static void drawLoweredScrollButtonBezel(Graphics g, int x, int y,
                                             int width, int height) {
        int maxX, maxY;

        maxX = x + width;
        maxY = y + height;

        g.setColor(Color.gray153);
        g.drawLine(x, maxY - 1, maxX - 1, maxY - 1);
        g.drawLine(maxX - 1, y, maxX - 1, maxY - 2);

        g.setColor(Color.lightGray);
        g.drawLine(x, y, maxX - 2, y);
        g.drawLine(x, y + 1, x, maxY - 2);
    }
}

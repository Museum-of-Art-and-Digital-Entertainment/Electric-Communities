// Color.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Object subclass representing a color.
  */
public class Color implements Codable {
    java.awt.Color      _color = java.awt.Color.white;

    /** The color red. */
    public final static Color   red = new Color(255, 0, 0);
    /** The color green. */
    public final static Color   green = new Color(0, 255, 0);
    /** The color blue. */
    public final static Color   blue = new Color(0, 0, 255);
    /** The color cyan. */
    public final static Color   cyan = new Color(0, 255, 255);
    /** The color magenta. */
    public final static Color   magenta = new Color(255, 0, 255);
    /** The color yellow. */
    public final static Color   yellow = new Color(255, 255, 0);
    /** The color orange. */
    public final static Color   orange = new Color(255, 200, 0);
    /** The color pink. */
    public final static Color   pink = new Color(255, 175, 175);

    /** The color white. */
    public final static Color   white = new Color(255, 255, 255);
    /** The color light gray (75% white). */
    public final static Color   lightGray = new Color(192, 192, 192);
    /** The color gray (50% white). */
    public final static Color   gray = new Color(128, 128, 128);
    /** The color dark gray (25% white). */
    public final static Color   darkGray = new Color(64, 64, 64);
    /** The color black. */
    public final static Color   black = new Color(0, 0, 0);

    /** Commonly available undithered gray. */
    final static Color   gray192 = lightGray;
    /** Commonly available undithered gray. */
    final static Color   gray128 = gray;

    /** AppletViewer undithered gray. */
    final static Color   gray231 = new Color(231, 231, 231);
    /** AppletViewer undithered gray. */
    final static Color   gray204 = new Color(204, 204, 204);
    /** AppletViewer undithered gray. */
    final static Color   gray153 = new Color(153, 153, 153);
    /** AppletViewer undithered gray. */
    final static Color   gray102 = new Color(102, 102, 102);
    /** AppletViewer undithered gray. */
    final static Color   gray51 = new Color(51, 51, 51);

    /** Win95 Navigator almost-gray. */
    final static Color   gray160 = new Color(160, 160, 164);
    /** Win95 Navigator almost-gray. */
    final static Color   gray255 = new Color(255, 251, 240);


    final static String         R_KEY = "r";
    final static String         G_KEY = "g";
    final static String         B_KEY = "b";

    private static Class colorClass;

    private static Class colorClass() {
        if (colorClass == null) {
            colorClass = Color.black.getClass();
        }

        return colorClass;
    }

    /** Drag and Drop data type.
      * @see DragSession
      */
    public final static String COLOR_TYPE = "netscape.application.Color";



/* class methods */

    /** Returns the RGB value for the given hue, saturation, and brightness
      * values.
      */
    public static int rgbForHSB(float hue, float saturation,
                                float brightness) {
        return java.awt.Color.HSBtoRGB(hue, saturation, brightness);
    }

    /** Returns a Color for the given hue, saturation, and brightness
      * values.
      */
    public static Color colorForHSB(float hue, float saturation,
                                    float brightness) {
        return new Color(rgbForHSB(hue, saturation, brightness));
    }


/* constructors */

    /** Constructs a Color.  This constructor is only useful during decoding.
      * @see Color(int, int, int)
      * @see Color(int)
      * @see Color(float, float, float)
      */
    public Color() {
        super();
    }

    /** Constructs a Color for the given red, green, and blue values.  These
      * values must be in the range 0-255.
      */
    public Color(int red, int green, int blue) {
        this();

        _color = new java.awt.Color(red, green, blue);
    }

    /** Constructs a Color for the given red, green, and blue values, where red
      * is specified by rgb bits 16-23, green by bits 8-15, and blue by bits
      * 0-7.
      */
    public Color(int rgb) {
        this();

        _color = new java.awt.Color(rgb);
    }

    /** Constructs a Color for the given red, green, and blue values.  These
      * values must be in the range 0.0-1.0.
      */
    public Color(float red, float green, float blue) {
        this();

        if (red < 0.0) red = 0.0f;
        if (red > 1.0) red = 1.0f;
        if (green < 0.0) green = 0.0f;
        if (green > 1.0) green = 1.0f;
        if (blue < 0.0) blue = 0.0f;
        if (blue > 1.0) blue = 1.0f;

        _color = new java.awt.Color(red, green, blue);
    }

    Color(java.awt.Color awtColor) {
        this();

        _color = awtColor;
    }



/* actions, attributes */

    /** Returns the Color's red component, in the range 0-255.
      */
    public int red() {
        return _color.getRed();
    }

    /** Returns the Color's green component, in the range 0-255. */
    public int green() {
        return _color.getGreen();
    }

    /** Returns the Color's blue component, in the range 0-255.
      */
    public int blue() {
        return _color.getBlue();
    }

    /** Returns the Color's rgb value, where bits 16-23 represent red,
      * bits 8-15 represent green, and bits 0-7 represent blue.
      */
    public int rgb() {
        return _color.getRGB();
    }

    /** Returns the Color's hash code.
      */
    public int hashCode() {
        return _color.hashCode();
    }

    /** Returns <b>true</b> if <b>anObject</b> equals the Color.
      */
    public boolean equals(Object anObject) {
        Color           theColor;

        if (anObject instanceof Color) {
            theColor = (Color)anObject;
            return _color.equals(theColor.awtColor());
        } else if (anObject instanceof java.awt.Color) {
            return _color.equals((java.awt.Color)anObject);
        }

        return false;
    }

    /** Returns the Color's String representation.
      */
    public String toString() {
        return getClass().getName() + " (" + red() + ", " + green() +
               ", " + blue() +")";
    }

    java.awt.Color awtColor() {
        return _color;
    }

    /** Returns a "lighter" version of this Color.
      * @see #darkerColor
      */
    public Color lighterColor() {
        int r, g, b;

        // This case is really common.

        if (this == Color.lightGray) {
            return Color.white;
        }

        r = (256 * red()) / 192;
        if (r > 255)
            r = 255;

        g = (256 * green()) / 192;
        if (g > 255)
            g = 255;

        b = (256 * blue()) / 192;
        if (b > 255)
            b = 255;

        return new Color(r, g, b);
    }

    /** Returns a "darker" version of this Color.
      * @see #lighterColor
      */
    public Color darkerColor() {
        int r, g, b;

        // This case is really common.

        if (this == Color.lightGray) {
            return Color.gray;
        }

       r = (128 * red()) / 192;
        g = (128 * green()) / 192;
        b = (128 * blue()) / 192;

        return new Color(r, g, b);
    }



/* archiving */


    /** Describes the Color class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Color", 1);
        info.addField(R_KEY, BYTE_TYPE);
        info.addField(G_KEY, BYTE_TYPE);
        info.addField(B_KEY, BYTE_TYPE);
    }

    /** Encodes the Color.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeByte(R_KEY, (byte)red());
        encoder.encodeByte(G_KEY, (byte)green());
        encoder.encodeByte(B_KEY, (byte)blue());
    }

    /** Decodes the Color.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int r, g, b;

        // This is a really annoying hack because there are no unsigned bytes
        // in java.

        r = decoder.decodeByte(R_KEY) & 0xff;
        g = decoder.decodeByte(G_KEY) & 0xff;
        b = decoder.decodeByte(B_KEY) & 0xff;

        // If this is a subclass of color, do no replacement.

        if (getClass() == colorClass()) {
            // Check to see if the color is one of the standard ones.  If so,
            // just use the shared instance.

            if (r == 0) {
                if (g == 0) {
                    if (b == 0) {
                        decoder.replaceObject(Color.black);
                        return;
                    } else if (b == 255) {
                        decoder.replaceObject(Color.blue);
                        return;
                    }
                } else if (g == 255) {
                    if (b == 0) {
                        decoder.replaceObject(Color.green);
                        return;
                    } else if (b == 255) {
                        decoder.replaceObject(Color.cyan);
                        return;
                    }
                }
            } else if (r == 255) {
                if (g == 255) {
                    if (b == 255) {
                        decoder.replaceObject(Color.white);
                        return;
                    } else if (b == 0) {
                        decoder.replaceObject(Color.yellow);
                        return;
                    }
                } else if (g == 0) {
                    if (b == 0) {
                        decoder.replaceObject(Color.red);
                        return;
                    } else if (b == 255) {
                        decoder.replaceObject(Color.magenta);
                        return;
                    }
                }
            } else if (r == 192) {
                if (g == 192 && b == 192) {
                    decoder.replaceObject(Color.lightGray);
                    return;
                }
            } else if (r == 128) {
                if (g == 128 && b == 128) {
                    decoder.replaceObject(Color.gray);
                    return;
                }
            } else if (r == 64) {
                if (g == 64 && b == 64) {
                    decoder.replaceObject(Color.darkGray);
                    return;
                }
            }
        }

        _color = new java.awt.Color(r, g, b);
    }

    /** Finishes the Color's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}

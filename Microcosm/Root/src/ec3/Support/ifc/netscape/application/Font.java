// Font.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.net.URL;
import java.awt.image.FilteredImageSource;
import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;

import netscape.util.*;

/** Object subclass representing a Font.
  */


public class Font implements Codable {
    FontMetrics                 _metrics;
    String                      _name;
    URL                         _url;
    Bitmap                      _glyphsImage;
    Vector                      _glyphVector, _widthsVector;
    Hashtable                   _description;
    java.awt.Font               _awtFont;
    int                         _type, _widthsArrayBase, _widthsArray[];

    final static int            INVALID = 0, AWT = 1, DOWNLOADED = 2;

    /** Plain font style. */
    public final static int     PLAIN = java.awt.Font.PLAIN;
    /** Bold font style. */
    public final static int     BOLD = java.awt.Font.BOLD;
    /** Italic font style. */
    public final static int     ITALIC = java.awt.Font.ITALIC;

    final static String         FAMILY = "Family", STYLE = "Style",
                                SIZE = "Size", WIDTHS = "Widths";
    final static String         DESCRIPTION = "Description",
                                GLYPHS = "glyphs.gif";

    final static String         NAME_KEY = "name";
    final static String         STYLE_KEY = "style";
    final static String         SIZE_KEY = "size";
    private static Class fontClass;

    private static Class fontClass() {
        if (fontClass == null) {
            fontClass = new Font().getClass();
        }

        return fontClass;
    }

    /** Constructs an empty Font. This constructor is only useful when
      * decoding.
      */
    public Font() {
        super();
        _type = INVALID;
    }

    /** Constructs a Font with the specified name, style and size.
      */
    public Font(String name, int style, int size) {
        this();

        _awtFont = new java.awt.Font(name, style, size);
        _name = name;
        if (_awtFont != null) {
            _type = AWT;
        }
    }

    /** Returns the "default" font, currently defined to be Helvetica 12.
      */
    public static Font defaultFont() {
        return fontNamed("Helvetica", Font.PLAIN, 12);
    }

    /** Retrieves the Font for the specified font name, style, and size, and
      * places it in the Application's font cache, allowing future requests
      * for the font to be handled efficiently.
      */
    public static synchronized Font fontNamed(String fontName, int style,
                                              int size) {
        Font font;
        String uniqueName;
        Application app;

        if (fontName == null || size == 0) {
            return null;
        }

        uniqueName = fontName + "." + style + "." + size;

        app = Application.application();
        font = (Font)app.fontByName.get(uniqueName);

        if (font != null)
            return font;

        font = new Font(fontName, style, size);
        if (!font.isValid())
            return null;

        app.fontByName.put(uniqueName, font);

        return font;
    }

    /** Returns the Font identified by the String <b>aString</b>. Font names
      * normally take the form "FontName:style:size". This is a convience for
      * fontNamed() as the string is parsed apart and
      * then fontNamed(name, style, size) is called.
      */
    public static Font fontNamed(String aString) {
        String[]        fontDef;
        String          fontName, fontStyleString;
        char            firstChar;
        int             fontSize, fontStyle;

        fontDef = stringsForString(aString);
        if (fontDef == null || fontDef.length == 0) {
            return null;
        }

        fontName = fontDef[0];
        if (fontName.equals("Default")) {
            return defaultFont();
        } else if (fontName.length() == 0) {
            return null;
        }

        /* if just a name, it's a downloadable font */
        if (fontDef.length == 1) {
            URL url;
            Application app = Application.application();

            url = app._appResources.urlForFontNamed(fontName);
            return getFontFromURL(url, fontName);
        }

        fontStyleString = fontDef[1];
        firstChar = fontStyleString.charAt(0);
        if (firstChar == 'P' || firstChar == 'p') {
            fontStyle = Font.PLAIN;
        } else if (firstChar == 'B' || firstChar == 'b') {
            fontStyle = Font.BOLD;
        } else if (firstChar == 'I' || firstChar == 'i') {
            fontStyle = Font.ITALIC;
        } else {
            try {
                fontStyle = Integer.parseInt(fontStyleString);
            } catch (NumberFormatException e) {
                fontStyle = Font.PLAIN;
            }
        }

        fontSize = parseInt(fontDef[2]);
        if (fontSize < 0) {
            fontSize = 0;
        }

        return fontNamed(fontName, fontStyle, fontSize);
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String[] stringsForString(String aString) {
        Vector          stringVector;
        String[]        strings;
        String          string0, string1, string2;
        StringBuffer    tmpBuffer;
        int             i, count;

        if (aString == null) {
            return null;
        }

        if (aString.indexOf(':') == -1) {
            strings = new String[1];
            strings[0] = aString;
            return strings;
        }

        count = aString.length();
        tmpBuffer = new StringBuffer();
        stringVector = new Vector();
        for (i = 0; i < count; i++) {
            if (aString.charAt(i) == ':') {
                stringVector.addElement(tmpBuffer.toString());
                tmpBuffer = new StringBuffer();
            } else {
                tmpBuffer.append(aString.charAt(i));
            }
        }
        stringVector.addElement(tmpBuffer.toString());

        count = stringVector.count();
        strings = new String[count];
        for (i = 0; i < count; i++) {
            strings[i] = (String)stringVector.elementAt(i);
        }
        return strings;
    }

    // This is used in decoding.  Take a look at this again to see if it
    // causes problems by having multiple fonts with the same name.  ALERT!

    synchronized void nameFont(String fontName, Font font) {
        Application.application().fontByName.put(fontName, font);
    }

    static synchronized Font getFontFromURL(URL absoluteURL, String fontName) {
        Font font;
        Application app = Application.application();

        if (absoluteURL == null) {
            return null;
        }

        if (fontName != null) {
            font = (Font)app.fontByName.get(fontName);
            if (font != null)
                return font;
        }

        font = new Font(absoluteURL, fontName);
        if (!font.isValid())
            return null;

        app.fontByName.put(fontName, font);

        return font;
    }

    /** Convenience constructor for instantiating a downloadable Font with
      * the specified name and URL locating the Font's resources on the web
      * server.
      */
    Font(java.net.URL fontURL, String name) {
        this();

        URL                     configURL;
        java.io.InputStream     inputStream;
        Deserializer            deserializer;

        _url = fontURL;
        _name = name;
        _type = DOWNLOADED;

        _glyphVector = new Vector();
        _widthsVector = new Vector();

      /* description file */
        try {
            configURL = new URL(fontURL, _name + ".font/" + DESCRIPTION);
        } catch (Exception e) {
            System.err.println(
                "Font.init() - Trouble creating font description URL " +
                        fontURL + _name + ".font/" + DESCRIPTION + " : " + e);
            _type = INVALID;
            return;
        }

        try {
            inputStream = configURL.openStream();
            deserializer = new Deserializer(inputStream);
            _description = (Hashtable)deserializer.readObject();

            _loadWidths();
        } catch (Exception e) {
            System.err.println(
                "Font.init() - Trouble retrieving font description URL " +
                                                    configURL);
            e.printStackTrace(System.err);
            _type = INVALID;
            return;
        }

      /* glyphs image */
        _loadGlyphs(fontURL);
    }

    void _loadWidths() {
        Object    widthsInfo[];
        int       i, count, index;

        if (_description == null) {
            return;
        }

        widthsInfo = (Object[])_description.get(WIDTHS);

        if (widthsInfo == null) {
            System.err.println(
                "Font._loadWidths() - No widths information for " + _name);
            return;
        }

        _widthsArrayBase = parseInt((String)widthsInfo[0]);

        count = widthsInfo.length - 1;
        _widthsArray = new int[_widthsArrayBase + count];
        for (i = 0; i < _widthsArrayBase; i++) {
            _widthsArray[i] = 0;
        }
        for (i = 1, index = _widthsArrayBase; i < count; i++, index++) {
            _widthsArray[index] = parseInt((String)widthsInfo[i]);
        }

        if (_widthsArray[' '] == 0) {
            _widthsArray[' '] = 5;
        }
    }

    java.awt.Image croppedImage(int x, int y, int width, int height) {
        java.awt.Image  croppedImage;

        croppedImage = AWTCompatibility.awtApplet().createImage(
            new FilteredImageSource(_glyphsImage.awtImage.getSource(),
            new CropImageFilter(x, y, width, height)));

        return croppedImage;
    }

    void _loadGlyphs(java.net.URL fontURL) {
        java.net.URL                            glyphURL;
        Image                                glyphsImage, newImage;
        java.awt.image.ImageFilter              imageChopper;
        FilteredImageSource                     filteredImageSource;
        java.awt.Image                          awtImage;
        int                                     i, x, c;

      /* description file */
        try {
            glyphURL = new URL(fontURL, _name + ".font/" + GLYPHS);
        } catch (Exception e) {
            System.err.println(
                "Font.init() - Trouble creating font glyph URL " +
                        fontURL + _name + ".font/" + GLYPHS + " : " + e);
            _type = INVALID;
            return;
        }

        _glyphsImage = Bitmap.bitmapFromURL(glyphURL);
        _glyphsImage.loadData();

        if (_glyphsImage == null || !_glyphsImage.isValid()) {
            System.err.println(
                "Font._loadGlyphs() - Trouble loading glyphs for " + _name);
            return;
        }

        _glyphsImage.loadData();

        for (i = _widthsArrayBase, x = 0; i < _widthsArray.length; i++) {
            awtImage = croppedImage(x, 0, _widthsArray[i],
                                     _glyphsImage.height());
            _glyphVector.addElement(
                                AWTCompatibility.bitmapForAWTImage(awtImage));
            newImage = (Image)_glyphVector.lastElement();

            x += _widthsArray[i];
        }
    }

    /** Returns <b>true</b> if the Font is valid.  A Font will not
      * be valid if there were problems creating its java.awt.Font (for local
      * Fonts), or there was a problem downloading the Font's resources
      * (for downloadable Fonts).
      */
    boolean isValid() {
        return (_type != INVALID);
    }

    /** Returns <b>true</b> if the Font was downloaded from the Applet's
      * web server.
      */
    boolean wasDownloaded() {
        return (_type == DOWNLOADED);
    }

    /** Returns the Font's font metrics. */
    public FontMetrics fontMetrics() {
        if (_metrics == null) {
            _metrics = new FontMetrics(this);
        }

        return _metrics;
    }

    /** Returns the Font's family. */
    public String family() {
        if (_type == INVALID) {
            return "";
        }

        if (_awtFont != null) {
            return _awtFont.getFamily();
        } else {
            return (String)_description.get(FAMILY);
        }
    }

    /** Returns the Font's name.  For local fonts this is the font name,
      * such as "Helvetica".
      */
    public String name() {
        if (_type == INVALID) {
            return "";
        }

        return _name;
    }

    /** Returns the integer representation of the Font's style. */
    public int style() {
        if (_type == INVALID) {
            return -1;
        }

        if (_awtFont != null) {
            return _awtFont.getStyle();
        } else {
            return parseInt((String)_description.get(STYLE));
        }
    }

    /** Returns the Font's point size. */
    public int size() {
        if (_type == INVALID) {
            return -1;
        }

        if (_awtFont != null) {
            return _awtFont.getSize();
        } else {
            return parseInt((String)_description.get(SIZE));
        }
    }

    /** Returns <b>true</b> if the Font's style is "plain." */
    public boolean isPlain() {
        return ((style() == PLAIN) ? true : false);
    }

    /** Returns <b>true</b> if the Font's style is "bold." */
    public boolean isBold() {
        return (((style() & BOLD) > 0) ? true : false );
    }

    /** Returns <b>true</b> if the Font's style is "italic."
      */
    public boolean isItalic() {
        return (((style() & ITALIC) > 0) ? true : false );
    }

    /** Returns the Font's "glyph vector," its collection of images that
      * comprise the font.  This method is valid only with a downloaded font.
      */
    Vector glyphVector() {
        return _glyphVector;
    }

    String _stringValueFromDescription(String keyName) {
        if (keyName == null || _description == null) {
            return "";
        }

        return (String)_description.get(keyName);
    }

    int _intValueFromDescription(String keyName) {
        if (keyName == null || _description == null) {
            return 0;
        }

        return parseInt((String)_description.get(keyName));
    }

    /** Returns the Font's string representation. The string takes the form
      * "Fontname.style.size" and is suitable for use in Font.fontNamed().
      */
    public String toString() {
        String          styleString;

        if (_type == INVALID || wasDownloaded()) {
            return _name;
        }

        if (isBold()) {
            if (isItalic()) {
                styleString = "BoldItalic";
            } else {
                styleString = "Bold";
            }
        } else if (isItalic()) {
            styleString = "Italic";
        } else {
            styleString = "Plain";
        }

        return family() + ":" + styleString + ":" + size();
    }

    /** Describes the Font class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Font", 1);
        info.addField(NAME_KEY, STRING_TYPE);
        info.addField(STYLE_KEY, INT_TYPE);
        info.addField(SIZE_KEY, INT_TYPE);
    }

    /** Encodes the Font instance by name.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeString(NAME_KEY, name());
        encoder.encodeInt(STYLE_KEY, style());
        encoder.encodeInt(SIZE_KEY, size());
    }

    /** Decodes the Font instance by name.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        Font    font;
        String  name;
        int style, size;

        name = decoder.decodeString(NAME_KEY);
        style = decoder.decodeInt(STYLE_KEY);
        size = decoder.decodeInt(SIZE_KEY);

        font = fontNamed(name, style, size);

        // Only replace the object if we are not a subclass of Font.

        if (getClass() == fontClass()) {
            decoder.replaceObject(font);
        } else {
            _name = font._name;
            _type = font._type;
            _awtFont = font._awtFont;
            nameFont(_name, this);
        }
    }

    /** Finishes the Font's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}

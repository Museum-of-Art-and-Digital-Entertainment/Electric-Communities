/**
 * StoneLook.java
 *
 * This is the bottleneck collection of colors, images, layout constants, etc.
 * used by our custom interface. It's called "stonelook" for historical
 * reasons (it originally looked like sandstone & marble, but no longer does).
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 */
package ec.ifc.stonelook;

import ec.e.run.Trace;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.StringTokenizer;

import netscape.application.Application;
import netscape.application.Bitmap;
import netscape.application.Color;
import netscape.application.Font;

import netscape.util.Hashtable;
import netscape.util.Vector;

import ec.cert.CryptoHash;
import ec.e.rep.steward.SuperRepository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.run.Trace;
import ec.e.start.crew.CrewCapabilities;

import ec.ifc.app.ECApplication;
import ec.ifc.app.ECBitmap;

import ec.util.NestedException;

/**
 * Collection of constants, accessors, and utilities used to implement the
 * stone-texture look.
 */
public class StoneLook {

    //
    // keys for attributes hashtable
    //

    // integers (please maintain alphabetical order)
    /** key with integer as value (set with setIntForAttribute) */
    public final static String BEZEL_THICKNESS
        = "BEZEL_THICKNESS";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String EXTRA_THIN_MARGIN
        = "EXTRA_THIN_MARGIN";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String MAX_ICON_SIZE
        = "MAX_ICON_SIZE";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_BUTTON_HEIGHT
        = "STANDARD_BUTTON_HEIGHT";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_BUTTON_MARGIN
        = "STANDARD_BUTTON_MARGIN";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_BUTTON_TITLE_PADDING
        = "STANDARD_BUTTON_TITLE_PADDING";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_MARGIN
        = "STANDARD_MARGIN";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_TAB_THICKNESS
        = "STANDARD_TAB_THICKNESS";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String STANDARD_TEXT_HEIGHT
        = "STANDARD_TEXT_HEIGHT";
    /** key with integer as value (set with setIntForAttribute) */
    public final static String THIN_MARGIN
        = "THIN_MARGIN";

    // images (please maintain alphabetical order)
    /** key with file name as value (set with setImageForAttribute) */
    public final static String CHECKBOX_ON
        = "CHECKBOX_ON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String CHECKBOX_ON_DISABLED
        = "CHECKBOX_ON_DISABLED";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String CHECKBOX_OFF
        = "CHECKBOX_OFF";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String CHECKBOX_OFF_DISABLED
        = "CHECKBOX_OFF_DISABLED";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String DISABLED_POPUP_ARROW
        = "DISABLED_POPUP_ARROW";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String DISABLED_SCROLL_DOWN_BUTTON
        = "DISABLED_SCROLL_DOWN_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String DISABLED_SCROLL_LEFT_BUTTON
        = "DISABLED_SCROLL_LEFT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String DISABLED_SCROLL_RIGHT_BUTTON
        = "DISABLED_SCROLL_RIGHT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String DISABLED_SCROLL_UP_BUTTON
        = "DISABLED_SCROLL_UP_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String HORIZ_SCROLL_KNOB_IMAGE
        = "HORIZ_SCROLL_KNOB_IMAGE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String HORIZ_SCROLL_TRACK_TEXTURE
        = "HORIZ_SCROLL_TRACK_TEXTURE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String PAGE_CORNER_FIRST
        = "PAGE_CORNER_FIRST";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String PAGE_CORNER_LAST
        = "PAGE_CORNER_LAST";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String PAGE_CORNER_MIDDLE
        = "PAGE_CORNER_MIDDLE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String RADIO_BUTTON_ON
        = "RADIO_BUTTON_ON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String RADIO_BUTTON_ON_DISABLED
        = "RADIO_BUTTON_ON_DISABLED";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String RADIO_BUTTON_OFF
        = "RADIO_BUTTON_OFF";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String RADIO_BUTTON_OFF_DISABLED
        = "RADIO_BUTTON_OFF_DISABLED";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String SMALL_HORIZ_SCROLL_KNOB_IMAGE
        = "SMALL_HORIZ_SCROLL_KNOB_IMAGE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String SMALL_VERT_SCROLL_KNOB_IMAGE
        = "SMALL_VERT_SCROLL_KNOB_IMAGE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_POPUP_ARROW
        = "STANDARD_COLOR_POPUP_ARROW";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_DOWN_BUTTON
        = "STANDARD_COLOR_SCROLL_DOWN_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_DOWN_PRESSED_BUTTON
        = "STANDARD_COLOR_SCROLL_DOWN_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_LEFT_BUTTON
        = "STANDARD_COLOR_SCROLL_LEFT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_LEFT_PRESSED_BUTTON
        = "STANDARD_COLOR_SCROLL_LEFT_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_RIGHT_BUTTON
        = "STANDARD_COLOR_SCROLL_RIGHT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_RIGHT_PRESSED_BUTTON
        = "STANDARD_COLOR_SCROLL_RIGHT_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_UP_BUTTON
        = "STANDARD_COLOR_SCROLL_UP_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String STANDARD_COLOR_SCROLL_UP_PRESSED_BUTTON
        = "STANDARD_COLOR_SCROLL_UP_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_DOWN_BUTTON
        = "TAB_DOWN_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_DOWN_OTHER_BUTTON
        = "TAB_DOWN_OTHER_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_DOWN_OTHER_PRESSED_BUTTON
        = "TAB_DOWN_OTHER_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_DOWN_PRESSED_BUTTON
        = "TAB_DOWN_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_LEFT_BUTTON
        = "TAB_LEFT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_LEFT_OTHER_BUTTON
        = "TAB_LEFT_OTHER_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_LEFT_OTHER_PRESSED_BUTTON
        = "TAB_LEFT_OTHER_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_LEFT_PRESSED_BUTTON
        = "TAB_LEFT_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_RIGHT_BUTTON
        = "TAB_RIGHT_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_RIGHT_OTHER_BUTTON
        = "TAB_RIGHT_OTHER_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_RIGHT_OTHER_PRESSED_BUTTON
        = "TAB_RIGHT_OTHER_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_RIGHT_PRESSED_BUTTON
        = "TAB_RIGHT_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_UP_BUTTON
        = "TAB_UP_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_UP_OTHER_BUTTON
        = "TAB_UP_OTHER_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_UP_OTHER_PRESSED_BUTTON
        = "TAB_UP_OTHER_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String TAB_UP_PRESSED_BUTTON
        = "TAB_UP_PRESSED_BUTTON";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String VERT_SCROLL_KNOB_IMAGE
        = "VERT_SCROLL_KNOB_IMAGE";
    /** key with file name as value (set with setImageForAttribute) */
    public final static String VERT_SCROLL_TRACK_TEXTURE
        = "VERT_SCROLL_TRACK_TEXTURE";

    // colors (please maintain alphabetical order)
    /** key with Color as value (set with setColorForAttribute) */
    public final static String ACTIVE_BUTTON_TITLE_COLOR
        = "ACTIVE_BUTTON_TITLE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String BORDERLESS_BUTTON_TITLE_COLOR
        = "BORDERLESS_BUTTON_TITLE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DARK_BACKGROUND_COLOR
        = "DARK_BACKGROUND_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DARK_STATUS_TEXT_COLOR
        = "DARK_STATUS_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DATA_BACKGROUND_COLOR
        = "DATA_BACKGROUND_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DEFAULT_BUTTON_INDICATOR_COLOR
        = "DEFAULT_BUTTON_INDICATOR_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_BUTTON_COLOR
        = "DISABLED_BUTTON_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_BORDER_COLOR
        = "DISABLED_BORDER_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_BUTTON_TITLE_COLOR
        = "DISABLED_BUTTON_TITLE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_POPUP_COLOR
        = "DISABLED_POPUP_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_SCROLL_BAR_FILL_COLOR
        = "DISABLED_SCROLL_BAR_FILL_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_EDITABLE_TEXT_COLOR
        = "DISABLED_EDITABLE_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_EDITABLE_TEXT_FILL_COLOR
        = "DISABLED_EDITABLE_TEXT_FILL_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String DISABLED_STATIC_TEXT_COLOR
        = "DISABLED_STATIC_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String EDITABLE_BORDER_COLOR
        = "EDITABLE_BORDER_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String EDITABLE_TEXT_COLOR
        = "EDITABLE_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String EDITABLE_TEXT_FILL_COLOR
        = "EDITABLE_TEXT_FILL_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String EDITABLE_TEXT_SELECTION_COLOR
        = "EDITABLE_TEXT_SELECTION_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String EMPHASIZED_TEXT_COLOR
        = "EMPHASIZED_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LIGHT_BACKGROUND_COLOR
        = "LIGHT_BACKGROUND_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LIGHT_STATUS_TEXT_COLOR
        = "LIGHT_STATUS_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LIGHTEST_BACKGROUND_COLOR
        = "LIGHTEST_BACKGROUND_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LIST_ITEM_SELECTED_COLOR
        = "LIST_ITEM_SELECTED_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LIST_ITEM_SELECTED_TITLE_COLOR
        = "LIST_ITEM_SELECTED_TITLE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String LOWERED_BUTTON_COLOR
        = "LOWERED_BUTTON_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String POPUP_COLOR
        = "POPUP_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String RAISED_BUTTON_COLOR
        = "RAISED_BUTTON_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SCROLL_BAR_BORDER_HIGHLIGHT_COLOR
        = "SCROLL_BAR_BORDER_HIGHLIGHT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SCROLL_BAR_BORDER_INSIDE_COLOR
        = "SCROLL_BAR_BORDER_INSIDE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SCROLL_BAR_BORDER_LINE_COLOR
        = "SCROLL_BAR_BORDER_LINE_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SCROLL_KNOB_COLOR
        = "SCROLL_KNOB_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_COLOR_HORIZ
        = "SELECTED_TAB_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_COLOR_VERT
        = "SELECTED_TAB_COLOR_VERT";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_EDGE_COLOR_HORIZ
        = "SELECTED_TAB_EDGE_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_EDGE_COLOR_VERT
        = "SELECTED_TAB_EDGE_COLOR_VERT";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_TITLE_COLOR_HORIZ
        = "SELECTED_TAB_TITLE_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TAB_TITLE_COLOR_VERT
        = "SELECTED_TAB_TITLE_COLOR_VERT";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String SELECTED_TEXT_COLOR
        = "SELECTED_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String STANDARD_BORDER_COLOR
        = "STANDARD_BORDER_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String STANDARD_BORDER_HIGHLIGHT_COLOR
        = "STANDARD_BORDER_HIGHLIGHT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String STATIC_TEXT_COLOR
        = "STATIC_TEXT_COLOR";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_EDGE_COLOR_HORIZ
        = "TAB_EDGE_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_EDGE_COLOR_VERT
        = "TAB_EDGE_COLOR_VERT";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_FILL_COLOR_HORIZ
        = "TAB_FILL_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_FILL_COLOR_VERT
        = "TAB_FILL_COLOR_VERT";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_TITLE_COLOR_HORIZ
        = "TAB_TITLE_COLOR_HORIZ";
    /** key with Color as value (set with setColorForAttribute) */
    public final static String TAB_TITLE_COLOR_VERT
        = "TAB_TITLE_COLOR_VERT";

    // color sets (please maintain alphabetical order)
    /** key with Color array as value (set with setColorArrayForAttribute) */
    public final static String SLAB_BORDER_HIGHLIGHT_COLORS
        = "SLAB_BORDER_HIGHLIGHT_COLORS";
    /** key with Color array as value (set with setColorArrayForAttribute) */
    public final static String SLAB_BORDER_SHADOW_COLORS
        = "SLAB_BORDER_SHADOW_COLORS";

    // fonts (please maintain alphabetical order)
    /** key with Font as value (set with setFontForAttribute) */
    public final static String GIANT_FONT_BOLD
        = "GIANT_FONT_BOLD";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String GIANT_FONT_PLAIN
        = "GIANT_FONT_PLAIN";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String LARGE_FONT_BOLD
        = "LARGE_FONT_BOLD";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String LARGE_FONT_PLAIN
        = "LARGE_FONT_PLAIN";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String SMALL_FONT_BOLD
        = "SMALL_FONT_BOLD";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String SMALL_FONT_PLAIN
        = "SMALL_FONT_PLAIN";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String STANDARD_FONT_BOLD
        = "STANDARD_FONT_BOLD";
    /** key with Font as value (set with setFontForAttribute) */
    public final static String STANDARD_FONT_PLAIN
        = "STANDARD_FONT_PLAIN";

    // type constants used internally
    private final static String COLOR_TYPE = "COLOR";
    private final static String COLOR_ARRAY_TYPE = "COLOR ARRAY";
    private final static String FONT_TYPE = "FONT";
    private final static String INT_TYPE = "INT";
    private final static String IMAGE_TYPE = "IMAGE";

    //
    // initialization
    //

    // these initial capacities should be just big enough
    // to hold all of the values of that type. This is an
    // optimization so that the hashtables don't have to
    // re-hash as items are added.
    private final static int BITMAP_INITIAL_CAPACITY = 61;
    private final static int COLOR_INITIAL_CAPACITY = 61;
    private final static int COLOR_ARRAY_INITIAL_CAPACITY = 5;
    private final static int FONT_INITIAL_CAPACITY = 11;
    private final static int INTEGER_INITIAL_CAPACITY = 17;
    private final static int TOTAL_INITIAL_CAPACITY = 157;

    /** table mapping attribute keys (Strings) to integers, file names, etc. */
    private final static Hashtable myAttributes = new Hashtable(TOTAL_INITIAL_CAPACITY);
    /** table mapping file names to bitmaps */
    private final static Hashtable myBitmaps = new Hashtable(BITMAP_INITIAL_CAPACITY);
    /** table mapping color values as Strings to Color objects */
    private final static Hashtable myColors = new Hashtable(COLOR_INITIAL_CAPACITY);
    /** table mapping color sets as Strings to Color[] objects */
    private final static Hashtable myColorArrays = new Hashtable(COLOR_ARRAY_INITIAL_CAPACITY);
    /** table mapping font descriptions as Strings to Font objects */
    private final static Hashtable myFonts = new Hashtable(FONT_INITIAL_CAPACITY);
    /** table mapping keys to types */
    private final static Hashtable myTypeTable = new Hashtable(TOTAL_INITIAL_CAPACITY);

    private static SuperRepository myRepository = null;

    static  {
        // get stone look by default
        fillWithDefaults();
    }

    //
    // --- methods start here ----
    //

    //
    // public layout measurements
    //

    /**
     * Returns thickness of bezel border. Assumes all edges have same
     * thickness, and thickness is the same for raised and lowered bezel.
     */
    public static int bezelThickness() {
        // use constant here for speed; measuring image is more correct
        // but very slow (especially with editable text fields)
        return intAttribute(BEZEL_THICKNESS);
    }

    /**
     * Tight margin between some closely related items in a window, such as an
     * icon and its text label.
     */
    public static int extraThinMargin() {
        return intAttribute(EXTRA_THIN_MARGIN);
    }

    /** Maximum height/width of icon */
    public static int maxIconSize() {
        return intAttribute(MAX_ICON_SIZE);
    }

    /** Typical height of a buttons */
    public static int standardButtonHeight() {
        return intAttribute(STANDARD_BUTTON_HEIGHT);
    }

    /** Typical horizontal spacing between buttons */
    public static int standardButtonMargin() {
        return intAttribute(STANDARD_BUTTON_MARGIN);
    }

    /**
     * Space to add to the width of a button's title to get the
     * minimum decent-looking button width
     */
    public static int standardButtonTitlePadding() {
        return intAttribute(STANDARD_BUTTON_TITLE_PADDING);
    }

    /**
     * Margin between edges of window and UI elements inside the window. Also
     * typical margin between UI elements in a window.
     */
    public static int standardMargin() {
        return intAttribute(STANDARD_MARGIN);
    }

    /** Thickness of typical tab. */
    public static int standardTabThickness() {
        return intAttribute(STANDARD_TAB_THICKNESS);
    }

    /** Height of a typical text field. */
    public static int standardTextHeight() {
        return intAttribute(STANDARD_TEXT_HEIGHT);
    }

    /**
     * Margin between some closely related items in a window, such as an
     * icon and associated button.
     */
    public static int thinMargin() {
        return intAttribute(THIN_MARGIN);
    }

    //
    // fonts
    //

    /**
     * Bold version of largest font to use with stone look user interface
     * elements.
     * @see #giantFontPlain
     */
    public static Font giantFontBold() {
        return fontAttribute(GIANT_FONT_BOLD);
    }

    /**
     * Plain version of argest font to use with stone look user interface
     * elements. Come on, you don't really want to use a bigger font.
     * No you don't. Stop that.
     * @see #giantFontBold
     */
    public static Font giantFontPlain() {
        return fontAttribute(GIANT_FONT_PLAIN);
    }

    /**
     * Bold version of large font to use with stone look user interface
     * elements.
     * @see #largeFontPlain
     */
    public static Font largeFontBold() {
        return fontAttribute(LARGE_FONT_BOLD);
    }

    /**
     * Plain version of large font to use with stone look user interface
     * elements.
     * @see #largeFontBold
     */
    public static Font largeFontPlain() {
        return fontAttribute(LARGE_FONT_PLAIN);
    }

    /**
     * Bold version of smallest font to use with stone look user interface
     * elements.
     * @see #smallFontPlain
     */
    public static Font smallFontBold() {
        return fontAttribute(SMALL_FONT_BOLD);
    }

    /**
     * Plain version of smallest font to use with stone look user interface
     * elements.
     * @see #smallFontBold
     */
    public static Font smallFontPlain() {
        return fontAttribute(SMALL_FONT_PLAIN);
    }

    /**
     * Bold version of standard sized font to use with stone look user
     * interface elements. Bigger than <b>smallFont</b>, smaller than
     * <b>largeFont</b>.
     * @see #standardFontPlain
     */
    public static Font standardFontBold() {
        return fontAttribute(STANDARD_FONT_BOLD);
    }

    /**
     * Plain version of standard sized font to use with stone look user
     * interface elements. Bigger than <b>smallFont</b>, smaller than
     * <b>largeFont</b>.
     * @see #standardFontPlain
     */
    public static Font standardFontPlain() {
        return fontAttribute(STANDARD_FONT_PLAIN);
    }


    //
    // colors
    //

    /**
     * Returns color used to draw active button titles
     */
    public static Color activeButtonTitleColor() {
        return colorAttribute(ACTIVE_BUTTON_TITLE_COLOR);
    }

    /**
     * Returns color used to draw button titles (both enabled and
     * disabled) for borderless buttons such as check boxes and radio buttons.
     */
    public static Color borderlessButtonTitleColor() {
        return colorAttribute(BORDERLESS_BUTTON_TITLE_COLOR);
    }

    /**
     * Color sometimes used as a background for images and text. Darker
     * than lightBackgroundColor()
     * @see #lightBackgroundColor
     */
    public static Color darkBackgroundColor() {
        return colorAttribute(DARK_BACKGROUND_COLOR);
    }

    /**
     * Color used for bottom-of-the-window status text when it's against
     * a light background
     * @see #lightStatusTextColor
     */
    public static Color darkStatusTextColor() {
        return colorAttribute(DARK_STATUS_TEXT_COLOR);
    }

    /**
     * Color often used as background for displayed data, such
     * as lists, control groups, and blocks of non-editable text.
     */
    public static Color dataBackgroundColor() {
        return colorAttribute(DATA_BACKGROUND_COLOR);
    }

    /**
     * Returns color used to draw disabled border lines.
     */
    public static Color disabledBorderColor() {
        return colorAttribute(DISABLED_BORDER_COLOR);
    }

    /** Returns color used to draw ring in default button. */
    public static Color defaultButtonIndicatorColor() {
        return colorAttribute(DEFAULT_BUTTON_INDICATOR_COLOR);
    }

    /** Returns color used to fill disabled buttons. */
    public static Color disabledButtonColor() {
        return colorAttribute(DISABLED_BUTTON_COLOR);
    }

    /**
     * Returns color used to draw disabled button titles.
     */
    public static Color disabledButtonTitleColor() {
        return colorAttribute(DISABLED_BUTTON_TITLE_COLOR);
    }

    /** color used to draw characters of temporarily non-editable text */
    public static Color disabledEditableTextColor() {
        return colorAttribute(DISABLED_EDITABLE_TEXT_COLOR);
    }

    /** color used to fill areas behind editable text */
    public static Color disabledEditableTextFillColor() {
        return colorAttribute(DISABLED_EDITABLE_TEXT_FILL_COLOR);
    }

    /** Returns color used to fill disabled scroll bar */
    public static Color disabledScrollBarColor() {
        return colorAttribute(DISABLED_SCROLL_BAR_FILL_COLOR);
    }

    /** color used to draw disabled text labels and such */
    public static Color disabledStaticTextColor() {
        return colorAttribute(DISABLED_STATIC_TEXT_COLOR);
    }

    /** color used to fill disabled popup */
    public static Color disabledPopupColor() {
        return colorAttribute(DISABLED_POPUP_COLOR);
    }

    /** Returns color used to draw border lines around editable text, etc. */
    public static Color editableBorderColor() {
        return colorAttribute(EDITABLE_BORDER_COLOR);
    }

    /** color used to draw characters of editable text */
    public static Color editableTextColor() {
        return colorAttribute(EDITABLE_TEXT_COLOR);
    }

    /** color used to fill areas behind editable text */
    public static Color editableTextFillColor() {
        return colorAttribute(EDITABLE_TEXT_FILL_COLOR);
    }

    /** color used to draw behind selected text */
    public static Color editableTextSelectionColor() {
        return colorAttribute(EDITABLE_TEXT_SELECTION_COLOR);
    }

    /** color used to draw emphasized text in a block of text */
    public static Color emphasizedTextColor() {
        return colorAttribute(EMPHASIZED_TEXT_COLOR);
    }

    /**
     * Color often used as a background for images and text. Lighter
     * than darkBackgroundColor()
     * @see #darkBackgroundColor
     */
    public static Color lightBackgroundColor() {
        return colorAttribute(LIGHT_BACKGROUND_COLOR);
    }

    /**
     * Color occasionally used as a background for raised containers
     * that are themselves on lightBackgroundColor.
     * @see #lightBackgroundColor
     */
    public static Color lightestBackgroundColor() {
        return colorAttribute(LIGHTEST_BACKGROUND_COLOR);
    }

    /**
     * Color used for bottom-of-the-window status text when it's against
     * a dark background
     * @see #darkStatusTextColor
     */
    public static Color lightStatusTextColor() {
        return colorAttribute(LIGHT_STATUS_TEXT_COLOR);
    }

    /** Returns color used to draw background of selected list item */
    public static Color listItemSelectedColor() {
        return colorAttribute(LIST_ITEM_SELECTED_COLOR);
    }

    /** Returns color used to draw title of selected list item */
    public static Color listItemSelectedTitleColor() {
        return colorAttribute(LIST_ITEM_SELECTED_TITLE_COLOR);
    }

    /** Returns color used to fill lowered bordered buttons */
    public static Color loweredButtonColor() {
        return colorAttribute(LOWERED_BUTTON_COLOR);
    }

    /** color used to fill enabled popup */
    public static Color popupColor() {
        return colorAttribute(POPUP_COLOR);
    }

    /** color used to draw highlights on border line around scroll bar */
    public static Color scrollBarBorderHighlightColor() {
        return colorAttribute(SCROLL_BAR_BORDER_HIGHLIGHT_COLOR);
    }

    /** color used to draw line inside border around scroll bar */
    public static Color scrollBarBorderInsideColor() {
        return colorAttribute(SCROLL_BAR_BORDER_INSIDE_COLOR);
    }

    /** color used to draw border line around scroll bar */
    public static Color scrollBarBorderLineColor() {
        return colorAttribute(SCROLL_BAR_BORDER_LINE_COLOR);
    }

    /** color used to fill scroll bar knob */
    public static Color scrollKnobColor() {
        return colorAttribute(SCROLL_KNOB_COLOR);
    }

    /** Returns color used to fill raised bordered buttons */
    public static Color raisedButtonColor() {
        return colorAttribute(RAISED_BUTTON_COLOR);
    }

    /** Color used for body of selected tab. */
    public static Color selectedTabColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? SELECTED_TAB_COLOR_HORIZ
            : SELECTED_TAB_COLOR_VERT);
    }

    /** Color used for selected tab's border */
    public static Color selectedTabEdgeColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? SELECTED_TAB_EDGE_COLOR_HORIZ
            : SELECTED_TAB_EDGE_COLOR_VERT);
    }

    /** Color used for selected tab's title */
    public static Color selectedTabTitleColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? SELECTED_TAB_TITLE_COLOR_HORIZ
            : SELECTED_TAB_TITLE_COLOR_VERT);
    }

    /** Color used for selected editable text */
    public static Color selectedTextColor() {
        return colorAttribute(SELECTED_TEXT_COLOR);
    }

    /** Returns color used to draw border lines around containers, etc. */
    public static Color standardBorderColor() {
        return colorAttribute(STANDARD_BORDER_COLOR);
    }

    /** Returns color used to draw border lines around containers, etc. */
    public static Color standardBorderHighlightColor() {
        return colorAttribute(STANDARD_BORDER_HIGHLIGHT_COLOR);
    }

    /** Returns color used to draw static (permanently non-editable) text */
    public static Color staticTextColor() {
        return colorAttribute(STATIC_TEXT_COLOR);
    }

    /** Color used for edge of unselected tabs. */
    public static Color tabEdgeColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? TAB_EDGE_COLOR_HORIZ
            : TAB_EDGE_COLOR_VERT);
    }

    /** Color used for body of tabs. */
    public static Color tabFillColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? TAB_FILL_COLOR_HORIZ
            : TAB_FILL_COLOR_VERT);
    }

    /** Color used for title of unselected tabs. */
    public static Color tabTitleColor(boolean horizontal) {
        return colorAttribute(
            horizontal
            ? TAB_TITLE_COLOR_HORIZ
            : TAB_TITLE_COLOR_VERT);
    }

    //
    // color sets
    //

    /** Returns colors used for lightened edges of slab border */
    public static Color[] slabBorderHighlightColors() {
        return colorArrayAttribute(SLAB_BORDER_HIGHLIGHT_COLORS);
    }

    /** Returns colors used for darkened edges of slab border */
    public static Color[] slabBorderShadowColors() {
        return colorArrayAttribute(SLAB_BORDER_SHADOW_COLORS);
    }

    //
    // images
    //

    /** Returns image used for unselected check box */
    public static Bitmap checkboxOff(boolean enabled) {
        return bitmapAttribute(enabled
            ? CHECKBOX_OFF
            : CHECKBOX_OFF_DISABLED);
    }

    /** Returns image used for selected check box */
    public static Bitmap checkboxOn(boolean enabled) {
        return bitmapAttribute(enabled
            ? CHECKBOX_ON
            : CHECKBOX_ON_DISABLED);
    }

    /** Returns image used for unselected radio button */
    public static Bitmap radioButtonOff(boolean enabled) {
        return bitmapAttribute(enabled
            ? RADIO_BUTTON_OFF
            : RADIO_BUTTON_OFF_DISABLED);
    }

    /** Returns image used for selected radio button */
    public static Bitmap radioButtonOn(boolean enabled) {
        return bitmapAttribute(enabled
            ? RADIO_BUTTON_ON
            : RADIO_BUTTON_ON_DISABLED);
    }

    /**
     * Returns image drawn in center of scroll knob if scrollKnobImage
     * doesn't fit.
     */
    public static Bitmap smallScrollKnobImage(boolean horizontal) {
        return horizontal
            ? bitmapAttribute(SMALL_HORIZ_SCROLL_KNOB_IMAGE)
            : bitmapAttribute(SMALL_VERT_SCROLL_KNOB_IMAGE);

    }

    /** Returns image drawn in center of scroll knob */
    public static Bitmap scrollKnobImage(boolean horizontal) {
        return horizontal
            ? bitmapAttribute(HORIZ_SCROLL_KNOB_IMAGE)
            : bitmapAttribute(VERT_SCROLL_KNOB_IMAGE);

    }

    /**
     * Returns texture used for body of scroll track
     */
    public static Bitmap scrollTrackTexture(boolean horizontal) {
        return horizontal
            ? bitmapAttribute(HORIZ_SCROLL_TRACK_TEXTURE)
            : bitmapAttribute(VERT_SCROLL_TRACK_TEXTURE);

    }

    /**
     * Returns image used for page-turning corner (SLPageTurner) when the
     * first page is showing.
     */
    public static Bitmap pageCornerFirstPageImage() {
        return bitmapAttribute(PAGE_CORNER_FIRST);
    }

    /**
     * Returns image used for page-turning corner (SLPageTurner) when the
     * last page is showing.
     */
    public static Bitmap pageCornerLastPageImage() {
        return bitmapAttribute(PAGE_CORNER_LAST);
    }

    /**
     * Returns image used for page-turning corner (SLPageTurner) when some
     * page other than the first or last is showing.
     */
    public static Bitmap pageCornerMiddlePageImage() {
        return bitmapAttribute(PAGE_CORNER_MIDDLE);
    }

    /**
     * Returns image used for pop-up menu arrows
     */
    public static Bitmap popupArrowImage() {
        return bitmapAttribute(STANDARD_COLOR_POPUP_ARROW);
    }

    /** Returns image used for disabled pop-up menu arrows */
    public static Bitmap popupArrowDisabledImage() {
        return bitmapAttribute(DISABLED_POPUP_ARROW);
    }

    /**
     * Returns image used for scroll-up button (not pressed)
     */
    public static Bitmap scrollUpImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_UP_BUTTON);
    }

    /**
     * Returns image used for pressed scroll-up button
     */
    public static Bitmap scrollUpPressedImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_UP_PRESSED_BUTTON);
    }

    /** Returns image used for disabled scroll-up button */
    public static Bitmap scrollUpDisabledImage() {
        return bitmapAttribute(DISABLED_SCROLL_UP_BUTTON);
    }

    /**
     * Returns image used for scroll-down button (not pressed)
     */
    public static Bitmap scrollDownImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_DOWN_BUTTON);
    }

    /**
     * Returns image used for pressed scroll-down button
     */
    public static Bitmap scrollDownPressedImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_DOWN_PRESSED_BUTTON);
    }

    /** Returns image used for disabled scroll-down button */
    public static Bitmap scrollDownDisabledImage() {
        return bitmapAttribute(DISABLED_SCROLL_DOWN_BUTTON);
    }

    /**
     * Returns image used for scroll-left button (not pressed)
     */
    public static Bitmap scrollLeftImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_LEFT_BUTTON);
    }

    /**
     * Returns image used for pressed scroll-left button
     */
    public static Bitmap scrollLeftPressedImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_LEFT_PRESSED_BUTTON);
    }

    /** Returns image used for disabled scroll-left button */
    public static Bitmap scrollLeftDisabledImage() {
        return bitmapAttribute(DISABLED_SCROLL_LEFT_BUTTON);
    }

    /**
     * Returns image used for scroll-right button (not pressed)
     */
    public static Bitmap scrollRightImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_RIGHT_BUTTON);
    }

    /**
     * Returns image used for pressed scroll-right button
     */
    public static Bitmap scrollRightPressedImage() {
        return bitmapAttribute(STANDARD_COLOR_SCROLL_RIGHT_PRESSED_BUTTON);
    }

    /** Returns image used for disabled scroll-right button */
    public static Bitmap scrollRightDisabledImage() {
        return bitmapAttribute(DISABLED_SCROLL_RIGHT_BUTTON);
    }

    /**
     * Returns image used for tab view scroll-up button (not pressed)
     */
    public static Bitmap tabUpImage() {
        return bitmapAttribute(TAB_UP_BUTTON);
    }

    /**
     * Returns image used for tab view pressed scroll-up button
     */
    public static Bitmap tabUpPressedImage() {
        return bitmapAttribute(TAB_UP_PRESSED_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-up button
     */
    public static Bitmap tabUpOtherImage() {
        return bitmapAttribute(TAB_UP_OTHER_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-up pressed button
     */
    public static Bitmap tabUpOtherPressedImage() {
        return bitmapAttribute(TAB_UP_OTHER_PRESSED_BUTTON);
    }

    /**
     * Returns image used for tab view scroll-down button (not pressed)
     */
    public static Bitmap tabDownImage() {
        return bitmapAttribute(TAB_DOWN_BUTTON);
    }

    /**
     * Returns image used for tab view pressed scroll-down button
     */
    public static Bitmap tabDownPressedImage() {
        return bitmapAttribute(TAB_DOWN_PRESSED_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-down button
     */
    public static Bitmap tabDownOtherImage() {
        return bitmapAttribute(TAB_DOWN_OTHER_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-down pressed button
     */
    public static Bitmap tabDownOtherPressedImage() {
        return bitmapAttribute(TAB_DOWN_OTHER_PRESSED_BUTTON);
    }

    /**
     * Returns image used for tab view scroll-left button (not pressed)
     */
    public static Bitmap tabLeftImage() {
        return bitmapAttribute(TAB_LEFT_BUTTON);
    }

    /**
     * Returns image used for tab view pressed scroll-left button
     */
    public static Bitmap tabLeftPressedImage() {
        return bitmapAttribute(TAB_LEFT_PRESSED_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-left button
     */
    public static Bitmap tabLeftOtherImage() {
        return bitmapAttribute(TAB_LEFT_OTHER_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-left pressed button
     */
    public static Bitmap tabLeftOtherPressedImage() {
        return bitmapAttribute(TAB_LEFT_OTHER_PRESSED_BUTTON);
    }

    /**
     * Returns image used for tab view scroll-right button (not pressed)
     */
    public static Bitmap tabRightImage() {
        return bitmapAttribute(TAB_RIGHT_BUTTON);
    }

    /**
     * Returns image used for tab view pressed scroll-right button
     */
    public static Bitmap tabRightPressedImage() {
        return bitmapAttribute(TAB_RIGHT_PRESSED_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-right button
     */
    public static Bitmap tabRightOtherImage() {
        return bitmapAttribute(TAB_RIGHT_OTHER_BUTTON);
    }

    /**
     * Returns *other* image used for tab view scroll-right pressed button
     */
    public static Bitmap tabRightOtherPressedImage() {
        return bitmapAttribute(TAB_RIGHT_OTHER_PRESSED_BUTTON);
    }

    //
    // utilities
    //

    /**
     * Updates attribute values as read from file, one per line.
     * Format for valid line is: "KEY:VALUE", where KEY is one
     * of the keys defined in this class, and VALUE is an appropriate
     * value for KEY (e.g., if KEY expects a color, VALUE must be of
     * form "RRR GGG BBB"). Lines with invalid formats are ignored.
     */
    public static void applyOverridesFromFile(File f) {
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                BufferedReader br =
                    new BufferedReader(new InputStreamReader(fis));
                applyOverridesFromBufferedReader(br);
                fis.close();
            } catch (IOException e) {
                Trace.gui.errorm("couldn't read from " + f.getName());
            }
        } else {
            if (Trace.gui.warning && Trace.ON) {
                Trace.gui.warningm("couldn't find overrides file named " + f.getName());
            }
        }
    }

    /**
     * Fill in attribute hashtable with all the default measurements,
     * colors, images, etc. These defaults implement the "stone look"
     */
    public static void fillWithDefaults() {
        myAttributes.clear();

        // constants (please maintain alphabetical order)

        setIntForAttribute(BEZEL_THICKNESS, 2);
        setIntForAttribute(EXTRA_THIN_MARGIN, 2);
        setIntForAttribute(MAX_ICON_SIZE, 48);
        setIntForAttribute(STANDARD_BUTTON_HEIGHT, 21);
        setIntForAttribute(STANDARD_BUTTON_MARGIN, 12);
        setIntForAttribute(STANDARD_BUTTON_TITLE_PADDING, 24);
        setIntForAttribute(STANDARD_MARGIN, 8);
        setIntForAttribute(STANDARD_TAB_THICKNESS, 30);
        setIntForAttribute(STANDARD_TEXT_HEIGHT, 25);
        setIntForAttribute(THIN_MARGIN, 3);

        // images (please maintain alphabetical order)
        setImageForAttribute(CHECKBOX_ON,
            "check_button_on.gif");
        setImageForAttribute(CHECKBOX_ON_DISABLED,
            "check_button_on_disabled.gif");
        setImageForAttribute(CHECKBOX_OFF,
            "radio_check_button_off.gif");
        setImageForAttribute(CHECKBOX_OFF_DISABLED,
            "radio_check_button_off_disabled.gif");
        setImageForAttribute(DISABLED_POPUP_ARROW,
            "pop_down_arrow_disabled.gif");
        setImageForAttribute(DISABLED_SCROLL_DOWN_BUTTON,
            "scroll_down_arrow_disabled.gif");
        setImageForAttribute(DISABLED_SCROLL_LEFT_BUTTON,
            "scroll_left_arrow_disabled.gif");
        setImageForAttribute(DISABLED_SCROLL_RIGHT_BUTTON,
            "scroll_right_arrow_disabled.gif");
        setImageForAttribute(DISABLED_SCROLL_UP_BUTTON,
            "scroll_up_arrow_disabled.gif");
        setImageForAttribute(HORIZ_SCROLL_KNOB_IMAGE,
            "horiz_scroll_knob_image.gif");
        setImageForAttribute(HORIZ_SCROLL_TRACK_TEXTURE,
            "scroll_track_texture.gif");
        setImageForAttribute(PAGE_CORNER_FIRST,
            "page_turner_first.gif");
        setImageForAttribute(PAGE_CORNER_LAST,
            "page_turner_last.gif");
        setImageForAttribute(PAGE_CORNER_MIDDLE,
            "page_turner_middle.gif");
        setImageForAttribute(RADIO_BUTTON_ON,
            "radio_button_on.gif");
        setImageForAttribute(RADIO_BUTTON_ON_DISABLED,
            "radio_button_on_disabled.gif");
        setImageForAttribute(RADIO_BUTTON_OFF,
            "radio_check_button_off.gif");
        setImageForAttribute(RADIO_BUTTON_OFF_DISABLED,
            "radio_check_button_off_disabled.gif");
        setImageForAttribute(SMALL_HORIZ_SCROLL_KNOB_IMAGE,
            "small_horiz_scroll_knob_image.gif");
        setImageForAttribute(SMALL_VERT_SCROLL_KNOB_IMAGE,
            "small_vert_scroll_knob_image.gif");
        setImageForAttribute(STANDARD_COLOR_POPUP_ARROW,
            "pop_down_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_DOWN_BUTTON,
            "scroll_down_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_DOWN_PRESSED_BUTTON,
            "scroll_down_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_LEFT_BUTTON,
            "scroll_left_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_LEFT_PRESSED_BUTTON,
            "scroll_left_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_RIGHT_BUTTON,
            "scroll_right_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_RIGHT_PRESSED_BUTTON,
            "scroll_right_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_UP_BUTTON,
            "scroll_up_arrow.gif");
        setImageForAttribute(STANDARD_COLOR_SCROLL_UP_PRESSED_BUTTON,
            "scroll_up_arrow.gif");
        setImageForAttribute(TAB_DOWN_BUTTON,
            "tab_down.gif");
        setImageForAttribute(TAB_DOWN_OTHER_BUTTON,
            "tab_down_missing.gif");
        setImageForAttribute(TAB_DOWN_OTHER_PRESSED_BUTTON,
            "tab_down_missing_pressed.gif");
        setImageForAttribute(TAB_DOWN_PRESSED_BUTTON,
            "tab_down_pressed.gif");
        setImageForAttribute(TAB_LEFT_BUTTON,
            "tab_left.gif");
        setImageForAttribute(TAB_LEFT_OTHER_BUTTON,
            "tab_left_green.gif");
        setImageForAttribute(TAB_LEFT_OTHER_PRESSED_BUTTON,
            "tab_left_pressed.gif");
        setImageForAttribute(TAB_LEFT_PRESSED_BUTTON,
            "tab_left_pressed.gif");
        setImageForAttribute(TAB_RIGHT_BUTTON,
            "tab_right.gif");
        setImageForAttribute(TAB_RIGHT_OTHER_BUTTON,
            "tab_right_green.gif");
        setImageForAttribute(TAB_RIGHT_OTHER_PRESSED_BUTTON,
            "tab_right_pressed.gif");
        setImageForAttribute(TAB_RIGHT_PRESSED_BUTTON,
            "tab_right_pressed.gif");
        setImageForAttribute(TAB_UP_BUTTON,
            "tab_up.gif");
        setImageForAttribute(TAB_UP_OTHER_BUTTON,
            "tab_up_missing.gif");
        setImageForAttribute(TAB_UP_OTHER_PRESSED_BUTTON,
            "tab_up_missing_pressed.gif");
        setImageForAttribute(TAB_UP_PRESSED_BUTTON,
            "tab_up_pressed.gif");
        setImageForAttribute(VERT_SCROLL_KNOB_IMAGE,
            "vert_scroll_knob_image.gif");
        setImageForAttribute(VERT_SCROLL_TRACK_TEXTURE,
            "scroll_track_texture.gif");

        // for convenience, name the colors
        String black = "0 0 0";
        String white = "255 255 255";
        String tan = "255 239 198";
        String darkBlue = "0 66 90";
        String disabledBlueGray = "173 181 181";
        String mediumGray = "150 150 150";
        String lightBlue = "181 198 206";
        String veryLightBlue = "231 231 222";
        String red = "255 0 0";
        String borderShadow = "115 115 107";
        String borderHighlight = "222 222 214";
        String mediumBlue = "90 123 132";
        String vertTabEdge = "138 163 187";
        String horizTabEdge = "216 229 241";

        // colors (please maintain alphabetical order)
        setColorForAttribute(ACTIVE_BUTTON_TITLE_COLOR, black);
        setColorForAttribute(BORDERLESS_BUTTON_TITLE_COLOR, black);
        setColorForAttribute(DARK_BACKGROUND_COLOR, darkBlue);
        setColorForAttribute(DARK_STATUS_TEXT_COLOR, darkBlue);
        setColorForAttribute(DATA_BACKGROUND_COLOR, tan);
        setColorForAttribute(DEFAULT_BUTTON_INDICATOR_COLOR, black);
        setColorForAttribute(DISABLED_BUTTON_COLOR, disabledBlueGray);
        setColorForAttribute(DISABLED_BORDER_COLOR, borderShadow);
        setColorForAttribute(DISABLED_BUTTON_TITLE_COLOR, white);
        setColorForAttribute(DISABLED_POPUP_COLOR, lightBlue);
        setColorForAttribute(DISABLED_SCROLL_BAR_FILL_COLOR, disabledBlueGray);
        setColorForAttribute(DISABLED_EDITABLE_TEXT_COLOR, mediumGray);
        setColorForAttribute(DISABLED_EDITABLE_TEXT_FILL_COLOR, white);
        setColorForAttribute(DISABLED_STATIC_TEXT_COLOR, mediumGray);
        setColorForAttribute(EDITABLE_BORDER_COLOR, borderShadow);
        setColorForAttribute(EDITABLE_TEXT_COLOR, black);
        setColorForAttribute(EDITABLE_TEXT_FILL_COLOR, white);
        setColorForAttribute(EDITABLE_TEXT_SELECTION_COLOR, darkBlue);
        setColorForAttribute(EMPHASIZED_TEXT_COLOR, red);
        setColorForAttribute(LIGHT_BACKGROUND_COLOR, lightBlue);
        setColorForAttribute(LIGHTEST_BACKGROUND_COLOR, veryLightBlue);
        setColorForAttribute(LIGHT_STATUS_TEXT_COLOR, lightBlue);
        setColorForAttribute(LIST_ITEM_SELECTED_COLOR, darkBlue);
        setColorForAttribute(LIST_ITEM_SELECTED_TITLE_COLOR, white);
        setColorForAttribute(LOWERED_BUTTON_COLOR, lightBlue);
        setColorForAttribute(POPUP_COLOR, lightBlue);
        setColorForAttribute(RAISED_BUTTON_COLOR, lightBlue);
        setColorForAttribute(SCROLL_BAR_BORDER_HIGHLIGHT_COLOR, borderHighlight);
        setColorForAttribute(SCROLL_BAR_BORDER_INSIDE_COLOR, lightBlue);
        setColorForAttribute(SCROLL_BAR_BORDER_LINE_COLOR, borderShadow);
        setColorForAttribute(SCROLL_KNOB_COLOR, lightBlue);
        setColorForAttribute(SELECTED_TAB_COLOR_HORIZ, veryLightBlue);
        setColorForAttribute(SELECTED_TAB_COLOR_VERT, lightBlue);
        setColorForAttribute(SELECTED_TAB_EDGE_COLOR_HORIZ, white);
        setColorForAttribute(SELECTED_TAB_EDGE_COLOR_VERT, black);
        setColorForAttribute(SELECTED_TAB_TITLE_COLOR_HORIZ, black);
        setColorForAttribute(SELECTED_TAB_TITLE_COLOR_VERT, black);
        setColorForAttribute(SELECTED_TEXT_COLOR, white);
        setColorForAttribute(STANDARD_BORDER_COLOR, borderShadow);
        setColorForAttribute(STANDARD_BORDER_HIGHLIGHT_COLOR, borderHighlight);
        setColorForAttribute(STATIC_TEXT_COLOR, black);
        setColorForAttribute(TAB_EDGE_COLOR_HORIZ, horizTabEdge);
        setColorForAttribute(TAB_EDGE_COLOR_VERT, vertTabEdge);
        setColorForAttribute(TAB_FILL_COLOR_HORIZ, lightBlue);
        setColorForAttribute(TAB_FILL_COLOR_VERT, mediumBlue);
        setColorForAttribute(TAB_TITLE_COLOR_HORIZ, black);
        setColorForAttribute(TAB_TITLE_COLOR_VERT, black);

        // color sets (please maintain alphabetical order)
        setColorArrayForAttribute(SLAB_BORDER_HIGHLIGHT_COLORS,
            "129 146 154,151 170 178,194 211 213");
        setColorArrayForAttribute(SLAB_BORDER_SHADOW_COLORS,
            "86 97 101,107 122 128,151 170 178");

        // fonts (please maintain alphabetical order)
        setFontForAttribute(GIANT_FONT_BOLD,
            "Helvetica," + Font.BOLD + ",18");
        setFontForAttribute(GIANT_FONT_PLAIN,
            "Helvetica," + Font.PLAIN + ",18");
        setFontForAttribute(LARGE_FONT_BOLD,
            "Helvetica," + Font.BOLD + ",14");
        setFontForAttribute(LARGE_FONT_PLAIN,
            "Helvetica," + Font.PLAIN + ",14");
        setFontForAttribute(SMALL_FONT_BOLD,
            "Helvetica," + Font.BOLD + ",10");
        setFontForAttribute(SMALL_FONT_PLAIN,
            "Helvetica," + Font.PLAIN + ",10");
        setFontForAttribute(STANDARD_FONT_BOLD,
            "Helvetica," + Font.BOLD + ",12");
        setFontForAttribute(STANDARD_FONT_PLAIN,
            "Helvetica," + Font.PLAIN + ",12");
    }

    /**
     * Sets the value of attribute represented by <b>key</b> to the
     * specified set of colors. <b>arrayAsString</b> must be of the form
     * "RRR GGG BBB,RRR GGG BBB,...".
     */
    public static void setColorArrayForAttribute(String key,
                                                  String arrayAsString) {
        // first create the color array if it hasn't already been created
        if (!myColorArrays.containsKey(arrayAsString)) {
            Color[] colors;
            try {
                colors = makeColorArrayFromString(arrayAsString);
            } catch (MalformedStringException e) {
                Trace.gui.errorm(e.getMessage());
                return;
            }
            myColorArrays.put(arrayAsString, colors);
        }

        // now update attributes and types table
        myAttributes.put(key, arrayAsString);
        myTypeTable.put(key, COLOR_ARRAY_TYPE);
    }

    /**
     * Sets the value of attribute represented by <b>key</b> to the
     * specified color. <b>colorAsString</b> must be of the form
     * "RRR GGG BBB".
     */
    public static void setColorForAttribute(String key,
                                             String colorAsString) {

        try {
            // first ensure that the color object has been made
            addColorToTable(colorAsString);
        } catch (MalformedStringException e) {
            Trace.gui.errorm(e.getMessage());
            return;
        }

        // now update attributes table
        myAttributes.put(key, colorAsString);
        myTypeTable.put(key, COLOR_TYPE);
    }

    /**
     * Sets the value of attribute represented by <b>key</b> to the
     * specified font. <b>fontAsString</b> must be of the form
     * "fontname,style,size", where style is one of the int constants
     * defined in class Font.
     */
    public static void setFontForAttribute(String key, String fontAsString) {
        // first create the Font if it hasn't already been created
        Font font = null;
        if (!myFonts.containsKey(fontAsString)) {

            try {
                font = makeFontFromString(fontAsString);
            } catch (MalformedStringException e) {
                // for startup load-order reasons we don't fully understand,
                // sometimes you can't load a font at static init time.
                // This code has been changed (by Jay) to do "lazy loading"
                // of fonts to work around this problem. In case we ever want
                // to debug it, the Trace line is still here.
                if (Trace.gui.debug && Trace.ON) {
                    Trace.gui.debugm(e.getMessage());
                }
            }
            if (font != null) {
                myFonts.put(fontAsString, font);
            }
        }

        // now update attributes table
        myAttributes.put(key, fontAsString);
        myTypeTable.put(key, FONT_TYPE);
    }

    /**
     * Sets the value of attribute represented by <b>key</b> to the
     * specified String, representing an image file name.
     */
    public static void setImageForAttribute(String key, String value) {
        // stoopid hashtable can't have null for a value
        if (value == null) {
            value = "";
        }
        myAttributes.put(key, value);
        myTypeTable.put(key, IMAGE_TYPE);
    }

    /**
     * Sets the value of attribute represented by <b>key</b> to the
     * specified int.
     */
    public static void setIntForAttribute(String key, int value) {
        myAttributes.put(key, new Integer(value));
        myTypeTable.put(key, INT_TYPE);
    }

    /**
     * Sets repository IV, provided repository has been initialized,
     * should be called before any call of bitmapFrom... method
     */
    public static void setRepository(Object theRepository) {
        myRepository = (SuperRepository)theRepository;
    }

    //
    // private methods
    //

    /**
     * If the color object for <b>colorAsString</b> hasn't already been
     * added to the internal colors table, does so. In any case, returns
     * the color object (new or pre-existing)
     */
    private static Color addColorToTable(String colorAsString)
        throws MalformedStringException
    {
        // first create the color if it hasn't already been created
        Color color = (Color)myColors.get(colorAsString);
        if (color == null) {
            color = makeColorFromString(colorAsString);
            }
        // add new color to colors table
        myColors.put(colorAsString, color);

        return color;
    }

    /**
     * The guts of applyOverridesFromFile, could be reused some day
     * with non-file input streams.
     */
    private static void applyOverridesFromBufferedReader(BufferedReader br) {
        for (;;) {
            String line;
            try {
                line = br.readLine();
            } catch (IOException e) {
                Trace.gui.errorm("couldn't read from " + br);
                break;
            }
            // check to see if we're done
            if (line == null) {
                break;
            }
            try {
                StringTokenizer st = new StringTokenizer(line, ":");
                // skip over blank lines
                if (!st.hasMoreTokens()) {
                    continue;
                }
                // key must be present, but value is sometimes null
                String key = st.nextToken();
                String value = null;
                if (st.hasMoreTokens()) {
                    value = st.nextToken();
                }

                if (!myAttributes.containsKey(key)) {
                    Trace.gui.errorm(
                        "trying to override non-existent key '" + key + "'");
                    continue;
                }

                String type = typeForKey(key);
                if (type == null) {
                    // this should never happen
                    Trace.gui.errorm(
                        "type previously unspecified for key '" + key + "'");
                    continue;
                }

                if (type.equals(COLOR_TYPE)) {
                    setColorForAttribute(key, value);
                }
                else if (type.equals(COLOR_ARRAY_TYPE)) {
                    setColorArrayForAttribute(key, value);
                }
                else if (type.equals(FONT_TYPE)) {
                    setFontForAttribute(key, value);
                }
                else if (type.equals(INT_TYPE)) {
                    setIntForAttribute(key, Integer.parseInt(value));
                }
                else if (type.equals(IMAGE_TYPE)) {
                    setImageForAttribute(key, value);
                }
                else {
                    Trace.gui.errorm("unknown type '" + type
                                       + "', line ignored");
                }
            } catch (Exception e) {
                // malformed line; ignore it
                Trace.gui.errorm("ignored unparseable line: '" + line + "'");
            }
        }
    }

    /**
     * Returns the value for this key converted to a Bitmap. Assumes
     * value in hashtable is a String for the file name of the Bitmap
     */
    private static Bitmap bitmapAttribute(String key) {
        String imageName = stringAttribute(key);
        imageName = imageName.trim();
        if (imageName.length() == 0) {
            return null;
        }

        Bitmap result = (Bitmap)myBitmaps.get(imageName);
        if (result == null) {
            // this bitmap hasn't been retrieved yet, so let's cache it
            result = bitmapFromRepositoryOrDisk(imageName);
            if (result != null) {
                myBitmaps.put(imageName, result);
            }
        }

        return result;

    }

    private static Bitmap bitmapFromRepositoryOrDisk(String imageName) {
        String fullName = "gui/stonelook/" + imageName;

        if (myRepository != null) {
            try {
                Object cryptoKey = myRepository.getCryptoHash(fullName);
                if (cryptoKey == null) {
                    throw new RepositoryKeyNotFoundException("System object named '" +
                                                             fullName + "' not in symbol table");
                }
                byte[] imageBytes = (byte[])myRepository.get((CryptoHash)cryptoKey);

                return ECBitmap.bitmapFromByteArray(imageBytes);
            } catch (IOException iox) {
                throw new NestedException("Crew access to in-vat SuperRepository using key " + fullName, iox);
            }
            catch (Exception e) {
                Trace.gui.errorm("Couldn't read Stonelook image '" + imageName + "' from repository, will try from file");
                return Bitmap.bitmapNamed(fullName);
            }
        }
        else {
            throw new NullPointerException("Stonelook: Repository hasn't been set yet!");
        }
    }


    /**
     * Returns the value for this key converted to a Color array.
     * Assumes value in attributes hashtable is a string ("RRR GGG BBB,
     * RRR GGG BBB, ...")
     */
    private static Color[] colorArrayAttribute(String key) {
        String arrayAsString = (String)myAttributes.get(key);
        if (arrayAsString == null) {
            Trace.gui.errorm("no stored value for '" + key + "'");
            return null;
        }
        return (Color[])myColorArrays.get(arrayAsString);
    }

    /**
     * Returns the value for this key converted to a Color. Assumes
     * value in hashtable is a color value as a string ("RRR GGG BBB")
     */
    private static Color colorAttribute(String key) {
        String colorAsString = (String)myAttributes.get(key);
        if (colorAsString == null) {
            Trace.gui.errorm("no stored value for '" + key + "'");
            return null;
        }
        return (Color)myColors.get(colorAsString);
    }

    /**
     * Returns the value for this key converted to a Font. Assumes
     * value in hashtable is a font description as a string
     * ("fontname,style,size")
     */
    private static Font fontAttribute(String key) {
        String fontAsString = (String)myAttributes.get(key);
        if (fontAsString == null) {
            Trace.gui.errorm("no stored value for '" + key + "'");
            return null;
        } // XXX JAY - changed to detect a miss and attempt to reload a font.
        Font aFont = (Font) myFonts.get(fontAsString);
        if(aFont != null) return aFont;
        setFontForAttribute(key, fontAsString);
        aFont = (Font) myFonts.get(fontAsString);
        return aFont;
    }

    /**
     * Returns the value for this key converted to an int. Assumes
     * value in hashtable is an Integer
     */
    private static int intAttribute(String key) {
        Integer integerObject = (Integer)myAttributes.get(key);
        if (integerObject == null) {
            Trace.gui.errorm("no stored value for '" + key + "'");
            return 0;
        }
        return integerObject.intValue();
    }

    /**
     * Returns new Color array using colors specified by string. String
     * must be of form "RRR GGG BBB,RRR GGG BBB,...". If string is
     * malformed, throws MalformedStringException.
     */
    private static Color[] makeColorArrayFromString(String arrayAsString)
        throws MalformedStringException
    {
        try {
            // trim white space off beginning and end; this allows a
            // a whitespace-only string to count as an empty array
            arrayAsString = arrayAsString.trim();

            // first break array string into color strings
            StringTokenizer st = new StringTokenizer(arrayAsString, ",");
            Vector colorStrings = new Vector();
            while (st.hasMoreTokens()) {
                colorStrings.addElement(st.nextToken());
            }
            // now convert Vector of Strings to array of Colors. Along the
            // way, ensure that the color table contains all these Colors
            int count = colorStrings.size();
            Color[] array = new Color[count];
            for (int i = 0; i < count; i += 1) {
                String colorString = (String)colorStrings.elementAt(i);
                array[i] = addColorToTable(colorString);
            }
            return array;

        } catch (Exception e) {
            throw new MalformedStringException(
                "can't read color array from '" + arrayAsString + "'");
        }
    }

    /**
     * Returns new Color object using RGB values specified by string. String
     * must be of form "RRR GGG BBB". If string is malformed, throws
     * MalformedStringException.
     */
    private static Color makeColorFromString(String colorAsString)
        throws MalformedStringException
    {
        StringTokenizer st = new StringTokenizer(colorAsString);
        try {
            int r = Integer.parseInt(st.nextToken());
            int g = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            return new Color(r, g, b);
        } catch (Exception e) {
            throw new MalformedStringException(
                "can't read color from '" + colorAsString + "'");
        }
    }

    /**
     * Returns new Font object using description specified by string. String
     * must be of form "fontname,style,size", where style is one of the
     * constant ints defined in class Font. If string is malformed, throws
     * MalformedStringException.
     */
    private static Font makeFontFromString(String fontAsString)
        throws MalformedStringException
    {
        StringTokenizer st = new StringTokenizer(fontAsString, ",");
        Font font;
        try {
            String name = st.nextToken();
            int style = Integer.parseInt(st.nextToken());
            int size = Integer.parseInt(st.nextToken());
            font = Font.fontNamed(name, style, size);
        } catch (Exception e) {
            font = null;
        }

        if (font == null) {
            throw new MalformedStringException(
                "can't create font from '" + fontAsString + "'");
        }

        return font;
    }

    /**
     * Returns the value for this key converted to a String. Assumes
     * value in hashtable is a String
     */
    private static String stringAttribute(String key) {
        return (String)myAttributes.get(key);
    }

    /**
     * Returns the type of the given key. The type is one of the
     * constant values COLOR_TYPE, COLOR_ARRAY_TYPE, FONT_TYPE, INT_TYPE,
     * or IMAGE_TYPE. If the key isn't known to StoneLook, returns null.
     */
    private static String typeForKey(String key) {
        return (String)myTypeTable.get(key);
    }

}

/**
 * Used internally to signify bad string representations for colors,
 * fonts, etc.
 */
class MalformedStringException extends Exception {
    /** Constructs a MalformedStringException. */
    public MalformedStringException() {
        super();
    }

    /**
     * Constructs a MalformedStringException with descriptive message
     * <b>string</b>.
     */
    public MalformedStringException(String string) {
        super(string);
    }
}


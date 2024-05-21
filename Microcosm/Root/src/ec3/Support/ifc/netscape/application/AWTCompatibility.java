// AWTCompatibility.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.awt.image.ImageProducer;


// ALERT! - How does the new cursor system work with AWT components?


/** A collection of static methods for conversion between many AWT and IFC
  * primitives.
  * @note 1.0 bitmapForAWT... methods will no longer leak the bitmaps
  * @note 1.0 added api to get the frame for a RootView
  * @note 1.0 added api to get colors from AWT colors & vice versa
  * @note 1.0 added api to get graphics from AWT graphics.
  */
public class AWTCompatibility {
    private AWTCompatibility() {
    }

    /** Creates a new Bitmap instance from a java.awt.Image.
      */
    public static Bitmap bitmapForAWTImage(java.awt.Image awtImage) {
        return new Bitmap(awtImage);
    }

    /** Creates a new Bitmap instance from a java.awt.image.ImageProducer.
      */
    public static Bitmap bitmapForAWTImageProducer(
        java.awt.image.ImageProducer producer) {
        return new Bitmap(
                        Application.application().applet.createImage(
                                                            producer));
    }

    /** Returns a java.awt.Image instance from a Bitmap.
      */
    public static java.awt.Image awtImageForBitmap(Bitmap bitmap) {
        return bitmap.awtImage;
    }

    /** Returns a java.awt.image.ImageProducer instance from a Bitmap.
      */
    public static java.awt.image.ImageProducer awtImageProducerForBitmap(
                                                            Bitmap bitmap) {
        return bitmap.awtImage.getSource();
    }

    /** Creates a new Sound instance from a java.applet.AudioClip.
      */
    public static Sound soundForAWTAudioClip(java.applet.AudioClip clip) {
        Sound sound;

        sound = new Sound();
        sound.awtSound = clip;

        return sound;
    }

    /** Returns a java.applet.AudioClip instance from a Sound.
      */
    public static java.applet.AudioClip awtAudioClipForSound(Sound sound) {
        return sound.awtSound;
    }

    /** Creates a new Font instance from a java.awt.Font.
      */
    public static Font fontForAWTFont(java.awt.Font awtFont) {
        Font font;

        font = new Font();
        font._awtFont = awtFont;
        font._name = awtFont.getName();
        font._type = Font.AWT;

        return font;
    }

    /** Returns a java.awt.Font instance from a Font.
      */
    public static java.awt.Font awtFontForFont(Font font) {
        return font._awtFont;
    }

    /** Creates a new Color instance from a java.awt.Color.
      *
      */
    public static Color colorForAWTColor(java.awt.Color awtColor) {
        return new Color(awtColor);
    }

    /** Returns a java.awt.Color instance from a Color.
      *
      */
    public static java.awt.Color awtColorForColor(Color color) {
        return color._color;
    }

    /** Creates a new FontMetrics instance from a java.awt.FontMetrics.
      */
    public static FontMetrics fontMetricsForAWTFontMetrics(java.awt.FontMetrics
                                                            awtFontMetrics) {
        return new FontMetrics(awtFontMetrics);
    }

    /** Returns a java.awt.FontMetrics from a FontMetrics.
      */
    public static java.awt.FontMetrics awtFontMetricsForFontMetrics(FontMetrics
                                                            fontMetrics) {
        return fontMetrics._awtMetrics;
    }

    /** Returns a java.awt.MenuBar from a Menu. Returns <b>null</b> if the
      * Menu is not a top-level Menu.
      */
    public static java.awt.MenuBar awtMenuBarForMenu(Menu menu) {
        if (menu.isTopLevel()) {
            return menu.awtMenuBar();
        }
        return null;
    }

    /** Returns a java.awt.Menu from a Menu. Returns <b>null</b> if the Menu
      * is a top-level Menu.
      */
    public static java.awt.Menu awtMenuForMenu(Menu menu) {
        if (!menu.isTopLevel()) {
            return menu.awtMenu();
        }
        return null;
    }

    /** Returns a java.awt.MenuItem from a MenuItem.
      */
    public static java.awt.MenuItem awtMenuItemForMenuItem(MenuItem menuItem) {
        return menuItem.foundationMenuItem();
    }

    /** Creates a new Graphics instance from a java.awt.Graphics.
      *
      */
    public static Graphics graphicsForAWTGraphics(java.awt.Graphics g) {
        java.awt.Rectangle r = g.getClipRect();
        Rect clipRect;

        if (r != null) {
            clipRect = new Rect(r.x, r.y, r.width, r.height);
        } else {
            clipRect = new Rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        return new Graphics(clipRect, g.create());
    }

    /** Returns a java.awt.Graphics instance from a Graphics.
      */
    public static java.awt.Graphics awtGraphicsForGraphics(Graphics g) {
        return g.awtGraphics();
    }

    /** Returns the java.awt.Canvas underlying a RootView.
      */
    public static java.awt.Panel awtPanelForRootView(
        RootView rootView) {
        return rootView.panel();
    }

    /** Returns the java.awt.Window underlying an ExternalWindow.
      * This method returns <b>null</b> if the ExternalWindow <b>window</b>
      * is not visible.
      */
    public static java.awt.Window awtWindowForExternalWindow(ExternalWindow
                                                                    window) {
        return window.awtWindow;
    }

    /** Returns the java.applet.Applet that started this application.
      */
    public static java.applet.Applet awtApplet() {
        Application app;

        app = Application.application();
        if (app == null)
            return null;

        return app.applet;
    }

    /** Returns the java.awt.FileDialog used by the supplied FileChooser.
      */
    public static java.awt.FileDialog awtFileDialogForFileChooser(FileChooser
                                                                fileChooser) {
        return fileChooser.awtDialog;
    }

    /** Returns the application's Toolkit. */
    public static java.awt.Toolkit awtToolkit() {
        return java.awt.Toolkit.getDefaultToolkit();
    }

    /** Returns the AWT Frame that contains the RootView
      *
      */
    public static java.awt.Frame awtFrameForRootView(RootView rootView) {
        return rootView.panel().frame();
    }
}

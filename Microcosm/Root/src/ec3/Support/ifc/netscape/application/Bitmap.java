// Bitmap.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.net.URL;


/** Image subclass that draws bitmapped data.  Typically, you retrieve a
  * Bitmap by name from a GIF file:
  * <pre>
  *     bitmap = Bitmap.bitmapNamed("ColorBackground.gif");
  * </pre>
  * You can also use a Bitmap for offscreen drawing.  You can construct a
  * Graphics object to draw into a Bitmap using the code:
  * <pre>
  *     bitmap = new Bitmap(width, height);
  *     g = new Graphics(bitmap);
  * </pre>
  * @note 1.0 added alert images and keyboard arrow images to system images
  * @note 1.0 added primative namedBitmap() to take cache flag, changed
  *           conviences to call new primative. This will allow users to
  *           create images that will be released.
  * @note 1.0 getPixels will block until finished instead of 40ms timeout
  */
public class Bitmap extends Image {
    java.awt.Image      awtImage;
    BitmapObserver      bitmapObserver;
    Target              updateTarget;
    Rect                updateRect;
    String              name, updateCommand;
    int                 imageNumber;
    boolean             loaded = false, valid = true, transparent = true,
                        loadIncrementally, added;
    java.awt.MediaTracker mediaTracker;

    final static int    WIDTH = 0, HEIGHT = 1;

    // Codable information

    private static Class bitmapClass;

    final static String NAME_KEY = "name";
    final static String UPDATE_TARGET_KEY = "updateTarget";
    final static String UPDATE_COMMAND_KEY = "updateCommand";
    final static String TRANSPARENT_KEY = "transparent";
    final static String LOAD_INCREMENTALLY_KEY = "loadIncrementally";

/* static methods */

    /** Returns the Bitmap named <b>bitmapName</b>. The application maintains
      * a cache of named Bitmaps that it checks first.  If not located in
      * the cache, this method looks for the specified bitmap in the "images"
      * directory in the same directory as the Application's index.html
      * file.  In other words, it constructs the URL
      * <pre>
      *     "codebase"/images/bitmapName
      * </pre>
      * and attempts to load this image.  <b>bitmapName</b> can specify a
      * file name or path, such as "newImages/ColorBackground.gif".
      * This method starts loading the bitmap's data if <b>startLoading</b> is
      * <b>true</b>.  Otherwise, the IFC retrieves the data when needed.
      * If <b>cache</b> is true the bitmap will be added to an Application
      * cache for faster future lookups. Images placed in the cache are not
      * released for the lifetime of the Application object. If <b>cache</b>
      * is false, the image will not be placed in the application cache, and
      * it will be released in the normal manner.
      *
      */
    public static synchronized Bitmap bitmapNamed(String bitmapName,
                                                  boolean startLoading,
                                                  boolean cache) {
        Application app;
        Bitmap bitmap;
        URL url;

        if (bitmapName == null || bitmapName.equals(""))
            return null;

        app = application();
        bitmap = (Bitmap)app.bitmapByName.get(bitmapName);

        if (bitmap != null)
            return bitmap;

        // ALERT!  This is here until we get resources straight.
        bitmap = systemBitmapNamed(bitmapName);
        if (bitmap != null) {
            if(cache)   {
                app.bitmapByName.put(bitmapName, bitmap);
                // If we are caching, we can use the shared media tracker,
                // because the image can not be released from bitmapByName
                // hashtable.
                bitmap.mediaTracker = Application.application().mediaTracker();
            }
            bitmap.name = bitmapName;
            return bitmap;
        }

        url = app._appResources.urlForBitmapNamed(bitmapName);
        bitmap = bitmapFromURL(url);

        if (bitmap == null)
            return null;

        if(cache)   {
            app.bitmapByName.put(bitmapName, bitmap);
            // If we are caching, we can use the shared media tracker,
            // because the image can not be released from bitmapByName
            // hashtable.
            bitmap.mediaTracker = Application.application().mediaTracker();
        }
        bitmap.name = bitmapName;

        /* start loading the bitmap */
        if (startLoading) {
            bitmap.startLoadingData();
        }

        return bitmap;
    }


    /** Returns the Bitmap named <b>bitmapName</b>. The application maintains
      * a cache of named Bitmaps that it checks first.  If not located in
      * the cache, this method looks for the specified bitmap in the "images"
      * directory in the same directory as the Application's index.html
      * file.  In other words, it constructs the URL
      * <pre>
      *     "codebase"/images/bitmapName
      * </pre>
      * and attempts to load this image.  <b>bitmapName</b> can specify a
      * file name or path, such as "newImages/ColorBackground.gif".
      * This method starts loading the bitmap's data if <b>startLoading</b> is
      * <b>true</b>.  Otherwise, the IFC retrieves the data when needed.
      * This call is equivelent to:
      * <PRE>
      *      Bitmap.bitmapNamed(bitmapName, startLoading, true);
      * </PRE>
      * @see #bitmapNamed(String, boolean, boolean)
      */
    public static Bitmap bitmapNamed(String bitmapName,
                                                  boolean startLoading) {
        return bitmapNamed(bitmapName, startLoading, true);
    }

    /** Convenience method for retrieving the bitmap <b>bitmapName</b>.
      * Immediately begins loading its data.  Equivalent to the code:
      * <pre>
      *     Bitmap.bitmapNamed(bitmapName, true, true);
      * </pre>
      * @see #bitmapNamed(String, boolean, boolean)
      */
    public static Bitmap bitmapNamed(String bitmapName) {
        return bitmapNamed(bitmapName, true, true);
    }

    /** Returns a Bitmap initialized with data from the URL <b>url</b>.
      */
    public static Bitmap bitmapFromURL(URL url) {
        java.awt.Image awtImage;
        Bitmap bitmap;

        awtImage = AWTCompatibility.awtApplet().getImage(url);

        if (awtImage == null)
            return null;

        bitmap = new Bitmap(awtImage);

        return bitmap;
    }

    /** Returns a graphics object that can be used to draw into the Bitmap.
      * The <b>dispose()</b> method should be called on this object when no
      * longer needed, to immediately free its
      * resources.  Bitmap subclasses can override this method to
      * provide callers a Graphics subclass instance.
      */
    public Graphics createGraphics() {
        return Graphics.newGraphics(this);
    }

/* constructors */


    /** Constructs a Bitmap with no name and no data. This method is only
      * useful when decoding.
      */
    public Bitmap() {
        super();

        imageNumber = application().nextBitmapNumber();
    }

    /** Constructs a Bitmap of size (<b>width</b>, <b>height</b>) with blank
      * contents. Typically, you use Bitmaps with no contents for offscreen
      * composition.
      */
    public Bitmap(int width, int height) {
        this();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid bitmap size: " +
                                               width + "x" + height);
        }
        awtImage = AWTCompatibility.awtApplet().createImage(width, height);
        setLoaded(true);
    }

    Bitmap(java.awt.Image awtImage) {
        this();
        this.awtImage = awtImage;
    }

    /** Constructs a Bitmap of size (<b>width</b>, <b>height</b>) with
      * contents derived from the integer array <b>pixels</b>. Equivalent to
      * the code:
      * <pre>
      *     Bitmap(pixels, width, height, 0, width)
      * </pre>
      * @see #Bitmap(int[], int, int, int, int)
      */
    public Bitmap(int pixels[], int width, int height) {
        this(pixels, width, height, 0, width);
    }

    /** Constructs a Bitmap of size (<b>width</b>, <b>height</b>) with
      * contents derived from the integer array <b>pixels</b>. <b>offset</b>
      * indicates the position of the first pixel in the array. <b>scanSize</b>
      * indicates the number of pixels in the array per line.<p>
      * The format follows the default Java color model, where each pixel is a
      * 32-bit integer. Bits 24-31 are the alpha transparency, bits 16-23 are
      * the red value, bits 8-15 are the green value, and bits 0-7 are the blue
      * value.
      */
    public Bitmap(int pixels[], int width, int height, int offset,
                  int scanSize) {
        this();

        java.awt.image.MemoryImageSource   src;
        java.awt.Image                     image;

        src = new java.awt.image.MemoryImageSource(width, height, pixels,
                                                   offset, scanSize);
        awtImage = AWTCompatibility.awtApplet().createImage(src);
        setLoaded(true);
    }

    /** Fills the integer array <b>pixels</b> with 32-bit pixel values
      * for the entire Bitmap. The array should be large enough to hold
      * (<b>width()</b> * <b>height()</b>) values. Equivalent to the code:
      * <pre>
      *     grabPixels(pixels, 0, 0, width(), height(), 0, width());
      * </pre>
      * Returns <b>true</b> on success, <b>false</b> on failure.
      * @see #Bitmap(int[], int, int, int, int)
      * @see #grabPixels(int[], int, int, int, int, int, int)
      */
    public boolean grabPixels(int pixels[]) {
        return grabPixels(pixels, 0, 0, width(), height(), 0, width());
    }

    /** Fills the integer array <b>pixels</b> with 32-bit pixel values
      * for the Bitmap. The array should be large enough to hold (<b>width</b>
      * <b>height</b>) values. <b>x</b> and <b>y</b> determine the origin of
      * the rectangle of pixels to retrieve from the Bitmap, relative to the
      * default (unscaled) size of the Bitmap. <b>width</b> and <b>height</b>
      * indicate the size of the rectangle. <b>offset</b> indicates how far
      * into the array the first pixel should be stored. <b>scanSize</b> is the
      * distance from the start of one row of pixels to the start of the next
      * row in the array. The pixel format follows the default Java RGB color
      * model.<p>
      * Returns <b>true</b> on success, <b>false</b> on failure.
      * @see #Bitmap(int[], int, int, int, int)
      */
    public boolean grabPixels(int pixels[], int x, int y, int width,
                              int height, int offset, int scanSize) {
        java.awt.image.PixelGrabber  grabber;
        java.awt.Image               image;
        boolean                      status;

        image = AWTCompatibility.awtImageForBitmap(this);
        grabber = new java.awt.image.PixelGrabber(image, x, y, width, height,
                                                  pixels, offset, scanSize);
        try {
            status = grabber.grabPixels();
        } catch (InterruptedException e) {
            status = false;
        }
        return status;
    }

    /** Returns the name used to load the Bitmap. Returns <b>null</b>
      * if the Bitmap was not obtained via <b>Bitmap.bitmapNamed()</b>.
      * @see #bitmapNamed(String)
      */
    public String name() {
        return name;
    }

    BitmapObserver bitmapObserver() {
        if (bitmapObserver == null) {
            bitmapObserver = new BitmapObserver(application(), this);
        }
        return bitmapObserver;
    }

    int getWidthOrHeight(int dimension) {
        BitmapObserver  observer;
        int             value = -1;
        boolean         done = false;

        if (dimension != WIDTH && dimension != HEIGHT) {
            throw new IllegalArgumentException("Invalid dimension request: " +
                                               dimension);
        }

        if (hasLoadedData()) {
            if (dimension == WIDTH) {
                return awtImage.getWidth(null);
            } else {
                return awtImage.getHeight(null);
            }
        }

//      startLoadingData();

        observer = bitmapObserver();

        synchronized(observer) {
            while (!done) {
                /// ALERT!
                /// Calling getWidth/Height twice here is intentional.
                /// Something has changed with the JDK 1.1.1 release,
                /// regarding MemoryImageSource. In the past, they would
                /// always return a valid value, and we would never call
                /// the wait(). Under JDK 1.1.1 they seem to return -1
                /// the first time and then never notify the observer that
                /// anything has changed. So the original code here got
                /// stuck in the wait() call and was never interupted.
                /// Calling these methods twice was the 'safest' fix we
                /// could find at this time. Investigate further and
                /// determine the true problem and solution.
                if (dimension == WIDTH) {
                    value = awtImage.getWidth(observer);
                    value = awtImage.getWidth(observer);
                } else {
                    value = awtImage.getHeight(observer);
                    value = awtImage.getHeight(observer);
                }
                if (value != -1 || !isValid()) {
                    break;
                }

                if ((observer.lastInfo & ImageObserver.ERROR) != 0 ||
                    (observer.lastInfo & ImageObserver.ABORT) != 0) {
                    valid = false;
                    reportWhyInvalid();
                    setLoaded(true);
                } else  {
                    try {
                        observer.wait();
                    } catch (java.lang.InterruptedException e) {
                    }
                    /* are we valid? */
                    if ((observer.lastInfo & ImageObserver.ERROR) != 0 ||
                        (observer.lastInfo & ImageObserver.ABORT) != 0) {
                        valid = false;
                        reportWhyInvalid();
                        setLoaded(true);
                    }
                }
            }
        }

        return value;
    }

    /** Returns the Bitmap's width.  Begins loading the Bitmap's data, if
      * necessary, returning once the Bitmap's size information becomes
      * available.
      */
    public int width() {
        return getWidthOrHeight(WIDTH);
    }

    /** Returns the Bitmap's height.  Begins loading the Bitmap's data, if
      * necessary, returning once the Bitmap's size information becomes
      * available.
      */
    public int height() {
        return getWidthOrHeight(HEIGHT);
    }

    /** Sets whether the Bitmap is transparent.
      * @see #isTransparent
      */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    /** Returns <b>true</b> if the Bitmap is transparent.  This method does
      * <i>not</i> check the Bitmap's data.  A Bitmap assumes itself
      * transparent unless modified using the <b>setTransparent()</b> method.
      * Returning <b>true</b> is always safe, but an Image user may
      * be able to avoid drawing the region under the Image if this method
      * returns <b>false</b>.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Draws the Bitmap at (<b>x</b>, <b>y</b>). */
    public void drawAt(Graphics g, int x, int y) {
        g.drawBitmapAt(this, x, y);
    }

    /** Draws the Bitmap scaled to fit the supplied rectangle.
      */
    public void drawScaled(Graphics g, int x, int y, int width, int height) {
        g.drawBitmapScaled(this, x, y, width, height);
    }

    /** Overridden to generate an error message just like the other Image
      * drawing methods do when confronted with in invalid bitmap.
      * @private
      */
    public void drawTiled(Graphics g, int x, int y, int width, int height) {
        if (!isValid()) {
            System.err.println("Graphics.drawBitmapTiled() - Invalid bitmap: "+
                               name());
            return;
        }

        super.drawTiled(g, x, y, width, height);
    }

    /** Renders a scaled version of the Bitmap to
      * have width <b>newWidth</b> and height <b>newHeight</b>,
      * retrieving the Bitmap's original data if it has not already
      * done so.  In general, you will never need to call this method.
      * Instead, you'll call the Bitmap's <b>drawScaled()</b>, and the Bitmap
      * will ensure that an appropriately-scaled version exists.  Returns
      * <b>true</b> if the Bitmap's data was successfully
      * loaded and scaled, <b>false</b> if there was a problem.
      */
    boolean createScaledVersion(int newWidth, int newHeight) {
        Application     application;
        BitmapObserver  observer;

        if (!isValid()) {
            return false;
        }

        application = application();
        observer = bitmapObserver();

        while (!application.applet.prepareImage(awtImage, newWidth, newHeight,
                                                                observer)) {
            if (loadsIncrementally()) {
                return true;
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
            }
        }

/*      observer = bitmapObserver(true);
        synchronized(observer) {
            while (!application.applet.prepareImage(awtImage, newWidth,
                                                    newHeight, observer)) {
                if (loadsIncrementally() || observer.allBitsPresent() ||
                    observer.imageHasProblem()) {
                    return true;
                }

                try {
                    observer.wait();
                } catch (java.lang.InterruptedException e) {
                }
            }
        }*/

        return true;
    }

    /** Convenience method for rendering a scaled version of the Bitmap,
      * scaling its width by <b>scaleX</b> and height by <b>scaleY</b>.
      * Equivalent to the code:
      * <pre>
      *     createScaledVersion((int)(scaleX * width()),
      *                         (int)(scaleY * height()));
      * </pre>
      * @see #createScaledVersion
      * @return <b>true</b> if the Bitmap's data was successfully
      * loaded and scaled, <b>false</b> if there was a problem.
      */
    boolean createScaledVersion(float scaleX, float scaleY) {
        return createScaledVersion((int)(scaleX * width()),
                                   (int)(scaleY * height()));
    }

    void startLoadingData() {
        MediaTracker    tracker;

        tracker = tracker();

        /* add to the list */
        if (!added) {
            tracker.addImage(awtImage, imageNumber);
            added = true;
        }

        /* start loading its data */
        tracker.checkID(imageNumber, true);
//      valid = !tracker.isErrorID(imageNumber);
    }

    /** Begins the loading of the Bitmap's data. This method does not return
      * until the data has been loaded, unless the Bitmap is set to be loaded
      * incrementally. In general, you never call this method.
      * Any methods requiring the Bitmap's data will automatically call
      * <b>loadData()</b>.
      */
    public void loadData() {
        MediaTracker    tracker;

        if (loaded) {
            return;
        }

        tracker = tracker();

        while (!loaded) {
            try {
                startLoadingData();

                /* bail out if we don't want to block */
                if (loadIncrementally) {
                    break;
                }

                tracker.waitForID(imageNumber);
                setLoaded(true);
            } catch (InterruptedException e) {
                System.err.println("Bitmap.loadData() - " + e);
                // valid = false;
            }
        }

        if (valid) {
            valid = !tracker.isErrorID(imageNumber);
        }

        if (!valid) {
            reportWhyInvalid();
            setLoaded(true);
        }
    }

    void setLoaded(boolean flag) {
        loaded = flag;
    }

    /** Returns <b>true</b> if the Bitmap attempted to load its data and
      * succeeded.
      * @see #loadData
      */
    public boolean hasLoadedData() {
        return loaded;
    }

    /** Configures the Bitmap to load and display its data incrementally.
      * An incrementally-loaded Bitmap notifies its update Target when
      * additional data becomes available, and when drawn, draws all available
      * data rather than waiting until all data is present.
      * @see #setUpdateTarget
      */
    public void setLoadsIncrementally(boolean flag) {
        loadIncrementally = flag;
    }

    /** Returns <b>true</b> if the Bitmap's data will load incrementally.
      * @see #setLoadsIncrementally
      */
    public boolean loadsIncrementally() {
        return loadIncrementally;
    }

    /** Returns the Rect defining the newly-available portion of an
      * incrementally-loaded Bitmap.  Returns an empty Rect if the Bitmap
      * is not loading incrementally, or no additional data has become
      * available since this method was most recently called.
      * @see #setLoadsIncrementally
      */
    public synchronized Rect updateRect() {
        Rect    tmpRect;

        if (updateRect == null) {
            tmpRect = new Rect();
        } else {
            tmpRect = updateRect;
            updateRect = null;
        }

        return tmpRect;
    }

    /** Sets the Target that should receive a command when the
      * incrementally-loaded Bitmap receives additional data.
      * @see #setLoadsIncrementally
      * @see #setUpdateCommand
      */
    public synchronized void setUpdateTarget(Target aTarget) {
        updateTarget = aTarget;
    }

    /** Returns the Bitmap's update target.
      * @see #setUpdateTarget
      */
    public synchronized Target updateTarget() {
        return updateTarget;
    }

    /** Sets the command sent to an incrementally-loaded Bitmap's update Target
      * when additional data becomes available.
      * @see #setUpdateTarget
      */
    public synchronized void setUpdateCommand(String command) {
        updateCommand = command;
    }

    /** Returns the Bitmap's update command.
      * @see #setUpdateCommand
      */
    public synchronized String updateCommand() {
        return updateCommand;
    }

    void reportWhyInvalid() {
        String          message = "";
        int             status;

        status = tracker().statusID(imageNumber, false);

        if ((status & MediaTracker.ABORTED) != 0) {
            message = message + " ABORTED";
        } else if ((status & MediaTracker.COMPLETE) != 0) {
            message = message + " COMPLETE";
        } else if ((status & MediaTracker.ERRORED) != 0) {
            message = message + " ERRORED";
        } else if ((status & MediaTracker.LOADING) != 0) {
            message = message + " LOADING";
        }

        System.err.println("Invalid bitmap: " + name() + message);
    }

    /** Returns <b>true</b> if the Bitmap's data was successfully loaded
      * and is valid.
      * @see #loadData
      */
    public boolean isValid() {
// ALERT! - this method doesn't care if we've tried to load data or not!
        return valid;
    }

    /** Flushes all resources allocated to the Bitmap, including cached
      * pixel data and system resources.
      */
    public void flush() {
        awtImage.flush();
    }

    /** Returns the Bitmap's String representation.
      */
    public String toString() {
        if (name != null) {
            return "Bitmap(" + name + ")";
        } else
            return super.toString();
    }

    /** Describes the Bitmap class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.Bitmap", 1);
        info.addField(NAME_KEY, STRING_TYPE);
        info.addField(UPDATE_TARGET_KEY, OBJECT_TYPE);
        info.addField(UPDATE_COMMAND_KEY, STRING_TYPE);
        info.addField(TRANSPARENT_KEY, BOOLEAN_TYPE);
        info.addField(LOAD_INCREMENTALLY_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the Bitmap.  For now, Bitmaps are only encodable by name.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        if (name == null) {
            throw new CodingException("encoded Bitmaps must have a name");
        }

        encoder.encodeString(NAME_KEY, name);
        encoder.encodeObject(UPDATE_TARGET_KEY, (Codable)updateTarget);
        encoder.encodeString(UPDATE_COMMAND_KEY, updateCommand);
        encoder.encodeBoolean(TRANSPARENT_KEY, transparent);
        encoder.encodeBoolean(LOAD_INCREMENTALLY_KEY, loadIncrementally);
    }

    /** Decodes the Bitmap.  For now, this simply decodes the name
      * and calls <b>Bitmap.bitmapNamed()</b>.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        Bitmap tmp;

        super.decode(decoder);

        name = decoder.decodeString(NAME_KEY);
        if (name == null) {
            throw new CodingException("encoded Bitmaps must have a name");
        }

        // ALERT!  These are not preserved during the replacement.

        updateTarget = (Target)decoder.decodeObject(UPDATE_TARGET_KEY);
        updateCommand = decoder.decodeString(UPDATE_COMMAND_KEY);
        transparent = decoder.decodeBoolean(TRANSPARENT_KEY);
        loadIncrementally = decoder.decodeBoolean(LOAD_INCREMENTALLY_KEY);

        tmp = bitmapNamed(name);
        if (tmp == null)
            throw new CodingException("unable to find bitmap named: " + name);

        // Only replace with the shared Bitmap if this is not a subclass of
        // Bitmap.

        // ALERT!  How does this play with incremental loading?

        if (getClass() == bitmapClass()) {
            decoder.replaceObject(tmp);
        } else {
            awtImage = tmp.awtImage;
        }
    }

    private static Class bitmapClass() {
        if (bitmapClass == null)
            bitmapClass = new Bitmap().getClass();

        return bitmapClass;
    }

    private static Application application() {
        return Application.application();
    }

    // This crud is slammed into Bitmap to avoid bringing in the class
    // SystemImages unless we actually need a bitmap from there.

    private static Bitmap systemBitmapNamed(String name) {
        Bitmap bitmap = null;
        String systemName;

        if (name == null)
            return null;
        else if (!name.startsWith("netscape/application/"))
            return null;
        else {
            systemName = name.substring("netscape/application/".length());
            if (systemName.equals("RedGrad.gif"))
                bitmap = SystemImages.redGrad();
            else if (systemName.equals("GreenGrad.gif"))
                bitmap = SystemImages.greenGrad();
            else if (systemName.equals("BlueGrad.gif"))
                bitmap = SystemImages.blueGrad();
            else if (systemName.equals("CheckMark.gif"))
                bitmap = SystemImages.checkMark();
            else if (systemName.equals("CloseButton.gif"))
                bitmap = SystemImages.closeButton();
            else if (systemName.equals("CloseButtonActive.gif"))
                bitmap = SystemImages.closeButtonActive();
            else if (systemName.equals("ColorScrollKnob.gif"))
                bitmap = SystemImages.colorScrollKnob();
            else if (systemName.equals("PopupKnob.gif"))
                bitmap = SystemImages.popupKnob();
            else if (systemName.equals("PopupKnobH.gif"))
                bitmap = SystemImages.popupKnobH();
            else if (systemName.equals("RadioButtonOff.gif"))
                bitmap = SystemImages.radioButtonOff();
            else if (systemName.equals("RadioButtonOn.gif"))
                bitmap = SystemImages.radioButtonOn();
            else if (systemName.equals("ResizeLeft.gif"))
                bitmap = SystemImages.resizeLeft();
            else if (systemName.equals("ResizeRight.gif"))
                bitmap = SystemImages.resizeRight();
            else if (systemName.equals("ScrollDownArrow.gif"))
                bitmap = SystemImages.scrollDownArrow();
            else if (systemName.equals("ScrollDownArrowActive.gif"))
                bitmap = SystemImages.scrollDownArrowActive();
            else if (systemName.equals("ScrollKnobH.gif"))
                bitmap = SystemImages.scrollKnobH();
            else if (systemName.equals("ScrollKnobV.gif"))
                bitmap = SystemImages.scrollKnobV();
            else if (systemName.equals("ScrollLeftArrow.gif"))
                bitmap = SystemImages.scrollLeftArrow();
            else if (systemName.equals("ScrollLeftArrowActive.gif"))
                bitmap = SystemImages.scrollLeftArrowActive();
            else if (systemName.equals("ScrollRightArrow.gif"))
                bitmap = SystemImages.scrollRightArrow();
            else if (systemName.equals("ScrollRightArrowActive.gif"))
                bitmap = SystemImages.scrollRightArrowActive();
            else if (systemName.equals("ScrollTrayBottom.gif"))
                bitmap = SystemImages.scrollTrayBottom();
            else if (systemName.equals("ScrollTrayLeft.gif"))
                bitmap = SystemImages.scrollTrayLeft();
            else if (systemName.equals("ScrollTrayRight.gif"))
                bitmap = SystemImages.scrollTrayRight();
            else if (systemName.equals("ScrollTrayTop.gif"))
                bitmap = SystemImages.scrollTrayTop();
            else if (systemName.equals("ScrollUpArrow.gif"))
                bitmap = SystemImages.scrollUpArrow();
            else if (systemName.equals("ScrollUpArrowActive.gif"))
                bitmap = SystemImages.scrollUpArrowActive();
            else if (systemName.equals("TitleBarLeft.gif"))
                bitmap = SystemImages.titleBarLeft();
            else if (systemName.equals("TitleBarRight.gif"))
                bitmap = SystemImages.titleBarRight();
            else if (systemName.equals("alertNotification.gif"))
                bitmap = SystemImages.alertNotification();
            else if (systemName.equals("alertQuestion.gif"))
                bitmap = SystemImages.alertQuestion();
            else if (systemName.equals("alertWarning.gif"))
                bitmap = SystemImages.alertWarning();
            else if (systemName.equals("topLeftArrow.gif"))
                bitmap = SystemImages.topLeftArrow();
            else if (systemName.equals("topRightArrow.gif"))
                bitmap = SystemImages.topRightArrow();
            else if (systemName.equals("bottomRightArrow.gif"))
                bitmap = SystemImages.bottomRightArrow();
            else if (systemName.equals("bottomLeftArrow.gif"))
                bitmap = SystemImages.bottomLeftArrow();
        }

        return bitmap;
    }

    /** Overridden to return the Bitmap with name <b>name</b>.  If you want
      * to locate a Bitmap by name, call the static <b>Bitmap.bitmapNamed()</b>
      * method.
      * @see #bitmapNamed
      * @see Image#imageNamed
      * @private
      */
    public Image imageWithName(String name) {
        return Bitmap.bitmapNamed(name);
    }

    synchronized void unionWithUpdateRect(int x, int y, int width,
                                          int height) {
        if (updateRect == null) {
            updateRect = new Rect(x, y, width, height);
        } else {
            updateRect.unionWith(x, y, width, height);
        }
    }

    java.awt.MediaTracker tracker() {
        if(mediaTracker == null)
            mediaTracker = new java.awt.MediaTracker(AWTCompatibility.awtApplet());
        return mediaTracker;
    }

}

// ImageSequence.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** DrawingSequence subclass that animates a collection of Images (Bitmaps or
  * other).  You can add one or more Images to an ImageSequence by hand, or
  * if you're working with a number of Bitmaps that share the same name except
  * for a number suffix (i.e. Ball0, Ball1, Ball2, etc.), you can provide
  * the ImageSequence with the first Bitmap name and a count, and the
  * ImageSequence will load each Bitmap itself.  ImageSequences can also
  * produce an animation from a single Image that contains a number of frames
  * (oriented either vertically or horizontally).  Given an "Image strip" Image
  * and a frame width or height, the ImageSequence will treat and display
  * portions of the single Image as individual frames.
  */

public class ImageSequence extends DrawingSequence {
    Vector      imageVector;
    Image       imageStrip;
    int         frameWidth, frameHeight;

    final static String         VECTOR_KEY = "imageVector",
                                IMAGESTRIP_KEY = "imageStrip",
                                FRAMEWIDTH_KEY = "frameWidth",
                                FRAMEHEIGHT_KEY = "frameHeight";



    /* constructors */

    /** Constructs an ImageSequence without an owner.
      */
    public ImageSequence() {
        super();
    }

    /** Constructs an ImageSequence with owner <B>owner</B>, a frame
      * rate of 1 millisecond, current frame number of 0, and a direction of
      * FORWARD.
      */
    public ImageSequence(DrawingSequenceOwner owner) {
        super(owner);
    }

    /** Returns the ImageSequence's collection of Images. */
    public Vector images() {
        return imageVector;
    }

    /** Adds <b>anImage</b> to the ImageSequence's collection of Images. */
    public void addImage(Image anImage) {
        if (imageVector == null) {
            imageVector = new Vector();
        }

        imageVector.addElement(anImage);
        frameCount = imageVector.count();
    }

    /** Adds a sequence of Bitmaps with the same name but different number
      * suffixes, such as Ball0, Ball1, Ball2, and so on.
      * <b>firstImageName</b> should be the full name of the first Bitmap
      * (Ball0, in this case), and <b>count</b> should be the total number of
      * images to add, including the first one.  If the Images are named
      * "Ball0," "Ball1," "Ball2," and "Ball3," <b>count</b> should be 4.
      * Calls <b>Bitmap.bitmapNamed()</b> to locate the named Bitmaps.
      */
    public void addImagesFromName(String firstImageName, int count) {
        Application     application;
        String          namePrefix, fileType;
        int             periodIndex, i, nextDigitIndex, startingNumber,
                        columnOffset;

        if (firstImageName == null || count < 0) {
            return;
        }

        application = Application.application();

        periodIndex = firstImageName.lastIndexOf('.');
        if (periodIndex == -1) {
            addImage(Bitmap.bitmapNamed(firstImageName));
            return;
        }
        fileType = firstImageName.substring(periodIndex);

        nextDigitIndex = periodIndex - 1;
        startingNumber = 0;
        columnOffset = 1;
        while (nextDigitIndex > 0 &&
               Character.isDigit(firstImageName.charAt(nextDigitIndex))) {
            startingNumber += columnOffset *
                        Character.digit(firstImageName.charAt(nextDigitIndex),
                                        10);

            columnOffset *= 10;
            nextDigitIndex--;
        }

        namePrefix = firstImageName.substring(0, nextDigitIndex + 1);

        if (imageVector == null) {
            imageVector = new Vector();
        }

        count += startingNumber;
        for (i = startingNumber; i < count; i++) {
            imageVector.addElement(Bitmap.bitmapNamed(
                                                namePrefix + i + fileType));
        }

        frameCount = imageVector.count();
    }

    /** Removes <b>anImage</b> from the ImageSequence's collection of
      * Images.
      */
    public void removeImage(Image anImage) {
        if (imageVector != null) {
            imageVector.removeElement(anImage);
            frameCount = imageVector.count();
        }
    }

    /** Empties the ImageSequence of Images and clears its Image strip.
      */
    public void removeAllImages() {
        if (imageVector != null) {
            imageVector.removeAllElements();
        }
        imageStrip = null;
        currentFrameNumber = frameCount = 0;
    }

    /** Sets the Image from which the ImageSequence should extract individual
      * frames.  You set the strip's orientation and frame size through the
      * <b>setFrameWidth()</b> or <b>setFrameHeight()</b> methods.
      * @see #setFrameWidth
      * @see #setFrameHeight
      */
    public void setImageStrip(Image anImage) {
        imageStrip = anImage;
        currentFrameNumber = frameCount = 0;
    }

    /** Returns the ImageSequence's Image strip.
      * @see #setImageStrip
      */
    public Image imageStrip() {
        return imageStrip;
    }

    /** Configures the ImageSequence to extract frames horizontally, assuming a
      * frame width of <b>pixels</b>.
      * @see #setImageStrip
      */
    public void setFrameWidth(int pixels) {
        if (pixels < 0) {
            pixels = 0;
        }

        frameWidth = pixels;
        if (frameWidth > 0 && imageStrip != null) {
            frameCount = imageStrip.width() / frameWidth;
        } else {
            currentFrameNumber = frameCount = 0;
        }
    }

    /** Returns the ImageSequence's frame width.
      * @see #setFrameWidth
      */
    public int frameWidth() {
        return frameWidth;
    }

    /** Configures the ImageSequence to extract frames vertically, assuming a
      * frame height of <b>pixels</b>.
      * @see #setImageStrip
      */
    public void setFrameHeight(int pixels) {
        if (pixels < 0) {
            pixels = 0;
        }

        frameHeight = pixels;
        if (frameHeight > 0 && imageStrip != null) {
            frameCount = imageStrip.height() / frameHeight;
        } else {
            currentFrameNumber = frameCount = 0;
        }
    }

    /** Returns the ImageSequence's frame height.
      * @see #setFrameHeight
      */
    public int frameHeight() {
        return frameHeight;
    }

    /** Returns the number of Images (or frames, in the case of an Image
      * strip) that the ImageSequence will display.
      */
    public int imageCount() {
        return frameCount();
    }

    /** Sets the ImageSequence to display Image number <b>imageNumber</b>.
      */
    public void setCurrentImageNumber(int imageNumber) {
        if (imageNumber < 0) {
            imageNumber = 0;
        } else if (imageNumber >= frameCount) {
            imageNumber = frameCount - 1;
        }

        currentFrameNumber = imageNumber;
    }

    /** Returns the current Image being displayed by the ImageSequence, or
      * its entire Image strip if configured to use one.
      */
    public Image currentImage() {
        if (imageVector != null) {
            return (Image)imageVector.elementAt(currentFrameNumber);
        }

        return imageStrip;
    }

    /** Returns the unioned Size of the ImageSequence's Images (the minimum
      * area required to fully display each Image).
      */
    public Size maxSize() {
        Image        nextImage;
        int          i, maxWidth, maxHeight;

        if (imageStrip != null) {
            if (frameWidth > 0) {
                return new Size(frameWidth, imageStrip.height());
            } else if (frameHeight > 0) {
                return new Size(imageStrip.width(), frameHeight);
            }
        } else if (imageVector != null && !imageVector.isEmpty()) {
            maxWidth = maxHeight = 0;
            i = imageVector.count();
            while (i-- > 0) {
                nextImage = (Image)imageVector.elementAt(i);
                if (nextImage.width() > maxWidth) {
                    maxWidth = nextImage.width();
                }
                if (nextImage.height() > maxHeight) {
                    maxHeight = nextImage.height();
                }
            }

            return new Size(maxWidth, maxHeight);
        }

        return new Size(0, 0);
    }

    /** Returns the width of the ImageSequence's tallest Image.
      */
    public int width() {
        Image        nextImage;
        int          i, maxWidth = 0;

        if (imageStrip != null) {
            if (frameWidth > 0) {
                return frameWidth;
            } else {
                return imageStrip.width();
            }
        }

        if (imageVector == null) {
            return 0;
        }

        i = imageVector.count();
        while (i-- > 0) {
            nextImage = (Image)imageVector.elementAt(i);
            if (nextImage.width() > maxWidth) {
                maxWidth = nextImage.width();
            }
        }

        return maxWidth;
    }

    /** Returns the height of the ImageSequence's tallest Image.
      */
    public int height() {
        Image        nextImage;
        int             i, maxHeight = 0;

        if (imageStrip != null) {
            if (frameHeight > 0) {
                return frameHeight;
            } else {
                return imageStrip.height();
            }
        }

        if (imageVector == null) {
            return 0;
        }

        i = imageVector.count();
        while (i-- > 0) {
            nextImage = (Image)imageVector.elementAt(i);
            if (nextImage.height() > maxHeight) {
                maxHeight = nextImage.height();
            }
        }

        return maxHeight;
    }

    /** Draws the ImageSequence's current Image at (<b>x</b>, <b>y</b>).
      */
    public void drawAt(Graphics g, int x, int y) {
        Image        nextImage;
        Rect         tmpRect;

        if (imageVector != null) {
            nextImage = (Image)imageVector.elementAt(currentFrameNumber);
            if (nextImage != null) {
                // This might not work the way you want if the images have
                // different sizes.  ALERT!
                nextImage.drawAt(g, x, y);
            }
        } else if (frameWidth > 0) {
            tmpRect = Rect.newRect(x, y, frameWidth, imageStrip.height());
            g.pushState();
            g.setClipRect(tmpRect);
            imageStrip.drawAt(g, x - frameWidth * currentFrameNumber, y);
            g.popState();
            Rect.returnRect(tmpRect);
        } else if (frameHeight > 0) {
            tmpRect = Rect.newRect(x, y, imageStrip.width(), frameHeight);
            g.pushState();
            g.setClipRect(tmpRect);
            imageStrip.drawAt(g, x, y - frameHeight * currentFrameNumber);
            g.popState();
            Rect.returnRect(tmpRect);
        }
    }

    /** Draws the ImageSequence's current Image, scaled to fit the supplied
      * rectangle.
      */
    public void drawScaled(Graphics g, int x, int y, int width, int height) {
        Image        nextImage;
        Rect         tmpRect;

        // This doesn't do scaling as you might want.  ALERT!

        if (imageVector != null) {
            nextImage = (Image)imageVector.elementAt(currentFrameNumber);
            if (nextImage != null) {
                nextImage.drawCentered(g, x, y, width, height);
            }
        } else if (frameWidth > 0) {
            x += (width - frameWidth) / 2;
            y += (height - imageStrip.height()) / 2;
            tmpRect = Rect.newRect(x, y, frameWidth, imageStrip.height());
            g.pushState();
            g.setClipRect(tmpRect);
            imageStrip.drawAt(g, x - frameWidth * currentFrameNumber, y);
            g.popState();
            Rect.returnRect(tmpRect);
        } else if (frameHeight > 0) {
            x += (width - imageStrip.width()) / 2;
            y += (height - frameHeight) / 2;
            tmpRect = Rect.newRect(x, y, imageStrip.width(), frameHeight);
            g.pushState();
            g.setClipRect(tmpRect);
            imageStrip.drawAt(g, x, y - frameHeight * currentFrameNumber);
            g.popState();
            Rect.returnRect(tmpRect);
        }
    }

/* archiving */


    /** Describes the ImageSequence class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ImageSequence", 1);
        info.addField(VECTOR_KEY, OBJECT_TYPE);
        info.addField(IMAGESTRIP_KEY, OBJECT_TYPE);
        info.addField(FRAMEWIDTH_KEY, INT_TYPE);
        info.addField(FRAMEHEIGHT_KEY, INT_TYPE);
    }

    /** Encodes the ImageSequence.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(VECTOR_KEY, imageVector);
        encoder.encodeObject(IMAGESTRIP_KEY, imageStrip);

        encoder.encodeInt(FRAMEWIDTH_KEY, frameWidth);
        encoder.encodeInt(FRAMEHEIGHT_KEY, frameHeight);
    }

    /** Decodes the ImageSequence.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        imageVector = (Vector)decoder.decodeObject(VECTOR_KEY);
        imageStrip = (Image)decoder.decodeObject(IMAGESTRIP_KEY);

        frameWidth = decoder.decodeInt(FRAMEWIDTH_KEY);
        frameHeight = decoder.decodeInt(FRAMEHEIGHT_KEY);
    }
}

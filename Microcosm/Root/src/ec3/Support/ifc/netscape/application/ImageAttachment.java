// ImageAttachment.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** TextAttachment subclass for placing an Image among a TextView's characters.
  */
public class ImageAttachment extends TextAttachment implements Codable {
    private Image image;
    boolean incrementalBitmap;
    int     width,height;

    final static String IMAGE_KEY = "image";

    /** Constructs an empty ImageAttachment.
      */
    public ImageAttachment() {
        super();
        image = null;
        incrementalBitmap = false;
    }

    /** Constructs an ImageAttachment containing <b>anImage</b>.
      */
    public ImageAttachment(Image anImage) {
        super();
        image = anImage;
        incrementalBitmap = false;
    }

    /** Constructs an ImageAttachment that loads <b>aBitmap</b>
      * incrementally.
      */
    ImageAttachment(Bitmap aBitmap,int width,int height) {
        super();
        image = aBitmap;
        this.width = width;
        this.height= height;
        incrementalBitmap = true;
    }

    /** Set the ImageAttachment's Image to <b>anImage</b>.
      */
    public void setImage(Image anImage) {
        image = anImage;
    }

    /** Returns the ImageAttachment's Image.
      * @see #setImage
      */
    public Image image() {
        return image;
    }

    /** Overridden to return the ImageAttachment's width.
      */
    public int width() {
        if( incrementalBitmap )
            return width;

        if( image != null )
            return image.width();
        else
            return 0;
    }

    /** Overridden to return the ImageAttachment's height.
      */
    public int height() {
        if( incrementalBitmap )
            return height;

        if( image != null )
            return image.height();
        else
            return 0;
    }

    /** Draws the ImageAttachment's Image.
      */
    public void drawInRect(Graphics g, Rect boundsRect) {
        Rect clipRect;
        if (g == null || boundsRect == null) {
            return;
        }
        clipRect = g.clipRect();
        if( image != null ) {
            image.drawAt(g,boundsRect.x,boundsRect.y);
        }
    }

    /** Describes the ImageAttachment class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.ImageAttachment", 1);
        info.addField(IMAGE_KEY,OBJECT_TYPE);
    }

    /** Encodes the ImageAttachment instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(IMAGE_KEY,(Codable)image);
    }

    /** Decodes the ImageAttachment instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        image = (Image) decoder.decodeObject(IMAGE_KEY);
    }

    /** Finishes the ImageAttachment's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
        incrementalBitmap = false;
    }
}

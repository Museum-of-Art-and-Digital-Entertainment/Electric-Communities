// CheckButtonImage.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Image subclass that draws the images displayed by "check" buttons.
  *
  * @private
  */


public class CheckButtonImage extends Image {
    boolean             drawsCheckMark;

    final static String         DRAWS_CHECK_KEY = "drawsCheckMark";

    private static Bitmap       checkBitmap;

    private Bitmap checkBitmap() {
        if (checkBitmap == null) {
            checkBitmap =
                Bitmap.bitmapNamed("netscape/application/CheckMark.gif");
        }

        return checkBitmap;
    }

    /** Constructs a CheckButtonImage without a check mark. */
    public CheckButtonImage() {
        super();
    }

    /** Constructs a CheckButtonImage that draws or does not draw a check mark.
      */
    public CheckButtonImage(boolean drawsCheckMark) {
        this();

        this.drawsCheckMark = drawsCheckMark;
    }

    /** Sets whether the CheckButtonImage displays a check mark. */
    public void setDrawsCheckMark(boolean flag) {
        drawsCheckMark = flag;
    }

    /** Returns <b>true</b> if the CheckButtonImage displays a check mark. */
    public boolean drawsCheckMark() {
        return drawsCheckMark;
    }

    /** Returns the CheckButtonImage's width. */
    public int width() {
        return 16;
    }

    /** Returns the CheckButtonImage's height. */
    public int height() {
        return 16;
    }

    /** Draws the CheckButtonImage at the given location. */
    public void drawAt(Graphics g, int x, int y) {
        Rect    tmpRect;

        tmpRect = Rect.newRect(x, y, width(), height());

        BezelBorder.raisedButtonBezel().drawInRect(g, tmpRect);
        g.setColor(Color.lightGray);
        g.fillRect(tmpRect.x + 2, tmpRect.y + 2, tmpRect.width - 4,
                   tmpRect.height - 4);
        //Button.drawButton(g, tmpRect, false);

        if (drawsCheckMark) {
            checkBitmap().drawCentered(g, tmpRect);
        }

        Rect.returnRect(tmpRect);
    }

    /** Draws the CheckButtonImage scaled to fit the supplied rectangle.
      */
    public void drawScaled(Graphics g, int x, int y, int width, int height) {
        Rect    tmpRect;

        tmpRect = Rect.newRect(x, y, width, height);

        BezelBorder.raisedButtonBezel().drawInRect(g, tmpRect);
        g.setColor(Color.lightGray);
        g.fillRect(tmpRect.x + 2, tmpRect.y + 2, tmpRect.width - 4,
                   tmpRect.height - 4);
        //Button.drawButton(g, tmpRect, false);

        if (drawsCheckMark) {
            checkBitmap().drawCentered(g, x, y, width, height);
        }

        Rect.returnRect(tmpRect);
    }

    /** Describes the CheckButtonImage class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.CheckButtonImage", 1);
        info.addField(DRAWS_CHECK_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the CheckButtonImage.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeBoolean(DRAWS_CHECK_KEY, drawsCheckMark);
    }

    /** Decodes the CheckButtonImage.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        drawsCheckMark = decoder.decodeBoolean(DRAWS_CHECK_KEY);
    }

    /** Finishes the CheckButtonImage decoding; only calls super.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
    }
}

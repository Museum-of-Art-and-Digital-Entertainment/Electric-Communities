// DragWell.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass that vends drag-and-droppable items.  Unless changed, the
  * DragWell vends the same data each time the user clicks and drags from
  * the DragWell.
  */
public class DragWell extends View implements DragSource {
    Image        image;
    String       dataType;
    Object       data;
    Border       border = BezelBorder.loweredBezel();
    boolean      enabled = true;

    final static String         IMAGE_KEY = "image",
                                DATA_KEY = "data",
                                DATATYPE_KEY = "dataType",
                                ENABLED_KEY = "enabled",
                                BORDER_KEY = "border";


    /* constructors */

    /** Constructs a DragWell with origin (0, 0) and zero width
      * and height
      */
    public DragWell() {
        this(0, 0, 0, 0);
    }

    /** Constructs a DragWell with bounds <B>rect</B>.
      */
    public DragWell(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a DragWell
      * with bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public DragWell(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /** Overriden to return <b>false</b>. */
    public boolean isTransparent() {
        return false;
    }

    /** Sets the DragWell's Image, the Image it displays within its bounds,
      * and then calls its
      * <B>draw()</B> method to redraw it.  If you do not want to immediately
      * redraw the DragWell, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setImage(Image anImage) {
        if (image != anImage) {
            image = anImage;
            draw();
        }
    }

    /** Returns the DragWell's Image.
      * @see #setImage
      */
    public Image image() {
        return image;
    }

    /** Sets the type of the data represented by the DragWell's Image.
      * @see DragSession#setDataType
      */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /** Returns the type of the data represented by the DragWell's Image.
      * @see #setDataType
      */
    public String dataType() {
        return dataType;
    }

    /** Sets the object represented by the DragWell's Image.  When the user
      * clicks the DragWell, the DragWell initiates a DragSession displaying
      * the DragWell's Image and containing the DragWell's data.
      */
    public void setData(Object anObject) {
        data = anObject;
    }

    /** Returns the object represented by the DragWell's Image.
      * @see #setData
      */
    public Object data() {
        return data;
    }

    /** Disables or enables the DragWell, and calls <b>draw()</b> to redraw it.
      * A disabled DragWell does not respond to mouse clicks.
      * If you do not want to immediately
      * redraw the DragWell, you should first call its
      * <B>disableDrawing()</B> method.
      * @see View#disableDrawing
      */
    public void setEnabled(boolean flag) {
        if (enabled != flag) {
            enabled = flag;

            draw();
        }
    }

    /** Returns <B>true</b> if the DragWell is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets the DragWell's Border.
      * @see Border
      */
    public void setBorder(Border newBorder) {
        if (newBorder == null)
            newBorder = EmptyBorder.emptyBorder();

        border = newBorder;
    }

    /** Returns the DragWell's Border.
      * @see #setBorder
      */
    public Border border() {
        return border;
    }

    /** Initiates a DragSession where the user can drag the DragWell's
      * data, unless the DragWell is disabled (in which case nothing
      * happens).
      * @see #setEnabled
      * @return <b>false</b> if disabled, <b>true</b> otherwise.
      */
    public boolean mouseDown(MouseEvent event) {
        Image        theImage;
        Rect         tmpRect;

        if (!enabled) {
            return false;
        }

        theImage = image();
        if (theImage == null) {
            return false;
        }

        tmpRect = new Rect((width() - theImage.width()) / 2,
                           (height() - theImage.height()) / 2,
                           theImage.width(), theImage.height());

        if (!tmpRect.contains(event.x, event.y)) {
            return false;
        }

        new DragSession(this, theImage,
                        tmpRect.x, tmpRect.y,
                        event.x, event.y,
                        dataType(), data);

        return true;
    }

    /** Draws the DragWell by drawing its Border, filled with the DragWell's
      * Image.
      */
    public void drawView(Graphics g) {
        Image        theImage;

        g.setColor(Color.lightGray);
        g.fillRect(0, 0, width(), height());

        theImage = image();
        if (theImage != null) {
            theImage.drawCentered(g, 0, 0, width(), height());
        }

        border.drawInRect(g, 0, 0, width(), height());
    }

/* drag source */

    /** DragSource support method.
      * @see DragSource#sourceView
      */
    public View sourceView(DragSession session) {
        return this;
    }

    /** DragSource support method.
      * @see DragSource#dragAccepted
      */
    public void dragWasAccepted(DragSession session) {
    }

    /** DragSource support method.
      * @see DragSource#dragRejected
      */
    public boolean dragWasRejected(DragSession session) {
        return true;
    }

/* archiving */


    /** Describes the DragWell class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.DragWell", 1);
        info.addField(IMAGE_KEY, OBJECT_TYPE);
        info.addField(DATA_KEY, OBJECT_TYPE);
        info.addField(DATATYPE_KEY, STRING_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
    }

    /** Encodes the DragWell instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(IMAGE_KEY, image);
        encoder.encodeObject(DATA_KEY, (Codable)data);
        encoder.encodeString(DATATYPE_KEY, dataType);

        encoder.encodeBoolean(ENABLED_KEY, enabled);

        if (border instanceof EmptyBorder)
            encoder.encodeObject(BORDER_KEY, null);
        else
            encoder.encodeObject(BORDER_KEY, border);
    }

    /** Decodes the DragWell instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);

        image = (Image)decoder.decodeObject(IMAGE_KEY);
        data = decoder.decodeObject(DATA_KEY);
        dataType = decoder.decodeString(DATATYPE_KEY);

        enabled = decoder.decodeBoolean(ENABLED_KEY);

        setBorder((Border)decoder.decodeObject(BORDER_KEY));
    }
}

// TextAttachment.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;


/** Abstract object subclass for placing items (Images, controls, and so on)
  * among a TextView's characters. Use ImageAttachment to display an Image.
  * Subclass TextAttachment for anything else.
  * @see ImageAttachment
  * @note 1.0 new API for becoming visible
  */
public abstract class TextAttachment implements Codable {
    private TextView    _owner;
    private int         _width, _height;
    private boolean     _visible = false;

    final static String OWNER_KEY = "owner";
    final static String WIDTH_KEY = "width";
    final static String HEIGHT_KEY= "height";

    /** Constructs an empty TextAttachment.
      */
    public void TextAttachment() {
        _owner = null;
        _width = _height = 0;
    }

    /** Set <b>aTextView</b> as the TextAttachment's owner (the TextView that
      * will display this TextAttachment).
     */
    public void setOwner(TextView aTextView) {
        _owner = aTextView;
    }

    /** Returns the TextAttachment's owner.
      */
    public TextView owner() {
        return _owner;
    }

    /** Sets the TextAttachment's pixel width.
      */
    public void setWidth(int width) {
        _width = width;
    }

    /** Returns the TextAttachment's pixel width.
      * @see #setWidth
      */
    public int width() {
        return _width;
    }

    /** Sets the TextAttachment's pixel height.
     */
    public void setHeight(int height) {
        _height = height;
    }

    /** Returns the TextAttachment's pixel height.
      * @see #setHeight
      */
    public int height() {
        return _height;
    }

    /** Method responsible for drawing the TextAttachment's contents
      * within the Graphics <b>g</b>.  <b>boundsRect</b> is the
      * TextAttachment's bounds within its owner's coordinate system.
      */
     public void drawInRect(Graphics g, Rect boundsRect) {
     }

    /** Called by the TextAttachment's owner when the user clicks within
      * the TextAttachment.  This method should return <b>false</b> if the
      * TextAttachment isn't interested in the event, <b>true</b> otherwise.
      */
    public boolean mouseDown(MouseEvent event) {
        return false;
    }

    /** Called by the TextAttachment's owner as the user drags the mouse after
      * having clicked within the TextAttachment.  Only called if
      * <b>mouseDown()</b> returned <b>true</b>.
      */
    public void mouseDragged(MouseEvent event) {
    }

    /** Called by the TextAttachment's owner when the user releases the mouse
      * after having clicked within the TextAttachment.  Only called if
      * <b>mouseDown()</b> returned <b>true</b>.
      */
    public void mouseUp(MouseEvent event) {
    }


    /** Called when the attachment is about to get visible for the first
      * time.
      *
      */
    public void willBecomeVisibleWithBounds(Rect bounds) {
    }

    /** Called when the attachment bounds did change.
      *
      */
    public void boundsDidChange(Rect newBounds) {
    }

    /** Called when the attachment is about to get removed
      *
      */
    public void willBecomeInvisible() {
    }

    /** Describes the TextAttachment class's information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.TextAttachment", 1);
        info.addField(OWNER_KEY,OBJECT_TYPE);
        info.addField(WIDTH_KEY,INT_TYPE);
        info.addField(HEIGHT_KEY,INT_TYPE);
    }

    /** Encodes the TextAttachment instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(OWNER_KEY,_owner);
        encoder.encodeInt(WIDTH_KEY,_width);
        encoder.encodeInt(HEIGHT_KEY,_height);
    }

    /** Decodes the TextAttachment instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        _owner = (TextView)decoder.decodeObject(OWNER_KEY);
        _width = decoder.decodeInt(WIDTH_KEY);
        _height= decoder.decodeInt(HEIGHT_KEY);
    }

    /** Finishes the TextAttachment decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }

    void _willShowWithBounds(Rect rect) {
        if( _visible )
            boundsDidChange(rect);
        else {
            willBecomeVisibleWithBounds(rect);
            _visible = true;
        }
    }

    void _willHide() {
        willBecomeInvisible();
        _visible = false;
    }
}









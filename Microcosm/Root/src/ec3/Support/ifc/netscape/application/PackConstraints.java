// PackConstraints.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass describing constraints associated with a View managed by
  * a PackLayout LayoutManager.  You will usually configure a PackConstraints
  * instance appropriately and then associate this instance with a View using
  * the PackLayout's <b>setConstraints()</b> method.<p>
  * The default constraints are:<ul>
  *   <li>anchor = ANCHOR_CENTER
  *   <li>expand = false
  *   <li>fillx = false
  *   <li>filly = false
  *   <li>ipadx = 0
  *   <li>ipady = 0
  *   <li>padx = 0
  *   <li>side = SIDE_TOP
  * </ul>
  *
  */
public class PackConstraints extends Object implements Codable, Cloneable {
    int         anchor;
    boolean     expand;
    boolean     fillX;
    boolean     fillY;
    int         iPadX;
    int         iPadY;
    int         padX;
    int         padY;
    int         side;

    final static String ANCHOR_KEY = "anchor",
                        EXPAND_KEY = "expand",
                        FILLX_KEY  = "fillx",
                        FILLY_KEY  = "filly",
                        IPADX_KEY  = "ipadx",
                        IPADY_KEY  = "ipady",
                        PADX_KEY   = "padx",
                        PADY_KEY   = "pady",
                        SIDE_KEY   = "side";

    /** Position View north of the allocated area. */
    public static final  int ANCHOR_NORTH       = 0;
    /** Position View northeast of the allocated area. */
    public static final  int ANCHOR_NORTHEAST   = 1;
    /** Position View east of the allocated area. */
    public static final  int ANCHOR_EAST        = 2;
    /** Position View southeast of the allocated area. */
    public static final  int ANCHOR_SOUTHEAST   = 3;
    /** Position View south of the allocated area. */
    public static final  int ANCHOR_SOUTH       = 4;
    /** Position View southwest of the allocated area. */
    public static final  int ANCHOR_SOUTHWEST   = 5;
    /** Position View west of the allocated area. */
    public static final  int ANCHOR_WEST        = 6;
    /** Position View northwest of the allocated area. */
    public static final  int ANCHOR_NORTHWEST   = 7;
    /** Position View centered within the allocated area. */
    public static final  int ANCHOR_CENTER      = 8;

    /** Position View above the allocated area. */
    public static final  int SIDE_TOP       = 0;
    /** Position View below the allocated area. */
    public static final  int SIDE_BOTTOM    = 1;
    /** Position View to the left of the allocated area. */
    public static final  int SIDE_LEFT      = 2;
    /** Position View to the right of the allocated area. */
    public static final  int SIDE_RIGHT     = 3;

    /** Constructs a PackConstraints with default constraints. */
    public PackConstraints()    {
        anchor = ANCHOR_CENTER;
        expand = false;
        fillX  = false;
        fillY  = false;
        iPadX  = 0;
        iPadY  = 0;
        padX   = 0;
        padY   = 0;
        side   = SIDE_TOP;
    }

    /** Constructs a PackConstraints with the specified constraints. */
    public PackConstraints(int anchor, boolean expand,
                              boolean fillX, boolean fillY, int iPadX,
                              int iPadY, int padX , int padY, int side) {
        setAnchor(anchor);
        setExpand(expand);
        setFillX(fillX);
        setFillY(fillY);
        setInternalPadX(iPadX);
        setInternalPadY(iPadY);
        setPadX(padX);
        setPadY(padY);
        setSide(side);
    }


    /** Sets the location in which the View should be placed within its area.
      * Used when the View is smaller than its allocated area.
      */
    public void setAnchor(int value)    {
        if(value < ANCHOR_NORTH || value > ANCHOR_CENTER)
            throw new InconsistencyException(this +
                            "Invalid Anchor value: " + value);
        anchor = value;
    }

    /** Returns the anchor value.
      * @see #setAnchor
      */
    public int anchor()    {
        return anchor;
    }

    /** Specifies whether the PackLayout should enlarge the View to fill any
      * extra space within the View's allocated area.
      */
    public void setExpand(boolean value)    {
        expand = value;
    }

    /** Returns <b>true</b> if the PackLayout should enlarge the View to fill
      * any extra space within the View's allocated area.
      * @see #setExpand
      */
    public boolean expand()    {
        return expand;
    }

    /** Specifies whether the PackLayout should enlarge the View to fill any
      * extra space along the X-axis of the View's allocated area.
      */
    public void setFillX(boolean value) {
        fillX = value;
    }

    /** Returns <b>true</b> if the PackLayout should enlarge the View to fill
      * any extra space along the X-axis of the View's allocated area.
      * @see #setFillX
      */
    public boolean fillX() {
        return fillX;
    }

    /** Specifies whether the PackLayout should enlarge the View to fill any
      * extra space along the Y-axis of the View's allocated area.
      */
    public void setFillY(boolean value) {
        fillY = value;
    }

    /** Returns <b>true</b> if the PackLayout should enlarge the View to fill
      * any extra space along the Y-axis of the View's allocated area.
      * @see #setFillY
      */
    public boolean fillY() {
        return fillY;
    }

    /** Sets the padding the PackLayout adds to the View's minimum
      * width.  The PackLayout forces the View's minimum width to its
      * original minimum width plus twice its "internalPadX."
      */
    public void setInternalPadX(int value)  {
        iPadX = value;
    }

    /** Returns the View's "internalPadX."
      * @see #setInternalPadX
      */
    public int internalPadX()  {
        return iPadX;
    }

    /** Sets the padding the PackLayout adds to the View's minimum
      * height.  The PackLayout forces the View's minimum height to its
      * original minimum height plus twice its "internalPadY."
      */
    public void setInternalPadY(int value)  {
        iPadY = value;
    }

    /** Returns the View's "internalPadY."
      * @see #setInternalPadY
      */
    public int internalPadY()  {
        return iPadY;
    }

    /** Sets the padding the PackLayout adds to the View's width.  The
      * PackLayout does not change the View's width, but it does create
      * empty space "padX" pixels wide to the View's left and right.
      */
    public void setPadX(int value)  {
        padX = value;
    }

    /** Returns the View's "padX."
      * @see #setPadX
      */
    public int padX()  {
        return padX;
    }

    /** Sets the padding the PackLayout adds to the View's height.  The
      * PackLayout does not change the View's height, but it does create
      * empty space "padY" pixels tall above and below the View.
      */
    public void setPadY(int value)  {
        padY = value;
    }

    /** Returns the View's "padY."
      * @see #setPadY
      */
    public int padY()  {
        return padY;
    }

    /** Sets the side of the View's allocated area to which the PackLayout
      * should attach the View.  Possible values are SIDE_TOP, SIDE_BOTTOM,
      * SIDE_LEFT, and SIDE_RIGHT.
      */
    public void setSide(int value)  {
        if(value < SIDE_TOP || value > SIDE_RIGHT)
            throw new InconsistencyException(this +
                            "Invalid Side value: " + value);
        side = value;
    }

    /** Returns the side of the View's allocated area to which the PackLayout
      * should attach the View.
      * @see #setSide
      */
    public int side()  {
        return side;
    }



/// Codable Interface
    /** Describes the PackConstraints class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info)   {
        info.addClass("netscape.application.PackConstraints", 1);
        info.addField(ANCHOR_KEY, INT_TYPE);
        info.addField(EXPAND_KEY, BOOLEAN_TYPE);
        info.addField(FILLX_KEY, BOOLEAN_TYPE);
        info.addField(FILLY_KEY, BOOLEAN_TYPE);
        info.addField(IPADX_KEY, INT_TYPE);
        info.addField(IPADY_KEY, INT_TYPE);
        info.addField(PADX_KEY, INT_TYPE);
        info.addField(PADY_KEY, INT_TYPE);
        info.addField(SIDE_KEY, INT_TYPE);
    }

    /** Encodes the PackConstraints.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeInt(ANCHOR_KEY, anchor);
        encoder.encodeBoolean(EXPAND_KEY, expand);
        encoder.encodeBoolean(FILLX_KEY, fillX);
        encoder.encodeBoolean(FILLY_KEY, fillY);
        encoder.encodeInt(IPADX_KEY, iPadX);
        encoder.encodeInt(IPADY_KEY, iPadY);
        encoder.encodeInt(PADX_KEY, padX);
        encoder.encodeInt(PADY_KEY, padY);
        encoder.encodeInt(SIDE_KEY, side);
    }

    /** Decodes the PackConstraints.
      * @see Codable#decode
      */
   public void decode(Decoder decoder) throws CodingException {
        anchor = decoder.decodeInt(ANCHOR_KEY);
        expand = decoder.decodeBoolean(EXPAND_KEY);
        fillX = decoder.decodeBoolean(FILLX_KEY);
        fillY = decoder.decodeBoolean(FILLY_KEY);
        iPadX = decoder.decodeInt(IPADX_KEY);
        iPadY = decoder.decodeInt(IPADY_KEY);
        padX = decoder.decodeInt(PADX_KEY);
        padY = decoder.decodeInt(PADY_KEY);
        side = decoder.decodeInt(SIDE_KEY);
    }

    /** Finishes the PackConstraints decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }

    public Object clone()   {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e){
             throw new InconsistencyException(this +
                            ": clone() not supported :" + e);
       }
    }
}

// ScrollingTarget.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface for objects interested in being controlled by a ScrollBar, such
  * as a ScrollView.
  * @see ScrollBar
  * @see ScrollView
  * @see ScrollGroup
  */
public interface Scrollable {
    /** Axis value indicating a Horizontal scroller. */
    public static final int HORIZONTAL = 0;

    /** Axis value indicating a Vertical scroller. */
    public static final int VERTICAL = 1;

    /** This method should return the total length of the visible scroll area.
      * This length determines the percent visible area, which sets the
      * scroller knob size.  <b>axis</b> indicates the scrolling direction,
      * either VERTICAL or HORIZONTAL.
      */
    public int lengthOfScrollViewForAxis(int axis);

    /** This method should return the total length of the View being scrolled.
      * This length determines the percent visible area, which sets the
      * scroller knob size.  <b>axis</b> indicates the scrolling direction,
      * either VERTICAL or HORIZONTAL.
      */
    public int lengthOfContentViewForAxis(int axis);

    /** This method should return the visible scroll area's origin
      * relative to the origin of the View being scrolled.  This position
      * determines the scroller knob's position.  <b>axis</b> indicates the
      * scrolling direction, either VERTICAL or HORIZONTAL.
      */
    public int positionOfContentViewForAxis(int axis);

    /** This method tells the Scrollable to change the origin of the View
      * it's scrolling to (<b>x</b>, <b>y</b>)
      */
    public void scrollTo(int x, int y);

    /** This method tells the Scrollable to move the origin of the View
      * it's scrolling by (<b>deltaX</b>, <b>deltaY</b>)
      */
    public void scrollBy(int deltaX, int deltaY);
}

// LayoutManager.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

/** Interface for objects interested in acting as a LayoutManager, an object
  * which determines the size and position of a View's subviews. Views
  * automatically call their LayoutManager's <b>layoutView()</b> method from
  * their <b>didSizeBy()</b> method (in response to a resize), so that the
  * LayoutManager can adjust the View's subviews' sizes and positions.  Views
  * also call their LayoutManager's <b>addSubview()</b> and
  * <b>removeSubview()</b> methods as they gain and lose subviews.  In
  * response, a LayoutManager can immediately relayout the View's subviews, or
  * do nothing until all modifications have been made to the View.  In this
  * case, <b>layoutView()</b> must be called explicitly once the modifications
  * have been completed.
  * @see View#setLayoutManager
  */

public interface LayoutManager {
    /** Notifies the LayoutManager that <b>aView</b> has been added to the
      * View hierarchy.
      */
    public void addSubview(View aView);

    /** Notifies the LayoutManager that <b>aView</b> has been removed from the
      * View hierarchy.
      */
    public void removeSubview(View aView);

    /** Requests that the LayoutManager position <b>aView</b>'s subviews.
      * A View calls <b>layoutView()</b> from its <b>didSizeBy()</b> method.
      * <b>deltaWidth</b> and <b>deltaHeight</b> represent the delta between
      * <b>aView</b>'s old size and its current size.
      * @see View#didSizeBy
      */
    public void layoutView(View aView, int deltaWidth, int deltaHeight);
}


// Window.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Interface implemented by both the InternalWindow and ExternalWindow
  * classes, defining functionality common to the two Window types.
  * @see InternalWindow
  * @see ExternalWindow
  * @note 1.0 added moveToFront/Back
  * @note 1.0 added support for Menus
  * @note 1.0 added support for focus model
  */
public interface Window extends Target {
    /** A Window type that has no title bar or border. */
    public final static int BLANK_TYPE = 0;

    /** A Window type that has a title bar and border. */
    public final static int TITLE_TYPE = 1;

    /** Command that makes the Window visible. */
    public final static String SHOW = "show";

    /** Command that hides the Window. */
    public final static String HIDE = "hide";

    /** Returns the Size defining the Window's content area. Use this
      * Size to properly position and size any View that you plan to add to the
      * Window.
      */
    public Size contentSize();

    /** Adds <B>aView</B> to the Window. */
    public void addSubview(View aView);

    /** Displays the Window. */
    public void show();

    /** Displays the Window until dismissed by the user.  This method will
      * not return until the user closes the Window.  While the Window remains
      * visible, no View outside the Window will receive Events.
      */
    public void showModally();

    /** Hides the Window. */
    public void hide();

    /** Move the window to the front.
      *
      */
    public void moveToFront();

    /** Move the window to the back.
      *
      */
    public void moveToBack();

    /** Returns <b>true</b> if the Window is currently visible. */
    public boolean isVisible();

    /** Sets the Window's title, the string displayed in its title bar.
      */
    public void setTitle(String aString);

    /** Returns the Window's title.
      * @see #setTitle
      */
    public String title();

    /** Sets the Window's owner, the object interested in learning about
      * special events, such as the user closing the Window.
      */
    public void setOwner(WindowOwner anObject);

    /** Returns the Window's owner.
      * @see #setOwner
      */
    public WindowOwner owner();

    /** Sets the MenuView that will appear along the top edge of the Window.
      */
    public void setMenuView(MenuView aMenuView);

    /** Returns the MenuView that appears along the top edge of the Window.
      */
    public MenuView menuView();

    /** Returns the View containing the point (<B>x</B>, <B>y</B>). */
    public View viewForMouse(int x, int y);

    /** Sets the Window's bounds to the rectangle (<b>x</b>, <b>y</b>,
      * <b>width</b>, <b>height</b>).
      */
    public void setBounds(int x, int y, int width, int height);

    /** Sets the Window's bounds to <b>newBounds</b>. */
    public void setBounds(Rect newBounds);

    /** Sets the Window's size to (<b>width</b>, <b>height</b>). */
    public void sizeTo(int width, int height);

    /** Changes the Window's size by (<b>deltaWidth</b>, <b>deltaHeight</b>).
      */
    public void sizeBy(int deltaWidth, int deltaHeight);

    /** Changes the Window's location by (<b>deltaX</b>, <b>deltaY</b>).
      */
    public void moveBy(int deltaX, int deltaY);

    /** Sets the Window's location to (<b>x</b>, <b>y</b>). */
    public void moveTo(int x, int y);

    /** Centers the Window. */
    public void center();

    /** Returns the size the Window must be to support a content
      * size of (<B>width</B>, <B>height</B>).
      */
    public Size windowSizeForContentSize(int width, int height);

   /** Returns a newly-allocated copy of the Window's bounding
     * rectangle, which defines the Window's size and position.
     */
    public Rect bounds();

    /** Sets the Window's minimum size to (<b>width</b>, <b>height</b>). */
    public void setMinSize(int width, int height);

    /** Returns the Window's minimum size.  Returns <b>null</b> if not set. */
    public Size minSize();

    /** Sets whether the user can resize the Window. */
    public void setResizable(boolean flag);

    /** Returns <b>true</b> if the user can resize the Window.
      * @see #setResizable
      */
    public boolean isResizable();

   /** Sets whether the window contains a document. Windows containing document
     * are treated in a different manner by the target chain.
     *
     */
    public void setContainsDocument(boolean containsDocument);

    /** Return whether the window contains a document.
      *
      */
    public boolean containsDocument();

    /** If the window contains a document, this method is called
      * when the window just became the current document.
      *
     */
    public void didBecomeCurrentDocument();

    /** If the window contains a document, this method is called
      * when the window is no longer the current document.
      *
      */
    public void didResignCurrentDocument();

    /** Return whether this window is the current document.
      *
      */
    public boolean isCurrentDocument();

}



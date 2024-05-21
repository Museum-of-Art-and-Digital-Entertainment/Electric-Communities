// AWTComponentView.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.awt.Component;
import netscape.util.*;


/* ALERT!
    - Can't add components to the panel using index other than 0.
    - Focus events come in when clicking on AWT things and then ours
    */

/** View subclass that supports the embedding of an AWT Component in an IFC
  * application. The component will not be clipped by other IFC Views,
  * including the AWTComponentView itself, or any of its ancestors.
  */
public class AWTComponentView extends View {
    Component component;
    // This will go away when we changed the semantics of
    // ancestorWasRemovedFromViewHierarchy.
    RootView rootView;

    /** Constructs an AWTComponentView with origin (0, 0) and zero width and
      * height.
      */
    public AWTComponentView() {
        this(0, 0, 0, 0);
    }

    /** Constructs an AWTComponentView with bounds <B>rect</B>.
      */
    public AWTComponentView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs an AWTComponentView with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public AWTComponentView(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    void setComponentBounds() {
        Rect cBounds = _superview.convertRectToView(null, bounds);

        component.reshape(cBounds.x, cBounds.y, cBounds.width, cBounds.height);
    }

    void addComponent() {
        if (rootView != null) {
            rootView.addComponentView(this);
        }
    }

    void removeComponent() {
        if (component != null && rootView != null) {
            rootView.removeComponentView(this);
        }
    }

    /** Sets the AWT component that will be embedded in the AWTComponentView.
      */
    public void setAWTComponent(Component aComponent) {
        removeComponent();
        component = aComponent;
        addComponent();
    }

    /** Returns the AWT component.
      * @see #setComponent
      */
    public Component awtComponent() {
        return component;
    }

    /** Overridden to take special action when the AWTComponentView or an
      * ancestor leaves the Application's View hierarchy.
      * @see View#ancestorWillRemoveFromViewHierarchy
      */
    protected void ancestorWillRemoveFromViewHierarchy(View removedView) {
        removeComponent();
        rootView = null;
    }

    /** Overridden to take special action when the AWTComponentView or an
      * ancestor becomes part of the Application's View hierarchy.
      * @see View#ancestorWasAddedToViewHierarchy
      */
    protected void ancestorWasAddedToViewHierarchy(View addedView) {
        rootView = rootView();
        addComponent();
    }
}

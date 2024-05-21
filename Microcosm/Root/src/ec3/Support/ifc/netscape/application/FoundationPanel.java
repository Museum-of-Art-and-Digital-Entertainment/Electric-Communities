// FoundationPanel.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Event;

/** java.awt.Panel subclass that glues the IFC View-based world to the
  * AWT component-based world.  Each Application has at least one
  * FoundationPanel instance. If you use ExternalWindows, you'll have an
  * additional instance per Window.  You'll rarely access a FoundationPanel
  * directly - instead you'll work with an Application or RootView.
  * @see Application
  * @see RootView
  * @note 1.0 added support for JDK 1.1 printing
  * @note 1.0 requesting focus in keydown because of JDK 1.1 incompatibility
  *           with JDK 1.0.2
  */
public class FoundationPanel extends Panel {
    RootView rootView;

    /** Constructs a FoundationPanel with a RootView. */
    public FoundationPanel() {
        setRootView(new RootView());
    }

    /** Constructs a FoundationPanel and RootView of a specified size. */
    public FoundationPanel(int width, int height) {
        setRootView(new RootView(0, 0, width, height));
        resize(width, height);
    }

    /** Returns the Panel's RootView, the View that sits at the top
      * of the View hierarchy.
      * @see RootView
      */
    public RootView rootView() {
        return rootView;
    }

    /** Sets the Panel's RootView. */
    public void setRootView(RootView rootView) {
        Application application = Application.application();

        if (this.rootView != null) {
            application.removeRootView(this.rootView);
        }
        this.rootView = rootView;
        rootView.setPanel(this);
        rootView.setVisible(!application.isPaused);
        application.addRootView(rootView);
    }

    /** Overridden to resize the RootView when the FoundationPanel changes
      * size.
      */
    public void resize(int width, int height) {
        Application app = Application.application();

        super.resize(width, height);
        // It is possible for the app to be null.  Sometimes we get called
        // from the main thread group.  This seems like a security hole, but
        // it still happens.
        if (app != null && app.eventLoop.shouldProcessSynchronously()) {
            rootView.processEvent(
                        ApplicationEvent.newResizeEvent(width, height));
        } else {
            addEvent(ApplicationEvent.newResizeEvent(width, height));
        }
    }

    /** Overridden to resize the RootView when the FoundationPanel changes
      * size.
      */
    public void reshape(int x, int y, int width, int height) {
        Application app = Application.application();

        super.reshape(x, y, width, height);
        // System.err.println("AWT reshape: " + new Rect(x, y, width, height));
        // It is possible for the app to be null.  Sometimes we get called
        // from the main thread group.  This seems like a security hole, but
        // it still happens.
        if (app != null && app.eventLoop.shouldProcessSynchronously()) {
            rootView.processEvent(
                            ApplicationEvent.newResizeEvent(width, height));
        } else {
            addEvent(ApplicationEvent.newResizeEvent(width, height));
        }
    }

    /** Updates the Panel. Calls <b>paint()</b>.
      */
    public void update(java.awt.Graphics g) {
        paint(g);
    }

    /** Paints the Panel by eventually calling <b>redraw()</b> on the RootView.
      */
    public void paint(java.awt.Graphics g) {
        if (JDK11AirLock.isPrintGraphics(g)) {
            Application app = Application.application();

            if (app != null) {
                if (app.eventLoop.shouldProcessSynchronously()) {
                    Rect rect = new Rect(0, 0, rootView.width(), rootView.height());
                    Graphics ifcGraphics = new Graphics(rect, g);

                    rootView.redraw(ifcGraphics, rect);
                    rootView.redrawTransparentWindows(ifcGraphics, rect, null);
                } else {
                    ApplicationEvent event = ApplicationEvent.newPrintEvent(g);

                    event.processor = rootView;
                    app.eventLoop().addEventAndWait(event);
                }
            } else {
                System.err.println("Can't print with no application");
            }
        } else {
            addEvent(ApplicationEvent.newUpdateEvent(g));
            super.paint(g);
        }
    }

    /** Overridden to add a MouseEvent of type <b>MouseEvent.MOUSE_DOWN</b>
      * to the Application's EventLoop.
      */
    public boolean mouseDown(java.awt.Event evt, int x, int y) {
        requestFocus();
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_DOWN, x, y,
                                evt.modifiers));
        return true;
    }

    /** Overridden to add a MouseEvent of type
      * <b>MouseEvent.MOUSE_DRAGGED</b> to the Application's EventLoop.
      */
    public boolean mouseDrag(java.awt.Event evt, int x, int y) {
      /* funnel window drag events to a separate event queue */
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_DRAGGED, x, y,
                                evt.modifiers));

        return true;
    }

    /** Overridden to add a MouseEvent of type <b>MouseEvent.MOUSE_UP</b> to
      * the Application's EventLoop.
      */
    public boolean mouseUp(java.awt.Event evt, int x, int y) {
      /* funnel window drag events to a separate event queue */
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_UP, x, y,
                                evt.modifiers));

        return true;
    }

    /** Overridden to add a MouseEvent of type
      * <b>MouseEvent.MOUSE_ENTERED</b> to the Application's EventLoop.
      */
    public boolean mouseEnter(java.awt.Event evt, int x, int y) {
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_ENTERED, x, y,
                                evt.modifiers));
        return true;
    }

    /** Overridden to add a MouseEvent of type <b>MouseEvent.MOUSE_MOVED</b>
      * to the Application's EventLoop.
      */
    public boolean mouseMove(java.awt.Event evt, int x, int y) {
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_MOVED, x, y,
                                evt.modifiers));
        return true;
    }

    /** Overridden to add a MouseEvent of type <b>MouseEvent.MOUSE_EXITED</b>
      * to the Application's EventLoop.
      */
    public boolean mouseExit(java.awt.Event evt, int x, int y) {
        addEvent(new MouseEvent(evt.when, MouseEvent.MOUSE_EXITED, x, y,
                                evt.modifiers));

        return true;
    }

    /** Overridden to add a KeyEvent of type <b>KeyEvent.KEY_DOWN</b> to
      * the Application's EventLoop.
      */
    public boolean keyDown(java.awt.Event evt, int key) {
        if (evt.target == this) {
            addEvent(new KeyEvent(evt.when, key, evt.modifiers, true));
            return true;
        } else {
            return super.keyDown(evt, key);
        }
    }

    /** Overridden to add a KeyEvent of type <b>KeyEvent.KEY_UP</b> to
      * the Application's EventLoop.
      */
    public boolean keyUp(java.awt.Event evt, int key) {
        if (evt.target == this) {
            addEvent(new KeyEvent(evt.when, key, evt.modifiers, false));
            return true;
        } else {
            return super.keyUp(evt, key);
        }
    }

    /** Overridden to force the focused View to stop editing.
      */
    public synchronized boolean lostFocus(java.awt.Event evt, Object what) {
        addEvent(ApplicationEvent.newFocusEvent(false));
        return true;
    }

    /** Overridden to force the focused View to start editing.
      */
    public boolean gotFocus(java.awt.Event evt, Object what) {
        addEvent(ApplicationEvent.newFocusEvent(true));
        return true;
    }

    private synchronized void addEvent(netscape.application.Event anEvent) {
        if (anEvent.processor() == null) {
            anEvent.setProcessor(rootView);
        }
        if (rootView != null) {
            if (rootView.application() != null) {
                rootView.application().eventLoop().addEvent(anEvent);
            } else {   // ALERT!  This should never happen.
//                System.err.println("No Application on RootView");
            }
        } else {
//            System.err.println("No RootView");
        }
    }

    java.awt.Frame frame() {
        java.awt.Frame          frame;
        java.awt.Component      parent;

        parent = getParent();
        while (parent != null && !(parent instanceof java.awt.Frame)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            return (java.awt.Frame)parent;
        }

        return null;
    }

    /** @private */
    public void setCursor(int cursor) {
        java.awt.Frame          frame;

        frame = frame();
        if (frame != null) {
            frame.setCursor(cursor);
        }
    }

    /** @private */
    public void layout() {
    }

    /** @private */
    public void printAll(java.awt.Graphics g) {
        paint(g);
    }
}

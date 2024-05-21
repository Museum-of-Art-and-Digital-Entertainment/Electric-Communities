// FoundationWindow.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import java.awt.Window;
import java.awt.Frame;
import java.awt.Dimension;
import netscape.util.*;

/** FoundationFrame is...
  * Subclass of window to allow ifc component in BLANK_TYPE
  * windows.
  * @private
  */
public class FoundationWindow extends java.awt.Window {
    ExternalWindow externalWindow;

    public FoundationWindow(java.awt.Frame parent) {
        super(parent);
    }

    public boolean handleEvent(java.awt.Event event) {
        Application app = Application.application();
        if (event.id == java.awt.Event.WINDOW_DESTROY) {
            if( app != null && app.eventLoop.shouldProcessSynchronously())
                externalWindow.hide();
            else {
                /** Cannot wait. This will block the AWT thread and may cause
                 * a dead lock if the window owner needs to show an alert
                 */
                externalWindow.rootView().application().performCommandLater(
                    externalWindow, ExternalWindow.HIDE, null, false);
            }
            return true;
        } else {
            return super.handleEvent(event);
        }
    }

    public ExternalWindow externalWindow() {
        return externalWindow;
    }

    void setExternalWindow(ExternalWindow wFrame) {
        externalWindow = wFrame;
    }

    public void layout() {
        java.awt.Dimension size = size();
        java.awt.Insets insets = insets();
        int x = insets.left, y = insets.top,
            w = size.width - (insets.left + insets.right),
            h = size.height - (insets.top + insets.bottom);

        if (w > 0 && h > 0) {
            externalWindow.panel().reshape(x, y, w, h);
        }
    }

    public java.awt.Dimension minimumSize() {
        if (externalWindow != null) {
            Size size = externalWindow.minSize();
            return new java.awt.Dimension(size.width, size.height);
        } else
            return null;
    }
}

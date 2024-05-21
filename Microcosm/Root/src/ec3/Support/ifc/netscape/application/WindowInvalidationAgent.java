// WindowInvalidationAgent.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

class WindowInvalidationAgent implements Target {
    ExternalWindow window;

    WindowInvalidationAgent(ExternalWindow aWindow) {
        super();
        window = aWindow;
    }

    void run() {
        Application app = Application.application();
        if (app != null)
            app.performCommandLater(this, "invalidate", null, false);
    }

    public void performCommand(String command, Object data) {
        window.invalidateAWTWindow();
    }
}


// IFCListViewAccess.java
// By Dima Nasledov
// Copyright 1997 Electric Communities.  All rights reserved.

package netscape.application;

/**
 * We have to acceess Button's timer to be able to kill it when we need.
 * The problem is that IFC's button doesn't stop it's timer when disabled,
 */
public class IFCButtonAccess {

    public static final Timer timer(Button button) {
        return button._actionTimer;
    }

    public static final void killTimer(Button button) {
        if (button._actionTimer != null) {
            button._actionTimer.stop();
            button._actionTimer = null;
        }
    }
}

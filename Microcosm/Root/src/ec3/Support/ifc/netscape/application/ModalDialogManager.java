// ModalDialogManager.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/*
 * ModalWindow.show blocks on win32 but not on unix
 * This class uses a thread to bring the modal window
 * to the front. This is to avoid having our main thread
 * blocking
 */
class ModalDialogManager implements Runnable {
    private java.awt.Dialog modalDialog;

    ModalDialogManager( java.awt.Dialog aModalDialog) {
        super();
        modalDialog = aModalDialog;
    }

    void show() {
        Thread thread;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        modalDialog.show();
    }
}

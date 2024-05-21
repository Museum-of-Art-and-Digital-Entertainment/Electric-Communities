// ApplicationObserver.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

/** Interface implemented by objects interested in receiving notifications
  * when the Application changes state.
  * @note 1.0 added notifications when focus changes and when
  *           current document changes
  */
public interface ApplicationObserver {



    /** Informs the observer that the application's EventLoop has just started
      * running.
      */
    public void applicationDidStart(Application application);

    /** Informs the observer that the application's EventLoop has just finished
      * receiving events.
      */
    public void applicationDidStop(Application application);

    /** Informs the observer that the application's focused view has changed.
      * If <b>focusedView</b> is null, the application no longer has a focused
      * view.
      *
      */
    public void focusDidChange(Application application, View focusedView);

    /** Informs the observer that the application's current document window
      * has changed. If <b>document</b> is null, the application no longer has a
      * current document window.
      *
      */
    public void currentDocumentDidChange(Application application,
                                         Window document);

    /** Informs the observer that the Applet that launched the Application
      * is no longer visible.
      */
    public void applicationDidPause(Application application);

    /** Informs the observer that the Applet that launched the Application
      * is visible again.
      */
    public void applicationDidResume(Application application);
}

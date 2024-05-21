// TargetChain.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.


package netscape.application;
import netscape.util.*;

/** Object subclass used to locate a Target within an application that is
  * capable of performing a specific command.  When asked to perform a command,
  * the TargetChain asks each of its potential Targets, in turn, whether or
  * not they implement the ExtendedTarget interface, and if so, calls their
  * <b>canPerformCommand()</b> method to determine whether or not they can
  * perform the specified command.  The TargetChain forwards the command on
  * to the first object that can perform the command.  The TargetChain
  * returned by the static <b>applicationChain()</b> method contains the
  * following potential Targets:
  * <UL>
  * <LI>Each of the targets added using addTarget with atFront == true</LI>
  * <LI>firstRootView's focused view </LI>
  * <LI>if there is a document window </LI>
  * <UL>
  * <LI>document window's focused view if different from firstRootView's focused
  * view</LI>
  * <LI>document window's owner</LI>
  * <LI>document window </LI>
  * </UL>
  * <LI>else if firstRootView not equal to mainRootView </LI>
  * <UL>
  * <LI>mainRootView's focused view </LI>
  * </UL>
  * <LI>firstRootView's window's owner</LI>
  * <LI>firstRootView's window</LI>
  * <LI>firstRootView</LI>
  * <LI>application</LI>
  * <LI>Each of the targets added using addTarget with atFront == false </LI>
  * </UL>
  * When a window is modal, the target chain changes so a component outside of
  * the modal loop cannot receive a command.
  * The TargetChain is useful in situations where the Target that should
  * receive a command is context sensitive.  For example, a Menu item
  * representing the "cut" command should send its command to the current
  * selection.  If an application has multiple TextFields, the TextField that
  * should receive the command will depend upon which TextField the user has
  * decided to edit.  By setting the Menu item's Target to the application's
  * TargetChain, the application can configure the Menu item's Target once,
  * with the assurance that the TargetChain will locate and forward the command
  * to the appropriate object.
  * @see Target
  * @note 1.0 changes for focus model and keyboard UI
  */

public class TargetChain implements ExtendedTarget {
    private Vector preTargets = new Vector();
    private Vector postTargets = new Vector();
    private static TargetChain applicationChain;

    private TargetChain() {
        super();
    }

    /** Returns the Application global TargetChain.
        */
    public static TargetChain applicationChain() {
        if (applicationChain == null) {
            applicationChain = new TargetChain();
        }
        return applicationChain;
    }

    /** Adds <b>target</b> to the TargetChain. If <b>atFront</b> is
      * <b>true</b>,the TargetChain will query <b>target</b> before any of the
      * ExtendedTargets currently in its list. Otherwise, the TargetChain will
      * query <b>target</b> only after all current ExtendedTargets.
      */
    public synchronized void addTarget(ExtendedTarget target,
                                       boolean atFront) {
        if (atFront) {
            preTargets.insertElementAt(target, 0);
        } else {
            postTargets.addElement(target);
        }
    }

    /** Removes <b>target</b> from the TargetChain (only if <b>target</b> was
      * added using <b>addTarget()</b>).
      * @see #addTarget
      */
    public synchronized void removeTarget(ExtendedTarget target) {
        preTargets.removeElement(target);
        postTargets.removeElement(target);
    }

    /** Returns the first Target that performs <B>command</B>.
      */
    public synchronized Target targetForCommand(String command) {
        int             i, size;
        Application     app;
        View            focusedView;
        RootView        firstRootView,mainRootView;
        Window          documentWindow, firstWindow;
        WindowOwner     documentOwner, firstOwner;

        View modalView;

        /* Target added with atFront == true */
        size = preTargets.size();
        for (i = 0; i < size; i++) {
            ExtendedTarget target = (ExtendedTarget) preTargets.elementAt(i);

            if (target.canPerformCommand(command)) {
                return target;
            }
        }

        app = Application.application();

        /** Modal session going on? */
        modalView = app.modalView();

        if(modalView == null) {
            /* First root view's focused view */

            firstRootView = app.firstRootView();
            if (firstRootView != null) {
                focusedView = firstRootView.focusedView();
                if (objectCanPerformCommand(focusedView,command))
                    return (Target)focusedView;
            }

            /* Document window */
            documentWindow = app.currentDocumentWindow();
            if(documentWindow != null) {

                /* Document window focused view ? */
                if(documentWindow instanceof InternalWindow)
                    focusedView = ((InternalWindow)documentWindow).focusedView();
                else
                    focusedView = ((ExternalWindow)documentWindow).rootView().focusedView();
                if(objectCanPerformCommand(focusedView,command))
                    return (Target)focusedView;

                /* Document window owner ?*/
                documentOwner = documentWindow.owner();
                if(objectCanPerformCommand(documentOwner,command))
                    return (Target)documentOwner;

                /* Document window ? */
                if(objectCanPerformCommand(documentWindow,command))
                    return (Target)documentWindow;
            } else if((mainRootView = (app.mainRootView())) != null)    {
                if(mainRootView.mainWindow() == null) {
                    /* mainRootView's focusedview ? */
                    focusedView = mainRootView.focusedView();
                } else  {
                    focusedView = mainRootView.rootViewFocusedView();
                }
                if(objectCanPerformCommand(focusedView,command))
                    return (Target)focusedView;
            }

            if(firstRootView != null) {
                /* firstRootView's window owner */
                firstWindow = firstRootView.externalWindow();
                if(firstWindow != null) {
                    firstOwner = firstWindow.owner();
                    if(objectCanPerformCommand(firstOwner,command))
                        return (Target)firstOwner;

                    /* firstWindow? */
                    if(objectCanPerformCommand(firstWindow,command))
                        return (Target)firstWindow;
                }

                /* firstRootView? */
                if(objectCanPerformCommand(firstRootView,command))
                    return (Target) firstRootView;
            }

            /** Application? **/
            if(objectCanPerformCommand(app,command))
                return (Target) app;
        } else { /* Modal view not null */
            if(modalView instanceof RootView) { /* External Window modal */
                firstRootView = (RootView) modalView;
                focusedView = firstRootView.focusedView();
                firstWindow = firstRootView.externalWindow();
            } else if(modalView instanceof InternalWindow) {/* Internal Window modal */
                firstRootView = null;
                focusedView = ((InternalWindow)modalView).focusedView();
                firstWindow = (Window) modalView;
            } else { /* Random modal view */
                firstRootView = null;
                focusedView = modalView;
                firstWindow = null;
            }

            /* Focused view? */
            if(objectCanPerformCommand(focusedView,command))
                return (Target)focusedView;

            if(firstWindow != null) {
                firstOwner = firstWindow.owner();
                /* Window owner?  */
                if(objectCanPerformCommand(firstOwner,command))
                    return (Target)firstOwner;
                /* Window? */
                if(objectCanPerformCommand(firstWindow,command))
                    return (Target)firstWindow;
            }

            /* firstRootView? */
            if(firstRootView != null) {
                if(objectCanPerformCommand(firstRootView,command))
                    return (Target)firstRootView;
            }
        }

        /* Target added with atFront == true */
        size = postTargets.size();
        for (i = 0; i < size; i++) {
            ExtendedTarget target = (ExtendedTarget) postTargets.elementAt(i);

            if (target.canPerformCommand(command)) {
                return target;
            }
        }

        return null;
    }

    /** Returns <b>true</b> if any of the TargetChain's Targets can perform
      * <B>command</B>.
      */
    public boolean canPerformCommand(String command) {
        return (targetForCommand(command) != null);
    }

    /** Forwards the performCommand message to the first Target in the chain
      * that can perform <B>command</B>.
      */
    public void performCommand(String command, Object data) {
        Target target = targetForCommand(command);

        if (target != null) {
            target.performCommand(command, data);
        }
    }

    private boolean objectCanPerformCommand(Object anObject,String command) {
        if( anObject != null &&
           (anObject instanceof ExtendedTarget) &&
            ((ExtendedTarget)anObject).canPerformCommand(command))
            return true;
        else
            return false;
    }
}


package ec.ifc.app;

import ec.trace.Trace;
import netscape.application.*;
import netscape.util.*;
import java.awt.Dimension;

/**
 * Subclass of ExternalWindow that uses an ECRootView and
 * adds some additional behavior such as tip views, default/cancel buttons,
 * and window positioning support.
 */
public class ECExternalWindow extends ExternalWindow
{
    /** object responsible for determining/maintaining window's position */
    protected WindowPositioner myPositioner = null;
    /** used to uniquely identify this window for saving/restoring position */
    protected String myPositioningKey = null;
    
    //
    // Constructors
    //
    
    /** returns a new ECExternalWindow of type TITLE_TYPE */    
    public ECExternalWindow() {
        this(TITLE_TYPE);
    }
    
    /**
     * Returns a new ECExternalWindow of the specified type. The
     * type can be TITLE_TYPE or BLANK_TYPE.
     */ 
    public ECExternalWindow(int type) {
        super(type);
        FoundationPanel myPanel = panel();
        myPanel.setRootView(new ECRootView());
    }
    
    //
    // public methods
    //
        
    /** Returns the cancel button of this window's root view */
    public ECButton cancelButton() {
        return ((ECRootView)rootView()).cancelButton();
    }

    /** Returns the default button of this window's root view */
    public ECButton defaultButton() {
        return ((ECRootView)rootView()).defaultButton();
    }
    
    /** Overridden to save window position before hiding */
    public void hide() {
        saveBounds();
        super.hide();
        
        if (Trace.gui.usage && Trace.ON) {
            // hide might have been prevented from succeeding
            if (!isVisible()) {
                Trace.gui.usagem("hid " + TraceUtils.traceDescription(this));
            }
        }       
    } 
    
    /**
     * Returns the object responsible for determining a position for this
     * window and saving the position between sessions. To save the position
     * between sessions, a positioningKey must also be set.
     * @see #setPositioningKey
     */
    public WindowPositioner positioner() {
        return myPositioner;
    }

    /**
     * Returns the string used to uniquely identify this window for the sake
     * of saving/restoring window positions. If this is null, the window's
     * position will not be saved.
     */
    public String positioningKey() {
        return myPositioningKey;
    }

    /** Empties the set of strings to display as tips */    
    public void removeAllTips() {
        ((ECRootView)rootView()).removeAllTips();
    }
    
    /**
     * Removes the string or ECTipViewOwner that provides the tip for
     * the specified view.
     */ 
    public void removeTipForView(View view) {
        ((ECRootView)rootView()).removeTipForView(view);
    }
    
    /**
     * Makes this window the target of keyboard events.
     * This is a convenience routine that passes the call along
     * to the ECRootView.
     */
    public void requestFocus() {
        ((ECRootView)rootView()).requestFocus();
    }
    
    /**
     * Sets the position and size of this window to that recommended by its
     * WindowPositioner. If it has no WindowPositioner, does nothing.
     * @see #setSizeToRecommended
     * @see #setPositionToRecommended
     */
    public void setBoundsToRecommended() {
        if (myPositioner == null) {
            return;
        }
        
        if (!isResizable()) {
            // bug in IFC/AWT1.1 makes setBounds on an unresizable
            // window do the wrong thing. Catch it here and fix
            // callers (could just fix it here except that you can't
            // call setResizable() on visible windows, so I'll let
            // the caller sort out where the calls should go)
            Trace.gui.errorm("setting bounds on unresizable window; this doesn't work with AWT/IFC 1.1");
            Trace.gui.errorm(Trace.getStackTrace());
        }
        setBounds(myPositioner.recommendBounds(this));
        
        // store the just-set bounds, so we can use them as a fallback
        // in minimize/maximize situations
        saveBounds();
    }
    
    /**
     * Sets the top-left corner of this window to that recommended by its
     * WindowManager. If this window has no WindowManager, does nothing.
     * @see #setBoundsToRecommended
     * @see #setSizeToRecommended
     */
    public void setPositionToRecommended() {
        if (myPositioner == null) {
            return;
        }
        
        Rect recommendedBounds = myPositioner.recommendBounds(this);
        moveTo(recommendedBounds.x, recommendedBounds.y);

        // store the just-set bounds, so we can use them as a fallback
        // in minimize/maximize situations
        saveBounds();
    }
    
    /**
     * Sets the size of this window to that recommended by its
     * WindowManager. If this window has no WindowManager, does nothing.
     * @see #setBoundsToRecommended
     * @see #setSizeToRecommended
     */
    public void setSizeToRecommended() {
        if (myPositioner == null) {
            return;
        }
        
        if (!isResizable()) {
            // bug in IFC/AWT1.1 makes setBounds on an unresizable
            // window do the wrong thing. Catch it here and fix
            // callers (could just fix it here except that you can't
            // call setResizable() on visible windows, so I'll let
            // the caller sort out where the calls should go)
            Trace.gui.errorm("setting size on unresizable window;" + 
                " this doesn't work with AWT/IFC 1.1");
            Trace.gui.errorm(Trace.getStackTrace());
        }
        Rect recommendedBounds = myPositioner.recommendBounds(this);
        sizeTo(recommendedBounds.width, recommendedBounds.height);

        // store the just-set bounds, so we can use them as a fallback
        // in minimize/maximize situations
        saveBounds();
    }
    
    /** Sets the cancel button of this window's root view */
    public void setCancelButton(ECButton newCancelButton) {
        ((ECRootView)rootView()).setCancelButton(newCancelButton);
    }

    /** Sets the default button of this window's root view */
    public void setDefaultButton(ECButton newDefaultButton) {
        ((ECRootView)rootView()).setDefaultButton(newDefaultButton);
    }
    
    /**
     * Sets the object responsible for determining a position for this
     * window and saving/restoring positions between sessions.
     * @see #positioner 
     */
    public void setPositioner(WindowPositioner newPositioner) {
        myPositioner = newPositioner;
    }
    
    /**
     * Sets the positioning key. This should be set to a unique string for
     * each window that wants its position saved between sessions, and left
     * at null for other windows. These strings will be in a human-readable
     * file, so they should make sense. A good example is
     * "LOG_WINDOW_POSITION". The window must also have a positioner set
     * for its position to be saved between sessions.
     * @see #setPositioner
     * @see #positioningKey
     */
     public void setPositioningKey(String newKey) {
        myPositioningKey = newKey;
    }
    
    /**
     * Specifies a string to display when the mouse rests over the
     * specified view
     */ 
    public void setTipForView(String tip, View view) {
        ((ECRootView)rootView()).setTipForView(tip, view);
    }
    
    /**
     * Specifies an ECTipViewOwner that will be asked for a string to
     * display when the mouse rests over the specified view.
     */ 
    public void setTipForView(ECTipViewOwner owner, View view) {
        ((ECRootView)rootView()).setTipForView(owner, view);
    }
    
    /** 
     * Called by WindowManager; returns whether or not window is
     * in a save-worthy position. This returns true unless window
     * is in minimized or maximized size & position
     */
    public boolean shouldSaveBounds() {
        return !isMinimallySized() && !isMaximallySized();
    }   

    /**
     * Overridden to request focus on this window after showing.
     */
    public void show() {
        boolean isMinimallySized = isMinimallySized();
        boolean isVisible = isVisible();
    
        if (isMinimallySized && isVisible) {
            // trick minimized window into un-minimizing, without benefit of
            // any call to do so. To clear the native information about the window
            // state, we have to make sure the native window is disposed by hiding
            // the window before re-showing it. In addition, we've got to delay the
            // command to show the window after hiding it so that IFC's window
            // invalidation agent has a chance to do its thing.
            hide();
            Application.application().performCommandLater(this, SHOW, this);
            return;                 
        }
        
        if (!isVisible && (isMinimallySized || isMaximallySized())) {
            // if the window was minimized or maximized when it was last hidden,
            // then we don't want it to come back up with the bounds it had
            // before. We want it to come up with its pre-minimization or
            // pre-maximization bounds, but we don't know what those were, so
            // we'll just use the window positioner's logic to find new bounds.
            setBoundsToRecommended();
        }
        
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem("showed " + TraceUtils.traceDescription(this));
        }
        super.show();
        requestFocus();
    }

    //
    // non-public methods
    //

    /** Returns the pixel dimensions of the screen */
    private Dimension getScreenSize() {
        return java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    }   

    /**
     * Returns true if the window seems to be currently maximized
     * in the Windows sense of filling the screen.
     * Note that this information isn't available without native code,
     * so this is just a guess based on the window's size. This method
     * checks bounds only; it does not check whether the window is
     * currently visible.
     */
    private boolean isMaximallySized() {
        Dimension screenSize = getScreenSize();
            
        // quick and dirty check: if window content is exactly as
        // wide as screen, we'll call it maximized. Checking the
        // height would involve knowing the magic constant (or is it?)
        // for the task bar height. If this proves insufficient we can
        // check that the window is horizontally centered on the screen
        // also; if that still isn't good enough we'll probably have
        // to use native code.
        return screenSize.width == contentSize().width;
    }

    /**
     * Returns true if the window seems to be currently minimized,
     * in the Window sense of being in the task bar but not on screen.
     * Note that this information isn't available without native code,
     * so this is just a guess based on the window's position (when
     * minimized, windows are moved to a specific large-valued position).
     * This method checks position only; it does not check whether the
     * window is currently visible.
     */
    private boolean isMinimallySized() {
        Rect bounds = bounds();
        // minimized windows are moved to 3000,3000; we'll
        // just check whether any part of the window is
        // on screen, since the effect is the same.
        Dimension screenSize = getScreenSize();
        return !bounds().intersects(0, 0, screenSize.width, screenSize.height);
    }
    
    /** record the current bounds with the window's positioner */
    private void saveBounds() {
        if (myPositioner != null && myPositioningKey != null) {
            myPositioner.saveBounds(this);
        }       
    } 
}


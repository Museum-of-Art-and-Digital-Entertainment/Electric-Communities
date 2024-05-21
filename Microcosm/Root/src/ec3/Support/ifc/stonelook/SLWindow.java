
/**
 * SLWindow.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 *  12/17/97    agm added check to set title to prohibit null titles
 */
package ec.ifc.stonelook;

import ec.trace.Trace;

import ec.ifc.app.ECApplication;
import ec.ifc.app.ECExternalWindow;

import netscape.application.*;

/**
 * Subclass of ExternalWindow that uses the stone-texture look.
 */
public class SLWindow extends ECExternalWindow 
                 implements Target
{

    static Trace tr = new Trace("ec.ifc.stonelook.SLWindow");
    
    /** title for hidden close box, useful only for debugging */
    private static final String CLOSE_BOX_TITLE = "close box";
    
    /** command wired up to close box */
    private static final String commandHideWindow = "hide window";
    
    /** subview for clients to put subviews in */
    private ContainerView myContainerView = null;

    /** button that acts as window's close box */
    private SLButton myCloseBox = null;

    /** does this window have a close box? */
    private boolean myHasCloseBox = true;

    //
    // Constructors
    //
    
    /**
     * Creates a new external window with the stone-texture look. 
     * The parameters specify the initial position and the size of the window
     */
    public SLWindow(int x, int y, int contentWidth, int contentHeight) {
        super(TITLE_TYPE);
        rootView().setBuffered(true);       
        
        // set up container that fills window
        myContainerView = new ContainerView(0, 0, contentWidth, contentHeight);
        myContainerView.setBorder(null);
        myContainerView.setBackgroundColor(StoneLook.lightBackgroundColor());
    
        Size windowSize = 
            windowSizeForContentSize(contentWidth, contentHeight);
        setBounds(x, y, windowSize.width, windowSize.height);
        
        addSubview(myContainerView);
        myContainerView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myContainerView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        // set up close box
        myCloseBox = new SLButton(0, 0, 0, 0);
        myCloseBox.setTransparent(true);
        myCloseBox.setTarget(this);
        // title appears in trace messages only
        myCloseBox.setTitle(CLOSE_BOX_TITLE);
        myCloseBox.setCommand(commandHideWindow);
        if (myHasCloseBox)
            addSubview(myCloseBox);
            setCancelButton(myCloseBox);
      }


    //
    // public methods
    //
    
    /**
     * Returns the button used as a close box for this window, if this
     * window has a close box, or null otherwise.
     */
     public SLButton getCloseBox() {
        if (myHasCloseBox)
            return myCloseBox;

        return null;
    }

    /**
     * Returns a container view positioned inside the window.
     * Additional window subviews should be added as subviews of this view.
     * If you treat this like a root view you will be happy and prosper.
     */
    public ContainerView getContainerView() {
        return myContainerView;
    }

    /**
     * Returns whether this window has a close box.
     * @see #setHasCloseBox
     */
    public boolean getHasCloseBox() {
        return myHasCloseBox;
    }

    /**
     * Processes "hide window" commands sent by close box.
     */
    public void performCommand(String command, Object data) {
        if (commandHideWindow.equals(command)) {
            hide();         
        } else {
            super.performCommand(command, data);
        }
    }

    /**
     * Sets whether this window has a close box, and redraws appropriately.
     * @see #getHasCloseBox
     */
    public void setHasCloseBox(boolean newValue) {
        if (newValue == myHasCloseBox)
            return;

        myHasCloseBox = newValue;
        if (newValue)
            addSubview(myCloseBox);
        else
            myCloseBox.removeFromSuperview();
    }

    /**
      * Sets the title for this window
      * if they pass a null title tell them but then deal with it in a reasonable way
      */
      
    public void setTitle(String theTitle) {
        if (theTitle == null) {
            tr.errorm("Null titles are not allowed");
            super.setTitle(" ");
        } else {
            super.setTitle(theTitle);
        }
    }
    
    /**
     * Overridden to draw immediately after showing, to avoid annoying period
     * of time in which white rectangle lingers on screen.
     */
    public void show() {
        super.show();
        rootView().draw();
        rootView().setRedrawAll(false); // see note in RootView.java about possible bug
        rootView().resetDirtyViews();
        
        // stop timing after the next redraw. This will have no effect
        // if timing isn't on, or if timing is on but no timing interval
        // has been started. It's easier to put this here than put a whole
        // bunch of stopTimingSoon()s in a bunch of different presenters
        Application app = Application.application();
        if (app instanceof ECApplication) {
            ((ECApplication)app).stopTimingSoon(null);
        }
    }
}


package ec.ifc.app;

import netscape.application.*;

import ec.e.run.Trace;

/**
 * A subclass of ListView that supports moving the selection with up/down arrow keys. 
 *
 * @author         John Sullivan
 */
public class ECListView extends ListView implements ECKeyFilter,
                                                    ECTabKeyDispenser {
    // instance variables
    private ECButton myDoubleClickButton = null;
    private ECTabKeyHandler myTabKeyHandler = null;
    /** set by constructor when this list view is for a popup menu */
    private boolean myIsListForPopup = false;
    /** used by popup lists to handle popped-up state properly */
    private boolean myStayPoppedUp = true;
    
    //
    // constructors
    //
    
    /** returns a new ECListView */
    public ECListView() {
        this(false);
    }
    
    /**
     * Returns a new ECListView. If <b>forPopup</b> is true,
     * the list view is for a popup menu and will handle mouse events
     * a little differently. This flavor of the constructor is normally
     * called only by the ECPopup constructor.
     */
    public ECListView(boolean forPopup) {
        super();
        myIsListForPopup = forPopup;
    }
    
    //
    // public and protected methods
    //
    
    /** overridden to reset popped-up state of popup lists */
    protected void ancestorWillRemoveFromViewHierarchy(View removedView) {
        super.ancestorWillRemoveFromViewHierarchy(removedView);
        if (myIsListForPopup) {
            myStayPoppedUp = true;      
        }
    }   
    
    /** Returns the button (if any) that is activated by a double click */
    public ECButton doubleClickButton() {
        return myDoubleClickButton;
    }
    
    /**
     * If the key was the up arrow, moves the selection to the previous
     * item. If the key was the down arrow, moves the selection to the
     * next item. If there was no selected item, or multiple selected items,
     * or the list view is disabled, or any other key was pressed, does nothing. 
     */
    public void keyDown(KeyEvent event)  {
        //System.out.println("ECListView.keyDown called with key " + event.key);

        if (!isEnabled()) {
            //System.out.println("list view disabled, so ignoring key event");
            return;
        }

        // ListView.multipleItemsSelected() is buggy and will return true if
        // exactly one item is selected
        if (selectedItems().size() > 1) {
            //System.out.println("multiple items are selected, so ignoring key event");
            return;
        }

        if (event.isUpArrowKey()) {
            // select previous item, if there is one
            int index = selectedIndex();
            if (index > 0)
                selectIndexViaKeyboard(index - 1);
            else if (index < 0)
                selectIndexViaKeyboard(count() - 1);
            return;
        } else if (event.isDownArrowKey()) {
            // select next item, if there is one
            int index = selectedIndex();
            if (index < count() - 1)
                selectIndexViaKeyboard(index + 1);
            else if (index < 0)
                selectIndexViaKeyboard(0);
            return;
        } else if (event.isPageUpKey()) {
            pageUpOrDown(true);
            return;
        } else if (event.isPageDownKey()) {
            pageUpOrDown(false);
            return;
        }

    }
    
    /**
     * Overridden so that any mouse drag on a pop-up menu list
     * will cause the menu to be hidden when the mouse is released.
     */
    public void mouseDragged(MouseEvent event) {
        if (myIsListForPopup) {
            myStayPoppedUp = false;
        }
        super.mouseDragged(event);
    }

    /**
     * Overridden to make this list view the focused view
     * and to handle leaving pop-up menus open on a single click
     */
    public void mouseUp (MouseEvent event) {
        // if this is the list of a popup that should remain
        // popped up, don't call super (so it won't perform
        // the command, and will stay popped up), but do
        // set it up to close the menu on the next click
        if (myIsListForPopup && myStayPoppedUp) {
            myStayPoppedUp = false;
        } else {
            super.mouseUp(event);
        } 

        setFocusedView();
    }
    
    /** Overridden just to supply tracing information */
    public void sendCommand() {
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem("selected " 
                + TraceUtils.traceDescription(selectedItem()));
        }
        super.sendCommand();
    }

    /**
     * Overridden to auto-click the doubleClickButton, if
     * there is one (otherwise calls inherited method)
     * @see #setDoubleClickButton
     */
    public void sendDoubleCommand() {
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem("double-clicked " 
                + TraceUtils.traceDescription(selectedItem()));
        }
        
        if (myDoubleClickButton != null) {
            myDoubleClickButton.click();
        } else {
            super.sendDoubleCommand();
        }
    }
    
    /**
     * Sets a button that is activated automatically on a double-click.
     * If this is set to null (the default), the double-click will be
     * processed in the inherited way, by sending the list's double-
     * click command to the list's target. <b>button</b> must be either
     * PUSH_TYPE or CONTINUOUS_TYPE, or null.
     */
    public void setDoubleClickButton(ECButton button) {
        if (button != null) {
            int buttonType = button.type();
            if (buttonType != Button.PUSH_TYPE 
                    && buttonType != Button.CONTINUOUS_TYPE) {
                if (Trace.gui.warning && Trace.ON) {
                    Trace.gui.warningm("invalid button type, ignoring request");
                }
                return;
            }           
        }

        myDoubleClickButton = button;
    }

    /**
     * Responsibility from ECTabKeyDispenser. Sets object that will
     * handle tab key presses.
     */
    public void setTabKeyHandler(ECTabKeyHandler newHandler) {
        myTabKeyHandler = newHandler;
    }

    /**
     * Responsibility from ECTabKeyDispenser. Returns object that will
     * handle tab key presses.
     */
    public ECTabKeyHandler tabKeyHandler() {
        return myTabKeyHandler;
    }

    /** Returns false so the cancel button (if any) can be activated */
    public boolean wantsEscapeKey() {
        return false;
    }

    /** Returns false so the default button (if any) can be activated */
    public boolean wantsReturnKey() {
        return false;
    }
    
    //
    // private methods
    //
    
    private void pageUpOrDown(boolean up) {
        View superview = superview();
        if (!(superview instanceof ScrollView)) {
            return;
        }
        
        ScrollView scrollView = (ScrollView)superview;
    
        int jumpBy = scrollView.height()/rowHeight();
        if (jumpBy > 1) {
            // unless there's only one item showing, jump one item less
            // so user retains a little context;
            jumpBy -= 1;
        }

        int index = selectedIndex();
        int newIndex;
        if (up) {
            newIndex = Math.max(index - jumpBy, 0);
        } else {
            newIndex = Math.min(index + jumpBy, count() - 1);
        }
        
        if (newIndex != index) {
            selectIndexViaKeyboard(newIndex);
        }
    }

    private void selectIndexViaKeyboard(int index) {
        selectOnly(itemAt(index));
        scrollItemAtToVisible(index);
        sendCommand();
    }
}



package ec.ifc.app;

import netscape.application.*;
import netscape.util.*;

import ec.e.run.Trace;

public class ECRootView extends RootView implements ECTabKeyDispenser
{
    private int lastx;
    private int lasty;
    private Timer timer;
    private boolean mouseDown = false;
    private boolean showingTip = false;
    private Hashtable tips; 
    private TextField tipField;
    private Object lastTipElement = null;
    private View lastView = null;
    private ECButton myDefaultButton = null;
    private ECButton myCancelButton = null;
    private ECTabKeyHandler myTabKeyHandler = null;
    
    private static Vector commands = new Vector();
    
    private static final String ShowTip = "ShowTip";
    private static final String HideTip = "HideTip";
    
    /** delay between the cursor resting on a point and the tip appearing */
    private static final int ShowTipDelay = 1000;

    /** delay until tip is hidden */
    // Note this is very long (1 minute). Ellen doesn't want the tips to
    // hide at all, but in case she changes her mind I left in the tip-hiding
    // mechanism with a very long delay -- JWS 6/24/97  
    private static final int HideTipDelay = 60000;
    
    private static final int TipFieldHeight = 16;
    /** x distance between cursor position and top-left corner of tip field */
    private static final int TIP_OFFSET_X = 10;
    /** y distance between cursor position and top-left corner of tip field */
    private static final int TIP_OFFSET_Y = 20;
    /** alternate y offset when standard one is out of bounds */
    private static final int TIP_ABOVE_OFFSET_Y = 4; 
    
    static {
        commands.addElement(ShowTip);
        commands.addElement(HideTip);
    }
        
    //
    // Constructor
    //
    
    public ECRootView()
    {
        super();
        setupRootView();
    }

    //
    // static methods
    //
    
    /** Returns whether or not tips should be displayed */  
    private static boolean tipsEnabled() {
        ECApplication app = (ECApplication)Application.application();
        return app.tipsEnabled();
    }
    
    //
    // public methods
    //
    
    /**
     * Pins width and height to those of minSize(), unless minSize is null. This allows
     * a workaround for subview resizing bugs that occur after subviews have been sized
     * to negative widths or heights. This would not be necessary if ExternalWindow 
     * respected its own minSize, but a comment in the IFC documentation claims that
     * this is a bug in AWT.
     */
    public void setBounds(int x, int y, int width, int height)  {
        Size minSize = minSize();

        if (minSize != null)  {
            if (minSize.width > width)  {
                width = minSize.width;
            }

            if (minSize.height > height)  {
                height = minSize.height;
            }
        }

        super.setBounds(x, y, width, height);
    }

    /**
     * Handles tip displays and default and cancel button key equivalents.
     */
    public void processEvent (Event event)
    {
        if (handleSpecialKeys(event)) {
            return;
        }
        
        boolean isMouseEvent = event instanceof MouseEvent;
        
        // take down any modal popup menu that's still up, unless
        // the click is in it
        if (isMouseEvent && event.type() == MouseEvent.MOUSE_DOWN) {
            // if we closed a modal popup with this click, then do not
            // further process the click (i.e., don't use it to do something
            // else also). This is the safe behavior, and is what the Mac
            // does. Unfortunately, evil Windows does use the click to do
            // something else also, so we might want to change this to match
            // Windows's yucky standard. To do that, just call handleModalPopup,
            // ignoring its return value (then continuing on to the
            // super.processEvent call)
            if (handleModalPopup((MouseEvent)event)) {
                return;
            }
        }
        
        super.processEvent(event);
        
        if (showingTip) {
            hideTip();
        }

        timer.stop();
        
        if (!isMouseEvent) {
            return;
        }
        MouseEvent mouseEvent = (MouseEvent)event;

        int type = mouseEvent.type();
        if (type == MouseEvent.MOUSE_DOWN) {
            mouseDown = true;
        }
        else if (type == MouseEvent.MOUSE_UP) {
            mouseDown = false;
        }
        if (mouseDown) {
            return;
        }
        
        lastx = mouseEvent.x;
        lasty = mouseEvent.y;

        lastView = viewForMouse(lastx, lasty);
        if (lastView == null) {
            return;
        }

        lastTipElement = tips.get(lastView);
        if (lastTipElement == null) {
            return;
        }
        timer.setCommand(ShowTip);
        timer.setInitialDelay(ShowTipDelay);
        timer.start();
    }

    public void performCommand (String string, Object object)
    {
        if (ShowTip.equals(string)) {
            showTip();
        }
        else if (HideTip.equals(string)) {
            hideTip();
        }
        else {
            super.performCommand(string, object);
        }
    }

    public boolean canPerformCommand (String string)
    {
        return (commands.contains(string) || super.canPerformCommand(string));
    }

    public void setTipForView(String tip, View view) {
        // If tip for this item is up, remove it.
        // Might be nicer to refresh it; this would require more mechanism
        if (view == lastView) {
            hideTip();
        }
        tips.put(view, tip);
    }
    
    public void setTipForView(ECTipViewOwner owner, View view) {
        tips.put(view, owner);
    }
    
    public void removeAllTips() {
        hideTip();
        tips.clear();
    }
    
    public void removeTipForView(View view) {
        // If tip for this item is up, remove it.
        if (view == lastView) {
            hideTip();
        }
        tips.remove(view);
    }

    /** Makes this root view the target of keyboard events. */
    public void requestFocus() {
    // setFocusedView() should do the right thing here, but it doesn't work
    // with BLANK_TYPE external windows, so we go all the way down to the
    // AWT method, which works for both TITLE_TYPE and BLANK_TYPE windows.  

        java.awt.Panel panel = 
            netscape.application.AWTCompatibility.awtPanelForRootView(this);
        panel.requestFocus();
    }

    /**
     * Returns the cancel button. The cancel button is activated by
     * the Escape key unless the focused view uses it.
     */
    public ECButton cancelButton() {
        return myCancelButton;
    }

    /**
     * Returns the default button. The default button that is activated by
     * the Return or Enter key unless the focused view uses it.
     */
    public ECButton defaultButton() {
        return myDefaultButton;
    }

    /**
     * Activates the cancel button if appropriate.
     * Other classes can call this explicitly to activate the cancel button
     * (if any), but it's usually easier to implement ECKeyFilter and
     * return false for wantsEscapeKey.
     * @see #processEvent
     * @see ECKeyFilter
     */
    public void handleEscapeKey() {
        if (Trace.gui.usage && Trace.ON) {
            if (myCancelButton == null) {
                Trace.gui.usagem("Escape pressed, but no cancel button");
            } else {
                Trace.gui.usagem("Escape sent to cancel button: " 
                    + TraceUtils.traceDescription(myCancelButton));
            }
        }
        activateButton(myCancelButton);
    }

    /**
     * Activates the default button if appropriate.
     * Other classes can call this explicitly to activate the default button
     * (if any), but it's usually easier to implement ECKeyFilter and
     * return false for wantsReturnKey.
     * @see #processEvent
     * @see ECKeyFilter
     */
    public void handleReturnKey(boolean unshifted) {
        if (Trace.gui.usage && Trace.ON) {
            if (myDefaultButton == null || !myDefaultButton.isEnabled()) {
                Trace.gui.usagem(
                    "Return/Enter pressed, but no default button, so converting Return/Enter to Tab");
            } else {
                Trace.gui.usagem(
                    "Return/Enter sent to default button: " 
                    + TraceUtils.traceDescription(myDefaultButton));
            }
        }
        if (myDefaultButton != null && myDefaultButton.isEnabled()) {
            activateButton(myDefaultButton);        
        } else {
            // act just like tab key
            int mask = unshifted ? 0 : KeyEvent.SHIFT_MASK;
            processEvent(new KeyEvent(System.currentTimeMillis(), KeyEvent.TAB_KEY, mask, true));
        }
    }

    /**
     * Sets the cancel button.
     * @see #cancelButton
     */
    public void setCancelButton(ECButton newCancelButton) {
        myCancelButton = newCancelButton;
    }

    /**
     * Sets the default button.
     * @see #defaultButton
     */
    public void setDefaultButton(ECButton newDefaultButton) {
        myDefaultButton = newDefaultButton;
    }
    
    /**
     * Sets the object that handles tab keys pressed when
     * there is no focused view.
     */
    public void setTabKeyHandler(ECTabKeyHandler newHandler) {
        myTabKeyHandler = newHandler;
    }
    
    /**
     * Returns the object that handles tab keys pressed
     * when there is no focused view
     */
    public ECTabKeyHandler tabKeyHandler() {
        return myTabKeyHandler;
    }

    //
    // private methods
    //

    /** simulate a button push on the specified button */
    private boolean activateButton(ECButton button) {
        if (button == null) {
            return false;
        }

        button.click();     
        return true;
    }
    
    /** take down the currently-displayed popup menu, if any */
    private boolean handleModalPopup(MouseEvent event) {
        Application app = Application.application();
        View modalView = app.modalView();
        if (modalView != null && app instanceof ECApplication) {
            ECApplication ecApp = (ECApplication)app;
            Popup currentPopup = ecApp.currentPopup();
            if (currentPopup != null && modalView == currentPopup.popupWindow()
                                     && viewExcludedFromModalSession(viewForMouse(event.x, event.y))) {
                ecApp.cancelCurrentPopup();
                return true;
            }
        }
        return false;
    }

    /**
     * If the focused view doesn't want the key, handles it here and returns
     * true, else returns false.
     */
    private boolean handleReturnOrEscapeKey(boolean isReturn, boolean unshifted) {
        View keyView = focusedView();
        ECKeyFilter filter = null;
        if (keyView instanceof ECKeyFilter)
            filter = (ECKeyFilter)keyView;

        // we'll handle the return key for the default button if
        // we can tell the focused view doesn't want it
        if (isReturn) {
            if (keyView == null ||
                    (filter != null && !filter.wantsReturnKey())) {
                handleReturnKey(unshifted);
                return true;
            }               
        } else {
            // we'll handle the escape key for the cancel button if
            // we can tell the focused view doesn't want it
            if (keyView == null ||
                    (filter != null && !filter.wantsEscapeKey())) {
                handleEscapeKey();
                return true;
            }               
        }
        
        return false;
    }

    /**
     * Returns true if the event is a key event handled explicitly
     * here (return key, escape key, tab key, in some cases)
     */
    private boolean handleSpecialKeys(Event event) {
        if (!(event instanceof KeyEvent)) {
            return false;   
        } 
        
        KeyEvent keyEvent = (KeyEvent)event;
        
        if (keyEvent.type() != KeyEvent.KEY_DOWN) {
            return false;
        }

        boolean returnKey = keyEvent.isReturnKey();
        if (returnKey || keyEvent.isEscapeKey()) {
            return handleReturnOrEscapeKey(returnKey, !keyEvent.isShiftKeyDown());
        }
        
        boolean tabKey = keyEvent.isTabKey();
        boolean backTabKey = keyEvent.isBackTabKey();
        if (tabKey || backTabKey) {
            return handleTabKey(tabKey);
        }

        // it wasn't a key we're interested in      
        return false;
    }

    /**
     * If the focused view dispenses the tab key, handles it here and returns
     * true, else returns false.
     */
    private boolean handleTabKey(boolean forward) {
        ECTabKeyDispenser dispenser = null;
        View keyView = focusedView();
        
        if (keyView == null) {
            // special case where the root view itself will act as the dispenser
            dispenser = this;
        } else if (keyView instanceof ECTabKeyDispenser) {
            dispenser = (ECTabKeyDispenser)keyView;
        }
        
        // focused view doesn't dispense tab keys, so tab key will be
        // processed normally (not here)
        if (dispenser == null) {
            return false;
        }
        
        ECTabKeyHandler handler = dispenser.tabKeyHandler();
        
        // no tab key handler, so tab key will be processed normally (not here)
        if (handler == null) {
            return false;
        }

        handler.handleTabKey(dispenser, forward);
        return true;
    }

    private void setupRootView() {
        timer = new Timer(this, ShowTip, ShowTipDelay);
        timer.setRepeats(false);        
        tips = new Hashtable();     
    }
    
    private void showTip() {
        if (!tipsEnabled()) {
            return;
        }
    
        if (tipField == null) {
            tipField = new TextField(0, 0, 100, TipFieldHeight);
            tipField.setEditable(false);
            tipField.setBorder(LineBorder.blackLine());
            tipField.setJustification(Graphics.CENTERED);
            tipField.setBackgroundColor(Color.white);
        }
        int x = lastx;
        int y = lasty;
        String tipString = null;
        timer.stop();
        if (lastTipElement == null) {
            return;
        }
        if (lastTipElement instanceof ECTipViewOwner) {
            Point p = convertToView(lastView, x, y);
            ECTipViewOwner owner = (ECTipViewOwner)lastTipElement;
            tipString = owner.getTipForPositionInView(lastView, p.x, p.y);
            if (tipString == null) {
                return;
            }
        }
        else {
            tipString = (String)lastTipElement;
        }
        tipField.setStringValue(tipString);
        tipField.sizeToMinSize();
        tipField.sizeBy(6, 0);
        
        // start with tip a little below and to right of cursor
        int tipX = x + TIP_OFFSET_X;
        int tipY = y + TIP_OFFSET_Y;
        
        // squoosh tip left to fit in this rootview if necessary
        int tipWidth = tipField.width();
        int rootWidth = width();
        if (tipX + tipWidth > rootWidth) {
            tipX = Math.max(rootWidth - tipWidth, 0);            
        }
        
        // move tip above cursor to fit in this rootview if necessary
        int tipHeight = tipField.height();
        if (tipY + tipHeight > height()) {
            tipY = y - TIP_ABOVE_OFFSET_Y - tipHeight;
        }
        tipField.moveTo(tipX, tipY);
        
        addSubview(tipField);
        tipField.draw();
        
        timer.setCommand(HideTip);
        timer.setInitialDelay(HideTipDelay);
        timer.start();
        showingTip = true;
    }
    
    private void hideTip() {
        if (tipField == null || !showingTip) {
            return;
        }
        
        Rect tipBounds = tipField.bounds();
        removeSubview(tipField);
        draw(tipBounds);
        
        showingTip = false;
    }
}

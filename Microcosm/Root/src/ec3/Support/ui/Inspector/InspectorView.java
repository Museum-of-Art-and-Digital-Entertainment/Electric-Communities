package ec.ui;

import ec.e.inspect.*;
import ec.ifc.app.*;
import netscape.application.*;
import ec.e.file.EStdio;
import netscape.util.Vector;
import netscape.util.Enumeration;

/**

 * Class for displaying values in an Inspector (see ec.inspect.Inspector) in an
 * IFC view. <p>

 * This class is misnamed for historical reasons. It is actually not a
 * subclass of View. Rather, it is an object that manages the
 * InspectorViewToolbar view and the scrolling list view of all the
 * elements which are part of the ExtermalWindow's rootview. These are
 * managed in synchronized pairs - they added and removed from the
 * window together.

 * An inspectorView contains an inspector for an object and never lets
 * go of it. The InspectorView is stored in a table in
 * IFCInspector. This means that once you've asked to inspect an
 * object, then anytime you want to inspect the same object again you
 * get the same InspectorView. This is true even if you reach the
 * object through a different reference path.

 * The window that the object is inspected in may change, though. This
 * is controlled by the following rules:<p>

 * If you are re-inspecting some object, and its window is already
 * open somewhere, then that window is brought to the top without
 * moving it or resizing it. <p>

 * Otherwise, if you ask for a new window (e.g. through a right-click
 * on the button for the object), then you get a new window. If the
 * object has been displayed before, then the new window will be moved
 * and resized to the last used position and size for that object. <p>

 * Otherwise the view replaces the view in the current window. If the
 * object has been displayed before, then the window is resized to the
 * last used size but is not moved.  If the object has not been
 * displayed before, then a default size is used<p>

 * When clicking the "Back" button, similar rules apply. If the
 * previous window is on the screen somewhere, then it is brought to
 * the top. It is not moved or resized.<p>

 * Otherwise, if the click was a right-click, then a new window is
 * opened for the object. If it has been shown before, then the new
 * window is positioned and sized the way it was when it was last
 * visible.<p>

 * Otherwise (i.e. the back button was left-clicked) the object is
 * displayed in the current window. The current window is not moved
 * but is resized to match the last displayed size.<p>

 */

public class InspectorView extends Object implements WindowOwner {

    /* The currently displayed inspector, describes the object */
    private Inspector myInspector = null;

    // These get cleared when we lose our window
    private ECExternalWindow myWindow = null;
    private ScrollGroup myScrollGroup = null;
    private InspectorViewToolBar myToolBar = null;
    private FieldListView listView = null;
    private Vector fieldViewsVector = null;

    // Tool bar flags etc.
    private boolean useFullNames = false;
    private boolean showAddresses = false;
    private String windowName = null; // name of window, showing drill-down chain

    // View history management
    private InspectorView previousView = null;
    public Rect myWinBounds = null;
    private float myScrollPercent = (float)0.0;

    // Capabilities
    private IFCInspectorUI myUI = null;

    /**

     * Constructor, creates inspector view with given dimensions and
     * specified checkboxes and buttons in the toolbar.

     */

    InspectorView(IFCInspectorUI myUI,
                  Inspector inspector,
                  String windowName,
                  InspectorView previousView) {
        myWinBounds = null;
        this.myUI = myUI;
        this.myInspector = inspector;
        this.windowName = windowName;
        this.previousView = previousView;
    }

    /**

     * Returns true if we still have a window we can display ourselves in
     
     */

    public boolean hasWindow() {
        return (myWindow != null);
    }

    /**

     * Clear our window reference - someone decided to re-use it for
     * some other view. This is actually called from our
     * InspectorViewToolBar part since we aren't an immediate subview
     * of the Window ourselves.

     */

    /* package */ void lostItsWindow() {
        fieldViewsVector = null; // Discard button list since it's now invalid.
        myWindow = null;
        if (myScrollGroup != null) {
            myScrollPercent = myScrollGroup.vertScrollBar().scrollPercent();
            myScrollGroup.removeFromSuperview();
            myScrollGroup = null;
        }
        if (myToolBar != null) {
            myToolBar.removeFromSuperview();
            myToolBar = null;
        }
    }

    /**

     * Show this InspectorView in the given window, if
     * given. Otherwise create a new window. Handle re-use of window
     * since they may contain items that are currenty displayed.

     * We pass in parentWindow only to get its coordinates in case we
     * are creating a new window and want to stagger the position of
     * it relative to its parent.

     */

    public void showSelfInWindow(ECExternalWindow win, ECExternalWindow parentWindow) {
        /*
        System.out.println("\n\n\n========================================" +
                           "\nSSIW    win: " + win +
                           "\nSSIW parent: " + parentWindow +
                           "\nSSIW   this: " + this + 
                           "\nSSIW bounds: " + myWinBounds);

                           */
        if (win != null) {      // Re-use an existing window?
            Enumeration e = win.rootView().subviews().elements();
            while (e.hasMoreElements()) {     // We must clean it up first.
                View v = (View)e.nextElement();
                if (v instanceof InspectorViewToolBar) ((InspectorViewToolBar)v).lostItsWindow();
            }
            myWindow = win;     // Re-use the given old window.
            if (myWinBounds == null) {
                myWinBounds = new Rect(win.bounds());
                if (myWinBounds.width < 400) myWinBounds.width = 400;
                myWinBounds.height = 0; // Fix up height later
            } else {
                // If we have window use window's position and myWinBounds size.
                myWinBounds.x = win.bounds().x;
                myWinBounds.y = win.bounds().y;
            }
        } else {                // Make new window
            myWindow = new ECExternalWindow();
            if (myWinBounds == null) {
                if (parentWindow != null) { // Stagger new window from parent if we have one
                    myWinBounds = new Rect(parentWindow.bounds());
                    if (myWinBounds.width < 400) myWinBounds.width = 400;
                    myWinBounds.height = 0; // Fix up height later
                    myWinBounds.x += 20;
                    myWinBounds.y += 20; // Stagger window a bit`
                } else { // Otherwise just start at top left of screen with default size.
                    (new Error("FYI; No parent window")).printStackTrace();
                    myWinBounds = new Rect(60,60,400,0); 
                }
            } else { // Just use myWinBounds as window bounds.
                
            }
        }
        updateVisibleButtons();
        showContent(null, true);
    }

    /**

     * Create new ListView.

     */

    void createListView(int suggestedWidth) {
        if (listView != null) {
            listView.removeFromSuperview();
        }
        listView = new FieldListView(myInspector, useFullNames, showAddresses,
                                     myUI, windowName, this);
        // We never change the height - we scroll around in this view.
        if (suggestedWidth > 0) listView.sizeTo(suggestedWidth, listView.height());
        listView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    }

    InspectorViewToolBar createNewToolBar() {

        // Figure out all options for the tool bar.

        boolean showBackButton = (previousView != null);
        boolean showTargetBreakBox = myInspector.isEObject();
        boolean targetBroken = false;
        if (showTargetBreakBox) targetBroken = myInspector.isWatchedTarget();
        boolean showReferencersButtons = false;
        if (Inspector.runningUnderDebuggingVM() &&
            ! (myInspector instanceof ReferencersInspector))
            showReferencersButtons = true;

        // Content width is now correctly determined as
        // myWinBounds.width.  That's all we needed to be able to
        // create the toolbar.

        InspectorViewToolBar result = new InspectorViewToolBar
          (0, 0, myWinBounds.width, InspectorViewToolBar.HEIGHT,
           this,
           showBackButton,
           showTargetBreakBox,
           targetBroken,
           false,
           showReferencersButtons);
        return result;
    }

    /**

     * Show the content, i.e. the InspectorView. We (re)create the
     * ToolBar and the scroll bar region here so that they are up to
     * date since the data and the path determine what buttons are
     * included in the tool bar and the size of the InspectorView and
     * current window (if given) determines the size of the scrolling
     * region. <p>

     * This assumes we already have a window and will fail if we
     * don't.<p>

     * It also assumes that myWinBounds is correctly set up except for
     * one detail: If myWinBounds.height is zero, then we are supposed
     * to set it to whatever looks good, i.e. we try to make it the
     * right size for the listview so that we don't have to scroll at
     * all.

     */

    public void showContent(Rect winBounds, boolean createToolBar) {

        if (winBounds != null) {
            myWinBounds = new Rect(winBounds);
        }

        else if (myWinBounds == null) {
            myWinBounds = new Rect(myWindow.bounds());
        }

        // createListView creates the list view and then attempts to
        // resize its width to the given width, unless given as zero.

        createListView(myWinBounds.width - 24);

        // Look at the size we actually got and use that if we can.
        // Also add two small constants here to make things look right.

        int listWidth = listView.width() + 16; // Add scroll bar width
        int listHeight = listView.height() + 4; // Add 4 so all shows without scrolling
        if (listHeight > 500) listHeight = 500; // Max out the list height, scroll instead.

        if (createToolBar) {
            myToolBar = createNewToolBar();
        }
        else myToolBar.setDirty(true);

        myToolBar.updateColors(previousView); // Start out showing right color

        int toolHeight = myToolBar.height(); // Use the height we actully got.

        // If we had an old window (and hence a non-zero
        // myWinBounds.height) then we want to use that height
        // otherwise we want to use the height of the listview. Since
        // the height of the window includes the toolbar height we ned
        // to convert back and forth here.

        // otherheight is the height of the window's title bar.  We
        // don't want to use windowsizeforcontentsize etc since we
        // don't want to resize the window quite yet.

        int otherHeight = 28;   // Window title bar etc overhead.

        if (myWinBounds.height != 0) { // We have an old window size - Use that height.
            listHeight = myWinBounds.height - (toolHeight + otherHeight);
        }
        else myWinBounds.height = listHeight + (toolHeight + otherHeight); // Use listview height

        // Make sure we can actually display something in the resulting height.
        if (myWinBounds.height < (toolHeight + 20)) myWinBounds.height = 400;

        // At this point, myWinBounds, listWidth, and listHeight are all correct.
        // Now we can resize the window.

        myWindow.setBounds(myWinBounds);

        // Create a scroll group - a scrolling view and a scrollbar

        myScrollGroup = new ScrollGroup(0, toolHeight, listWidth, listHeight);
        myScrollGroup.setHasVertScrollBar(true);
        myScrollGroup.setBorder(ECScrollBorder.border());

        // Now that window's bounds are set, it's safe to add in all
        // the subviews. Doing so earlier can confuse the automatic resize
        // instruction code

        myScrollGroup.setContentView(listView);
        myWindow.addSubview(myScrollGroup);
        myWindow.addSubview(myToolBar);
        myWindow.setOwner(this);

        // Scroll to remembered position, or 0.0 if new window.
        myScrollGroup.vertScrollBar().setScrollPercent(myScrollPercent);

        // And now we finally dare make the subviews resizable.

        myScrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        myScrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myToolBar.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);

        // An Inspector never knows its name - has to be provided from caller.

        myWindow.setTitle(windowName);

        // Finish up by setting various flags.

        myWindow.rootView().setBuffered(true);
        myToolBar.setDirty(true);
        myScrollGroup.setDirty(true);
        myWindow.show();
    }

    public void refreshObjectView() {
        showContent(myWindow.bounds(), false);
    }

    void setUseFullNames(boolean useFullNames) {
        this.useFullNames = useFullNames;
        showContent(myWindow.bounds(), false);
    }

    void setShowAddresses(boolean showAddresses) {
        this.showAddresses = showAddresses;
        showContent(myWindow.bounds(), false);
    }

    void setRunQueueTargetBreakState(boolean breakObject) {
        myInspector.setRunQueueTargetBreakState(breakObject);
    }

    boolean isInspecting(Object object) {
        return myInspector.isInspecting(object);
    }

    public void acceptMessage() {
        EStdio.err().println("Accept message");

    }

    public void inspectSource() {
        EStdio.err().println("Inspect source");

    }

    public void inspectMessage() {
        EStdio.err().println("Inspect message");

    }

    public void inspectTarget() {
        EStdio.err().println("Inspect target");

    }

    public void showQueue() {
        EStdio.err().println("Show object in queue");

    }

    void moveToFront() {
        myWindow.show();
    }

    /**

     * Allow button FieldView objects to add themselves to my
     * fieldViewsVector so that we can change their colors effectively

     */

    public void addToFieldViewsVector(FieldView f) {
        if (fieldViewsVector == null) fieldViewsVector = new Vector(5);
        fieldViewsVector.addElement(f);
    }

    /**

     * Update all buttons in all windows. Some of them will turn blue
     * or cyan depending on the new state of other views.  You cannot
     * limit the changes to only those windows that called this window
     * into existence. Instead, we just do every window we have.

     */

    public void updateVisibleButtons() {
        java.util.Enumeration e = myUI.inspectorViewsEnumeration();
        while (e.hasMoreElements()) {
            InspectorView view = (InspectorView)e.nextElement();
            if (view != null && view.hasWindow()) view.updateColors();
        }
    }
            
    void updateColors() {
        if (myToolBar != null) myToolBar.updateColors(previousView); // Start with back button
        if (fieldViewsVector == null) return;

        // Loop through all our object reference buttons
        
        Enumeration e = fieldViewsVector.elements();
        while (e.hasMoreElements()) {
            FieldView fv = (FieldView)e.nextElement();
            fv.updateFieldColor();
        }
    }

    void backCommand(boolean reUseWindow) {
        if (previousView != null) {
            if (previousView.hasWindow()) {
                previousView.moveToFront();
                return;
            }
            if (reUseWindow) {
                myWinBounds = new Rect(myWindow.bounds()); // Remember our bounds now
                myScrollPercent = myScrollGroup.vertScrollBar().scrollPercent();
                ECExternalWindow win = myWindow;
                lostItsWindow();
                previousView.showSelfInWindow(win, win);
            } else {
                previousView.showSelfInWindow(null, myWindow);
            }
            updateVisibleButtons();
        }
    }

    public void setMessage(String message) {
        myToolBar.setMessage(message);
    }

    public void showReferencers() {
        System.out.println("ShowReferencers called on " + myInspector);

        int address = myInspector.addressOf();
        
        System.out.println("Address is " + Inspector.addressString(address,1));

        TraceInfo[] referencers = myInspector.getReferencers();
        if (referencers == null) {
            System.out.println("getReferencers() returned null");
            return;
        }
        System.out.println("Dump of referencers:");

        for (int i = 0; i < referencers.length; i++) {
            System.out.println(referencers[i].toString());
        }

        myUI.inspectObject(referencers, " ->" + myInspector.getMyObjectName());
    }

    public void showAllReferencers() {
        myInspector.printFullTrace();
    }

    public void dumpAsRoot() {
        System.out.println("dumpAsRoot called on " + myInspector);
        myInspector.dumpObject(System.out);
    }

    /*

     * Responsibilities from WindowOwner - we only care about windowDidHide.

     */

    public void windowDidBecomeMain(netscape.application.Window window) {
    }

    public void windowDidHide(netscape.application.Window window) {
        //        myUI.removeInspectorView(this);
    }

    public void windowDidResignMain(netscape.application.Window window) {
    }

    public void windowDidShow(netscape.application.Window window) {
    }

    public boolean windowWillHide(netscape.application.Window window) {
        myWinBounds = new Rect(myWindow.bounds()); // Remember our bounds now
        myScrollPercent = myScrollGroup.vertScrollBar().scrollPercent();
        lostItsWindow();
        updateVisibleButtons();
        return true;
    }

    public boolean windowWillShow(netscape.application.Window window) {
        return true;
    }

    public void windowWillSizeBy(netscape.application.Window window, Size size) {
    }
}


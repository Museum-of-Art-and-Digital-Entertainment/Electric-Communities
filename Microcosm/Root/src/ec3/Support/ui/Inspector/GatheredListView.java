package ec.ui;

import java.util.*;
import ec.ifc.app.*;
import netscape.application.*;

/**

 * Display all gathered objects in a specific category in an IFC view,
 * to allow user to pick a gathered object for inspection by clicking
 * on its button.

 * The GatheredListView displays a button for each gathered object,
 * labeled by its gathered name. The buttons are displayed in a
 * fixed-sized view which will be displayed in a containerview which
 * will be a scrolling area.

 */

public class GatheredListView extends Object implements WindowOwner, Target {

    private ScrollingButtonListView scrollingButtonListView = null;
    private ECExternalWindow ourWindow = null;
    private String ourTitle = null;
    private GatheredListViewToolBar toolBar = null;
    private InspectorUIPreferences myPrefs = null;
    private int winContentWidth = 200; // If 0, we use the value in myPrefs
    private int winContentHeight = 0;
    private Hashtable gatheredObjects = null;
    private IFCInspectorUI myUI = null;

    /**
     * Create a window displaying a button for each key in the given hashtable.
     * @param name The category of the objects, and the title of the window.
     * @param catObjects A hashtable of objects in this category
     * @param preferences An instance containing Inspector's user preference values.
     */

    GatheredListView(String categoryName, Hashtable catObjects,
                            InspectorUIPreferences preferences, ECExternalWindow parentWindow,
                            IFCInspectorUI myUI) {

        this.gatheredObjects = catObjects;
        this.myPrefs = null;
        //        this.myPrefs = preferences;
        this.myUI = myUI;

        // The window only needs to be created once.

        createGatheredObjectWindow(categoryName, parentWindow);

        // We may want to recompute the buttons in view of recent changes to the hashtable we were given

        displayAsButtons(gatheredObjects);     // Can be called again to refresh view, if so desired
        // Create a tool bar

        toolBar = new GatheredListViewToolBar(this);
        ourWindow.addSubview(toolBar);
        ourWindow.show();
    }

    /**

     * Create the scrolling button list and the scrolling view that
     * displays it, given the hashtable that contains the objects we
     * want to display (and their names). <p>

     * This method can be called later to refresh the contents of the
     * button list in case more categories have been added to the
     * category hashtable.

     */

    private void displayAsButtons(Hashtable buttonNames) {

        // Create the scrolling button view first. It determines the
        // width of everything.  if we already had one we want to
        // remove it from the view hiearachy first.  Otherwise we
        // would end up with multiple similar subviews in the same
        // containing view.

        if (scrollingButtonListView != null) {
            scrollingButtonListView.removeFromSuperview();
        }

        if (buttonNames != null) {
            synchronized(buttonNames) {
                scrollingButtonListView = new ScrollingButtonListView
                  (buttonNames, this, myPrefs, winContentWidth);

                //     winContentWidth = scrollingButtonListView.width();

                // Move the scrolling view down to below the toolbar
                // -- we know the toolbar height
                scrollingButtonListView.moveTo(0,GatheredListViewToolBar.HEIGHT);

                // Compute the height of everything in our view
                winContentHeight = scrollingButtonListView.height()
                                   + GatheredListViewToolBar.HEIGHT;

                // size the window appropriately
                // since our height might have changed (by adding or removing buttons).
                Size windowSize = ourWindow.windowSizeForContentSize
                  (winContentWidth, winContentHeight);

                // This is an IFC or AWT bug: If you attempt to do
                // sizeTo() on the window using windowSize then the
                // window will jump to its initial position if it has
                // been moved in the meantime. Doing this the hard way
                // as below works - the window stays put when you
                // click the Refresh button.

                Rect bounds =  ourWindow.bounds();
                bounds.width = windowSize.width;
                bounds.height =  windowSize.height;
                ourWindow.setBounds(bounds);
                
                // Build view hierachy now that everything is 
                // correctly placed and sized.
                ourWindow.addSubview(scrollingButtonListView);
            }
        }

    }

    void moveToFront() {
        // show will move it to front and activate it
        ourWindow.show();
    }

    Rect getBounds() {
        return ourWindow.bounds();
    }
    
    private void createGatheredObjectWindow(String title,
                                            ECExternalWindow parentWindow) {
        int x, y;
    
        if (parentWindow != null) {
            Rect parentBounds = parentWindow.bounds();
            x = parentBounds.x + 20;
            y = parentBounds.y + 40; // Stagger new window from parent if we have one
        } else {
            x = 60;
            y = 60;
        }

        ourWindow = new ECExternalWindow();
        ourWindow.moveTo(x,y);
        ourWindow.setOwner(this);
        ourWindow.setTitle(title); // Title of window is the category name
        ourTitle = title;
        ourWindow.rootView().setBuffered(true);
    }

    /**

     * Perform command; responsibility from Target interface.  This
     * gets called when user clicks on one of our gathered-object
     * buttons.  The command is just the name of the object.

     */

    public void performCommand(String command, Object arg) {
        if (GatheredListViewToolBar.REFRESH_COMMAND.equals(command)) {
            displayAsButtons(gatheredObjects);
        } else {
            Object object = gatheredObjects.get(command);
            if (object != null) {
                myUI.inspect(object, command, ourWindow, null,
                              ourTitle, false); // Command is object's name.
            }
        }
    }

    /**

     * Responsibilities from WindowOwner

     */

    public void windowDidBecomeMain(netscape.application.Window window) {
    }

    public void windowDidHide(netscape.application.Window window) {
        myUI.removeGatheredListView(this);
    }

    public void windowDidResignMain(netscape.application.Window window) {
    }

    public void windowDidShow(netscape.application.Window window) {
    }

    public boolean windowWillHide(netscape.application.Window window) {
        return true;
    }

    public boolean windowWillShow(netscape.application.Window window) {
        return true;
    }

    public void windowWillSizeBy(netscape.application.Window window, Size size) {
    }
}


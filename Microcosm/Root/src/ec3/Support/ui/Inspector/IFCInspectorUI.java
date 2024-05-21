package ec.ui;

import ec.e.inspect.*;
import ec.ifc.app.*;
import java.util.*;
import netscape.application.*;
import ec.ifc.app.ECEvent;
import ec.e.run.OnceOnlyException;
import ec.e.run.RtRun;
import ec.e.run.RtQ;
import ec.e.run.RunQueueDebugger;
import ec.e.inspect.Runlet;
import ec.e.file.EStdio;

public class IFCInspectorUI extends Object implements Target, InspectorUI, EventProcessor {

    private static IFCInspectorUI myUI = null;
    private static InspectorRunner inspectorApp = null;

    private static final int SHOW_CONTROL_PANEL_WINDOW = -2;

    private int INITIAL_X = 40; // Initial control panel window position
    private int INITIAL_Y = 40;
    private int INITIAL_WIDTH = 174; // and size
    private int INITIAL_HEIGHT = 160;

    private Hashtable gatheredListViews;   // All currently open gathered object list views (categories)
    private Vector inspectorViews;   // All known InspectorViews, open or closed.
    private Application myApplication = null;
    private boolean panelInitialized = false;
    private ECExternalWindow myWindow = null; // The control panel window
    private int winContentHeight = 100;
    private int winContentWidth = 180;
    private CategoryListViewToolBar toolBar = null;
    private ScrollingButtonListView scrollingButtonListView = null;
    private QueueView queueView = null;
    private InspectorUIPreferences myPrefs = null;
    private Hashtable objectCategories = null;
    private int minHeight = 60;
    private int minWidth = 60;
    private EZListenerWindow ezListener = null;
    public static boolean useEZListener = false; // XXX remove later when EZListener works?

    /** 

     * Required initializer. You must call this method if you want to
     * use the IFC based UI for the Inspector under a running IFC. If
     * you don't have IFC running, call the IFCInspectorUI.start()
     * method instead.<p>

     * @param level suspect - This (String) argument determines the
     * initial configuration of the Inspector. If the level argument
     * is null, the inspector is initially disabled and won't even
     * gather objects.  <p>

     * If the level argument is "hidden" (not case-sensitive) then the
     * inspector will be gathering objects but will not initially
     * display its control panel window. You can enable full inspector
     * access later. <p>

     * If you give the level argument to "full", as in
     * ec.ui.IFCInspectorUI.initialize("full"); then the inspector
     * control panel will be available from the beginning and object
     * gathering starts early on in the startup sequence.

     */

    public static void initialize(String level) {
        ////EStdio.out().println("IFCInspector initialize called with level " + level); 
        if (level == null) {    // Not specifying inspector turns it off.
            ////EStdio.err().println("Inspector and gathering are turned off.");
            Inspector.enableGathering(false);
        } else {
            level = level.toLowerCase();

            // Specifying inspector = "off" also turns it off.

            if (level.equals("off")) {
                Inspector.enableGathering(false);
                ////EStdio.err().println("Inspector and gathering are explicitly turned off.");
                return;
            }
            
            ////EStdio.err().println("Inspector initial mode:" + level);
            Inspector.enableGathering(true);
            if (myUI == null) {
                try {
                    myUI = new IFCInspectorUI();
                     ec.e.inspect.Inspector.setUI((IFCInspectorUI)null, myUI);

                     // If we specify "runqueue", "runq", or "run", enable runqueue inspector.
                     // This is not the default, since it will slow us down.
                     // Specifying "stop" sets Hold state to hold all events from the beginning.

                    if (level.indexOf("stop") >= 0) {
                        ////EStdio.err().println("Run Queue Inspector init - Pausing run queue"); 
                        // User wants event processing to stop.
                        // Must be done after the setUI() call above.
                        ec.e.inspect.Inspector.setHoldUpdateUI(RunQueueInspector.HOLD_ALL);
                        ec.e.inspect.Inspector.setupRunQueueInspector();
                    } else if (level.indexOf("run") >= 0) {
                        ////EStdio.err().println("Run Queue Inspector init");
                        // User wants event analysis turned on early - enable low level RQI
                        // Must be done after the setUI() call above.
                        ec.e.inspect.Inspector.setupRunQueueInspector();
                    }

                    // unless we specify "hidden" or "hide", show the Inspector control panel.

                    if (level.indexOf("hid") < 0) myUI.showControlPanel();
                    if (level.indexOf("ez") >= 0) useEZListener = true;
                } catch (ec.e.run.OnceOnlyException oox) {
                    throw new Error("Setting inspector UI failed - " + oox);
                }
            }
        }
    }

    /**
     
     * Run the inspector in a thread of its own. Creates the thread
     * and starts IFC in that thread. Then calls initialize() with the
     * string argument given. You should not call this if you already
     * have IFC running, such as in Microcosm. This is intended to be
     * used if you are writing a standalone program and want to
     * inspect something there. You can put
     * ec.ui.IFCInspector.start("full") as one of the first statements
     * that get executed in your code and then call gather() or
     * inspect() as desired. <p>

     * Note that this static method makes it trivial to start the
     * inspector on the local machine. It allows you to inspect all
     * gathered objects, and requires no capabilitities to do so, at
     * the moment. XXX CCC This is therefore a major capability leak.

     */

    public static void start(String level) {
        try {
            _start(level);
        } catch (Throwable t) {
            System.out.println("*** Whoa! Throwable in IFCInspectorUI start");
            t.printStackTrace();
        }
    }
    
    static void _start (String level)  {
        if (inspectorApp == null) {
            inspectorApp = new InspectorRunner();
            initialize(level);
            inspectorApp.start();
        } else {
            if (myUI != null) {
                myUI.showControlPanel();
            }
        }
    }

    /**

     * Immediately inspect an object with a given name. This is done
     * by sending our IFCInspectorUI an IFC event with the object and
     * the name. This way inspection is done in the IFC thread,
     * regardless of which thread the inspect method was invoked from.

     * @param object nullOK untrusted - The object to inspect.
     * null objects are ignored *but* will (like any request to
     * inspect an object) cause the Inspector Control Panel window to
     * appear if it hasn't been visible before. The difference is that
     * a null object will not open an actual object inspection window,
     * only the control panel.

     * @param name nullOK untrusted - The name for the
     * object. Displayed in window's title bar. if given as null, then
     * the name "unnamed" will be used.

     */

    public void inspectObject(Object object, String name) {
        if (myApplication == null) {
            myApplication = Application.application();
            if (myApplication == null) {
                EStdio.err().println("Cannot inspect " + name + " - Not running under IFC");
                return;
            }
        }
        ECEvent event = new InspectorEvent(object,name);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
    }

    public void ensureQueueView() {
        if (queueView == null) {
            try {
                queueView = new QueueView(myWindow,707,310);
            } catch (OnceOnlyException ooe) {
                EStdio.err().println("Could not create a runqueue inspector window: " +
                                     ooe.getMessage());
                return;
            }
        }
    }

    /**

     * Handle the InspectorEvent from inspectObject. This gets done in
     * the IFC thread. The event contains an object reference and a
     * name for the object, which will be displayed in the Inspector's
     * object display window.
     * @param event untrusted - An IFC event or InspectorEvent containing an
     * object and a name.

     */

    public void processEvent(Event event) {
        if (! panelInitialized) { // We're not ready yet, must do initializations first
            initIFCInspectorUI(event); // Initialize now, in the IFC thread
            return;             // initIFCInspectorUI() will re-queue the event for us.
        }

        // If object and name are null, then this event just asks us
        // to redisplay the control panel window and refresh it.

        if (event instanceof InspectorEvent) {
            InspectorEvent iEvent = (InspectorEvent)event;
            switch (iEvent.type()) {

            case 0: return;

            case Inspector.INSPECT:
                if (iEvent.getObject() != null) {
                    inspect(iEvent.getObject(), iEvent.getName(),
                            (ECExternalWindow)null, (InspectorView)null, null, false);
                } 
                else if ((iEvent.getName() == null) && (iEvent.getObject() == null)) {
                    refreshCategoryButtons();
                    break;
                }
                break;
            case Inspector.REFRESH_RUNQUEUE:
                ensureQueueView();
                queueView.refreshQueueView(iEvent.getName(), iEvent.getObject());
                break;
            case Inspector.INVALIDATE_RUNLET:
                ensureQueueView();
                queueView.invalidateRunlet(iEvent.getName(), iEvent.getObject());
                break;
            case Inspector.REFRESH_HOLDSTATE:
                Object holdStateObject = iEvent.getObject();
                if (holdStateObject instanceof Integer) {
                    ensureQueueView();
                    queueView.setHoldState(((Integer)holdStateObject).intValue());
                }
                break;
            case Inspector.PROFILE_EXECUTION:
            case Inspector.PROFILE_RUNQUEUE:
                ensureQueueView();
                queueView.refreshQueueView(iEvent.getName(), iEvent.getTimeInQueue(), iEvent.getQueueLength());
                break;
            }
        }
    }

    /**

     * Send outselves an event to refresh the Runqueue display.  This
     * is called from the E Runqueue thread whenever runqueue
     * inspection is enabled.

     */

    public void refreshRunqueueDisplay(Object refreshObject) {
        if (myApplication == null) {
            myApplication = Application.application();
            if (myApplication == null) {
                EStdio.err().println("Cannot inspect run queue - not running under IFC");
                return;
            }
        }
        ECEvent event = new InspectorEvent(Inspector.REFRESH_RUNQUEUE, refreshObject, (String)null);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
    }

    /**

     * Send outselves an event to refresh one element in the Runqueue
     * display.  This is called from the E Runqueue thread whenever
     * runqueue inspection is enabled.

     */

    public void invalidateRunlet(Object refreshObject) {
        if (myApplication == null) {
            myApplication = Application.application();
            if (myApplication == null) {
                EStdio.err().println("Cannot inspect run queue - not running under IFC");
                return;
            }
        }
        ECEvent event = new InspectorEvent(Inspector.INVALIDATE_RUNLET, refreshObject, (String)null);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
    }

    /**

     */
    public void profileRunqueue(String objName, long timeInQueue, int queueLength) {
        ECEvent event = 
            new InspectorEvent(Inspector.PROFILE_RUNQUEUE, objName, timeInQueue, queueLength);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
    }

    /**

     */
    public void profileExecution(String objName, long timeToExecute) {
        ECEvent event = 
            new InspectorEvent(Inspector.PROFILE_EXECUTION, objName, timeToExecute);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
    }

    /**

     * Send outselves an event to refresh the hold state radios.  This
     * is called from the E Runqueue thread on startup and possibly
     * later.

     */

    public void refreshHoldState(int holdState) {
        if (myApplication == null) {
            myApplication = Application.application();
            if (myApplication == null) {
                EStdio.err().println("Cannot inspect run queue - not running under IFC");
                return;
            }
        }
        ECEvent event = new InspectorEvent(Inspector.REFRESH_HOLDSTATE,
                                           new Integer(holdState), (String)null);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
        // (new Error("Creating queueview")).printStackTrace();
    }

    /** 

     * Methods dealing with the Category Button List Window

     */

    /**

     * Generate or update the button list. If categories are added,
     * e.g. by user code calling gather() with new category names,
     * then we need to be able to refresh the button lisst to include
     * the new buttons. We get the data from the
     * (sub-presentation-level) Inspector.
     
     */

    private void refreshCategoryButtons() {
        objectCategories = Inspector.getObjectCategories();
        displayAsButtons(objectCategories);
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
            
                // Move the scrolling view to below the toolbar -
                // we know the toolbar height
                scrollingButtonListView.moveTo(0,toolBar.height());

                // Compute the height of everything in our view
                winContentHeight = scrollingButtonListView.height() + toolBar.height();

                // size the window appropriately since our height might have changed
                // (by adding or removing buttons).
                Size windowSize = myWindow.windowSizeForContentSize
                  (winContentWidth, winContentHeight);

                // This is an IFC or AWT bug: If you attempt to do
                // sizeTo() on the window using windowSize then the
                // window will jump to its initial position if it has
                // been moved in the meantime. Doing this the hard way
                // as below works - the window stays put when you
                // click the Refresh button.

                Rect bounds =  myWindow.bounds();
                bounds.width = windowSize.width;
                bounds.height =  windowSize.height;
                myWindow.setBounds(bounds);
        
                // Build view hierarchy now that everything is correctly
                // placed and sized.
                myWindow.addSubview(scrollingButtonListView);
       // ec.e.inspect.Inspector.gather(scrollingButtonListView,"scrollingButtonListView");
            }
        }
    }

    /** 

     * private initializer.

     */

    private void initIFCInspectorUI(Event event) {
        myPrefs = new InspectorUIPreferences();
        gatheredListViews = new Hashtable(40); // Currently open gathered object button lists
        inspectorViews = new Vector(40); // Currently open inspectorviews of inspected objects

        // This call cannot be made outside of IFC thread
        myWindow = new ECExternalWindow();
        myWindow.moveTo(INITIAL_X,INITIAL_Y);
        myWindow.sizeTo(INITIAL_WIDTH,INITIAL_HEIGHT);
        myWindow.setTitle("Inspector");
        myWindow.rootView().setBuffered(true);

        // Create a tool bar

        toolBar = new CategoryListViewToolBar(this,useEZListener);
        myWindow.addSubview(toolBar);

        refreshCategoryButtons(); // Create buttons. Can be called again if desired
        myWindow.show();

        panelInitialized = true;
        myApplication.eventLoop().addEvent(event); // Queue up event so we can act on it.
    }

    /**
      
     * Keep track of which objects are already displayed in various
     * inspector windows so that we can avoid opening duplicate
     * windows to the same object. Don't add it if it's there already.
     * @param inspectorView trusted - The view we have just opened.

     */

    private void addInspectorView(InspectorView inspectorView) {
        inspectorViews.addElement(inspectorView);
        inspectorView.updateVisibleButtons();
    }

    /**

     * Remove inspectorviews from the vector of open inspectorviews
     * when we close a window. 
     * @param inspectorView trusted - The view we have just closed.

     */

    void removeInspectorView(InspectorView iv) {
        if (! inspectorViews.removeElement(iv))
            System.out.println("Could not find object corresponding to the window that was closed");
        else iv.updateVisibleButtons();
    }

    /**

     * Look for an InspectorView in the vector of existing
     * InspectorViews (open windows) when given an object.
     * @param object nullOK untrusted - The object we are looking for.

     */

    public InspectorView findInspectorView(Object object) {
        Enumeration e = inspectorViews.elements();
        InspectorView iv;
        InspectorView result = null;
        
        while (e.hasMoreElements()) {
            iv = (InspectorView)e.nextElement();
            if (iv.isInspecting(object)) {
                if (result != null)
                    System.out.println("Duplicate inspectorview found for " + iv);
                result = iv;
            }
        }
        return result;
    }

    public Enumeration inspectorViewsEnumeration() {
        return inspectorViews.elements();
    }

    void removeGatheredListView(GatheredListView closingView) {
        if (gatheredListViews != null) {
            Enumeration lve = gatheredListViews.keys();

            while (lve.hasMoreElements()) {
                Object key = lve.nextElement();
                if (gatheredListViews.get(key) == closingView) {
                    gatheredListViews.remove(key); // Remove this view
                }
            } // We do them all, for good measure.
        }
    }

    /**
      
     * Inspect an object that is displayed as a button in an existing
     * IFC-UI Inspector window.  We don't go the long (more proper)
     * route through the non-UI Inspector methods since these don't
     * support the parent window and parent name arguments. These
     * allow us to implement features like chained names of windows
     * and windows that position themselves near their parent windows.

     * @param object nullOK untrusted - The object to inspect. null objects are ignored.
     * @param name nullOK untrusted - The name for the
     * object. Displayed in window's title bar.
     * @param parentWindow nullOK trusted - parent window. used only for positioning new window.

     * @param parentname nullOK untrusted - Name of parent window -
     * used when creating new cascaded window name (by prepending <name> + " in ")
     
     */

    void inspect(Object object, String name, ECExternalWindow parentWindow,
                 InspectorView parentView, String parentName, boolean reUseWindow) {
        if (object == null) return;
        if (name == null) name = "Unnamed";
        String windowName = name;
        if (parentName != null) windowName = name + " in " + parentName;

        // Is object already inspected in some inspectorview?

        InspectorView inspectorView = findInspectorView(object);
        if (inspectorView != null) {
            if (inspectorView.hasWindow()) {
                inspectorView.moveToFront();
                return;             // All done.
            }
        }

        if (inspectorView == null) { // Need to create an InspectorView

            // Attempt to create an inspector for the object first in
            // case that fails since that problem is cheaper to recover from.

            Inspector inspector = Inspector.createInspectorForObject(object,name);
            if (inspector == null) return; // Can't inspect.

            inspectorView = new InspectorView(myUI,
                                              inspector,
                                              windowName,
                                              parentView);

            // Add this view to our own list of views since it's new.

            addInspectorView(inspectorView);
        }

        // We now have a good InspectorView. Tell it to show itself in
        // a window. Figure out if we are re-using an existing window.

        ECExternalWindow win = null;
        if (reUseWindow) win = parentWindow;

        // If win is null, which is allowed, then a new window is
        // created but parentWindow's coordinates are needed to
        // compute staggering offset from parent window. Therefore we
        // pass in parentWindow no matter what.

        inspectorView.showSelfInWindow(win,parentWindow);
    }

    void inspectAtAddressCommand(String addressAsString) {
        if (addressAsString == null) return;
        int address = Inspector.parseAddress(addressAsString);
        try {
            Object object = Inspector.objectify(address);
            if (object == null) {
                System.out.println("Objectify returned null");
                return;
            }
            String name = Inspector.objectName(object);
            inspectObject(object,name);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void memoryDump() {
        Inspector.memoryDump();
    }

    /**

     * Perform command; responsibility from Target interface.  Handles
     * commands that come from the toolbar (like the Refresh button)
     * and commands from all buttons in the category list
     * window. These buttons just send the string which is their name
     * as a command, and the corresponding entry in the category table
     * is brought to fron (if it is already displayed in a window) or
     * opened as a new window (if it isn't).

     * @param command nullOK trusted - The command to perform.
     * @param arg nullOK untrusted - Ignored.

     */

    public void performCommand(String command, Object arg) {
        if (! panelInitialized) return; // Ignore requests until we have our windows set up right
        if (CategoryListViewToolBar.REFRESH_COMMAND.equals(command)) {
            refreshCategoryButtons();
        } else if (CategoryListViewToolBar.OPEN_EZ_LISTENER.equals(command)) {
            if (ezListener == null) {
                ezListener = new EZListenerWindow(this);
                ezListener.show();
                ezListener.println("Lithp ith lithtening");
            }
            //      else ezListener.moveToFront();
        } else if (CategoryListViewToolBar.INSPECT_AT_ADDRESS.equals(command)) {
            inspectAtAddressCommand(((TextField)arg).stringValue());
        } else if (CategoryListViewToolBar.DUMP_MEMORY_COMMAND.equals(command)) {
            memoryDump();
        } else if (CategoryListViewToolBar.INSPECT_ERQ_COMMAND.equals(command)) {
            synchronized(myWindow) {
                try {
                    if (queueView == null) ensureQueueView();
                    else queueView.moveToFront();
                } catch (OnceOnlyException ooe) {
                    EStdio.err().println("Could not create a runqueue inspector window: " + ooe);
                    return;
                }
            }
        } else {
            Hashtable categories = Inspector.getObjectCategories();
            if (categories != null) {
                Hashtable catObjects = (Hashtable)categories.get(command);

                // XXX Check whether the hashtable is already displayed in some window
                // and if so, just pop it to the top.

                if (catObjects != null) {
                    GatheredListView gatheredListView = (GatheredListView)gatheredListViews.get(command);
                    if (gatheredListView == null) {
                        gatheredListView = new GatheredListView(command,catObjects,myPrefs,
                                                                myWindow, myUI);
                        gatheredListViews.put(command,gatheredListView);
                    } else {
                        gatheredListView.moveToFront();
                    }                    
                }
            }
        }
    }

    /**

     * Show our control panel. This must be done by sending ourselves
     * an event. We send a plain ECevent (not an InspectorEvent) since
     * we don't want to inspect anything, just show the window.

     */

    public void showControlPanel() {
        inspectObject(null,null);
    }

 }

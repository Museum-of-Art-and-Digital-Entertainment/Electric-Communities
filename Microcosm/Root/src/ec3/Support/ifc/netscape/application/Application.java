// Application.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.lang.Thread;
import java.net.URL;
import java.applet.AppletContext;

/** Object subclass that represents the overall IFC-based Java
  * application. An Application instance maintains application-wide state and
  * manages access to resources or objects that provide resources
  * or services, such as RootViews.
  * @note 1.0 New methods for Keyboard UI support
  * @note 1.0 New methods for Menu support
  * @note 1.0 Added internal methods so users can properly override will/did
  *           process event.
  * @note 1.0 Added support for JDK 1.1 Clipboard
  * @note 1.0 Focus model changed because of Keyboard UI
  * @note 1.0 added applet() method
  * @note 1.0 added setClipboardText(), clipboardText() methods
  */

public class Application implements Runnable, EventProcessor {
    static Hashtable    groupToApplication = new Hashtable();
    static final String _releaseName = "IFC 1.1";
    static Clipboard    clipboard;
    static Object       clipboardLock = new Object();

    java.applet.Applet    applet;
    AppletResources     _appResources;
    EventLoop           eventLoop = new EventLoop();
    Vector              _languageVector;
    Vector              rootViews = new Vector();
    RootView            mainRootView;
    boolean             didCreateApplet;
    Vector              _modalVector = new Vector();
    Vector              observers = new Vector();
    Vector              activeMenuViews = new Vector();

    // This is application wide state kept on behalf of Bitmap, Sound, and
    // Font. Because we keep a hashtable of images, they will never go away
    // for the lifetime of an app. This code would get much nicer if each
    // application got its own static variables and would get better still if
    // the language had weak references.

    java.awt.MediaTracker tracker = null;
    int                   bitmapCount = 0;
    Hashtable             bitmapByName = new Hashtable();
    Hashtable             soundByName = new Hashtable();
    Hashtable             fontByName = new Hashtable();

    // This is information kept for graphics debugging
    DebugGraphicsInfo   debugGraphicsInfo;

    // This is the shared TimerQueue for the Application.
    TimerQueue timerQueue;

    // This lock is used to make the awt thread waiting for
    // the application cleanup
    Object cleanupLock;

    boolean isPaused;
    boolean _kbdUIEnabled = true;

    Window  currentDocumentWindow; /* Contains the current document window internal or external */

    KeyboardArrow keyboardArrow;  /* Contains the internal window used to display the arrow */


    /** Arrow position for the keyboard UI arrow */

    /** Arrow pointing to the selected view's top left corner
      *
      */
    public static final int TOP_LEFT_POSITION = 0;

    /** Arrow pointing to the selected view's bottom left corner
      *
      */
    public static final int BOTTOM_LEFT_POSITION  = 1;

    /** Arrow pointing to the selected view's top right corner
      *
      */
    public static final int TOP_RIGHT_POSITION = 2;

    /** Arrow pointing to the selected view's bottom right corner
      *
      */
    public static final int BOTTOM_RIGHT_POSITION = 3;

    static final int FIRST_POSITION    = TOP_LEFT_POSITION;
    static final int LAST_POSITION     = BOTTOM_RIGHT_POSITION;

    static final int arrowXOffset = 0;
    static final int arrowYOffset = 0;

    /** Constructs an Application. The Application will not begin processing
      * Events until it receives a <b>run()</b> message.  You will almost never
      * create an Application instance - the IFC machinery will create
      * one for you.
      * @see #run
      */
    public Application() {
        super();
        FoundationApplet ifcApplet;

        groupToApplication.put(Thread.currentThread().getThreadGroup(), this);
        ifcApplet = FoundationApplet.applet();
        if (ifcApplet == null) {
            ifcApplet = createApplet();
            ifcApplet.setApplication(this);
            didCreateApplet = true;
        } else {
            isPaused = true;
            ifcApplet.setupCanvas(this);
        }
        applet = ifcApplet;
        _appResources = new AppletResources(this, codeBase());
        timerQueue = new TimerQueue();
    }

    public static String releaseName()  {
        return _releaseName;
    }

    /** Constructs an Application for an existing Applet.  You should only
      * use this constructor when including IFC Views within an AWT-based
      * component hierarchy.  You must call <b>run()</b> on the returned
      * Application for your Views to draw and receive events.
      * @see FoundationPanel
      * @see #run
      */
    public Application(java.applet.Applet applet) {
        groupToApplication.put(Thread.currentThread().getThreadGroup(), this);
        this.applet = applet;
        _appResources = new AppletResources(this, codeBase());
        timerQueue = new TimerQueue();
        appletStarted();
    }

    /** Returns the application instance. */
    public static Application application() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Application app = (Application)groupToApplication.get(group);

        if(app == null) {   /// ddk - ADDED FOR JAVASCRIPT SUPPORT
            app = FoundationApplet.currentApplication();
        }

        /*
        if (app == null) {
            System.err.println("Not in Application thread group!");
            System.err.println("Thread: " + Thread.currentThread());
            Thread.currentThread().getThreadGroup().list();
            Thread.currentThread().getThreadGroup().stop();
            Thread.dumpStack();
        }
        */
        return app;
    }

    /** Initializes the Application.  Override to perform any initialization.
      */
    public void init() {
    }

    /** Called when the run loop has exited.
     */
    public void cleanup() {
        Enumeration groups = groupToApplication.keys();
        int iView;

        applicationDidStop();

        /* This will not destroy the applet. This will remove the application
         * and thread groups
         */
        if (applet instanceof FoundationApplet) {
            ((FoundationApplet)applet).cleanup();
        }

        if (didCreateApplet) {
            ((FoundationApplet)applet).destroyFromIFC();
        }

        while (groups.hasMoreElements()) {
            ThreadGroup group = (ThreadGroup)groups.nextElement();
            Application app = (Application)groupToApplication.get(group);

            if (app == this) {
                groupToApplication.remove(group);
                break;
            }
        }

    }

    /** Destroys the Application, including all ExternalWindows.
      * Stops the EventLoop and causes <b>run()</b> to return.
      */
    public void stopRunning() {
        eventLoop.stopRunning();
    }

    /** This method is called in the AWT thread. It will
     *  stop the event loop and wait until run is done
     */
    void stopRunningForAWT() {
        synchronized( cleanupLock ) {
            eventLoop.stopRunning();
            while( true ) {
                try {
                    cleanupLock.wait();
                    return;
                } catch ( java.lang.InterruptedException e ) {
                    /** ALERT what should we do? Can this really happen? */
                }
            }
        }
    }

    /** Starts the Application's EventLoop.
      */
    public void run() {
        // Having init() called here is kind of strange.  ALERT!
        cleanupLock = new Object();
        applicationDidStart();
        init();
        eventLoop.run();
        cleanup();
        /* Unblock the awt thread if necessary */
        synchronized( cleanupLock ) {
            cleanupLock.notify();
        }
    }

/* attributes */

    /** Returns the Application's EventLoop.
      * @see EventLoop
      */
    public EventLoop eventLoop() {
        return eventLoop;
    }

    /**
     * Returns a Vector containing the user's language preferences,
     * specified by Strings, in order of preference.  For example, for a user
     * preferring French over English, the Vector would contain the
     * Strings "French" and "English," in that order.<br><br>
     * <i><b>Note:</b> This method will return an empty Vector until such time
     * as Applets can save user preferences to their local machine.  If you
     * maintain user preferences at your site, you can request this
     * Vector and fill it with the appropriate strings.</I>
     */
    Vector languagePreferences() {
        if (_languageVector == null) {
            _languageVector = new Vector();
        }

        return _languageVector;
    }

/* actions */

    /*
     * Convenience method for forcing all pending drawing requests to appear
     * onscreen immediately.  Equivalent to
     * <pre>
     *     getToolkit().sync();
     * </pre>
     * <I><b>Note:</b> Due to bugs in the getToolkit().sync() native
     * implementation, this method currently does nothing.</i>
     */
    void syncGraphics() {
  /* awt */
// hangs the system sometimes with "unexpected asynch reply" messages
//      getToolkit().sync();
    }



/* interfaces */

    /*
     * Returns a java.io.InputStream containing the Velocity interface file
     * named
     * <b>interfaceName</b>.  Uses the Application's AppletResources
     * instance to
     * retrieve the correct localized interface file based on the user's
     * language preferences.
     * @see AppletResources
     */
    java.io.InputStream streamForInterface(String interfaceName) {
        return _appResources.streamForInterface(interfaceName);
    }

/* other resources */

    /** Returns a java.io.InputStream containing data for the file named
      * <b>resourceName</b> of type <b>type</b> (i.e. resides within a
      * directory named "<b>type</b>" at the same directory level as the
      * application's index.html file).
      */
    java.io.InputStream streamForResourceOfType(String resourceName,
                                                       String type) {
        return _appResources.streamForResourceOfType(resourceName, type);
    }

    /** Returns <b>true</b> if the Application was started as an Applet. */
    public boolean isApplet() {
        return !didCreateApplet;
    }

    java.io.InputStream streamForRelativePath(String relativePath) {
  /* awt */
        URL                     documentURL;
        java.io.InputStream     inputStream;

        try {
            documentURL = new URL(codeBase(), relativePath);
        } catch (Exception e) {
            System.err.println("Application.streamForRelativePath() - " + e);
            documentURL = null;
        }

        if (documentURL == null) {
            return null;
        }

        try {
            inputStream = documentURL.openStream();
        } catch (Exception e) {
            System.err.println(
                "Application.streamForURL() - Trouble retrieving URL " +
                documentURL + " : " + e);
            inputStream = null;
        }

        return inputStream;
    }

    AppletContext getAppletContext() {
        return applet == null ? null : applet.getAppletContext();
    }

    /** Returns the Application's codebase, the URL where the Applet's class
      * file originated.
      */
    public URL codeBase() {
        return applet.getCodeBase();
    }

    /** Returns the values for the parameter <b>name</b>.
      */
    public String parameterNamed(String name) {
        return applet == null ? null : applet.getParameter(name);
    }

    /** Returns the Application's "main" RootView.  In the case of an Applet,
      * this method returns the RootView associated with the Applet.  If
      * not an Applet and never set by the Application, this method returns
      * <b>null</b>.
      * @see #setMainRootView
      */
    public RootView mainRootView() {
        return mainRootView;
    }

    /** Sets the main RootView.
      * @see #mainRootView
      */
    public void setMainRootView(RootView view) {
        addRootView(view);
        mainRootView = view;
    }

    /** Returns the Applet's RootView, or the RootView of the ExternalWindow
      * that was most recently made the top-most window.
      */
    RootView firstRootView() {
        return (RootView)rootViews.lastElement();
    }

    /** Returns a Vector containing all the RootViews being displayed
      * by the Application.  This vector is for <i>reading only</i> - do not
      * modify.
      */
    public Vector rootViews() {
        return rootViews;
    }

    /** Returns a Vector containing all the ExternalWindows being displayed
      * by the Application.  This Vector is for <i>reading only</i> - do not
      * modify.
      */
    public Vector externalWindows() {
        int i, count = rootViews.count();
        Vector windows = new Vector();

        for (i = 0; i < count; i++) {
            RootView rootView = (RootView)rootViews.elementAt(i);
            ExternalWindow window = rootView.externalWindow();

            if (window != null) {
                windows.addElement(window);
            }
        }
        return windows;
    }

    /* We are playing this add/remove game to make sure that
     * we don't add the same rootview twice and that the
     * firstRootView is not changed while adding a new rootview.
     */
    void addRootView(RootView view) {
        if (!rootViews.contains(view)) {
            rootViews.insertElementAt(view, 0);
            view.setApplication(this);
        }
    }

    void removeRootView(RootView view) {
        rootViews.removeElement(view);
        view.setApplication(null);
        if(rootViews.count() > 0)
            ((RootView)rootViews.lastElement()).didBecomeFirstRootView();
    }

    void makeFirstRootView(RootView view) {
        RootView wasFirstRootView;

        if(rootViews.indexOf(view) == -1)
            return;

        if(rootViews.lastElement() != view) {
            wasFirstRootView = (RootView)rootViews.lastElement();
            rootViews.removeElement(view);
            rootViews.addElement(view);
            if(wasFirstRootView != null)
                wasFirstRootView.didResignFirstRootView();
            view.didBecomeFirstRootView();
        }
    }

    java.awt.Frame frame() {
        java.awt.Component comp;
        for (comp = applet;
             comp != null && !(comp instanceof java.awt.Frame);
             comp = comp.getParent());
        if (comp != null)
            return (java.awt.Frame) comp;
        else
            return null;
    }

    synchronized java.awt.MediaTracker mediaTracker() {
        if (tracker == null) {
            tracker = new java.awt.MediaTracker(applet);
        }

        return tracker;
    }

    synchronized int nextBitmapNumber() {
        return bitmapCount++;
    }

    synchronized TimerQueue timerQueue() {
        return timerQueue;
    }

    /** Registers <b>menuView</b> for notification to receive
      * <b>mouseWillDown</b> messages.  The registered MenuView must be
      * top-level (i.e. it has a null owner).
      */
    void addActiveMenuView(MenuView menuView) {
        activeMenuViews.addElementIfAbsent(menuView);
    }

    /** Unregisters <b>menuView</b> for <b>mouseWillDown</b> notification.
      * @see #addActiveMenuView
      */
    void removeActiveMenuView(MenuView menuView) {
        activeMenuViews.removeElement(menuView);
    }

    /** Called before the EventLoop processes <b>anEvent</b>.
      * The default implementation does nothing.
      */
    public void willProcessEvent(Event anEvent) {
    }

    /** Called before the EventLoop processes <b>anEvent</b>, but before
      * <b>willProcessEvent</b> is called.
      */
    void willProcessInternalEvent(Event anEvent) {
        MenuView   menuView;
        int        i;

        if (activeMenuViews.count() == 0 ||
                anEvent.type() != MouseEvent.MOUSE_DOWN) {
            return;
        }

        menuView = (MenuView)activeMenuViews.lastElement();
        ((MenuView)menuView).mouseWillDown((MouseEvent)anEvent);
    }

    /** Called after the EventLoop has processed <b>anEvent</b>.
      * The default implementation does nothing.
      */
    public void didProcessEvent(Event anEvent) {
    }

    /** Called after the EventLoop has processed <b>anEvent</b>, and after
      * <b>didProcessEvent()</b> is called.
      */
    void didProcessInternalEvent(Event anEvent) {
        drawAllDirtyViews();
    }

    /** This method is called by InternalWindow and ExternalWindow
     *  in showModally before waiting for an event.
     */
    void drawAllDirtyViews() {
        int i, count;
        RootView rView;

        count = rootViews.count();
        for (i = 0; i < count; i++) {
            rView = (RootView)rootViews.elementAt(i);
            rView.drawDirtyViews();
            rView._updateCursorAndMoveView();
        }
    }

    boolean isMac() {
        String          osName;

        osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Mac")) {
            return true;
        }
        return false;
    }

    /** Subclassers can override to catch key down Events when there's no
      * focused view. The default implementation handle keyboard UI.
      * always call super if your application subclass is not processing
      * the event.
      */
    public void keyDown(KeyEvent event) {
        boolean processed = false;
        RootView frv = firstRootView();
        if(frv != null)
            processed = frv.processKeyboardEvent(event,false);

        if(!processed) {
            // Beep?
        }
    }

    /** Subclassers can override to catch key up Events when there's no
      * focused view.The default implementation handle keyboard UI.
      * always call super if your application subclass is not processing
      * the event.
      */
    public void keyUp(KeyEvent event) {
    }


    /** Modal views */
    void beginModalSessionForView(View aView) {
        RootView        rootView;

        if (aView == null)
            throw new InconsistencyException(
                            "beginModalSessionForView called with null view");

        _modalVector.addElement(aView);

        rootView = aView.rootView();
        if (rootView != null) {
            rootView.updateCursor();
        }
    }

    void endModalSessionForView(View aView) {
        RootView        rootView;

        if( aView != _modalVector.lastElement()) {
            throw new InconsistencyException("endModalSessionForView called for"+
                                    " a view that is not the last modal view");
        }

        _modalVector.removeLastElement();

        rootView = aView.rootView();
        if (rootView != null) {
            rootView.updateCursor();
            rootView.validateSelectedView();
        }
    }

    /** Returns the top level view that has started a modal session.
      */
    public View modalView() {
        if( _modalVector.count() > 0)
            return (View) _modalVector.lastElement();
        else
            return null;
    }

    boolean isModalViewShowing() {
        if (_modalVector.count() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** Causes <b>target</b> to receive a <b>performCommand()</b> message
      * with <b>command</b> and <b>object</b>, from the Application's
      * main thread using the Application's EventLoop.  This method will not
      * return until the command has been performed. It can only be called
      * from threads <i>other than</i> the main thread.
      * @see #eventLoop
      * @see EventLoop#mainThread
      */
    public void performCommandAndWait(Target target,
                                      String command,
                                      Object data) {
        CommandEvent commandEvent = new CommandEvent(target, command, data);

        eventLoop.addEventAndWait(commandEvent);
    }

    /** Causes <b>target</b> to receive a <b>performCommand()</b> message
      * with <b>command</b> and <b>object</b>, after the current Event and
      * others in the Application's EventLoop have been processed. If
      * <b>ignorePrevious</b> is <b>true</b>, this method disposes of all
      * pending requests with the same <b>target</b> and <b>command</b>.
      * @see #eventLoop
      */
    public void performCommandLater(Target target,
                                    String command,
                                    Object data, boolean ignorePrevious) {
        CommandEvent commandEvent = new CommandEvent(target, command, data);

        if (ignorePrevious) {
            eventLoop.filterEvents(
                new CommandFilter(commandEvent.target, commandEvent.command,
                                  data));
        }
        eventLoop.addEvent(commandEvent);
    }

    /** Causes <b>target</b> to receive a <b>performCommand()</b> message
      * with <b>command</b> and <b>object</b>, after the current Event and
      * others in the Application's EventLoop have been processed.
      * Equivalent to the code:
      * <pre>
      *     performCommandLater(target, command, data, false)
      * </pre>
      * @see #performCommandLater(Target, String, Object, boolean)
      */
    public void performCommandLater(Target target, String command,
                                    Object data) {
        performCommandLater(target, command, data, false);
    }

    /** Creates and returns the Application's Applet.
      * This method will only be called in a stand-alone application.
      * Application subclasses can override this method to provide a
      * custom subclass of FoundationApplet. This Applet must have been
      * added to a java.awt.Frame and have an AppletStub.
      * @see FoundationApplet
      * @see java.awt.Frame
      * @see java.applet.AppletStub
      */
    protected FoundationApplet createApplet() {
        java.awt.Frame awtFrame = new java.awt.Frame();
        FoundationApplet applet = new FoundationApplet();

        awtFrame.add(applet);
        awtFrame.addNotify();
        applet.addNotify();
        applet.setStub(new FoundationAppletStub());
        return applet;
    }

    String exceptionHeader() {
        return "Uncaught exception.  IFC release: " + _releaseName;
    }

    /** Adds <b>observer</b> as an object that will receive notifications
      * when the Application's running state changes. An observer
      * might be interested in learning when the HTML page containing the
      * Application is no longer visible, for example.  See the
      * ApplicationObserver interface for more information.
      * @see ApplicationObserver
      * @see #removeObserver
      */
    public void addObserver(ApplicationObserver observer) {
        observers.addElementIfAbsent(observer);
    }

    /** Removes <b>observer</b> from the group of objects interested in
      * notifications of changes in the Application's running state.
      * @see #addObserver
      */
    public void removeObserver(ApplicationObserver observer) {
        observers.removeElement(observer);
    }

    /** Called from an Applet's <b>start()</b> method.  Notifies all
      * ApplicationObservers that the Application has resumed.
      * Normally called from FoundationApplet.
      */
    public void appletStarted() {
        int i = observers.count();

        isPaused = false;
        while (i-- > 0) {
            ApplicationObserver observer;

            observer = (ApplicationObserver)observers.elementAt(i);
            observer.applicationDidResume(this);
        }
        i = rootViews.count();
        while (i-- > 0) {
            RootView rootView = (RootView)rootViews.elementAt(i);

            if (rootView.externalWindow() == null) {
                rootView.setVisible(true);
            }
        }
    }

    /** Called from an Applet's <b>stop()</b> method.  Notifies all
      * ApplicationObservers that the Application has paused.
      * Normally called from FoundationApplet.
      */
    public void appletStopped() {
        int i = observers.count();

        isPaused = true;
        while (i-- > 0) {
            ApplicationObserver observer;

            observer = (ApplicationObserver)observers.elementAt(i);
            observer.applicationDidPause(this);
        }
        i = rootViews.count();
        while (i-- > 0) {
            RootView rootView = (RootView)rootViews.elementAt(i);

            if (rootView.externalWindow() == null) {
                rootView.setVisible(false);
            }
        }
    }

    void applicationDidStart() {
        int i = observers.count();

        while (i-- > 0) {
            ApplicationObserver observer;

            observer = (ApplicationObserver)observers.elementAt(i);
            observer.applicationDidStart(this);
        }
    }

    void applicationDidStop() {
        int i = observers.count();

        while (i-- > 0) {
            ApplicationObserver observer;

            observer = (ApplicationObserver)observers.elementAt(i);
            observer.applicationDidStop(this);
        }
    }

    /** Processes Application-specific Events. */
    public void processEvent(Event event) {
        if (event instanceof ApplicationEvent) {
            if (event.type == ApplicationEvent.APPLET_STOPPED) {
                appletStopped();
            } else if (event.type == ApplicationEvent.APPLET_STARTED) {
                appletStarted();
            }
        }
    }

    /** Returns <b>true</b> if the Application's EventLoop is
      * currently running.
      */
    public boolean isRunning() {
        return eventLoop.isRunning();
    }

    /** Returns <b>true</b> if the Application is currently paused.
      * Applications pause when their Applet's HTML page becomes hidden.
      */
    public boolean isPaused() {
        return isPaused();
    }

    static Clipboard clipboard() {
        synchronized (clipboardLock) {
            if (clipboard == null) {
                clipboard = JDK11AirLock.clipboard();
                if (clipboard == null) {
                    clipboard = new TextBag();
                }
            }
            return clipboard;
        }
    }

    /** Makes <b>aWindow</b> the current document for the application.
      * <b>aWindow</b> should contain a document: isDocument should
      * return true. If <b>aWindow</b> is null, the application will
      * not have a current document.
      *
      */
    public void makeCurrentDocumentWindow(Window aWindow) {
        int i;
        ApplicationObserver observer;

        if(aWindow != null && !(aWindow.containsDocument())) {
            throw new InconsistencyException("makeCurrentDocumentWindow: window is not a document");
        }

        if( currentDocumentWindow != null ) {
            currentDocumentWindow.didResignCurrentDocument();
            currentDocumentWindow = null;
        }

        if(aWindow != null) {
            currentDocumentWindow = aWindow;
            currentDocumentWindow.didBecomeCurrentDocument();
        }

        i = observers.count();
        while (i-- > 0) {
            observer = (ApplicationObserver)observers.elementAt(i);
            try {
                /* New API for 1.1 */
                observer.currentDocumentDidChange(this,
                             currentDocumentWindow);
            } catch (IncompatibleClassChangeError e) {
            }
        }
    }

    /** Return the current document window. Return null if there is no
     *  current document window
     */
    public Window currentDocumentWindow() {
        return currentDocumentWindow;
    }

    /** Find and make a new window the current document.
      * The new window should be different than <b>aWindow</b>
      * If <b>aWindow</b> is an external window, try to find an external
      * window that contains a document. If not found, try an internal window
      * in the main root view that contains a document.
      * If <b>aWindow</b> is an internal window, try to find an internal
      * window in the same root view that contains a document.
      * If <b>aWindow</b> is null, try to find an external window that
      * contains a document and then an internal window in the main
      * root view that contains a document.
      * Override this method if your application needs a better strategy
      * to find a new document window. This method is called when a window
      * containing a document is closed.
      *
      */
    public void chooseNextCurrentDocumentWindow(Window aWindow) {
        Window nextWindow = null;
        if(aWindow == null || aWindow instanceof ExternalWindow) {
            nextWindow = _chooseNextExternalWindowWithDocument((ExternalWindow)aWindow);
            if( nextWindow == null &&  mainRootView() != null )
                nextWindow = _chooseNextInternalWindowWithDocument(mainRootView(),null);
        } else { // Internal Window
            if(((InternalWindow)aWindow).rootView() != null)
                nextWindow = _chooseNextInternalWindowWithDocument(
                                    ((InternalWindow)aWindow).rootView(),
                                    (InternalWindow)aWindow);
        }
        makeCurrentDocumentWindow(nextWindow);
    }

    /** Find the next internal window in aRootView that contains a document **/
    Window _chooseNextInternalWindowWithDocument(RootView aRootView,InternalWindow win) {
        Vector vector = aRootView.internalWindows();
        InternalWindow w;
        int i;
        for(i = vector.count()-1 ; i >= 0 ; i--){
            w = (InternalWindow) vector.elementAt(i);
            if(w == win)
                continue;
            else if(w.containsDocument())
                return w;
        }
        return null;
    }

    Window _chooseNextExternalWindowWithDocument(ExternalWindow win) {
        Vector vector = externalWindows();
        ExternalWindow w;
        int i;
        for(i = vector.count()-1 ; i >= 0 ; i--) {
            w = (ExternalWindow) vector.elementAt(i);
            if(w == win)
                continue;
            else if(w.containsDocument())
                return w;
        }
        return null;
    }

    void focusChanged(View newFocusedView) {
        int i;
        ApplicationObserver observer;

        i = observers.count();
        while (i-- > 0) {
            observer = (ApplicationObserver)observers.elementAt(i);
            try {
                /* New API for 1.1 */
                observer.focusDidChange(this, newFocusedView);
            } catch (IncompatibleClassChangeError e) {
            }
        }
    }


    /** Keyboard UI changes **/
    KeyboardArrow keyboardArrow() {
        if(keyboardArrow == null)
            keyboardArrow = new KeyboardArrow();
        return keyboardArrow;
    }

    /** Return the arrow position that should be used to show that <b>aView</b>
      * is the selected view. Override this method and return the direction you
      * need The default implementation will make sure that the arrow is
      * visible. Default implementation's return value can be:
      * TOP_LEFT_POSITION, TOP_RIGHT_POSITION, BOTTOM_LEFT_POSITION or
      * BOTTOM_RIGHT_POSITION
      *
      */
    public int keyboardArrowPosition(View aView) {
        RootView rv = aView.rootView();
        Rect rvBounds = rv.localBounds();
        Rect  vBounds;
        Image  image;
        Point  hotSpot;
        Point  extr = new Point();
        int i;

        vBounds = new Rect();
        aView.computeVisibleRect(vBounds);
        vBounds.intersectWith(aView.keyboardRect());
        aView.convertRectToView(rv,vBounds,vBounds);

        for(i=FIRST_POSITION ; i <= LAST_POSITION ; i++) {
            image = keyboardArrowImage(i);
            hotSpot = keyboardArrowHotSpot(i);
            extr    = keyboardArrowLocation(aView,i);
            extr.x -= hotSpot.x;
            extr.y -= hotSpot.y;
            if(rvBounds.contains(new Rect(extr.x,extr.y,image.width(),image.height())))
                return i;
        }

        // Edge case, no image fit. Return the first one
        return FIRST_POSITION;
    }

    /** Return the arrow image that should be used for the position
      * <b>position</b>
      *
      */
    public Image keyboardArrowImage(int position) {
        switch(position) {
        case TOP_LEFT_POSITION:
            return Bitmap.bitmapNamed("netscape/application/topLeftArrow.gif");
        case TOP_RIGHT_POSITION:
            return Bitmap.bitmapNamed("netscape/application/topRightArrow.gif");
        case BOTTOM_RIGHT_POSITION:
            return Bitmap.bitmapNamed("netscape/application/bottomRightArrow.gif");
        case BOTTOM_LEFT_POSITION:
            return Bitmap.bitmapNamed("netscape/application/bottomLeftArrow.gif");
        default:
            return null;
        }
    }

    /** Return the Point that should be used for the the keyboard arrow
      * image for the position <b>position</b>
      *
      */
    public Point keyboardArrowHotSpot(int position) {
        switch(position) {
        case TOP_LEFT_POSITION:
            return new Point(8,12);
        case TOP_RIGHT_POSITION:
            return new Point(0,12);
        case BOTTOM_RIGHT_POSITION:
            return new Point(0,0);
        case BOTTOM_LEFT_POSITION:
            return new Point(8,0);
        default:
            return null;
        }
    }

    /** Return the arrow location.The location is the point where
      * the arrow hot spot should be in <b>aView</b>'s root view
      * coordinate system.
      *
      */
    public Point keyboardArrowLocation(View aView,int position) {
        RootView rv = aView.rootView();
        Rect rvBounds = rv.localBounds();
        Rect b = aView.localBounds();

        b = new Rect();
        aView.computeVisibleRect(b);
        b.intersectWith(aView.keyboardRect());

        if(b.width == 0 || b.height == 0)
            return new Point(Integer.MAX_VALUE,Integer.MAX_VALUE);

        aView.convertRectToView(aView.rootView(),b,b);
        switch(position) {
        case TOP_LEFT_POSITION:
            return new Point(b.x + arrowXOffset,b.y + arrowYOffset);
        case TOP_RIGHT_POSITION:
            return new Point(b.x + b.width - arrowXOffset,
                             b.y + arrowYOffset);
        case BOTTOM_LEFT_POSITION:
            return new Point(b.x + arrowXOffset,
                             b.y + b.height - arrowYOffset);
        case BOTTOM_RIGHT_POSITION:
            return new Point(b.x + b.width - arrowXOffset,
                             b.y + b.height - arrowYOffset);
        default:
            throw new InconsistencyException("Unknown position " + position);
        }
    }

    /** Enable/Disable keyboard UI for this application. This method should
      * be called in the init method of Application. This flag is checked
      * once at startup, so changes to this switch will be ignored during the
      * lifetime of the application.
      *
      */
    public void setKeyboardUIEnabled(boolean aFlag) {
        _kbdUIEnabled=aFlag;
    }

    /** Return whether keyboard UI is enabled
      *
      */
    public boolean isKeyboardUIEnabled() {
        return _kbdUIEnabled;
    }

    /** @private */
    public java.applet.Applet applet() {
        return applet;
    }

    /** @private */
    public AppletResources appletResources() {
        return _appResources;
    }

    /** Returns the "current" text string.  This interacts with the
      * the native clipboard on platforms where it is available
      *
      */
    public static String clipboardText() {
        return clipboard().text();
    }

    /** Returns the "current" text string.  This interacts with the
      * the native clipboard on platforms where it is available
      *
      */
    public static void setClipboardText(String text) {
        clipboard().setText(text);
    }
}


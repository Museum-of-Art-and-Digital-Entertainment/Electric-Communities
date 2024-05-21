// ExternalWindow.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.IllegalMonitorStateException;

/** Object subclass providing a platform-dependent window containing IFC
  * components. Like InternalWindow, it implements the
  * Window interface. The following code demonstrates the normal sequence for
  * creating an ExternalWindow:
  * <pre>
  *     window = new ExternalWindow();
  *     windowSize = window.windowSizeForContentSize(contentWidth, contentHeight);
  *     window.sizeTo(windowSize.width, windowSize.height);
  *     window.moveTo(x, y);
  *     window.show();
  * </pre>
  * @note 1.0 application wide document window support added.
  * @note 1.0 changes for Menu support
  * @note 1.0 fixed problem with very fast hide/show patterns
  * @note 1.0 fixed problem with view being resized by showModally()
  * @note 1.0 center will now use the screen size
  * @note 1.0 changes for new Window interface and focus model
  * @note 1.0 added moveToFront/Back
  */
public class ExternalWindow implements Window, ApplicationObserver {
    java.awt.Window             awtWindow;
    private FoundationPanel     panel;
    private WindowOwner         owner;
    private int                 type;
    private Size                minimumSize;
    private String              title;
    private Rect                bounds;
    private boolean             resizable=true;
    private boolean             visible = false;
    private boolean             hideOnPause = true;
    private boolean             showOnResume = false;
    private boolean             containsDocument = false;
    private boolean             waitingForInvalidation = false;
    Menu                        menu;
    MenuView                    menuView;

    final static String         AWTWINDOW_KEY = "awtWindow",
                                PANEL_KEY = "panel",
                                OWNER_KEY = "owner",
                                TYPE_KEY = "type",
                                MINSIZE_KEY = "minimumSize",
                                MENU_KEY = "menu",
                                HIDEONPAUSE_KEY = "hideOnPause",
                                CONTAINS_DOCUMENT_KEY = "containsDocument";


    /** Constructs an ExternalWindow with a style of Window.TITLE_TYPE.
      */
    public ExternalWindow() {
        this(TITLE_TYPE);
    }

    private java.awt.Frame firstRootViewParentFrame() {
       Application app = Application.application();
       RootView firstRootView;

       if( app != null ) {
           firstRootView = app.firstRootView();
           if( firstRootView != null ) {
               FoundationPanel panel = firstRootView.panel();
               java.awt.Component parent;
               parent = panel.getParent();
               while(parent != null && !(parent instanceof java.awt.Frame))
                   parent = parent.getParent();
               if( parent != null ) {
                   return (java.awt.Frame) parent;
               }
           }
       }
       return appletParentFrame();
    }

    private java.awt.Frame appletParentFrame() {
        java.applet.Applet applet = AWTCompatibility.awtApplet();
        java.awt.Component parent;

        if( applet != null ) {
            parent = applet.getParent();
            while(parent != null && !(parent instanceof java.awt.Frame))
                parent = parent.getParent();
        } else
            return null;
        return (java.awt.Frame) parent;
    }

    private synchronized void validateAWTWindow(int type,boolean modal)  {
        if(waitingForInvalidation)
            waitingForInvalidation = false; /* Cancel any pending invalidation */
        if( awtWindow == null ) {
            Application app = Application.application();
            Insets insets;
            RootView rootView = panel.rootView();
            boolean rootViewAutoresize;

            if( modal ) {
                FoundationDialog dialog = createDialog();
                dialog.setExternalWindow( this );
                awtWindow = dialog;
            } else {
                if( type == TITLE_TYPE ) {
                    FoundationFrame netcodeFrame = createFrame();
                    netcodeFrame.setExternalWindow(this);
                    awtWindow = netcodeFrame;
                } else {
                    FoundationWindow netcodeWindow = createWindow();
                    netcodeWindow.setExternalWindow(this);
                    awtWindow = netcodeWindow;
                }
            }

            /** java.awt.window does not implement setResizable(boolean)
             *  this is why we are testing classes like this.
             */
            if( awtWindow instanceof java.awt.Dialog )
                ((java.awt.Dialog)awtWindow).setResizable(resizable);
            else if( awtWindow instanceof FoundationFrame)
                ((FoundationFrame)awtWindow).setResizable(resizable);

            awtWindow.addNotify();
            awtWindow.add(panel);
//            rootViewAutoresize = rootView.doesAutoResizeSubviews();
//            rootView.setAutoResizeSubviews(false);
            awtWindow.reshape( bounds.x, bounds.y, bounds.width, bounds.height);
            awtWindow.layout();
//            rootView.setAutoResizeSubviews( rootViewAutoresize);

            if (type == TITLE_TYPE) {
                if( awtWindow instanceof java.awt.Dialog )
                    ((java.awt.Dialog)awtWindow).setTitle(title);
                else
                    ((FoundationFrame)awtWindow).setTitle(title);
            }

            if (menu != null) {
                ((FoundationFrame)awtWindow).setMenuBar(menu.awtMenuBar());
            }
        }
    }

    // This method is only called by the WindowInvalidationAgent
    // to release the real AWT window objects in response to a hide() call.
    synchronized void invalidateAWTWindow() {
        if( waitingForInvalidation ) {
            _invalidateAWTWindow();
        }
        waitingForInvalidation = false;
    }

    /// This invalidate method does the real work of releasing the AWT resources.
    void _invalidateAWTWindow() {
        if( awtWindow != null ) {
            bounds = bounds(); /* Sync window location with the ivar */
            awtWindow.remove(panel);
            awtWindow.dispose();
            awtWindow = null;
        }
    }

    /** Constructs an ExternalWindow of type <B>windowType</B>.  Creates
      * the platform-dependent (native) window that will hold the
      * ExternalWindow's contents, as well as the window's RootView
      * and AWT Panel.  The ExternalWindow does <I>not</I> appear onscreen
      * until it receives a <b>show()</b> message.
      */
    public ExternalWindow(int windowType) {
        RootView rootView;
        Application app = Application.application();

        title = "";
        type = windowType;
        panel = createPanel();
        bounds = new Rect(0,0,0,0);
        setBounds(0, 0, 150, 150); /* Size should not be 0 for inset to work */
        Application.application().addObserver(this);
    }

    /** Sets the ExternalWindow's title (the string displayed in its title
      * bar).
      */
    public void setTitle(String aTitle) {

        if( aTitle == null )
            aTitle = "";

        title = aTitle;
        if( awtWindow != null && type == TITLE_TYPE) {
            if( awtWindow instanceof java.awt.Dialog )
                ((java.awt.Dialog)awtWindow).setTitle(title);
            else
                ((FoundationFrame)awtWindow).setTitle(title);
        }
    }

    /** Returns the ExternalWindow's title.
      * @see #setTitle
      */
    public String title() {
        return title;
    }

    /** Displays the ExternalWindow.
      * @see #hide
      */
    public void show() {
        validateAWTWindow( type, false );
        if (owner == null || owner.windowWillShow(this)) {
            awtWindow.show();
            panel.rootView.setVisible(true);
            visible = true;
            showOnResume = false;
            awtWindow.toFront();
            if (owner != null)
                owner.windowDidShow(this);
        }

    }

    /** Displays the ExternalWindow until dismissed by the user.  This method
      * will not return until the user closes the Window.
      */
    public void showModally() {
        Application application = Application.application();
        Event event;
        EventLoop eventLoop = Application.application().eventLoop();

        if (type == BLANK_TYPE)
            throw new InconsistencyException(
                                        "Cannot run blank windows modally");

        if (owner == null || owner.windowWillShow(this)) {
            validateAWTWindow(type, true );
            ModalDialogManager modalManager;

            modalManager = new ModalDialogManager((java.awt.Dialog)awtWindow);
            modalManager.show();
            showOnResume = false;
            panel.rootView.setVisible(true);
            visible = true;

            if (owner != null)
                owner.windowDidShow(this);

            application.beginModalSessionForView(this.rootView());
            application.drawAllDirtyViews();
            while (this.isVisible()) {
                event = eventLoop.getNextEvent();
                try {
                    eventLoop.processEvent(event);
                } catch (Exception e) {
                    System.err.println("Uncaught Exception.");
                    e.printStackTrace(System.err);
                    System.err.println("Restarting modal EventLoop.");
                }
            }
            application.endModalSessionForView(this.rootView());
        }
    }

    /** Hides the ExternalWindow.
      * @see #show
      */
    public void hide() {
        WindowInvalidationAgent agent;

        if (awtWindow == null) {
            return;
        }

        if (owner == null || owner.windowWillHide(this)) {
            if(containsDocument() && isCurrentDocument())
                Application.application().chooseNextCurrentDocumentWindow(this);
            awtWindow.hide();
            visible = false;
            panel.rootView.setVisible(false);
            showOnResume = false;
            if (owner != null)
                owner.windowDidHide(this);
            agent = new WindowInvalidationAgent(this);
            waitingForInvalidation = true;
            agent.run();
        }
    }

    /** Returns <b>true</b> if the ExternalWindow is currently visible
      * (is onscreen).
      */
    public boolean isVisible() {
        return visible;
    }

    /** Closes the ExternalWindow and destroys the native window.
      */
    public void dispose() {
        RootView rootView = rootView();
        Application app = rootView.application();

        if (containsDocument() && isCurrentDocument()) {
            app.chooseNextCurrentDocumentWindow(this);
        }

        visible = false;
        /** Should invalidate before removing the root view
          * invalidate might cause an awt layout and a post event
          */
        _invalidateAWTWindow();
        app.removeObserver(this);
        app.removeRootView(rootView);
        panel.rootView.setVisible(false);
        panel.rootView = null;
    }

    /** Sets the ExternalWindow's Menu. This is rendered as a native
      * menu that is created through the AWT Menu API.
      * @see Menu, #setMenuView
      *
      */
    public void setMenu(Menu aMenu) {
        java.awt.MenuBar menuBar;

        menu = aMenu;
        if (!menu.isTopLevel()) {
            throw new InconsistencyException("menu must be main menu");
        }

        menu.setApplication(rootView().application());

        menuBar = menu.awtMenuBar();
        if (awtWindow != null) {
            ((FoundationFrame) awtWindow).setMenuBar(menuBar);
        }
    }

    /** Returns the ExternalWindow's Menu.
      * @see #setMenu
      */
    public Menu menu() {
        return menu;
    }

    /** Sets the MenuView that will appear along the top edge of the Window.
      * This will be an IFC View-based Menu.
      * @see MenuView, #setMenu
      *
      */
    public void setMenuView(MenuView aMenuView) {
        int x, y, width, height;

        if (aMenuView != null && aMenuView == menuView) {
            return;
        }

        if (menuView != null) {
            menuView.removeFromSuperview();
        }
        menuView = aMenuView;

        x = rootView().bounds.x;
        y = rootView().bounds.y;
        width = rootView().bounds.width;
        height = menuView.height();
        menuView.setBounds(x, y, width, height);

        addSubview(menuView);
    }

    /** Returns the MenuView that appears along the top edge of the Window.
      * @see #setMenuView
      */
    public MenuView menuView() {
        return menuView;
    }

    /** Returns the RootView that occupies the ExternalWindow.
      */
    public RootView rootView() {
        return panel.rootView;
    }

    /** Returns the Application to which the ExternalWindow belongs.
      */
    Application application() {
        return Application.application();
    }

    /** Sets the ExternalWindow's owner, the object interested in learning
      * about special events such as the user closing the ExternalWindow.
      */
    public void setOwner(WindowOwner wOwner) {
        owner = wOwner;
    }

    /** Returns the ExternalWindow's owner.
      * @see #setOwner
      */
    public WindowOwner owner() {
        return owner;
    }

    void didBecomeMain() {
        if (owner != null)
            owner.windowDidBecomeMain(this);
        if(containsDocument())
            Application.application().makeCurrentDocumentWindow(this);
    }

    void didResignMain() {
        if (owner != null)
            owner.windowDidResignMain(this);
    }

    /** Returns the Size defining the ExternalWindow's content area. Use this
      * Size to properly position and size any View that you plan to add to the
      * ExternalWindow.
      */
    public Size contentSize() {
        RootView rootView = rootView();

        if (rootView == null) {
            return null;
        }

        return new Size(rootView.bounds.width, rootView.bounds.height);
    }

    /** Adds <b>aView</b> to the ExternalWindow.
      */
    public void addSubview(View aView) {
        RootView rootView = rootView();
        if (rootView != null)
            rootView.addSubview(aView);
    }

    /** Sets the Window's bounds to the rectangle (<b>x</b>, <b>y</b>,
      * <b>width</b>, <b>height</b>).  This is the primitive method for
      * resizing or moving.  All the other related methods ultimately call
      * this one.
      */
    public void setBounds(int x, int y, int width, int height) {
        boolean sizeChanged = false;
        Size    deltaSize;
        java.awt.Rectangle wBounds;

        if (owner != null &&
            (bounds.width != width || bounds.height != height)) {
            deltaSize = new Size(width - bounds.width, height - bounds.height);
            owner.windowWillSizeBy(this, deltaSize);
            width = bounds.width + deltaSize.width;
            height = bounds.height + deltaSize.height;
        }

        if (bounds.x != x || bounds.y != y || bounds.width != width ||
            bounds.height != height) {
            if( bounds.width != width || bounds.height != height )
                sizeChanged = true;
            bounds.setBounds(x, y, width, height);
            if (awtWindow != null) {
                awtWindow.reshape(x, y, width, height);
                wBounds = awtWindow.bounds();
                bounds.setBounds(wBounds.x, wBounds.y, wBounds.width,
                                 wBounds.height);
                awtWindow.layout();

            }
        }

        if( sizeChanged && awtWindow == null ) {
            validateAWTWindow(type,false);
            _invalidateAWTWindow();
        }
    }

    /** This method is called by RootView when the it is processing
     *  ResizeEvent to allow the window to update its bounds and to
     *  call windowWillSizeBy
     */
    void validateBounds() {
        if( awtWindow != null ) {
            Rect newBounds;
            java.awt.Point location = awtWindow.location();
            java.awt.Dimension size = awtWindow.size();
            Size deltaSize;

            newBounds = new Rect(location.x, location.y, size.width,
                                 size.height);
            if(!newBounds.equals(bounds)) {
                deltaSize = new Size(newBounds.width - bounds.width,
                                     newBounds.height - bounds.height);
                if( owner != null )
                    owner.windowWillSizeBy(this,deltaSize);
                bounds.setBounds(newBounds.x,newBounds.y,
                                 newBounds.width,newBounds.height);
            }
        }
    }

    /** Sets the ExternalWindow's bounds to <b>newBounds</b>.
      */
    public void setBounds(Rect newBounds) {
        setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
    }

    /** Sets the ExternalWindow's size to (<b>width</b>, <b>height</b>).
      */
    public void sizeTo(int width, int height) {
        setBounds(bounds.x, bounds.y, width, height);
    }

    /** Changes the ExternalWindow's size by <b>deltaWidth</b> and
      * <b>deltaHeight</b>.
      */
    public void sizeBy(int deltaWidth, int deltaHeight) {
        setBounds(bounds.x, bounds.y, bounds.width + deltaWidth,
                  bounds.height + deltaHeight);
    }

    /** Changes the ExternalWindow's location by <b>deltaX</b> and
      * <b>deltaY</b>.
      */
    public void moveBy(int deltaX, int deltaY) {
        setBounds(bounds.x + deltaX, bounds.y + deltaY, bounds.width,
                  bounds.height);
    }

    /** Centers the ExternalWindow (as well as possible for a native window).
      */
    public void center() {
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rect b = new Rect(this.bounds());

        b.x = (int)Math.floor((double)(screenSize.width - b.width) / 2.0);
        b.y = (int)Math.floor((double)(screenSize.height - b.height) / 2.0);

        this.setBounds(b);
    }

    /** Sets the ExternalWindow's origin to (<b>x</b>, <b>y</b>). */
    public void moveTo(int x, int y) {
        setBounds(x, y, bounds.width, bounds.height);
    }

    /** Returns the size the ExternalWindow must be to support a content
      * size of (<B>width</B>, <B>height</B>).
      */
    public Size windowSizeForContentSize(int width, int height) {
        Insets insets;
        boolean hasAWTWindow = (awtWindow != null);

        if(!hasAWTWindow)
            validateAWTWindow(type,false);
        insets = awtWindow.insets();

        if(!hasAWTWindow)
            _invalidateAWTWindow();

        return new Size(width + insets.left + insets.right,
                        height + insets.top + insets.bottom);
    }

    /** Returns the View containing the point (<B>x</B>, <B>y</B>).
      */
    public View viewForMouse(int x, int y) {
        return rootView().viewForMouse(x, y);
    }

    /** Sets a minimum size for the ExternalWindow.<p>
      * <i><b>Note:</b> The AWT does not appear to support this feature.</i>
      */
    public void setMinSize(int width, int height) {
        minimumSize = new Size(width, height);
    }

    /** Returns the ExternalWindow's minimum size, if set. Otherwise,
      * returns <b>null</b>.
      */
    public Size minSize() {
        return minimumSize;
    }

   /** Returns a newly-allocated copy of the ExternalWindow's bounding
     * rectangle, which defines the ExternalWindow's size and position.
     */
    public Rect bounds() {
        if( awtWindow != null ) {
            java.awt.Point location = awtWindow.location();
            java.awt.Dimension size = awtWindow.size();

            return new Rect(location.x, location.y, size.width, size.height);
        } else
            return new Rect(bounds);
    }

    /** Sets whether the ExternalWindow can be resized by the user.
      * Throws an error if called when the ExternalWindow is visible.
      */
    public void setResizable(boolean flag) {
        resizable = flag;
        if( awtWindow != null ) {
            throw new InconsistencyException(
                    "Cannot call setResizable on a visible external window");
        }
    }

    /** Returns <b>true</b> if the user can resize the ExternalWindow.
      * @see #setResizable
      */
    public boolean isResizable() {
        return resizable;
    }

    /** Returns the FoundationPanel the ExternalWindow uses to display its
      * RootView.
      */
    public FoundationPanel panel() {
        return panel;
    }

    /** Sets whether the window contains a document. Windows containing document
      * are treated in a different manner by the target chain.
      *
      */
    public void setContainsDocument(boolean containsDocument) {
        this.containsDocument = containsDocument;
        if(containsDocument == false && Application.application().currentDocumentWindow() == this)
            Application.application().chooseNextCurrentDocumentWindow(this);
        else if(containsDocument == true && Application.application().firstRootView() == rootView())
            Application.application().makeCurrentDocumentWindow(this);
    }

    /** Return whether the window contains a document.
      *
      */
    public boolean containsDocument() {
        return containsDocument;
    }

    /** If the window contains a document, this method is called
      * when the window just became the current document.
      *
      */
    public void didBecomeCurrentDocument() {
    }

    /** If the window contains a document, this method is called
      * when the window is no longer the current document.
      *
      */
    public void didResignCurrentDocument() {
    }

    /** Return whether this window is the current document
      *
      */
    public boolean isCurrentDocument() {
        if(Application.application().currentDocumentWindow() == this)
            return true;
        else
            return false;
    }

    /* ALERT!
    ** Describes the ExternalWindow class' information.
      * @see Codable#describeClassInfo
      *
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.ExternalWindow", 1);
        info.addField(AWTWINDOW_KEY, OBJECT_TYPE);
        info.addField(PANEL_KEY, OBJECT_TYPE);
        info.addField(OWNER_KEY, OBJECT_TYPE);

        info.addField(TYPE_KEY, INT_TYPE);

        info.addField(MINSIZE_KEY, OBJECT_TYPE);
        info.addField(MENU_KEY, OBJECT_TYPE);
        info.addField(HIDEONPAUSE_KEY, BOOLEAN_TYPE);
        info.addField(CONTAINS_DOCUMENT_KEY,BOOLEAN_TYPE);
    }

    ** Encodes the ExternalWindow instance.
      * @see Codable#encode
      *
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(AWTWINDOW_KEY, awtWindow);
        encoder.encodeObject(PANEL_KEY, panel);
        encoder.encodeObject(OWNER_KEY, owner);

        encoder.encodeInt(TYPE_KEY, type);

        encoder.encodeObject(MINSIZE_KEY, minimumSize);
        encoder.encodeObject(MENU_KEY, menu);
        encoder.encodeBoolean(HIDEONPAUSE_KEY, hideOnPause);
        encoder.encodeBoolean(CONTAINS_DOCUMENT_KEY, containsDocument);
    }

    ** Decodes the ExternalWindow instance.
      * @see Codable#decode
      *
    public void decode(Decoder decoder) throws CodingException {
        awtWindow = (java.awt.Window)decoder.decodeObject(AWTWINDOW_KEY);
        panel = (FoundationPanel)decoder.decodeObject(PANEL_KEY);
        owner = (WindowOwner)decoder.decodeObject(OWNER_KEY);

        type = (int)decoder.decodeInt(TYPE_KEY);

        minimumSize = (Size)decoder.decodeObject(MINSIZE_KEY);
        menu = (Menu)decoder.decodeObject(MENU_KEY);
        hideOnPause = decoder.decodeBoolean(HIDEONPAUSE_KEY);
    }

    ** Finishes the ExternalWindow decoding.
      * @see Codable#finishDecoding
      *
    public void finishDecoding() throws CodingException {
        Application.application().addObserver(this);
    }
    */

    /** Creates and returns the ExternalWindow's FoundationDialog.
      * This method will be called if the ExternalWindow has a title bar
      * and is being displayed modally.
      * ExternalWindow subclasses can override this method to provide a
      * custom FoundationDialog subclass.
      * @see FoundationFrame
      */
    protected FoundationDialog createDialog() {
        return new FoundationDialog(firstRootViewParentFrame(), true);
    }

    /** Creates and returns the ExternalWindow's FoundationFrame.
      * This method will be called if the ExternalWindow has a title bar.
      * ExternalWindow subclasses can override this method to provide a
      * custom FoundationFrame subclass.
      * @see FoundationFrame
      */
    protected FoundationFrame createFrame() {
        return new FoundationFrame();
    }

    /** Creates and returns the ExternalWindow's AWT Window.
      * This method will be called if the ExternalWindow has no title bar.
      * ExternalWindow subclasses can override this method to provide a
      * custom FoundationWindow subclass.
      * @see FoundationWindow
      * @private
      */
    protected FoundationWindow createWindow() {
        return new FoundationWindow(appletParentFrame());
    }

    /** Creates and returns the ExternalWindow's FoundationPanel.
      * ExternalWindow subclasses can override this method to provide a
      * custom FoundationPanel subclass.
      * @see FoundationPanel
      */
    protected FoundationPanel createPanel() {
        return new FoundationPanel();
    }

    /** ApplicationObserver method. */
    public void applicationDidStart(Application application) {
    }

    /** ApplicationObserver method. */
    public void applicationDidStop(Application application) {
        dispose();
    }

    /** ApplicationObserver method.
      *
      */
    public void focusDidChange(Application application, View focusedView) {
    }

    /** ApplicationObserver method.
      *
      */
    public void currentDocumentDidChange(Application application,
                                         Window document) {
    }

    /** ApplicationObserver method.  If <b>hidesWhenPaused()</b> is <b>true</b>
      * and the ExternalWindow is visible, hides the ExternalWindow.
      * @see #setHidesWhenPaused
      * @see #applicationDidResume
      */
    public void applicationDidPause(Application application) {
        if (hideOnPause && visible) {
            hide();
            showOnResume = true;
        }
    }

    /** ApplicationObserver method.  If <b>hidesWhenPaused()</b> is <b>true</b>
      * and the ExternalWindow was visible when the Application paused,
      * makes the ExternalWindow visible.
      * @see #setHidesWhenPaused
      * @see #applicationDidPause
      */
    public void applicationDidResume(Application application) {
        if (showOnResume) {
            show();
        }
    }

    /** Sets whether the ExternalWindow hides when the Application pauses.
      * If <b>flag</b> is <b>true</b>, the ExternalWindow's
      * <b>applicationDidPause()</b> method hides the Window if the Window
      * is visible.  The <b>applicationDidResume()</b> brings the Window
      * back onscreen.
      * @see #applicationDidPause
      * @see #applicationDidResume
      */
    public void setHidesWhenPaused(boolean flag) {
        hideOnPause = flag;
    }

    /** Returns <b>true</b> if the ExternalWindow hides when the Application
      * pauses.
      * @see #setHidesWhenPaused
      */
    public boolean hidesWhenPaused() {
        return hideOnPause;
    }

    /** Implements the ExternalWindow's commands:
      * <ul>
      * <li>SHOW - calls the ExternalWindow's <b>show()</b> method, causing
      * the ExternalWindow to appear onscreen.
      * <li>HIDE - calls the ExternalWindow's <b>hide()</b> method,
      * removing the ExternalWindow from the screen.
      * </ul>
      * @see #show
      * @see #hide
      */
    public void performCommand(String command, Object data) {
        if (SHOW.equals(command)) {
            show();
        } else if (HIDE.equals(command)) {
            hide();
        } else {
            throw new NoSuchMethodError("unknown command: " + command);
        }
    }

    /** Move the window to the front.
      *
      */

    public void moveToFront() {
        if(!isVisible())
            return;
        else {
            awtWindow.toFront();
        }
    }

    /** Move the window to the back.
      *
      */
    public void moveToBack() {
        if(!isVisible())
            return;
        else {
            awtWindow.toBack();
        }

    }
}


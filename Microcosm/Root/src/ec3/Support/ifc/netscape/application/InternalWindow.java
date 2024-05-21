// InternalWindow.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

// ALERT! - remove old component methods

/** View subclass implementing "window-like" behavior
  * found in traditional windowing systems.  An InternalWindow can have a
  * title bar (displaying a text string) by which a user can drag the
  * InternalWindow around the screen; a close button that the user can click
  * to make a InternalWindow invisible (remove it from the View hierarchy); and
  * a resize bar, allowing a user to resize the InternalWindow and its
  * contents.<p>
  * An InternalWindow has a "contentView," which is the ancestor of all
  * Views added to InternalWindow programmatically.  Calling InternalWindow's
  * <B>addSubvew()</B> adds the View to the InternalWindow's contentView
  * rather than directly to the InternalWindow.<p>
  * You make an InternalWindow visible (add it to the View hierarchy) by
  * calling the <B>show()</B> method, and remove it by calling
  * <B>hide()</B>.  Each RootView can contain zero or more
  * InternalWindows, but only one visible InternalWindow can be the
  * Application's "main InternalWindow."  The main InternalWindow displays its
  * title bar differently than all other InternalWindows, and represents the
  * InternalWindow in which the user is currently working.  The IFC passes
  * key events to the focused View in the current main InternalWindow.
  * InternalWindows containing tool palettes or other sets
  * of controls might never become the main Window because they
  * do not need to receive key events.  Sending the message
  * <B>setCanBecomeMain(false)</B> will prevent this. <p>
  * In general, InternalWindows overlap each other.  To ensure that certain
  * InternalWindows never obscure certain others, you can assign
  * InternalWindows to "layers."  For example, if you want a tool palette to
  * never be obscured by a document InternalWindow, you can set the tool
  * palette InternalWindow's layer to <B>PALETTE_LAYER</B>, a layer higher than
  * the default InternalWindow layer. Other InternalWindows in the tool
  * palette's layer can obscure each other, but they can never be obscured by
  * InternalWindows in lower layers.  The InternalWindow class defines
  * several layers; if needed, you can define your own, but the
  * predefined layers should be enough.<p>
  * InternalWindow are rectangular regions.  If you need to use a View with
  * InternalWindow-like properties (clipping, primarily) but also have it
  * appear non-rectangular, you can make the InternalWindow transparent.  A
  * transparent InternalWindow allocates an offscreen buffer to perform its
  * drawing.  Unless
  * the InternalWindow's contentView contains Views that draw, the
  * InternalWindow remains completely transparent or invisible (except for the
  * InternalWindow's Border, if set).  Clicking and
  * dragging anywhere within the InternalWindow's bounds
  * moves the InternalWindow. You can regulate this movement by overriding the
  * InternalWindow's <b>isPointInBorder()</b> method.<p>
  * Objects interested in events such as the InternalWindow closing,
  * resizing, and so on, can implement the WindowOwner
  * interfaces and set themselves as an InternalWindow's owner.<p>
  * @note 1.0 internal window document removed
  * @note 1.0 application wide document window support added.
  * @note 1.0 removed InternalWindowOwner
  * @note 1.0 focusing a view inside the window now makes the internal
  *           window the main window
  * @note 1.0 focusing a view in a window that cannot become main does
  *           not change the focused view in the rootview anymore
  * @note 1.0 remove Component support
  * @note 1.0 added support for Menus
  * @note 1.0 added ignoreWindowClipView layer
  * @note 1.0 rewrote drawing of Windows with layers and transparency
  * @note 1.0 fixed bug with resize windows on incorrect rootView
  * @note 1.0 fixes for coordinate view transforms
  * @note 1.0 archiving changes for focus model and menu
  * @note 1.0 added protected method willMoveTo() to intercept movements
  */


public class InternalWindow extends View implements Window {
    // Even though this is set, the window may not be visible.
    RootView            rootView;
    WindowOwner          _owner;
    WindowContentView    _contentView;
    View                 _focusedView;/* Warning always call focusedView() */
    View                 _defaultSelectedView;
    Button               _closeButton;
    MenuView             menuView;

    Font                 _titleFont;
    String               _title = "";
    Border               _border;
    int                  _layer, _type = TITLE_TYPE, _lastX, _lastY,
                         _resizePart = NO_PART;
    boolean              _closeable, _resizable, _canBecomeMain = true,
                         _containsDocument = false, _drewOnLastDrag,
                         _drawToBackingStore, _dragStart, _onscreen = true,
                         _createdDrawingBuffer, transparent = false,
                         scrollToVisible;

    static Vector        _resizeWindowVector = new Vector();

    final static int            ABOVE = 0, BEHIND = 1;
    final static int            NO_PART = 0, LEFT_PART = 1, MIDDLE_PART = 2,
                                RIGHT_PART = 3;

    final static String         ownerKey = "owner",
                                contentViewKey = "contentView",
                                focusedViewKey = "focusedView",
                                closeButtonKey = "closeButton",
                                titleFontKey = "titleFont",
                                titleKey = "title", borderKey = "border",
                                layerKey = "layer", typeKey = "type",
                                closeableKey = "closeable",
                                resizableKey = "resizeable",
                                canBecomeMainKey = "canBecomeMain",
                                containsDocumentKey = "canBecomeDocument",
                                onscreenKey = "onscreen",
                                transparentKey = "transparent",
                                scrollToVisibleKey = "scrollToVisible",
                                defaultSelectedViewKey = "defaultSelectedView",
                                menuViewKey = "menuView";


    /** Default window layer. */
    public final static int     DEFAULT_LAYER = 0;
    /** "Palette" window layer. */
    public final static int     PALETTE_LAYER = 100;
    /** Modal InternalWindow layer. */
    public final static int     MODAL_LAYER   = 200;
    /** Popup window layer. */
    public final static int     POPUP_LAYER = 300;
    /** Drag layer. */
    public final static int     DRAG_LAYER = 400;

    /** Windows on this Layer will NOT be clipped to the WindowClipView
      * @private
      */
    public final static int     IGNORE_WINDOW_CLIPVIEW_LAYER = 500;


    /* constructors */

    /** Constructs an InternalWindow with origin (0, 0), zero width and
      * height, and no title.
      */
    public InternalWindow() {
        this(0, 0, 0, 0);
    }

    /** Constructs an InternalWindow with bounds <B>rect</B>.
      */
    public InternalWindow(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs an InternalWindow with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public InternalWindow(int x, int y, int width, int height) {
        this(TITLE_TYPE, x, y, width, height);
    }

    /** Constructs an InternalWindow of type
      * <b>type</b> and bounds (<B>x</B>, <B>y</B>, <B>width</B>,
      * <B>height</B>).
      */
    public InternalWindow(int type, int x, int y, int width, int height) {
        super(x, y, width, height);

        rootView = application().mainRootView();

        _titleFont = Font.fontNamed("Helvetica", Font.BOLD, 12);

        _contentView = new WindowContentView(0, 0, 1, 1);
        _contentView.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
        _contentView.setVertResizeInstruction(HEIGHT_CAN_CHANGE);
        addSubviewToWindow(_contentView);

        _layer = DEFAULT_LAYER;

        _border = new InternalWindowBorder(this);

        layoutParts();
        setType(type);
        _defaultSelectedView = this;
    }

    /** Returns the InternalWindow's MenuView height.
      */
    int menuViewHeight() {
        if (menuView == null) {
            return 0;
        } else {
            return menuView.height();
        }
    }

    /** Returns the InternalWindow's title bar height, by asking its
      * InternalWindowBorder. <b>BLANK_TYPE</b> InternalWindows have
      * zero-height title bars.
      */
    int titleBarMargin() {
        if (_type == BLANK_TYPE) {
            return 0;
        } else {
            return _border.topMargin();
        }
    }

    /** Returns the InternalWindow's left border width, by asking its
      * InternalWindowBorder. <b>BLANK_TYPE</b> InternalWindows have a
      * zero-width left border.
      */
    int leftBorderMargin() {
        if (_type == BLANK_TYPE) {
            return 0;
        } else {
            return _border.leftMargin();
        }
    }

    /** Returns the InternalWindow's right border width, by asking its
      * InternalWindowBorder. <b>BLANK_TYPE</b> InternalWindows have a
      * zero-width right border.
      */
    int rightBorderMargin() {
        if (_type == BLANK_TYPE) {
            return 0;
        } else {
            return _border.rightMargin();
        }
    }

    /** Returns the InternalWindow's bottom border height, by asking its
      * InternalWindowBorder. <b>BLANK_TYPE</b> InternalWindows have a
      * zero-height bottom border.
      */
    int bottomBorderMargin() {
        if (_type == BLANK_TYPE) {
            return 0;
        } else {
            return _border.bottomMargin();
        }
    }

    /** Returns the InternalWindow's contentView, the View in which all
      * programmatically-added Views live.
      */
    public WindowContentView contentView() {
        return _contentView;
    }

    /** Returns the Size defining the InternalWindow's content area. Use this
      * Size to properly position and size any View that you plan to add to the
      * InternalWindow.
      */
    public Size contentSize() {
        if (_contentView == null) {
            return null;
        }

        return new Size(_contentView.bounds.width, _contentView.bounds.height);
    }

    /** Resizes and repositions the InternalWindow's contentView to its
      * correct size and location within the InternalWindow based on the
      * InternalWindow's Border.  Positions the close button, if present.
      */
    public void layoutParts() {
        if (_contentView != null) {
            _contentView.setAutoResizeSubviews(false);
            _contentView.setBounds(leftBorderMargin(),
                                   titleBarMargin() + menuViewHeight(),
                                   bounds.width -
                                   (leftBorderMargin() + rightBorderMargin()),
                                   bounds.height -
                                   (titleBarMargin() + menuViewHeight() +
                                      bottomBorderMargin()));
            _contentView.setAutoResizeSubviews(true);
        }

        if (_closeButton != null) {
            _closeButton.removeFromSuperview();

            _closeButton.moveTo(0, 2 + (titleBarMargin() - 4 -
                                        _closeButton.bounds.height) / 2);
            if (_closeable) {
                addSubview(_closeButton);
            }
        }
    }

    /** Adds <B>aView</B> to the InternalWindow's contentView.  To add a View
      * directly to the InternalWindow, call <b>addSubviewToWindow()</b>.
      * @see #addSubviewToWindow
      */
    public void addSubview(View aView) {
        if (aView == _contentView || aView == _closeButton) {
            addSubviewToWindow(aView);
        } else if (_contentView != null) {
            _contentView.addSubview(aView);
        }
    }

    /** Adds <B>aView</B> directly to the InternalWindow.  Unless you're adding
      * special Views or controls to the InternalWindow's border, you should
      * call <b>addSubview()</b> instead of this method to add the View to the
      * InternalWindow's contentView.
      * @see #addSubview
      */
    public void addSubviewToWindow(View aView) {
        super.addSubview(aView);
    }

    /** Sets the InternalWindow's RootView, the RootView on which it should
      * appear.  This method does <i>not</i> place the InternalWindow in the
      * View hierarchy, or make it visible in any way.  You
      * need to call this method only if you intend for the InternalWindow to
      * appear on any RootView other than the Application's
      * main RootView.
      */
    public void setRootView(RootView rView) {
        if (rootView != rView) {
            if ((rootView != null) && (_superview != null))
                rootView.removeWindow(this);
        }
        rootView = rView;
    }

    /** Makes the InternalWindow visible (adds it to its RootView's
      * View hierarchy).  Call this method instead of adding the
      * InternalWindow as a subview of the RootView.
     */
    public void show() {
        if (rootView == null) {
            throw new InconsistencyException(
                                    "Can't show Window.  No RootView");
        }
        if (_owner == null || _owner.windowWillShow(this)) {
            if (_superview == null) {
                rootView.addWindowRelativeTo(this, ABOVE, null);
            } else {
                rootView.makeWindowVisible(this, ABOVE, null);
            }
            if (_owner != null)
                _owner.windowDidShow(this);
        }
    }

    /** Makes the InternalWindow visible. This method does not return
      * until the user closes the InternalWindow. While the InternalWindow is
      * visible, Views that are not subviews of the InternalWindow will not
      * receive Events.  If the InternalWindow's layer is < MODAL_LAYER, sets
      * it to MODAL_LAYER for the modal session.
      */
    public void showModally() {
        View focusedView;
        Application application = Application.application();
        Event event;
        EventLoop eventLoop = application.eventLoop();
        int previousLayer = layer();

        if( previousLayer < MODAL_LAYER ) {
            setLayer( MODAL_LAYER );
        }

        this.show();
        rootView()._setMainWindow(this);
        application.beginModalSessionForView(this);
        application.drawAllDirtyViews();
        while(this.isVisible()) {
            event = eventLoop.getNextEvent();
            try {
                eventLoop.processEvent(event);
            } catch (Exception e) {
                System.err.println("Uncaught Exception.");
                e.printStackTrace(System.err);
                System.err.println("Restarting modal EventLoop.");
            }
        }
        application.endModalSessionForView(this);

        if( previousLayer != layer() )
            setLayer(previousLayer);
    }

    /** Makes the InternalWindow visible, positioned in front of
      * <B>aWindow</B>.
      * @see #show
      */
    public void showInFrontOf(InternalWindow aWindow) {
        if (aWindow.rootView != rootView) {
            setRootView(aWindow.rootView);
            rootView.addWindowRelativeTo(this, ABOVE, aWindow);
        } else if (_superview == null) {
            rootView.addWindowRelativeTo(this, ABOVE, aWindow);
        } else {
            rootView.makeWindowVisible(this, ABOVE, aWindow);
        }
    }

    /** Makes the InternalWindow visible, positioned behind <B>aWindow</B>.
      * @see #show
      */
    public void showBehind(InternalWindow aWindow) {
        if (aWindow.rootView != rootView) {
            setRootView(aWindow.rootView);
            rootView.addWindowRelativeTo(this, BEHIND, aWindow);
        } else if (_superview == null) {
            rootView.addWindowRelativeTo(this, BEHIND, aWindow);
        }
    }

    /** Move the window to the front of all other windows with the same layer
      *
      */
    public void moveToFront() {
        InternalWindow   topWindow;

        if(!isVisible()) {
            return;
        }

        topWindow = rootView.frontWindowWithLayer(layer());
        if(topWindow != null && topWindow != this) {
            rootView.makeWindowVisible(this,ABOVE,topWindow);
        }
    }

    /** Move the window behind all other windows with the same layer.
      *
      */
    public void moveToBack() {
        InternalWindow   bottomWindow;

        if(!isVisible())
            return;

        bottomWindow = rootView.backWindowWithLayer(layer());
        if(bottomWindow != null && bottomWindow != this) {
            rootView.makeWindowVisible(this,BEHIND,bottomWindow);
        }
    }

    /** Hides the InternalWindow (removes it from its RootView's View
      * hierarchy).
      * Call this method instead of <B>removeFromSuperview()</b>.
      */
    public void hide() {
        if(isVisible()) {
            if (_owner == null || _owner.windowWillHide(this)) {
                RootView rView;

                if(containsDocument() && isCurrentDocument()) {
                    Application.application().chooseNextCurrentDocumentWindow(this);
                }
                rView = rootView();
                if (rView != null) {
                    rView.removeWindow(this);
                }
                if (_owner != null)
                    _owner.windowDidHide(this);
            }
        }
    }

    /** Tells the IFC that this InternalWindow can become the
      * main InternalWindow.
      */
    public void setCanBecomeMain(boolean flag) {
        _canBecomeMain = flag;
        if (isMain() && !_canBecomeMain && rootView != null) {
            rootView._setMainWindow(null);
        }
    }

    /** Returns <B>true</B> if the InternalWindow can become the main
      * InternalWindow.
      */
    public boolean canBecomeMain() {
        return _canBecomeMain;
    }

    /** Returns <B>true</B> if the InternalWindow is visible.
      */
    public boolean isVisible() {
        return (_superview != null);
    }

    /** Sets whether the InternalWindow is visible when unarchived.
      * @private
      */
    public void setOnscreenAtStartup(boolean flag) {
        _onscreen = flag;
    }

    /** Returns <b>true</b> if the InternalWindow is visible when unarchived.
      * @private
      */
    public boolean onscreenAtStartup() {
        return _onscreen;
    }

    /** Returns <B>true</B> if the InternalWindow is the Application's main
      * InternalWindow.
      */
    public boolean isMain() {
        return ((rootView != null) && rootView.mainWindow() == this);
    }

    /** Called by the InternalWindow to create its close button.
      * Override this method to return your own special close button.
      */
    protected Button createCloseButton() {
        Button    button;

        button = new Button(0, 0, 1, 1);
        button.setImage(Bitmap.bitmapNamed(
                               "netscape/application/CloseButton.gif"));
        button.setAltImage(Bitmap.bitmapNamed(
                               "netscape/application/CloseButtonActive.gif"));
        //          button.setBezeled(false);
        button.setTransparent(true);
        button.sizeToMinSize();
        button.setHorizResizeInstruction(RIGHT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(BOTTOM_MARGIN_CAN_CHANGE);
        button.moveTo(0, 2 + (titleBarMargin() - 4 -
                              button.bounds.height) / 2);
        button.setTarget(this);
        button.setCommand(HIDE);
        button.removeAllCommandsForKeys();

        return button;
    }

    /** If the InternalWindow is not <b>BLANK_TYPE</b>, adds or removes a
      * "close" button to (from) the InternalWindow.
      */
    public void setCloseable(boolean flag) {
        _closeable = flag;
        if (_type == BLANK_TYPE) {
            _closeable = false;
        }
        if (_closeable && _closeButton == null) {
            _closeButton = createCloseButton();
        }

        if (_closeable) {
            addSubviewToWindow(_closeButton);
        } else if (_closeButton != null) {
            _closeButton.removeFromSuperview();
        }
    }

    /** Returns <B>true</B> if the InternalWindow has a close button.
      * @see #setCloseable
      */
    public boolean isCloseable() {
        return _closeable;
    }

    /** If the InternalWindow is not <b>BLANK_TYPE</b> or <b>TITLE_TYPE</b>,
      * makes the InternalWindow resizable.
      */
    public void setResizable(boolean flag) {
        if (flag != _resizable) {
            _resizable = flag;

            if (_resizable) {
                _contentView.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
                _contentView.setVertResizeInstruction(HEIGHT_CAN_CHANGE);
            }

            if (_type == TITLE_TYPE) {
                drawBottomBorder();
                layoutParts();
            }
        }
    }

    /** Returns <B>true</B> if the InternalWindow can be resized by the
      * user.
      * @see #setResizable
      */
    public boolean isResizable() {
        return _resizable;
    }

    /** Returns the width of the controls on a resizable InternalWindowBorder.
      */
    int resizePartWidth() {
        if (_border instanceof InternalWindowBorder) {
            return ((InternalWindowBorder)_border).resizePartWidth();
        }
        return 0;
    }

    /** Returns the Size the InternalWindow must be to support a content
      * size of (<B>width</B>, <B>height</B>).
      */
    public Size windowSizeForContentSize(int width, int height) {
        return new Size(width + leftBorderMargin() + rightBorderMargin(),
                        height + titleBarMargin() + menuViewHeight() +
                        bottomBorderMargin());
    }

    /** Sets the InternalWindow's title, the string displayed in its title
      * bar.
      */
    public void setTitle(String aString) {
        if (_title != null && aString != null && _title.equals(aString)) {
            return;
        }

        _title = aString;
        drawTitleBar();
    }

    /** Returns the InternalWindow's title.
      * @see #setTitle
      */
    public String title() {
        return _title;
    }

    /** Sets the InternalWindow's Border.
      * @see Border
      */
    public void setBorder(Border border) {
        _border = border;
    }

    /** Returns the InternalWindow's Border.
      * @see #setBorder
      */
    public Border border() {
        return _border;
    }

    /** Sets the InternalWindow's layer.  InternalWindows in a higher layer
      * cannot be obscured by InternalWindows in a lower layer.
      */
    public void setLayer(int windowLayer) {
        _layer = windowLayer;
    }

    /** Returns the InternalWindow's layer.
      * @see #setLayer
      */
    public int layer() {
        return _layer;
    }

    /** Returns the InternalWindow's minimum size, based in part on the
      * presence of its Border.  <B>BLANK_TYPE</B>
      * InternalWindows return a Size instance with zero width and height.
      */
    public Size minSize() {
        if (_minSize != null) {
            return new Size(_minSize);
        }

        setMinSize(resizePartWidth() * 2 + 1, titleBarMargin() +
                   menuViewHeight() + 2);

        if (_type == BLANK_TYPE) {
            _minSize.width = 0;
            _minSize.height = 0;
            return _minSize;
        }

        if (_minSize.width < leftBorderMargin() + rightBorderMargin()) {
            _minSize.width = leftBorderMargin() + rightBorderMargin();
        }

        if (_minSize.height < titleBarMargin() + menuViewHeight() +
                                                 bottomBorderMargin()) {
            _minSize.height = titleBarMargin() + menuViewHeight() +
                                                 bottomBorderMargin();
        }

        return new Size(_minSize);
    }

    /** Sets the InternalWindow's owner, an object interested in special
      * events, such as the user closing the InternalWindow.
      */
    public void setOwner(WindowOwner anObject) {
        _owner = anObject;
    }

    /** Returns the InternalWindow's owner.
      * @see #setOwner
      */
    public WindowOwner owner() {
        return _owner;
    }

    /** Sets the MenuView that will appear along the top edge of the Window.
      * This will be an IFC View-based Menu.
      * @see MenuView
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

        x = leftBorderMargin();
        y = titleBarMargin();
        width = bounds.width - (leftBorderMargin() + rightBorderMargin());
        height = menuView.height();
        if (height == 0) {
            height = menuView.minSize().height;
        }

        menuView.setBounds(x, y, width, height);

        addSubviewToWindow(menuView);
        layoutParts();
    }

    /** Returns the MenuView that appears along the top edge of this
      * InternalWindow, null if one is not set.
      * @see #setMenuView
      *
      */
    public MenuView menuView() {
        return menuView;
    }

    /** Returns <B>true</B> if the InternalWindow's title or resize bar
      * contains the point (<b>x</b>, <b>y</b>).
      * In the case of <B>BLANK_TYPE</B> InternalWindows, this method returns
      * <b>true</b> if the point is anywhere within the InternalWindow's bounds.
      * Override this method to implement any special InternalWindow dragging
      * hotspots your InternalWindow subclass needs.
      */
    public boolean isPointInBorder(int x, int y) {
        if (_type == BLANK_TYPE) {
            return Rect.contains(0, 0, bounds.width, bounds.height, x, y);
        }

        if (_resizable && y > bounds.height - bottomBorderMargin()) {
            if (x < resizePartWidth()) {
                _resizePart = LEFT_PART;
            } else if (x > bounds.width - resizePartWidth()) {
                _resizePart = RIGHT_PART;
            } else {
                _resizePart = MIDDLE_PART;
            }
            return true;
        }

        if (y > titleBarMargin()) {
            return false;
        }

        return true;
    }

    /** Overridden to handle special condition of
      * <B>BLANK_TYPE</B> InternalWindows.
      */
    public View viewForMouse(int x, int y) {
        View    theView;
        theView = super.viewForMouse(x, y);

        if (_type == BLANK_TYPE && theView == _contentView) {
            theView = this;
        }

        return theView;
    }

    /** Sets the InternalWindow to be transparent.  A transparent
      * InternalWindow allocates an Image to buffer its drawing, and the
      * IFC's drawing machinery
      * ensures that any portions of the InternalWindow's bounds that it
      * does not draw contain the images bnehind the InternalWindow.
      * @see #isPointInBorder
      */
    public void setTransparent(boolean flag) {
        transparent = flag;

        if (transparent) {
            _contentView.setTransparent(true);
            setBuffered(true);
        } else {
            setBuffered(false);
            _contentView.setTransparent(false);
        }
    }

    /** Overridden to return <b>true</b> if the InternalWindow is transparent.
      * @see #setTransparent
      */
    public boolean isTransparent() {
        return transparent;
    }

    /** Sets the InternalWindow's type.  See the InternalWindow type constants
      * for an enumeration of the various types.
      */
    public void setType(int windowType) {
        _type = windowType;
        if (_type == BLANK_TYPE) {
            setCanBecomeMain(false);
            setCloseable(false);
        }
        layoutParts();
    }

    /** Returns the InternalWindow's type.
      * @see #setType
      */
    public int type() {
        return _type;
    }

    void updateDrawingBuffer() {
        InternalWindow  nextWindow;
        View            bufferView;
        int             i, count, start;
        Graphics        bufferedGraphics;
        Rect            windowRect, rootBounds;
        boolean         foundWindow = false;

        if (drawingBuffer == null) {
            return;
        }

        if (rootView == null) {
            throw new InconsistencyException(
                                        "Can't draw window - no RootView");
        }

        windowRect = Rect.newRect();
        rootView.disableWindowsAbove(this, true);
        reenableDrawing();
        bufferedGraphics = drawingBuffer.createGraphics();
        bufferedGraphics.setDebugOptions(shouldDebugGraphics());
        superview().convertRectToView(null, bounds, windowRect);
        bufferedGraphics.pushState();
        bufferedGraphics.translate(-windowRect.x, -windowRect.y);
        rootView.draw(bufferedGraphics, windowRect);
        bufferedGraphics.popState();

        count = isTransparent() ? rootView.windows.count()
                                : rootView.windows.indexOf(this);
        start = 0;
        for (i = count; (i-- > 0) && (start == 0); ) {
            nextWindow = (InternalWindow)rootView.windows.elementAt(i);
            if (!nextWindow.isTransparent() &&
                nextWindow.bounds.contains(bounds)) {
                start = i;
            }
        }
        if (start == 0) {
            bufferView = rootView.viewWithBuffer(rootView, windowRect);
            if (bufferView != null) {
                Rect bufferRect = Rect.newRect(0, 0, width(), height());

                convertRectToView(bufferView, bufferRect, bufferRect);
                bufferedGraphics.pushState();
                bufferedGraphics.translate(-bufferRect.x,
                                           -bufferRect.y);
                bufferView.draw(bufferedGraphics, bufferRect);
                bufferedGraphics.popState();
                Rect.returnRect(bufferRect);
            }
        }
        for (i = start; i < count; i++) {
            nextWindow = (InternalWindow)rootView.windows.elementAt(i);
            if (nextWindow.bounds.intersects(bounds)) {
                windowRect.setBounds(0, 0, width(), height());
                /* since we translated the buffer to our coordinates we need
                 * to bring it back to the other window's coordinate system
                 */
                convertRectToView(nextWindow, windowRect, windowRect);
                bufferedGraphics.pushState();
                bufferedGraphics.translate(-windowRect.x,
                                           -windowRect.y);
                nextWindow.draw(bufferedGraphics, windowRect);
                bufferedGraphics.popState();
            }
        }
        Rect.returnRect(windowRect);

        rootView.disableWindowsAbove(this, false);
        reenableDrawing();
// ALERT! - We don't seem to need this draw....but why?
//        draw(bufferedGraphics, null);
        bufferedGraphics.dispose();

    }

    /** Handles a mouse click in the title or resize bar.
      */
    public boolean mouseDown(MouseEvent event) {
        InternalWindow window;

        rootView.makeWindowVisible(this, ABOVE, null);

        if (!isPointInBorder(event.x, event.y)) {
            return false;
        }

        _lastX = event.x + bounds.x;
        _lastY = event.y + bounds.y;

        if (_resizePart != NO_PART) {
          /* left */
            window = new InternalWindow(bounds.x, bounds.y, 1, bounds.height);
            window.setType(BLANK_TYPE);
            window._contentView.setTransparent(false);
            window._contentView.setBackgroundColor(Color.darkGray);
            window.setLayer(DRAG_LAYER);
            window.setVertResizeInstruction(HEIGHT_CAN_CHANGE);
            window.setRootView(rootView());
            window.show();
            _resizeWindowVector.addElement(window);

          /* right */
            window = new InternalWindow(bounds.maxX() - 1, bounds.y,
                                        1, bounds.height);
            window.setType(BLANK_TYPE);
            window._contentView.setTransparent(false);
            window._contentView.setBackgroundColor(Color.darkGray);
            window.setLayer(DRAG_LAYER);
            window.setVertResizeInstruction(HEIGHT_CAN_CHANGE);
            window.setRootView(rootView());
            window.show();
            _resizeWindowVector.addElement(window);

          /* top */
            window = new InternalWindow(bounds.x + 1, bounds.y,
                                        bounds.width - 2, 1);
            window.setType(BLANK_TYPE);
            window._contentView.setTransparent(false);
            window._contentView.setBackgroundColor(Color.darkGray);
            window.setLayer(DRAG_LAYER);
            window.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
            window.setRootView(rootView());
            window.show();
            _resizeWindowVector.addElement(window);

          /* bottom */
            window = new InternalWindow(bounds.x + 1, bounds.maxY() - 1,
                                        bounds.width - 2, 1);
            window.setType(BLANK_TYPE);
            window._contentView.setTransparent(false);
            window._contentView.setBackgroundColor(Color.darkGray);
            window.setLayer(DRAG_LAYER);
            window.setHorizResizeInstruction(WIDTH_CAN_CHANGE);
            window.setRootView(rootView());
            window.show();
            _resizeWindowVector.addElement(window);

            return true;
        }

        _dragStart = true;

        return true;
    }

    // This override is here to hack in the old behavior of moveBy & sizeBy.
    // We don't call super()!  ALERT!

    /** Overridden to provide special InternalWindow resizing behavior. */
    public void setBounds(int x, int y, int width, int height) {
        int dx, dy, dw, dh;

        dx = x - bounds.x;
        dy = y - bounds.y;
        dw = width - bounds.width;
        dh = height - bounds.height;

        if (isVisible()) {
            moveByAndSizeBy(dx, dy, dw, dh);
            return;
        }

        _moveBy(dx, dy);
        _sizeBy(dw, dh);
        _setBounds(x, y, width, height);
    }

    private void _super_moveBy(int dx, int dy) {
        if (dx == 0 && dy == 0)
            return;

        _setBounds(bounds.x + dx, bounds.y + dy, bounds.width, bounds.height);
        if (_superview != null) {
            _superview.subviewDidMove(this);
        }
        didMoveBy(dx, dy);
    }

    private void _super_sizeBy(int dw, int dh) {
        if (dw == 0 && dh == 0)
            return;

        _setBounds(bounds.x, bounds.y, bounds.width + dw, bounds.height + dh);

        if (buffered) {
            if (bounds.width != 0 && bounds.height != 0) {
                drawingBuffer = new Bitmap(bounds.width, bounds.height);
                drawingBufferValid = false;
            } else if (drawingBuffer != null) {
                drawingBuffer.flush();
                drawingBuffer = null;
            }
        }

        disableDrawing();
        if (_superview != null) {
            _superview.subviewDidResize(this);
        }
        super.didSizeBy(dw, dh);
        reenableDrawing();
    }

    /** Called by the InternalWindow before every move, whether through
      * explicit setBounds() calls or with mouse dragging. Subclasses can
      * override this method to modify <b>newPoint</b> to
      * constrain movement. The Point <b>newPoint</b> is in the coordinate
      * system of this Window's superview.
      *
      */
    protected void willMoveTo(Point newPoint) {
    }

    private void _moveBy(int deltaX, int deltaY) {
        MouseEvent      moveEvent;
        Point           tmpPoint;

        if (deltaX == 0 && deltaY == 0) {
            return;
        }

        tmpPoint = Point.newPoint(bounds.x + deltaX, bounds.y + deltaY);
        willMoveTo(tmpPoint);
        deltaX = tmpPoint.x - bounds.x;
        deltaY = tmpPoint.y - bounds.y;
        Point.returnPoint(tmpPoint);

        if (!isVisible()) {
            _super_moveBy(deltaX, deltaY);
            return;
        }

        _lastX = bounds.x;
        _lastY = bounds.y;
        _dragStart = true;

        moveEvent = new MouseEvent(0, MouseEvent.MOUSE_DRAGGED,
                                   _lastX + deltaX, _lastY + deltaY, 0);
        mouseDragged(moveEvent);

        _dragStart = false;
    }

    void _checkSize(Size deltaSize) {
        Size    minSize;

        minSize = minSize();
        if (bounds.width + deltaSize.width < minSize.width) {
            deltaSize.width = minSize.width - bounds.width;
        }
        if (bounds.height + deltaSize.height < minSize.height) {
            deltaSize.height = minSize.height - bounds.height;
        }

        if (_owner != null) {
            _owner.windowWillSizeBy(this, deltaSize);
        }
    }

    private void _sizeBy(int deltaWidth, int deltaHeight) {
        Rect    tmpRect;
        Size    tmpSize;

        if (deltaWidth == 0 && deltaHeight == 0) {
            return;
        }

        tmpSize = Size.newSize(deltaWidth, deltaHeight);
        _checkSize(tmpSize);
        deltaWidth = tmpSize.width;
        deltaHeight = tmpSize.height;
        Size.returnSize(tmpSize);

        if (!isVisible()) {
            _super_sizeBy(deltaWidth, deltaHeight);
            layoutParts();
            return;
        }

        tmpRect = Rect.newRect(bounds);
        _super_sizeBy(deltaWidth, deltaHeight);
        layoutParts();

        /* in Constructor, we may not be subviews of the RootView, so
         * have to convert
         */
        if (canDraw()) {
            tmpRect.unionWith(bounds);
            superview().convertRectToView(null, tmpRect, tmpRect);
            rootView.redraw(tmpRect);
        }

        Rect.returnRect(tmpRect);
    }

    /** Smoothly, simultaneously moves and resizes a visible Window.
      */
    private void moveByAndSizeBy(int deltaX, int deltaY, int deltaWidth,
                                 int deltaHeight) {
        Rect    tmpRect;
        Size    tmpSize;
        Point   tmpPoint;

        if (deltaX == 0 && deltaY == 0 && deltaWidth == 0 &&
            deltaHeight == 0) {
            return;
        }

        tmpSize = Size.newSize(deltaWidth, deltaHeight);
        _checkSize(tmpSize);
        deltaWidth = tmpSize.width;
        deltaHeight = tmpSize.height;
        Size.returnSize(tmpSize);

        tmpPoint = Point.newPoint(bounds.x + deltaX, bounds.y + deltaY);
        willMoveTo(tmpPoint);
        deltaX = tmpPoint.x - bounds.x;
        deltaY = tmpPoint.y - bounds.y;
        Point.returnPoint(tmpPoint);

        if (!isVisible()) {
            _super_moveBy(deltaX, deltaY);
            _super_sizeBy(deltaWidth, deltaHeight);
            layoutParts();
            return;
        }

        tmpRect = Rect.newRect(bounds);
        _super_moveBy(deltaX, deltaY);
        _super_sizeBy(deltaWidth, deltaHeight);
        if (deltaWidth != 0 || deltaHeight != 0) {
            layoutParts();
        }
        tmpRect.unionWith(bounds);

        /// If we are not the immediate subview of our rootView, we need
        /// to translate the rect to the rootView. This happens in constructor
        /// when we wrap the InternalWindow in a ComponentView. Normally this
        /// should not happen, as the InternalWindow will always be in the rootView.
        if(superview() != rootView) {
            superview().convertRectToView(rootView, tmpRect, tmpRect);
        }

        if (isTransparent()) {
            disableDrawing();
            rootView.redraw(tmpRect);
            reenableDrawing();
            draw();
        } else {
            rootView.redraw(tmpRect);
        }

        Rect.returnRect(tmpRect);
    }

    /** Convenience method for smoothly, simultaneously moving and resizing a
      * visible Window.  Equivalent to
      * <pre>
      *     moveByAndSizeBy(x - bounds.x, y - bounds.y,
      *                     width - bounds.width,
      *                     height - bounds.height);
      * </pre>
      */
    private void moveToAndSizeTo(int x, int y, int width, int height) {
        moveByAndSizeBy(x - bounds.x, y - bounds.y,
                        width - bounds.width,
                        height - bounds.height);
    }

    /** Configures the InternalWindow to move itself in response to scroll
      * requests sent via <b>scrollRectToVisible</b>.  By default, any scroll
      * requests received via <b>scrollRectToVisible</b> are ignored.  If
      * <b>flag</b> is <b>true</b>, the InternalWindow will move itself so
      * that the scroll rectangle is completely visible, meaning not clipped
      * by the RootView.  Popups use this feature to allow users to
      * navigate Popup windows that popup such that they are clipped by
      * the RootView.
      * @see View#scrollRectToVisible
      * @private
      */
    public void setScrollsToVisible(boolean flag) {
        scrollToVisible = flag;
    }

    /** Returns <b>true</b> if the InternalWindow moves itself in response to
      * scroll requests sent via <b>scrollRectToVisible</b>.
      * @see #setScrollToVisible
      * @private
      */
    public boolean scrollsToVisible() {
        return scrollToVisible;
    }

    /** Overridden to autoscroll the InternalWindow if configured to
      * autoscroll.
      * @see #setScrollToVisible
      * @private
      */
    public void scrollRectToVisible(Rect aRect) {
        Rect    visibleRect, tmpRect, absoluteBounds, backgroundBounds;
        int     deltaX = 0, deltaY = 0;

        if (!scrollToVisible) {
            return;
        }

        visibleRect = Rect.newRect();
        computeVisibleRect(visibleRect);

        /* are we completely visible? */
        if (visibleRect.width == bounds.width &&
            visibleRect.height == bounds.height) {
            Rect.returnRect(visibleRect);
            return;
        }

        if (!visibleRect.contains(aRect)) {
            convertRectToView(null, visibleRect, visibleRect);
            tmpRect = Rect.newRect();
            convertRectToView(null, aRect, tmpRect);
            absoluteBounds = Rect.newRect(0, 0, width(), height());
            convertRectToView(null, absoluteBounds, absoluteBounds);

            if (tmpRect.x < visibleRect.x &&
                visibleRect.x > absoluteBounds.x) {
                deltaX = visibleRect.x - tmpRect.x;
            } else if (tmpRect.maxX() > visibleRect.maxX() &&
                       visibleRect.maxX() < absoluteBounds.maxX()) {
                deltaX = visibleRect.maxX() - tmpRect.maxX();
            }

            if (tmpRect.y < visibleRect.y &&
                visibleRect.y > absoluteBounds.y) {
                deltaY = visibleRect.y - tmpRect.y;
            } else if (tmpRect.maxY() > visibleRect.maxY() &&
                       visibleRect.maxY() < absoluteBounds.maxY()) {
                deltaY = visibleRect.maxY() - tmpRect.maxY();
            }

            /* don't yank onscreen more than 3 pixels, any side */
            backgroundBounds = rootView().bounds;
            if (deltaX > 0 && absoluteBounds.x + deltaX > 3) {
                deltaX = 3 - absoluteBounds.x;
            } else if (deltaX < 0 && absoluteBounds.maxX() + deltaX <
                                                backgroundBounds.maxX() - 3) {
                deltaX = backgroundBounds.maxX() - 3 - absoluteBounds.maxX();
            }
            if (deltaY > 0 && absoluteBounds.y + deltaY > 3) {
                deltaY = 3 - absoluteBounds.y;
            } else if (deltaY < 0 && absoluteBounds.maxY() + deltaY <
                                                backgroundBounds.maxY() - 3) {
                deltaY = backgroundBounds.maxY() - 3 - absoluteBounds.maxY();
            }

            moveBy(deltaX, deltaY);

            Rect.returnRect(tmpRect);
            Rect.returnRect(absoluteBounds);
        }

        Rect.returnRect(visibleRect);
    }

    /** Overridden to do nothing.
      */
    public void subviewDidResize() {
    }

    /** Centers the InternalWindow's within its RootView. */
    public void center() {
        Rect    rootViewBounds;
        Rect    newBounds;
        rootViewBounds = rootView.bounds;

        newBounds = new Rect((int)((rootViewBounds.width - bounds.width) / 2),
                               (int)((rootViewBounds.height - bounds.height) / 2),
                               bounds.width, bounds.height);
        if(newBounds.y < 0)
            newBounds.y = 0;
        setBounds(newBounds);
    }

    void mouseResizeDrag(MouseEvent event) {
        InternalWindow  window;
        Rect            newBounds;
        Size            tmpSize;
        int             deltaX = 0, deltaY = 0;

        event.x += bounds.x;
        event.y += bounds.y;

        if (_resizePart == MIDDLE_PART /*||
            (_resizeInstructions & WIDTH_CAN_CHANGE) == 0*/) {
            event.x = _lastX;
        }

        newBounds = Rect.newRect(bounds);

        if (_resizePart == LEFT_PART) {
            newBounds.moveBy(event.x - _lastX, 0);
            newBounds.sizeBy(_lastX - event.x, event.y - _lastY);
        } else {
            newBounds.sizeBy(event.x - _lastX, event.y - _lastY);
        }
        /* don't let the user drag the bottom border off the screen */
        if (newBounds.height > superview().height() - newBounds.y) {
            newBounds.sizeBy(0, superview().height() - newBounds.height -
                             newBounds.y);
        }
        tmpSize = Size.newSize(newBounds.width - bounds.width,
                               newBounds.height - bounds.height);
        _checkSize(tmpSize);
        if (_resizePart == LEFT_PART) {
            if (newBounds.x > bounds.x + bounds.width) {
                newBounds.moveBy(bounds.x - newBounds.x - tmpSize.width, 0);
            } else {
                newBounds.moveBy(newBounds.width - bounds.width -
                                 tmpSize.width, 0);
            }
        }
        newBounds.sizeBy(tmpSize.width -
                         (newBounds.width - bounds.width),
                         tmpSize.height -
                         (newBounds.height - bounds.height));
        Size.returnSize(tmpSize);

        window = (InternalWindow)_resizeWindowVector.elementAt(0);
        deltaY = newBounds.height - window.bounds.height;
        if (_resizePart == LEFT_PART) {
            if (deltaY < 0) {
              /* right, top, bottom, left */
                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.sizeTo(1, newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(2);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.y,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.moveToAndSizeTo(newBounds.x, newBounds.y, 1,
                                       newBounds.height);
            } else {
              /* left, bottom, top, right */
                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.moveToAndSizeTo(newBounds.x, newBounds.y, 1,
                                       newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(2);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.y,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.sizeTo(1, newBounds.height);
            }
        } else if (_resizePart == MIDDLE_PART) {
            if (deltaY < 0) {
              /* left, right, bottom */
                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.sizeTo(1, newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.sizeTo(1, newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       window.bounds.width,
                                       window.bounds.height);
            } else {
              /* bottom, left, right */
                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       window.bounds.width,
                                       window.bounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.sizeTo(1, newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.sizeTo(1, newBounds.height);
            }
        } else {
            if (deltaY < 0) {
              /* left, top, bottom, right */
                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.sizeTo(1, newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(2);
                window.sizeTo(newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.moveToAndSizeTo(newBounds.maxX() - 1, newBounds.y, 1,
                                       newBounds.height);
            } else {
              /* right, bottom, top, left */
                window = (InternalWindow)_resizeWindowVector.elementAt(1);
                window.moveToAndSizeTo(newBounds.maxX() - 1, newBounds.y, 1,
                                       newBounds.height);

                window = (InternalWindow)_resizeWindowVector.elementAt(3);
                window.moveToAndSizeTo(newBounds.x + 1, newBounds.maxY() - 1,
                                       newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(2);
                window.sizeTo(newBounds.width - 2, 1);

                window = (InternalWindow)_resizeWindowVector.elementAt(0);
                window.sizeTo(1, newBounds.height);
            }
        }

        Rect.returnRect(newBounds);
    }

    /** Handles a mouse drag in the title or resize bar.
      */
    public void mouseDragged(MouseEvent event) {
        Graphics        g;
        Vector          rectVector;
        Rect            oldBounds, newBounds, clipRect;
        Point           tmpPoint;
        int             x, y, minX, minY, maxX, maxY, eventX, eventY;
        boolean         canCopyBits, shouldDraw, clipRectContainsNewBounds;

        /* resize? */
        if (_resizePart != NO_PART) {
            mouseResizeDrag(event);
            return;
        }

        /* we want everything to be in the absolute coord. system */
        eventX = event.x + bounds.x;
        eventY = event.y + bounds.y;

        /* don't let the user drag the window completely offscreen */
        if (_type == BLANK_TYPE) {
            minY = -5;
        } else {
            minY = -titleBarMargin() + 5;
        }
        maxY = superview().height() - 5;

        y = bounds.y + eventY - _lastY;
        if (y < minY) {
            eventY = minY + _lastY - bounds.y;
        } else if (y > maxY) {
            eventY = maxY + _lastY - bounds.y;
        }

        minX = -bounds.width + 5;
        maxX = superview().width() - 5;

        x = bounds.x + eventX - _lastX;
        if (x < minX) {
            eventX = minX + _lastX - bounds.x;
        } else if (x > maxX) {
            eventX = maxX + _lastX - bounds.x;
        }

        tmpPoint = Point.newPoint(bounds.x + eventX - _lastX,
                                  bounds.y + eventY - _lastY);
        willMoveTo(tmpPoint);
        eventX = tmpPoint.x + _lastX - bounds.x;
        eventY = tmpPoint.y + _lastY - bounds.y;
        Point.returnPoint(tmpPoint);

        /* setup */
        clipRect = Rect.newRect();
        computeVisibleRect(clipRect);
//        clipRect.x += bounds.x;
//        clipRect.y += bounds.y;

        /* in constructor, Window may not be subview of RootView, so
         * can't just assume its bounds is in the absolute coordinate
         * system.
         */
        oldBounds = Rect.newRect();
        convertRectToView(null, clipRect, oldBounds);
        newBounds = Rect.newRect(clipRect);
        newBounds.moveBy(eventX - _lastX, eventY - _lastY);
        clipRectContainsNewBounds = clipRect.contains(newBounds);
        convertRectToView(null, newBounds, newBounds);

        rectVector = rootView.windowRects(newBounds, this);

        if (_dragStart) {
            if (rectVector == null || rectVector.isEmpty()) {
                rectVector = rootView.windowRects(oldBounds, this);
            }

            _dragStart = false;
        }

        if ((rectVector == null || rectVector.isEmpty()) &&
            clipRectContainsNewBounds && !isTransparent()) {
            canCopyBits = true;
            shouldDraw = false;
        } else {
            canCopyBits = false;
            shouldDraw = true;
            if (!isTransparent() && drawingBuffer == null) {
                Graphics bufferedGraphics;

                _createdDrawingBuffer = true;
                setBuffered(true);
                _drawToBackingStore = true;
                bufferedGraphics = drawingBuffer.createGraphics();
                bufferedGraphics.setDebugOptions(shouldDebugGraphics());
                draw(bufferedGraphics, null);
                bufferedGraphics.dispose();
                bufferedGraphics = null;
                _drawToBackingStore = false;
            }
        }

        VectorCache.returnVector(rectVector);

        if (canCopyBits && !_drewOnLastDrag) {
            g = rootView().createGraphics();
            _super_moveBy(eventX - _lastX, eventY - _lastY);
            g.setClipRect(clipRect);

            g.copyArea(oldBounds.x, oldBounds.y, oldBounds.width,
                       oldBounds.height, bounds.x, bounds.y);
            g.dispose();
            g = null;
        } else if (canCopyBits) {
            _super_moveBy(eventX - _lastX, eventY - _lastY);
            _drewOnLastDrag = false;
            shouldDraw = true;
        } else {
            _super_moveBy(eventX - _lastX, eventY - _lastY);
        }

        if (shouldDraw && !isTransparent()) {
            if (drawingBuffer != null) {
                drawingBufferIsBitCache = true;
            }

            draw();

            if (drawingBuffer != null) {
                drawingBufferIsBitCache = false;
            }
            if (!canCopyBits) {
                _drewOnLastDrag = true;
            }
        }

        if (isTransparent()) {
            draw();
        }
        rootView.disableWindowsAbove(this, true);
        rootView.redraw(oldBounds);
        rootView.disableWindowsAbove(this, false);

        _lastX = eventX;
        _lastY = eventY;

        oldBounds.unionWith(bounds);
        rootView.redrawTransparentWindows(oldBounds, this);

        Rect.returnRect(clipRect);
    }

    /** Handles a mouse up in the title or resize bar.
      */
    public void mouseUp(MouseEvent event) {
        InternalWindow nextWindow, secondWindow;
        int             i;

        if (_resizePart != NO_PART) {
            nextWindow = (InternalWindow)_resizeWindowVector.elementAt(0);
            secondWindow = (InternalWindow)_resizeWindowVector.elementAt(1);

            if (_resizePart == LEFT_PART) {
                moveByAndSizeBy(nextWindow.bounds.x - bounds.x, 0,
                                secondWindow.bounds.x - nextWindow.bounds.x -
                                                            bounds.width + 1,
                                nextWindow.bounds.height - bounds.height);
            } else {
                sizeTo(secondWindow.bounds.x - nextWindow.bounds.x + 1,
                       nextWindow.bounds.height);
            }

            i = _resizeWindowVector.count();
            while (i-- > 0) {
                nextWindow = (InternalWindow)_resizeWindowVector.elementAt(i);
                nextWindow.hide();
            }

            _resizeWindowVector.removeAllElements();

            _resizePart = NO_PART;
        }

        if (_createdDrawingBuffer) {
            setBuffered(false);
            _createdDrawingBuffer = false;
        }
    }

    void _drawLine(Graphics g, int x1, int x2, int y, Color c1, Color c2) {
        int     i, count;

        g.setColor(c1);
        for (i = x1; i <= x2; i += 2) {
            g.drawLine(i, y, i, y);
        }
        g.setColor(c2);
        for (i = x1 + 1; i <= x2; i += 2) {
            g.drawLine(i, y, i, y);
        }
    }

    /** Draws the InternalWindow's Border.
      * If the InternalWindow is of type <B>BLANK_TYPE</B>, this method does
      * nothing. You never call this method directly - use the <b>draw()</b>
      * method to draw the InternalWindow.
      * @see View#draw
      */
    public void drawView(Graphics g) {
        if (drawingBuffer != null && !g.isDrawingBuffer() ||
            _type == BLANK_TYPE) {
            return;
        }

        _border.drawInRect(g, 0, 0, bounds.width, bounds.height);
    }

    /** Convenience method for drawing the InternalWindow's title bar portion,
      * only if the InternalWindow is not of type
      * <b>BLANK_TYPE</b>.  Calls <B>draw()</B>.
      */
    public void drawTitleBar() {
        Rect    tmpRect;

        if (_type == BLANK_TYPE) {
            return;
        }

        tmpRect = Rect.newRect(0, 0, bounds.width, titleBarMargin());
        draw(tmpRect);

        Rect.returnRect(tmpRect);
    }


    /** Convenience method for drawing the InternalWindow's bottom Border
      * portion , only if the InternalWindow is not of type
      * <b>BLANK_TYPE</b>.  Calls <B>draw()</B>.
      */
    public void drawBottomBorder() {
        Rect    tmpRect;

        if (_type == BLANK_TYPE) {
            return;
        }

        tmpRect = Rect.newRect(0, bounds.height - bottomBorderMargin(),
                               bounds.width, bottomBorderMargin());
        draw(tmpRect);

        Rect.returnRect(tmpRect);
    }

    /** Overridden to handle special needs of transparent InternalWindows.
      */
    public void draw(Graphics g, Rect clipRect) {
        View        containingView;

       if (isTransparent() && (g == null || !g.isDrawingBuffer()) &&
            rootView._redrawTransWindows) {
//            containingView = _viewForRect(clipRect, this);
            containingView = null;
            if (containingView == null || containingView.isTransparent()) {
                updateDrawingBuffer();
            }
        }
        super.draw(g, clipRect);
    }

    /** Informs the InternalWindow that it has become the main InternalWindow,
      * and notifies its owner.
      * @see #setOwner
      */
    public void didBecomeMain() {
        drawTitleBar();

        if (_owner != null) {
            _owner.windowDidBecomeMain(this);
        }

        rootView.setFocusedView(focusedView(), false);

        if(containsDocument()) {
            Application.application().makeCurrentDocumentWindow(this);
        }
    }

    /** Informs the InternalWindow that another InternalWindow has replaced it
      * as the Application's main InternalWindow, and notifies its owner.
      * @see #setOwner
      */
    public void didResignMain() {
        drawTitleBar();

        if (_owner != null) {
            _owner.windowDidResignMain(this);
        }
        if (rootView != null)
            rootView.setFocusedView(null, false);
    }

    /** Sets whether the window contains a document. Windows containing document
      * are treated in a different manner by the target chain.
      *
      */
    public void setContainsDocument(boolean containsDocument) {
        _containsDocument = containsDocument;
    }

    /** Return whether the window contains a document.
     */
    public boolean containsDocument() {
        return _containsDocument;
    }

    /** For backward compatibility
     *  @private
     */
    public void setCanBecomeDocument(boolean containsDocument) {
        setContainsDocument(containsDocument);
    }

    /** For backward compatibility
     *  @private
     */
    public boolean canBecomeDocument() {
        return containsDocument();
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


    /** Sets the InternalWindow's "focused" View, the View that will receive
      * key events.
      */
    public void setFocusedView(View view) {
        _focusedView = view;
        if (rootView != null) {
            if(rootView.mainWindow() == this && view != null)
                rootView.makeWindowVisible(this,ABOVE,null);
            rootView.setFocusedView(focusedView());
        }
    }

    /** Returns the InternalWindow's "focused" View.
      * @see #setFocusedView
      */
    public View focusedView() {
        if(_focusedView != null && _focusedView.descendsFrom(this))
            return _focusedView;
        else {
            _focusedView = null;
            return _focusedView;
        }
    }

/* archiving */


    /** Describes the InternalWindow class' information.
      * @seeCodable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.InternalWindow", 3);
        info.addField(ownerKey, OBJECT_TYPE);
        info.addField(contentViewKey, OBJECT_TYPE);
        info.addField(focusedViewKey, OBJECT_TYPE);
        info.addField(closeButtonKey, OBJECT_TYPE);
        info.addField(titleFontKey, OBJECT_TYPE);
        info.addField(titleKey, STRING_TYPE);
        info.addField(borderKey, OBJECT_TYPE);
        info.addField(layerKey, INT_TYPE);
        info.addField(typeKey, INT_TYPE);
        info.addField(closeableKey, BOOLEAN_TYPE);
        info.addField(resizableKey, BOOLEAN_TYPE );
        info.addField(canBecomeMainKey, BOOLEAN_TYPE);
        info.addField(containsDocumentKey, BOOLEAN_TYPE);
        info.addField(onscreenKey, BOOLEAN_TYPE);
        info.addField(transparentKey, BOOLEAN_TYPE);
        info.addField(scrollToVisibleKey, BOOLEAN_TYPE);
        info.addField(defaultSelectedViewKey,OBJECT_TYPE);

        info.addField(menuViewKey, OBJECT_TYPE);
    }

    /** Encodes the InternalWindow instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(ownerKey, (Codable)_owner);
        encoder.encodeObject(contentViewKey, _contentView);
        encoder.encodeObject(focusedViewKey, focusedView());
        encoder.encodeObject(closeButtonKey, _closeButton);
        encoder.encodeObject(titleFontKey, _titleFont);

        encoder.encodeString(titleKey, _title);

        encoder.encodeObject(borderKey, _border);

        encoder.encodeInt(layerKey, _layer);
        encoder.encodeInt(typeKey, _type);

        encoder.encodeBoolean(closeableKey, _closeable);
        encoder.encodeBoolean(resizableKey, _resizable);
        encoder.encodeBoolean(canBecomeMainKey, _canBecomeMain);
        encoder.encodeBoolean(containsDocumentKey, _containsDocument);
        encoder.encodeBoolean(onscreenKey, _onscreen);
        encoder.encodeBoolean(transparentKey, transparent);
        encoder.encodeBoolean(scrollToVisibleKey, scrollToVisible);
        encoder.encodeObject(defaultSelectedViewKey,_defaultSelectedView);

        encoder.encodeObject(menuViewKey, menuView);
    }

    /** Decodes the InternalWindow instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.TextField");
        super.decode(decoder);

        _owner = (WindowOwner)decoder.decodeObject(ownerKey);
        _contentView = (WindowContentView)decoder.decodeObject(contentViewKey);
        _focusedView = (View)decoder.decodeObject(focusedViewKey);
        _closeButton = (Button)decoder.decodeObject(closeButtonKey);
        _titleFont = (Font)decoder.decodeObject(titleFontKey);

        _title = decoder.decodeString(titleKey);

        _border = (Border)decoder.decodeObject(borderKey);

        _layer = decoder.decodeInt(layerKey);
        _type = decoder.decodeInt(typeKey);

        _closeable = decoder.decodeBoolean(closeableKey);
        _resizable = decoder.decodeBoolean(resizableKey);
        _canBecomeMain = decoder.decodeBoolean(canBecomeMainKey);
        _containsDocument = decoder.decodeBoolean(containsDocumentKey);
        _onscreen = decoder.decodeBoolean(onscreenKey);
        transparent = decoder.decodeBoolean(transparentKey);
        scrollToVisible = decoder.decodeBoolean(scrollToVisibleKey);
        if(version > 1) {
            _defaultSelectedView = (View) decoder.decodeObject(defaultSelectedViewKey);
        }
        if (version > 2) {
            menuView = (MenuView)decoder.decodeObject(menuViewKey);
        }
    }

    /** Finishes the InternalWindow instance decoding.  Initializes the Slider's value.
     * @see Codable#finishDecoding
     */
    public void finishDecoding() throws CodingException {
        super.finishDecoding();
        if(_closeButton != null)
            _closeButton.removeAllCommandsForKeys();
    }

    /** Returns the InternalWindow's string representation. */
    public String toString() {
        if (_title != null)
            return "InternalWindow (" + _title + ")";
        else
            return super.toString();
    }

    View ancestorWithDrawingBuffer() {
        if (drawingBuffer != null) {
            return this;
        }

        return null;
    }

    /** Overridden to return the InternalWindow.
      * @see View#window
      */
    public InternalWindow window() {
        return this;
    }

    /** @private */
    public Font font() {
        return _titleFont;
    }

    /** Implements InternalWindow's commands:
      * <ul>
      * <li>SHOW - calls the InternalWindow's <b>show()</b> method.
      * <li>HIDE - calls the InternalWindow's <b>hide()</b> method.
      * </ul>
      * Subclasses of InternalWindow should call super.performCommand() to
      * allow the default close Button to send its "hide" command.
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

    /** Overriden to return true.  Internal windows are root for keyboard UI.
      *
      */
    public boolean hidesSubviewsFromKeyboard() {
        return true;
    }

    /** Set the default selected view, when the window becomes main.
      *
      */
    public void setDefaultSelectedView(View aView) {
        _defaultSelectedView = aView;
    }

    /** Returns the default selected view when the window become main
      *
      */
    public View defaultSelectedView() {
        return _defaultSelectedView;
    }

    /** Overriden to return true if the window is currently main
      *
      */
    public boolean canBecomeSelectedView() {
        if(isMain())
            return true;
        else
            return false;
    }

    boolean wantsKeyboardArrow() {
        return false;
    }
}

// RootView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass that functions as the top level View of the Applet frame
  * or ExternalWindow. It displays a Color and/or an Image (tiled, centered,
  * or scaled). All other Applet Views and InternalWindows are descendants
  * of the RootView.<p>You will rarely need to work with your
  * Applet's RootView. You will never instantiate a RootView
  * directly.
  * @note 1.0 RootView now keep track of its focused view when a window become
  * main when there is no longer any main window, the focus resume for the
  * rootview focused view.
  * @note 1.0 lots of changes drawing, keyboard UI, coordinate trns, menus
  */

// ALERT! Need a boolean that says we're in mouse drag mode
public class RootView extends View implements EventProcessor,
    ExtendedTarget {
    Color               _backgroundColor;
    Image               _image;
    FoundationPanel     panel;
    Application         application;

    Timer               _autoscrollTimer;
    ColorChooser        colorChooser;
    FontChooser         fontChooser;
    InternalWindow      _mainWindow;
    View                _mouseView, _moveView, _focusedView, _windowClipView,
                        _mouseClickView,_rootViewFocusedView;
                        /* Warning always use rootViewFocusedView()
                           to access this ivar */
    View                _selectedView; /* Selected view for keyboard UI */
    View                _defaultSelectedView;
    Vector              windows = new Vector();
    long                _lastClickTime;
    int                 _clickCount, _mouseX, _mouseY, _currentCursor,
                        _viewCursor, _overrideCursor = DEFAULT_CURSOR,
                        mouseDownCount, _imageDisplayStyle;
    Vector              dirtyViews = new Vector();
    boolean             _redrawTransWindows = true, recomputeCursor,
                        recomputeMoveView, isVisible, redrawAll = true;
    Vector              componentViews;
    MouseFilter         mouseFilter = new MouseFilter();

    static Vector _commands;

    static final String VALIDATE_SELECTED_VIEW = "validateSelectedView";

    static {
        _commands = new Vector();
        _commands.addElement(SHOW_FONT_CHOOSER);
        _commands.addElement(SHOW_COLOR_CHOOSER);
        _commands.addElement(NEW_FONT_SELECTION);
        _commands.addElement(VALIDATE_SELECTED_VIEW);
    }

    /* constructors */

    /** Constructs a RootView with origin <B>(0, 0)</b> and zero
      * width and height.
      */
    public RootView() {
        this(0, 0, 0, 0);
    }

    /** Constructs a RootView with bounds <B>rect</B>.
      */
    public RootView(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a RootView with
      * bounds (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>)
      */
    public RootView(int x, int y, int width, int height) {
        super(x, y, width, height);

        _backgroundColor = Color.gray;
        application = Application.application();
        _defaultSelectedView = this;
    }

    /* window management */

    void addWindowRelativeTo(InternalWindow aWindow, int position,
                             InternalWindow otherWindow) {
        if (aWindow == null) {
            return;
        }

        if (_windowClipView != null && aWindow.layer() < InternalWindow.IGNORE_WINDOW_CLIPVIEW_LAYER) {
            _windowClipView.addSubview(aWindow);
        } else {
            addSubview(aWindow);
        }

        makeWindowVisible(aWindow, position, otherWindow);
    }

    void removeWindow(InternalWindow aWindow) {
        InternalWindow  newMain, theWindow;
        int             index,count;
        Rect windowBounds;

        if (aWindow == null) {
            return;
        }

        windowBounds = absoluteWindowBounds(aWindow);

        aWindow.removeFromSuperview();

        index = windows.indexOf(aWindow) - 1;
        if (index < 0) {
            index = windows.indexOf(aWindow);
        }

        windows.removeElement(aWindow);
        if (aWindow == _mainWindow) {
            theWindow = null;
            for(index = windows.count() - 1 ; index >= 0 ; index--) {
                if(((InternalWindow)windows.elementAt(index)).canBecomeMain()) {
                    theWindow = (InternalWindow)windows.elementAt(index);
                    break;
                }
            }
            _setMainWindow(theWindow);
        }

        redraw(windowBounds);
        redrawTransparentWindows(windowBounds, null);

        if(!(aWindow  instanceof KeyboardArrow))
            validateSelectedView();
        createMouseEnterLater();
    }

    /** Returns the Vector containing all InternalWindows currently displayed
      * in the RootView.  Do <i>not</i> modify this Vector.
     */
    public Vector internalWindows() {
        return windows;
    }

    /** Returns the Application's main InternalWindow.  The main InternalWindow
      * represents the InternalWindow the user is currently working with, and
      * displays its title bar differently than all other InternalWindows.  The
      * IFC passes key Events to the View in the main InternalWindow that has
      * requested to receive them.
      * @see InternalWindow
      */
    public InternalWindow mainWindow() {
        return _mainWindow;
    }

    void _setMainWindow(InternalWindow newMain) {
        InternalWindow      oldMain,nextWindow;

        if ((_mainWindow == newMain) ||
            (newMain != null && !newMain.canBecomeMain())) {
            return;
        }

        oldMain = _mainWindow;
        _mainWindow = newMain;

        if (oldMain != null) {
            oldMain.didResignMain();
        } else if(_focusedView != null && !isInWindow(_focusedView)) {
            _rootViewFocusedView = _focusedView;
            setFocusedView(null,false);
        }


        if (_mainWindow != null) {
            _mainWindow.didBecomeMain();
        } else if(rootViewFocusedView() != null) {
            setFocusedView(rootViewFocusedView(),false);
            _rootViewFocusedView = null;
        }
        validateSelectedView();
        createMouseEnterLater();
    }

    InternalWindow frontWindowWithLayer(int aLayer) {
        int i;
        InternalWindow win;

        for(i = windows.count()-1; i >= 0 ; i--) {
            win = (InternalWindow)windows.elementAt(i);
            if(win.layer() == aLayer)
                return win;
        }
        return null;
    }

    InternalWindow backWindowWithLayer(int aLayer) {
        int i,c = windows.count();
        InternalWindow win;

        for(i=0; i<c ; i++) {
            win = (InternalWindow)windows.elementAt(i);
            if(win.layer() == aLayer)
                return win;
        }
        return null;
    }

    void makeWindowVisible(InternalWindow aWindow, int position,
                           InternalWindow otherWindow) {
        InternalWindow          nextWindow = null;
        int             originalIndex, windowLayer, i;
        boolean         found, redrawWindow, inserted;

        if (aWindow == null) {
            return;
        }

        /** If the window is not in this view hierarchy
         *  it it impossible to move it to the front
         */
        if(!aWindow.descendsFrom(this))
            return;

        originalIndex = windows.indexOf(aWindow);
        windowLayer = aWindow.layer();

      /* make sure relative window is in the same layer */
        if (otherWindow != null) {
            if (otherWindow.layer() > windowLayer) {
                otherWindow = null;
                position = InternalWindow.ABOVE;
            } else if (otherWindow.layer() < windowLayer) {
                otherWindow = null;
                position = InternalWindow.BEHIND;
            }
        }

        windows.removeElement(aWindow);

        if (otherWindow != null) {
            if (position == InternalWindow.ABOVE) {
                found = windows.insertElementAfter(aWindow, otherWindow);
            } else {
                found = windows.insertElementBefore(aWindow, otherWindow);
            }

            if (found) {
                _setMainWindow(aWindow);

                if (originalIndex != windows.indexOf(aWindow)) {
                    aWindow.draw();
                    updateTransWindows(aWindow);
                    if(position == InternalWindow.BEHIND) {
                        updateWindowsAbove(aWindow);
                    }
                }
                return;
            }

          /* otherWindow not in window list, so just ignore */
            otherWindow = null;
        }

      /* make first window in our layer */
        i = windows.count();
        while (i-- > 0) {
            nextWindow = (InternalWindow)windows.elementAt(i);

            if (nextWindow.layer() > windowLayer) {
                continue;
            } else if (nextWindow.layer() <= windowLayer) {
                break;
            }
        }

        if (nextWindow == null) {
            windows.insertElementAt(aWindow, 0);
            inserted = true;
        } else if (nextWindow.layer() > windowLayer) {
            inserted = windows.insertElementBefore(aWindow, nextWindow);
        } else {
            inserted = windows.insertElementAfter(aWindow, nextWindow);
        }
        if (!inserted) {
            windows.insertElementAt(aWindow, 0);
        }

        _setMainWindow(aWindow);

        if (originalIndex != windows.indexOf(aWindow)) {
            aWindow.draw();
            updateTransWindows(aWindow);
        }
    }


    void updateWindowsAbove(InternalWindow aWindow) {
        InternalWindow win;
        int i,c,index;
        Rect windowBounds = absoluteWindowBounds(aWindow);
        Rect  winBounds;
        index = windows.indexOf(aWindow);
        Rect  inter = new Rect();

        for(i=index+1,c=windows.count() ; i < c ;i++) {
            win = (InternalWindow) windows.elementAt(i);
            winBounds = absoluteWindowBounds(win);
            if(windowBounds.intersects(winBounds)) {
                inter.setBounds(windowBounds);
                inter.intersectWith(winBounds);
                convertRectToView(win,inter,inter);
                win.addDirtyRect(inter);
            }
        }
    }

    void updateTransWindows(InternalWindow belowWindow) {
        InternalWindow          nextWindow;
        Rect            belowRect;
        int             i, count;

        if (belowWindow == null) {
            return;
        }

        /// ALERT FIX 3
        count = windows.indexOf(belowWindow);
        belowRect = belowWindow.superview().convertRectToView(this, belowWindow.bounds);
//        belowRect = belowWindow.bounds;
        for (i = 0; i < count; i++) {
            Rect windowBounds;
            nextWindow = (InternalWindow)windows.elementAt(i);

            if (!nextWindow.isTransparent()) {
                continue;
            }
            windowBounds = absoluteWindowBounds(nextWindow);

            if (windowBounds.intersects(belowRect) ||
                belowRect.intersects(windowBounds)) {

                nextWindow.updateDrawingBuffer();
            }
        }
    }


  /* drawing */

    void disableWindowsAbove(InternalWindow aWindow, boolean flag) {
        InternalWindow  nextWindow;
        int     i, count;

        if (aWindow == null) {
            return;
        }

        i = windows.indexOf(aWindow);

        if (i == -1) {
            return;
        }

        count = windows.count();
        for (; i < count; i++) {
            nextWindow = (InternalWindow)windows.elementAt(i);
            if (flag) {
                nextWindow.disableDrawing();
            } else {
                nextWindow.reenableDrawing();
            }
        }
    }

    Vector windowRects(Rect aRect, InternalWindow viewWindow) {
        InternalWindow    nextWindow;
        Vector            rectVector = null;
        int               i, count;

        count = windows.count();
        if (viewWindow != null) {
            i = windows.indexOf(viewWindow) + 1;
        } else {
            i = 0;
        }
        for (; i < count; i++) {
            /// ALERT FIX 4 - THIS IS A BIG ONE
            Rect windowBounds;
            nextWindow = (InternalWindow)windows.elementAt(i);

            windowBounds = absoluteWindowBounds(nextWindow);

            if (windowBounds.intersects(aRect) ||
                aRect.intersects(windowBounds)) {
                if (rectVector == null) {
                    rectVector = VectorCache.newVector();
                }
                rectVector.addElement(new Rect(windowBounds));
            }
        }

        return rectVector;
    }

    void setRedrawTransparentWindows(boolean flag) {
        _redrawTransWindows = flag;
    }

    /** @private
      */
    public void redrawTransparentWindows(Rect clipRect,
                                         InternalWindow aboveWindow) {
        redrawTransparentWindows(null, clipRect, aboveWindow);
    }

    /** @private
      */
    public void redrawTransparentWindows(Graphics g,
                                         Rect clipRect,
                                         InternalWindow aboveWindow) {
        InternalWindow  nextWindow;
        int             i, count;
        Rect            windowRect = null;

        if (!_redrawTransWindows) {
            return;
        }
/// ALERT!
/// THIS WAS PUT IN TO HANDLE THE CASE IN CONSTRUCTOR WHERE
/// BORDERED POPUP WAS SETTING THE BORDER IN IT'S DRAW METHOD WHICH FORCED A
/// DRAW() TO GET CALLED AND WE INFINITE RECURSED.
/// BUT THIS FIXED CAUSED TRANSPARENT INTERNAL WINDOWS NOT TO DRAW PROPERLY WHEN
/// THINGS BEHIND THEM MOVED.
//        _redrawTransWindows = false;
        count = windows.count();
        if (aboveWindow != null) {
            i = windows.indexOf(aboveWindow) + 1;
        } else {
            i = 0;
        }
        for (; i < count; i++) {
            /// ALERT FIX 5
            Rect windowBounds;
            nextWindow = (InternalWindow)windows.elementAt(i);

            if (!nextWindow.isTransparent()) {
                continue;
            }

            windowBounds = absoluteWindowBounds(nextWindow);
            if (windowBounds.intersects(clipRect)) {

                if (windowRect == null)
                    windowRect = Rect.newRect();

                convertRectToView(nextWindow, clipRect, windowRect);

                nextWindow.draw(g, windowRect);
            }
        }
        if (windowRect != null) {
            Rect.returnRect(windowRect);
        }
//        _redrawTransWindows = true;
    }

     void paint(ApplicationEvent updateEvent) {
        UpdateFilter updateFilter = new UpdateFilter(updateEvent.rect());
        int i;

        updateFilter.rootView = this;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        application.eventLoop().filterEvents(updateFilter);

        if (redrawAll) {
            // We are redrawing the entire RootView because on Windows
            // there is a bug which causes the wrong clipRect to be given.
            Rect clipRect = Rect.newRect(0, 0, bounds.width, bounds.height);

            redraw(clipRect);
            redrawTransparentWindows(clipRect, null);
            Rect.returnRect(clipRect);
        } else {
            redraw(updateFilter._rect);
            redrawTransparentWindows(updateFilter._rect, null);
        }

        /* jla - this was needed to get repaint events to flush properly in
         * navigator on solaris
         */
        AWTCompatibility.awtToolkit().sync();
    }

    void print(ApplicationEvent printEvent) {
        Rect rect = new Rect(0, 0, width(), height());
        Graphics ifcGraphics = new Graphics(rect, printEvent.graphics());

        redraw(ifcGraphics, rect);
        redrawTransparentWindows(ifcGraphics, rect, null);
    }

    void resize(ApplicationEvent resizeEvent) {
        ResizeFilter resizeFilter = new ResizeFilter();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        resizeFilter.lastEvent = resizeEvent;
        application.eventLoop().filterEvents(resizeFilter);

        sizeTo(resizeFilter.lastEvent.rect().width,
               resizeFilter.lastEvent.rect().height);
    }

/* events */

    /** If called after a MOUSE_DOWN or MOUSE_DRAGGED Event, forces all
      * subsequent MOUSE_DRAGGED Events and the MOUSE_UP Event to go to
      * <b>aView</b>.
      */
    public void setMouseView(View aView) {
        _mouseView = aView;
    }

    /** Returns the View currently receiving MOUSE_DRAGGED or MOUSE_UP Events.
      * @see #setMouseView
      */
    public View mouseView() {
        return _mouseView;
    }


    /** Returns the View under point (<b>x</b>, <b>y</b>).
     */
    public View viewForMouse(int x, int y) {
        InternalWindow  nextWindow;
        View    theView = null;
        int     i;

        i = windows.count();
        while (i-- > 0 && theView == null) {
            // ALERT FIX 2
            Rect windowBounds;
            nextWindow = (InternalWindow)windows.elementAt(i);
            windowBounds = absoluteWindowBounds(nextWindow);
            theView = nextWindow.viewForMouse(x - windowBounds.x,
                                              y - windowBounds.y);
        }

        if (theView != null)
            return theView;
        else
            return super.viewForMouse(x, y);
    }

    void _mouseDown(MouseEvent event) {
        InternalWindow  mouseWindow;
        View            newMouseView, origMouseView, modalView;
        long            currentTime;

        mouseDownCount++;

        /* if one mouse button is already down, filter out additional downs */
        if (mouseDownCount > 1) {
            return;
        }

        /* who got clicked */
        newMouseView = viewForMouse(event.x, event.y);
        if (newMouseView == null) {
            _mouseView = null;
            return;
        }

        /* have to compare new view to current to determine if there's really
         * a double-click
         */
        // click count is now kept by the awt.  ALERT!
        currentTime = event.timeStamp;
        if (_mouseClickView == newMouseView &&
            (currentTime - _lastClickTime < 250)) {
            _clickCount++;
        } else {
            _clickCount = 1;
        }
        _lastClickTime = currentTime;
        event.setClickCount(_clickCount);

        /* we set this even if we're in a modal loop because subsequent mouse
         * moved events will be meant for the clicked view;  if it isn't the
         * same as the modal view or descendant, we'll just filter it at
         * that point
         */
        _mouseView = _mouseClickView = newMouseView;

        if (viewExcludedFromModalSession(newMouseView))
            return;

        /* set the _mouseView's window's active view */
        if (!(_mouseView instanceof InternalWindow)) {
            mouseWindow = _mouseView.window();
            if (!(_mouseView instanceof MenuView) || (mouseWindow != null)) {
                _setMainWindow(mouseWindow);
            }
        }

        /* pass the event along */
        event.x -= _mouseView.absoluteX();
        event.y -= _mouseView.absoluteY();

        /* inside of mouse down, the program might set the mouse view to be
         * some other view;  if mouseDown() returns false and we don't check
         * for this, the new mouse view will not receive more mouse events
         */
        origMouseView = _mouseView;

        if (!_mouseView.mouseDown(event) && (origMouseView == _mouseView)) {
            _mouseView = null;
        }
    }

    /** Returns <b>true</b> if the RootView is visible. */
    public boolean isVisible() {
        return isVisible;
    }

    void setVisible(boolean flag) {
        if (isVisible != flag) {
            isVisible = flag;
            if (isVisible) {
                ancestorWasAddedToViewHierarchy(this);
                if (_focusedView != null) {
                    _focusedView._startFocus();
                    Application.application().focusChanged(_focusedView);
                }
            } else {
                ancestorWillRemoveFromViewHierarchy(this);
                if (_focusedView != null) {
                    _focusedView._pauseFocus();
                }
            }
        }
    }

    void _mouseDrag(MouseEvent event) {
        Autoscroller    autoscroller;
        boolean         pointIsVisible;

        /* modal loop? */
        if(viewExcludedFromModalSession(_mouseView))
            return;

        if (_mouseView != null) {
            pointIsVisible =
                _mouseView.containsPointInVisibleRect(event.x, event.y);

            if (_mouseView.wantsAutoscrollEvents() && !pointIsVisible) {
                if (_autoscrollTimer == null) {
                    autoscroller = new Autoscroller();

                    _autoscrollTimer = new Timer(autoscroller, "autoscroll",
                                                 100);
                    _autoscrollTimer.start();
                    _autoscrollTimer.setData(_mouseView);
                    autoscroller.setEvent(
                                _mouseView.convertEventToView(null, event));
                } else {
                    autoscroller = (Autoscroller)_autoscrollTimer.target();
                    autoscroller.setEvent(
                                _mouseView.convertEventToView(null, event));
                }
            } else {
                if (_autoscrollTimer != null) {
                    _autoscrollTimer.stop();
                    _autoscrollTimer = null;
                }
                event.setClickCount(_clickCount);

                _mouseView.mouseDragged(event);
            }
        } else if (_autoscrollTimer != null) {
            _autoscrollTimer.stop();
            _autoscrollTimer = null;
        }
    }

    void _mouseUp(MouseEvent event) {
        View newMoveView;

        mouseDownCount--;

        /* if mouse still down (multiple mouse buttons) filter out the up */
        if (mouseDownCount > 0) {
            return;
        }

        /* modal loop? */
        if(viewExcludedFromModalSession(_mouseView)) {
            return;
        }

        if (_mouseView != null) {
            event.setClickCount(_clickCount);
            _mouseView.mouseUp(event);

            if (_autoscrollTimer != null) {
                _autoscrollTimer.stop();
                _autoscrollTimer = null;
            }
        }
        /* if released outside of move view, need to generate
         * a mouse move event
         */
        newMoveView = viewForMouse(_mouseX, _mouseY);
        if (newMoveView != _moveView) {
            createMouseEnter();
        }
        _mouseClickView = _mouseView;
        _mouseView = null;
    }

    void createMouseEnter() {
        MouseEvent        fakeMouseEvent;

        fakeMouseEvent = new MouseEvent(System.currentTimeMillis(),
                                        MouseEvent.MOUSE_ENTERED, _mouseX,
                                        _mouseY, 0);
        _mouseEnter(fakeMouseEvent);
    }

    void createMouseEnterLater() {
        if (_mouseView == null) {
            recomputeMoveView = true;
        }
    }

    void _mouseEnter(MouseEvent event) {
        _mouseMove(event);
    }

    void _mouseMove(MouseEvent event) {
        View            newMoveView, oldMoveView;

        newMoveView = viewForMouse(event.x, event.y);

        /** When we have a modal view, we should send mouse moved events
         *  only to view that are descendant from the modal view
         */
        if(viewExcludedFromModalSession(newMoveView))
            return;

        if (newMoveView == _moveView) {
            MouseEvent cEvent;
            if (_moveView == null) {
                return;
            }

            cEvent = convertEventToView(_moveView,event);

            _moveView.mouseMoved(cEvent);
        } else {
            oldMoveView = _moveView;
            _moveView = newMoveView;
            if (oldMoveView != null) {
                MouseEvent e = convertEventToView(oldMoveView, event);
                e.setType(MouseEvent.MOUSE_EXITED);
                oldMoveView.mouseExited(e);
            }

            if (_moveView != null) {
                MouseEvent e = convertEventToView(_moveView, event);
                e.setType(MouseEvent.MOUSE_ENTERED);
                _moveView.mouseEntered(e);
            }
        }

        updateCursorLater();
    }

    void _mouseExit(MouseEvent event) {
        if (_moveView != null) {
            _moveView.mouseExited(convertEventToView(_moveView, event));
            _moveView = null;

            updateCursorLater();
        }
    }



/* cursors */


    void flushCursor() {
        int        cursorToSet;

        if (_overrideCursor != DEFAULT_CURSOR) {
            cursorToSet = _overrideCursor;
        } else {
            cursorToSet = _viewCursor;
        }

        if (cursorToSet != _currentCursor) {
            panel.setCursor(cursorToSet);
            _currentCursor = cursorToSet;
        }
    }

    void computeCursor() {
        View        underView;
        Point        mousePoint;

        underView = viewForMouse(_mouseX, _mouseY);
        if (underView != null) {
            mousePoint = Point.newPoint();
            convertToView(underView, _mouseX, _mouseY, mousePoint);

            _viewCursor = underView.cursorForPoint(mousePoint.x, mousePoint.y);

            Point.returnPoint(mousePoint);
        } else {
            _viewCursor = ARROW_CURSOR;
        }

        flushCursor();
    }

    /** Returns the current cursor.
      * @see View#cursorForPoint
      * @see #setOverrideCursor
      */
    public int cursor() {
        return _currentCursor;
    }

    /** Forces the RootView's currently displayed cursor to the cursor
      * identified by <b>cursorIdent</b>, regardless of the cursor requested by
      * the View currently under the mouse.  To return the cursor to what it
      * should be, call <b>removeOverrideCursor()</b>.<p>
      * You will almost never call this method - instead, your View subclasses
      * will override their <b>cursorForPoint()</b> method to return the
      * correct cursor for that point within the View.
      * @see View#cursorForPoint
      * @see #removeOverrideCursor
      */
     public void setOverrideCursor(int cursorIdent) {
        if (cursorIdent < DEFAULT_CURSOR || cursorIdent > MOVE_CURSOR) {
            throw new InconsistencyException("Unknown cursor type: " +
                                             cursorIdent);
        }

        if (_overrideCursor != cursorIdent) {
            _overrideCursor = cursorIdent;
            flushCursor();
        }
    }

    /** Removes the override cursor, returning the cursor to the cursor
      * requested by the View currently under the mouse.
      * @see #setOverrideCursor
      */
    public void removeOverrideCursor() {
        setOverrideCursor(DEFAULT_CURSOR);
    }

    /** Forces the RootView to immediately compute and set the cursor
      * based on the View under the mouse.
      */
    public void updateCursor() {
        computeCursor();
    }

    /** Forces the RootView to recompute the current cursor upon
      * reaching the top of the EventLoop.
      */
    public void updateCursorLater() {
        recomputeCursor = true;
    }

    void _updateCursorAndMoveView() {
        if (recomputeMoveView) {
            createMouseEnter();
            recomputeMoveView = false;
            recomputeCursor = false;
        } else if (recomputeCursor) {
            computeCursor();
            recomputeCursor = false;
        }
    }



/* mouse events */


    void _keyDown(KeyEvent event) {
        View            keyView;
        ExternalWindow  externalWindow;
        boolean         eventHandled = false;
        MenuItem        item = null;

        keyView = _focusedView;
        externalWindow = externalWindow();

        // ALERT.  This is only for AWT Menus.
        if (externalWindow != null) {
            if (externalWindow.menu() != null) {
                if (JDK11AirLock.menuShortcutExists()) {
                    // ALERT.  JDK 1.1.1 native Menus with MenuShortcuts
                    // perform the associated MenuItem's command,
                    // and also call keyDown after, so we need to make sure
                    // the event is not handled twice.  We check to see
                    // if one of the MenuItems would have presumably
                    // already handled the Event by seeing if any MenuItems
                    // have the corresponding command key-equivalent.
                    item = externalWindow.menu().itemForKeyEvent(event);
                    if (item != null) {
                        eventHandled = true;
                    }
                } else {
                    eventHandled = externalWindow.menu().handleCommandKeyEvent(event);
                }
            }
        }
        if (!eventHandled) {
            if(processKeyboardEvent(event,true))
                return;
            if (keyView != null && !viewExcludedFromModalSession(keyView)) {
                keyView.keyDown(event);
            } else {
                application().keyDown(event);
            }
        }
    }

    void _keyUp(KeyEvent event) {
        View    keyView;

        keyView = _focusedView;

        if (keyView != null && !viewExcludedFromModalSession(keyView)) {
            keyView.keyUp(event);
        } else {
            application().keyUp(event);
        }
    }

/* panels */

    /** Makes the ColorChooser visible.
      * @see ColorChooser
      */
    public void showColorChooser() {
        colorChooser().show();
    }

    /** Returns a reference to the shared ColorChooser.
      * @see ColorChooser
      */
    public ColorChooser colorChooser() {
        if (colorChooser == null) {
            InternalWindow window = new InternalWindow(0, 0, 10, 10);

            window.setRootView(this);
            colorChooser = new ColorChooser();
            colorChooser.setWindow(window);
            window.center();
        }
        return colorChooser;
    }

    /** Makes the FontChooser visible.
     * @see FontChooser
     */
    public void showFontChooser() {
        fontChooser().show();
    }

    /** Returns a reference to the shared FontChooser.
      * @see FontChooser
      */
    public FontChooser fontChooser() {
        if (fontChooser == null) {
            InternalWindow fontWindow = new InternalWindow(0, 0, 1, 1);

            fontWindow.setRootView(this);
            fontChooser = new FontChooser();
            fontChooser.setWindow(fontWindow);
            fontWindow.center();
        }

        return fontChooser;
    }

    /** Returns the RootView's ExternalWindow, if any.
      */
    public ExternalWindow externalWindow() {
        java.awt.Component component;

        for (component = panel;
             component != null;
             component = component.getParent()) {
            if (component instanceof FoundationFrame)
                return ((FoundationFrame) component).externalWindow;
            else if(component instanceof FoundationDialog)
                return ((FoundationDialog) component).externalWindow;
            else if(component instanceof FoundationWindow)
                return ((FoundationWindow) component).externalWindow;
        }
        return null;
    }

  /* running */

    MouseEvent removeMouseEvents(MouseEvent mouseEvent) {
        MouseEvent newEvent;

        newEvent = (MouseEvent)application.eventLoop().filterEvents(mouseFilter);

        return newEvent != null ? newEvent : mouseEvent;
    }

    void _convertMouseEventToMouseView(MouseEvent mouseEvent) {
        Point        mousePoint;

        if (_mouseView == null || mouseEvent == null) {
            return;
        }

        mousePoint = Point.newPoint();
        convertToView(_mouseView, mouseEvent.x, mouseEvent.y, mousePoint);

        mouseEvent.x = mousePoint.x;
        mouseEvent.y = mousePoint.y;

        Point.returnPoint(mousePoint);
    }

    /** Processes the various Events directed at the RootView.
      * You should never call this method.
      */
    public void processEvent(Event event) {
        MouseEvent      mouseEvent;
        int             type;

        /* If someone closes a window by calling dispose(), we may
         * have some event pending for a rootView that's no longer connected
         * to the application. When the rootView is removed, Application
         * call setApplet(null). This sets the application ivar to null.
         * if application is null, we should just ignore the event.
         */
        if (application == null)
            return;

        if (event instanceof MouseEvent) {
            mouseEvent = (MouseEvent)event;
            type = mouseEvent.type;

            if (type == MouseEvent.MOUSE_MOVED && mouseDownCount > 0 &&
                _mouseView != null) {
                /* something has gone wrong - we got a move with the
                 * mouse still down.  Pretend we got a mouse up at the
                 * last known point.
                 */
                MouseEvent e = convertEventToView(_mouseView, mouseEvent);
                e.setType(MouseEvent.MOUSE_UP);
                _mouseUp(e);
            }

            if ((type == MouseEvent.MOUSE_DRAGGED) && _mouseView != null &&
                 _mouseView.wantsMouseEventCoalescing()) {

                mouseEvent = removeMouseEvents(mouseEvent);
                type = mouseEvent.type();
            } else if ((type == MouseEvent.MOUSE_MOVED) && _moveView != null &&
                 _moveView.wantsMouseEventCoalescing()) {

                mouseEvent = removeMouseEvents(mouseEvent);
                type = mouseEvent.type();
            }

            _mouseX = mouseEvent.x;
            _mouseY = mouseEvent.y;

            switch (type) {
                case MouseEvent.MOUSE_DOWN:
                    /** Sometimes the awt forgets to tell us that
                     *  we are getting the focus. This is why we
                     *  need this hack
                     */
                    if(application.firstRootView() != this)
                        application.makeFirstRootView(this);
                    _mouseDown(mouseEvent);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    _convertMouseEventToMouseView(mouseEvent);
                    _mouseDrag(mouseEvent);
                    break;
                case MouseEvent.MOUSE_UP:
                    _convertMouseEventToMouseView(mouseEvent);
                    _mouseUp(mouseEvent);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    if (_mouseView == null) {
                        _mouseEnter(mouseEvent);
                    }
                    break;
                case MouseEvent.MOUSE_MOVED:
                    _mouseMove(mouseEvent);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    if (_mouseView == null) {
                        _mouseExit(mouseEvent);
                    }
                    break;
            }
        } else if (event instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent) event;
            /** Sometimes the awt forgets to tell us that
             *  we are getting the focus. This is why we
             *  need this hack
             */
            if(application.firstRootView() != this)
                application.makeFirstRootView(this);
            if (keyEvent.type == KeyEvent.KEY_DOWN)
                _keyDown(keyEvent);
            else
                _keyUp(keyEvent);
        } else if (event instanceof ApplicationEvent) {
            ExternalWindow externalWindow = null;

            switch (event.type) {
                case ApplicationEvent.GOT_FOCUS:
                    application.makeFirstRootView(this);
                    if (externalWindow != null) {
                        externalWindow.didBecomeMain();
                    }
                    if (_focusedView != null) {
                        _focusedView.resumeFocus();
                    }
                    break;
                case ApplicationEvent.LOST_FOCUS:
                    externalWindow = externalWindow();
                    if (externalWindow != null) {
                        externalWindow.didResignMain();
                    }
                    if (_mainWindow != null) {
                        _setMainWindow(null);
                    }
                    if (_focusedView != null) {
                        _focusedView._pauseFocus();
                    }
                    break;
                case ApplicationEvent.UPDATE:
                    //ALERT! Hack for the null graphics
                    if (isVisible) {
                        paint((ApplicationEvent)event);
                    }
                    break;

                case ApplicationEvent.RESIZE:
                    /// ALERT fix for JDK 1.1.1 bug.
                    /// RESHAPE events are not followed by paint() events in 1.1.1.
                    /// Adding an extra UPDATE event to the EventLoop.
                    /// This only happens when the reshape is making the panel
                    /// smaller, going larger forces expose events that make us draw
                    /// properly. We will only add an event if we are being resized
                    /// smaller than our current size. This may cause an additional
                    /// paint to be called under 1.0.2, but the paint coalescing
                    /// should do it's job.
                    if(!bounds().equals(((ApplicationEvent)event).rect())
                        && bounds().contains(((ApplicationEvent)event).rect())) {
                        ApplicationEvent updateEvent = new ApplicationEvent();
                        updateEvent.data = ((ApplicationEvent)event).rect();
                        updateEvent.type = ApplicationEvent.UPDATE;
                        updateEvent.setProcessor(this);
                        if(application() != null && application().eventLoop() != null)
                            application().eventLoop().addEvent(updateEvent);
                    }

                    resize((ApplicationEvent)event);
                    /* There is no way to receive a notification when
                     * an external window get resized. This call to
                     * validateBounds() is a hack to give the external window
                     * a chance to call windowWillSizeBy() and to
                     * update its bounds
                     */
                    externalWindow = externalWindow();
                    if (externalWindow != null) {
                        externalWindow.validateBounds();
                    }
                    break;
                case ApplicationEvent.APPLET_STARTED:
                    break;
                case ApplicationEvent.PRINT:
                    print((ApplicationEvent)event);
                    break;
                default:
            }
        }
    }

    void setFocusedView(View view, boolean hard) {
        View wasFocusedView;

        if (view != _focusedView) {
            if (_focusedView != null) {
                wasFocusedView = _focusedView;
                _focusedView = null;
                if (hard) {
                    wasFocusedView._stopFocus();
                } else {
                    wasFocusedView._pauseFocus();
                }
            } else
                _focusedView = null;

            if(_focusedView == null) {
                _focusedView = view;
                if (_focusedView != null) {
                    _focusedView._startFocus();
                }
            }
            Application.application().focusChanged(_focusedView);
            Application.application().performCommandLater(this,
                                                          VALIDATE_SELECTED_VIEW,
                                                          null,true);
        }
    }

    /** Directs all key Events to <b>view</b>.
      */
    public void setFocusedView(View view) {
        setFocusedView(view, true);
    }

    /** Returns the View set to receive all key Events.
      * @see #setFocusedView
      */
    public View focusedView() {
        return _focusedView;
    }

    /** Implements the RootView's commands:
      * <ul>
      * <li>ExtendedTarget.SHOW_FONT_CHOOSER - makes the FontChooser visible.
      * <li>ExtendedTarget.SHOW_COLOR_CHOOSER - makes the ColorChooser visible.
      * <li>ExtendedTarget.NEW_FONT_SELECTION - causes the FontChooser to
      *                                         display the Font that is passed
      *                                         as the object.
      * </ul>
      */
    public void performCommand(String command, Object data) {
        if (SHOW_FONT_CHOOSER.equals(command)) {
            showFontChooser();
        } else if (SHOW_COLOR_CHOOSER.equals(command)) {
            showColorChooser();
        } else if (NEW_FONT_SELECTION.equals(command)) {
            if (fontChooser != null) {
                fontChooser.setFont((Font)data);
            }
        } else if(VALIDATE_SELECTED_VIEW.equals(command)) {
            validateSelectedView();
        } else {
            throw new NoSuchMethodError("unknown command: " + command);
        }
    }

    /** Returns <b>true</b> for the commands that the RootView can perform.
      * @see #performCommand
      */
    public boolean canPerformCommand(String command) {
       return _commands.contains(command);
    }

    /** Sets the RootView's Color.
      */
    public void setColor(Color aColor) {
        _backgroundColor = aColor;
    }

    /** Returns the RootView's Color.
      * @see #setColor
      */
    public Color color() {
        return _backgroundColor;
    }

    /** Sets the RootView's Image.
      * @see #setImageDisplayStyle
      */
    public void setImage(Image anImage) {
        _image = anImage;
    }

    /** Returns the RootView's Image.
      * @see #setImage
      */
    public Image image() {
        return _image;
    }

    /** Sets the style the RootView uses to display its Image
      * (Image.CENTERED, Image.TILED, or Image.SCALED).
      */
    public void setImageDisplayStyle(int aStyle) {
        if (aStyle != Image.CENTERED && aStyle != Image.TILED &&
            aStyle != Image.SCALED) {
            throw new InconsistencyException("Unknown image display style: " +
                aStyle);
        }

        _imageDisplayStyle = aStyle;
    }

    /** Returns the style the RootView uses to display its Image.
      * @see #setImageDisplayStyle
      */
    public int imageDisplayStyle() {
        return _imageDisplayStyle;
    }

    /** Returns <b>false</b> - RootViews are not transparent.
      */
    public boolean isTransparent() {
        return false;
    }

    /** Draws the RootView's contents.
      */
    public void drawView(Graphics g) {
        if (_image == null || (_imageDisplayStyle == Image.CENTERED &&
                            (_image.width() < bounds.width ||
                             _image.height() < bounds.height))) {
            if (_backgroundColor != null) {
                g.setColor(_backgroundColor);
                g.fillRect(g.clipRect());
            }
        }

        if (_image != null) {
            _image.drawWithStyle(g, 0, 0,
                                 bounds.width, bounds.height,
                                 _imageDisplayStyle);
        }
    }

    /** Overridden to draw just the <b>aRect</b> portion of the RootView,
      * ignoring any Windows that may intersect the Rect.  To draw everything
      * within a given Rect, call <b>redraw()</b>.
      * @see #redraw
      */
    public void draw(Graphics g, Rect aRect) {
        InternalWindow  nextWindow;
        Vector          enableVector;
        int             i;

        enableVector = new Vector();

        i = windows.count();
        while (i-- > 0) {
            nextWindow = (InternalWindow)windows.elementAt(i);
            if (nextWindow.isDrawingEnabled()) {
                nextWindow.disableDrawing();
                enableVector.addElement(nextWindow);
            }
        }

        super.draw(g, aRect);

        i = enableVector.count();
        while (i-- > 0) {
            nextWindow = (InternalWindow)enableVector.elementAt(i);
            nextWindow.reenableDrawing();
        }
    }

    // opaqueView is dead.  ALERT!

    View viewWithBuffer(View someView, Rect updateRect) {
        View    nextView, bufferView, opaqueView = null;
        int     i;
        Rect    subRect;
        Vector  views;

        i = someView.subviewCount();
        if (i == 0)
            return null;

        views = someView.subviews();
        subRect = Rect.newRect(0, 0, updateRect.width, updateRect.height);

        while (i-- > 0) {
            nextView = (View)views.elementAt(i);

            if (nextView instanceof InternalWindow ||
                !nextView.bounds.contains(updateRect)) {
                continue;
            }

            if (nextView.isBuffered()) {
                Rect.returnRect(subRect);
                return nextView;
            }

            if (!nextView.isTransparent()) {
//              opaqueView = nextView;
            }

            subRect.x = updateRect.x - nextView.bounds.x;
            subRect.y = updateRect.y - nextView.bounds.y;
            bufferView = viewWithBuffer(nextView, subRect);
            if (bufferView != null) {
                Rect.returnRect(subRect);
                return bufferView;
            }
            if (opaqueView != null) {
                Rect.returnRect(subRect);
                return opaqueView;
            }
        }

        Rect.returnRect(subRect);

        return null;
    }

    /** Similar to <b>draw()</b>, except that it draws everything intersecting
      * the Rect <b>aRect</b> (RootView and InternalWindows).
      */
    void redraw(Graphics g, Rect aRect) {
        InternalWindow  nextWindow;
        View            bufferView, viewForRect;
        Vector          intersectedWindows;
        int             i, count;
        boolean         foundWindow;
        Rect            subRect;

        subRect = Rect.newRect(0, 0, aRect.width, aRect.height);

        if (aRect == null) {
            aRect = new Rect(0, 0, bounds.width, bounds.height);
        }

        intersectedWindows = VectorCache.newVector();

        setRedrawTransparentWindows(false);

        /* which windows intersect aRect, and is there one that contains it? */
        count = windows.count();
        foundWindow = false;
        for (i = count; (i-- > 0) && !foundWindow; ) {
            /// ALERT FIX 1
            Rect windowBounds;
            nextWindow = (InternalWindow)windows.elementAt(i);
            windowBounds = absoluteWindowBounds(nextWindow);
            if (windowBounds.intersects(aRect)) {
                intersectedWindows.addElement(nextWindow);
                if (!nextWindow.isTransparent() &&
                    windowBounds.contains(aRect)) {
                    foundWindow = true;
                }
            }
        }

        /* no window completely contains aRect, so find an opaque view that
         * does
         */
        if (!foundWindow) {
            viewForRect = _viewForRect(aRect, null);

            if (viewForRect != null) {
                convertRectToView(viewForRect, aRect, subRect);
                g.pushState();
                g.translate(aRect.x - subRect.x, aRect.y - subRect.y);
                viewForRect.draw(g, subRect);
                g.popState();
            } else {
                draw(g, aRect);
            }
        }

        count = intersectedWindows.count();
        for (i = 0; i < count; i++) {
            View windowSuperview;

            nextWindow = (InternalWindow)intersectedWindows.elementAt(i);

            windowSuperview = nextWindow.superview();
            convertRectToView(windowSuperview, aRect, subRect);
            viewForRect = nextWindow._viewForRect(subRect, windowSuperview);
            if (viewForRect == null) {
                viewForRect = nextWindow;
            }
            convertRectToView(viewForRect, aRect, subRect);

            g.pushState();
            g.translate(aRect.x - subRect.x, aRect.y - subRect.y);
            viewForRect.draw(g, subRect);
            g.popState();
        }

        setRedrawTransparentWindows(true);
        Rect.returnRect(subRect);
        VectorCache.returnVector(intersectedWindows);
    }

    /** Similar to <b>draw()</b>, except that it draws everything intersecting
      * the Rect <b>aRect</b> (RootView and InternalWindows).
      */
    public void redraw(Rect aRect) {
        Graphics graphics = createGraphics();

        redraw(graphics, aRect);
        graphics.dispose();
    }

    /** Convenience method for drawing the entire RootView.  Equivalent
      * to the following code:
      * <pre>
      *     redraw(new Graphics(rootView()), null);
      * </pre>
      */
    void redraw() {
        Graphics graphics = createGraphics();
        Rect    tmpRect;

        tmpRect = Rect.newRect(0, 0, bounds.width, bounds.height);
        redraw(graphics, tmpRect);
        Rect.returnRect(tmpRect);

        graphics.dispose();
    }

    /** View calls this method when it goes from not dirty to dirty.
      */
    synchronized void markDirty(View dirtyView) {
        // Add the view to the list of views of dirtyViews.  If we are
        // currently processing dirtyViews then dirtyViews will be set to
        // null.  Nobody should mark themselves dirty while drawing.

        if (dirtyViews != null) {
            dirtyViews.addElement(dirtyView);
        } else {
            throw new InconsistencyException("Don't dirty a View while the list of dirty views is being drawn!");
        }
    }

    /** View calls this method when it goes from dirty to not dirty.
      */
    synchronized void markClean(View dirtyView) {
        // If a view becomes clean, remove it from the dirtyViews Vector.  If
        // dirtyViews is null then we are in the process of cleaning the
        // dirtyViews so we can just ignore the request since the vector is
        // about to be emptied anyway.

        if (dirtyViews != null)
            dirtyViews.removeElement(dirtyView);
    }

    /** Marks all of the RootView's dirty subviews as clean, without
      * drawing them.
      */
    public synchronized void resetDirtyViews() {
        int i, count;
        View dirtyView;
        Vector tmpDirtyViews;

        tmpDirtyViews = dirtyViews;
        dirtyViews = null;

        count = tmpDirtyViews.count();
        for (i = 0; i < count; i++) {
            dirtyView = (View)tmpDirtyViews.elementAt(i);
            dirtyView.setDirty(false);
        }

        tmpDirtyViews.removeAllElements();
        dirtyViews = tmpDirtyViews;
    }

    /** Draws all of the RootView's dirty subviews. This method
      * is called automatically after the EventLoop processes an Event.
      */
    public synchronized void drawDirtyViews() {
        int i, count;
        Rect tmp;
        Vector roots, tmpDirtyViews;
        View dirtyView;

        count = dirtyViews.count();
        if (count == 0)
            return;

        // While we are processing the vector of dirty views, move it aside
        // so that it can't be mucked with by errant views.  The method
        // markDirty() will throw an exception if someone tries to mark a view
        // dirty while we are processing the list.

        tmpDirtyViews = dirtyViews;

        try {
            dirtyViews = null;

            roots = new Vector(count);
            tmp = new Rect();

            for (i = 0; i < count; i++) {
                dirtyView = (View)tmpDirtyViews.elementAt(i);
                collectDirtyViews(dirtyView, roots, tmp);
            }

            count = roots.count();
            for (i = 0; i < count; i++) {
                dirtyView = (View)roots.elementAt(i);
                dirtyView.draw(dirtyView.dirtyRect);
            }
        } finally {
            dirtyViews = tmpDirtyViews;
            resetDirtyViews();
        }
    }

    void collectDirtyViews(View dirtyView, Vector roots, Rect tmp) {
        int dx, dy, rootDx, rootDy;
        View view, rootDirtyView;

        // Find the highest superview which is dirty.  When we get out of this
        // rootDx and rootDy will contain the translation from the
        // rootDirtyView's coordinate system to the coordinates of the
        // original dirtyView.  The tmp Rect is also used to compute the
        // visible portion of the dirtyRect.

        view = rootDirtyView = dirtyView;
        dx = rootDx = 0;
        dy = rootDy = 0;
        tmp.setBounds(0, 0, view.width(), view.height());

        // If the dirtyRect is null, then the whole View is dirty.

        // If this can be done last, it might optimize some special cases.
        // ALERT!
        if (dirtyView.dirtyRect != null)
            tmp.intersectWith(dirtyView.dirtyRect);

        if (tmp.isEmpty())
            return;

        do {
            dx += view.bounds.x;
            dy += view.bounds.y;
            tmp.moveBy(view.bounds.x, view.bounds.y);

            view = view.superview();

            if (view != null) {
                tmp.intersectWith(0, 0, view.width(), view.height());
                if (tmp.isEmpty())
                    return;

                if (view.isDirty()) {
                    rootDirtyView = view;
                    rootDx = dx;
                    rootDy = dy;
                }
            }
        } while (view != null && !(view instanceof InternalWindow));

        // At this point the visible portion of the dirtyRect is in the
        // RootView's coordinate system.  Put it in the coordinate
        // system of the rootDirtyView.  Also add the dirtyRect of the
        // dirtyView to the rootDirtyView.

        if (dirtyView != rootDirtyView) {
            tmp.moveBy(rootDx - dx, rootDy - dy);
            rootDirtyView.addDirtyRect(tmp);
        }

        // If we haven't seen this root before, then we need to add it to the
        // list of root dirty Views.

        if (!roots.containsIdentical(rootDirtyView))
            roots.addElement(rootDirtyView);
    }

    /** Overridden to prevent mouse Events from being sent to the
      * RootView. Returns <b>false</b>.
      */
    public boolean mouseDown(MouseEvent event) {
        return false;
    }

    /** Overridden to return this View.
      */
    public RootView rootView() {
        if (panel == null)
            return super.rootView();
        else
            return this;
    }

    void setPanel(FoundationPanel p) {
        panel = p;
    }

    /** Returns the FoundationPanel the RootView is being displayed in. */
    public FoundationPanel panel() {
        return panel;
    }

    /** Returns the Application that owns the RootView.
      */
    Application application() {
        return application;
    }

    void setApplication(Application a) {
        application = a;
    }

    /** @private
      */
    public void setWindowClipView(View aView) {
        _windowClipView = aView;
    }

    /** @private
      */
    public View windowClipView() {
        return _windowClipView;
    }

    void addComponentView(AWTComponentView componentView) {
        if (componentViews == null) {
            componentViews = new Vector();
        }

        componentViews.addElement(componentView);
        componentView.setComponentBounds();
        panel.add(componentView.component);
    }

    void removeComponentView(AWTComponentView componentView) {
        if (componentViews == null) {
            componentViews = new Vector();
        }

        componentViews.removeElement(componentView);
        panel.remove(componentView.component);
    }

    private final void subviewDidResizeOrMove(View aView) {
        AWTComponentView        view;
        int                     count, i;

        if (componentViews != null) {
            count = componentViews.count();

            for (i = 0; i < count; i++) {
               view = (AWTComponentView)componentViews.elementAt(i);

                if (view.descendsFrom(aView)) {
                    view.setComponentBounds();
                }
            }
        }

        createMouseEnterLater();
        if(application!=null) {
            KeyboardArrow arrow = application.keyboardArrow();
            View v = arrow.view();
            if(v != null && v.rootView() == this) {
                updateArrowLocation(arrow);
            }
        }
    }

    /** @private
      */
    public void subviewDidResize(View aView) {
        subviewDidResizeOrMove(aView);
    }

    /** @private
      */
    public void subviewDidMove(View aView) {
        subviewDidResizeOrMove(aView);
    }

    /** @private
      */
    public boolean canDraw() {
        if (panel.getParent() == null)
            return false;
        else
            return isVisible;
    }

    /** Returns a newly-allocated Point containing the mouse's last known
      * location, in the RootView's coordinate system.
      */
    public Point mousePoint() {
        return new Point(_mouseX, _mouseY);
    }


    /** This method determines if <b>aView</b> should not get events
      * while there is a modal session in progress. If this method returns
      * false, then <b>aView</b> is allowed to get events. The current
      * implementation confirms that <b>aView</b> is a subview of the current
      * modal view and returns true. You can override this method if you want
      * to extend the allowed views to include views
      * other than those in the modal view.
      */
    public boolean viewExcludedFromModalSession(View aView ) {
        View modalView;

        if(aView == null)
            return true;

        modalView = Application.application().modalView();
        if( modalView != null && !aView.descendsFrom(modalView)) {
            if(aView instanceof DragView || aView instanceof InternalWindow)
                return false;
            else
                return true;
        } else
            return false;
    }

    /** @private */
    public void setRedrawAll(boolean flag) {
        redrawAll = flag;
    }

    /** @private */
    public boolean redrawAll() {
        return redrawAll;
    }

    Rect absoluteWindowBounds(InternalWindow window)    {
        if(window.superview() != this)
            return window.superview().convertRectToView(this, window.bounds);
        return window.bounds;
    }

    void viewHierarchyChanged(){
        if(_focusedView != null && !_focusedView.descendsFrom(this))
            setFocusedView(null);

        Application.application().performCommandLater(this,VALIDATE_SELECTED_VIEW,null,true);
    }

    private boolean isInWindow(View aView) {
      View superview;

      if(aView == null)
          return false;
      else if(aView instanceof InternalWindow)
          return true;
      else {
          superview = aView.superview();
          do {
              if(superview == this)
                  return false;
              else if(superview instanceof InternalWindow)
                  return true;
              superview = superview.superview();
          } while(superview != null);
      }
      return false;
    }

    /** Return true if the root view as received a mouse down and
     *  is waiting for a mouse up
     * @private
     */
    public boolean mouseStillDown() {
        if(mouseDownCount > 0)
            return true;
        else
            return false;
    }

    View rootViewFocusedView() {
        if(_rootViewFocusedView != null) {
            if(_rootViewFocusedView.descendsFrom(this))
                return _rootViewFocusedView;
            else
                _rootViewFocusedView = null;
        }
        return null;
    }


    /*** Selected view for keyboard UI ****/

    /** Set the default selected view
      *
      */
    public void setDefaultSelectedView(View aView) {
        _defaultSelectedView = aView;
    }

    /** Returns the default selected view
      *
      */
    public View defaultSelectedView() {
        return _defaultSelectedView;
    }

    /** Ask the receiving RootView to make <b>newSelectedView</b>
      * the selected view for keyboard UI. If <b>changeFocus</b>
      * is true, the rootview will abort any editing to select
      * the view. This method does nothing if keyboard UI is not
      * active.
      *
      */
    public void selectView(View newSelectedView,boolean abortCurrentEditing) {
        View kbdRoot = keyboardRootView();

        if((application != null && !application.isKeyboardUIEnabled()) ||
           _selectedView == kbdRoot || _selectedView == null)
            return;

        if(abortCurrentEditing)
            setFocusedView(null);

        if(_focusedView == null &&
           newSelectedView.canBecomeSelectedView() &&
           newSelectedView.descendsFrom(kbdRoot)) {
            View superview = newSelectedView.superview();
            if(superview != kbdRoot) {
                do {
                    if(superview.hidesSubviewsFromKeyboard())
                        newSelectedView = superview;
                    superview = superview.superview();
                } while(superview != kbdRoot);
            }
            makeSelectedView(newSelectedView);
        } else {
            if(kbdRoot instanceof RootView)
                ((RootView)kbdRoot).setDefaultSelectedView(newSelectedView);
            else if(kbdRoot instanceof InternalWindow)
                ((InternalWindow)kbdRoot).setDefaultSelectedView(newSelectedView);
        }
    }

    /** Ask the receiving RootView to make the view following <b>aView</b>
      * to become selected.
      *
      */
    public void selectViewAfter(View aView) {
        View nextSelectedView = null;
        nextSelectedView = findNextView(aView,keyboardRootView(),true);
        makeSelectedView(nextSelectedView);
    }

    /** Ask the receiving RootView to make the view before <b>aView</b>
      * to become selected.
      *
      */
    public void selectViewBefore(View aView) {
        View nextSelectedView = null;
        nextSelectedView = findNextView(aView,keyboardRootView(),false);
        makeSelectedView(nextSelectedView);
    }

    void didBecomeFirstRootView() {
        ExternalWindow externalWindow = externalWindow();

        if (externalWindow != null) {
            externalWindow.didBecomeMain();
        }
        if (_focusedView != null) {
            _focusedView._startFocus();
            Application.application().focusChanged(_focusedView);
        }
        application.performCommandLater(this,VALIDATE_SELECTED_VIEW, null,true);
    }

    void didResignFirstRootView() {
        ExternalWindow externalWindow = externalWindow();


        if (externalWindow != null) {
            externalWindow.didResignMain();
        }
        if (_mainWindow != null) {
            _setMainWindow(null);
        }
        if (_focusedView != null) {
            _focusedView._pauseFocus();
        }
        application.performCommandLater(this,VALIDATE_SELECTED_VIEW, null,true);
    }

    void makeSelectedView(View nextSelectedView) {
        if(application != null && application.isKeyboardUIEnabled()) {
            View keyboardRootView = keyboardRootView();
            if(nextSelectedView != _selectedView) {
                if(_selectedView!=null) {
                    _selectedView.willBecomeUnselected();
                    _selectedView = null;
                }

                _selectedView = nextSelectedView;

                if(_selectedView != null) {
                    if(keyboardRootView instanceof RootView)
                        ((RootView)keyboardRootView).setDefaultSelectedView(_selectedView);
                    else if(keyboardRootView instanceof InternalWindow)
                        ((InternalWindow)keyboardRootView).setDefaultSelectedView(_selectedView);

                    _selectedView.scrollRectToVisible(new Rect(0,0,
                                                               _selectedView.width(),
                                                               _selectedView.height()));
                    _selectedView.willBecomeSelected();
                }
                validateKeyboardArrow();
            }
        }
    }

    void validateKeyboardArrow() {
        if(application != null && application.isKeyboardUIEnabled()) {
            View needArrowView = null;
            if(_selectedView != null && _selectedView.wantsKeyboardArrow())
                needArrowView = _selectedView;
            else if(_focusedView != null && _focusedView.canBecomeSelectedView() &&
                    _focusedView.wantsKeyboardArrow())
                needArrowView = _focusedView;
            else
                needArrowView = null;

            if(needArrowView != null)
                showKeyboardArrowForView(needArrowView);
            else
                hideKeyboardArrow();
        }
    }

    void validateSelectedView() {
        if(application != null && !application.isKeyboardUIEnabled())
            return;

        if(_focusedView != null
            || (application != null && application.firstRootView() != this)) {
            if(_selectedView != null) {
                makeSelectedView(null);
            }
        } else {
            View kbdRoot = keyboardRootView();
            View defaultView;

            if(_selectedView != null && _selectedView.descendsFrom(kbdRoot)) {
                boolean opaqueNodeFound = false;

                /** if selected view is InternalWindow or RootView, it means that kbd UI was not
                 *  previously active. We should select the default selected view of the next
                 *  keyboard root view if kbd root view is different
                 */
                if(!((_selectedView instanceof InternalWindow ||
                      _selectedView instanceof RootView) && _selectedView != kbdRoot)) {
                    if(_selectedView != kbdRoot) {
                        View superview = _selectedView.superview();

                        while(superview != null && superview != kbdRoot) {
                            if(superview.hidesSubviewsFromKeyboard()) {
                                opaqueNodeFound = true;
                                break;
                            }
                            superview = superview.superview();
                        }
                    }
                    if(!opaqueNodeFound)
                        return;
                    else
                        makeSelectedView(null);
                }
            }

            if(kbdRoot instanceof RootView)
                defaultView = ((RootView)kbdRoot).defaultSelectedView();
            else if(kbdRoot instanceof InternalWindow)
                defaultView = ((InternalWindow)kbdRoot).defaultSelectedView();
            else
                defaultView = null;

            if(defaultView != null && defaultView.descendsFrom(kbdRoot)) {
                makeSelectedView(defaultView);
            } else {
                selectNextSelectableView();
            }
        }
        validateKeyboardArrow();
    }

    /** Select the next available view for the keyboard UI **/
    void selectNextSelectableView() {
        View kbdRoot = keyboardRootView();
        View nextSelectedView = null;

        if(_selectedView != null && _selectedView.descendsFrom(kbdRoot))
            nextSelectedView = findNextView(_selectedView,kbdRoot,true);
        else
            nextSelectedView = findNextView(null,kbdRoot,true);

        makeSelectedView(nextSelectedView);
    }

    /** Select the previous available view for the keyboard UI **/
    void selectPreviousSelectableView() {
        View kbdRoot = keyboardRootView();
        View nextSelectedView = null;
        if(_selectedView != null && _selectedView.descendsFrom(kbdRoot))
            nextSelectedView = findNextView(_selectedView,kbdRoot,false);
        else
            nextSelectedView = findNextView(null,kbdRoot,false);
        makeSelectedView(nextSelectedView);
    }

    boolean processKeyboardEvent(KeyEvent anEvent,boolean commandModifierOnly) {
        View nextSelectedView;
        View view;
        View kbdRoot;
        KeyStroke ks;

        if((application != null && application.isKeyboardUIEnabled() == false)
                                  || subviews().count() == 0)
            return false;

        if(commandModifierOnly && !anEvent.isControlKeyDown())
            return false;

        if(!commandModifierOnly && anEvent.isControlKeyDown())
            return false;

        kbdRoot = keyboardRootView();
        validateSelectedView();

        if(!commandModifierOnly) {
            if(anEvent.isTabKey() || anEvent.isBackTabKey()) {
                if(anEvent.isBackTabKey())
                    selectPreviousSelectableView();
                else
                    selectNextSelectableView();
                return true;
            }
        }

        ks = new KeyStroke(anEvent);

        /** Try to send the key stroke to the selected widget **/
        if(_selectedView != null) {
            if(_selectedView.performCommandForKeyStroke(ks,View.WHEN_SELECTED)) {
                return true;
            }

            /** All the views from the selectedView to the keyboard root view **/
            if(_selectedView != kbdRoot) {
                view = _selectedView.superview();
                while(view != kbdRoot) {
                    if(view.performCommandForKeyStroke(ks,View.WHEN_IN_MAIN_WINDOW)) {
                        return true;
                    }
                    view = view.superview();
                }
            }
        }

        /** All the other views from kbdRoot **/
        view = kbdRoot;
        do {
            view = nextView(view,kbdRoot,true,true,false);
            if(view.performCommandForKeyStroke(ks,View.WHEN_IN_MAIN_WINDOW)) {
                return true;
            }
        } while(view != kbdRoot);

        /** All the other views.***/
        view = this;
        do {
            view = nextView(view,this,true,true,true);
            if(view.performCommandForKeyStroke(ks,View.ALWAYS)) {
                return true;
            }
        } while(view != this);

        return false;
    }

   private View nextView(View view,View root,boolean canGoDown,boolean moveForward,
                        boolean includingInternalWindows) {
        View nsv = null;
        if(view == root && canGoDown) {
            Vector sub = root.subviews();
            if(sub.count() > 0) {
                if(moveForward)
                    return root.firstSubview();
                else
                    return root.lastSubview();
            } else
                return null;
        } else {
            if(canGoDown &&
               (!view.hidesSubviewsFromKeyboard() ||
                (includingInternalWindows && view instanceof InternalWindow)) &&
               view.subviews().count() > 0) {
                if(moveForward)
                    return view.firstSubview();
                else
                    return view.lastSubview();
            } else {
                View superView = view.superview();
                if(moveForward) {
                    nsv = superView.viewAfter(view);
                    if(nsv != null)
                        return nsv;
                } else {
                    nsv = superView.viewBefore(view);
                    if(nsv != null)
                        return nsv;
                }
                if(superView == root)
                    return root;
                else
                    return nextView(superView,root,false,moveForward,includingInternalWindows);
            }
        }
    }

    private View findNextView(View selectedView,View root,boolean moveForward) {
        View nextView;
        View initialView;

        if(root==null)
            return null;


        if(selectedView == null) {
            return root;
        }

        nextView = initialView = selectedView;
        do {
            nextView = nextView(nextView,root,true,moveForward,false);
            if(nextView == null)
                break;
            if(nextView == initialView)
                break;
        } while(!(nextView.canBecomeSelectedView()));

        return nextView;
    }

    private View keyboardRootView() {
        View modalView = Application.application().modalView();
        if(modalView != null && modalView.isInViewHierarchy()) {
            return modalView;
        } else {
            Window mainWindow = mainWindow();
            if(mainWindow != null) {
                return (View)mainWindow;
            } else {
                if(application != null && application.firstRootView() != null)
                    return application.firstRootView();
                else
                    return this;
            }
        }
    }

    void showKeyboardArrowForView(View aView) {
        if(application!=null){
            KeyboardArrow  arrow = application.keyboardArrow();
            if(arrow.view() == aView)
                return;
            else {
                arrow.setRootView(this);
                arrow.setView(aView);
                updateArrowLocation(arrow);
                arrow.show();
            }
        }
    }

    void updateArrowLocation(KeyboardArrow arrow) {
        View aView = arrow.view();
        int        position  = application.keyboardArrowPosition(aView);
        Image     arrowImage =  application.keyboardArrowImage(position);
        Point     arrowHotSpot= application.keyboardArrowHotSpot(position);
        Point     arrowLocation = application.keyboardArrowLocation(aView,position);

        arrowLocation.x -= arrowHotSpot.x;
        arrowLocation.y -= arrowHotSpot.y;

        if(windowClipView() != null) {
            convertPointToView(windowClipView(),arrowLocation,arrowLocation);
        }
        arrow.setImage(arrowImage);
        arrow.moveTo(arrowLocation.x,
                     arrowLocation.y);
    }

    void hideKeyboardArrow() {
        if(application!=null){
            KeyboardArrow arrow = application.keyboardArrow();

            if(arrow.rootView() == this) {
                arrow.hide();
                arrow.setRootView(null);
                arrow.setView(null);
            }
        }
    }

    public boolean canBecomeSelectedView() {
        return true;
    }

    boolean wantsKeyboardArrow() {
        return false;
    }
}

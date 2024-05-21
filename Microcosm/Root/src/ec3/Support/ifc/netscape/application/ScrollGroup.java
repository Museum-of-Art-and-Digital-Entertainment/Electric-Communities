// ScrollGroup.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;

/** View subclass that automates the use of a ScrollView
  * and its controlling ScrollBars. You can create a ScrollView and
  * ScrollBars and connect them to each other, or you can create a
  * ScrollGroup that does this work for you. The ScrollGroup places the
  * vertical ScrollBar to the ScrollView's right, and the horizontal
  * ScrollBar below the ScrollView (if configured to have ScrollBars). It draws
  * the specified Border around its ScrollView. If you want to use
  * different ScrollBar positioning, such as different sides of the
  * ScrollView or non-abutting, you can create a ScrollGroup subclass or
  * simply instantiate and position the ScrollView and ScrollBars yourself.
  * @see ScrollBar
  * @see ScrollView
  * @note 1.0 draw->dirtyrect
  * @note 1.0 calc of needing bars using the scrollableObject now
  * @note 1.0 archiving changed
  */

public class ScrollGroup extends View implements ScrollBarOwner {
    ScrollView  scrollView;
    ScrollBar   vertScrollBar, horizScrollBar;
    Border      border;
    Border      interiorBorder;
    Color       cornerColor;
    int         horizScrollDisplay, vertScrollDisplay;
    private boolean     ignoreScrollBars;
    private final static boolean debugScrollers = false;

    /** Option for the ScrollGroup to never show a ScrollBar.
      */
    public static final int     NEVER_DISPLAY      = 0;
    /** Option for the ScrollGroup to always show a ScrollBar.
      */
    public static final int     ALWAYS_DISPLAY     = 1;
    /** Option for the ScrollGroup to only display a ScrollBar if the
      * contentView does not fit within the ScrollView (i.e. if the ScrollBar
      * is active).
      */
    public static final int     AS_NEEDED_DISPLAY  = 2;

    static final String         SCROLLVIEW_KEY = "scrollView",
                                VERTSCROLLBAR_KEY = "vertScrollBar",
                                HORIZSCROLLBAR_KEY = "horizScrollBar",
                                BORDER_KEY = "border",
                                HORIZDISPLAY_KEY = "horizScrollDisplay",
                                VERTDISPLAY_KEY = "vertScrollDisplay",
                                CORNER_COLOR_KEY = "cornerColor";

    /** Constructs a ScrollGroup with origin (<B>0</B>, <B>0</B>) and
      * zero width and height.
      */
    public ScrollGroup() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ScrollGroup with bounds <B>rect</B>.
      */
    public ScrollGroup(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ScrollGroup with bounds
      * (<B>x</B>, <B>y</B>, <B>width</B>, <B>height</B>).
      */
    public ScrollGroup(int x, int y, int width, int height) {
        super(x, y, width, height);

        border = EmptyBorder.emptyBorder();
        interiorBorder = new ScrollViewLineBorder();

        scrollView = createScrollView();
        addSubview(scrollView);

        // We need scrollbars for the putParts to work right
        horizScrollBar = createScrollBar(true);
        horizScrollBar.setScrollableObject(scrollView);
        horizScrollBar.setScrollBarOwner(this);
        scrollView.addScrollBar(horizScrollBar);
        addSubview(horizScrollBar);

        vertScrollBar = createScrollBar(false);
        vertScrollBar.setScrollableObject(scrollView);
        vertScrollBar.setScrollBarOwner(this);
        scrollView.addScrollBar(vertScrollBar);
        addSubview(vertScrollBar);

        ignoreScrollBars = false;

        setCornerColor(Color.lightGray);

        layoutView(0, 0);
    }

    /** Returns the ScrollGroup's minimum size.  Returns the value set by
      * <b>setMinSize()</b>, or computes the minimum size based on the
      * <b>vertScrollBarDisplay()</b> and <b>horizScrollBarDisplay()</b>
      * settings.
      */
    public Size minSize() {
        Size    minSize;
        int     minWidth = 0, minHeight = 0;

        /// If they have set the min Size on the ScrollGroup manually,
        /// ie a non-zero width/height
        /// we should honor the request.
        minSize = super.minSize();
        if (minSize != null && (minSize.width != 0 || minSize.height != 0))
            return minSize;

        // Never show either scroller
        if (vertScrollDisplay == NEVER_DISPLAY
            && horizScrollDisplay == NEVER_DISPLAY) {
                return new Size(border.widthMargin(),
                                border.heightMargin());
        }

        // both scrollers are possibly shown
        if (vertScrollDisplay != NEVER_DISPLAY
            && horizScrollDisplay != NEVER_DISPLAY) {
            return new Size(border.widthMargin()
                                + horizScrollBar().minSize().width
                                + vertScrollBar().minSize().width,
                                border.heightMargin()
                                    + horizScrollBar().minSize().height
                                    + vertScrollBar().minSize().height);
        }

        /// If we get here, we are only ever showing one of the scrollers
        if (horizScrollDisplay == ALWAYS_DISPLAY
            || horizScrollDisplay == AS_NEEDED_DISPLAY) {
            return new Size(border.widthMargin()
                                + horizScrollBar().minSize().width,
                            border.heightMargin()
                                + horizScrollBar().minSize().height * 2);
        }

        if (vertScrollDisplay == ALWAYS_DISPLAY
            || vertScrollDisplay == AS_NEEDED_DISPLAY)  {
            return new Size(border.widthMargin()
                                + vertScrollBar().minSize().width * 2,
                            border.heightMargin()
                                + vertScrollBar().minSize().height);
        }

        // We should never get here, but to be safe
        return super.minSize();
    }

    /** Sets the ScrollGroup's border.
      * This will call <b>layoutView()</b>
      * to make sure the rest of the views get positioned properly.
      * @see Border
      */
    /*
    public void setBorder(Border newBorder) {
        if (newBorder == null)
            newBorder = EmptyBorder.emptyBorder();

        border = newBorder;
        layoutView(0,0);
    }
    */

    /** Returns the ScrollGroup's border.
      * @see #setBorder
      */
    /*
    public Border border() {
        return border;
    }
    */

    /** Overridden to return <b>false</b> unless any of the ScrollGroup's
      * subviews are transparent.
      */
    public boolean isTransparent() {
        Vector sViews;
        int i, count;

        sViews = subviews();
        count = sViews.count();

        for (i = 0; i < count; i++) {
            if (((View)subviews().elementAt(i)).isTransparent())    {
                return true;
            }
        }

        return false;
    }

    /** Sets the ScrollGroup's interior Border, that is, the Border that
      * surrounds the ScrollView.  Calls <b>layoutView()</b>
      * to properly position the ScrollGroup's other Views.
      * @see Border
      */
    public void setBorder(Border newBorder) {
        if (newBorder == null)
            newBorder = EmptyBorder.emptyBorder();

        interiorBorder = newBorder;
        layoutView(0,0);
    }

    /** Returns the ScrollGroup's interior Border.
      * @see #setInteriorBorder
      */
    public Border border() {
        return interiorBorder;
    }


    /** Creates a new ScrollView. */
    protected ScrollView createScrollView() {
        return new ScrollView(0, 0, bounds.width, bounds.height);
    }

    /** Returns the ScrollGroup's ScrollView. */
    public ScrollView scrollView() {
        return scrollView;
    }

    /** Creates a new ScrollBar with the correct orientation. */
    protected ScrollBar createScrollBar(boolean horizontal) {
        ScrollBar aBar;

        if (horizontal) {
            aBar = new ScrollBar(0, 0, bounds.width, 1, Scrollable.HORIZONTAL);
        } else {
            aBar = new ScrollBar(0, 0, 1, bounds.height, Scrollable.VERTICAL);
        }
        return aBar;
    }

    /** Sets the vertical ScrollBar's behavior. This method exists for backward
      * compatibility.  Equivalent to the following code:
      * <PRE>
      *     if (flag)
      *         setVertScrollBarDisplay(ALWAYS_DISPLAY);
      *     else
      *         setVertScrollBarDisplay(NEVER_DISPLAY);
      * </PRE>
      */
    public void setHasVertScrollBar(boolean flag) {
        if (flag)
            setVertScrollBarDisplay(ALWAYS_DISPLAY);
        else
            setVertScrollBarDisplay(NEVER_DISPLAY);
    }

    /** Returns <b>true</b> if the setting for the vertical ScrollBar
      * equals ALWAYS_DISPLAY.
      * @see #vertScrollBarDisplay
      */
    public boolean hasVertScrollBar() {
        return vertScrollDisplay == ALWAYS_DISPLAY;
    }

    /** Sets the vertical ScrollBar's behavior.  The ScrollBar can be
      * configured to display when needed, to never display, or to always
      * display. Calls <b>layoutView()</b> to properly position the
      * ScrollGroup's other Views.
      * @see #ALWAYS_DISPLAY
      * @see #NEVER_DISPLAY
      * @see #AS_NEEDED_DISPLAY
      * @see #setHorizScrollBarDisplay
      */
    public void setVertScrollBarDisplay(int flag) {
        vertScrollDisplay = flag;
        if (vertScrollBar == null){
            vertScrollBar = createScrollBar(false);
            vertScrollBar.setScrollableObject(scrollView);
            vertScrollBar.setScrollBarOwner(this);
            scrollView.addScrollBar(vertScrollBar);
            addSubview(vertScrollBar);
        }
        layoutView(0,0);
    }

    /** Returns the vertical ScrollBar's behavior setting.
      * @see #setVertScrollBarDisplay
      */
    public int vertScrollBarDisplay()   {
        return vertScrollDisplay;
    }

    /** Returns the ScrollGroup's vertical ScrollBar. */
    public ScrollBar vertScrollBar() {
        return vertScrollBar;
    }

    /** Sets the horizontal ScrollBar's behavior. This method exists for
      * backward compatibility.  Equivalent to the following code:
      * <PRE>
      *     if (flag)
      *         setHorizScrollBarDisplay(ALWAYS_DISPLAY);
      *     else
      *         setHorizScrollBarDisplay(NEVER_DISPLAY);
      * </PRE>
      */
    public void setHasHorizScrollBar(boolean flag) {
        if (flag)
            setHorizScrollBarDisplay(ALWAYS_DISPLAY);
        else
            setHorizScrollBarDisplay(NEVER_DISPLAY);
    }

    /** Returns <b>true</b> if the setting for the horizontal ScrollBar
      * equals ALWAYS_DISPLAY.
      * @see #horizScrollBarDisplay
      */
    public boolean hasHorizScrollBar() {
        return horizScrollDisplay == ALWAYS_DISPLAY;
    }

    /** Sets the horizontal ScrollBar's behavior.  The ScrollBar can be
      * configured to display when needed, to never display, or to always
      * display. Calls <b>layoutView()</b> to properly position the
      * ScrollGroup's other Views.
      * @see #ALWAYS_DISPLAY
      * @see #NEVER_DISPLAY
      * @see #AS_NEEDED_DISPLAY
      * @see #setHorizScrollBarDisplay
      */
    public void setHorizScrollBarDisplay(int flag) {
        horizScrollDisplay = flag;
        if (horizScrollBar == null) {
            horizScrollBar = createScrollBar(true);
            horizScrollBar.setScrollableObject(scrollView);
            horizScrollBar.setScrollBarOwner(this);
            scrollView.addScrollBar(horizScrollBar);
            addSubview(horizScrollBar);
        }
        layoutView(0, 0);
    }

    /** Returns the horizontal ScrollBar's behavior setting.
      * @see #setHorizScrollBarDisplay
      */
    public int horizScrollBarDisplay()  {
        return horizScrollDisplay;
    }


    /** Returns the ScrollGroup's horizontal ScrollBar. */
    public ScrollBar horizScrollBar() {
        return horizScrollBar;
    }

    /* This guy does the magic of laying out the view.
     * Order is important. Don't move the code around.
     */
    private void putParts() {
        int     sViewX, sViewY, sViewWidth, sViewHeight;
        Rect    contentBounds;

        if(debugScrollers)
            System.err.println("  putParts");

        // Start from a known state
        horizScrollBar.removeFromSuperview();
        vertScrollBar.removeFromSuperview();

        if (scrollView == null)
            return;

        contentBounds = new Rect(0, 0, 0, 0);
        if(horizScrollBar != null && horizScrollBar.scrollableObject() != null) {
            contentBounds.width =
                horizScrollBar.scrollableObject().lengthOfContentViewForAxis(
                                                Scrollable.HORIZONTAL);
        } else if (scrollView.contentView != null) {
            contentBounds.width = scrollView.contentView.bounds.width;
        }

        if(vertScrollBar != null && vertScrollBar.scrollableObject() != null)   {
            contentBounds.height =
                vertScrollBar.scrollableObject().lengthOfContentViewForAxis(
                                                Scrollable.VERTICAL);
        } else if (scrollView.contentView != null) {
            contentBounds.height = scrollView.contentView.bounds.height;
        }

        if (debugScrollers)
            if (vertScrollBar != null)
                System.err.println("   vert: " + vertScrollBar.isActive());
        if (debugScrollers)
            if (horizScrollBar != null)
                System.err.println("   horz: " + horizScrollBar.isActive());


        sViewX = border.leftMargin() + interiorBorder.leftMargin();
        sViewY = border.topMargin() + interiorBorder.topMargin();
        sViewWidth = bounds.width - border.widthMargin() -
            interiorBorder.widthMargin();
        sViewHeight= bounds.height - border.heightMargin() -
            interiorBorder.heightMargin();

        ignoreScrollBars = true;
        scrollView.moveTo(sViewX, sViewY);
        scrollView.sizeTo(sViewWidth, sViewHeight);
        // This is kinda wrong. The scrollView gets sized to the full
        // width, height assuming no bars, but we then put the bars on
        // Since the bars have been removed from the superview, that's
        // ok. The reason we are doing this, is so that the ScrollBars
        // are the right size before we do the calcs. Otherwise, we
        // might be out of sync, because the scrollbar is sized wrong
        // and thinks it is active.
        vertScrollBar.moveTo(sViewX + sViewWidth - vertScrollBar.bounds.width,
                             sViewY);
        vertScrollBar.sizeTo(vertScrollBar.bounds.width, sViewHeight);
        horizScrollBar.moveTo(sViewX, sViewY + sViewHeight -
                              horizScrollBar.bounds.height);
        horizScrollBar.sizeTo(sViewWidth, horizScrollBar.bounds.height);
        ignoreScrollBars = false;

        /// Need both scrollbars
        if ((vertScrollDisplay == ALWAYS_DISPLAY
             && horizScrollDisplay == ALWAYS_DISPLAY)
            || ((vertScrollDisplay == AS_NEEDED_DISPLAY
                 && horizScrollDisplay == AS_NEEDED_DISPLAY)
                    && (scrollView.bounds.width <
                            contentBounds.width
                    && scrollView.bounds.height <
                            contentBounds.height)))  {
            setBothScrollersOn();
            return;
        }

        // Need Vert scrollbars
        if ((vertScrollDisplay == ALWAYS_DISPLAY)
                || (vertScrollDisplay == AS_NEEDED_DISPLAY
                    && scrollView.bounds.height <
                        contentBounds.height))    {
            // By adding the Vertical do we also need the horizontal?
            if (debugScrollers)
                System.err.println("  " + (scrollView.bounds.width
                                         - contentBounds.width)
                                         + " < " + vertScrollBar.bounds.width);
            if ((horizScrollDisplay == ALWAYS_DISPLAY)
                || (horizScrollDisplay == AS_NEEDED_DISPLAY
                    && ((scrollView.bounds.width - contentBounds.width)
                        < vertScrollBar.bounds.width))) {
                if(debugScrollers)
                    System.err.println("because of vert");
                setBothScrollersOn();
                return;
            }

            horizScrollBar.removeFromSuperview();

            ignoreScrollBars = true;
            scrollView.moveTo(sViewX, sViewY);
            scrollView.sizeTo(sViewWidth - vertScrollBar.bounds.width,
                              sViewHeight);
            vertScrollBar.moveTo(bounds.width - border.rightMargin() -
                                 vertScrollBar.bounds.width,
                                 border.topMargin());
            vertScrollBar.sizeTo(vertScrollBar.bounds.width, bounds.height -
                                 border.heightMargin());
            addSubview(vertScrollBar);
            ignoreScrollBars = false;

            if (debugScrollers)
                System.err.println(" Vert");
            return;
        }

        // Inactive Scrollbar - need to remove it and adjust the scrollview
        // Really this means that we don't need either scroller. Make it so.
        if ((vertScrollDisplay == AS_NEEDED_DISPLAY
             && horizScrollDisplay == AS_NEEDED_DISPLAY)
                 && (scrollView.bounds.height == contentBounds.height)) {
            ignoreScrollBars = true;
            vertScrollBar.removeFromSuperview();
            horizScrollBar.removeFromSuperview();
            scrollView.moveTo(sViewX, sViewY);
            scrollView.sizeTo(sViewWidth, sViewHeight);
            ignoreScrollBars = false;
            if (debugScrollers)
                System.err.println(" vert == Removed");
            return;
        }

        /// Need horiz scrollbars
        if ((horizScrollDisplay == ALWAYS_DISPLAY)
                || (horizScrollDisplay == AS_NEEDED_DISPLAY
                    && scrollView.bounds.width < contentBounds.width)) {
            // By adding the Horizontal do we also need the vertical?
            if((vertScrollDisplay == ALWAYS_DISPLAY)
                || (vertScrollDisplay == AS_NEEDED_DISPLAY
                    && scrollView.bounds.height - contentBounds.height <
                            horizScrollBar.bounds.height)) {
                if (debugScrollers)
                    System.err.println("because of horiz");
                setBothScrollersOn();
                return;
            }

            vertScrollBar.removeFromSuperview();

            ignoreScrollBars = true;
            scrollView.moveTo(sViewX, sViewY);
            scrollView.sizeTo(sViewWidth,
                              sViewHeight - horizScrollBar.bounds.height);
            horizScrollBar.moveTo(border.leftMargin(),
                                  bounds.height - border.bottomMargin() -
                                  horizScrollBar.bounds.height);
            horizScrollBar.sizeTo(bounds.width - border.widthMargin(),
                                  horizScrollBar.bounds.height);
            addSubview(horizScrollBar);
            ignoreScrollBars = false;

            if (debugScrollers)
                System.err.println(" Horiz " );
            return;
        }

        // Inactive Scrollbar - need to remove it and adjust the scrollview
        // Really this means that we don't need either scroller. Make it so.
        if ((horizScrollDisplay == AS_NEEDED_DISPLAY
            && vertScrollDisplay == AS_NEEDED_DISPLAY)
            && (scrollView.bounds.width == contentBounds.width)) {
            ignoreScrollBars = true;
            horizScrollBar.removeFromSuperview();
            vertScrollBar.removeFromSuperview();
            scrollView.moveTo(sViewX, sViewY);
            scrollView.sizeTo(sViewWidth, sViewHeight);
            ignoreScrollBars = false;
            if (debugScrollers)
                System.err.println(" Horiz == Removed");
            return;
        }

        if (debugScrollers)
            System.err.println(" Fall through case - None ");

//        if(debugScrollers) System.err.println(" ALMOST MISSED A CASE " +
//                                "Inconsistancy Error " +
//                                            "\n - " + scrollView +
//                                            "\n - " + scrollView.contentView +
//                                            "\n - " + this);
//
//          if(debugScrollers)
//          System.err.println("    H:" + scrollView.bounds.height + " , "
//                                                                                              + contentBounds.height
//                                                                                              + vertScrollBar);
//        if(debugScrollers)
//          System.err.println("    W:" + scrollView.bounds.width + " , "
//                                                                                              + contentBounds.width
//                                                                                              + horizScrollBar);
    }

    void setBothScrollersOn()   {
        int     sViewX, sViewY, sViewWidth, sViewHeight;

        sViewX = border.leftMargin() + interiorBorder.leftMargin();
        sViewY = border.topMargin() + interiorBorder.topMargin();
        sViewWidth  = bounds.width - border.widthMargin() -
            interiorBorder.widthMargin();
        sViewHeight = bounds.height - border.heightMargin() -
            interiorBorder.heightMargin();

        ignoreScrollBars = true;

        scrollView.moveTo(sViewX, sViewY);
        scrollView.sizeTo(sViewWidth - vertScrollBar.bounds.width,
                          sViewHeight - horizScrollBar.bounds.height);
        horizScrollBar.moveTo(border.leftMargin(),
                              bounds.height - border.bottomMargin() -
                              horizScrollBar.bounds.height);
        horizScrollBar.sizeTo(bounds.width - border.widthMargin() -
                              vertScrollBar.bounds.width + 2,
                              horizScrollBar.bounds.height);
        addSubview(horizScrollBar);

        vertScrollBar.moveTo(bounds.width - border.rightMargin() -
                             vertScrollBar.bounds.width, border.topMargin());
        vertScrollBar.sizeTo(vertScrollBar.bounds.width,
                             bounds.height - border.heightMargin() -
                             horizScrollBar.bounds.height + 2);
        addSubview(vertScrollBar);

        ignoreScrollBars = false;

        if (debugScrollers)
            System.err.println(" Both");
    }

    /** Draws the ScrollGroup's interior Border. */
    public void drawView(Graphics g) {
        border.drawInRect(g, 0, 0,  bounds.width, bounds.height);
        interiorBorder.drawInRect(g, border.leftMargin(), border.topMargin(),
               scrollView.bounds.width + interiorBorder.widthMargin(),
               scrollView.bounds.height + interiorBorder.heightMargin());
        if (cornerColor != null && horizScrollBarIsVisible() &&
               vertScrollBarIsVisible()) {
            g.setColor(cornerColor);
            g.fillRect(horizScrollBar.bounds.maxX(),
                       vertScrollBar.bounds.maxY(),
                       vertScrollBar.bounds.width - 2,
                       horizScrollBar.bounds.height - 2);
        }
    }

    /** Draws the ScrollView portion of the ScrollGroup. */
    public void drawContents() {
        scrollView.setDirty(true);
    }

    /** @private */
    public void drawSubviews(Graphics g) {
        super.drawSubviews(g);
        drawView(g);
    }

    /** Convenience method for making <b>aView</b> the ScrollView's content
      * View.  Calls <b>layoutView()</b> to properly position the
      * ScrollGroup's other Views.
      * @see ScrollView#setContentView
      */
    public void setContentView(View aView) {
        scrollView.setContentView(aView);
        layoutView(0, 0);
    }

    /** Convenience method for retrieving the ScrollGroup's ScrollView's
      * contentView.
      * @see ScrollView#contentView
      */
    public View contentView() {
        return scrollView.contentView();
    }

    /** Convenience method for setting the ScrollGroup's ScrollView's
      * background Color.
      * @see ScrollView#setBackgroundColor
      */
    public void setBackgroundColor(Color aColor) {
        scrollView.setBackgroundColor(aColor);
    }

    /** Sets the color displayed in the bottom right corner of the
      * ScrollGroup when both a horizontal Scrollbar and vertical ScrollBar
      * are visible.
      * @private
      */
    public void setCornerColor(Color aColor) {
        cornerColor = aColor;
    }

/* archiving */

    /** Describes the ScrollGroup class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ScrollGroup", 2);
        info.addField(SCROLLVIEW_KEY, OBJECT_TYPE);
        info.addField(VERTSCROLLBAR_KEY, OBJECT_TYPE);
        info.addField(HORIZSCROLLBAR_KEY, OBJECT_TYPE);
        info.addField(BORDER_KEY, OBJECT_TYPE);
        info.addField(HORIZDISPLAY_KEY, INT_TYPE);
        info.addField(VERTDISPLAY_KEY, INT_TYPE);
        info.addField(CORNER_COLOR_KEY, OBJECT_TYPE);
    }

    /** Encodes the ScrollGroup instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(SCROLLVIEW_KEY, scrollView);
        encoder.encodeObject(VERTSCROLLBAR_KEY, vertScrollBar);
        encoder.encodeObject(HORIZSCROLLBAR_KEY, horizScrollBar);
        encoder.encodeObject(BORDER_KEY, interiorBorder);
        encoder.encodeInt(HORIZDISPLAY_KEY, horizScrollDisplay);
        encoder.encodeInt(VERTDISPLAY_KEY, vertScrollDisplay);
        encoder.encodeObject(CORNER_COLOR_KEY, cornerColor);
    }

    /** Decodes the ScrollGroup instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.ScrollGroup");
        super.decode(decoder);

        scrollView = (ScrollView)decoder.decodeObject(SCROLLVIEW_KEY);
        vertScrollBar = (ScrollBar)decoder.decodeObject(VERTSCROLLBAR_KEY);
        horizScrollBar = (ScrollBar)decoder.decodeObject(HORIZSCROLLBAR_KEY);
        interiorBorder = (Border)decoder.decodeObject(BORDER_KEY);
        horizScrollDisplay = (int)decoder.decodeInt(HORIZDISPLAY_KEY);
        vertScrollDisplay = (int)decoder.decodeInt(VERTDISPLAY_KEY);
        if(version >= 2)
            cornerColor = (Color)decoder.decodeObject(CORNER_COLOR_KEY);
        else
            cornerColor = Color.lightGray;
    }

    /* ScrollBarOwner */

    /** Implemented to catch ScrollBar activation and ensure their proper
      * display.
      * @see ScrollBarOwner
      */
    public void scrollBarDidBecomeActive(ScrollBar aScrollBar)    {
        if(debugScrollers)
            System.err.println("--scrollBarBecameActive");

        if(ignoreScrollBars)
            return;
        if(aScrollBar == horizScrollBar && horizScrollDisplay == NEVER_DISPLAY)
            return;
        if(aScrollBar == vertScrollBar && vertScrollDisplay == NEVER_DISPLAY)
            return;

        layoutView(0, 0);
        if(debugScrollers)  {
            if(aScrollBar == vertScrollBar && !vertScrollBarIsVisible())
                System.err.println("Inconsistancy Error " + aScrollBar +
                    " - " + scrollView +
                    " - " + scrollView.contentView +
                    " - " + this);
            else if(aScrollBar == horizScrollBar && !horizScrollBarIsVisible())
                System.err.println("Inconsistancy Error " + aScrollBar +
                    "\n - " + scrollView +
                    "\n - " + scrollView.contentView +
                    "\n - " + this);
        }
   }

    /** Implemented to catch ScrollBar deactivation and ensure their removal,
      * if necessary.
      * @see ScrollBarOwner
      */
    public void scrollBarDidBecomeInactive(ScrollBar aScrollBar)  {
        if(debugScrollers)
            System.err.println("--scrollBarBecameInactive");

        if(ignoreScrollBars)
            return;
        if(aScrollBar == horizScrollBar && horizScrollDisplay == NEVER_DISPLAY)
            return;
        if(aScrollBar == vertScrollBar && vertScrollDisplay == NEVER_DISPLAY)
            return;

        layoutView(0, 0);
    }

    /** Implemented to catch ScrollBar enabling and ensure their display.
      * @see ScrollBarOwner
      */
    public void scrollBarWasEnabled(ScrollBar aScrollBar) {
        if(debugScrollers)
            System.err.println("--scrollBarEnabled");

        if(ignoreScrollBars)
            return;
        if(aScrollBar == horizScrollBar && horizScrollDisplay == NEVER_DISPLAY)
            return;
        if(aScrollBar == vertScrollBar && vertScrollDisplay == NEVER_DISPLAY)
            return;

        layoutView(0, 0);
    }

    /** Implemented to catch ScrollBar disabling and ensure their removal,
      * if necessary.
      * @see ScrollBarOwner
      */
    public void scrollBarWasDisabled(ScrollBar aScrollBar)    {
        if(debugScrollers)
            System.err.println("--scrollBarDisabled");

        if(ignoreScrollBars)
            return;
        if(aScrollBar == horizScrollBar && horizScrollDisplay == NEVER_DISPLAY)
            return;
        if(aScrollBar == vertScrollBar && vertScrollDisplay == NEVER_DISPLAY)
            return;

        layoutView(0, 0);
    }

    /** Returns <b>true</b> if the vertical ScrollBar is visible. Equivalent to
      * <PRE>
      *     return vertScrollBar().isInViewHierarchy();
      * </PRE>
      */
    public boolean vertScrollBarIsVisible() {
        return vertScrollBar.isInViewHierarchy();
    }

    /** Returns <b>true</b> if the horizontal ScrollBar is visible. Equivalent
      * to
      * <PRE>
      *     return horizScrollBar().isInViewHierarchy();
      * </PRE>
      */
    public boolean horizScrollBarIsVisible() {
        return horizScrollBar.isInViewHierarchy();
    }

    /** Overridden to implement special ScrollGroup subview layout behavior.
      */
    public void layoutView(int x, int y)    {
        putParts();
        ignoreScrollBars = false;   // We shouldn't need this, but just in case
        setDirty(true);
    }
}

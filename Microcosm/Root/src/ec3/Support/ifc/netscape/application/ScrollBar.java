// ScrollBar.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import java.lang.Math;
import netscape.util.*;

// When clicking in the tray, the knob should scroll until the point is
//   under the mouse - ALERT!

/** View subclass that controls the display of a Scrollable object's
  * contents. A ScrollBar has scroll buttons, which when clicked or held
  * incrementally scroll the Scrollable object's contents, and a scroll tray,
  * the area between the ScrollBar's scroll buttons that contains the
  * ScrollBar's scroll knob. The scroll knob's position represents the portion
  * of the Scrollable object's contentView that is currently visible within the
  * Scrollable object.<p>
  * The ScrollBar class implements a "proportional" scroll knob, meaning
  * that the knob's length represents the percentage of the contentView
  * currently visible within the Scrollable object; the ScrollBar's tray
  * represents
  * the contentView's total length. In other words, the knob's length is
  * to the scroll tray's length as the Scrollable object's length is to its
  * contentView's length. A ScrollBar is typically instantiated by a
  * ScrollGroup, but need not be. You can instantiate an object implementing
  * the Scrollable interface and a crollBar, and connect them by calling the
  * ScrollBar's <b>setScrollableObject()</b> method and the Scrollable's
  * <b>addScrollBar()</b> method.
  * @see Scrollable
  * @see ScrollView
  * @see ScrollGroup
  * @note 1.0 draws->dirtyRect
  * @note 1.0 better behavior at small sizes
  * @note 1.0 archiving the owner now
  * @note 1.0 support for keyboard UI
  */

public class ScrollBar extends View implements Target {
    Scrollable  scrollableView;
    Button      increaseButton, decreaseButton;
    Image       knobImage, trayTopImage, trayBottomImage, trayLeftImage,
                trayRightImage;
    Timer       timer;
    int         scrollValue, origScrollValue, knobLength,
                lastMouseValue, lastAltMouseValue, lineIncrement, axis;
    boolean     active, enabled, shouldRedraw;
    float       pageSizeAsPercent;
    int         pixelScrollValue;   // Not used if we lose the hack
    ScrollBarOwner  scrollBarOwner;

    /** The default line scroll increment. */
    public static final int     DEFAULT_LINE_INCREMENT = 12;
    /** The default page scroll increment. */
    public static final float   DEFAULT_PAGE_SIZE = 1.0f;

    /** Use to initialize a vertical ScrollBar to its default width.
      * @private
      */
    public static final int     DEFAULT_WIDTH = 0;
    /** Use to initialize a horizontal ScrollBar to its default height.
      * @private
      */
    public static final int     DEFAULT_HEIGHT = 0;
    /** Command updating the ScrollBar's scroll knob position and size. */
    public static final String  UPDATE = "updateScrollValue";

    /** Command to scroll down by a page.   */
    public static final String  SCROLL_PAGE_BACKWARD = "scrollPageBackward";
    /** Command to scroll up by a page.  */
    public static final String  SCROLL_PAGE_FORWARD = "scrollPageForward";
    /** Command to scroll right by a page.  */
    public static final String  SCROLL_LINE_BACKWARD = "scrollLineBackward";
    /** Command to scroll left by a page.  */
    public static final String  SCROLL_LINE_FORWARD = "scrollLineForward";

    private static final String TIMER_SCROLL_PAGE = "timerScroll";

    static final String         SCROLLVIEW_KEY = "scrollView",
                                INCREASEBUTTON_KEY = "increaseButton",
                                DECREASEBUTTON_KEY = "decreaseButton",
                                KNOBIMAGE_KEY = "image",
                                SCROLLVALUE_KEY = "scrollValue",
                                AXIS_KEY = "axis",
                                ACTIVE_KEY = "active",
                                ENABLED_KEY = "enabled",
                                LINE_INCREMENT_KEY = "lineIncrement",
                                OWNER_KEY = "owner";

    /** Constructs a ScrollBar with origin (0, 0) and zero width and
      * height.
      */
    public ScrollBar() {
        this(0, 0, 0, 0);
    }

    /** Constructs a ScrollBar with bounds <B>rect</B>.
      */
    public ScrollBar(Rect rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /** Constructs a ScrollBar with bounds
      * (<B>x</B>,<B> y</B>, <B>width</B>, <B>height</B>). The ScrollBar
      * will have a vertical orientation.
      */
    public ScrollBar(int x, int y, int width, int height) {
        this(x, y, width, height, Scrollable.VERTICAL);
    }

    /** Constructs a ScrollBar with bounds
      * (<B>x</B>,<B> y</B>, <B>width</B>, <B>height</B>), and the specified
      * orientation.
      */
    public ScrollBar(int x, int y, int width, int height, int theAxis) {
        super(x, y, width, height);

        Button  button;

        axis = theAxis;
        lineIncrement = DEFAULT_LINE_INCREMENT;
        pageSizeAsPercent  = DEFAULT_PAGE_SIZE;
        setEnabled(true);

        /* increase button */
        button = new Button(0, 0, 16, 16);
        button.setType(Button.CONTINUOUS_TYPE);
        button.setBordered(true);
        button.setRaisedBorder(BezelBorder.raisedScrollButtonBezel());
        button.setLoweredBorder(BezelBorder.loweredScrollButtonBezel());
        if (axis == Scrollable.HORIZONTAL) {
            button.setImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollRightArrow.gif"));
            button.setAltImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollRightArrowActive.gif"));

        } else {
            button.setImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollDownArrow.gif"));
            button.setAltImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollDownArrowActive.gif"));
        }
        setIncreaseButton(button);

        /* decrease button */
        button = new Button(0, 0, 16, 16);
        button.setType(Button.CONTINUOUS_TYPE);
        button.setBordered(true);
        button.setRaisedBorder(BezelBorder.raisedScrollButtonBezel());
        button.setLoweredBorder(BezelBorder.loweredScrollButtonBezel());
        if (axis == Scrollable.HORIZONTAL) {
            button.setImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollLeftArrow.gif"));
            button.setAltImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollLeftArrowActive.gif"));
        } else {
            button.setImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollUpArrow.gif"));
            button.setAltImage(Bitmap.bitmapNamed(
                      "netscape/application/ScrollUpArrowActive.gif"));
        }
        setDecreaseButton(button);

        if (axis == Scrollable.HORIZONTAL) {
            setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
            setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        } else {
            setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
            setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        }

        if (axis == Scrollable.HORIZONTAL) {
            setKnobImage(Bitmap.bitmapNamed(
                "netscape/application/ScrollKnobH.gif"));
        } else {
            setKnobImage(Bitmap.bitmapNamed(
                "netscape/application/ScrollKnobV.gif"));
        }

        trayTopImage = Bitmap.bitmapNamed(
                          "netscape/application/ScrollTrayTop.gif");
        trayBottomImage = Bitmap.bitmapNamed(
                          "netscape/application/ScrollTrayBottom.gif");
        trayLeftImage = Bitmap.bitmapNamed(
                          "netscape/application/ScrollTrayLeft.gif");
        trayRightImage = Bitmap.bitmapNamed(
                          "netscape/application/ScrollTrayRight.gif");

        /* check size and activate if large enough */
        if ((axis == Scrollable.HORIZONTAL) && bounds.height == 0 ||
            !(axis == Scrollable.HORIZONTAL) && bounds.width == 0) {
            _adjustToFit();
        }
        _computeScrollValue();
        _setupKeyboard();

    }


    /** Overridden to return <b>false</b> (ScrollBars are opaque). */
    public boolean isTransparent() {
        return false;
    }

    /** Returns a Rect describing the ScrollBar's "interior," the ScrollBar's
      * bounds minus its left, right, top and bottom borders.
      */
    public Rect interiorRect() {
        return Rect.newRect(1, 1, width() - 2, height() - 2);
    }

    /** Returns the ScrollBar's minimum size, which is the ScrollBar's border
      * widths and heights plus the area required to display its scroll
      * buttons.
      */
    public Size minSize() {
        int     width, height;

//ALERT!!
//        if (_minSize != null) {
//            return new Size(_minSize);
//        }

        if (increaseButton != null) {
            width = increaseButton.bounds.width;
            height = increaseButton.bounds.height;
        } else {
            width = height = 0;
        }
        if (decreaseButton != null) {
            if (axis == Scrollable.HORIZONTAL) {
                width += decreaseButton.bounds.width;
                if (height < decreaseButton.bounds.height) {
                    height = decreaseButton.bounds.height;
                }
            } else {
                if (width < decreaseButton.bounds.width) {
                    width = decreaseButton.bounds.width;
                }
                height += decreaseButton.bounds.height;
            }
        }

        return new Size(width + 2, height + 2);
    }

    /* Makes sure a vertical ScrollBar is wide enough and a horizontal
     * ScrollBar is tall enough for its buttons.
     */
    void _adjustToFit() {
        Size    minSize;

        minSize = minSize();
        if (axis == Scrollable.HORIZONTAL) {
            sizeTo(bounds.width, minSize.height);
        } else {
            sizeTo(minSize.width, bounds.height);
        }
    }

    /** Overridden to ensure there is room enough for the scroll Buttons.
      */
    public void didSizeBy(int deltaWidth, int deltaHeight) {
        super.didSizeBy(deltaWidth, deltaHeight);
        _computeScrollValue();
    }


/* scroll buttons */

    /** Makes <b>aButton</b> the ScrollBar's "increase" Button, the
      * the Button that scrolls to the right in a horizontal ScrollBar and
      * scrolls down in a vertical ScrollBar.  Sets the Button's resize
      * instructions and command it sends when clicked.
      */
    public void setIncreaseButton(Button aButton) {
        if (increaseButton != null) {
            increaseButton.removeFromSuperview();
        }

        increaseButton = aButton;
        if (increaseButton != null) {
            increaseButton.setTarget(this);

            increaseButton.setCommand(SCROLL_LINE_FORWARD);
            if (axis == Scrollable.HORIZONTAL) {
                increaseButton.setHorizResizeInstruction(
                    View.LEFT_MARGIN_CAN_CHANGE);
                increaseButton.setVertResizeInstruction(
                    View.HEIGHT_CAN_CHANGE);
            } else {
                increaseButton.setHorizResizeInstruction(
                    View.WIDTH_CAN_CHANGE);
                increaseButton.setVertResizeInstruction(
                    View.TOP_MARGIN_CAN_CHANGE);
            }

            /* make sure just enough room to properly display the button */
            _adjustToFit();
        }

        if (enabled && active) {
            addParts();
        }
    }

    /** Returns the ScrollBar's "increase" Button.
      * @see #setIncreaseButton
      */
    public Button increaseButton() {
        return increaseButton;
    }

    /** Makes <B>aButton</B> the ScrollBar's "decrease" Button, the Button
      * that scrolls
      * to the left in a horizontal ScrollBar and scrolls up in a vertical
      * ScrollBar. Sets the Button's resize instructions and the command that
      * it sends when clicked.
      */
    public void setDecreaseButton(Button aButton) {
        if (decreaseButton != null) {
            decreaseButton.removeFromSuperview();
        }

        decreaseButton = aButton;
        if (decreaseButton != null) {
            decreaseButton.setTarget(this);

            decreaseButton.setCommand(SCROLL_LINE_BACKWARD);
            if (axis == Scrollable.HORIZONTAL) {
                decreaseButton.setHorizResizeInstruction(
                    View.RIGHT_MARGIN_CAN_CHANGE);
                decreaseButton.setVertResizeInstruction(
                    View.HEIGHT_CAN_CHANGE);
            } else {
                decreaseButton.setHorizResizeInstruction(
                    View.WIDTH_CAN_CHANGE);
                decreaseButton.setVertResizeInstruction(
                    View.BOTTOM_MARGIN_CAN_CHANGE);
            }

            /* make sure just enough room to properly display the button */
            _adjustToFit();
        }
        if (enabled && active) {
            addParts();
        }
    }

    /** Returns the ScrollBar's "decrease" Button.
      * @see #setDecreaseButton
      */
    public Button decreaseButton() {
        return decreaseButton;
    }

    /** Adds the ScrollBar's scroll Buttons to the ScrollBar. Called whenever
      * the ScrollBar switches from the disabled to enabled or inactive to
      * active states. Override to add a ScrollBar subclass' additional
      * controls.
      */
    public void addParts() {
        int     x, y;

        x = 1;
        y = 1;

        if (decreaseButton != null) {
            decreaseButton.moveTo(x, y);
        }
        if (axis == Scrollable.HORIZONTAL) {
            if (increaseButton != null) {
                increaseButton.moveTo(width() - 1 - increaseButton.width(), y);
            }
        } else {
            if (increaseButton != null) {
                increaseButton.moveTo(x, height() - 1 -
                                      increaseButton.height());
            }
        }

        addSubview(increaseButton);
        addSubview(decreaseButton);
    }

    /** Removes the ScrollBar's scroll Buttons from the ScrollBar. Called
      * whenever the ScrollBar switches from the enabled to disabled or active
      * to inactive states. Override to remove a ScrollBar subclass' additional
      * controls.
      */
    public void removeParts() {
        if (increaseButton != null) {
            increaseButton.removeFromSuperview();
        }
        if (decreaseButton != null) {
            decreaseButton.removeFromSuperview();
        }
    }



/* scrollView */


    /** Sets the ScrollBar's Scrollable object, the object that the ScrollBar
      * sends scroll commands to. Normally you will also have to add this
      * ScrollBar to the Scrollable object's list of objects it notifies of
      * of scroll changes.
      * @see ScrollView#addScrollBar
      */
    public void setScrollableObject(Scrollable aScrollableView) {
        if (scrollableView == aScrollableView) {
            return;
        }

        scrollableView = aScrollableView;
        _computeScrollValue();
    }

    /** Returns the ScrollBar's Scrollable object, the object to which the
      * ScrollBar sends scroll commands.
      * @see #setScrollableObject
      */
    public Scrollable scrollableObject() {
        return scrollableView;
    }

    /** Sets the ScrollBar owner, the object that receives notifications
      * regarding the ScrollBar's state.
      * @see ScrollBarOwner
      */
    public void setScrollBarOwner(ScrollBarOwner owner) {
        scrollBarOwner = owner;
    }

    /** Returns the ScrollBar owner, the object that receives notifications
      * regarding the ScrollBar's state.
      * @see #setScrollBarOwner
      */
    public ScrollBarOwner scrollBarOwner()  {
        return scrollBarOwner;
    }


/* scroll tray */


    /** Returns a Rect describing the ScrollBar's "tray," the ScrollBar's
      * interior minus the area covered by its scroll Buttons and its
      * border.  Override
      * this method to return the appropriate tray Rect if you create a
      * ScrollBar subclass that has additional controls or does not
      * have a one pixel border.
      */
    public Rect scrollTrayRect() {
        int     x, maxX, y, maxY;

        if (axis == Scrollable.HORIZONTAL) {
            /* have to compute by hand since the button may not be a subview
             * and therefore may not have the correct coordinates
             */
            x = 1;
            if (decreaseButton != null) {
                x += decreaseButton.bounds.width;
            }
            maxX = width() - 1;
            if (increaseButton != null) {
                maxX -= increaseButton.width();
            }

            return Rect.newRect(x, 1, maxX - x, height() - 2);
        } else {
            y = 1;
            if (decreaseButton != null) {
                y += decreaseButton.height();
            }
            maxY = height() - 1;
            if (increaseButton != null) {
                maxY -= increaseButton.height();
            }

            return Rect.newRect(1, y, width() - 2, maxY - y);
        }
    }

    /** Returns the length of the scroll tray, the portion of the ScrollBar
      * not covered by controls, containing the ScrollBar's knob.  Returns the
      * distance (height if vertical or width if horizontal) from the Rect
      * returned by <b>scrollTrayRect()</b>.
      * @see #scrollTrayRect
      */
    public int scrollTrayLength() {
        Rect    scrollTrayRect;
        int     length;

        scrollTrayRect = scrollTrayRect();
        length = (axis == Scrollable.HORIZONTAL) ? scrollTrayRect.width
                                                 : scrollTrayRect.height;
        Rect.returnRect(scrollTrayRect);

        return length;
    }



/* scroll knob */


    /** Sets the knob's image. */
    public void setKnobImage(Image anImage) {
        knobImage = anImage;

        /*
         * make sure just enough room to properly display the image (if
         * the scoller's size is based on its knob image
         */
        _adjustToFit();
    }

    /** Returns the knob's image.
      * @see #setKnobImage
      */
    public Image knobImage() {
        return knobImage;
    }

    /** Returns a Rect describing the ScrollBar's "knob," the rectangular
      * control the user can click and drag to cause the ScrollBar's
      * Scrollable to scroll.  Computes the rect based on the ScrollBar's
      * scroll value and knob length.
      * @see #scrollValue
      * @see #knobLength
      */
    public Rect knobRect() {
        Rect    tmpRect;
        int     x, y, width, height;

        tmpRect = scrollTrayRect();
        if (axis == Scrollable.HORIZONTAL) {
            x = tmpRect.x + scrollValue;
            y = tmpRect.y;
            width = knobLength;
            height = tmpRect.height;
        } else {
            x = tmpRect.x;
            y = tmpRect.y + scrollValue;
            width = tmpRect.width;
            height = knobLength;
        }

        tmpRect.setBounds(x, y, width, height);

        return tmpRect;
    }
    /** Sets the length of the ScrollBar's scroll knob. You never call this
      * method, but if your ScrollBar subclass does not want a proportional
      * scroll knob, you can override it to prevent the ScrollBar's knob length
      * from changing.
      */
    public void setKnobLength(int newKnobLength) {
        int     scrollTrayLength;

        scrollTrayLength = scrollTrayLength();
        if (newKnobLength < minKnobLength()) {
            newKnobLength = minKnobLength();
        } else if (newKnobLength > scrollTrayLength) {
            newKnobLength = scrollTrayLength;
        }
        knobLength = newKnobLength;
    }

    /** Returns the scroll knob's current length.
      * @see #setKnobLength
      */
    public int knobLength() {
        return knobLength;
    }

    /** Returns the scroll knob's minimum length. By default, this is the
      * same as the scroll knob's width or, for a horizontal ScrollBar, its
      * height.
      */
    public int minKnobLength() {
        if (axis == Scrollable.HORIZONTAL) {
            return height() - 2;
        }

        return width() - 2;
    }



/* actions */


    /** Enables or disables the ScrollBar and its constituent parts. A disabled
      * ScrollBar does not respond to mouse clicks and does not display its
      * scroll Buttons or knob.
      */
    public void setEnabled(boolean value) {
        if (value == enabled) {
            return;
        }

        enabled = value;

        if (active && enabled) {
            addParts();
        } else if (!enabled) {
            removeParts();
        } else {
            return;
        }

        setDirty(true);

        if(scrollBarOwner != null)  {
            if(enabled)
                scrollBarOwner.scrollBarWasEnabled(this);
            else
                scrollBarOwner.scrollBarWasDisabled(this);
        }

    }

    /** Returns <b>true</b> if the ScrollBar is enabled.
      * @see #setEnabled
      */
    public boolean isEnabled() {
        return enabled;
    }

   /** Activates or deactivates the ScrollBar and its constituent parts. An
     * inactive ScrollBar does not respond to mouse clicks and does not display
     * its scroll Buttons or knob. ScrollBars automatically become inactive
     * whenever they do not have a Scrollable to send scroll commands to, or
     * when the Scrollable's contentView is smaller than the Scrollable
     * itself (there is nothing to scroll into view). If you want to disable a
     * ScrollBar, call its <B>setEnabled()</B> method.
     * @see #setEnabled
     */
    public void setActive(boolean value) {
        if (value == active) {
            return;
        }

        active = value;

        if (active && enabled) {
            addParts();
        } else if (!active) {
            removeParts();
        }

        setDirty(true);

        if(scrollBarOwner != null)  {
            if(active)
                scrollBarOwner.scrollBarDidBecomeActive(this);
            else
                scrollBarOwner.scrollBarDidBecomeInactive(this);
        }

    }

    /** Returns <b>true</b> if the ScrollBar is active.
      * @see #setActive
      */
    public boolean isActive() {
        return active;
    }



/* drawing */

    /** Draws the ScrollBar's knob and Image within <B>rect</B>. You never
      * call this method directly, but should override
      * it to produce custom knob drawing.
      */
    public void drawViewKnobInRect(Graphics g, Rect rect) {
        Rect    tmpRect;

        BezelBorder.raisedScrollButtonBezel().drawInRect(g, rect);
        g.setColor(Color.lightGray);
        g.fillRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);

        if (knobImage != null) {
            knobImage.drawCentered(g, rect);
        }
    }

    /** Draws the ScrollBar.  Eventually calls <b>drawViewKnobInRect()</b>.
      * You never call this method directly.  Call the ScrollBar's
      * <b>draw()</b> method to draw the ScrollBar.
      * @see #drawViewKnobInRect
      */
    public void drawView(Graphics g) {
        Rect    interiorRect, areaRect, knobRect;
        int     x, y, width, height, maxX, maxY, otherX, otherY,
                beforeY, beforeX, buttonSize;

        /* draw the outside border */
        g.setColor(Color.gray153);
        g.drawLine(0, 0, 0, height() - 1);
        g.drawLine(1, 0, width() - 1, 0);

        g.setColor(Color.gray231);
        g.drawLine(width() - 1, 0, width() - 1, height());
        g.drawLine(0, height() - 1, width() - 1, height() - 1);

        areaRect = scrollTrayRect();

        x = interiorRect().x;
        y = interiorRect().y;
        maxX = interiorRect().maxX();
        maxY = interiorRect().maxY();
        beforeX = areaRect.x;
        beforeY = areaRect.y;
        otherX = areaRect.x + areaRect.width;
        otherY = areaRect.y + areaRect.height;

        if (!isEnabled() || !isActive()) {
            if (axis == Scrollable.HORIZONTAL) {
                g.setColor(Color.lightGray);
                g.fillRect(x + 1, y + 1, beforeX - 3, maxY - 3);
                g.fillRect(otherX + 1, y + 1, maxX - otherX - 2, maxY - 3);
                g.fillRect(areaRect.x + 1, areaRect.y + 1,
                           areaRect.width - 2, areaRect.height - 2);

                g.setColor(Color.gray153);
                g.drawLine(x, maxY - 1, maxX - 1, maxY - 1);
                g.drawLine(maxX - 1, y, maxX - 1, maxY - 2);
                g.drawLine(beforeX - 1, y, beforeX - 1, maxY - 2);
                g.drawLine(otherX - 1, y, otherX - 1, maxY - 2);

                // draw the decrease arrow
                g.drawLine(x + 5, y + 7, x + 5, y + 8);
                g.drawLine(x + 6, y + 6, x + 6, y + 9);
                g.drawLine(x + 7, y + 5, x + 7, y + 10);
                g.drawLine(x + 8, y + 4, x + 8, y + 11);
                g.drawLine(x + 9, y + 3, x + 9, y + 12);

                // draw the increase arrow
                g.drawLine(otherX + 6, y + 3, otherX + 6, y + 12);
                g.drawLine(otherX + 7, y + 4, otherX + 7, y + 11);
                g.drawLine(otherX + 8, y + 5, otherX + 8, y + 10);
                g.drawLine(otherX + 9, y + 6, otherX + 9, y + 9);
                g.drawLine(otherX + 10, y + 7, otherX + 10, y + 8);

                g.setColor(Color.gray231);
                g.drawLine(x, y, x, maxY - 2);
                g.drawLine(x + 1, y, beforeX - 2, y);
                g.drawLine(beforeX, y, beforeX, maxY - 2);
                g.drawLine(beforeX + 1, y, otherX - 2, y);
                g.drawLine(otherX, y, otherX, maxY - 2);
                g.drawLine(otherX + 1, y, maxX - 2, y);
            } else {
                g.setColor(Color.lightGray);
                g.fillRect(x + 1, y + 1, maxX - 3, beforeY - 3);
                g.fillRect(x + 1, otherY + 1, maxX - 3, maxY - otherY - 2);
                g.fillRect(areaRect.x + 1, areaRect.y + 1,
                           areaRect.width - 2, areaRect.height - 2);

                g.setColor(Color.gray153);
                g.drawLine(maxX - 1, y, maxX - 1, maxY - 1);
                g.drawLine(x, maxY - 1, maxX - 2, maxY - 1);
                g.drawLine(x, beforeY - 1, maxX - 2, beforeY - 1);
                g.drawLine(x, otherY - 1, maxX - 2, otherY - 1);

                // draw the increase arrow
                g.drawLine(x + 7, y + 5, x + 8, y + 5);
                g.drawLine(x + 6, y + 6, x + 9, y + 6);
                g.drawLine(x + 5, y + 7, x + 10, y + 7);
                g.drawLine(x + 4, y + 8, x + 11, y + 8);
                g.drawLine(x + 3, y + 9, x + 12, y + 9);

                // draw the decrease arrow
                g.drawLine(x + 3, otherY + 6, x + 12, otherY + 6);
                g.drawLine(x + 4, otherY + 7, x + 11, otherY + 7);
                g.drawLine(x + 5, otherY + 8, x + 10, otherY + 8);
                g.drawLine(x + 6, otherY + 9, x + 9, otherY + 9);
                g.drawLine(x + 7, otherY + 10, x + 8, otherY + 10);

                g.setColor(Color.gray231);
                g.drawLine(x, y, maxX - 2, y);
                g.drawLine(x, y + 1, x, beforeY - 2);
                g.drawLine(x, beforeY, maxX - 2, beforeY);
                g.drawLine(x, beforeY + 1, x, otherY - 2);
                g.drawLine(x, otherY, maxX - 2, otherY);
                g.drawLine(x, otherY + 1, x, maxY - 2);
            }
            Rect.returnRect(areaRect);
            return;
        }

        if (knobLength <= scrollTrayLength()) {
            /// There is a knob that can be scrolled in the tray, draw
            /// the tray and the kob properly.
            x = areaRect.x;
            maxX = areaRect.maxX();
            y = areaRect.y;
            maxY = areaRect.maxY();

            if (axis == Scrollable.HORIZONTAL) {
                beforeX = x + scrollValue - 1;
                otherX = x + scrollValue + knobLength;

                if (scrollValue > 0) {
                    g.setColor(Color.gray153);
                    g.drawLine(x, y, beforeX, y);
                    g.drawLine(x, y + 1, x, maxY - 2);

                    trayTopImage.drawTiled(g, x + 1, y + 1, beforeX - x, 1);
                    trayBottomImage.drawTiled(g, x + 1, maxY - 1, beforeX - x, 1);

                    trayLeftImage.drawTiled(g, x + 1, y + 1, 1, maxY - 3);

                    g.setColor(Color.lightGray);
                    g.fillRect(x + 2, y + 2, beforeX - x - 1, maxY - y - 3);
                }
                if (otherX < maxX) {
                    g.setColor(Color.gray153);
                    g.drawLine(otherX, y, maxX - 2, y);
                    g.drawLine(maxX - 1, y, maxX - 1, maxY - 1);

                    trayTopImage.drawTiled(g, otherX, y + 1, maxX - otherX - 1, 1);
                    trayBottomImage.drawTiled(g, otherX, maxY - 1,
                                              maxX - otherX - 1, 1);

                    trayLeftImage.drawTiled(g, otherX, y + 1, 1, maxY - 3);
                    trayRightImage.drawTiled(g, maxX - 2, y + 1, 1, maxY - 3);

                    g.setColor(Color.lightGray);
                    g.fillRect(otherX + 1, y + 2, maxX - otherX - 3, maxY - y - 3);
                }
            } else {
                beforeY = y + scrollValue - 1;
                otherY = y + scrollValue + knobLength;

                if (scrollValue > 0) {
                    g.setColor(Color.gray153);
                    g.drawLine(x, y + 1, x, beforeY);
                    g.drawLine(x, y, maxX - 1, y);

                    trayTopImage.drawTiled(g, x + 1, y + 1, maxX - 2, 1);

                    trayLeftImage.drawTiled(g, x + 1, y + 1, 1, beforeY - y);
                    trayRightImage.drawTiled(g, maxX - 1, y + 1, 1, beforeY - y);

                    g.setColor(Color.lightGray);
                    g.fillRect(x + 2, y + 2, maxX - x - 3, beforeY - y - 1);
                }
                if (otherY < maxY) {
                    g.setColor(Color.gray153);
                    g.drawLine(x, otherY, x, maxY - 2);
                    g.drawLine(x, maxY - 1, maxX - 1, maxY - 1);

                    trayTopImage.drawTiled(g, x + 1, otherY, maxX - x - 2, 1);
                    trayBottomImage.drawTiled(g, x + 1, maxY - 2, maxX - x - 1, 1);

                    trayLeftImage.drawTiled(g, x + 1, otherY, 1,
                                            maxY - otherY - 2);
                    trayRightImage.drawTiled(g, maxX - 1, otherY, 1,
                                             maxY - otherY - 2);

                    g.setColor(Color.lightGray);
                    g.fillRect(x + 2, otherY + 1, maxX - x - 3, maxY - otherY - 3);
                }
            }
            Rect.returnRect(areaRect);

            /* draw the knob */
            knobRect = knobRect();
            drawViewKnobInRect(g, knobRect);
            Rect.returnRect(knobRect);
        } else  {
            // There is not enough room to display the knob, draw the filler
            if (axis == Scrollable.HORIZONTAL) {
                g.setColor(Color.lightGray);
                g.fillRect(areaRect.x + 1, areaRect.y + 1,
                           areaRect.width - 2, areaRect.height - 2);

                g.setColor(Color.gray102);
                g.drawLine(x, maxY - 1, maxX - 1, maxY - 1);    // Bottom
                g.drawLine(otherX - 1, y, otherX - 1, maxY - 2);    // Right

                g.setColor(Color.gray231);
                g.drawLine(beforeX, y, beforeX, maxY - 2);  // Left
                g.drawLine(beforeX + 1, y, otherX - 2, y);  // Top
            } else {
                g.setColor(Color.lightGray);
                g.fillRect(areaRect.x + 1, areaRect.y + 1,
                           areaRect.width - 2, areaRect.height - 2);

                g.setColor(Color.gray102);
                g.drawLine(maxX - 1, y, maxX - 1, maxY - 1);    // Right Side
                g.drawLine(x, otherY - 1, maxX - 2, otherY - 1);    // Bottom

                g.setColor(Color.gray231);
                g.drawLine(x, beforeY, maxX - 2, beforeY);  // Top Side
                g.drawLine(x, beforeY + 1, x, otherY - 2);  // Left side
            }
        }
    }

    /** Redraws the ScrollBar's scroll tray and knob by computing their
      * bounding rect and passing that to <b>draw()</b>.
      */
    public void drawScrollTray() {
        Rect    tmpRect;

        tmpRect = scrollTrayRect();
        addDirtyRect(tmpRect);
        Rect.returnRect(tmpRect);
    }



/* setting the knob's position and size */


    void _setScrollValue(int newValue) {
        if (newValue < 0) {
            scrollValue = 0;
        } else if (newValue > _maxScrollValue()) {
            scrollValue = _maxScrollValue();
        } else {
            scrollValue = newValue;
        }
    }

    void _setScrollPercent(float percent) {
        int     value;

        if(percent < 0.0f || percent > 1.0f)
            return;
        value = (int)(percent * (float)_maxScrollValue());
        if (value != 0 || percent == 0.0f) {
            _setScrollValue(value);
        } else {
            _setScrollValue((int)Math.ceil(percent *
                                                (float)_maxScrollValue()));
        }
    }

    void _setPercentVisible(float percent) {
        int     scrollTrayLength;
        int     newKnobLength;

        scrollTrayLength = scrollTrayLength();
        newKnobLength = (int)(percent * scrollTrayLength);
        setKnobLength(newKnobLength);

        if(scrollTrayLength < 1
            || scrollTrayLength <= newKnobLength)
            setActive(false);
        else
            setActive(true);
    }

    void _computeScrollValue() {
        int     scrollX,  scrollWidth, delta, contentX, contentWidth,
                oldScrollValue, oldKnobLength, theAxis;
        boolean oldActive;

        oldScrollValue = scrollValue;
        oldKnobLength = knobLength;
        oldActive = active;
        scrollX = 0;

        if(scrollableView != null) {
            theAxis = axis();
            scrollWidth = scrollableView.lengthOfScrollViewForAxis(theAxis);
            contentWidth = scrollableView.lengthOfContentViewForAxis(theAxis);
            contentX = scrollableView.positionOfContentViewForAxis(theAxis);
        } else {
            scrollWidth = 0;
            contentWidth = 0;
            contentX = 0;
        }

        if (contentWidth == 0) {
            _setPercentVisible(1.0f);
        } else {
            _setPercentVisible((float)scrollWidth / (float)contentWidth);
        }

        delta = contentWidth - scrollWidth;
        if (delta <= 0) {
            _setScrollPercent(0.0f);
        } else {
            _setScrollPercent((float)(scrollX - contentX) / (float)delta);
        }

        /* This logic always returns false during normal operation.
         * The scrollView is telling us to update, after we've told it
         * to move. I think we already have the changes and assume nothing
         * has changed. I think this may still need to be here to handle
         * the case where the user is selecting text down, past the
         * scrollView's visible region and we need to track the selection end.
         * Just a quick Look Through - ddk ALERT!
         */

        if ((scrollValue != oldScrollValue
             || knobLength != oldKnobLength
             || active != oldActive)
            && isActive()
            && isEnabled()) {

           drawScrollTray();
        }
    }

    /** Returns the ScrollBar's "scroll value," the pixel position of its
      * scroll knob.
      */
    public int scrollValue() {
        return scrollValue;
    }

    int pixelScrollValue() {
        return pixelScrollValue;
    }

    int _maxScrollValue() {
        return scrollTrayLength() - knobLength;
    }

   /** Convenience method for retrieving the scroll value as a fraction between
     * 0 and 1.
     */
    public float scrollPercent() {
        int     maxValue;

        maxValue = _maxScrollValue();
        if (maxValue == 0) {
            return 0.0f;
        }

        return (float)scrollValue() / (float)maxValue;
    }

    int _mouseValue(MouseEvent event) {
        Rect    scrollTrayRect;
        int     value;

        scrollTrayRect = scrollTrayRect();
        value = (axis == Scrollable.HORIZONTAL) ? event.x - scrollTrayRect.x :
                              event.y - scrollTrayRect.y;
        Rect.returnRect(scrollTrayRect);

        return value;
    }

    /** Overridden to allow the user to click in the scroll tray or click and
      * drag the scroll knob.
      */
    public boolean mouseDown(MouseEvent event) {
        int     oldScrollValue, delta;

        if (!isEnabled() || !isActive()) {
            return false;
        }

        lastMouseValue = lastAltMouseValue = _mouseValue(event);

        if (event.isMetaKeyDown()
            && ((lastMouseValue >= scrollValue + knobLength)
                || (lastMouseValue < scrollValue))) {
            oldScrollValue = scrollValue;
            _setScrollValue(lastMouseValue - knobLength / 2);
            delta = scrollValue - oldScrollValue;

            if (delta != 0) {
                scrollToCurrentPosition();
            }
        } else if (lastMouseValue < scrollValue) {
            scrollPageBackward();
            timer = new Timer(this, TIMER_SCROLL_PAGE, 75);
            timer.setInitialDelay(300);
            if (lastMouseValue < scrollValue) {
                timer.start();
            } else if (lastMouseValue < scrollValue + (knobLength)) {
                timer = null;
            }
        } else if (lastMouseValue >= scrollValue + (knobLength)) {
            scrollPageForward();
            timer = new Timer(this, TIMER_SCROLL_PAGE, 75);
            timer.setInitialDelay(300);
            if (lastMouseValue >= scrollValue - (knobLength)) {
                timer.start();
            } else if (lastMouseValue >= scrollValue) {
                timer = null;
            }
        }

        origScrollValue = scrollValue;

        return true;
    }

    /** Overridden to allow the user to click in the scroll tray or click and
      * drag the scroll knob.
      */
    public void mouseDragged(MouseEvent event) {
        String          command;
        int             value, delta, oldScrollValue;

        value = _mouseValue(event);

        if (!isEnabled() || !isActive() || timer != null) {
            lastMouseValue = value;
            origScrollValue = scrollValue;
            return;
        }

        if (event.isControlKeyDown()) {
            pixelScrollValue = lastAltMouseValue - value;
            scrollByPixel(pixelScrollValue);
            lastAltMouseValue = value;
            origScrollValue = scrollValue;

            return;
        }

        lastAltMouseValue = value;

        oldScrollValue = scrollValue;
        _setScrollValue(origScrollValue + (value - lastMouseValue));
        delta = scrollValue - oldScrollValue;

        if (delta != 0) {
            scrollToCurrentPosition();
        }
    }

    private void timerScroll() {
        if (lastMouseValue < scrollValue) {
            scrollPageBackward();
            if (lastMouseValue >= scrollValue) {
                timer.stop();
                if (lastMouseValue < scrollValue + (knobLength)) {
                    timer = null;
                }
            }
        } else if (lastMouseValue >= scrollValue + (knobLength)) {
            scrollPageForward();
            if (lastMouseValue < scrollValue - (knobLength)) {
                timer.stop();
                if (lastMouseValue >= scrollValue) {
                    timer = null;
                }
            }
        }
        origScrollValue = scrollValue;
    }

    /** Overridden to allow the user to click in the scroll tray or click and
      * drag the scroll knob.
      */
    public void mouseUp(MouseEvent event) {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /** Implements the ScrollBar's commands.
      */
    public void performCommand(String command, Object data) {
        if (TIMER_SCROLL_PAGE.equals(command)) {
            timerScroll();
        } else if(UPDATE.equals(command)) {
            update();
        } else if(SCROLL_LINE_FORWARD.equals(command))  {
            scrollLineForward();
        } else if(SCROLL_LINE_BACKWARD.equals(command))  {
            scrollLineBackward();
        } else if(SCROLL_PAGE_FORWARD.equals(command))  {
            scrollPageForward();
        } else if(SCROLL_PAGE_BACKWARD.equals(command))  {
            scrollPageBackward();
        } else  {
            throw new NoSuchMethodError("unknown command: " + command);
        }
    }

    private void update() {
        int             oldScrollValue, oldKnobLength;
        boolean         oldActive;

        /* have to force the drawing, even though _computeScrollValue()
         * makes the same call - not sure why
         * See comment under _computeScrollValue for current theory - ddk
         */
        oldScrollValue = scrollValue;
        oldKnobLength = knobLength;
        oldActive = active;

        oldActive = active;
        _computeScrollValue();

        if (oldActive != active) {
            setDirty(true);
        } else if (shouldRedraw || (scrollValue != oldScrollValue ||
                    knobLength != oldKnobLength || active != oldActive) &&
                    isActive() && isEnabled()) {
            drawScrollTray();
        }
    }

/* archiving */


    /** Describes the ScrollBar class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);

        info.addClass("netscape.application.ScrollBar", 2);
        info.addField(SCROLLVIEW_KEY, OBJECT_TYPE);
        info.addField(INCREASEBUTTON_KEY, OBJECT_TYPE);
        info.addField(DECREASEBUTTON_KEY, OBJECT_TYPE);
        info.addField(KNOBIMAGE_KEY, OBJECT_TYPE);
        info.addField(SCROLLVALUE_KEY, INT_TYPE);
        info.addField(AXIS_KEY, INT_TYPE);
        info.addField(ACTIVE_KEY, BOOLEAN_TYPE);
        info.addField(ENABLED_KEY, BOOLEAN_TYPE);
        info.addField(LINE_INCREMENT_KEY, INT_TYPE);
        info.addField(OWNER_KEY, OBJECT_TYPE);
    }

    /** Encodes the ScrollBar instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);

        encoder.encodeObject(SCROLLVIEW_KEY, scrollableView);
        encoder.encodeObject(INCREASEBUTTON_KEY, increaseButton);
        encoder.encodeObject(DECREASEBUTTON_KEY, decreaseButton);
        encoder.encodeObject(KNOBIMAGE_KEY, knobImage);
        encoder.encodeInt(SCROLLVALUE_KEY, scrollValue);
        encoder.encodeInt(AXIS_KEY, axis);
        encoder.encodeBoolean(ACTIVE_KEY, active);
        encoder.encodeBoolean(ENABLED_KEY, enabled);
        encoder.encodeInt(LINE_INCREMENT_KEY, lineIncrement);
        encoder.encodeObject(OWNER_KEY, scrollBarOwner);
    }

    /** Decodes the ScrollBar instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        int version = decoder.versionForClassName("netscape.application.ScrollBar");
        super.decode(decoder);

        scrollableView = (Scrollable)decoder.decodeObject(SCROLLVIEW_KEY);
        increaseButton = (Button)decoder.decodeObject(INCREASEBUTTON_KEY);
        decreaseButton = (Button)decoder.decodeObject(DECREASEBUTTON_KEY);
        knobImage = (Image)decoder.decodeObject(KNOBIMAGE_KEY);
        scrollValue = decoder.decodeInt(SCROLLVALUE_KEY);
        axis = decoder.decodeInt(AXIS_KEY);
        active = decoder.decodeBoolean(ACTIVE_KEY);
        enabled = decoder.decodeBoolean(ENABLED_KEY);

        if(version >= 2)    {
            lineIncrement = decoder.decodeInt(LINE_INCREMENT_KEY);
            scrollBarOwner = (ScrollBarOwner)decoder.decodeObject(OWNER_KEY);
        } else  {
            lineIncrement = DEFAULT_LINE_INCREMENT;
            scrollBarOwner = null;
        }

        _computeScrollValue();
    }


    /** Returns the axis controlled by this ScrollBar. */
    public int axis()   {
        return axis;
    }

    /** Returns the ScrollBar's page size as a percentage of the
      * <b>lengthOfScrollViewForAxis()</b> value.
      * @see #setPageSizeAsPercent
      * @see Scrollable#lengthOfScrollViewForAxis
      */
    public float pageSizeAsPercent()    {
        return pageSizeAsPercent;
    }

    /** Sets the ScrollBar's page size. When asked to scroll
      * one page, the ScrollView determines the number of pixels
      * to scroll by multiplying this percentage by the appropriate axis
      * length.
      * @see #setPageSizeAsPercent
      */
    public void setPageSizeAsPercent(float value)   {
        if (value <= 0.0f || value > 1.0f) {
            return;
        }
        pageSizeAsPercent = value;
    }

    /** Sets the ScrollBar's line increment. This line increment
      * is the number of pixels that the ScrollBar scrolls the Scrollable
      * object when asked to scroll by lines.
      */
    public void setLineIncrement(int value) {
        if(value > 0)   {
            lineIncrement = value;
        }
    }

    /** Returns the ScrollBar's line increment.
      * @see #setLineIncrement
      */
    public int lineIncrement()  {
        return lineIncrement;
    }

/* Scrolling control methods */

    /** Primitive method that sends the scrollTo method to the Scrolling view.
      * Handy place to wrap the shouldRedraw sets
      */
    private void scrollTo(int x, int y)  {
        shouldRedraw = true;
        if(scrollableView != null)
            scrollableView.scrollTo(x,y);
        shouldRedraw = false;
    }

    /** Primitive method that sends the scrollBy method to the Scrolling view.
      * Handy place to wrap the shouldRedraw sets
      */
    private void scrollBy(int deltaX, int deltaY)  {
        shouldRedraw = true;
        if(scrollableView != null) {
            scrollableView.scrollBy(deltaX, deltaY);
        }
        shouldRedraw = false;
    }

    /** Scrolls the Scrollable object the amount of pixels along the axis.
      */
    private void scrollByPixel(int amount) {
        if(scrollableView != null) {
            if(axis == Scrollable.HORIZONTAL)
                scrollBy(amount,0);
            else
                scrollBy(0,amount);
        }
    }

    /** Scrolls the Scrollable object the amount of lines along the axis.
      */
    private void scrollByLine(int amount)  {
        if(scrollableView != null) {
            if(axis == Scrollable.HORIZONTAL)
                scrollBy(amount*lineIncrement,0);
            else
                scrollBy(0,amount*lineIncrement);
        }
    }

    /** Scrolls the Scrollable object the amount of pages along the axis.
      * Amount is some percent of a page. Normally this is defaulted to 1.0f
      * to scroll a single page.
      */
    private void scrollByPage(float amount)  {
        if(scrollableView != null) {
            if(axis == Scrollable.HORIZONTAL)
                scrollBy((int)(amount*scrollableView.lengthOfScrollViewForAxis(axis)),0);
            else
                scrollBy(0,(int)(amount*scrollableView.lengthOfScrollViewForAxis(axis)));
        }
    }

    /** Scrolls the Scrollable object to a specific amount relative to the top
      * or left of the Scrollable object.
      */
    private void scrollToPercent(float amount) {
        int scrollLen, contentLen, contentPosition, newAxis;
        if(scrollableView != null) {
            scrollLen = scrollableView.lengthOfContentViewForAxis(axis);
            contentLen = scrollableView.lengthOfScrollViewForAxis(axis);
            newAxis = -(int)(amount * (float)(scrollLen  - contentLen));
            if(axis == Scrollable.HORIZONTAL)
                scrollTo(newAxis,scrollableView.positionOfContentViewForAxis(Scrollable.VERTICAL));
            else
                scrollTo(scrollableView.positionOfContentViewForAxis(Scrollable.HORIZONTAL),newAxis);
        }
    }

    /** Scrolls the Scrollable object the appropriate direction by
      * <b>lineIncrement()</b> pixels. Forward is to the right or down.
      * @see #lineIncrement
      */
    public void scrollLineForward() {
        scrollByLine(-1);
    }

    /** Scrolls the Scrollable object the appropriate direction by
      * <b>lineIncrement()</b> pixels. Backward is to the left or up.
      * @see #lineIncrement
      */
    public void scrollLineBackward()    {
        scrollByLine(1);
    }

    /** Scrolls the Scrollable object the appropriate direction by
      *<b>pageSizeAsPercent()</b> pixels. Forward is to the right or down.
      * @see #pageSizeAsPercent
      */
    public void scrollPageForward() {
        scrollByPage(-pageSizeAsPercent);
    }

    /** Scrolls the Scrollable object the appropriate direction by
      * <b>pageSizeAsPercent()</b> pixels. Backward is to the left or up.
      * @see #pageSizeAsPercent
      */
    public void scrollPageBackward()    {
        scrollByPage(pageSizeAsPercent);
    }

    /** Scrolls the Scrollable object to match the current value returned by
      * <b>scrollPercent()</b>.
      * @see #scrollPercent
      */
    public void scrollToCurrentPosition()   {
        scrollToPercent(scrollPercent());
    }

    /** Scrolls <b>value</b> percent into the contentView.
      * @see #scrollPercent
      */
    public void setScrollPercent(float value)   {
        _setScrollPercent(value);
        scrollToCurrentPosition();
    }

    void _setupKeyboard() {
        removeAllCommandsForKeys();
        if(axis() == Scrollable.HORIZONTAL) {
            setCommandForKey(SCROLL_LINE_FORWARD,KeyEvent.RIGHT_ARROW_KEY,View.WHEN_IN_MAIN_WINDOW);
            setCommandForKey(SCROLL_LINE_BACKWARD,KeyEvent.LEFT_ARROW_KEY,View.WHEN_IN_MAIN_WINDOW);
        } else {
            setCommandForKey(SCROLL_LINE_FORWARD,KeyEvent.DOWN_ARROW_KEY,View.WHEN_IN_MAIN_WINDOW);
            setCommandForKey(SCROLL_LINE_BACKWARD,KeyEvent.UP_ARROW_KEY,View.WHEN_IN_MAIN_WINDOW);
        }
    }

    /** Overridden to return <b>true</b>.
      *
      */
    public boolean hidesSubviewsFromKeyboard() {
        return true;
    }
}

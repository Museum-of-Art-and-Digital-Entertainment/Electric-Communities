package ec.ifc.app;

import java.util.Vector;

import netscape.application.Button;
import netscape.application.Color;
import netscape.application.Graphics;
import netscape.application.Image;
import netscape.application.MouseEvent;
import netscape.application.Rect;
import netscape.application.Target;
import netscape.application.Timer;
import netscape.application.View;

import ec.e.run.Trace;


/** 
 * View subclass that provides a way to choose an item from a set with
 * an appearance reminiscent of index tabs in a notebook. The API is based
 * on that of ListView, though somewhat simpler.<P>
 *
 * Typical usage is to create an ECTabView and modify its prototypeItem as
 * desired before adding items. Each added item should be given a title,
 * and optionally a command string. When the user clicks a tab, this command
 * string will be sent to the ECTabView's target, if any. Alternatively,
 * the tab items can have no commands, in which case the ECTabView's command
 * will be sent when the user clicks any tab. Then it is up to the target to
 * check which tab item is selected.<P>
 *
 * The drawing in this class is very simple and not particularly attractive.
 * At the moment, we're only instantiating a subclass that does fancier
 * drawing, so it's not worth the effort to make this one draw nicely. Maybe
 * this will change someday.
 */
public class ECTabView extends View implements Target, ECTipViewOwner  {

    //
    // constants
    //
    /** tabs are above the information that they swap in and out */
    public static final int ABOVE_CONTENT = 0;
    /** tabs are below the information that they swap in and out */
    public static final int BELOW_CONTENT = 1;
    /** tabs are to left of the information that they swap in and out */
    public static final int LEFT_OF_CONTENT = 2;
    /** tabs are to right of the information that they swap in and out */
    public static final int RIGHT_OF_CONTENT = 3;

    /** default tab orientation */
    protected static final int DEFAULT_TAB_LOCATION = LEFT_OF_CONTENT;

    /** command to scroll the tabs up or left */
    private final static String SCROLL_UP_LEFT_COMMAND = "TAB_SCROLL_UP_LEFT";
    /** command to scroll the tabs down or right */
    private final static String SCROLL_DOWN_RIGHT_COMMAND = "TAB_SCROLL_DOWN_RIGHT";
    /** command to animate scrolling redraw */
    private final static String ANIMATE_SCROLLING = "ANIMATE_SCROLLING";

    /** tips */
    private static final String  TIP_TAB_SCROLLBAR = "Click to see more tabs";
    private static final String  TIP_GREEN_TAB_SCROLLBAR = "Click to find the current tab";

    //
    // protected data
    //
    protected boolean    myAutomaticTabLength = true;
    protected ECTabItem  mySelectedItem;
    protected Color      myBackgroundColor;
    protected ECTabItem  myPrototypeItem;
    protected int        myLocation = DEFAULT_TAB_LOCATION;
    protected Vector     myItems = new Vector();
    protected ECButton   myTopLeftButton = new ECButton();
    protected ECButton   myBottomRightButton = new ECButton();

    protected String     myCommand = null;
    protected Target     myTarget = null;
    
    //
    // private data
    //
    private   Rect       myTabRect = new Rect();
    private   boolean    myButtonsVisible = false;  
    private   int        myTabIndex = 0;
    private   int        myTargetTabIndex = 0;
    private   int        myTabLength = 1;
    private   int        mySpacing = 4;
    private   int        myMinTabLength = 1;
    private   boolean    myIsScrolling = false;
    private   int        myButtonDelay = 400;
    private   Timer      myTimer = null;
    private   int        myScrollDelay = 0;
    private   int        myScrollStep = 0;
    private   int        myNumScrollSteps = 3;
    private   boolean    myCanHandleMouse = true;

    private Image myBottomRightImage = null;
    private Image myBottomRightAltImage = null;
    private Image myTopLeftImage = null;
    private Image myTopLeftAltImage = null;

    private Image myBottomRightOtherImage = null;
    private Image myBottomRightOtherAltImage = null;
    private Image myTopLeftOtherImage = null;
    private Image myTopLeftOtherAltImage = null;

    //
    // constructors
    //
    /** Constructs an ECTabView object with bounds 0,0,0,0 */
    public ECTabView() {
        this(0, 0, 0, 0, new ECTabItem(), DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an ECTabView object with the given bounds */
    public ECTabView(Rect bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height, new ECTabItem(), 
            DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an ECTabView object with the given bounds */
    public ECTabView(int x, int y, int width, int height) {
        this(x, y, width, height, new ECTabItem(), DEFAULT_TAB_LOCATION);
    }
    
    /** Constructs an ECTabView object with the given bounds and location */
    public ECTabView(int x, int y, int width, int height, int location) {
        this(x, y, width, height, new ECTabItem(), location);
    }
        
    /** Constructs an ECTabView object with the given bounds and tab item */
    protected ECTabView(int x, int y, int width, int height, ECTabItem item, int location) {
        if (location < ABOVE_CONTENT || location > RIGHT_OF_CONTENT) {
            throw new IllegalArgumentException("invalid location " + location);
        }

        myLocation = location;
        setPrototypeItem(item);

        myTopLeftButton.setTarget(this);
        myTopLeftButton.setCommand(SCROLL_UP_LEFT_COMMAND);
        myTopLeftButton.setType(Button.CONTINUOUS_TYPE);
        myTopLeftButton.setRepeatDelay(myButtonDelay);
        myTopLeftButton.setEnabled(false);
        myTopLeftButton.disableDrawing();
        myTopLeftButton.setVertResizeInstruction(BOTTOM_MARGIN_CAN_CHANGE);
        myTopLeftButton.setHorizResizeInstruction(RIGHT_MARGIN_CAN_CHANGE);

        myBottomRightButton.setTarget(this);
        myBottomRightButton.setCommand(SCROLL_DOWN_RIGHT_COMMAND);
        myBottomRightButton.setType(Button.CONTINUOUS_TYPE);
        myBottomRightButton.setRepeatDelay(myButtonDelay);
        myBottomRightButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
        myBottomRightButton.setHorizResizeInstruction(LEFT_MARGIN_CAN_CHANGE);

        myTimer = new Timer(this, ANIMATE_SCROLLING, myScrollDelay);

        setBounds(x, y, width, height);
    }

    //
    // public class methods
    //
    
    /**
     * Returns whether or not the length (long dimension) of each tab is
     * computed automatically. When this is true, the tabs are sized to exactly
     * fit across the long dimension of the ECTabView object.
     * @see #getTabLength

     */
    public boolean automaticTabLength() {
        return myAutomaticTabLength;
    }
    
    /**
     * Returns the largest result for tabItem.minLength() called on each tab.
     * A common use pattern is to set the minTabLength to the result of this
     * call once, just after installing all the tab items.
     */
    public int calculateMinTabLength()  {
        int result = 1;
        int count = count();
        for (int i = 0; i < count; i += 1) {
            result = Math.max(result, itemAt(i).minLength());
        }
        return result;
    }
    
    //
    // public instance methods
    //

    /** 
     * Adds an additional tab to the end of the ECTabView by cloning the
     * prototype ListItem. Returns the newly-create ECTabItem.
     */
    public ECTabItem addItem() {
        return insertItemAt(myItems.size());
    }

    /** 
     * Adds an additional tab to the end of the ECTabView by cloning the
     * prototype ListItem. Returns the newly-create ECTabItem.
     */
    public ECTabItem addItem(String theTitle) {
        ECTabItem theItem = insertItemAt(myItems.size());
        theItem.setTitle(theTitle);
        return theItem;
    }
 
    /** 
     * Adds an additional tab to the end of the ECTabView by cloning the
     * prototype ListItem. Returns the newly-create ECTabItem.
     */
    public ECTabItem addItem(String theTitle, String theTip) {
        ECTabItem theItem = insertItemAt(myItems.size());
        theItem.setTitle(theTitle);
        theItem.setTip(theTip);
        return theItem;
    }

    /**
     * Adds the ECTabItem <b>item</b> to the end of the ECTabView.
     */
    public ECTabItem addItem(ECTabItem item) {
        return insertItemAt(item, myItems.size());
    }
    
    /**
     * Returns whether or not the scroll buttons are currently visible 
     */
    public boolean buttonsAreVisible() {
        return myButtonsVisible;
    }

    /** 
     * Returns the color used to fill behind the tabs.
     * @see #drawBackground
     */
    public Color backgroundColor() {
        return myBackgroundColor;
    }

    /**
     * Returns the ECTabItem's command. ECTabView sends this command to its
     * target when the user clicks an ECTabItem and that ECTabItem has no
     * command of its own.
     */
    public String command() {
        return myCommand;
    }
    
    /** Returns the number of tabs in the ECTabView */
    public int count() {
        return myItems.size();
    }

    /**
     * Called by drawView before drawing individual tabs. Fills the bounds
     * of the ECTabView with the background color.
     */
    public void drawBackground(Graphics g) {
        if (myBackgroundColor != null) {
            g.setColor(myBackgroundColor);
            g.fillRect(bounds());
        }
    }
    
    /** 
     * Overridden to draw set of labelled tabs.
     * Calls drawBackground() followed by tab.drawInRect() for each tab.
     */
    public void drawView(Graphics g)  {
        if (!myIsScrolling && (myTabIndex == myTargetTabIndex)) {
            drawStatic(g);
        }
        else {
            drawAnimated(g);
        }
    }

    /**
     * Responsibility of ECTipViewOwner, returns corresponding tip string
     */
    public String getTipForPositionInView (View view, int x, int y) {

        if (view == myTopLeftButton) {
            if (myTopLeftButton.image() == myTopLeftOtherImage) {
                return TIP_GREEN_TAB_SCROLLBAR;
            }
            else {
                return TIP_TAB_SCROLLBAR;
            }
        }
        else if (view == myBottomRightButton) {
            if (myBottomRightButton.image() == myBottomRightOtherImage) {
                return TIP_GREEN_TAB_SCROLLBAR;
            }
            else {
                return TIP_TAB_SCROLLBAR;
            }
        }
        else {
            return ((ECTabItem)myItems.elementAt(indexFromPoint(x, y))).tip();
        }
    }

    /**
     * Returns the ECTabView's location with respect to the content that it
     * swaps in and out. This is used to determine the layout and some
     * aspects of the appearance of the individual tabs. Possible values
     * are ECTabView.ABOVE_CONTENT, ECTabView.BELOW_CONTENT,
     * ECTabView.LEFT_OF_CONTENT, ECTabView.RIGHT_OF_CONTENT. Default value
     * is ECTabView.ABOVE_CONTENT.
     */
    public int location() {
        return myLocation;
    }
    
    /**
     * Returns the minimum allowed length (long dimension) of each tab,
     * if tab lengths aren't computed automatically
     * @see #setMinTabLength
     */
    public int minTabLength() {
        return myMinTabLength;
    }
    
    /** 
     * Returns prototype ECTabItem. This item is cloned to create new
     * tab entries when addItem is called with no parameters.
     */
    public ECTabItem prototypeItem() {
        return myPrototypeItem;
    }
    
    /** Adds the ECTabItem to the ECTabView at the given index */
    public ECTabItem insertItemAt(ECTabItem item, int index) {
        item.setTabView(this);
        myItems.insertElementAt(item, index);
        
        if (mySelectedItem == null) {
            selectItem(item);
        }
        
        initializeTabRect();
        return item;
    }
    
    /**
     * Adds an ECTabItem to the ECTabView at the given index by cloning
     * the prototype ECTabItem. Returns the newly-created ECTabItem.
     */
    public ECTabItem insertItemAt(int index) {
        ECTabItem newItem;
        
        newItem = (ECTabItem)myPrototypeItem.clone();

        return insertItemAt(newItem, index);
    }
    
    /** 
     * Returns true if location is ABOVE_CONTENT or BELOW_CONTENT.
     * Used to make layout decisions.
     */
    public boolean isHorizontal() {
        return myLocation == ABOVE_CONTENT || myLocation == BELOW_CONTENT;
    }
    
    /** Overridden to return true if background color is null */
    public boolean isTransparent() {
        return myBackgroundColor == null;
    }
    
    /** Returns the ECTabItem at the given index.
      */
    public ECTabItem itemAt(int index) {
        return (ECTabItem)myItems.elementAt(index);
    }

    /**
     * Selects the tab that was clicked on, then calls
     * <b>sendCommand()</b>
     * @see #sendCommand
     */
    public boolean mouseDown(MouseEvent event) {
        int tabIndex = indexFromPoint(event.x, event.y);

        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem(
                "mouse down in " + TraceUtils.traceDescription(itemAt(tabIndex)));
        }
    
        setSelectedIndex(tabIndex);
        sendCommand();
        
        return true;
    }

    /** Imaplementation of Target interface to scroll the tabs when scroll buttons
     *  are clicked
     */
    public void performCommand(String command, Object sender) {
        if (command.equals(ANIMATE_SCROLLING)) {
            myIsScrolling = true;
            setDirty(true);
            draw();
        }
        else if (command.equals(SCROLL_DOWN_RIGHT_COMMAND) && myCanHandleMouse) {
            int numTabs = count();
            int numTabsDisplayed = numTabsToDraw();

            // there is still room to scroll
            if (myTabIndex < numTabs - numTabsDisplayed) {
                ++myTargetTabIndex;
                myTimer.start();
                myCanHandleMouse = false;
            }

            // reached the lower limit of displayed tabs
            if (myTargetTabIndex >= numTabs - numTabsDisplayed) {
                myBottomRightButton.setEnabled(false);
                myBottomRightButton.disableDrawing();
            }

            // make sure you can scroll in opposite direction
            if (!myTopLeftButton.isEnabled()) {
                myTopLeftButton.setEnabled(true);
                myTopLeftButton.setState(false);
                myTopLeftButton.reenableDrawing();
            }

            checkButtonImages();
        }
        else if (command.equals(SCROLL_UP_LEFT_COMMAND) && myCanHandleMouse) {
            // there is still room to scroll
            if (myTabIndex > 0) {
                --myTargetTabIndex;
                myTimer.start();
                myCanHandleMouse = false;
            }

            // reached the upper limit of displayed tabs
            if (myTargetTabIndex <= 0) {
                myTopLeftButton.setEnabled(false);
                myTopLeftButton.disableDrawing();
            }

            // make sure you can scroll in opposite direction
            if (!myBottomRightButton.isEnabled()) {
                myBottomRightButton.setEnabled(true);
                myBottomRightButton.setState(false);
                myBottomRightButton.reenableDrawing();
            }

            checkButtonImages();
        }
    }

   /** returns index of tab given point (local to view) */
    public int indexFromPoint(int x, int y) {
        int numTabs = count();

        // This could be fancier and actually hit-test inside the polygons
        int index = (isHorizontal() ? x : y) / (myTabLength + mySpacing) + myTabIndex;

        // limit index to sensible values       
        if (index > numTabs - 1) {
            index = numTabs - 1;            
        }
        else if (index < 0) {
            index = 0;
        }
        
        return index;
    }

    /** Removes all ECTabItems from the ECTabView. */
    public void removeAllItems() {
        if ((myTimer != null) && myTimer.isRunning()) {
            myTimer.stop();
        }

        myItems.removeAllElements();
        mySelectedItem = null;
        myTabIndex = 0;
        myTargetTabIndex = 0;
    }
    
    /** Removes the ECTabItem at the given row index from the ECTabView. */
    public void removeItemAt(int index) {
        ECTabItem item;

        item = (ECTabItem)myItems.elementAt(index);
        myItems.removeElementAt(index);

        // if this was the selected item, select the adjacent item      
        if (item == mySelectedItem) {
            if (myItems.size() > 0) {
                index--;
                if (index < 0) {
                    index = 0;
                }
                selectItem((ECTabItem)myItems.elementAt(index));
            }
        }
        
        initializeTabRect();
    }

    /** Removes <b>item</b> from the ECTabView.
      */
    public void removeItem(ECTabItem item) {
        removeItemAt(myItems.indexOf(item));
    }

    /** Returns the index of the selected tab */
    public int selectedIndex() {
        return myItems.indexOf(mySelectedItem);
    }
    
    /** Returns the selected ECTabItem */
    public ECTabItem selectedItem() {
        return mySelectedItem;
    }

    /** Sets which tab is selected */
    public void selectItem(ECTabItem newItem) {
        if (newItem == mySelectedItem) {
            return;
        }
        
        if (mySelectedItem != null) {
            mySelectedItem.setSelected(false);
        }
        
        mySelectedItem = newItem;
        
        if (mySelectedItem != null) {
            mySelectedItem.setSelected(true);
        }
        
        setDirty(true);
    }

    /** Sends the command of the selected ECTabItem to the
      * ECTabView's target. If the selected ECTabItem does not have a command,
      * sends the ECTabView's command.
      * @see #setCommand
      */
    public void sendCommand() {
        if (myTarget != null) {
            String commandToSend = null;

            if (myCommand != null) {
                commandToSend = myCommand;
            }
            else {
                ECTabItem selectedItem = selectedItem();
                
                if (selectedItem != null) {
                    commandToSend = selectedItem.command();
                }
            }

            myTarget.performCommand(commandToSend, this);
        }
    }

    /**
     * Sets the color used to fill the bounds of the ECTabView.
     * @see #getBackgroundColor
     */
    public void setBackgroundColor(Color newBackgroundColor) {
        if (myBackgroundColor == newBackgroundColor) {
            return;
        }
        
        myBackgroundColor = newBackgroundColor;
        setDirty(true);
    }

    /** Overridden to recompute tab sizes */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        // position buttons appropriately for new bounds
        setTopLeftButtonBounds();
        setBottomRightButtonBounds();

        initializeTabRect();
        
    }
    
    /**
     * Sets the ECTabView's command.
     * @see #getCommand
     */
    public void setCommand(String newCommand) {
        myCommand = newCommand;
    }

    /**
     * Sets the image that will be used by bottom/right button when 
     * selected item is "scrolled off"
     */
    public void setOtherBottomRightButtonImage(Image image) {
        myBottomRightOtherImage = image;
    }

    /**
     * Sets the image that will be used by bottom/right pressed button when 
     * selected item is "scrolled off"
     */
    public void setOtherBottomRightButtonAltImage(Image image) {
        myBottomRightOtherAltImage = image;
    }

    /**
     * Sets the image that will be used by top/left button when 
     * selected item is "scrolled off"
     */
    public void setOtherTopLeftButtonImage(Image image) {
        myTopLeftOtherImage = image;
    }

    /**
     * Sets the image that will be used by top/left pressed button when 
     * selected item is "scrolled off"
     */
    public void setOtherTopLeftButtonAltImage(Image image) {
        myTopLeftOtherAltImage = image;
    }

    /**
     * Sets the prototype tab item.
     * @see #getPrototypeItem
     */
    public void setPrototypeItem(ECTabItem newPrototypeItem) {
        myPrototypeItem = newPrototypeItem;
    }
    
    /**
     * Sets distance between tabs. Values less than zero
     * are pinned to zero.
     * @see #getSpacing
     */
    public void setSpacing(int newSpacing) {
        newSpacing = Math.max(newSpacing, 0);
        if (newSpacing == mySpacing) {
            return;
        }
            
        mySpacing = newSpacing;
        initializeTabRect();
    }
    
    /**
     * Sets which tab is selected by calling selectItem() on the
     * item at the specified index
     */
    public void setSelectedIndex(int newIndex) {
        selectItem((ECTabItem)myItems.elementAt(newIndex));
        checkButtonImages();
    }
    
    /**
     * Sets whether or not tab lengths are computed automatically. If
     * this is set to false, use <b>setTabLength</b> to set an explicit length.
     */
    public void setAutomaticTabLength(boolean newValue) {
        if (newValue == myAutomaticTabLength) {
            return;
        }
        
        myAutomaticTabLength = newValue;

        // recompute tab lengths and dirty
        initializeTabRect();
        
        // if turning this off, no need to redraw since the old tab length
        // value will be used in a static way now
    }
    
    /**
     * Sets the "pressed" bitmap image of bottom/right push button on the edges, if it is present
     */
    public void setBottomRightButtonAltImage(Image anImage) {
        myBottomRightButton.setAltImage(anImage);
        myBottomRightAltImage = anImage;
    }

    /**
     * Sets the bitmap image of bottom/right push button on the edges, if it is present
     */
    public void setBottomRightButtonImage(Image anImage) {
        myBottomRightButton.setImage(anImage);
        myBottomRightImage = anImage;
        setBottomRightButtonBounds();
    }

    /** 
     * Sets the minimum length (long dimension) of each tab. This is ignored if
     * tab lengths are computed automatically.
     * @see #getMinTabLength
     * @see #getAutomaticTabLength
     */
    public void setMinTabLength(int newLength) {
        if (myAutomaticTabLength || (newLength == myMinTabLength)) {
            return;
        }
        
        myMinTabLength = newLength;
        initializeTabRect();
    }
    
    /**
     * Sets the number of animated drawing steps it performs when scrolling
     */
    public void setNumberScrollingSteps(int number) {
        myNumScrollSteps = number;
    }

    /**
     * Sets repeat delay for scroll buttons so that when they are pressed
     * they call performCommand every time the delay expired,
     */
    public void setRepeatDelay(int delay) {
        if (myButtonDelay != delay) {
            myButtonDelay = delay;
            myTopLeftButton.setRepeatDelay(delay);
            myBottomRightButton.setRepeatDelay(delay);
        }
    }

    /**
     * Sets the ECTabView's Target.
     * @see #getTarget
     */
    public void setTarget(Target newTarget) {
        myTarget = newTarget;
    }

    /**
     * Sets the "pressed" bitmap image of top/left push button on the edges, if it is present
     */
    public void setTopLeftButtonAltImage(Image anImage) {
        myTopLeftButton.setAltImage(anImage);
        myTopLeftAltImage = anImage;
    }

    /**
     * Sets the bitmap image of top/left push button
     */
    public void setTopLeftButtonImage(Image anImage) {
        myTopLeftButton.setImage(anImage);
        myTopLeftImage = anImage;
        setTopLeftButtonBounds();
    }

    /** Returns distance between tabs. Default value is 10 (pixels). */
    public int spacing() {
        return mySpacing;
    }

    /**
     * Returns the length (long dimension) of each tab. All tabs have this
     * same length. This value is valid whether or not tab lengths are
     * computed automatically.
     * @see #getAutomaticTabLength
     * @see #setMinTabLength
     */
    public int tabLength() {
        return myTabLength;
    }
    
    /**
     * Returns the ECTabView's target. The target is the object to which a
     * command will be sent when the user clicks on a tab.
     */
    public Target target() {
        return myTarget;
    }

    //
    // private instance methods
    //
    
    /** 
     * Determines the actual size that is being used by the tabs (excluding scroll buttons) 
     */
    private int actualTabViewLength() {
        int tabViewLength;

        if (isHorizontal()) {
            tabViewLength = width();
            if (myButtonsVisible) {
                tabViewLength -= myTopLeftButton.width() + myBottomRightButton.width();
            }
        }
        else {
            tabViewLength = height();
            if (myButtonsVisible) {
                tabViewLength -= myTopLeftButton.height() + myBottomRightButton.height();
            }
        }

        return tabViewLength;
    }

    /** 
     * Computes the length of each tab from the available space and
     * number of tabs.
     */
    private int calculateTabLength() {
        int numTabs = count();
        int tabViewLength = actualTabViewLength();
        int tabLength = (tabViewLength - mySpacing * (numTabs - 1)) / numTabs;

        if (myAutomaticTabLength) {
            return tabLength;
        }
        else {
            --numTabs;

            while ((tabLength < myMinTabLength) && (numTabs > 0)) {
                tabLength = (tabViewLength - mySpacing * (numTabs - 1)) / numTabs;
                if (tabLength >= myMinTabLength) {
                    break;
                }
                --numTabs;
            }
                
            return Math.max(tabLength, 1);
        }
    }

    private void checkButtonImages() {
        if ((myTopLeftOtherImage != null) && (myTopLeftOtherAltImage != null)) {
            int   selectedIndex = selectedIndex();
            Image bitmapImage = myTopLeftButton.image();

            if (selectedIndex < myTargetTabIndex) {
                if (bitmapImage != myTopLeftOtherImage) {
                    myTopLeftButton.setImage(myTopLeftOtherImage);
                    myTopLeftButton.setAltImage(myTopLeftOtherAltImage);
                }
            }
            else {
                if (bitmapImage != myTopLeftImage) {
                    myTopLeftButton.setImage(myTopLeftImage);
                    myTopLeftButton.setAltImage(myTopLeftAltImage);
                }
            }
        }

        if ((myBottomRightOtherImage != null) && (myBottomRightOtherAltImage != null)) {
            int   selectedIndex = selectedIndex();
            int   numTabsDisplayed = numTabsToDraw();
            Image bitmapImage = myBottomRightButton.image();

            if (selectedIndex > myTargetTabIndex + numTabsDisplayed - 1) {
                if (bitmapImage != myBottomRightOtherImage) {
                    myBottomRightButton.setImage(myBottomRightOtherImage);
                    myBottomRightButton.setAltImage(myBottomRightOtherAltImage);
                }
            }
            else {
                if (bitmapImage != myBottomRightImage) {
                    myBottomRightButton.setImage(myBottomRightImage);
                    myBottomRightButton.setAltImage(myBottomRightAltImage);
                }
            }
        }
    }
    
    private void drawAnimated(Graphics g) {
        if(++myScrollStep >= myNumScrollSteps) {
            if (myTimer.isRunning()) {
                myTimer.stop();
                myIsScrolling = false;
            }

            if (Trace.gui.usage && Trace.ON) {
                String direction = myTabIndex < myTargetTabIndex
                    ? "forwards"
                    : "backwards";
                Trace.gui.usagem("scrolled tabs " + direction + " in "
                    + TraceUtils.traceDescription(rootView().externalWindow()));
            }           
            
            myScrollStep = 0;
            myTabIndex = myTargetTabIndex;
            drawStatic(g);
            myCanHandleMouse = true;
            
            return;
        }

        Rect  tabRect = new Rect(myTabRect);
        Rect  firstRect = new Rect(tabRect), lastRect = new Rect(tabRect);
        int   firstOffset = 0, lastOffset = 0;
        int   numTabsToDraw = numTabsToDraw();
        int   tabIndex = Math.min(myTabIndex, count() - numTabsToDraw);

        int   deltaX = 0, deltaY = 0;
        int   firstDeltaX = 0, firstDeltaY = 0;

        if (isHorizontal()) {
            deltaX = myTabLength + mySpacing;

            if (tabIndex < myTargetTabIndex) {
                lastRect.width = (tabRect.width * myScrollStep) / myNumScrollSteps;
                firstRect.width = tabRect.width - lastRect.width;

                firstOffset = lastRect.width;
                lastOffset = -firstRect.width;
            }
            else {
                --tabIndex;  // we are going up, and need to start one earlier
                firstRect.width = (tabRect.width * myScrollStep) / myNumScrollSteps;
                lastRect.width = tabRect.width - firstRect.width;

                firstOffset = firstRect.width;
                lastOffset = -lastRect.width;
            }
                
            firstDeltaX = firstRect.width + mySpacing;
        }
        else {
            deltaY = myTabLength + mySpacing;

            if (tabIndex < myTargetTabIndex) {
                lastRect.height = (tabRect.height * myScrollStep) / myNumScrollSteps;
                firstRect.height = tabRect.height - lastRect.height;

                firstOffset = lastRect.height;
                lastOffset = -firstRect.height;
            }
            else {
                --tabIndex;  // we are going up, and need to start one earlier
                firstRect.height = (tabRect.height * myScrollStep) / myNumScrollSteps;
                lastRect.height = tabRect.height - firstRect.height;

                firstOffset = firstRect.height;
                lastOffset = -lastRect.height;
            }
            firstDeltaY = firstRect.height + mySpacing;
        }

        ECTabItem item = (ECTabItem)myItems.elementAt(tabIndex++);
        item.drawInRect(g, firstRect, false);
        tabRect.moveBy(firstDeltaX, firstDeltaY);

        // 1-based because we actually want to draw numTabsToDraw - 1 items
        for (int i = 1; i < numTabsToDraw; i++) {
            item = (ECTabItem)myItems.elementAt(tabIndex++);
            item.drawInRect(g, tabRect, true);
            tabRect.moveBy(deltaX, deltaY);
        }

        lastRect.moveBy(tabRect.x, tabRect.y);
        item = (ECTabItem)myItems.elementAt(tabIndex);
        item.drawInRect(g, lastRect, false);
    }

    int myDebuggingIndex = 0;
    
    private void drawStatic(Graphics g) {
        int numTabs = count();
        int numTabsDisplayed = numTabsToDraw();

        Rect  tabRect = new Rect(myTabRect);

        int   deltaX = 0, deltaY = 0;
        int   tabIndex = Math.min(myTabIndex, numTabs - numTabsDisplayed);
        
        myTargetTabIndex = myTabIndex = tabIndex;

        if (isHorizontal()) {
            deltaX = myTabLength + mySpacing;
            drawBackground(g);
        }
        else {
            deltaY = myTabLength + mySpacing;
        }

        // draw background
        //      drawBackground(g);

        // draw tabs
        for (int i = 0; i < numTabsDisplayed; i++) {
            ECTabItem item = (ECTabItem)myItems.elementAt(tabIndex++);
            item.drawInRect(g, tabRect, true);

            // offset rectangle for each tab
            tabRect.moveBy(deltaX, deltaY);
        }
    }

    /** 
     *  Bottleneck method to be called whenever number of tabs or tab size and
     *  spacing parameters change.  Also being called if resizing has happened.
     */
    private void initializeTabRect() {
        int     numTabs = count();
        
        if (numTabs == 0) {
            myTabRect.setBounds(0, 0, width(), height());
            return;
        }

        // start out without tab buttons in the hope that everything fits       
        removeTabButtons(); 
        myTabLength = calculateTabLength();
        
        if (myAutomaticTabLength) {
            setAutomaticTabRect();
        }
        else {
            boolean isHorizontal = isHorizontal();
            int     tabMaxLength = isHorizontal ? width() : height();
        
            // if items don't fit, make buttons visible and adjust location of myTabRect
            if ((numTabs - 1) * (myTabLength + mySpacing) + myTabLength > tabMaxLength) {

                if (installTabButtons()) {
                    // recalculate myTabLength, as buttons are now visible
                    myTabLength = calculateTabLength();
                }
            }

            // make sure that myTabRect dimensions are set
            if (isHorizontal) {
                myTabRect.width = myTabLength;
                myTabRect.height = height();
            }
            else {
                myTabRect.width = width();
                myTabRect.height = myTabLength;
            }
        }
        
        setDirty(true);
    }

    /**
     * Returns actual number of tabs that are supposed to be drawn
     */
    private int numTabsToDraw() {
        if (count() > 0) {
            int viewLength = actualTabViewLength();
            int result = viewLength / myTabLength;
            while((result - 1) * (myTabLength + mySpacing) + myTabLength > viewLength) {
                --result;
            }

            return result;
        }
        else {
            return 0;
        }
    }
    
    /**
     * installs tab buttons if they're not already visible. Returns
     * true if they were previously not visible, false if they already were.
     */
    private boolean installTabButtons() {
        if (myButtonsVisible) {
            return false;
        }
    
        myButtonsVisible = true;

        addSubview(myTopLeftButton);
        addSubview(myBottomRightButton);
        
        if (isHorizontal()) {
            myTabRect.x = myTopLeftButton.width();
            myTabRect.y = 0;
        }
        else {
            myTabRect.x = 0;
            myTabRect.y = myTopLeftButton.height();
        }

        ECRootView rootView = (ECRootView)rootView();
        if (rootView != null) {
            rootView.setTipForView(this, myTopLeftButton);
            rootView.setTipForView(this, myBottomRightButton);
        }
        return true;
    }
    
    /**
     * removes tab buttons if they're visible. Returns
     * true if they were previously visible, false if they already weren't.
     */
    private boolean removeTabButtons() {
        if (!myButtonsVisible) {
            return false;
        }
        
        myButtonsVisible = false;
        myTabRect.x = 0;
        myTabRect.y = 0;                    
        myTopLeftButton.removeFromSuperview();
        myBottomRightButton.removeFromSuperview();
        return true;
    }

    /**
     * Returns the bbox corresponding to the index of the tab
     */
    private Rect tabRectFromIndex(int index) {
        Rect  tabRect = new Rect(myTabRect);
        int   deltaX = 0, deltaY = 0;
        
        if (isHorizontal()) {
            deltaX = (myTabLength + mySpacing) * (index - myTabIndex);
        }
        else {
            deltaY = (myTabLength + mySpacing) * (index - myTabIndex);
        }

        tabRect.moveBy(deltaX, deltaY);
        
        return tabRect;
        
    }

    /**
     * Sets the size of the tab rect so that all the tabs fit in the tab view
     */
    private void setAutomaticTabRect() {
        if (isHorizontal()) {
            myTabRect.width = myTabLength;
            myTabRect.height = height();
        }
        else {
            myTabRect.width = width();
            myTabRect.height = myTabLength;
        }

        if (myButtonsVisible)
        {
            myButtonsVisible = false;
            myTopLeftButton.removeFromSuperview();
            myBottomRightButton.removeFromSuperview();
        }

        myTabRect.x = myTabRect.y = 0;
    }

    /**
     * Sets the bounding box of bitmap image of top/left push button
     */
    private void setBottomRightButtonBounds() {
        Image image = myBottomRightButton.image();
        // bail out if too early
        if (image == null) {
            return;
        }
        
        int imageWidth = image.width();
        int imageHeight = image.height();
        int thisWidth = width();
        int thisHeight = height();
        int leftCoord = 0, topCoord = 0;

        if (isHorizontal()) {    // try to center the image vertically
            if (imageHeight < thisHeight) {
                topCoord = (thisHeight - imageHeight) / 2;
            }
            leftCoord = thisWidth - imageWidth;
        }
        else {                  // try to center the image horizontally
            if (imageWidth < thisWidth) {
                leftCoord = (thisWidth - imageWidth) / 2;
            }
            topCoord = thisHeight - imageHeight;
        }

        myBottomRightButton.setBounds(leftCoord, topCoord, imageWidth, imageHeight);
    }

    /**
     * Sets the bounding box of bitmap image of top/left push button
     */
    private void setTopLeftButtonBounds() {
        Image image = myTopLeftButton.image();
        // bail out if too early
        if (image == null) {
            return;
        }
        int imageWidth = image.width();
        int imageHeight = image.height();
        int thisWidth = width();
        int thisHeight = height();
        int leftCoord = 0, topCoord = 0;

        if (isHorizontal()) {    // try to center the image vertically
            if (imageHeight < thisHeight) {
                topCoord = (thisHeight - imageHeight) / 2;
            }
        }
        else {                  // try to center the image horizontally
            if (imageWidth < thisWidth) {
                leftCoord = (thisWidth - imageWidth) / 2;
            }

        }

        myTopLeftButton.setBounds(leftCoord, topCoord, imageWidth, imageHeight);
    }
}

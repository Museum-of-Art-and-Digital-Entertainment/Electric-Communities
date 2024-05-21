/*******************************************************************
 *
 *    DESCRIPTION: UPDATEABLE TEXT WINDOW - A window that displays
 *    a text block and an "update" button to suck in new text.
 *
 *    AUTHOR: John Sullivan
 *
 *******************************************************************/
package ec.ui.util;

/** import files **/
import ec.ifc.app.ECExternalWindow;
import ec.ifc.app.ECTextView;
import ec.ifc.app.ECWindowController;

import netscape.application.Application;
import netscape.application.Button;
import netscape.application.Color;
import netscape.application.ContainerView;
import netscape.application.Font;
import netscape.application.Popup;
import netscape.application.Rect;
import netscape.application.RootView;
import netscape.application.ScrollGroup;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.TextField;
import netscape.application.TextView;
import netscape.application.Timer;
import netscape.application.LayoutManager;
import netscape.application.View;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.util.assertion.Assertion;


public class UpdateableTextWindow extends ECWindowController
                               implements Target, LayoutManager, WindowOwner {

    
    // layout constants
        
    /** initial x coordinate for window */
    protected static final int INITIAL_X = 40;
    /** initial y coordinate for window */
    protected static final int INITIAL_Y = 40;
    /** initial width for window */
    protected static final int INITIAL_WIDTH = 600;
    /** initial height for window */
    protected static final int INITIAL_HEIGHT = 300;
    /** space around things */
    protected static final int MARGIN = 8;
    /** width of standard button */
    protected static final int BUTTON_WIDTH = 80;
    /** height of standard button */
    protected static final int BUTTON_HEIGHT = 25;
    /** height of standard popup */
    protected static final int POPUP_HEIGHT = 25;
    /** width of font popup */
    protected static final int FONT_POPUP_WIDTH = 40;
    /** width of timer interval popup */
    protected static final int TIMER_POPUP_WIDTH = 120;
        
    // other constants  

    // user-visible string constants
    protected static final String UPDATE_BUTTON_TITLE = "Update Now";
    protected static final String FONT_POPUP_LABEL = "Font size:";
    protected static final String TIMER_POPUP_LABEL = "Auto update:";
    
    // commands
    protected static final String CREATE_WINDOW_COMMAND = "CREATE WINDOW";
    protected static final String UPDATE_TEXT_COMMAND = "UPDATE TEXT";
    protected static final String FONT_CHANGED_COMMAND = "FONT CHANGED";
    protected static final String INTERVAL_CHANGED_COMMAND = "INTERVAL CHANGED";
    
    // fonts
    protected static final Font[] FONTS = 
         {
            Font.fontNamed("Courier", Font.PLAIN, 9),
            Font.fontNamed("Courier", Font.PLAIN, 10),
            Font.fontNamed("Courier", Font.PLAIN, 12),
            Font.fontNamed("Courier", Font.PLAIN, 14),
            Font.fontNamed("Courier", Font.PLAIN, 18),
         };
         
    // timer interval values
    protected static final Integer[] TIMER_INTERVALS =
        {
            new Integer(1000),  // 1 second
            new Integer(2000),  // 2 seconds
            new Integer(5000),  // 5 seconds
            new Integer(10000), // 10 seconds
            new Integer(30000), // 30 seconds
            new Integer(60000), // 1 minute
            new Integer(300000),// 5 minutes
            new Integer(600000),// 10 minutes
            new Integer(0)      // special value meaning "stop timer"
        };
        
    // strings displayed in popups
    protected static final String[] FONT_TITLES = 
        {"9", "10", "12", "14", "18"};
    protected static final String[] TIMER_TITLES = 
        {
            "every second",
            "every 2 seconds",
            "every 5 seconds",
            "every 10 seconds",
            "every 30 seconds",
            "every minute",
            "every 5 minutes",
            "every 10 minutes",
            "never"
        };
    
    /*
    * Instance variables
    */
    
    protected ECExternalWindow myWindow = null;
    protected ScrollGroup myScrollGroup = null;
    protected Button myUpdateButton = null;
    protected Popup myFontPopup = null;
    protected TextField myFontLabel = null;
    protected Popup myTimerPopup = null;
    protected TextField myTimerLabel = null;
    protected TextView myTextView = null;
    protected Timer myTimer = null;
    
    
    //
    // Constructors
    //
    public UpdateableTextWindow(String title) {
        performLater(CREATE_WINDOW_COMMAND, title);
    }
    
    
    //
    // public methods
    //
    
    /** Responsibility from LayoutManager; does nothing here */
    public void addSubview(View v) {}
    
    /** Responsibility from ECWindowController; Returns the preferences window */
    public Window getWindow() {
        return myWindow;
    }
    
    /** Repositions the UI elements in the preferences window */
    public void layoutView(View view, int deltaWidth, int deltaHeight) {
        // layoutView is used to do this simple thing because
        // of the IFC bug where views with WIDTH_CAN_CHANGE or
        // HEIGHT_CAN_CHANGE resize instructions can get messed up
        // when the window is shrunken
        Assertion.test(view == myWindow.rootView());
        Rect rootViewBounds = view.bounds();
        // no need to move button or auto-timer stuff; they're in fixed places
        myFontPopup.moveTo(rootViewBounds.width - MARGIN - FONT_POPUP_WIDTH, MARGIN);
        myFontLabel.moveTo(myFontPopup.x() - MARGIN/2 - myFontLabel.width(), MARGIN);
        myScrollGroup.setBounds(MARGIN,
                                2*MARGIN + BUTTON_HEIGHT,
                                rootViewBounds.width - 2*MARGIN,
                                rootViewBounds.height - 3*MARGIN - BUTTON_HEIGHT);
    }

  /**
   * Overridden to process events sent by self.
   */
  public void performCommand(String command, Object arg) {
    if (CREATE_WINDOW_COMMAND.equals(command)) {
        createWindow((String)arg);
    } else if (UPDATE_TEXT_COMMAND.equals(command)) {
        updateText();
    } else if (FONT_CHANGED_COMMAND.equals(command)) {
        updateFont();
    } else if (INTERVAL_CHANGED_COMMAND.equals(command)) {
        updateTimerInterval();
    } else {
        super.performCommand(command, arg);
    }
  }
    
    /** Responsibility from LayoutManager; does nothing here */
    public void removeSubview(View v) {}

    /**
     * Responsibility from ECWindowController; overridden to just complain
     * if called (should never be called).
     */
    public void setWindow(Window newWindow) {
        // not allowed to change the window once set
        Assertion.fail();
    }
    
    /** responsibility from WindowOwner, does nothing here */
    public void windowDidBecomeMain(Window window) {}

    /** responsibility from WindowOwner, overridden to make sure timer is off */
    public void windowDidHide(Window window) {
        if (myTimer.isRunning()) {
            myTimer.stop();
        }
    }

    /** responsibility from WindowOwner, does nothing here */
    public void windowDidResignMain(Window window) {}

    /** responsibility from WindowOwner, overridden to make sure timer is on if it should be */
    public void windowDidShow(Window window) {
        updateTimerInterval();
    }

    /** responsibility from WindowOwner, does nothing here (returns true) */
    public boolean windowWillHide(Window window) {return true;}

    /** responsibility from WindowOwner, does nothing here (returns true) */
    public boolean windowWillShow(Window window) {return true;}

    /** responsibility from WindowOwner, does nothing here */
    public void windowWillSizeBy(Window window, Size size) {}

    //
    // private and protected methods
    //
    
    /**
     * Creates the IFC window that this class keeps track of, and its contents.
     */
    private void createWindow(String title) {
        myWindow = new ECExternalWindow();
        Size windowSize = myWindow.windowSizeForContentSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        myWindow.sizeTo(windowSize.width, windowSize.height);
        myWindow.moveTo(INITIAL_X, INITIAL_Y);
        myWindow.setTitle(title);
        
        // we'll manage changes in size of the root view
        RootView rootView = myWindow.rootView();
        rootView.setBuffered(true);
        
        rootView.setLayoutManager(this);

        myScrollGroup = new ScrollGroup();
        // since scroll group will fill contents of window, it doesn't
        // need its own border
        myScrollGroup.setBorder(null);
        rootView.addSubview(myScrollGroup);
        
        int x = MARGIN;
        int y = MARGIN;
        int width = BUTTON_WIDTH;
        
        myUpdateButton = Button.createPushButton(
            x, y, width, BUTTON_HEIGHT);
        myUpdateButton.setTitle(UPDATE_BUTTON_TITLE);
        myUpdateButton.setTarget(this);
        myUpdateButton.setCommand(UPDATE_TEXT_COMMAND);
        rootView.addSubview(myUpdateButton);
                
        myTimerLabel = TextField.createLabel(TIMER_POPUP_LABEL);

        x += width + MARGIN;
        width = myTimerLabel.width();
        
        myTimerLabel.sizeTo(width, POPUP_HEIGHT);
        myTimerLabel.moveTo(x, y);
        rootView.addSubview(myTimerLabel);
        
        x += width + MARGIN/2;
        width = TIMER_POPUP_WIDTH;
        
        myTimerPopup = new Popup(x, y, width, POPUP_HEIGHT);
        for (int i = 0; i < TIMER_TITLES.length; i += 1) {
            myTimerPopup.addItem(TIMER_TITLES[i], null);
        }
        myTimerPopup.setTarget(this);
        myTimerPopup.setCommand(INTERVAL_CHANGED_COMMAND);
        // start with auto-timer off, which is the last value
        myTimerPopup.selectItemAt(TIMER_TITLES.length - 1);
        rootView.addSubview(myTimerPopup);

        // font stuff will be positioned by layoutView, not here
        myFontLabel = TextField.createLabel(FONT_POPUP_LABEL);
        myFontLabel.sizeTo(myFontLabel.width(), POPUP_HEIGHT);
        rootView.addSubview(myFontLabel);
        
        
        myFontPopup = new Popup(0, 0, FONT_POPUP_WIDTH, POPUP_HEIGHT);
        for (int i = 0; i < FONT_TITLES.length; i += 1) {
            myFontPopup.addItem(FONT_TITLES[i], null);
        }
        myFontPopup.setTarget(this);
        myFontPopup.setCommand(FONT_CHANGED_COMMAND);
        // start with the middle-sized font
        myFontPopup.selectItemAt(FONT_TITLES.length/2);
        rootView.addSubview(myFontPopup);
        
        // use ECTextView so Control-C works for Copy
        myTextView = new ECTextView();
        myTextView.setEditable(false);
        myScrollGroup.setContentView(myTextView);
        myScrollGroup.setBackgroundColor(Color.white);
        myTextView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        updateFont();
        updateText();
                
        // only show scroll bar if necessary, which it won't be
        // unless user shrinks window. This needs to be after
        // the controls are added, because otherwise their initial
        // widths might vary based on whether the code thinks the
        // vertical scroll bar should be displayed
        myScrollGroup.setVertScrollBarDisplay(ScrollGroup.AS_NEEDED_DISPLAY);
        
        layoutView(rootView, 0, 0);
        
        myTimer = new Timer(this, UPDATE_TEXT_COMMAND, 0);
        updateTimerInterval();
        
        // we'll act as window owner to turn timer off when window is hidden
        myWindow.setOwner(this);
    }
        
    /**
     * Returns the most up-to-date text for display in this window.
     * Override to get whatever text this particular window needs.
     */
    protected String getUpdatedText() {
        return "Dummy text";
    }
    
    /** perform this command later so it'll be on the IFC event loop */
    private void performLater(String command, Object arg) {
        Application.application().performCommandLater(
            this, command, arg);
    }
    
    /** changes font in textview to match selection in popup */
    private void updateFont() {
        myTextView.setFont(FONTS[myFontPopup.selectedIndex()]);
    }
    
    /** updates the text in the main text area */
    private void updateText() {
        myTextView.setString(getUpdatedText());
    }

    /** updates the timer interval */
    private void updateTimerInterval() {
        int whichInterval = myTimerPopup.selectedIndex();
        if (whichInterval == TIMER_INTERVALS.length - 1) {
            // last interval selected; turn timer off
            if (myTimer.isRunning()) {
                myTimer.stop();
            }
        } else {
            // set timer delay, and make sure it's on
            myTimer.setDelay(TIMER_INTERVALS[whichInterval].intValue());
            if (!myTimer.isRunning()) {
                myTimer.start();
            }
        }
    }
}

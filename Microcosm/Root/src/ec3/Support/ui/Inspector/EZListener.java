/*******************************************************************
 *
 *    DESCRIPTION: EZ Listener Window - EZ command evaluator for use
 *    from Inspector and elsewhere.
 *
 *    AUTHORS: Trevor Morris wrote the first ConsoleWindow, later
 *    folded into this class.  Claire Griffin wrote the first
 *    LogWindow, inheriting from ConsoleWindow. John Sullivan combined
 *    the two and updated the appearance.  Kari Dubbelman copied the
 *    code to inspector so that all all COSM dependencies could be
 *    removed to avoid cross-package dependencies, and renamed the
 *    copy EZListenerWindow.
 *
 *******************************************************************/
package ec.ui;

/** import files **/

import ec.ifc.app.*;
import netscape.application.*;
import java.awt.Dimension;
import ec.e.io.EInputHandler;
import ec.ifc.stonelook.SLWindow;
import ec.ifc.stonelook.SLTextView;
import ec.ifc.stonelook.SLTextField;
import ec.ifc.stonelook.SLScrollGroup;
import ec.ifc.stonelook.StoneLook;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.timer.ClockController;
import ec.e.timer.Clock;
import ec.e.timer.ETickHandling;
//import ec.cosm.gui.dynamics.jiGUIFramework;
//import ec.cosm.gui.utils.GuiUtil;
//import ec.cosm.gui.utils.WindowManager;

import ec.e.inspect.InspectorUI;
import ec.ui.IFCInspectorUI;

/**
 * Inspector's EZ eval loop window. Shows a window which allows user to enter expressions and
 * has an output area which displays the echoed input plus any resulting output
 *
 * Any input typed by the user is not only reflected, it is passed on to the
 * "input handler" for the EZListenerWindow. The input handler is any object
 * of type "EInputHandler".
 *
 * Printing to the listener output can be done from any thread. The
 * print operations are "print" and "println". If the output grows
 * more than a specified size it is shrunk (by discarding the oldest
 * text). The maximum size can be specified by the "setMaxOutputSize"
 * method.
 *
 * Example of showing a listener window and printing to it:
 *  EZListenerWindow lw = new EZListenerWindow();
 *  lw.show();
 *  lw.println("Hello World");
 * WARNING: You must have created an ECApplication object before showing or
 * printing to a listener window. If you don't you'll get a null pointer
 * exception inside the window constructor (it's an IFC limitation)
 *
 * Example of setting input handler
 *  lw.setInputHandler(new MyInputHandler());
 * */

public class EZListenerWindow extends ECWindowController 
                       implements EventProcessor, LayoutManager {

    /*
    * Constants
    */

    /** Default maximum output size, in chars */
    protected static final int DEFAULT_MAX_OUTPUT_SIZE = 10000;

    /** window title */
    protected static final String LISTENER_WINDOW_TITLE = "Listener";

    /** Margin separating windows from each other and screen edges */
    protected static final int WINDOW_MARGIN = 0;

    /** Margin separating bottom of window from bottom of screen */
    protected static final int SCREEN_BOTTOM_MARGIN = 36;

    /** Minimal height for window at creation time */
    protected static final int MINIMUM_HEIGHT = 146;

    /** Margin separating UI components */
    protected static final int MARGIN = StoneLook.standardMargin();
    
    /** Margin between input and output fields */
    protected static final int GAP_BETWEEN_FIELDS = StoneLook.thinMargin(); 

    /** Input and output text area width */
    protected static final int INPUT_AND_OUTPUT_WIDTH = 400;

    /** Output text area height */
    protected static final int OUTPUT_HEIGHT = 300;

    /** Input text area height */
    protected static final int INPUT_HEIGHT = StoneLook.standardTextHeight();

    /** Prefix for input echoed in output area */
    protected static final String INPUT_PREFIX = "  ";
    
    /** key for window positioning stuff */ 
    protected static final String POSITIONING_KEY = "LISTENER WINDOW POSITION";
    
    /* Commands */
    protected static final String INPUT_COMMAND = "INPUT COMMAND";
    protected static final String ACQUIRE_FOCUS = "ACQUIRE FOCUS";
    protected static final String CREATE_WINDOW = "CREATE WINDOW";


    /*
    * Class variables
    */

    /* Cache global application object (can only be one per program) */
    protected static ECApplication cApplication = null;


    /*
    * Instance variables
    */
    
    /** the IFC external window */
    protected SLWindow myWindow = null;

    /** Scrolling text area, contains output history  */
    protected TextView myOutputField = null;

    /** scroll group holding output text */
    protected SLScrollGroup myOutputScrollGroup = null;
    
    /** Text input area */
    protected TextField myInputField = null;

    /** Input handler */
    protected EInputHandler myInputHandler = null;

    /** Maximum output size */
    protected int myMaxOutputSize = DEFAULT_MAX_OUTPUT_SIZE;

    /** Color for text this user typed */
    // XXX This needs to be computed at run time to match the color
    // used by the word balloons; same for all utterances
    protected Color myEchoColor = null;
    
    /** Our Inspector UI (IFC) */
    protected InspectorUI myUI = null;
                           
    //
    // Constructor
    //
    public EZListenerWindow(InspectorUI ui) {
        performLater(CREATE_WINDOW);
        this.myUI = ui;
    }
        
    //
    // public methods
    //
    
    /** Responsibility from LayoutManager; does nothing here */
    public void addSubview(View v) {}
    
    /**
    * Return current input handler
    */
    public synchronized EInputHandler getInputHandler() {
        return myInputHandler;
    }

    /**
    * Get maximum output size
    */
    public synchronized int getMaxOutputSize() {
        return myMaxOutputSize;
    }
    
    
    /** overridden to create a new window with proper subviews if necessary */
    public Window getWindow() {
        return myWindow;
    }

    /** Repositions the UI elements in the preferences window */
    public void layoutView(View view, int deltaWidth, int deltaHeight) {
        // layoutView is used to do this simple thing because
        // of the IFC bug where views with WIDTH_CAN_CHANGE or
        // HEIGHT_CAN_CHANGE resize instructions can get messed up
        // when the window is shrunken
        int width = view.width() - 2*MARGIN;
        int height = INPUT_HEIGHT;
        int x = MARGIN;
        int y = view.height() - MARGIN - height;
        myInputField.setBounds(x, y, width, height);
        
        height = y - GAP_BETWEEN_FIELDS - MARGIN;
        y = MARGIN;     
        
        myOutputScrollGroup.setBounds(x, y, width, height);
    }

  /**
   * Overridden to process INPUT_COMMAND.
   */
  public void performCommand(String command, Object arg) {
    if (INPUT_COMMAND.equals(command)) {
        processInput(((TextField)arg).stringValue());
    }
    else if (ACQUIRE_FOCUS.equals(command)) {
        myInputField.setFocusedView();
    }
    else if (CREATE_WINDOW.equals(command)) {
        createWindow();
    }
    else {
        super.performCommand(command, arg);
    }
  }
    
    /**
     * Responsibility from ECWindowController.
     * Overridden to make the input field be the focused view after the
     * window is displayed.
     */
    public void show() {
        // ECWindowController.present is delayed to synch with IFC thread
        super.show();
        // we will also delay the focus-acquisition in the same way
        performLater(ACQUIRE_FOCUS);
    }
    
  /**
   * Print the given line on the listener window. Can be called from any thread.
   * Passing null is equivalent to passing the null string
   */
  public void print(String string) {

    // Check for null
    if (string == null) {
      string = "";
    }

    // May be in wrong thread to call IFC directly. So we create an event,
    // with us as processor, then post it
    EZListenerWindowPrintEvent event = new EZListenerWindowPrintEvent(string);
    event.setProcessor(this);
    getApplication().eventLoop().addEvent(event);
  }


  /**
   * Like print, but appends a newline to the string
   */
  public void println(String string) {
    if (string == null) {
      string = "";
    }
    print(string + "\n");
  }

  /**
   * Responsibility from EventProcessor, called to process
   * EZListenerWindowPrintEvents. Only called from IFC thread.
   */
  public void processEvent(Event event) {
    // Extract string from print event and add it to output area
    EZListenerWindowPrintEvent printEvent = (EZListenerWindowPrintEvent)event;
    String stringToPrint = printEvent.getStringToPrint();
    appendString(stringToPrint);
  }


    /** Responsibility from LayoutManager; does nothing here */
    public void removeSubview(View v) {}

    /**
     * Places listener window beneath main window. Some of this might move into
     * WindowManager soon.
     */
    public void reposition() {
        ECExternalWindow listenerWindow = (ECExternalWindow)getWindow();     

        // set up auto-positioning stuff
        // XXX note that this puts all listener windows in the same place; this
        // is fine for 1.0 when there's only one
        /* Commented out by Kari, for now. We want this functionality but we need
           to figure out how to get the window position coordinates down here.
        if (framework != null)  {
            WindowManager wm = framework.getWindowManager();
            listenerWindow.setPositioningKey(POSITIONING_KEY);
            listenerWindow.setPositioner(wm);
            
            Rect savedBounds = wm.savedBounds(listenerWindow);
            if (savedBounds != null) {
                listenerWindow.setBounds(savedBounds);
            }
            else {
                // put listener window below main window
                Window mainWindow = framework.getWindow();
                Rect bounds = mainWindow.bounds();
                bounds.y = bounds.maxY() + WINDOW_MARGIN;
                Dimension screenSize = GuiUtil.getScreenSize();
                bounds.height = screenSize.height - SCREEN_BOTTOM_MARGIN
                                - bounds.y - WINDOW_MARGIN;
            
                // move window up if necessary to make it minimally-sized
                int tooSmallBy = MINIMUM_HEIGHT - bounds.height;
                if (tooSmallBy > 0) {
                    bounds.y -= tooSmallBy;
                    bounds.height += tooSmallBy;
                }
                listenerWindow.setBounds(bounds);
            }   
        }
        */
    }

    /**
    * Set input handler, which will be notified whenever user types a line
    * into listener input field
    */
    public synchronized void setInputHandler(EInputHandler handler) {
        myInputHandler = handler;
    }

  /**
   * Set maximum output size (in characters). A size of 0 or less sets the
   * size to the default maximum
   */
  public synchronized void setMaxOutputSize(int size) {
    // Make sure size is sensible, then update
    if (size <= 0) {
      size = DEFAULT_MAX_OUTPUT_SIZE;
    }
    myMaxOutputSize = size;
  }
  
  /**
   * Responsibility from ECWindowController; sets the window that's being
   * controlled. <b>newWindow</b> must be an SLWindow.
   */
  public void setWindow(Window newWindow) {
    myWindow = (SLWindow)newWindow;
}

    //
    // private and protected methods
    //

  /*
   * Append string to output buffer, ensure scrolled to end
   */
  private Range appendString(String string) {

    // Check for overflow, delete excess plus some slop if we've grown too big
    int excess = myOutputField.length() - myMaxOutputSize;
    if (excess > 0) {
      int toDelete = excess + (myMaxOutputSize / 10);
      myOutputField.replaceRangeWithString(new Range(0, toDelete), "");
    }

    // Append string
    Range range = myOutputField.appendString(string);

    // Scroll so new string is visible
    myOutputField.scrollRangeToVisible(range);

    return range;
  }

    /**
     * Creates the IFC window that this class keeps track of, and its contents.
     */
    private void createWindow() {
        // ensure application is created first (for debugging?)
        ECApplication app = getApplication();

        // Create myWindow and set size
        int contentWidth = (2 * MARGIN) + INPUT_AND_OUTPUT_WIDTH;
        int contentHeight = (3 * MARGIN) + INPUT_HEIGHT + OUTPUT_HEIGHT + 4;

        myWindow = new SLWindow(0, 0, contentWidth, contentHeight);
        myWindow.setTitle(LISTENER_WINDOW_TITLE);

        // fetch the container view that's part of every SLWindow
        ContainerView cv = myWindow.getContainerView();
        cv.setLayoutManager(this);
        
        // don't waste any time drawing while we're positioning things
        cv.disableDrawing();
        
        // Create output text view
        myOutputField = new SLTextView(0, 0,
                       INPUT_AND_OUTPUT_WIDTH, OUTPUT_HEIGHT);
        myOutputField.setEditable(false);
        myOutputField.setSelectable(true);
        myOutputField.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        myOutputField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);

        // Create scroll group for output text
        myOutputScrollGroup = new SLScrollGroup();
        myOutputScrollGroup.setContentView(myOutputField);
        myOutputScrollGroup.setBackgroundColor(myOutputField.backgroundColor());
        myOutputScrollGroup.setHasVertScrollBar(true);
        myOutputScrollGroup.setVertScrollBarDisplay(
            ScrollGroup.AS_NEEDED_DISPLAY);
        
        // important make sure we add this to the container view
        cv.addSubview(myOutputScrollGroup);

        // Set up color to use for text typed by this user.
        // XXX This needs to match color in word balloons (and we need to
        // do this for text typed by other avatars too). For now we're using
        // an arbitrary color
        myEchoColor = new Color(60, 9, 109);

        // Create input text view and set this to be its target
        myInputField = new SLTextField();
        myInputField.setCommand(INPUT_COMMAND);
        myInputField.setTarget(this);
        cv.addSubview(myInputField);
        
        cv.reenableDrawing();
        cv.layoutView(0, 0);        
    }

  /*
   * Return single, global application object
   */
  protected static ECApplication getApplication() {
    if (cApplication == null) {
      try {
    cApplication = (ECApplication)Application.application();
      }
      catch (ClassCastException e) {
    cApplication = null;
      }
      if (cApplication == null) {
    throw new Error("Cannot use listener before ECApplication object is set up");
      }
    }
    return cApplication;
  }

    /** perform this command later so it'll be on the IFC event loop */
    private void performLater(String command) {
        Application.application().performCommandLater(
            this, command, null);
    }   

  /*
   * Process line of input
   */
  private void processInput(String input) {

    // Assumption: this can only be called if window has been created

    // Add to output. We add first the output then a newline, then we
    // make the output look the way we want. Adding the newline separately
    // makes it easy to change the output without changing all subsequent
    // output as well.
    String stringToAppend = INPUT_PREFIX + input;
    Range range = appendString(stringToAppend);
    appendString("\n");
    myOutputField.addAttributeForRange(TextView.TEXT_COLOR_KEY,
                     myEchoColor,
                     range);

    // Reset input field
    myInputField.setStringValue("");
    myInputField.setInsertionPoint(0);

    // Notify input handler, if any
    if (myInputHandler != null) {
      getInputHandler() <- handleInput(input);
    }
  }

}


class EZListenerWindowPrintEvent extends ECEvent {

  static public int CONSOLE_WINDOW_PRINT_EVENT = 1100;

  private String myStringToPrint;


  EZListenerWindowPrintEvent(String string) {
    super(CONSOLE_WINDOW_PRINT_EVENT);
    myStringToPrint = string;
  }


  String getStringToPrint() {
    return myStringToPrint;
  }

}


eclass EZListenerWindowTestInputHandler implements EInputHandler, ETickHandling {

    private EZListenerWindow mylistenerWindow;

  EZListenerWindowTestInputHandler(EZListenerWindow listenerWindow) {
    mylistenerWindow = listenerWindow;
   
    ClockController cc = ClockController.TheQuakeProofClockController();
    Clock clock = cc.newClock(5000, this, null);
    clock.start();
  }


  emethod handleTick(int tick, Clock clock, Object arg) {
    System.out.println("Timer fired, tick = " + tick);
    mylistenerWindow.println("Timer fired, tick = " + tick);
  }


  emethod handleInput(String line) {
    System.out.println("Input handler got input - " + line);
    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
      System.exit(0);
    }
  }

}


/** IFC stuff needs to be in its own thread in e-land */
public class EZListenerWindowTester extends Thread {

    public EZListenerWindowTester (String args[]) {
        // we don't need no steenkin' args; ignore them
        this.start();
    }

    public void run () {
        Application app = new ECApplication();
        EZListenerWindow listenerWindow = new EZListenerWindow((InspectorUI)null);
        listenerWindow.setMaxOutputSize(1000);
        listenerWindow.show();
        listenerWindow.setInputHandler(new EZListenerWindowTestInputHandler(listenerWindow));
        listenerWindow.println("hello world");
        app.run();
    }
}


/** this is the puppy you e-launch to run the test */
public eclass EZListenerWindowTesterLauncher implements ELaunchable
{
    emethod go (EEnvironment env) {
        EZListenerWindowTester worker = new EZListenerWindowTester(env.args());
    }
}

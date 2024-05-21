package ec.ui;

import netscape.application.*;
import ec.e.io.EInputHandler;
import ec.misc.*;
import ec.ifc.app.*;

/**
 * IFC Console window class. Shows a window which allows user to enter text and
 * has an output area which displays the echoed input plus any output printed
 * to the console window.
 *
 * Any input typed by the user is not only reflected, it is passed on to the
 * "input handler" for the ConsoleWindow. The input handler is any object
 * of type "EInputHandler".
 *
 * Printing to the console can be done from any thread. The print operations
 * are "print" and "println". If the output grows more than a specified size
 * it is shrunk (by discarding the oldest text). The maximum size can be
 * specified by the "setMaxOutputSize" method.
 *
 * Provides a global "standard" console in a class variable called "standard"
 *
 * Example of showing the standard console and printing to it:
 *  ConsoleWindow.standard.show();
 *  ConsoleWindow.standard.println("Hello World");
 * WARNING: You must have created an ECApplication object before showing or
 * printing to a console window. If you don't you'll get a null pointer
 * exception inside the window constructor (it's an IFC limitation)
 *
 * Example of setting input handler
 *  ConsoleWindow.standard.setInputHandler(new MyInputHandler());
 *
 * You can see a test run of this class by typing:
 * "java ec.ui.ConsoleWindowTester"
 */
public class ConsoleWindow implements Target, EventProcessor {

  /*
   * Constants
   */

  /** Default maximum output size, in chars */
  public static final int DEFAULT_MAX_OUTPUT_SIZE = 10000;

  /** Default window title */
  public static final String DEFAULT_WINDOW_TITLE = "Console Window";

  /** Window x position - can change using "getWindow().moveTo(x, y) */
  public static final int WINDOW_X = 100;

  /** Window y position - can change using "getWindow().moveTo(x, y) */
  public static final int WINDOW_Y = 100;

  /** Margin separating UI components */
  public static final int MARGIN = 6;

  /** Input and output text area width */
  public static final int INPUT_AND_OUTPUT_WIDTH = 400;

  /** Output text area height */
  public static final int OUTPUT_HEIGHT = 300;

  /** Input text area height */
  public static final int INPUT_HEIGHT = 20;

  /* Prefix for input echoed in output area */
  public static final String INPUT_PREFIX = "> ";

  /* Commands */
  protected static final String INPUT_COMMAND = "INPUT COMMAND";


  /*
   * Class variables
   */

  /** Standard console window, always present (but not visible until shown) */
  public static ConsoleWindow standard = new ConsoleWindow();

  /* Cache global application object (can only be one per program) */
  protected static ECApplication cApplication = null;


  /*
   * Instance variables
   */

  /** Window */
  protected ECExternalWindow iWindow = null;

  /** Scrolling text area, contains output history  */
  protected TextView iOutputText = null;

  /** Text input area */
  protected TextField iInputText = null;

  /** Input handler */
  protected EInputHandler iInputHandler = null;

  /** Maximum output size */
  protected int iMaxOutputSize = DEFAULT_MAX_OUTPUT_SIZE;

  /** Font for echoing input */
  protected Font iEchoFont = null;


  /**
   * Constructor
   */
  protected ConsoleWindow() {
  }


  /**
   * Show the console window. Convenience method for getWindow().show()
   * Should only be called from IFC thread (or in main, at startup).
   */
  public void show() {
    getWindow().show();
  }


  /**
   * Hide the console window. Convenience method for getWindow().hide()
   * Should only be called from IFC thread (or in main, at startup).
   */
  public void hide() {
    if (iWindow != null) {
      iWindow.hide();
    }
  }


  /**
   * Print the given line on the console window. Can be called from any thread.
   * Passing null is equivalent to passing the null string
   */
  public void print(String string) {

    // Check for null
    if (string == null) {
      string = "";
    }

    // May be in wrong thread to call IFC directly. So we create an event,
    // with us as processor, then post it
    ConsoleWindowPrintEvent event = new ConsoleWindowPrintEvent(string);
    event.setProcessor(this);
    getApplication().getEventLoop().addEvent(event);
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
   * Set input handler, which will be notified whenever user types a line
   * into console input field
   */
  public synchronized void setInputHandler(EInputHandler handler) {
    iInputHandler = handler;
  }


  /**
   * Return current input handler
   */
  public synchronized EInputHandler getInputHandler() {
    return iInputHandler;
  }


  /*
   * Return IFC window use for the ConsoleWindow (useful for setting position,
   * size, title etc).
   * Should only be called from IFC thread (or in main, at startup).
   */
  public Window getWindow() {

    // Check if we're already set up
    if (iWindow != null) {
      return iWindow;
    }

    // Check that there is an ECApplication set up (will throw an error if
    // there is no application)
    getApplication();

    // Create iWindow and set size
    iWindow = new ECExternalWindow();
    int contentWidth = (2 * MARGIN) + INPUT_AND_OUTPUT_WIDTH;
    int contentHeight = (3 * MARGIN) + INPUT_HEIGHT + OUTPUT_HEIGHT;
    iWindow.setTitle(DEFAULT_WINDOW_TITLE);

    Size windowSize =
      iWindow.windowSizeForContentSize(contentWidth, contentHeight);
    iWindow.setBounds(WINDOW_X, WINDOW_Y, windowSize.width, windowSize.height);
    iWindow.setResizable(true);

    // Create container view
    ContainerView containerView = new ContainerView(0, 0,
						    contentWidth,
						    contentHeight);
    containerView.setBorder(ECWindowBorder.border());
    containerView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    containerView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    iWindow.addSubview(containerView);
    
    // Create output text view
    iOutputText = new ECTextView(0, 0,
			       INPUT_AND_OUTPUT_WIDTH, OUTPUT_HEIGHT);
    iOutputText.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    iOutputText.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    iOutputText.setEditable(false);

    // Create scroll group for output text
    ScrollGroup outputScrollGroup =
      new ScrollGroup(MARGIN, MARGIN,
		      INPUT_AND_OUTPUT_WIDTH, OUTPUT_HEIGHT);
    outputScrollGroup.setContentView(iOutputText);
    outputScrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    outputScrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    outputScrollGroup.setBackgroundColor(iOutputText.backgroundColor());
    outputScrollGroup.setHasVertScrollBar(true);
    outputScrollGroup.setBorder(ECScrollBorder.border());
    containerView.addSubview(outputScrollGroup);

    // Set up echo font for use in output text view. Use bold unless default
    // font is already bold, in which case use italic.
    Font outputFont = iOutputText.font();
    int echoStyle = outputFont.isBold() ? Font.ITALIC : Font.BOLD;
    iEchoFont =
      Font.fontNamed(outputFont.name(), echoStyle, outputFont.size());
    if (iEchoFont == null) {
      iEchoFont = outputFont;
    }

    // Create input text view and set this to be its target
    iInputText = new ECTextField(MARGIN, (2 * MARGIN) + OUTPUT_HEIGHT,
			       INPUT_AND_OUTPUT_WIDTH, INPUT_HEIGHT);
    iInputText.setCommand(INPUT_COMMAND);
    iInputText.setTarget(this);
    iInputText.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
    iInputText.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    containerView.addSubview(iInputText);

    return iWindow;
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
    iMaxOutputSize = size;
  }


  /**
   * Get maximum output size
   */
  public synchronized int getMaxOutputSize() {
    return iMaxOutputSize;
  }


  /**
   * Perform command; responsibility from Target interface, called when we
   * are target of a command from an IFC control etc.
   */
  public void performCommand(String command, Object arg) {
    if (command.equals(INPUT_COMMAND)) {
      processInput(((TextField)arg).stringValue());
    }
    else {
      throw new Error("Unexpected command: " + command + " arg = " + arg);
    }
  }


  /**
   * Responsibility from EventProcessor, called to process
   * ConsoleWindowPrintEvents. Only called from IFC thread.
   */
  public void processEvent(Event event) {

    // Make sure window is set up and visible
    if (!getWindow().isVisible()) {
      getWindow().show();
    }

    // Extract string from print event and add it to output area
    ConsoleWindowPrintEvent printEvent = (ConsoleWindowPrintEvent)event;
    String stringToPrint = printEvent.getStringToPrint();
    appendString(stringToPrint);
  }


  /*
   * Process line of input
   */
  private void processInput(String input) {

    // Assumption: this can only be called if window has been created

    // Add to output. We add first the output then a newline, then we
    // make the output bold. Adding the newline separately makes it
    // easy to make the output bold without making all subsequent output
    // bold as well.
    String stringToAppend = INPUT_PREFIX + input;
    Range range = appendString(stringToAppend);
    appendString("\n");
    iOutputText.addAttributeForRange(IFCTextViewConstants.getFontKey(),
				     iEchoFont,
				     range);

    // Reset input field
    iInputText.setStringValue("");
    iInputText.setInsertionPoint(0);

    // Notify input handler, if any
    if (iInputHandler != null) {
      getInputHandler() <- handleInput(input);
    }
  }


  /*
   * Append string to output buffer, ensure scrolled to end
   */
  Range appendString(String string) {

    // Check for overflow, delete excess plus some slop if we've grown too big
    int excess = iOutputText.length() - iMaxOutputSize;
    if (excess > 0) {
      int toDelete = excess + (iMaxOutputSize / 10);
      iOutputText.replaceRangeWithString(new Range(0, toDelete), "");
    }

    // Append string
    Range range = iOutputText.appendString(string);

    // Scroll so new string is visible
    iOutputText.scrollRangeToVisible(range);

    return range;
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
	throw new Error("Cannot use console before ECApplication object is set up");
      }
    }
    return cApplication;
  }

}


class ConsoleWindowPrintEvent extends ECEvent {

  static public int CONSOLE_WINDOW_PRINT_EVENT = 1100;

  private String iStringToPrint;


  ConsoleWindowPrintEvent(String string) {
    super(CONSOLE_WINDOW_PRINT_EVENT);
    iStringToPrint = string;
  }


  String getStringToPrint() {
    return iStringToPrint;
  }

}


eclass ConsoleWindowTestInputHandler implements EInputHandler {

  ConsoleWindowTestInputHandler() {
    Object[] tickInfo = new Object[1];
    RtEnvelope envelope;
    envelope <- ConsoleWindowTestInputHandler.timerFired(tickInfo);
    RtClock clock = new RtClock(5000, this, envelope, tickInfo);
    clock.start();
  }


  emethod timerFired(Object[] tickInfo) {
    int tickNumber = ((Integer)(tickInfo[0])).intValue();
    System.out.println("Timer fired, tick = " + tickNumber);
    ConsoleWindow.standard.println("Timer fired, tick = " + tickNumber);
  }


  emethod handleInput(String line) {
    System.out.println("Input handler got input - " + line);
    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
      System.exit(0);
    }
  }

}


class ConsoleWindowTester {

  /**
   * Main method
   */
  public static void main(String[] arg) {
    Application app = new ECApplication();
    ConsoleWindow.standard.setMaxOutputSize(1000);
    ConsoleWindow.standard.setInputHandler(new ConsoleWindowTestInputHandler());
    ConsoleWindow.standard.println("hello world");
    app.run();
  }

}

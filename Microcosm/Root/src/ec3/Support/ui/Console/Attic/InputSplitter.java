package ec.ui;


import ec.e.io.EInputHandler;
import ec.ifc.app.*;

/**
 * InputSplitter allows multiple EInputHandlers to be connected to a single
 * input. First you give the input the input splitter. Then you register your
 * input handlers with the input splitter, giving each a prefix string.
 *
 * As input comes in the splitter matches and strips off any prefix, then hands
 * the rest of the input to the appropriate input handler.
 *
 * There is a single default input handler which gets all input which does not
 * have a recognizable prefix.
 *
 * InputSplitters are useful as the input handler for a console window, where
 * you often want the input to be split among different objects.
 *
 * You can test this class with:
 * "java ec.ui.InputSplitterTester"
 */
public eclass InputSplitter implements EInputHandler {

  java.util.Hashtable iInputHandlers = new java.util.Hashtable();


  /**
   * Constructor, creates empty input splitter. Any input will be dropped
   * on the ground until input handlers are added.
   */
  public InputSplitter() {
  }


  /**
   * Add handler for all input with the given prefix. If 'prefix' is already
   * in use the old handler for that prefix is lost. 'prefix' cannot be null.
   */
  emethod addInputHandler(String prefix, EInputHandler handler) {
    iInputHandlers.put(prefix, handler);
  }


  /**
   * Add handler for all input that is not handled by other input handlers.
   * There can only be one default input handler.
   * This call is equivalent to add input handler with a null string as its
   * prefix argument.
   */
  emethod addDefaultInputHandler(EInputHandler handler) {
    addInputHandler("", handler);
  }


  /**
   * Remove input handler for given prefix; both prefix and handler
   * must match the corresponding call to 'addInputHandler'
   */
  emethod removeInputHandler(String prefix, EInputHandler handler) {
    EInputHandler old = (EInputHandler)iInputHandlers.remove(prefix);
    if (old != handler) {
System.out.println("Inconsistent arguments to removeInput");
//      throw new Error("Inconsistent arguments to removeInput");
    }
  }


  /**
   * Remove the default input handler
   */
  emethod removeDefaultInputHandler(EInputHandler handler) {
    removeInputHandler("", handler);
  }


  /**
   * Responsibility from EInputHandler - handle a line of input
   */
  emethod handleInput(String line) {

    // Note: if startOfLine stays null we'll get the default handler
    String startOfLine = "";
    String endOfLine = line.trim();

    int index = 0;
    int length = endOfLine.length();
    while (index < length) {
      if (Character.isSpace(endOfLine.charAt(index))) {
	startOfLine = endOfLine.substring(0, index);
	endOfLine = endOfLine.substring(index).trim();
	break;
      }
      else {
	index++;
      }
    }

    EInputHandler handler = (EInputHandler)iInputHandlers.get(startOfLine);
    if (handler != null) {
      handler <- handleInput(endOfLine);
    }
  }

}


/**
 * Simple input handler, for testing
 */
eclass EchoInput implements EInputHandler {

  String iName = null;


  EchoInput(String name) {
    iName = name;
  }


  emethod handleInput(String line) {
    ConsoleWindow.standard.println(iName + " got: " + line);
    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
      System.exit(0);
    }
  }
  

}


/**
 * Test routine for InputSplitter
 */
class InputSplitterTester {

  public static void main(String[] args) {

    // Create app object, input splitter
    ECApplication app = new ECApplication();
    InputSplitter splitter = new InputSplitter();

    // Add some named input handlers
    splitter <- addDefaultInputHandler(new EchoInput("default"));
    splitter <- addInputHandler("a", new EchoInput("a"));
    splitter <- addInputHandler("b", new EchoInput("b"));

    // Set up console window
    ConsoleWindow.standard.setInputHandler(splitter);
    ConsoleWindow.standard.show();
    ConsoleWindow.standard.println("Type a <string> to send to a, b <string> to send to b");

    // Go
    app.run();
  }

}

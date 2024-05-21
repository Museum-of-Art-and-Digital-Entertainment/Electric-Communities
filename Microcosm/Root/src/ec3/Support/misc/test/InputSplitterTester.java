package ec.misc.test;

import ec.misc.InputSplitter;
import ec.e.run.Trace;
import ec.ifc.app.ECApplication;
import ec.e.io.EInputHandler;

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

        // XXX We used to test this with a GUI class ConsoleWindow that no
        // longer exists. We should revive this test using some non-gui
        // e-console type thing some day, but for now we are lazy and just
        // comment out the old test so we can remember what it used to do.
        System.out.println("InputSplitterTester is currently useless. " +
            "Maybe you should make it useful again?");
        
//        // Set up console window
//        ConsoleWindow.standard.setInputHandler(splitter);
//        ConsoleWindow.standard.present();
//        ConsoleWindow.standard.println(
//          "Type a <string> to send to a, b <string> to send to b");

        // Go
        app.run();
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
    // XXX This next line was part of the old test, when there was
    // a ConsoleWindow class to rely on. See note in InputSplitterTester
    // below
//        ConsoleWindow.standard.println(iName + " got: " + line);
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
            System.exit(0);
        }
    }
  

}


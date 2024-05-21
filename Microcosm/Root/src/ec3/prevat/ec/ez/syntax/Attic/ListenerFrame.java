package ec.ez.syntax;

import ec.ez.prim.EZUniversal;
import netscape.application.*;
import java.io.*;
import java.lang.Runnable;

public class ListenerFrame implements Target {
    public ExternalWindow thisWindow;
    public Listener myListener;
    ListenerOutputStream outs;
    PrintStream printOuts;
    TextView myTextField;

    boolean dumpFlag = false;
    boolean runFlag = true;
    int promptPosition = 0;
    static int windowCtr = 0;   // for positioning listeners
    Thread runThread = null;
    static boolean firstTime = true;
    Menu myMenu;
    MenuItem runItem;
    MenuItem debugItem;

    public void init() {
        thisWindow = new ExternalWindow();
        thisWindow.setResizable(true);

        Size size = thisWindow.windowSizeForContentSize(532,448);
        thisWindow.setBounds(60, 60, size.width, size.height);
        Listener.root.setMainRootView(thisWindow.rootView());
        myTextField = new ListenerTextView(0, 0, 500, 400, this);
        myTextField.setFont(Font.fontNamed("Courier", Font.PLAIN, 12));
        myTextField.setEditable(true);
        myTextField.setString("");
        myTextField.enableResizing();
 //     thisWindow.addSubview(myTextField);


        myTextField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myTextField.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        ScrollGroup scrollGroup = new ScrollGroup(0, 16, 532, 432);
        scrollGroup.setContentView(myTextField);
        scrollGroup.setHasHorizScrollBar(true);
        scrollGroup.setHasVertScrollBar(true);
        scrollGroup.setHorizScrollBarDisplay(ScrollGroup.ALWAYS_DISPLAY);
        scrollGroup.setVertScrollBarDisplay(ScrollGroup.ALWAYS_DISPLAY);
        scrollGroup.setBorder(new BezelBorder(BezelBorder.GROOVED, Color.lightGray));
        scrollGroup.setBackgroundColor(Color.white);
        scrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        thisWindow.addSubview(scrollGroup);

   //     printOuts.println("EZ Listener Window");

        thisWindow.show();

        myMenu = new Menu(true);

        MenuItem ezMenu = myMenu.addItemWithSubmenu("EZ");
        MenuItem editMenu = myMenu.addItemWithSubmenu("Edit");

        ezMenu.submenu().addItem("New Window", "New Window", this);
        ezMenu.submenu().addSeparator();

        runItem = new MenuItem("Run on newline", "Run on newline", this, true);
        ezMenu.submenu().addItemAt(runItem,2);
        runItem.setState(runFlag);
        debugItem = new MenuItem("Debug Info", "Debug Info", this, true);
        ezMenu.submenu().addItemAt(debugItem,3);

        editMenu.submenu().addItem("Cut",'X',"Cut", this);
        editMenu.submenu().addItem("Copy",'C',"Copy", this);
        editMenu.submenu().addItem("Paste",'V', "Paste", this);

        editMenu.submenu().addSeparator();
        editMenu.submenu().addItem("Doit",'D',"Doit", this);

        MenuView menuView = new MenuView(myMenu);
        menuView.sizeToMinSize();
        thisWindow.setMenuView(menuView);

  //      run();
       // System.out = new PrintStream(outs);
       firstTime = false;
    }

    public void performCommand(String theCmd, Object theObj) {

   //     System.out.println(theCmd);
        if(theCmd.equals("Cut")) {
            myTextField.cut();
            checkPromptPosition();
       }
        if(theCmd.equals("Copy")) {
            myTextField.copy();
        }

        if(theCmd.equals("Paste")) {
            myTextField.paste();
            checkPromptPosition();
        }

        if(theCmd.equals("Doit")) {
            Range selR = myTextField.selectedRange();
            String theCode = myTextField.stringForRange(selR);
  //      Range after = new Range(selR.lastIndex(),0);
  //     myTextField.selectRange(after);
            evalString(theCode);
        }

        if(theCmd.equals("Run on newline")) {
            runFlag = runItem.state();
        }

        if(theCmd.equals("Debug Info")) {
            dumpFlag = debugItem.state();
            myListener.myExpandFlag = dumpFlag;
        }

        if(theCmd.equals("New Window")) {
                spawnListener();
        }
    }


    public boolean checkRunFlagState() {
        return runFlag;
    }

    public void spawnListener() {
        try {
            String mtArgs[] = new String[0];
            new Listener(mtArgs, null);
        } catch (Exception e) {
            e.printStackTrace(printOuts);
        }
    }


    public PrintStream getPrintStream() {
        outs = new ListenerOutputStream(this);
        printOuts = new PrintStream(outs);
       return printOuts;
    }

    public void printPrompt() {
 //      int start = textPane.getSelectionStart();
 //       int end = textPane.getSelectionEnd();
 //       if((start == end) && (start > 0)) {
 //           String theText = textPane.getText();
 //           if(theText.charAt(start - 1) == 10)
 //               return;
 //      }

        printOuts.println();
        promptPosition = myTextField.length();
        Range endOfField = new Range(promptPosition, 0);
        myTextField.selectRange(endOfField);
        // JAY maybe faster way to do this!
        myTextField.draw();
    }


    void checkPromptPosition() {
        int lastPos = myTextField.length();
        if(lastPos < promptPosition)
           promptPosition = lastPos;
    }

    public void evalLine() {
        Range selR = myTextField.selectedRange();
        Range typedRange = Range.rangeFromIndices(promptPosition,
                                                  selR.lastIndex());
        String theCode = myTextField.stringForRange(typedRange);
        if(!theCode.equals("")) {
            try {
                InputStream ins = new StringBufferInputStream(theCode);

                //Lexer testLexer = new Lexer("", new DataInputStream(ins));
                EZLexer testLexer = new EZLexer("", new DataInputStream(ins));

                if (testLexer.trialParse()) {
                    evalString(theCode);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void evalString(String runText) {
         boolean nullSelection = false;
         boolean nearEndOfBuffer = true;
        // If we have a selected expression, run it.
         if(!runText.equals("")) {
            runText = cleanUp(runText);
            if(!runText.equals("")) {
                EZUniversal.ezPrinter = printOuts;
                EZRunTask myRunner = new EZRunTask(myListener, this, new StringBufferInputStream(runText));
                runThread = new Thread(myRunner);
                runThread.start();
            }
         }
    }

    String cleanUp(String inText) {
        int first = 0;
        int last = inText.length() - 1;
        while (first < last) {
            char theChar = inText.charAt(first);
            if((theChar != 10) && (theChar != 13))
                break;
            first++;
        }

         while (last >= first) {
            char theChar = inText.charAt(last);
            if((theChar != 10) && (theChar != 13))
                break;
            last--;
        }

        if(last >= first) {
            return inText.substring(first,last + 1);
        }
          else {
            return "";
        }
    }

    public void abortRunTask() {
        if(runThread != null) {
            runThread.stop();
            runThread = null;
            printOuts.println("\n*User Break*\n");
            printPrompt();
        }
    }

    public void appendString(String theString) {
       // System.out.print(theString);
        myTextField.appendString (theString);
        myTextField.scrollRangeToVisible(new Range(myTextField.length(),0));
        promptPosition = myTextField.length();
    }
}

class ListenerTextView extends TextView {
    ListenerFrame myFrame;
    ListenerTextView(int x, int y, int w, int h, ListenerFrame theFrame) {
        super(x, y, w, h);
        myFrame = theFrame;
    }

    public void keyDown(KeyEvent event) {
        if(event.key == 46){
            if(event.isControlKeyDown() || event.isAltKeyDown()) {
                myFrame.abortRunTask();
                return;
            }
        }

        super.keyDown(event);

        if((event.key == 10) && (myFrame.checkRunFlagState())) {
            myFrame.evalLine();
        } else {
            myFrame.checkPromptPosition();
        }
    }
}

class ListenerOutputStream extends ByteArrayOutputStream {
    ListenerFrame myFrame;

    ListenerOutputStream(ListenerFrame theFrame) {
        myFrame = theFrame;
    }

    public synchronized void write(int b) {
        if(b != 13)
            super.write(b);
        if(b == 13) {
        // Display the text on the listener window right away.
            displayLine();
        }
    }
    public synchronized void write(byte[] b, int off, int len) {
        for(int i=off;i<(off + len);i++) {
            write(b[i]);
        }
    }

    void displayLine() {
         myFrame.appendString(toString());
         reset();
    }
}


class EZRunTask implements Runnable {
    Listener myListener;
    ListenerFrame myFrame;
    StringBufferInputStream inStream;

    EZRunTask(Listener theListener, ListenerFrame theFrame, StringBufferInputStream ins) {
        myListener = theListener;
        myFrame = theFrame;
        inStream = ins;
    }

    public void run() {
        myListener.evalInput(inStream);
        myFrame.printPrompt();
    }
}

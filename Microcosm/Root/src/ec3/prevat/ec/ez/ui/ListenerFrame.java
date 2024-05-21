package ec.ez.ui;

import ec.ez.prim.EZUniversal;
import ec.ez.syntax.EZLexer;
import netscape.application.*;
import java.io.*;
import java.lang.Runnable;

public class ListenerFrame extends View implements Target, LayoutManager {
    public ExternalWindow thisWindow;
    public Listener myListener;
    ListenerOutputStream outs;
    PrintStream printOuts;
    TextView myTextField;
    ListenerTextView myEntryField;
    ScrollGroup scrollGroup;
    MenuView menuView;

    boolean dumpFlag = false;
    boolean runFlag = true;

    static int windowCtr = 0;   // for positioning listeners
    Thread runThread = null;
    static boolean firstTime = true;
    Menu myMenu;
    MenuItem debugItem;

    /** Input text area height */
    protected static final int INPUT_HEIGHT = 28;
    /** Margin separating UI components */
    protected static final int MARGIN = 2;

    protected static final int GAP_BETWEEN_FIELDS = 2;
    protected static final int SCROLLHEIGHT = 16;

    public void init() {
        setBounds(0, 0, 556, 468);
        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        thisWindow = new ExternalWindow();
        thisWindow.setResizable(true);

        Size size = thisWindow.windowSizeForContentSize(556,468);
        thisWindow.setBounds(60, 60, size.width, size.height);
        Listener.root.setMainRootView(thisWindow.rootView());

        myMenu = new Menu(true);

        MenuItem ezMenu = myMenu.addItemWithSubmenu("EZ");
        MenuItem editMenu = myMenu.addItemWithSubmenu("Edit");

        ezMenu.submenu().addItem("New Window", "New Window", this);
        ezMenu.submenu().addItem("Clear Window",'B',"Clear Window", this);
        ezMenu.submenu().addSeparator();

        debugItem = new MenuItem("Debug Info", "Debug Info", this, true);
        ezMenu.submenu().addItemAt(debugItem,3);

        editMenu.submenu().addItem("Cut",'X',"Cut", this);
        editMenu.submenu().addItem("Copy",'C',"Copy", this);
        editMenu.submenu().addItem("Paste",'V', "Paste", this);

        editMenu.submenu().addSeparator();
        editMenu.submenu().addItem("Doit",'D',"Doit", this);

        menuView = new MenuView(myMenu);
        menuView.sizeToMinSize();
   //     thisWindow.setMenuView(menuView);


        myTextField = new TextView(0, 0, 520, 406);
        myTextField.setFont(Font.fontNamed("Courier", Font.PLAIN, 12));
        myTextField.setEditable(true);
        myTextField.setString("");
        myTextField.enableResizing();

        myTextField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myTextField.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        scrollGroup = new ScrollGroup(2, menuView.height() + 2, 552, 438);
        scrollGroup.setContentView(myTextField);
        scrollGroup.setHasHorizScrollBar(true);
        scrollGroup.setHasVertScrollBar(true);
        scrollGroup.setHorizScrollBarDisplay(ScrollGroup.ALWAYS_DISPLAY);
        scrollGroup.setVertScrollBarDisplay(ScrollGroup.ALWAYS_DISPLAY);
        scrollGroup.setBorder(new BezelBorder(BezelBorder.GROOVED, Color.lightGray));
        scrollGroup.setBackgroundColor(Color.white);
        scrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        addSubview(scrollGroup);

        myEntryField = new ListenerTextView(2, scrollGroup.height(), 552, INPUT_HEIGHT, this);
        myEntryField.setFont(Font.fontNamed("Courier", Font.PLAIN, 12));
        myEntryField.setEditable(true);
        myEntryField.setString("");
        myEntryField.enableResizing();
        myEntryField.setFocusedView();

        addSubview(myEntryField);

        // fetch the container view that's part of every SLWindow
        // ContainerView cv = thisWindow.getContainerView();
        setLayoutManager(this);

        thisWindow.addSubview(this);
        layoutView(this, 0, 0);

        thisWindow.show();
        thisWindow.setMenuView(menuView);

       firstTime = false;
    }


    /** Repositions the UI elements in the preferences window */
    public void layoutView(View view, int deltaWidth, int deltaHeight) {
        // layoutView is used to do this simple thing because
        // of the IFC bug where views with WIDTH_CAN_CHANGE or
        // HEIGHT_CAN_CHANGE resize instructions can get messed up
        // when the window is shrunken
        Rect myBounds = view.bounds();
        int width = myBounds.width - 2*MARGIN;
        int height = INPUT_HEIGHT;
        int x = MARGIN;
        int y = myBounds.height - MARGIN - height;
        myEntryField.setBounds(x, y, width, height);

        height = y - GAP_BETWEEN_FIELDS - MARGIN - SCROLLHEIGHT;
        y = MARGIN + menuView.height();

        scrollGroup.setBounds(x, y, width, height);
    }

    /** Responsibility from LayoutManager; does nothing here */
    public void removeSubview(View v) {}

    public void performCommand(String theCmd, Object theObj) {
        TextView editTarget;
        if(myEntryField.getTheFocusState()) {
            editTarget = myEntryField;
        } else {
            editTarget = myTextField;
        }

   //     System.out.println(theCmd);
        if(theCmd.equals("Cut")) {
            editTarget.cut();
       }
        if(theCmd.equals("Copy")) {
            editTarget.copy();
        }

        if(theCmd.equals("Paste")) {
            editTarget.paste();
        }

        if(theCmd.equals("Doit")) {
            Range selR = editTarget.selectedRange();
            String theCode = editTarget.stringForRange(selR);
  //      Range after = new Range(selR.lastIndex(),0);
  //     editTarget.selectRange(after);
            evalString(theCode);
        }

        if(theCmd.equals("Debug Info")) {
            dumpFlag = debugItem.state();
            myListener.myExpandFlag = dumpFlag;
        }

        if(theCmd.equals("New Window")) {
                spawnListener();
        }

        if(theCmd.equals("Clear Window")) {
                resetThisWindow();
        }
    }

    public void resetThisWindow() {
        myTextField.setString("");
        myTextField.draw();
        myEntryField.setString("");
        myEntryField.draw();
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
        printOuts.println();
        Range endOfField = new Range(myEntryField.length(), 0);
        myTextField.selectRange(endOfField);
        // JAY maybe faster way to do this!
        myTextField.draw();
    }

    public void evalLine() {
        String theCode = myEntryField.string();
        if(!theCode.equals("")) {
            try {
                InputStream ins = new StringBufferInputStream(theCode);

                EZLexer testLexer = new EZLexer("",ins);

                if (testLexer.trialParse()) {
                    // Parse was balanced, print string and evaluate it.
                    printOuts.println(">" + theCode);
                    myEntryField.setString("");
                    myEntryField.draw();
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
                EZRunTask myRunner
                    = new EZRunTask(myListener, this,
                                    new StringBufferInputStream(runText));
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
     //   promptPosition = myTextField.length();
    }
}

class ListenerTextView extends TextView {
    ListenerFrame myFrame;
    boolean focusedFlag = false;

    ListenerTextView(int x, int y, int w, int h, ListenerFrame theFrame) {
        super(x, y, w, h);
        myFrame = theFrame;
    }

    public void startFocus() {
        super.startFocus();
        focusedFlag = true;
    }

    public void stopFocus() {
        super.stopFocus();
        focusedFlag = false;
    }

    public boolean getTheFocusState() {
        return focusedFlag;
    }

    public void keyDown(KeyEvent event) {
        if(event.key == 46){
            if(event.isControlKeyDown() || event.isAltKeyDown()) {
                myFrame.abortRunTask();
                return;
            }
        }

        super.keyDown(event);

        if(event.key == 10) {
            myFrame.evalLine();
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

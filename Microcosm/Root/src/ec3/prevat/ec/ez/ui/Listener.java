package ec.ez.ui;

import ec.ez.prim.PackagePath;
import ec.ez.collect.*;
import ec.ez.ezvm.*;
import ec.ez.syntax.EZLexer;
import ec.ez.syntax.EZParser;
import ec.ez.syntax.SyntaxException;
import java.io.*;
import java.util.Hashtable;
import netscape.application.*;


/**
 *
 */
public class Listener {

    static private final RuntimeException
        USAGE = new RuntimeException(
                  "usage: java ec.ez.syntax.Listener [-d] file");

    InputStream myIns;
    PrintStream myOuts;

    EZParser myParser;

    NameTableEditor myPov;
    boolean myDebugFlag = false;
    public boolean myExpandFlag = false;
    boolean myBatchFlag;
    public static Application root;
    static boolean rootRunning = false;
    static private Hashtable accumulation = new Hashtable();
    static public void main(String[] args)
         throws IOException, SyntaxException {

          Listener thisListener = new Listener(args);
 //       Hashtable test = new Hashtable();

 //       Listener.launchEZ(test);
    }

    static public void launchEZ(Hashtable initPOV)
         throws IOException, SyntaxException {

    //    String argv[] = new String[0];

    //    root = new Application();

    //    Listener listener = new Listener(argv, initPOV);
      //  listener.loop();
      //  System.out.println("End of listener loop");

      // Escape the vat!
      Thread startupThread = null;
      EZOpenListener mytask = new EZOpenListener();
      startupThread = new Thread(mytask);
      startupThread.start();
    }

    static public void fireUpEZ() {
        String argv[] = new String[0];
        root = new Application();
        Hashtable initPOV = new Hashtable();
        try {
            Listener listener = new Listener(argv, initPOV);
        } catch (Exception e) {}
    }
 
    // Activate EZ from elsewhere in the system.
    static public void openEZ(Hashtable initPOV) {
        rootRunning = true; // fake out root activation
        root = Application.application();
        String nullArgs[] = new String[0];
        try {
            Listener listener = new Listener(nullArgs, initPOV);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void openEZ() {
        openEZ(accumulation);
    }

    static public void gather(String key, Object value) {
        accumulation.put(key, value);
    }

    public Listener(String[] args) {
       String fname = "";
        if (args.length == 2) {
            if (args[0].equals("-d")) {
                myDebugFlag = true;
                fname = args[1];
            } else {
                throw USAGE;
            }
        } else if (args.length == 1) {
            myDebugFlag = false;
            fname = args[0];
        }

        EZLexer lexer = null;

        if(!fname.equals("")) {
            try {
                myIns = new FileInputStream(fname);
            } catch (Exception e) {
                System.out.println("File Not Found: " + fname);
            }
        } else {
          System.out.println("Reading from standard in.");
          System.out.println(System.in);
           myIns = System.in;
        }

        myOuts = System.out;
        System.out.println("EZ Listening active");
        myPov = PackagePath.standardNameTable(true, null, myOuts);

        if(myIns == System.in) {
            while (true)
                evalInput(myIns);
        } else {
            evalInput(myIns);
        }
    }

    public Listener(String[] args, Hashtable initPOV)
       throws IOException, SyntaxException {

        String fname = "";
        if (args.length == 2) {
            if (args[0].equals("-d")) {
                myDebugFlag = true;
                fname = args[1];
            } else {
                throw USAGE;
            }
        } else if (args.length == 1) {
            myDebugFlag = false;
            fname = args[0];
        }

        ListenerFrame listenWind = new ListenerFrame();
        listenWind.myListener = this;
        listenWind.init();

        myIns = new BufferedInputStream(new StringBufferInputStream(""));

        EZLexer lexer = new EZLexer(fname, myIns);
        myParser = new EZParser(lexer);

        myOuts = listenWind.getPrintStream();

        myPov = PackagePath.standardNameTable(true, initPOV, myOuts);

        if(!rootRunning) {
            rootRunning = true;
            root.run();
        }
    }

    public void evalInput(InputStream newIns) {
        try {
        //    myOuts.println();
            myIns = newIns;

            EZLexer lexer = new EZLexer("", myIns);
            myParser = myDebugFlag ? new EZParser(lexer, myOuts)
                                   : new EZParser(lexer);

            loop();
        } catch (Exception e) {
            e.printStackTrace(myOuts);
        }
    }

    public void loop() throws IOException, SyntaxException {
        boolean stayIn = true;
        while (stayIn) {
            try {
                stayIn = doOne();
            } catch (Throwable ex) {
                if(ex instanceof ThreadDeath) {
                    return;
                }
                myParser.diagnostic(ex, myOuts);
                return;
            }
        }
    }

    public boolean doOne() throws Throwable {
        if (myParser.isEndOfFile()) {
            return false;
        }
        Expr expr = null;

        expr = myParser.parseExpr();

        if (expr == null) {
            return false;
        }

        if (myExpandFlag) {
            myOuts.println("Expression expanded: ");
            expr.printOn(myOuts, 1);
            myOuts.println("");
        }

        Object result = expr.eval(myPov);
        myOuts.println("value: " + result);

        return true;
    }
}


class EZOpenListener implements Runnable {

    EZOpenListener() {
        
    }

    public void run() {
       Listener.fireUpEZ();
    }
}

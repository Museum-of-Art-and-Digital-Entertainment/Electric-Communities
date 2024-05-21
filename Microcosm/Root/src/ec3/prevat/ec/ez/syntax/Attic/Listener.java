package ec.ez.syntax;

import ec.ez.prim.PackagePath;
import ec.ez.collect.*;
import ec.ez.ezvm.*;
import java.io.*;
import java.util.Hashtable;
import netscape.application.*;


/**
 *
 */
public class Listener {

    static private RuntimeException
        USAGE = new RuntimeException(
                  "usage: java ec.ez.syntax.Listener [-d] file");

    InputStream myIns;
    PrintStream myOuts;

    //Parser myParser;
    EZParser myParser;

    NameTableEditor myPov;
    boolean myDebugFlag = false;
    public boolean myExpandFlag = false;
    boolean myBatchFlag;
    public static Application root;
    static boolean rootRunning = false;

    static public void main(String[] args) throws IOException, SyntaxException {
        Hashtable test = new Hashtable();

        Listener.launchEZ(test);
     //   root = new Application();

     //   Listener listener = new Listener(args);
     //   listener.loop();
    }


    static public void launchEZ(Hashtable initPOV) throws IOException, SyntaxException {
        String argv[] = new String[0];

        root = new Application();

        Listener listener = new Listener(argv, initPOV);
      //  listener.loop();
      //  System.out.println("End of listener loop");
    }

    public Listener(String[] args, Hashtable initPOV) throws IOException, SyntaxException {

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

        PrintStream outs = listenWind.getPrintStream();

        myIns = new BufferedInputStream(new StringBufferInputStream(""));

        myOuts = outs;

        //Lexer lexer = new Lexer(fname, new DataInputStream(myIns));
        //myParser = new Parser(lexer);
        EZLexer lexer = new EZLexer(fname, new DataInputStream(myIns));
        myParser = new EZParser(lexer);

        myPov = PackagePath.standardNameTable(true, initPOV);

        if(!rootRunning) {
            rootRunning = true;
            root.run();
        }

    }

    public void evalInput(InputStream newIns) {
        try {
        //    myOuts.println();
            myIns = newIns;

            //Lexer lexer = new Lexer("", new DataInputStream(myIns));
            EZLexer lexer = new EZLexer("", new DataInputStream(myIns));

            //myParser = myDebugFlag ? new Parser(lexer, myOuts)
            //                       : new Parser(lexer);

            myParser = myDebugFlag ? new EZParser(lexer, myOuts)
                                   : new EZParser(lexer);

//            myParser.swallowAnyNewlines();
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
        // JAY - a bit of a hack to allow lines with only comments
        // to exist - a cursory examination of the byacc generated parser
        // implies that catching the SyntaxException with message "syntax error"
        // will catch the exact situation when a blank line (or comment-only)
        // line was presented to the parser. XXX Need to double check this.
        try {
            expr = myParser.parseCommand();
        } catch (SyntaxException ex) {
            if(!ex.getMessage().equals("syntax error"))
                throw ex;
             else return true;
            }

        if (expr == null) {
            return false;
        }

        if (myExpandFlag) {
            myOuts.print("Expression expanded: ");
            expr.printOn(myOuts, 2);
            myOuts.println("");
        }

        Object result = expr.eval(myPov);
        myOuts.println("value: " + result);

        //myParser.swallowAnyNewlines();
 //       if (!myParser.isEndOfFile()) {
 //           throw new SyntaxException("Expected end of line or file");
//        }

        return true;
    }
}

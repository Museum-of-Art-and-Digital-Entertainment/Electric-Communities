package ec.ez.ezvm;

import ec.ez.runtime.ParseNode;
import java.io.PrintStream;
import java.io.IOException;

/**
 *
 */
public class RequestNode extends ParseNode {

    static private Expr NO_ARGS[] = {};

    private String myVerb;
    private Expr[] myArgs;

    public RequestNode(String verb, Expr[] args) {
        myVerb = verb;
        myArgs = args;
    }

    public RequestNode(String verb) {
        myVerb = verb;
        myArgs = NO_ARGS;
    }

    public RequestNode(String verb, Expr arg0) {
        Expr args[] = { arg0 };
        myVerb = verb;
        myArgs = args;
    }

    public RequestNode(String verb, Expr arg0, Expr arg1) {
        Expr args[] = { arg0, arg1 };
        myVerb = verb;
        myArgs = args;
    }

    public RequestNode(String verb, Expr arg0, Expr arg1, Expr arg2) {
        Expr args[] = { arg0, arg1, arg2 };
        myVerb = verb;
        myArgs = args;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print(myVerb);
        if (myArgs.length >= 1) {
            printListOn("(", myArgs, ", ", ")", os, indent);
        }
    }

    public String verb() { return myVerb; }
    public Expr[] args() { return myArgs; }
}


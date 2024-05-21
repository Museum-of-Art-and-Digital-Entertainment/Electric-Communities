package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.SourceSpan;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.prim.EZUniversal;
import ec.ez.prim.ScriptMaker;
import java.io.PrintStream;
import java.io.IOException;


/**
 * BNF: expr request <p>
 *
 * Tells the expression's value to perform the request now.
 */
public class CallExpr extends Expr {

    private Expr myRecipient;
    private RequestNode myRequest;

    public CallExpr(Expr recipient, RequestNode request) {
        myRecipient = recipient;
        myRequest = request;
    }

    public CallExpr(Expr recipient, String verb, Expr[] args) {
        this(recipient, new RequestNode(verb, args));
    }

    public CallExpr(Expr recipient, String verb) {
        this(recipient, new RequestNode(verb));
    }

    public CallExpr(Expr recipient, String verb, Expr arg0) {
        this(recipient, new RequestNode(verb, arg0));
    }

    public CallExpr(Expr recipient, String verb, Expr arg0, Expr arg1) {
        this(recipient, new RequestNode(verb, arg0, arg1));
    }

    public CallExpr(Expr recipient, String verb, Expr arg0, Expr arg1, Expr arg2) {
        this(recipient, new RequestNode(verb, arg0, arg1, arg2));
    }

    public void printOn(PrintStream os, int indent) throws IOException {
  //      os.print("(");
        myRecipient.printOn(os, indent);
        os.print(" ");
        myRequest.printOn(os, indent);
  //      os.print(")");
    }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object receiver = myRecipient.eval(pov);
        Expr[] argNodes = myRequest.args();
        Object[] args = new Object[argNodes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = argNodes[i].eval(pov);
        }
        try {
            return EZUniversal.perform(receiver, myRequest.verb(), args);

        } catch (Exception ex) {
            CallExpr.traceEx(ex, myRequest.verb(), optSource());
            throw ex;
        }
    }

    static public void traceEx(Exception ex,
                               String verb,
                               SourceSpan optSource) {
        //XXX do something
    }

    public Expr recipient() {
        return myRecipient;
    }

    public RequestNode request() {
        return myRequest;
    }
}


package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "try" block [ "catch" param block ] [ "finally" block ] <p>
 *
 * Evaluates the try-block, but should it throw an Exception (not a
 * Throwable!) and if there is a 'catch' clause, bind param to the
 * Exception and evaluate the catch-block.  If a finally block is
 * provided, it is evaluated on the way out, but it's value isn't
 * used.  The value of the try expression is the value of the
 * try-block, or the value of the catch-block if the try-block threw
 * an Exception. <p>
 *
 * @see ec.ez.ezvm.EscapeExpr
 * @see ec.ez.ezvm.ThrowExpr
 */
public class TryExpr extends Expr {

    private Expr myTryExpr;
    private Pattern myOptPattern;
    private Expr myOptCatcher;
    private Expr myOptFinally;

    public TryExpr(Expr tryExpr,
                   Pattern optPattern, Expr optCatcher,
                   Expr optFinally) {
        if (optCatcher == null && optPattern != null) {
            throw new RuntimeException("can't have catch pattern w/o "
                                       +"catch block");
        }
        myTryExpr = tryExpr;
        myOptPattern = optPattern;
        myOptCatcher = optCatcher;
        myOptFinally = optFinally;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("try ");
        myTryExpr.printAsBlockOn(os, indent);
        if(myOptCatcher != null) {
            os.print("catch ");
            if(myOptPattern != null) {
                myOptPattern.printOn(os, indent);
                os.print(" ");
            }
            myOptCatcher.printAsBlockOn(os, indent);
        }
        if(myOptFinally != null) {
            os.print("finally ");
            myOptFinally.printAsBlockOn(os, indent);
        }
    }

    public Expr tryExpr()    { return myTryExpr; }
    public Pattern optPattern()  { return myOptPattern; }
    public Expr optCatcher() { return myOptCatcher; }
    public Expr optFinally() { return myOptFinally; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        try {
            return myTryExpr.eval(pov.sprout());
        } catch (Exception ex) {
            if (myOptCatcher != null) {
                NameTable catchPov = pov.sprout();
                if (myOptPattern != null) {
                    catchPov = myOptPattern.mustBind((NameTableEditor)catchPov,
                                                   ex);
                }
                return myOptCatcher.eval(catchPov);
            } else {
                throw ex;
            }
        } finally {
            if (myOptFinally != null) {
                myOptFinally.eval(pov.sprout());
            }
        }
    }
}


package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "throw" ":" expr <p>
 *
 * Throws the value of the expression as an Exception.  If 'expr'
 * evaluates to an actual Java Exception, that is thrown.  Otherwise,
 * an Exception is thrown containing the value of 'expr' converted to
 * a string.  This applies even if 'expr's value is a non-Exception
 * Throwable.  EZ does not throw or catch Errors. <p>
 *
 * @see ec.ez.ezvm.TryExpr
 * @see java.lang.Error
 */
public class ThrowExpr extends Expr {

    private Expr myProblemExpr;

    public ThrowExpr(Expr problemExpr) {
        myProblemExpr = problemExpr;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("throw(");
        myProblemExpr.printOn(os, indent);
        os.print(")");
    }

    public Expr problemExpr() { return myProblemExpr; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object problem = myProblemExpr.eval(pov);
        if (problem instanceof Exception) {
            throw (Exception)problem;
        } else {
            throw new Exception("ez: " + problem);
        }
    }
}

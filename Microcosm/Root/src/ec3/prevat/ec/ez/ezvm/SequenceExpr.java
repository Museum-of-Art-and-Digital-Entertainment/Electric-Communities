package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: expr ";" expr <p>
 *
 * Do one and then the other.  Evaluates to the value of the second.
 */
public class SequenceExpr extends Expr {

    private Expr myFirst;
    private Expr mySecond;

    public SequenceExpr(Expr first, Expr second) {
        myFirst = first;
        mySecond = second;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("(");
        myFirst.printOn(os, indent);
        os.print(";");
        mySecond.lnPrintOn(os, indent);
        os.print(")");
    }

    public Expr first() { return myFirst; }
    public Expr second() { return mySecond; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        myFirst.eval(pov);
        return mySecond.eval(pov);
    }
}


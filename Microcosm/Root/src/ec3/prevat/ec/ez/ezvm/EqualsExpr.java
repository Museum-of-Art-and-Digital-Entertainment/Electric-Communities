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
 * BNF: expr == expr <p>
 *
 * Tests if they evaluate to values considered to be equal
 */
public class EqualsExpr extends Expr {

    /**
     * XXX Must design a decent, reliable, and secure equality.  The
     * following is just a stand-in.
     */
    static public boolean areEquals(Object a, Object b) {
        return (a == b) || (a.equals(b) && b.equals(a));
    }

    private Expr myLeft;
    private Expr myRight;

    public EqualsExpr(Expr left, Expr right) {
        myLeft = left;
        myRight = right;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("(");
        myLeft.printOn(os, indent);
        os.print(" == ");
        myRight.printOn(os, indent);
        os.print(")");
    }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object a = myLeft.eval(pov);
        Object b = myRight.eval(pov);
        if (areEquals(a, b)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Expr left() {
        return myLeft;
    }

    public Expr right() {
        return myRight;
    }
}


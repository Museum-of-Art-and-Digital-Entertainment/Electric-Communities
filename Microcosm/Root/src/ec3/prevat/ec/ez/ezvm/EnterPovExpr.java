package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "enterPov" expr block <p>
 *
 * Evaluate 'expr' in the current pov.  It must evaluate to a NameTable, in
 * which case, evaluate block in that pov.
 *
 * @see ec.ez.ezvm.PovExpr
 * @see ec.ez.ezvm.NameTable
 */
public class EnterPovExpr extends Expr {

    private Expr myPovExpr;
    private Expr myBlock;

    public EnterPovExpr(Expr povExpr, Expr block) {
        myPovExpr = povExpr;
        myBlock = block;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("enterPov (");
        myPovExpr.printOn(os, indent);
        os.print(") ");
        myBlock.printAsBlockOn(os, indent);
    }

    public Expr povExpr() { return myPovExpr; }
    public Expr block() { return myBlock; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        NameTable newPov = (NameTable)(myPovExpr.eval(pov));
        return myBlock.eval(newPov);
    }
}

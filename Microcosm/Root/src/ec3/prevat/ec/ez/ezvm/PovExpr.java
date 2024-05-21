package ec.ez.ezvm;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "pov" <p>
 *
 * @see ec.ez.ezvm.EnterPovExpr
 * @see ec.ez.ezvm.NameTable
 */
public class PovExpr extends Expr {

    public PovExpr() {}

    public Object eval(NameTable pov) throws Exception {
        return pov;
    }

    public void printOn(PrintStream os, int indent) {
        os.print("pov");
    }
}


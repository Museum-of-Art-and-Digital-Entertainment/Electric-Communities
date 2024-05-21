package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "loop" block <p>
 *
 * Evaluates 'block' repeatedly until the loop is exited somehow,
 * such as by the Ejector of an enclosing EscapeExpr
 */
public class LoopExpr extends Expr {

    private Expr myBlock;

    public LoopExpr(Expr block) {
        myBlock = block;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("loop ");
        myBlock.printAsBlockOn(os, indent);
    }

    public Expr block() { return myBlock; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        while (true) {
            myBlock.eval(pov.sprout());
        }
    }
}

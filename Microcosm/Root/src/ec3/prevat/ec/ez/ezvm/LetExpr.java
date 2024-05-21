package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "let" block <p>
 *
 * Evaluates 'block' in a lexically nested scope
 */
public class LetExpr extends Expr {

    private Expr myBlock;

    public LetExpr(Expr block) {
        myBlock = block;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("let ");
        myBlock.printAsBlockOn(os, indent);
    }

    public Expr block() { return myBlock; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        return myBlock.eval(pov.sprout());
    }
}

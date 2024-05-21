package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.Ejector;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "escape" pattern block <p>
 *
 * Evaluates 'block' in an environment where 'pattern' is bound to an
 * Ejector.  If the Ejector is never called, block evalutes normally.
 * If Ejector is called during the execution of the block, the block
 * exits, evaluating to Ejector's argument. <p>
 *
 * On the way out, any 'finally' clauses are run. <p>
 * @see ec.ez.ezvm.TryExpr
 */
public class EscapeExpr extends Expr {

    private Pattern myExitPattern;
    private Expr myRValue;

    public EscapeExpr(Pattern exitPattern, Expr rValue) {
        myExitPattern = exitPattern;
        myRValue = rValue;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("escape ");
        myExitPattern.printOn(os, indent);
        os.print(" ");
        myRValue.printAsBlockOn(os, indent);
    }

    public Pattern exitPattern() { return myExitPattern; }
    public Expr rValue() { return myRValue; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Ejector ejector = new Ejector();
        NameTable subPov = myExitPattern.mustBind(pov.sprout(), ejector);
        try {
            return myRValue.eval(subPov);
        } catch (Ejection ej) {
            return ejector.result(ej);
        } finally {
            ejector.disable();
        }
    }
}

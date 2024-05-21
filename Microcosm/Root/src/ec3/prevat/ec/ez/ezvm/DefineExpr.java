package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: "define" pattern ":=" expr <p>
 *
 * Introduce a binding for 'pattern' in the nearest layer of the
 * current environment.  'pattern' must not yet have a binding in this
 * layer.
 */
public class DefineExpr extends Expr {

    private Pattern myPattern;
    private Expr myRValue;

    public DefineExpr(Pattern pattern, Expr rValue) {
        myPattern = pattern;
        myRValue = rValue;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("define ");
        myPattern.printOn(os, indent);
        os.print(" := ");
        myRValue.printOn(os, indent);
    }

    public Pattern pattern() { return myPattern; }
    public Expr rValue() { return myRValue; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object result = myRValue.eval(pov);
        //XXX Weird.  Should I really ignore the result of mustBind()?
        myPattern.mustBind((NameTableEditor)pov, result);
        return result;
    }
}


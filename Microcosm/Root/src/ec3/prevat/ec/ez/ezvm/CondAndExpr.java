package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF:  expr "&&" expr
 */
public class CondAndExpr extends Expr {

    private Expr myLeft;
    private Expr myRight;

    public CondAndExpr(Expr left, Expr right) {
        myLeft = left;
        myRight = right;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("(");
        myLeft.printOn(os, indent);
        os.print(" && ");
        myRight.printOn(os, indent);
        os.print(")");
    }

    public Expr left() { return myLeft; }
    public Expr right() { return myRight; }


    public Object eval(NameTable pov) throws Exception, Ejection {
        NameTable newPov = myLeft.testBind(pov.sprout());
        if (newPov != null) {
            return myRight.eval(newPov);
        } else {
            return Boolean.FALSE;
        }
    }        


    public NameTable testBind(NameTable pov) throws Exception, Ejection {
        NameTable newPov = myLeft.testBind(pov.sprout());
        if (newPov != null) {
            return myRight.testBind(newPov);
        } else {
            return null;
        }
    }        
}

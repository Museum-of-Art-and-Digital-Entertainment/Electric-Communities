package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: noun ":=" expr <p>
 *
 * Changes the binding for 'noun' in the current environment to be the
 * value of 'expr'.  'noun' must already be bound.
 */
public class AssignExpr extends Expr {

    private String myNoun;
    private Expr myRValue;

    public AssignExpr(String noun, Expr rValue) {
        myNoun = noun;
        myRValue = rValue;
    }

    public String noun() { return myNoun; }
    public Expr rValue() { return myRValue; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        Object result = myRValue.eval(pov);
        ((NameTableEditor)pov).put(myNoun, result);
        return result;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print(myNoun + " := ");
        myRValue.printOn(os, indent);
    }

}


package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * 
 */
public class IfExpr extends Expr {

    private Expr myTest;
    private Expr myThen;
    private Expr myElse;

    public IfExpr(Expr test, Expr then, Expr els) {
        myTest = test;
        myThen = then;
        myElse = els;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("if (");
        myTest.printOn(os, indent);
        os.print(") ");
        myThen.printAsBlockOn(os, indent);
        os.print(" else ");
        myElse.printAsBlockOn(os, indent);
    }

    public Expr test() { return myTest; }
    public Expr then() { return myThen; }
    public Expr els()  { return myElse; }

    public Object eval(NameTable pov) throws Exception, Ejection {
        NameTable newPov = myTest.testBind(pov.sprout());
        if (newPov != null) {
            //the then part executes in a scope containing bindings
            //from the conditional 
            return myThen.eval(newPov.sprout());
        } else {
            //else is evaluated in a scope with no bindings from
            //the conditional 
            return myElse.eval(pov.sprout());
        }
    }        
}

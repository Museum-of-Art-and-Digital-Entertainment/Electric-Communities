package ec.ez.ezvm;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: noun <p>
 *
 * Returns the noun value
 */
public class NounExpr extends Expr {

    String myName;

    public NounExpr() {}

    public NounExpr(String name) {
        myName = name;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print(myName);
    }

    public String name() { return myName; }

    public Object eval(NameTable pov) throws Exception {
        return pov.get(myName);
    }

 //   public Object get(String theName) {
 //       System.out.println("In getter");
 //       return "Meow";
 //   }
}


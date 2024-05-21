package ec.ez.ezvm;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;

/**
 * BNF: literal <p>
 *
 * Returns the literal value
 */
public class LiteralExpr extends Expr {

    private String myPrintRep;
    private Object myValue;

    public LiteralExpr(String printRep, Object value) {
        myPrintRep = printRep;
        myValue = value;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print(myPrintRep);
    }

    public String printRep() { return myPrintRep; }
    public Object value() { return myValue; }

    public Object eval(NameTable pov) throws Exception {
        return myValue;
    }
}


package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.SourceSpan;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.prim.EZUniversal;
import ec.ez.prim.ScriptMaker;
import java.io.PrintStream;
import java.io.IOException;


/**
 * BNF: expr =~ pattern <p>
 *
 * Tests if the expression matches the pattern
 */
public class MatchBindExpr extends Expr {

    private Expr mySpecimen;
    private Pattern myPattern;

    public MatchBindExpr(Expr specimen, Pattern pattern) {
        mySpecimen = specimen;
        myPattern = pattern;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("(");
        mySpecimen.printOn(os, indent);
        os.print(" =~ ");
        myPattern.printOn(os, indent);
        os.print(")");
    }

    public Object eval(NameTable pov) throws Exception, Ejection {
        if (testBind(pov) != null) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public NameTable testBind(NameTable pov) throws Exception, Ejection {
        Object spec = mySpecimen.eval(pov);
        return myPattern.testBind(pov.sprout(), spec);
    }

    public Expr specimen() {
        return mySpecimen;
    }

    public Pattern pattern() {
        return myPattern;
    }
}


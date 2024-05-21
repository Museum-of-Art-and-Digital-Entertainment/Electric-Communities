package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.ParseNode;
import ec.ez.collect.NameTable;
import ec.ez.prim.JavaMemberNode;
import java.io.PrintStream;
import java.io.IOException;


/**
 * Those ParseNodes that--after expansion--define the kernel
 * expressions evaluated by the EZ Virtual Machine.
 */
public abstract class Expr extends ParseNode {

    /**
     * Used to evaluate this expression to a value.
     */
    public abstract Object eval(NameTable pov) throws Exception, Ejection;


    /**
     * Used to evaluate this expression for flow of control.  If this
     * expression evaluates to true, testBind() returns a NameTable
     * in which "then" code should be executed.  If this expression
     * evaluates to false, testBind() returns null. <p>
     *
     * The default behavior of testBind() is to call eval() and return pov
     * or null accordingly.
     *
     * @return nullOk;
     */
    public NameTable testBind(NameTable pov) throws Exception, Ejection {
        Boolean result = (Boolean)JavaMemberNode.coerce(eval(pov),
                                                        Boolean.class);
        if (result.booleanValue()) {
            return pov;
        } else {
            return null;
        }
    }


    public void printAsBlockOn(PrintStream os, int indent) throws IOException {
        os.print("{");
        lnPrintOn(os, indent +1);
        lnPrintOn(os, indent, "}");
    }
}


package ec.ez.prim;

import java.io.PrintStream;


/*package*/ class PrintFunc {

    private PrintStream myOuts;

    /*package*/PrintFunc(PrintStream outs) {
        myOuts = outs;
    }

    public Object run(Object obj) {
        myOuts.print(obj);
        return obj;
    }
}

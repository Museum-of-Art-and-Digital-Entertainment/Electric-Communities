package ec.ez.prim;

import java.io.PrintStream;


/*package*/ class PrintlnFunc {

    private PrintStream myOuts;

    /*package*/PrintlnFunc(PrintStream outs) {
        myOuts = outs;
    }

    public Object run(Object obj) {
        myOuts.println(obj);
        return obj;
    }
}

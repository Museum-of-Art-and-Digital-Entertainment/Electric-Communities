package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.Script;
import ec.ez.runtime.ParseNode;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import java.io.PrintStream;
import java.io.IOException;
import ec.ez.prim.EZImpl_$_Impl;

public class ForRequestNode extends ParseNode implements Script {

    private String myVParam;
    private String myPParam;
    private Expr myBody;

    public ForRequestNode(String vParam, String pParam, Expr body) {
        myVParam = vParam;
        myPParam = pParam;
        myBody = body;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("match [" + myVParam + ", " + myPParam + "] ");
        myBody.printAsBlockOn(os, indent);
    }

    public String vParam() { return myVParam; }
    public String pParam() { return myPParam; }
    public Expr   body()   { return myBody; }

    public Object execute(Object rec, String verb, Object[] args)
         throws Exception, Ejection {

        NameTableEditor pov = ((EZImpl_$_Impl)rec).pov().sprout();
        pov.introduce(myVParam, verb);
        pov.introduce(myPParam, args);
        return myBody.eval(pov);
    }
}


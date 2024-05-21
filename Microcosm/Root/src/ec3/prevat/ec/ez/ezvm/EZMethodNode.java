package ec.ez.ezvm;

import ec.ez.runtime.Ejection;
import ec.ez.runtime.MethodNode;
import ec.ez.collect.NameTable;
import ec.ez.collect.NameTableEditor;
import ec.ez.collect.NotFoundException;
import java.io.PrintStream;
import java.io.IOException;


public class EZMethodNode extends MethodNode {

    private String myVerb;
    private Pattern[] myPatterns;
    private Expr myBody;

    public EZMethodNode(String verb, Pattern[] patterns, Expr body) {
        myVerb = verb;
        myPatterns = patterns;
        myBody = body;
    }

    public void printOn(PrintStream os, int indent) throws IOException {
        os.print("to " + myVerb);
        if (myPatterns.length != 0) {
            os.print("(");
            int highPattern = myPatterns.length - 1;
            for (int i = 0; i < highPattern; i++) {
                myPatterns[i].printOn(os, indent);
                os.print(", ");
            }
            myPatterns[highPattern].printOn(os, indent);
            os.print(")");
        }
        os.print(" ");
        myBody.printAsBlockOn(os, indent);
    }

    public String verb() { return myVerb; }
    public int arity() { return myPatterns.length; }
    public Pattern[] patterns() { return myPatterns; }
    public Expr body() { return myBody; }


    public Object execute(Object rec, String aVerb, Object[] args)
         throws Exception, Ejection
    {
        if (! verb().equals(aVerb) || arity() != args.length) {
            throw new NotFoundException(aVerb + "/" + args.length);
        }
        NameTable pov = ((EZImpl_$_Impl)rec).pov().sprout();
        for (int i = 0; i < args.length; i++) {
            pov = myPatterns[i].mustBind((NameTableEditor)pov, args[i]);
        }
        return myBody.eval(pov);
    }
}


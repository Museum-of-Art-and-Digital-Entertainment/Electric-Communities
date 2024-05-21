package ec.ez.ezvm;
import ec.ez.collect.NotFoundException;
import java.io.PrintStream;
import java.io.IOException;

public abstract class MethodNode extends ParseNode implements Script {

    public abstract String verb();
    public abstract int arity();

    public String mangle() {
        return verb() + "/" + arity();
    }
}


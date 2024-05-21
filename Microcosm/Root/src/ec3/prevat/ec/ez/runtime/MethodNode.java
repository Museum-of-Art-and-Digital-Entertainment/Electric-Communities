package ec.ez.runtime;


public abstract class MethodNode extends ParseNode implements Script {

    public abstract String verb();
    public abstract int arity();

    public String mangle() {
        return verb() + "/" + arity();
    }
}


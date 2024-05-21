package ec.ez.ezvm;
import ec.ez.collect.NameTable;
import java.io.PrintStream;
import java.io.IOException;
import ec.ez.collect.NotFoundException;
import ec.ez.prim.PackagePath;

/**
 * BNF: noun <p>
 *
 * Returns the noun value
 */
public class PathExpr extends NounExpr {

    public PathExpr(String name) {
        myName = name;
    }

    public Object eval(NameTable pov) throws Exception {
        try {
            return pov.get(myName);
        } catch (NotFoundException e) {
            return new PackagePath(myName + ".");
        }
    }
}


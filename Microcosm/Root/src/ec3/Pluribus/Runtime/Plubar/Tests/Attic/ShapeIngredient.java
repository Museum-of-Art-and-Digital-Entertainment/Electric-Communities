package ec.pl.runtime.tests;

import ec.pl.runtime.Ingredient;
import ec.pl.runtime.istBase;
import ec.pl.runtime.PresenceEnvironment;

public class ShapeIngredient extends Ingredient {
    protected istShape myState = null;

    public ShapeIngredient (PresenceEnvironment env) {
        super(env);
    }

    public istBase getClientState() { return myState; }

    /** For initialization. */
    public void init(istBase state) {
        if (state != null) {
            myState = (istShape)state;
        } else {
            myState = new istShape();
        }
    }

    public void uSetShape(String shape) {
        System.out.println("  Changing shape from " + myState.shape +
                           " to " + shape);
        myState.shape = shape;
    }
    public void pHostSetShape(String shape) {
        System.out.println("  Called " + myName + ".pHostSetShape(" +
                           shape + ")");
    }
    public void pClientSetShape(String shape) {
        System.out.println("  Called " + myName + ".pClientSetShape(" +
                           shape + ")");
    }
}


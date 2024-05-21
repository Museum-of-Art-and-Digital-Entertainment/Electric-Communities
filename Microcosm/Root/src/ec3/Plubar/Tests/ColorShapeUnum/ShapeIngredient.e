package ec.plubar.tests.ColorShapeUnum;

import ec.plubar.Ingredient;
import ec.plubar.istBase;

public eclass ShapeIngredient extends Ingredient {
    protected istShape myState = null;

    public ShapeIngredient () {
        super();
    }

    public istBase getClientState() { return myState; }

    /** For initialization. */
    public void init() {
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


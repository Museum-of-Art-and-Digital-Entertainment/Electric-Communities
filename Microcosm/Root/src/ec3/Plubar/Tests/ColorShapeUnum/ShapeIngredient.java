package ec.plubar.tests.ColorShapeUnum;

import ec.plubar.Ingredient;
import ec.plubar.istBase;

public class ShapeIngredient extends Ingredient {
    protected istShape myState = null;

    public ShapeIngredient () {
        super();
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


package ec.plubar.tests.ColorShapeUnum;

import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;
import ec.plubar.istBase;

public eclass ColorIngredient extends Ingredient implements IngredientJif {
    protected istColor myState = null;

    public ColorIngredient () {
    }

    public istBase getClientState() { return myState; }

    /** For initialization. */
    public void init() {
    }

    public void uSetColor(String color) {
        System.out.println("  Changing color from " + myState.color +
                           " to " + color);
        myState.color = color;
    }
    public void pHostSetColor(String color) {
        System.out.println("  Called " + myName + ".pHostSetColor(" +
                           color + ")");
    }
    public void pClientSetColor(String color) {
        System.out.println("  Called " + myName + ".pClientSetColor(" +
                           color + ")");
    }
}

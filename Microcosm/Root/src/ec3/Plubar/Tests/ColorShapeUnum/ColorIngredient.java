package ec.plubar.tests.ColorShapeUnum;

import ec.plubar.Ingredient;
import ec.plubar.istBase;

public class ColorIngredient extends Ingredient {
    protected istColor myState = null;

    public ColorIngredient () {
        super();
    }

    public istBase getClientState() { return myState; }

    /** For initialization. */
    public void init(istBase state) {
        if (state != null) {
            myState = (istColor)state;
        } else {
            myState = new istColor();
        }
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

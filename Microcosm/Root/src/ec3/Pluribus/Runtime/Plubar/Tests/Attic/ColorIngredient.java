package ec.pl.runtime.tests;

import ec.pl.runtime.Ingredient;
import ec.pl.runtime.istBase;
import ec.pl.runtime.PresenceEnvironment;

public class ColorIngredient extends Ingredient {
    protected istColor myState = null;

    public ColorIngredient (PresenceEnvironment env) {
        super(env);
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

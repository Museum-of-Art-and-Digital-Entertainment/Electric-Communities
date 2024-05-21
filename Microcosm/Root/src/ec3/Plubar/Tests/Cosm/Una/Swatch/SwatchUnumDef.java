package ec.plubar.tests.cosm.una.swatch;

import ec.plubar.UnumException;

public class SwatchUnumDef extends ec.plubar.tests.cosm.una.prop.PropUnumDef {

    public SwatchUnumDef (String name) throws UnumException {
        super(name);

        String ing = "ec.cosm.ingredients.";

        // PropIngredientStructSet \
        // _and_ _Texturizer_Struct \
        //     _Handles_Unum_Host_Client(Texturizer)
        this.addIngredientRole("Texturizer", true);
        this.addUnumInterface(ing+"texturize.ukTexturizer$kind", "Texturizer");
        this.addHostPresenceInterface(ing+"texturize.pkTexturizerHost$kind", "Texturizer");
        this.addClientPresenceInterface(ing+"texturize.pkTexturizerClient$kind", "Texturizer");
    }
}
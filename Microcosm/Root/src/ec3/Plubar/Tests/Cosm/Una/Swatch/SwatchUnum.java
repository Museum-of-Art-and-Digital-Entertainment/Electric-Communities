package ec.plubar.tests.cosm.una.swatch;

import ec.cosm.ingredients.texturize.iiTexturizer$iicode;
import ec.plubar.Unum;
import ec.plubar.UnumException;

public class SwatchUnum {

    public static Unum createUnum() throws UnumException {
        Unum unum = null;
        SwatchUnumDef def = new SwatchUnumDef("Swatch");
        def.finishDefinition();

        unum = def.createUnum();
        
        unum.setIngredient("Texturizer", new iiTexturizer$iicode());
        
        unum.finishUnum();
        //KSSHack This is where you'd build a SoulState and pass it in
        //KSSHack to initialize the ingredients
        unum.init(null);

        return unum;
    }
}
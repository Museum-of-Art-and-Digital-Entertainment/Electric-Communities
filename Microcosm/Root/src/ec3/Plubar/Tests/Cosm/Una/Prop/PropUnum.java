package ec.plubar.tests.cosm.una.prop;

import ec.cosm.ingredients.behavior.iiECBehaviorManager$iicode;
import ec.cosm.ingredients.describe.iiSimpleDescriber$iicode;
import ec.cosm.ingredients.destination.iiWalkToThis$iicode;
import ec.cosm.ingredients.property.iiECProperty$iicode;
import ec.plubar.Unum;
import ec.plubar.UnumException;

public class PropUnum {

    public static Unum createUnum() throws UnumException {
        Unum unum = null;
        PropUnumDef def = new PropUnumDef("Prop");
        def.finishDefinition();

        unum = def.createUnum();
        
        unum.setIngredient("Describer", new iiSimpleDescriber$iicode());
        unum.setIngredient("Property", new iiECProperty$iicode());
        unum.setIngredient("Destination", new iiWalkToThis$iicode());
        unum.setIngredient("Portable", null);
        unum.setIngredient("Interface", null);
        unum.setIngredient("Cloneable", null);
        unum.setIngredient("BehaviorManager", new iiECBehaviorManager$iicode());
        unum.setIngredient("VerbManager", null);
        unum.setIngredient("Containership", null);
        unum.setIngredient("Compositable", null);
        
        unum.finishUnum();
        //KSSHack This is where you'd build a SoulState and pass it in
        //KSSHack to initialize the ingredients
        unum.init(null);

        return unum;
    }
}
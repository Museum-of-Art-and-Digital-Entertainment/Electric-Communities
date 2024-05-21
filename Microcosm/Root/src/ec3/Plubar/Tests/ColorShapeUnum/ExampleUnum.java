package ec.plubar.tests.ColorShapeUnum;

import ec.plubar.Ingredient;
import ec.plubar.SoulState;
import ec.plubar.Unum;
import ec.plubar.UnumDefinition;
import ec.plubar.UnumException;

class ExampleUnum {
    static Unum createUnum() throws UnumException {
        UnumDefinition def = new UnumDefinition("ColorShape");
        Unum unum = null;
                
        def.addIngredientRole("color", true);
        def.addIngredientRole("shape", true);
        def.addUnumMessage("uSetColor", "color");
        def.addUnumMessage("uSetShape", "shape");        
        def.addHostPresenceMessage("pHostSetColor", "color");
        def.addHostPresenceMessage("pHostSetShape", "shape");        
        def.addClientPresenceMessage("pClientSetColor", "color");
        def.addClientPresenceMessage("pClientSetShape", "shape");
        def.addClientPresenceMessage("pClientSetShape", "shape");
        def.addPresenceInterface("ec.plubar.tests.ColorShapeUnum.TestInterface", "shape");
        def.addUnumInterface(ec.plubar.tests.ColorShapeUnum.TestInterface.class, "color");
        def.addUnumInterface(ec.plubar.tests.ColorShapeUnum.ColorIngredient_$_Intf.class, "color");
    
        def.finishDefinition();

        unum = def.createUnum();
        
        ColorIngredient color = new ColorIngredient();
        unum.setIngredient("color", color);
        ShapeIngredient shape = new ShapeIngredient();
        unum.setIngredient("shape", shape);

        SoulState soulState = new SoulState();
        soulState.put("color", new istColor());
        soulState.put("shape", new istShape());
        
        unum.finishUnum();
        unum.init(soulState);

        return unum;
    }
}

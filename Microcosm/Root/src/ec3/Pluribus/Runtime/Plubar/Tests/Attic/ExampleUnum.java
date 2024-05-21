package ec.pl.runtime.tests;

import ec.pl.runtime.Unum;
import ec.pl.runtime.UnumDefinition;
import ec.pl.runtime.UnumException;
import ec.pl.runtime.PresenceEnvironment;

class ExampleUnum {
    static Unum createUnum() throws UnumException{
        UnumDefinition def = new UnumDefinition(2);
        Unum unum = null;
                
//        try {
            def.addIngredientRole("color", true);
            def.addIngredientRole("shape", true);
            def.addUnumMessage("uSetColor", "color");
            def.addUnumMessage("uSetShape", "shape");        
            def.addHostPresenceMessage("pHostSetColor", "color");
            def.addHostPresenceMessage("pHostSetShape", "shape");        
            def.addClientPresenceMessage("pClientSetColor", "color");
            def.addClientPresenceMessage("pClientSetShape", "shape");
            def.addClientPresenceMessage("pClientSetShape", "shape");
            def.addPresenceInterface("ec.pl.runtime.tests.TestInterface", "shape");
            def.addUnumInterface(ec.pl.runtime.tests.TestInterface.class, "color");
            def.addUnumInterface(ec.pl.runtime.tests.ColorIngredient.class, "color");
        
            def.finishDefinition();

            unum = def.createUnum();
            PresenceEnvironment env = new PresenceEnvironment();
            
            unum.setIngredient("color", new ColorIngredient(env));
            unum.setIngredient("shape", new ShapeIngredient(env));
            
            unum.finishUnum();
            unum.init(null);
//        } catch (UnumException exc) {
//            System.out.println("Error creating unum: " + exc + "(" + unum + ")");
//        }

        return unum;
    }
}

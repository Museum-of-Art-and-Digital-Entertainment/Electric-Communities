package ec.pl.runtime.plubar.examples.property;

class Property {
    static void addIngredientRoles() {
        def.addIngredientRole("behaviorManager");
        def.addIngredientRole("cloneable");
            . . .
    }

    static void addUnumMessages() {
    }

    static void addHostPresenceMessages() {
        def.addHostPresenceMessage("pHostSendToContainer",    "containership");
        def.addHostPresenceMessage("pHostSendToContainerUnum","containership");
            . . .
    }

    static void addClientPresenceMessages() {
        def.addClientPresenceMessage("pClientPerformGesture", "compositable");
        def.addClientPresenceMessage("pClientSetMood",        "compositable");
            . . .
    }

    static void setIngredients() {
        unum.setIngredient("behaviorManager", new iiECBehaviorManager(...));
        unum.setIngredient("cloneable",       new iiCloneable(...));
            . . .
    }

    static Unum createUnum() {
        UnumDefinition def = new UnumDefinition();
        addIngredientRoles();
        addUnumMessages();
        addHostPresenceMessages();
        addClientPresenceMessages();
        def.finishDefinition();
        Unum unum = def.createUnum();
        setIngredients();
        unum.finishUnum();
    }
}

class Swatch extends Property {
    static void addIngredientRoles() {
        super.addIngredientRoles();
        def.addIngredientRole("texturizer");
    }

    static void addUnumMessages() {
        super.addUnumMessages();
        def.addUnumMessage("uTexturize",    "texturizer");
    }

    static void addHostPresenceMessages() {
        super.addHostPresenceMessages();
    }

    static void addClientPresenceMessages() {
        super.addClientPresenceMessages();
    }

    static void setIngredients() {
        super.addClientPresenceMessages();
        unum.setIngredient("texturizer", new iiTexturizer(...));
    }

    static Unum createUnum() {
        UnumDefinition def = new UnumDefinition();
        addIngredientRoles();
        addUnumMessages();
        addHostPresenceMessages();
        addClientPresenceMessages();
        def.finishDefinition();
        Unum unum = def.createUnum();
        setIngredients();
        unum.finishUnum();
    }
}

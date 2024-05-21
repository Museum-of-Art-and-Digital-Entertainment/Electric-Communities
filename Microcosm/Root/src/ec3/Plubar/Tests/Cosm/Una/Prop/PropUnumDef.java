package ec.plubar.tests.cosm.una.prop;

import ec.plubar.UnumException;

public class PropUnumDef extends ec.plubar.UnumDefinition {

    public PropUnumDef (String name) throws UnumException {
        super(name);

        String ing = "ec.cosm.ingredients.";
        String obj = "ec.cosm.objects.";

        //_Describer_Struct \
        //    _Handles_Unum_Client(Describer)       \
        this.addIngredientRole("Describer", true);
        this.addUnumInterface(ing+"describe.ukDescriber$kind", "Describer");
        this.addClientPresenceInterface(ing+"describe.pkDescriberClient$kind", "Describer");

        //_and_ _Property_Struct                        \
        //    _Handles_Unum(Property)
        this.addIngredientRole("Property", true);
        this.addUnumInterface(ing+"property.ukProperty$kind", "Property");

        //_and_ _Destination_Struct                     \
        this.addIngredientRole("Destination", true);

        //_and_ _BehaviorManager_Struct                 \
        this.addIngredientRole("BehaviorManager", true);

        //_and_ _Portable_Struct                        \
        //    _Handles_Unum_Client(Portable)            \
        this.addIngredientRole("Portable", true);
        this.addUnumInterface(obj+"ukPortable$kind", "Portable");
        this.addClientPresenceInterface(obj+"pkPortableClient$kind", "Portable");

        //_and_ _Interface_Struct                   \
        //    _Handles_Unum_Client(Interface)           \
        this.addIngredientRole("Interface", true);
        this.addUnumInterface(obj+"ukInterface$kind", "Interface");
        this.addClientPresenceInterface(obj+"pkInterfaceClient$kind", "Interface");

        //_and_ _Cloneable_Struct                   \
        this.addIngredientRole("Cloneable", true);

        //_and_ _VerbManager_Struct                     \
        this.addIngredientRole("VerbManager", true);

        //_and_ _Containership_Struct               \
        //    _Handles_Unum_Host(Containable)       \
        //    _Handles_Unum_Host(Container)             \
        this.addIngredientRole("Containership", true);
        this.addUnumInterface(obj+"ukContainable$kind", "Containership");
        this.addHostPresenceInterface(obj+"pkContainableHost$kind", "Containership");
        this.addUnumInterface(obj+"ukContainer$kind", "Containership");
        this.addHostPresenceInterface(obj+"pkContainerHost$kind", "Containership");

        //_and_ _Compositable_Struct                    \
        //    _Handles_Unum_Host_Client(Compositable) \
        this.addIngredientRole("Compositable", true);
        this.addUnumInterface(obj+"ukCompositable$kind", "Compositable");
        this.addHostPresenceInterface(obj+"pkCompositableHost$kind", "Compositable");
        this.addClientPresenceInterface(obj+"pkCompositableClient$kind", "Compositable");
    }
}
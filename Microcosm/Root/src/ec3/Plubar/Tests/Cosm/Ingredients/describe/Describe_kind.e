/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.describe;

import java.util.Hashtable;
import ec.e.start.EEnvironment;
import ec.plubar.Ingredient;

public einterface ukDescriber$kind {
    uShortDescribe(EResult name, EResult thumbnail);
}

public einterface pkDescriberHost$kind {
}

public einterface pkDescriberClient$kind {
    pUpdateClientState(Object bundle);
}

public einterface ikDescriber$kind extends ukDescriber$kind, pkDescriberHost$kind, pkDescriberClient$kind {
    iDescribe(Hashtable aoBehaviorFacets);
}

public einterface iskDescriber$kind extends ikDescriber$kind {
}

    public interface jiDescriber {
    public void setShortDescription (String shortDescription);
    public void setDescription (String Description);
    public String getShortDescription ();
    public String getDescription ();
  }

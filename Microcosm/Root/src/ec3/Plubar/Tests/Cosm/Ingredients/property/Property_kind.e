/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.property;

import java.util.Hashtable;
import java.util.Vector;
import ec.cosm.objects.eeException;
import ec.cosm.ui.presenter.PropertySheetMessage;
import ec.cosm.ui.presenter.PSProperty;

public einterface ukProperty$kind {
    uPropertySheet();
}

public einterface pkProperty$kind {
}

public einterface ikProperty$kind {
    iPropertyInput(EResult messageOrderer, EResult propertyInputHandler, PropertySheetMessage msg, int source);
    iPropertySheet(Hashtable aoBehaviors, Hashtable propertyBehaviors);
}

public einterface iskProperty$kind extends ukProperty$kind, pkProperty$kind, ikProperty$kind {
}

    public interface jiPropertyManagerFacet {
    public void report(PSProperty property);
    public void accept(EResult status);
    public void forwardProperties(Vector /* PSProperty */ properties,
      EResult result);
  }
    public interface jiPropertyRegistration {
    public jiPropertyManagerFacet registerPropertyPeer(jiPropertyPeer peer);
  }
    public interface jiPropertyPeer {
    public void getProperties(
      EResult /* Vector of PSProperty */ peerProperties);
    public void propose(PSProperty property, EResult status);
  }
    public class eePropertySheetNoPropertiesException
    extends eeException {

    public eePropertySheetNoPropertiesException() {
      super("");
    }
  }
    public class eePropertySheetNoPermissionException
    extends eeException {

    public eePropertySheetNoPermissionException() {
      super("");
    }
  }
    public class eePropertySheetClientNotSupportedException
    extends eeException {

    public eePropertySheetClientNotSupportedException() {
      super("");
    }
  }

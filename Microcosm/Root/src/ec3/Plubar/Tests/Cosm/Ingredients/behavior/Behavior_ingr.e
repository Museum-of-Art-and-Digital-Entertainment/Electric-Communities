/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.behavior;

import java.util.Vector;
import java.util.Hashtable;
import ec.e.run.Trace;
import ec.plubar.Ingredient;
import ec.plubar.IngredientJif;

public interface iiECBehaviorManager$iijif
{
    Hashtable iRequestBehaviors();
}

public eclass iiECBehaviorManager$iicode extends Ingredient
implements IngredientJif, ikBehaviorManager$kind, iiECBehaviorManager$iijif, jiBehaviorManager
{
    private static final Trace theTrace =  new Trace("behaviormanager");
    Hashtable myBehaviorTable;

    /* State bundle 'myState' */
    private ec.cosm.ingredients.behavior.istBehaviorManager myState;
    public iiECBehaviorManager$iicode() {
        super();
    }

    public void initGeneric(Object state) {
        this.init((istBehaviorManager)state);
    }

    public Object jiGetClientState() {
      return myState;
    }
    public Hashtable iRequestBehaviors() {
      if (myBehaviorTable == null) {
        myBehaviorTable = new Hashtable();
      }
      return myBehaviorTable;
    }
    local void init(istBehaviorManager externalState) {
      if (null == externalState)  {
        myState = new istBehaviorManager();
      } else {
        myState = externalState;
      }
      
      if (myBehaviorTable == null)
        myBehaviorTable = new Hashtable();
    }
    local void init() {
      this.init(null);
    }
    emethod iRegisterBehavior(String name, jiBehaviorWrapper behavior) {
      if (theTrace.debug && Trace.ON) theTrace.debugm("registering "+behavior+" with name "+name);

      // since we want to tolerate init misorderings, we handle this here
      if (myBehaviorTable == null)
        myBehaviorTable = new Hashtable();
        
      myBehaviorTable.put(name, behavior);
    }
}

    public class jBehaviorRevoker
    implements jiBehaviorRevoker {
    private boolean myRevoked;
    public jBehaviorRevoker () {
      myRevoked = false;
    }
    public boolean isRevoked () {
      return myRevoked;
    }
    public void revoke () {
      myRevoked = true;
    }
  }

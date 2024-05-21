/* Produced by Pluribus 1.2 version date Feb 20 1998 10:59:20
   from D:/Build/.PlCpp on 20-February-1998 
   This file is machine generated. Don't edit it or you'll be sorry.
*/

package ec.cosm.ingredients.behavior;

import java.util.Hashtable;

public einterface ikBehaviorManager$kind {
    iRegisterBehavior(String name, jiBehaviorWrapper behavior);
}

public interface jiBehaviorManager {

    /**
     * request a master set of behaviors
     * these behaviors will be clonable at will by the (trusted) code which
     * calls this
     */
    Hashtable iRequestBehaviors();

}

    public interface jiBehaviorRevoker {
    // query whether it's been revoked
    public boolean isRevoked ();
    
    // revoke it...
    public void revoke ();
  }
    public interface jiBehaviorWrapper {
    // make a new clone of this wrapper, with the given revoker
    // we clone because these are the faceless items that get created by
    // requestBehaviors, above, and we don't want to have to explicitly specify
    // their class
    
    // Clone this BehaviorWrapper.  The cloned BehaviorWrapper cannot itself
    // be cloned (it will return null if you try).  This is so we can obtain
    // a basic table of these from the ikBehaviorManager, and clone from that
    // basic table, without the cloned objects themselves being reclonable
    // with a different revoker.
    public jiBehaviorWrapper cloneUnclonableWrapper
            (jiBehaviorRevoker sharedRevoker /*, eventually pstate?! */);
  }
    public abstract eclass jBehaviorWrapper
    implements jiBehaviorWrapper {
    private jiBehaviorRevoker myRevoker;
    // if myClonable is true, then this is clonable
    /*package*/ boolean myClonable;
    
    local jBehaviorWrapper () {
      myClonable = true;
      // myRevoker remains null
    }
    local jBehaviorWrapper (jiBehaviorRevoker revoker) {
      myRevoker = revoker;
      myClonable = false;
    }
    local final boolean isRevoked () {
      if (myRevoker != null && myRevoker.isRevoked())
        return true;
      else
        return false;
    }

    local jiBehaviorWrapper cloneUnclonableWrapper(jiBehaviorRevoker x) {
      throw new RuntimeException("BUG in javavm--this method shouldn't be called; it's supposed to be abstract");
    }
  }

/* 
        ECapabilities.e
        Jay Fenton 
        Proprietary and Confidential
        Copyright 1997 Electric Communities.  All rights reserved worldwide.
*/

package ec.tests.nu;
import ec.e.file.EStdio;
import java.lang.String;

/**
* Capabilities are made into E Objects so when they cross vat boundaries, only
* a proxy moves, not the contents. This prevents someone with a hacked client
* from manufacturing bogus capabilities.
*/

/**
* Interface for verifying that a capability is valid.
*/
interface verifyInterface {
/**
* @param forWho reference to object to check.
* @param whatKind string describing kind of capability.
* @return the factory which manufactured this capability (if it was valid).
*/
    CapabilityFactory verify(Object forWho, Object whatKind);
}

/**
* Interface for revoking a capability.
*/
interface revokeInterface {
/**
* @return Tear off a reference to the revoker for this capability.
*/
    ECapability tearOffRevoker();
/**
* @param Cause this capability to be revoked by supplying a revoker for it.
*/
    void revoke(ECapability revoke) ;
}

/**
* A class for representing a basic capability
*/
eclass ECapability implements verifyInterface {
    Object who;             // who it is valid for.
    Object kind;            // what kind it is.
    CapabilityFactory maker; // the factory that made it.

    ECapability() {
    }

/**
* @param forWho who it is valid for.
* @param whatKind what kind it is
* @param madeBy the factory that made it
* @return a properly filled in capability object.
*/
    ECapability( Object forWho, Object whatKind, CapabilityFactory madeBy) {
        who = forWho;
        kind = whatKind;
        maker = madeBy;
    }

/**
* Check a capability for validity. If it is valid, return the factory reference,
* else return null.
* @param forWho who it should be valid for.
* @param whatKind what kind it is supposed to be.
* @return the factory that made it iff the rest is valid.
*/
    local CapabilityFactory verify(Object forWho, Object whatKind) {
//      EStdio.out().println("A: " + forWho + whatKind + madeBy);
//      EStdio.out().println("B: " + who + kind + maker);
        if(forWho != who)
            return(null);

        if(!((String) whatKind).equals(kind))
            return(null);

        return(maker);
    }

// So that it can be extracted from a channel easily.
    local Object value() {
        return(this);
    }
}

/**
* A class for representing a revokable capability
*/
eclass ERevokableCapability extends ECapability implements revokeInterface {
    ECapability revoker = null;

/**
* @param forWho who it is valid for.
* @param whatKind what kind it is
* @param madeBy the factory that made it
* @return a properly filled in revokable capability object.
*/
    ERevokableCapability( Object forWho, Object whatKind, CapabilityFactory madeBy) {
        who = forWho;
        kind = whatKind;
        maker = madeBy;
    }
/**
* return a basic capability object which can be used as an argument to
* cause this revokable capability to be revoked.
*/
    local ECapability tearOffRevoker() {
        if(revoker != null)
            return(null);
        revoker = new ECapability((Object) this, "revoke", maker);
        return(revoker);
    }

/**
* Cause this capability to be revoked by supplying a revoker for it.
* @param revoke the revoker to use.
*/
    local void revoke(ECapability revoke) {
        if(revoker == revoke) {
            who = null;
            kind = null;
            maker = null;
        }
    }
}

/**
* A class which issues capabilitys for a given object (or for any object).
*/
public class CapabilityFactory {
    Object who; // who to issue capabilities on behalf of (for calls that dont specify otherwise).

/**
* Instantiate a factory for the object given.
* @param forWho the object to be the default subject.
* @return a factory object.
*/
    public CapabilityFactory(Object forWho) {
        who = forWho;
    }

/**
* Generate a capability of the requested kind.
* @param whatKind kind of capability to issue.
* @return a filled-in capability object.
*/
    public ECapability issueCapability(Object whatKind) {
        return new ECapability(who, whatKind, this);
    }

/**
* Generate a revocable capability of the requested kind.
* @param whatKind kind of capability to issue.
* @return a filled-in capability object.
*/
    public ERevokableCapability issueRevokableCapability(Object whatKind) {
        return new ERevokableCapability(who, whatKind, this);
    }

/**
* Generate a revocable capability of the requested kind.
* @param forWho the subject of the capability (which can be other than the default).
* @param whatKind kind of capability to issue.
* @return a filled-in capability object.
*/
    public ERevokableCapability issueRevokableCapabilityForOther(Object forWho, Object whatKind) {
        return new ERevokableCapability(forWho, whatKind, this);
    }
}
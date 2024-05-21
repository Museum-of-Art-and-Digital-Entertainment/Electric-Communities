package ec.plgen;

public interface Presence_$_Intf {
}

public class BaseUnum_$_Impl {
	protected void protectedReplacePresence (Presence_$_Intf newPresence) {
	}
	protected Presence_$_Intf protectedGetPresence () {
		return null;
	}
	
	void replacePresence (Presence_$_Intf newPresence, Object secretKey) {
	}
	Presence_$_Intf getPresence (Object secretKey) {
		return protectedGetPresence();
	}
}

public class PresenceEnvironment {
}

public class BasePresence_$_Impl {
	final PresenceEnvironment getEnvironment(Object secretKey) {
		return null;
	}

    final void registerInterestInProxy (Object presence, Object secretKey) {
	}

    synchronized final void notifyOtherPresenceAcceptHost (boolean accepted, Object secretKey) {
    }
}

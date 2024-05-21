package ec.pl.examples.dieroll;
import ec.e.comm.*;
import ec.pl.runtime.*;

public einterface DieRollPeer {
	dieroll();
}

public interface DieRollFactory extends AgencyFramework
{
	DieRollController getDieRollController();
}

public interface DieRollController {
	static final int EVENT_DIEROLL_VALUE =		1000;
	static final int EVENT_DIEROLL_REFRESH =	1001;
	static final int EVENT_DIEROLL_SETPEER =	1002;
	
	void postEvent (int eventType, int value, Object data);
	void setPeer (DieRollPeer peer);
}


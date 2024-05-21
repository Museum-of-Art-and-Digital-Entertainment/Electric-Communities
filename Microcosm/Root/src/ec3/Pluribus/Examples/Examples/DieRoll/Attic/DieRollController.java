package ec.plexamples.dieroll;

public interface DieRollController {
	static final int EVENT_DIEROLL_VALUE =		1000;
	static final int EVENT_DIEROLL_REFRESH =	1001;
	
	void postEvent (int eventType, int value);
	void setPeer (DieRollPeer peer);
}


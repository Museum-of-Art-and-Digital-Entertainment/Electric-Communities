package ec.pl.examples.web;
import ec.pl.runtime.*;


public interface WebFactory extends AgencyFramework
{
	WebController getWebController();
}

public interface WebController {
    static final int EVENT_STATUS = 1000;
    static final int EVENT_LINK = 1001;
    static final int EVENT_SELECTION = 1002;
	static final int EVENT_PEER = 1003;
	
	void postEvent (int eventType, boolean state);
	void postStatus (String status);
	void postLink (String link);
	void postSelection (int start, int end);
	void setPeer (WebPeer peer, String link, int start, int end);

}






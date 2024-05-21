package ec.pl.examples.lamp;
import ec.e.io.*;
import ec.e.comm.*;

public interface LampController {
	static final int EVENT_LAMP_STATE = 1000;
	static final int EVENT_HOST_STATE = 1001;
    static final int EVENT_STATUS = 1002;

	void postEvent (int eventType, boolean state);
	void postStatus (String status);
	void setPeer (LampPeer peer, boolean hostState, boolean lampState);
}

public eclass ConsoleLampController implements LampController, EInputHandler
{
    LampPeer peer;
    String hostString;
    boolean hostState;

	// The local user typed a newline character - so toggle the lamp.
	emethod handleInput (String line) {
		if (line != null && line.equals("transfer")) {
		    if (peer != null) peer <- lampTransfer();
		}
		if (line != null && line.equals("archive")) {
		    if (peer != null) peer <- lampArchive();
		}
		else {
		    if (peer != null) peer <- lampToggle();
	    }
	}

	local void postEvent (int eventType, boolean state) {
		switch (eventType) {
			case LampController.EVENT_LAMP_STATE:
                System.out.println(hostString + ": Lamp is " + (state ? "on" : "off"));
				break;
			case LampController.EVENT_HOST_STATE:
			    if (state != hostState) {
                    System.out.println("Became " + (state ? "host" : "client"));
                }
                else {
                    System.out.println("Host transfer occured, still client");
                }
                hostString = (state ? "Host" : "Client");
				break;
			default:
				System.out.println("Warning, unknown type in postEvent: " + eventType);
		}
	}

	local void postStatus (String status) {
        System.out.println(status);
	}

	local void setPeer (LampPeer peer, boolean hostState, boolean lampState) {
        RtConsole.setupConsoleReader(this, System.in);
		this.peer = peer;
        hostString = (hostState ? "Host" : "Client");
        this.hostState = hostState;
	    System.out.println("A " + hostString + " lamp now exists");
		if (hostState) postEvent(LampController.EVENT_LAMP_STATE, lampState);
	}
}

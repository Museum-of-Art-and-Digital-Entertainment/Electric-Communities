package ec.pl.examples.dieroll;

import ec.e.io.RtConsole;
import ec.e.io.EInputHandler;
import ec.e.cap.EEnvironment;

public class ConsoleDieRollFactory implements DieRollFactory
{
	public DieRollController getDieRollController() {
		return new ConsoleDieRollController();
	}

	public void setEnvironment (EEnvironment env) {
	}
	
	public void run() {
	}
}

public eclass ConsoleDieRollController implements DieRollController, EInputHandler
{
    DieRollPeer peer;
    int value;

	// The local user typed a newline character - so toggle the lamp.
	emethod handleInput (String line) {
		if (line != null && line.equals("quit")) {
		    System.exit(0);
		}
		else {
		    if (peer != null) peer <- dieroll();
	    }
	}

	local void postEvent (int eventType, int value, Object data) {
		switch (eventType) {
			case DieRollController.EVENT_DIEROLL_VALUE:
                System.out.println("Value is " + value);
				break;
			default:
				System.out.println("Warning, unknown type in postEvent: " + eventType);
		}
	}

	local void setPeer (DieRollPeer peer) {
        RtConsole.setupConsoleReader(this, System.in);
		this.peer = peer;
	}

    //
    // Starting it all up
    //

	public ConsoleDieRollController () {
	}
}

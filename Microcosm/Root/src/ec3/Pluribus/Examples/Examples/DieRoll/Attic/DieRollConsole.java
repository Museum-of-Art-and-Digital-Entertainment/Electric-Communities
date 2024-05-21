package ec.plexamples.dieroll;
import ec.e.io.*;

public class ConsoleDieRoll {
    public static void main (String args[]) {
        ConsoleDieRollController.main(args);
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

	local void postEvent (int eventType, int value) {
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
    static public void main (String args[]) {
        ConsoleDieRollController controller = new ConsoleDieRollController(args);
    }

	private ConsoleDieRollController (String args[]) {
		DieRoll.mainFromUI(args, this);
	}
}
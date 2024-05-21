// Lamp.java
// Copyright 1996 Netscape Communications Corp.  All rights reserved.

package ec.plexamples.dieroll;

import netscape_beta.application.*;
import netscape_beta.util.*;

public class IFCDieRoll {
    public static void main (String args[]) {
        IFCDieRollController.main(args);
    }
}

public class IFCDieRollController extends DieRollApplication implements Target, EventProcessor, DieRollController {
    Button rollButton;
	BackgroundView bv;
	DieRollPeer peer = null;
	String args[];
	ExternalWindow win;
	private static Bitmap dieBits[] = new Bitmap[6];

    //
    // DieRollController methods
    //
	public void postEvent (int eventType, int value) {
		DieRollEvent dieRollEvent = new DieRollEvent(eventType, value);
		setupEvent(dieRollEvent);
	}

	public void setPeer (DieRollPeer peer) {
		this.peer = peer;
		rollButton = new Button(0, 0, 480, 360);

		rollButton.setBezeled(false);
		rollButton.setTarget(this);
		rollButton.setCommand("roll");
		bv.addSubview(rollButton);

		win.becomeVisible();
	}

    //
    // EventProcessor methods
    //
	public void processEvent (Event event) {
		DieRollEvent dieRollEvent = (DieRollEvent)event;
		switch (dieRollEvent.getType()) {
			case DieRollController.EVENT_DIEROLL_VALUE:
				setDieValue(dieRollEvent.getValue()-1);
				break;
			default:
				System.out.println("Warning, unknown state in DieRollEvent: " + dieRollEvent.getType());
		}
	}

    //
    // Private state update methods
    // (Driven by events posted by Peer
    //
	private void setDieValue (int value) {
		//bv.setImage(dieBits[value]);
		rollButton.setImage(dieBits[value]);
		bv.draw();
	}

    //
    // Private utility methods
    //
    private void setupEvent(DieRollEvent dieRollEvent) {
		dieRollEvent.setProcessor(this);
		getEventLoop().addEvent(dieRollEvent);
    }

    //
    // Application methods
    //
    public void init() {

		Size size;
        super.init();

        win = new ExternalWindow();
        win.init();
        size = win.sizeForContentSize(480, 360);
		win.setBounds(100, 100, size.width, size.height);
        bv = win.backgroundView();
		bv.setUseDrawingBuffer(true);
        win.setTitle("Electric Communities Cosmic Die Roll");

		DieRoll.mainFromUI(args, this);
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
		if (command.equals("roll")) {
		    if (peer != null) peer <- dieroll();

		}
		else if (command.equals("quitApplication")) {
		    System.exit(0);
		}
    }

    //
    // Starting it all up
    //
    static public void main (String args[]) {
        String suffix;
        /*
		String osname = System.getProperty("os.name");
		if (osname.equals("SolarisXXXX")) suffix = "gif";
		else suffix = "jpg";
        */

		if ((args.length > 0) && args[0].equals("-bw")) suffix = "gif";
		else suffix = "jpg";

        IFCDieRollController controller = new IFCDieRollController(args);
		dieBits[0] = Bitmap.bitmapNamed("Dice1." + suffix);
		dieBits[1] = Bitmap.bitmapNamed("Dice2." + suffix);
		dieBits[2] = Bitmap.bitmapNamed("Dice3." + suffix);
		dieBits[3] = Bitmap.bitmapNamed("Dice4." + suffix);
		dieBits[4] = Bitmap.bitmapNamed("Dice5." + suffix);
		dieBits[5] = Bitmap.bitmapNamed("Dice6." + suffix);
        controller.run();
    }

	private IFCDieRollController (String args[]) {
		this.args = args;
	}
}

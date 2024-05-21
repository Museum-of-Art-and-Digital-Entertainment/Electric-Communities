// The DieRoll

package ec.pl.examples.dieroll;

import netscape.application.*;
import netscape.util.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import ec.e.cap.EEnvironment;
import ec.ifc.app.*;

public class IFCDieRollFactory extends ECApplication implements DieRollFactory
{
	private static EEnvironment env = null;
	private static ECApplication application;
	
	public IFCDieRollFactory () {
		if (application != null) return; // XXX Squawk!
		application = this;
	}
	
	// This is required since the Factory is made via Class.newInstance();
	public void setEnvironment (EEnvironment theEnv) {
		if (env != null) return; // Squawk!
		env = theEnv;
	}	

	public DieRollController getDieRollController() {
		return new IFCDieRollController(env, application);
	}
}

public class IFCDieRollController implements Target, EventProcessor, DieRollController {
    Button rollButton;
	ECRootView rootView;
	private boolean showingSequence = false;
	private DieRollPeer peer = null;
	private int value;
	private ECExternalWindow dieWindow;
	private static Bitmap dieBits[] = new Bitmap[6];
	private static ImageSequence dieSequences[] = new ImageSequence[6];
	private static ECExternalWindow quitDialog;
	private static boolean exitFlag;
	private ECApplication application;
	private String suffix;

    //
    // DieRollController methods
    //
	public void postEvent (int eventType, int value, Object data) {
		DieRollEvent dieRollEvent = new DieRollEvent(eventType, value, data);
		setupEvent(dieRollEvent);
	}

	public void setPeer (DieRollPeer peer) {
		postEvent(DieRollController.EVENT_DIEROLL_SETPEER, 0, peer);
	}
	
	private void setupPeer (DieRollPeer peer) {
		Size size;

		this.peer = peer;
	
        dieWindow = new ECExternalWindow();
        dieWindow.setTitle("Electric Communities Cosmic Die Roll");
	
		dieWindow.setBounds(300, 100, 480, 362);	

		Menu menu = new Menu(true);
		MenuItem menuItem = menu.addItemWithSubmenu("File");
		ECApplication.getSubmenu(menuItem).addItem("Quit", "quitApplication", this);
		dieWindow.setMenu(menu);

        size = dieWindow.windowSizeForContentSize(480, 362);
		dieWindow.setBounds(300, 100, size.width, size.height);	

        rootView = (ECRootView) dieWindow.rootView();
		rootView.setBuffered(true);
		rootView.setColor(Color.lightGray);

		rollButton = new Button(0, 2, 480, 360);
		rollButton.setBordered(false);
		rollButton.setTarget(this);
		rollButton.setCommand("roll");
		rootView.setTipForView("Press to roll", rollButton);
		rootView.addSubview(rollButton);

		TextField text = new TextField(0, 0, 480, 2);
		text.setEditable(false);
		text.setBorder(new BezelBorder(BezelBorder.GROOVED, Color.lightGray));
		rootView.addSubview(text);
		rootView.setTipForView("This is just a line", text);
	
		dieWindow.show();
	
		quitDialog = new ECExternalWindow();
		quitDialog.setTitle("Quit DieRoll?");
		ECRootView rv = (ECRootView)quitDialog.rootView();
		rv.setBuffered(true);
		rv.setColor(Color.lightGray);

		text = new TextField(10, 10, 160, 20);
		text.setEditable(false);
		text.setBackgroundColor(Color.gray);
		text.setTransparent(true);
		text.setJustification(Graphics.CENTERED);
		text.setStringValue("Do you really want to quit?");
		rv.addSubview(text);

		text = new TextField(25, 35, 130, 2);
		text.setEditable(false);
		text.setBorder(new BezelBorder(BezelBorder.GROOVED, Color.lightGray));
		rv.addSubview(text);
	
		Button quitButton = new Button(20, 48, 60, 26);
		quitButton.setTarget(this);
		quitButton.setTitle("OK");
		quitButton.setCommand("reallyQuit");
		rv.setTipForView("*** CAREFUL! ***", quitButton);
		rv.addSubview(quitButton);
		
		Button cancelButton = new Button(100, 48, 60, 26);
		cancelButton.setTarget(this);
		cancelButton.setTitle("Cancel");
		cancelButton.setCommand("cancelQuit");
		rv.setTipForView("Ahh ... relief ...", cancelButton);
		rv.addSubview(cancelButton);

        size = quitDialog.windowSizeForContentSize(180, 80);
		quitDialog.setBounds(200, 200, size.width, size.height);

		new DieRollThread(this, suffix, DieRollThread.LOAD_SEQUENCES);
	}

    //
    // EventProcessor methods
    //
	public void processEvent (Event event) {
		DieRollEvent dieRollEvent = (DieRollEvent)event;
		switch (dieRollEvent.getType()) {
			case DieRollController.EVENT_DIEROLL_REFRESH:
				refreshDieValue(dieRollEvent.getValue());
				break;
			case DieRollController.EVENT_DIEROLL_VALUE:
				setDieValue(dieRollEvent.getValue()-1);
				break;
			case DieRollController.EVENT_DIEROLL_SETPEER:
				setupPeer((DieRollPeer)dieRollEvent.getData());
				break;
			default:
				System.out.println("Warning, unknown type in DieRollEvent: " + dieRollEvent.getType());
		}
	}

    //
    // Private state update methods
    // (Driven by events posted by Peer
    //
  
	private void refreshDieValue (int refreshValue) {
		if (refreshValue == value) setDieValue(refreshValue);
	}
	
	private void setDieValue (int newValue) {
		Image image;
		System.out.println("SetDieValue for value " + (newValue + 1));
		if (showingSequence) dieSequences[value].stop();
		if (dieSequences[newValue] != null) {
			System.out.println("Using sequence");
		    showingSequence = true;
		    image = dieSequences[newValue];
            dieSequences[newValue].start();
		}
		else {
			System.out.println("Using static bitmap");
		    showingSequence = false;
		    image = dieBits[newValue];
		}
		value = newValue;
		rollButton.setImage(image);
		rootView.draw();
		System.out.println("Returning from setDieValue");
	}

    //
    // Private utility methods
    //
    
    private void notifySequence (int index) {
		DieRollEvent dieRollEvent = new DieRollEvent(DieRollController.EVENT_DIEROLL_REFRESH, index, null);
    	setupEvent(dieRollEvent);
    }
    
    private void setupEvent(DieRollEvent dieRollEvent) {
		dieRollEvent.setProcessor(this);
		application.getEventLoop().addEvent(dieRollEvent);
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
    	int x;
    	int y;
    	
		if (command.equals("roll")) {
		    if (peer != null) peer <- dieroll();

		}
		else if (command.equals("quitApplication")) {			
			Event event = application.getLastEvent();
			if (event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent)event;
				x = mouseEvent.x - 50;
				y = mouseEvent.y - 60;
			}
			else {
			    Rect winBounds = ECApplication.getBoundsForExternalWindow(dieWindow);
			    Rect quitBounds = ECApplication.getBoundsForExternalWindow(quitDialog);
				x = winBounds.x + (winBounds.width / 2) - (quitBounds.width / 2);
				y = winBounds.y + (winBounds.height / 2) - (quitBounds.height / 2);
			}
			quitDialog.moveTo(x, y);
			quitDialog.showModally();			
			if (exitFlag == true) System.exit(0);
		}
		else if (command.equals("reallyQuit")) {
			exitFlag = true;
			quitDialog.hide();
		}
		else if (command.equals("cancelQuit")) {
			exitFlag = false;
			quitDialog.hide();
		}		
    }

    static void loadBits (String suffix) {
    	int i;
        for (i = 1; i <= 6; i++) {
        	Bitmap bitmap = dieBits[i-1];
			bitmap.loadData();
	    	System.out.println("Loaded (unlazy) bitmap for " + i);
	    }
    }
    
    static void loadSequences (IFCDieRollController con, String suffix) {
		int i, j;
        String archiveName;
        Object objects[];

        for (i = 0; i < 1; i++) {
        	ImageSequence sequence = new ImageSequence();
            sequence.setFrameRate(83);
            sequence.setResetOnStart(true);
            sequence.setPlaybackMode(DrawingSequence.FORWARD_LOOP);
        	for (j = 0; j < 60; j+=1) {
        	    String name = "anim" + (i + 1) + "/Dice" + (i + 1) + (j < 10 ? "0" : "") + j + "." + suffix;
        	    Bitmap bitmap = Bitmap.bitmapNamed(name, true);
        	    bitmap.loadData();
				////System.out.println("Bitmap for " + name + " is " + bitmap);
        	    sequence.addImage(bitmap);
            }
            System.out.println("Sequence is " + sequence);
            dieSequences[i] = sequence;
            System.out.println("Notifying controller for sequence " + i);
            con.notifySequence(i);
        }
    }

	IFCDieRollController (EEnvironment env, ECApplication application) {
		int i;
        String archiveName;
        Object objects[];
		String colorSpace = env.getProperty("colorSpace");
			
		if ((colorSpace != null) && colorSpace.equals("bw")) suffix = "gif";
		else suffix = "jpg";

		this.application = application;

        for (i = 1; i <= 6; i++) {
        	String filename = "Dice" + i + "." + suffix;
	        System.out.println("Reading image " + i);
	    	dieBits[i-1] = Bitmap.bitmapNamed(filename, false);
	    }
	}
}

class DieRollThread extends Thread {
	private String suffix;
	IFCDieRollController con;
	int tag;
	
	static final int LOAD_BITS = 1;
	static final int LOAD_SEQUENCES = 2;
	
	private DieRollThread() {
	}
	
	DieRollThread (IFCDieRollController con, String suffix, int tag) {
		this.suffix = suffix;
		this.con = con;
		this.tag = tag;
		this.setDaemon(true);
		this.start();
	}
	
	public void run () {
		switch (tag) {
		case LOAD_BITS:
			IFCDieRollController.loadBits(suffix);
			break;
		case LOAD_SEQUENCES:
			IFCDieRollController.loadSequences(con, suffix);
			break;
		default:
			System.out.println("DieRollThread unknown tag: " + tag);
		}
	}	
}




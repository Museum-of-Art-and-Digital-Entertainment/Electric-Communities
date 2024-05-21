
package ec.pl.examples.web;

import netscape.application.*;
import netscape.util.*;
import ec.pl.runtime.*;
import ec.e.net.*;

public class WebApplication extends Application {
	private Event lastEvent;
	
	public EventLoop getEventLoop() {
		return eventLoop();
	}
	
	// Random utility methods for things that won't compile under
	// the ecomp compiler!
	
	public static Menu getSubmenu(MenuItem menuItem) {
		return menuItem.submenu();
	}
	
	public static Rect getBoundsForExternalWindow(ExternalWindow window) {
		return window.bounds();
	}
	
	public static int getIndexForRange(Range range) {
		return range.index();
	}
	
	public static int getLengthForRange(Range range) {
		return range.length();
	}
	
    //
    // Application methods
    //
    public void init() {
        super.init();
    }
 
     public void run() {
    	super.run();
    }
   
    public void willProcessEvent (Event event) {
    	this.lastEvent = event;
    	super.willProcessEvent(event);
    }

	public Event getLastEvent() {
		return lastEvent;
	}    
}

// This is just a hack to get this file to compile
// in a chicken and egg situation (don't ask)
class IFCWebController {
	void gotMouseUp(TextView sender) {
	}
	
	void gotMouseLink(TextView sender, String url) {
	}
}

class MyTextView extends TextView {
	private IFCWebController controller;
	private boolean mouseDown = false;
	private String lastURL = null;
	private int myCursor = View.TEXT_CURSOR;
	
	MyTextView (int x, int y, int width, int height, IFCWebController controller) {
		super(x, y, width, height);
		this.controller = controller;
	}
	
	public void mouseUp(MouseEvent event) {
		super.mouseUp(event);
		mouseDown = false;
		////System.out.println("Got a mouse up");
		controller.gotMouseUp(this);
	}
	
	public int cursorForPoint (int x, int y) {
		return myCursor;
	}
	
	public boolean mouseDown(MouseEvent event) {
		boolean ret = super.mouseDown(event);
		mouseDown = true;
		return ret;
	}
	
	public void mouseMoved(MouseEvent event) {
		super.mouseMoved(event);
		if (mouseDown) return;
		String url = getUrlAtCoordinates(event.x, event.y);
		if (url != lastURL) {
			lastURL = url;
			if (url == null) {
				myCursor = View.TEXT_CURSOR;
			}
			else {
				myCursor = View.HAND_CURSOR;
			}
			controller.gotMouseLink(this, url);
		}
	}
	
	public String getUrlAtCoordinates (int x, int y) {
		String url = null;
		try {
			netscape.util.Hashtable attributes = attributesAtIndex(indexForPoint(x, y));
			if (attributes == null) url = null;
			else url = (String) attributes.get(TextView.LINK_KEY);
		} catch (Exception e) {
			////System.out.println("Exception occured in getUrlAtCoordinates, " + e);
			////e.printStackTrace();
		}
		return url;
	}
}

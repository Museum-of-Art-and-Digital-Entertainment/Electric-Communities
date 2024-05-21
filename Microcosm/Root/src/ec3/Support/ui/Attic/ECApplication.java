package ec.ui;

import netscape.application.*;
import netscape.util.*;

public class ECApplication extends Application {
	private Event lastEvent;
	
	// added 06 January, 1997 - ctg
	public ECApplication()  {
		super();
	}

	public EventLoop getEventLoop() {
		return eventLoop();
	}
	
	// Random utility methods for things that won't compile under
	// the ecomp compiler!
	
	public static ScrollBar getVertScrollBar (ScrollGroup scrollGroup) {
		return scrollGroup.vertScrollBar();
	}

	public static Menu getSubmenu(MenuItem menuItem) {
		return menuItem.submenu();
	}
	
	public static Rect getBoundsForExternalWindow(ExternalWindow window) {
		return window.bounds();
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
	
	// added 06 January, 1997 - ctg
	public RootView getMainRootView()  {
		return(super.mainRootView());
		
	}

	public String getListItemTitle(ListItem li)  {
		return(li.title());
		
	}

	public Object getListItemData(ListItem li)  {
		return(li.data());
		
	}
	 
}


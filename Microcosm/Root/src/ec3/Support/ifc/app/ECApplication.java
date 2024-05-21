package ec.ifc.app;

import java.util.Hashtable;

import netscape.application.Application;
import netscape.application.Event;
import netscape.application.EventProcessor;
import netscape.application.Popup;

import ec.util.assertion.Assertion;
import ec.util.Native;

import ec.e.run.Trace;

public class ECApplication extends Application {

    /** 
     * same string as netscape.application.Popup's command of the
     * same name, but that one was private
     */
    protected final static String CLOSE_POPUP_AND_CANCEL = "cancel";
    private Hashtable myValuables;
    
    public final static Object TheApplicationSynchronizer = new Object();

    private Event lastEvent;
    private Popup myCurrentPopup;
    private boolean myTipsEnabled = true;
    
    // timing (debugging) stuff

    private String myStartTimeString = null;
    private long myStartTime = 0;   
    
    // added 06 January, 1997 - ctg
    public ECApplication()  {
        super();
        myValuables = new Hashtable();
    }

    //
    // Application methods
    //
    
    /**
     * Removes current popup menu from screen, without sending its command.
     * Normally called only from ECRootView.
     */
    public void cancelCurrentPopup() {
        if (myCurrentPopup != null) {
            myCurrentPopup.performCommand(CLOSE_POPUP_AND_CANCEL, null);        
        }
    }
    
    /** returns current popup, which is used by ECRootView in event handling */
    public Popup currentPopup() {
        return myCurrentPopup;
    }
    
    public void init() {
        // turn off IFC1.1's weird keyboard navigation that features
        // a tiny green arrow following you around. The documentation
        // points out that you have to turn it off in your application's
        // init method or it will be too late.
        setKeyboardUIEnabled(false);
        super.init();
    }
    
    /** 
     * Returns the main window, can be removed, as there is a generic
     * case of this method: getValuableAttribute
     */
    public MainWindowKeeper mainWindow() {
        return (MainWindowKeeper)myValuables.get("ec.ifc.app.MainWindowKeeper");
    }
    
    /** returns main window */
    public Object getValuableAttribute(String attributeName) {
        return myValuables.get(attributeName);
    }
    
    /** Overridden to handle timing events */
    public void processEvent(Event event) {
        if (event instanceof TimingDelayEvent) {
            stopTimingNow(((TimingDelayEvent)event).string);
        } else {
            super.processEvent(event);
        }
    }
    
    /**
     * sets current popup, which is used in event handling
     * by ECRootView
     */
    public void setCurrentPopup(Popup popup) {
        myCurrentPopup = popup;
    }
 
    /** 
     * sets an attribute that it is supposed to be kept around, not
     * garbage collected, during the lifetime of the application
     */
    public void setValuableAttribute(String attributeName, Object attribute) {
        myValuables.put(attributeName, attribute);
    }
    
    /** 
     * sets MainWindow attribute, can be removed, as it is the 
     * version of setValuableAttribute
     */
    public void setMainWindow(MainWindowKeeper mainWindow) {
        setValuableAttribute("ec.ifc.app.MainWindowKeeper", mainWindow);
    }
    
    /**
     * starts a timed hunk, which will be logged with the specified string.
     * @see #stopTimingSoon
     * @see #stopTimingNow
     */
    public void startTiming(String startString) {
        if (Trace.ON && Trace.gui.timing) {
            if (myStartTimeString != null) {
                Trace.gui.timingm("startTiming is clobbering already-started timing '"
                + myStartTimeString + "'");
            }
            
            myStartTimeString = startString;
            myStartTime = Native.queryTimer();
        }        
    }    
    
    /**
     * ends a timed hunk immediately. The string will be displayed as the
     * 'to' clause of 'from this to that took x microseconds' unless it is
     * null. If the string is null, the timing message will just be the
     * string supplied by startTiming.
     */
    public void stopTimingNow(String endString) {
        if (Trace.ON && Trace.gui.timing) {
            // end time only is treated as a simple time stamp
            if (myStartTimeString == null) {
                // in the degenerate case where both strings are null, we'll
                // assume this is a mismatched begin/end pair (which can happen
                // when there is more than one way to end a given startTiming)
                // and ignore it.
                if (endString != null) {
                    timeStamp(endString);
                }
                return;
            }
            
            long endTime = Native.queryTimer();

            String description;     
            if (endString == null) {
                description = myStartTimeString;          
            } else {
                description = "from " + myStartTimeString + " to " + endString; 
            }
            // truncate off some of the overly-precise digits
            Trace.gui.timingm(description + " took " + (float)((endTime/1000 - myStartTime/1000)/1000.0) 
                + " seconds");
            
            myStartTimeString = null;
        }        
    }
    
    /**
     * calls stopTimingNow() after one more pass through the event loop. Usually
     * called instead of stopTimingNow when you want to stop the timing after
     * the next redraw. The string will be displayed as the 'to' clause of 
     * 'from this to that took x microseconds' unless it is null. If the string
     * is null, the timing message will just be the string supplied by
     * startTiming.
     * @see #stopTimingNow
     */
    public void stopTimingSoon(String endString) {
        if (Trace.ON && Trace.gui.timing) {
            eventLoop().addEvent(new TimingDelayEvent(this, endString));
        }
    }
    
    /**
     * Spits out a message with the current time and the given string.
     * StartTiming/stopTiming or timeUntilNextEventLoop are preferred
     * since they output an interval, so use this only if you can't
     * use those.
     */
    public void timeStamp(String timingString) {
        if (Trace.ON && Trace.gui.timing) {
            long now = Native.queryTimer();
            Trace.gui.timingm(timingString + " at " + now + " microseconds (" + (long)(now/1000.0) + " milliseconds)");
        }
    }
 
    /**
     * Times from now until a new event is processed. Useful
     * for timing from now until after the next redraw. Equivalent
     * to calling startTiming(timingString) then stopTimingSoon(null).
     * @see #startTiming
     * @see #stopTimingSoon
     */
    public void timeUntilNextEventLoop(String timingString) {
        startTiming(timingString);
        stopTimingSoon(null);
    }
    
    /** overridden to store the last event */
    public void willProcessEvent (Event event) {
        this.lastEvent = event;
        super.willProcessEvent(event);
    }

    /** returns the last event */   
    public Event getLastEvent() {
        return lastEvent;
    }
    
    // tip support
    
    /** Returns whether or not "tool tips" should be displayed */
    public void setTipsEnabled(boolean newValue) {
        myTipsEnabled = newValue;
    }

    /** Sets whether or not "tool tips" should be displayed */
    public boolean tipsEnabled() {
        return myTipsEnabled;
    }
}


/** Simple little Event subclass used to carry a string across the event loop */
class TimingDelayEvent extends Event {
    public String string;
    
    TimingDelayEvent(EventProcessor processor, String string) {
        setProcessor(processor);
        this.string = string;
    }
}

/*
 *  Trace and Logging Package.  Written by Brian Marick,
 *  July-September 1997, for Electric Communities, Inc.
 *  Copyright (c) 1997 by Electric Communities.  All Rights Reserved.
 */

package ec.e.run;
import ec.util.assertion.*;
import ec.ifc.app.*;
import netscape.application.*;
import netscape.util.*;

/**
 * Class that controls dumping of messages to the on-screen display.
 * 
 * XXX This class cannot be used, given the current layering hierarchy
 * and build environment.  It should be replaced with something
 * decoupled from the trace code that can be built later.
 */

class TraceDisplay extends View 
                        implements TraceMessageAcceptor, Target, EventProcessor {

    
    /**
     * Set to true when the user registers
     * a desire to see a display.  If setup is
     * not complete, the desire will have to be
     * deferred.  Otherwise, the display is 
     * created immediately.
     */
    private boolean displayDesired = false;

    /**
     * Set to true when setup is complete.
     * The state of setup determines whether
     * a display is actually created.
     */
    private boolean isSetupComplete = false;

    /** 
     * Set to true when a display has been 
     * successfully created.
     */
    private boolean displayEnabled = false;


    /** 
     * This is how the display remembers the messages available
     * at the time it was first invoked.  Messages are never thrown
     * away. 
     * <p>
     * Note:  they're not even thrown away if the message display is 
     * closed, not until another one is opened.  To fix that, perhaps 
     * this class should be a WindowOwner, so that it gets window 
     * destruction events.
     */
    private TraceMessage myFirstMessage;

    /**
     * Determines how trace messages are to be displayed.
     */
    private TraceMessageStringifier myStringifier =
                                            new TraceMessageStringifier();


    /**
     * This external window contains the actual display of the trace.
     */
    ExternalWindow myDisplayWindow;

    
    /** 
     * The application that controls the event loop.
     */
    Application cApplication = null;


    /**
     * This view, within displayWindow, displays the text that has been traced.
     */
    TextView myTextView;

    /**
     * This scroll group contains the text view.
     */
    ScrollGroup myTextScroller;


    TraceDisplay() {
        // DANGER:  This constructor must be called as part of static
        // initialization of TraceController.  Until that
        // initialization is done, Trace should not be loaded.
        // Therefore, nothing in this constructor should directly or
        // indirectly use a tracing function.
        myStringifier.showDate(false);
        myStringifier.showTime(false);
    }

    /**
     * Called when all properties that could initially control the display
     * have been processed AND when the windowing system is available.  If 
     * the properties indicated a desire for the display, it is created.
     * <p>
     * Note that this object needn't accept messages before setup is complete.
     * It can get old messages from the TraceBuffer. 
     */
    public synchronized void setupIsComplete() {
        Trace.trace.debugm("Setup is complete. displayEnabled = " + displayEnabled);
        isSetupComplete = true;
        if (displayDesired) {
            // We were turned on during setup.  Now that the system
            // is ready, begin the display.
            initialDisplayInUnknownThread();
        }
    }

    /**
     * Display the trace messages whose first message is given, provided
     * the UI is available.  If not, display them after it becomes
     * available.
     */
    public synchronized void pleaseDisplay(TraceMessage firstMessage) {
        Trace.trace.debugm("Display is desired. displayEnabled = " + displayEnabled);
        myFirstMessage = firstMessage;
        displayDesired = true;
        if (isSetupComplete) {
            initialDisplayInUnknownThread();
        }
    }

    /**
     * Accept messages only when a display is open on the screen.
     */
    public boolean isAcceptingMessages() {
        return displayEnabled;
    }

    /** 
     * Accept a message.  Messages may come from outside, or they 
     * may be generated during a refresh of the window.
     * <p>
     * May be called in any thread.
     */
    // This needn't be synchronized, because it exists to 
    // dispatch to a new thread.
    public void accept(TraceMessage message) {
        
        TraceEvent event = new TraceEvent(TraceEvent.MESSAGE, message);
        event.setProcessor(this);
        cApplication.eventLoop().addEvent(event);
    }

    private void acceptInIFCThread(TraceMessage message) {
        // System.out.println(myStringifier.toString(message));
        myTextView.appendString(myStringifier.toString(message));
        myTextView.appendString("\n");
        ScrollBar scrollbar = myTextScroller.vertScrollBar();
        scrollbar.setScrollPercent((float)1.0);
        scrollbar.scrollToCurrentPosition();
        // System.out.println("Trace message posted in thread " + 
        //                  Thread.currentThread());
    }


    /**
     * Create the initial display.  There's some magic juju here.
     * The display must be created in the IFC thread, so we 
     * have to send an event to the event loop.  That event gets
     * delivered in the IFC thread.
     */
    protected void initialDisplayInUnknownThread() {
        Trace.trace.eventm("Creating initial trace display.");
        if (cApplication == null) {
            cApplication = Application.application();
        }
        if (cApplication == null) {
            Trace.trace.errorm("Could not open display - not running under IFC.");
            return;
        }                                   
        TraceEvent event = new TraceEvent(TraceEvent.BEGIN);
        event.setProcessor(this);
        cApplication.eventLoop().addEvent(event);
    }


    /**
     * Run in the IFC thread, this processes an event.  The only event
     * that matters in the current implementation is the initial event,
     * which just switches processing to this thread.
     */
    public void processEvent(Event event) {
        if (! (event instanceof TraceEvent)) {
            Trace.trace.warningm("Trace display got unexpected event " + event);
        }

        TraceEvent traceEvent = (TraceEvent) event;

        switch(traceEvent.getType()) {
            case TraceEvent.BEGIN:
                initialDisplayInIFCThread();
                break;
            case TraceEvent.MESSAGE:
                acceptInIFCThread(traceEvent.getMessage());
                break;
            default:
                Assertion.fail();
                break;
        }
    }

Should this code ever be resurrected and included in the product, you
must be aware of the following issue.  This comment is outside of
comment markers so that you are forced to notice it.

    /*
From: John Sullivan <sullivan@communities.com>
Subject: windows that might not be disposed
Cc: short-term tech info <ec_developer@communities.com>

I'm searching the source code for places where IFC ExternalWindows
(and
subclasses ECExternalWindow and SLWindow) are being created, to see
whether
or not they are being eventually dispose()d. If they aren't, then they
sit
around in memory until the application quits, even if none of our code
refers to them. (Until dispose() is called, there are still
gc-preventing-links from IFC's Application object to each external
window.)

Not every window needs to be disposed. If it's a window that the user
might
use again and again, then it's a tradeoff between the regained memory
from
gc'ing the window and the time it takes to reconstruct the window when
the
user wants to use it again. For instance (obviously), the main window
is
never disposed. For a less obvious example, the teleport list window
is
never disposed, since reopening it is likely and it doesn't take tons
of RAM.

Here's some windows I've found that don't seem to be disposed. I
marked
each with the name of the person I believe responsible for fixing the
problem. If I guessed wrong, please let me know. I would like to hear
from
everyone whose name appears here, so I know that they intend to deal
with
it. I'll continue harrassing people individually if I don't hear from
them.

EZ Listener main window [Jay]
-----------------------------
in ec3/Support/ui/inspector/EZListener.java.
EZListenerWindow.createWindow() makes an SLWindow that doesn't appear
to
ever be disposed. Perhaps there's just one of these, and it's OK to
not
dispose it?

EZ ListenerFrame window [Jay]
-----------------------------
in ec3/prevat/ec/ez/ui/ListenerFrame.java. ListenerFrame.init()
creates an
ExternalWindow, but there's no dispose() call in this file. I'm
guessing
that there might be lots of these, so they should be dispose()d when
hidden?

ancient Builder tools [John]
----------------------------
AnimationController, MasterController, PositionController,
CostumeController. These are ancient standalone tools that I need to
nuke
from our sources 'cuz they're just taking up space.

GUIContainerWindow [Claire]
---------------------------
in cosm1/ui/gui/uipresenter/GUIContainerWindow.java.
GUIContainerWindow.initWindow() makes an ECExternalWindow that doesn't
seem
to ever be disposed. Are we even using this class?

TraceDisplay window [Brian]
---------------------------
in ec3/E/runtime/e/trace/TraceDisplay.java.
TraceDisplay.initialDisplayInIFCThread() makes an ECExternalWindow
that
doesn't seem to ever be disposed. This class has a comment claiming it
can't be used given the current layering hierarachy and build
environment.
Should it be fixed so that if and when it is ever used, the window is
gc'ed
after use? Or perhaps there's only ever one of these windows, and it's
lightweight enough that we don't care?

     */

    private void initialDisplayInIFCThread() {
        if (! displayEnabled) {
            Size size;

            if (Trace.trace.debug && Trace.ON) {
                Trace.trace.debugm("Making display.  Thread is " + 
                                    Thread.currentThread());
            }
            // note: size is kind of bogus.  Just needs to be >= the
            // containing scrollview.
            myTextView = new TextView(0, 0, 600, 100);
            myTextView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
            myTextView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
            myTextView.setSelectable(true);

            myTextScroller = new ScrollGroup(0, 0, 600, 100);
            myTextScroller.setContentView(myTextView);
            myTextScroller.setHasVertScrollBar(true);
            myTextScroller.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
            myTextScroller.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

            myDisplayWindow = new ECExternalWindow();
            myDisplayWindow.setTitle("Trace Buffer Display");
            myDisplayWindow.rootView().setBuffered(true);
            size = myDisplayWindow.windowSizeForContentSize(
                        myTextScroller.width(), 
                        myTextScroller.height());
            myDisplayWindow.sizeTo(size.width, size.height);
            myDisplayWindow.setResizable(true);
            myDisplayWindow.addSubview(this);
            // If there comes a time that this should be a WindowOwner, add this.
            // myDisplayWindow.setOwner(this);
            myDisplayWindow.show();

            myDisplayWindow.addSubview(myTextScroller);   

            // The inspector reposts the event to do its equivalent
            // of refresh.  Don't know that that's necessary here.
            // cApplication.eventLoop().addEvent(event);
            refreshInIFCThread();
            displayEnabled = true;  // Now accept more messages.
        } else {
            Trace.trace.warningm("Trace display is already started.");
        }
    }

    protected void refreshInIFCThread() {
        myTextView.setString("");
        for (TraceMessage message = myFirstMessage;
             message != null;
             message = message.next) {
            
            Assertion.test(message.message != null);
            // System.out.println(myStringifier.toString(message));
            // myTextView.appendString(myStringifier.toString(message));
            // myTextView.appendString("\n");
            acceptInIFCThread(message);
        }
    }          

    // All the rest of these methods are required to satisfy
    // the interfaces.  

    public void performCommand(String command, Object arg) {}


    // Some of these are required if this becomes a WindowOwner.
    // I don't know why you'd want to do that.

    // public void windowDidHide (Window window) { 
    // Do something like the following if this becomes a
    // WindowOwner.
    //      ourUI.removeInspectorView(this); 
    // }
    // public boolean windowWillHide(Window window) { return true; }
    // public void windowDidBecomeMain (Window window) {}
    // public void windowDidResignMain (Window window) {}
    // public void windowDidShow(Window window) {}
    // public boolean windowWillShow(Window window) {return true;}
    // public void windowWillSizeBy(Window window, Size size) {}

}


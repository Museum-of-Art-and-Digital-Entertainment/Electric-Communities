package ec.ifc.app;

import ec.trace.Trace;
import netscape.application.Application;
import netscape.application.Target;
import netscape.application.Window;


/**
 * A no-frills implementation of WindowController that controls
 * an ECExternalWindow. getWindow() and setWindow() are left
 * abstract for subclasses to implement.
 */
public abstract class ECWindowController implements WindowController, Target {

    //
    // commands
    //
    private final static String delayCommandHide = "hide later";
    private final static String delayCommandShow = "show later";

    private boolean neverShown = true;
    
    //
    // Constructors
    //
    
    /** Returns a new ECWindowController that controls <b>window</b>*/
    public ECWindowController(ECExternalWindow window) {
        setWindow(window);
    }
    
    /** Returns a new ECWindowController */
    public ECWindowController() {
    }
    
    //
    // public methods
    //
    
    /** Responsibility from WindowController; hides the controlled window. */
    public void hide() {
        performLater(delayCommandHide);
    }
    
    /**
     * used to queue IFC messages so they get processed correctly.
     */
    public void performCommand(String command, Object arg) {
        if (delayCommandHide.equals(command)) {
            hideNow();
        }
        else if (delayCommandShow.equals(command)) {
            showNow();
        }
        else {
            throw new Error("Unexpected command: " + command + " arg = " + arg);
        }
    }
    
    /**
     * Responsibility from WindowController; does nothing here.
     * Override to position the window based on screen size,
     * other window locations, etc.
     */
    public void reposition() {
    }
    
    /** 
     * Responsibility from WindowController. Returns true if and only if
     * the window has never been shown.
     */
    public boolean shouldRepositionBeforeShowing() {
        return neverShown;
    }

    /** 
     * Responsibility from WindowController. Queues a show() on the window
     * if it exists, otherwise does nothing.
     */
    public void show() {
        performLater(delayCommandShow);
    }
    
    //
    // private methods
    //

    /** hide the window now */  
    private void hideNow() {
        ECExternalWindow window = (ECExternalWindow)getWindow();
        if (window != null)
            window.hide();      
    }
    
    /** show the window now */
    private void showNow() {
        ECExternalWindow window = (ECExternalWindow)getWindow();
    
        if (window == null) {
            if (Trace.gui.warning && Trace.ON) {
                Trace.gui.warningm("no window to show");
            }
            return;
        }
            
        if (!window.isVisible() && shouldRepositionBeforeShowing()) {
            reposition();
        }
        // show() brings the window to the front and makes it the
        // keyboard focus, whether or not it's already on screen
        window.show();
        
        neverShown = false;
    }

    /** simple form of performLater with no argument */
    private void performLater(String command) {
        performLater(command, null);
    }
    
    /** bottleneck for setting up commands to perform later */
    private void performLater(String command, Object arg) {
        Application.application().performCommandLater(
            this, command, arg);
    }   

    // Methods below this point are from WindowController.  They
    // shouldn't be needed, because this is an abstract class, but the
    // java-1.1.3 runtime seems to get confused about this.  -emm

    /** Returns the controlled window */
    public Window getWindow() {
        throw new Error("getWindow abstract method not overridden");
    }
    
    /** Hides the controlled window */
    // defined above
    //public void hide();
    
    /** shows the controlled window if necessary and brings it to the front */
    // defined above
    //public void show();
    
    /**
     * Repositions the controlled window based on relevant aspects of current
     * context such as screen size, other window locations, etc.
     */
    // defined above
    //public void reposition();
    
    /** Sets which window is controlled. */
    public void setWindow(Window newWindow) {
        throw new Error("setWindow abstract method not overridden");
    }

    /** 
     * Returns whether the controlled window's position should be adjusted
     * before the first time it is displayed.
     */
    // defined above
    //public boolean shouldRepositionBeforeShowing();
}


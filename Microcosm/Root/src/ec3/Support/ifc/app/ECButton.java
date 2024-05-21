
package ec.ifc.app;

import netscape.application.Button;
import netscape.application.IFCButtonAccess;
import netscape.application.MouseEvent;
import netscape.application.Target;

import ec.e.run.Trace;

/**
 * This class overwrites continuous push button functionality of IFC's Button class
 * Reason - bugs in IFC
 * It doesn't rely on Button's timer, instead, it has it's own timer and delay
 * It maintains its own CONTINUOUS_TYPE type, preserving Button's public API
 * 
 * 06.20.97    dima    Added support for drag and drop, so from now on it's not pure
 *                     netscape.application.Button
 */
public class ECButton extends Button {

    //
    // private data
    //
    private String   myMouseUpCommand = null;

    //
    // public methods
    //

    /** overridden to optionally print usage tracing information */
    public boolean mouseDown(MouseEvent event) {
        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem(
                "mouse down on " + TraceUtils.traceDescription(this));
        }

        return super.mouseDown(event);
    }   

    /**
     * Overwrites Button.mouseUp
     */
    public void mouseUp(MouseEvent event) {
        Target target = target();
        if (myMouseUpCommand != null && target != null) {
            target.performCommand(myMouseUpCommand, this);
        }

        // force killing the timer, because IFC's Button doesn't do it for disabled buttons
        IFCButtonAccess.killTimer(this);
        
        super.mouseUp(event);

        if (Trace.gui.usage && Trace.ON) {
            Trace.gui.usagem(
                "mouse up on " + TraceUtils.traceDescription(this));
            if (type() == Button.TOGGLE_TYPE) {
                Trace.gui.usagem("checkbox is now " + (state() ? "on" : "off"));
            }
        }       
    }
    
    /** returns a description of this object used by Trace messages */
    private String descriptionForTrace() {
        String buttonTitle, windowTitle;
        String buttonDescription, windowDescription;
        
        buttonTitle = title();          
        if (buttonTitle != null && buttonTitle.length() > 0) {
            buttonDescription = "button '" + buttonTitle + "'";
        } else {
            buttonDescription = "unnamed button";
        }
        if (!isEnabled()) {
            buttonDescription = "disabled " + buttonDescription;
        }
        
        windowTitle = rootView().externalWindow().title();
        if (windowTitle != null && windowTitle.length() > 0) {
            windowDescription = "window '" + windowTitle + "'";
        } else {
            windowDescription = "unnamed window";
        }
        return buttonDescription + " in " + windowDescription;
    }   

    /**
     * Sets the string command that will be sent to Target during mouseUp event
     */
    public void setMouseUpCommand(String aCommand) {
        myMouseUpCommand = aCommand;
    }

}


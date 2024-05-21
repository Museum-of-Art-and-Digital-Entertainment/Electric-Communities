package ec.ui;

import ec.ifc.app.*;
import netscape.application.*;

/*

 * By Monica L. Anderson 980206
 * Copyright 1998 Electric Communities Inc.

 */

class InspectorButton extends Button {

    private int modifiers = 0;

    InspectorButton(int x, int y, int width, int height) {
        super(x,y,width,height);
    }

    /**

     * InspectorButton saves away the modifier bits for the event for
     * the last click on it and provides predicates to query the
     * modifiers from.

     */

    public boolean mouseDown(MouseEvent event) {
        if (event.type() == MouseEvent.MOUSE_DOWN)
            modifiers = event.modifiers();
        return super.mouseDown(event);
    }

    /**

     * We don't want to hold on to the original event.  We also don't
     * want to copy it unless we'll be asked for it.  Therefore we
     * provide this help method to synthesize an event from the
     * modifiers int when so requested and apply the predicates
     * provided for MouseEvent on our synthesized event. 

     */

    private MouseEvent ensureEvent() {
        return new MouseEvent(0,MouseEvent.MOUSE_DOWN,0,0,modifiers);
    }


    public boolean wasMetaClick() {
        return ensureEvent().isMetaKeyDown();
    }

    public boolean wasRightClick() { // Same as meta, supposedly but I have no meta key...
        return ((modifiers & 4) != 0);
    }

    public boolean wasControlClick() {
        return ensureEvent().isControlKeyDown();
    }

    public boolean wasMiddleClick() { // Same as control
        return ensureEvent().isControlKeyDown();
    }

    public boolean wasShiftClick() {
        return ensureEvent().isShiftKeyDown();
    }

    public boolean wasAltClick() {
        return ensureEvent().isAltKeyDown();
    }
}

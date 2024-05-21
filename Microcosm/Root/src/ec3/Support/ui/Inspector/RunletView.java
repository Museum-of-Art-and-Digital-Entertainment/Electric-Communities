package ec.ui;

import ec.ifc.app.*;
import netscape.application.*;
import ec.e.file.EStdio;
import ec.ui.QueueView;
import ec.e.inspect.Runlet;
import netscape.util.Hashtable;

/**

 * View class for displaying a RunQueue event wrapper, known as a
 * Runlet.  We display its description in a text field.

 */

public class RunletView extends TextView {
    private Runlet myRunlet;
    private RunletProcessor myProcessor = null;

    private static Hashtable boldFontAttributes;

    static {
        boldFontAttributes = new Hashtable(5);
        boldFontAttributes.put(TextView.FONT_KEY,new Font("Courier", Font.BOLD, 12));
    }

    RunletView(int x, int y, int width, int height, Runlet runlet, boolean selected, 
               RunletProcessor aProcessor) {
        super(x, y, width, height);
        myRunlet = runlet;
        myProcessor = aProcessor;
        setEditable(false);
        setSelectable(false);
        runlet.setUIRef(this);
        if (selected) select();
        else unSelect();
    }
    
    public void select() {
        setBackgroundColor(QueueView.displayColor(myRunlet).darkerColor());
        setString(myRunlet.runStateString() + myRunlet.sourceString() + " ");
        Range r = appendString(myRunlet.messageString());
        appendString(" " + myRunlet.targetString());
        setAttributesForRange(boldFontAttributes, r);
    }

    public void unSelect() {
        if (myRunlet != null) {
            setBackgroundColor(QueueView.displayColor(myRunlet));
            setString(myRunlet.runStateString() + myRunlet.sourceString() + " ");
            Range r = appendString(myRunlet.messageString());
            appendString(" " + myRunlet.targetString());
            setAttributesForRange(boldFontAttributes, r);
        }
   }

    public boolean mouseDown(MouseEvent event) {
        int clicks = event.clickCount();
         
        if (clicks > 1) {
            if (event.isControlKeyDown()) {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_CONTROL_DOUBLE_CLICK_EVENT);
            } 
            else if (event.isShiftKeyDown()) {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_SHIFT_DOUBLE_CLICK_EVENT);
            } 
            else {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_DOUBLE_CLICK_EVENT);
            }
        } 
        else {
            if (event.isControlKeyDown()) {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_CONTROL_SINGLE_CLICK_EVENT);
            } 
            else if (event.isShiftKeyDown()) {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_SHIFT_SINGLE_CLICK_EVENT);
            } 
            else {
                myProcessor.handleEventLater(myRunlet, InspectorEvent.RUNLET_SINGLE_CLICK_EVENT);
            }
        }
        return true;
    }
}

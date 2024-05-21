package ec.ui;
import netscape.application.*;
import java.util.*;
import ec.ifc.app.*;

/**

 * View class for displaying a list of buttons.  The name string is
 * all we care about for each button.  It also becomes the command
 * that gets sent to target.  <p>

 * There are two ways to add items to the view.  You can supply a
 * hashtable where the keys are strings to be used as button labels,
 * when creating the view.  After that, addButton(String) will add a
 * button and will immediately redraw the button view, if necessary
 * sorting alphabetically.

 */

class ScrollingButtonListView extends ScrollGroup {
    public static int TOP_MARGIN = 0; // Margin at top of area above first button
    public static int BOTTOM_MARGIN = 0; // Margin at bottom of area below last button
    public static int LEFT_MARGIN = 0; // Margin to the left of each button
    public static int RIGHT_MARGIN = 0; // Margin to the right of each button
    private static int recentButtonHeight = 16; // Gets updated whenever we create a ListButtonView
    // public static int SCROLL_BAR_WIDTH = 20; // Width of our scroll bar

    /**

     * Constructor.

     * @param gatheredObjects trusted notNull - A hashtable of
     * gathered objects. The keys will be used as labels for the
     * buttons in the scrolling button view.

     * @param target trusted notNull - The target that the command
     * gets sent to when a button is clicked. Typically the containing
     * view. The command that is sent is simply the label from the
     * button.

     * @param prefs trusted nullOk - The global inspector preferences
     * instance.

     * @param requestedWidth - The requested width for this view, or 0
     * if not known.

     */

    ScrollingButtonListView(Hashtable gatheredObjects, Target target,
                            InspectorUIPreferences prefs, int requestedWidth) {
        // Set up some of our parameters

        setHasVertScrollBar(true);
        setBorder(ECScrollBorder.border());

        if (requestedWidth < 120) 
            if (prefs != null) requestedWidth = prefs.defaultWindowWidth();
            else requestedWidth = 200;

        Enumeration e = gatheredObjects.keys();

        // Create a button list view. This is a fixed size view containing all buttons.
        // We are the scrolling view that shows part of this fixed size view
        // in our display area.

        ButtonListView buttonListView = new ButtonListView
          (e, target, true, prefs, requestedWidth);

        // Compute what size we ourselves need to be.
        // Max out at a certain max height, as given in prefs.

        Rect listViewBounds = new Rect(0,0,requestedWidth,
                                       gatheredObjects.size() *
                                       ButtonListView.DEFAULT_BUTTON_HEIGHT);

        if (prefs != null) {
            if (listViewBounds.height > prefs.defaultWindowMaxHeight())
                listViewBounds.height = prefs.defaultWindowMaxHeight(); // Max out at this height
        }

        sizeTo(listViewBounds.width, listViewBounds.height);
        setContentView(buttonListView); // This buttonlistview is now our displayed contents

        buttonListView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    }
}

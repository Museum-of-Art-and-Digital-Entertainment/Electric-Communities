package ec.ui;
import netscape.application.*;
import java.util.*;
import ec.ifc.app.*;

/**

 * View class for displaying a list of Runlets

 */

class ScrollingRunletListView extends ScrollGroup {
    public static int TOP_MARGIN = 0; // Margin at top of area above first runlet
    public static int BOTTOM_MARGIN = 0; // Margin at bottom of area below last runlet
    public static int LEFT_MARGIN = 0; // Margin to the left of each runlet
    public static int RIGHT_MARGIN = 0; // Margin to the right of each runlet
    // public static int SCROLL_BAR_WIDTH = 20; // Width of our scroll bar

    RunletListView myRunletListView = null;

    /**

     * Constructor.

     * @param target trusted notNull - The target that the command
     * gets sent to when a runlet is clicked. Typically the containing
     * view. The command that is sent is the Runlet itself.

     * @param requestedWidth - The requested width for this view, or 0
     * if not known.

     */

    ScrollingRunletListView(int top, int left, int width, int height, Target target) {
        super(top,left,width,height);

        // Set up some of our parameters

        setHasVertScrollBar(true);
        setBorder(ECScrollBorder.border());
    }
}

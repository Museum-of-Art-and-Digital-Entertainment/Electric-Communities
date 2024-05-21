package ec.ui;
import netscape.application.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ec.e.inspect.Runlet;

/**

 * View class for displaying a Vector of Runlets

 */

class RunletListView extends ListView {
    private Vector          myRunletViews = null;
    private Vector          myRunlets = null;
    private RunletProcessor myProcessor;

    //    private int runletWidth = 260;
    private int runletHeight = 16;
    private int runletMinWidth = 100;
    private int runletVerticalPitch = 17;

    private int TOP_MARGIN = 2;
    private int LEFT_MARGIN = 2;
    private int RIGHT_MARGIN = 2;
    private int BOTTOM_MARGIN = 2;

    /**

     * Constructor to create a fixed-sized view (but that we will
     * resize to be taller as necessary) that can accept a number of
     * RunletViews. The Runlets themselves are kept in a Vector named
     * myRunlets. You create the view first without any Runlets in it
     * and then later add them using addRunlet().

     * @param target trusted notNull - The target to send a command to
     * when the user clicks on a runlet in this view.

     */

    RunletListView(int left, int top, int width, int height, RunletProcessor processor) {
        super(left, top, width, height);
        myRunlets = new Vector(30);
        myRunletViews = new Vector(30);
        myProcessor = processor;      // Save this locally since it's common to all of our runlets.
        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);

        // Position ourselves in our containing view

        //        setLayoutManager(layoutManager);
        //        layoutManager.layoutView(this,0,0);
    }

    /**

     * Add a single runlet to this view.

     * @param title trusted notNull - The title of the runlet.

     */

    public void addRunlet(Runlet runlet, boolean selected) {
        synchronized(myRunlets) {
            if (myRunlets.indexOf(runlet) < 0) {
                myRunlets.addElement(runlet);

                int nrRunlets = myRunletViews.size();

                RunletView newRunletView = new RunletView
                    (LEFT_MARGIN, 
                     nrRunlets * runletVerticalPitch + TOP_MARGIN,
                     width() - (LEFT_MARGIN + RIGHT_MARGIN),
                     runletHeight, 
                     runlet, 
                     selected,
                     myProcessor);
                myRunletViews.addElement(newRunletView);
                sizeTo(width(), runletVerticalPitch * (nrRunlets + 1) + TOP_MARGIN + BOTTOM_MARGIN);
                newRunletView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
                addSubview(newRunletView);
            }
        }
    }

    public void removeAllRunlets() {
        int nrRunlets = myRunlets.size();

        for (int i = nrRunlets; --i >= 0; ) {
            RunletView aRunlet = (RunletView)myRunletViews.elementAt(i);
            removeSubview(aRunlet);
        }

        myRunlets.removeAllElements();
        myRunletViews.removeAllElements();
    }
    
    public void update(long[] times, String[] names) {
        if ((times == null) && (names == null)) {
            removeAllItems();
            return;
        }

        int size = times.length;

        for(int i = 0; i < size; i++) {
            if (times[i] > 0) {
                if (i < count()) {
                    ListItem myItem = itemAt(i);
                    if (!((String)myItem.data()).equals(names[i])) {
                        myItem.setTitle(times[i] + ": " + names[i]);
                        myItem.setData(names[i]);
                    }
                }
                else {
                    ListItem newItem = new ListItem();
                    newItem.setTitle(times[i] + ": " + names[i]);
                    newItem.setData(names[i]);
                    addItem(newItem);
                }
            }
        }
        sizeToMinSize();
    }
}

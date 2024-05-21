package ec.ui;

import ec.ifc.app.*;
import netscape.application.*;

/**

 * View class for displaying a gathered object.  For now we only
 * display its name in a button.  In the future we may decide to also
 * have a delete button for each line, as well as other information or
 * UI elements.  Therefore this view is not the button itself -
 * instead it *contains* the button.

 */

class ListButtonView extends View {

    public String name;

    /**

     * Compares ourselves to another
     * ListButtonView for the purpose of sorting (using QuickSort).

     * @param other suspect notNull - The other ListButtonView.

     */

    public int compareTo(ListButtonView other) { return name.compareTo(other.name); }

    /**
     * Constructor
     *
     * @param name notNull - A String, the name of the button.

     * @param target trusted notNull - The target that the command
     * gets sent to when a button is clicked. Typically the containing
     * view. The command that is sent is simply the label from the
     * button.

     * @param width - The requested width for this view.

     * @param height - The requested height for this view.

     */


    ListButtonView(String name, Target target, View superView, int width, int height) {
        super(0, 0, width,height);
        this.name = name;

        // Now create a button to be the single item (so far) in this view.

        Button button = Button.createPushButton(0, 0, width, height);
        button.setTarget(target);
        button.setCommand(name);
        button.setTitle(name);
        addSubview(button);     // Make button be subview of this ListButtonView.
        superView.addSubview(this); // Make ourselves a subview of scrolling view above.
        button.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    }

    static void swap(ListButtonView a[], int x, int y) {
        ListButtonView tmp = a[x];
        a[x] = a[y];
        a[y] = tmp;
    }

    static int divide(ListButtonView a[], int low, int high) {
        
        if (low >= high) return low;

        // po: int split = low or some other split selection algorithm
        // or do abs inline
        // int split = low + (Math.abs(rand.nextInt()) % (high - low));

        int split = (low + high ) / 2; // KJD flailing.
    
        // po: do the swap's in line.
        swap(a, low, split);
        split = low;
        low++;
        while (true) {
            while (low < high && a[split].compareTo(a[low]) >= 0 )
                low++;
            while (high > low && a[high].compareTo(a[split]) >= 0)
                high--;
            if (low < high)
                swap(a, low, high);
            else
                break;
        }
        if (a[split].compareTo(a[low]) < 0)
            low--;
        swap(a, low, split);
        return low;

    }

    public static void qsort(ListButtonView a[], int low, int high) {
        int split  = divide(a, low, high);

        if (split > low)
            qsort(a, low, split-1);
        if (split < high)
            qsort(a, split+1, high);
    }
}

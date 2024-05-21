package ec.ui;
import netscape.application.*;
import java.util.Enumeration;
import java.util.Vector;

/**

 * View class for displaying a list of buttons.
 * The name string is all we care about for each button.
 * It also becomes the command that gets sent to target. <p>

 * There are two ways to add items to the view.
 * You can supply an enumeration yielding strings when creating the view.
 * After that, addButton(String) will add a button and will immediately
 * redraw the button view, if necessary after re-sorting alphabetically.

 */

class ButtonListView extends View {
    public static int TOP_MARGIN = 0; // Margin at top of area above first button
    public static int BOTTOM_MARGIN = 0; // Margin at bottom of area below last button
    public static int LEFT_MARGIN = 0; // Margin to the left of each button
    public static int RIGHT_MARGIN = 0; // Margin to the right of each button
    public static final int DEFAULT_BUTTON_HEIGHT = 16;

    private Vector buttons = null;
    private Vector alphaButtons = null; // Same data as in buttons but sorted alphabetically
    private boolean isSortedAlphabetically = false;
    private Target target;
    private LayoutManager layoutManager = new ButtonListLayoutManager(this);
    private InspectorUIPreferences prefs = null;
    private int buttonWidth = 260;      // Overridden by preferences
    private int myButtonHeight = DEFAULT_BUTTON_HEIGHT;   // Overridden by preferences
    private int buttonMinWidth = 100; // Overridden by preferences
    private int buttonVerticalPitch = myButtonHeight; // Overridden by preferences

    /**

     * Constructor to create a fixed-sized view containing a number of
     * ListButtonViews.

     * @param e trusted notNull - An enumeration yielding a number of
     * keys (Strings) which will be used as labels on the buttons in
     * the ListButtonView's.

     * @param target trusted notNull - The target to send a command to
     * when the user clicks on a button in this view.

     * @param sortInitially boolean - If set, then we will sort the
     * buttons alphabetically.

     * @param preferences trusted nullOK - An instance that contains
     * inspector UI preference values such as button size and window
     * width.

     * @param requestedWidth - If given a value larger than zero, then
     * this will become the widht of each ListButtonView.

     */

    ButtonListView(Enumeration e, Target target, boolean sortInitially,
                   InspectorUIPreferences preferences, int requestedWidth) {
        super();
        prefs = preferences;
        buttons = new Vector(30);
        if (prefs != null) {
            buttonWidth = prefs.defaultButtonWidth();
            myButtonHeight = prefs.defaultButtonHeight();
            buttonMinWidth = prefs.defaultButtonMinWidth();
            buttonVerticalPitch = myButtonHeight; // add intra-button spacing someday??
        }

        if (requestedWidth != 0) {
            buttonWidth = requestedWidth;
            if (buttonWidth < buttonMinWidth) buttonWidth = buttonMinWidth;
        }
        
        if (e != null) 
            while (e.hasMoreElements()) {
                String title = (String)e.nextElement();
                ListButtonView newButton = new ListButtonView(title, target, this, buttonWidth, myButtonHeight);
                buttons.addElement(newButton);
                // System.out.println("Added button for " + title + " of width " +
                // newButton.width() + " and height " + newButton.height());
            }

        // Position ourselves in our containing view

        moveTo(LEFT_MARGIN, TOP_MARGIN);
        
        // Set our default size. This is the complete view that
        // contains all buttons.  In our case, we can only be resized
        // horizontally.  If we get resized vertically, then that
        // should be handled by our containing scrollView's problem.
        // Scrolling never affects our vertical size.  and if that
        // ever would change, i.e. by changing the number of buttons,
        // then we resize this view or just reconstruct it.

        sizeTo(buttonWidth + LEFT_MARGIN + RIGHT_MARGIN,
               buttonVerticalPitch * buttons.size() + TOP_MARGIN + BOTTOM_MARGIN);

        // Set our minimum size. We can never be resized to become smaller than this.

        if (prefs != null) {
            setMinSize(buttonMinWidth + LEFT_MARGIN + RIGHT_MARGIN,
                       buttonVerticalPitch * buttons.size() + TOP_MARGIN + BOTTOM_MARGIN);
        }

        if (sortInitially) sortAlphabetically();
        else sortChronologically();
        this.target = target;
        this.setLayoutManager(layoutManager);
        layoutManager.layoutView(this,0,0);
    }

    /**

     * Add a single button to this view.

     * @param title trusted notNull - The title of the button. Also
     * the command string to send in casse the user clicks on the
     * button.

     */

    public void addButton(String title) {
        synchronized(buttons) {
            ListButtonView newButton = new ListButtonView(title, target, this,
                                                          buttonWidth,
                                                          myButtonHeight);
            buttons.addElement(newButton); // Add to chronological vector

            if (isSortedAlphabetically) {
                Enumeration e = alphaButtons.elements();
                int i = 0;
                while (e.hasMoreElements()) {
                    if (newButton.compareTo((ListButtonView)e.nextElement()) > 0) {
                        alphaButtons.insertElementAt(newButton,i);
                        break;
                    }
                    i++;
                }

                /* Move new button and all buttons below it to new positions */

                int nrButtons = alphaButtons.size();
                for (; i < nrButtons; i++) { // i is already inserting point index
                    ((ListButtonView)buttons.elementAt(i)).moveTo(LEFT_MARGIN,
                                                                  i * buttonVerticalPitch + TOP_MARGIN);
                }
            }
        }

        // Well someone added buttons to our view. Fix up the size.
        // XXX tell containing views we changed size so they can adjust scrollbars etc!

        sizeTo(buttonWidth, buttonVerticalPitch * buttons.size()); // Become tall enough to show all buttons.
        setMinSize(buttonMinWidth, buttonVerticalPitch * buttons.size());
    }

    /**

     * Sort the buttons in this view alphabetically, and henceforth
     * maintain them sorted alphabetically.  Does not forget the
     * initial (chronological) order of the buttons.

     */

    public void sortAlphabetically() {
        synchronized(buttons) {
            int nrButtons = buttons.size();
            ListButtonView[] sortArray = new ListButtonView[nrButtons];
            buttons.copyInto((Object[])sortArray);
            ListButtonView.qsort(sortArray,0,nrButtons - 1);
            Vector ab = new Vector(nrButtons + 30); // Leave some space for expansion
            disableDrawing();
            for (int i=0; i < nrButtons; i++) { 
                ListButtonView button = sortArray[i];
                button.moveTo(LEFT_MARGIN,
                              i * buttonVerticalPitch + TOP_MARGIN);
                ab.addElement(button); // convert to Vector
            }
            alphaButtons = ab; // don't assign result to instance var until done
            isSortedAlphabetically = true;
            reenableDrawing();
        }
    }

    /**

     * Sort the buttons in this view chronologically, and henceforth
     * maintain them sorted this way.  Data structures kept for
     * alphabetical sorting are discarded since they can easily be
     * recreated.

     */

    public void sortChronologically() {
        alphaButtons = null; // if we don't keep it sorted, it's worthless
        synchronized(buttons) {
            int nrButtons = buttons.size();
            disableDrawing();
            for (int i=0; i < nrButtons; i++) { 
                ((ListButtonView)buttons.elementAt(i)).moveTo
                  (LEFT_MARGIN, i * buttonVerticalPitch + TOP_MARGIN);
            }
            isSortedAlphabetically = false;
            reenableDrawing();
        }
    }

    /**

     * Layout this view. This method gets called from the layout
     * manager method layoutView() which we define in a separate class
     * to avoid a name clash but it's really in this class to get the
     * scoping right, under a different name.

     */

    public void layoutView1(View aView, int deltaWidth, int deltaHeight) {

        Enumeration e;
        if (isSortedAlphabetically) e = alphaButtons.elements();
        else e = buttons.elements();

        Rect bounds = aView.bounds();
        bounds.width += deltaWidth;
        if (bounds.width < buttonMinWidth) bounds.width = buttonMinWidth;

        bounds.height = myButtonHeight;
        bounds.x = 0;
        bounds.y = 0;

        for (;e.hasMoreElements(); bounds.y += myButtonHeight)
            ((ListButtonView)(e.nextElement())).setBounds(bounds);
    }
}


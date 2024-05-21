package ec.ui;
import netscape.application.*;
import ec.ifc.app.*;

/**

 * Tool bar, used by GatheredListView above title bar

 */

class GatheredListViewToolBar extends View implements Target {

    public final static String REFRESH_COMMAND = "REFRESH GATHERED VIEW COMMAND";
    private static int REFRESH_BUTTON_WIDTH = 50;
    private static int REFRESH_BUTTON_HEIGHT = 16;

    /* Width and height of view */

    public static int WIDTH = 200;// Note - Toolbars are always a a fixed-width item.
    public static int HEIGHT = 50; // Fill these with dialog items from the left!

    private Button refreshButton;
    private GatheredListView gatheredListView;

    /**

     * Constructor
     * @param gatheredListView trusted notNull - our containing view.

     */

    GatheredListViewToolBar(GatheredListView gatheredListView) {
        super(0, 0, WIDTH, HEIGHT);
        this.gatheredListView = gatheredListView;

        // Refresh button is in the lower left corner on all control panels.
        // it is indented by (10, -5) from the bottom left corner.
        // but since it is 16 high, it ends up being at (10, -21) from the (0,height) corner.

        refreshButton = Button.createPushButton(10, HEIGHT - (REFRESH_BUTTON_HEIGHT + 5),
                                                REFRESH_BUTTON_WIDTH, REFRESH_BUTTON_HEIGHT);
        refreshButton.setTitle("Refresh");
        refreshButton.setTarget(gatheredListView); // This command goes directly to containing view
        refreshButton.setCommand(REFRESH_COMMAND);
        addSubview(refreshButton);
        
// I keep the follwing comments because I'm lazy. I know I'll
// want to add stuff to this class later.

//         fullNamesCheckBox = Button.createCheckButton(260, 10, 120, 14);
//         fullNamesCheckBox.setRaisedBorder(ECScrollBorder.border());
//         fullNamesCheckBox.setLoweredBorder(ECScrollBorder.border());
//         fullNamesCheckBox.setState(false);
//         fullNamesCheckBox.setTitle("Full type names");
//         fullNamesCheckBox.setTarget(this);
//         fullNamesCheckBox.setCommand(TOGGLE_FULL_NAMES);
//         addSubview(fullNamesCheckBox);   

//         showAddressesCheckBox = Button.createCheckButton(260, 28, 120, 14);
//         showAddressesCheckBox.setRaisedBorder(ECScrollBorder.border());
//         showAddressesCheckBox.setLoweredBorder(ECScrollBorder.border());
//         showAddressesCheckBox.setState(false);
//         showAddressesCheckBox.setTitle("Show addresses");
//         showAddressesCheckBox.setTarget(this);
//         showAddressesCheckBox.setCommand(TOGGLE_SHOW_ADDRESSES);
//         addSubview(showAddressesCheckBox);   
    }

    /**

     * Perform command; responsibility from Target interface, called when we
     * are target of a command from an IFC control etc. <p>

     * @Note The refresh button events are sent directly to out
     * containing view.

     */

    public void performCommand(String command, Object arg) {
//         if (command.equals(INSPECT_SEMANTIC_COMMAND)) {
//             gatheredListView.refreshGatheredListView(true);
//         } else if (command.equals(INSPECT_BITS_COMMAND)) {
//             gatheredListView.refreshGatheredListView(false);
//         } else if (command.equals(REFRESH_COMMAND)) {
//             gatheredListView.refreshGatheredListView();
//         } else if (command.equals(TOGGLE_FULL_NAMES)) {
//             gatheredListView.setUseFullNames(fullNamesCheckBox.state());
//         } else if (command.equals(TOGGLE_SHOW_ADDRESSES)) {
//             gatheredListView.setShowAddresses(showAddressesCheckBox.state());
//         } else {
//             throw new Error("Unexpected command: " + command + " arg = " + arg);
//         }
    }
}

package ec.ui;
import netscape.application.*;
import ec.ifc.app.*;
import ec.e.inspect.Inspector;

/**

 * Tool bar, used by GatheredListView above title bar

 */

class CategoryListViewToolBar extends View implements Target {

    public final static String REFRESH_COMMAND = "REFRESH CATEGORIES COMMAND";
    public final static String OPEN_EZ_LISTENER = "OPEN EZ LISTENER WINDOW COMMAND";
    public final static String INSPECT_AT_ADDRESS = "INSPECT BY ADDRESS COMMAND";
    public final static String DUMP_MEMORY_COMMAND = "DUMP MEMORY COMMAND";
    public final static String INSPECT_ERQ_COMMAND = "Inspect E Run Queue";
    private static int REFRESH_BUTTON_WIDTH = 50;
    private static int REFRESH_BUTTON_HEIGHT = 16;
    private static int INSPECTERQ_BUTTON_WIDTH = 66;
    private static int INSPECTERQ_BUTTON_HEIGHT = 16;
    private static int OPENLISTENER_BUTTON_WIDTH = 66;
    private static int OPENLISTENER_BUTTON_HEIGHT = 16;

    /* Width and height of view */
    public static int WIDTH = 200;// Note - Toolbars are always a a fixed-width item.
    public static int HEIGHT = 90; // Fill these with dialog items from the left!

    private Button refreshButton;
    private Button inspectERQButton;
    private Button openListenerButton;
    private Button inspectByAddressButton;
    private View ourSuperView;
    public InspectorTextField addressField;

    /**

     * Constructor

     */

    CategoryListViewToolBar(Target target, boolean useEZListener) {
        super(0, 0, WIDTH, HEIGHT);

        // Refresh button is in the lower left corner on all control panels.
        // it is indented by (10, -5) from the bottom left corner.
        // but since it is 16 high, it ends up being at (10, -21) from the (0,height) corner.

        refreshButton = Button.createPushButton(10, HEIGHT - (REFRESH_BUTTON_HEIGHT + 5),
                                                REFRESH_BUTTON_WIDTH, REFRESH_BUTTON_HEIGHT);
        refreshButton.setTitle("Refresh");
        refreshButton.setTarget(target); // This command goes directly to containing view
        refreshButton.setCommand(REFRESH_COMMAND);
        addSubview(refreshButton);

        inspectERQButton = Button.createPushButton(90, HEIGHT - (INSPECTERQ_BUTTON_HEIGHT + 5),
                                                INSPECTERQ_BUTTON_WIDTH, INSPECTERQ_BUTTON_HEIGHT);
        inspectERQButton.setTitle("Messages");
        inspectERQButton.setTarget(target); // This command goes directly to containing view
        inspectERQButton.setCommand(INSPECT_ERQ_COMMAND);
        addSubview(inspectERQButton);


        if (useEZListener) {
            openListenerButton = Button.createPushButton
              (90, 35, OPENLISTENER_BUTTON_WIDTH, OPENLISTENER_BUTTON_HEIGHT);
            openListenerButton.setTitle("EZ listener");
            openListenerButton.setTarget(target); // This command goes directly to containing view
            openListenerButton.setCommand(OPEN_EZ_LISTENER);
            addSubview(openListenerButton);
        }

        if (ec.e.inspect.Inspector.runningUnderDebuggingVM()) {
            TextField addressFieldLabel = new TextField();
            addressFieldLabel.setBounds(5, 5, 60, 16);
            addressFieldLabel.setStringValue("Inspect @");
            addressFieldLabel.setTransparent(true);
            addSubview(addressFieldLabel);

            addressField = new InspectorTextField();
            addressField.setEditable(true);
            addressField.setTarget(target);
            addressField.setCommand(INSPECT_AT_ADDRESS);
            addressField.setBounds(62, 5, 108, 20);
            addressField.setStringValue(Inspector.addressString(Inspector.addressOf(this),1));
            addSubview(addressField);

            Button dumpMemoryButton = Button.createPushButton
              (10, 30, OPENLISTENER_BUTTON_WIDTH, OPENLISTENER_BUTTON_HEIGHT);
            dumpMemoryButton.setTitle("MemDump");
            dumpMemoryButton.setTarget(target);
            dumpMemoryButton.setCommand(DUMP_MEMORY_COMMAND);
            addSubview(dumpMemoryButton);
        }
    }

    /**

     * Perform command; responsibility from Target interface, called
     * when we are target of a command from an IFC control etc.

     * @note All button events go directly to out superview.

     */
    public void performCommand(String command, Object arg) {
        //         if (command.equals(INSPECT_SEMANTIC_COMMAND)) {
        //             CategoryListView.refreshCategoryListView(true);
        //         } else {
        //             throw new Error("Unexpected command: " + command + " arg = " + arg);
        //         }
    }
}

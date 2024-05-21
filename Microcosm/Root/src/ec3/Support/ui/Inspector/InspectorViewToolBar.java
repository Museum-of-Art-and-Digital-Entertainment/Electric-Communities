package ec.ui;

import netscape.application.*;
import ec.ifc.app.*;
import ec.e.inspect.Inspector;
/**

 * Tool bar, used by InspectorView above title bar

 */

class InspectorViewToolBar extends View implements Target {

    private static final String BACK_COMMAND = "BACK COMMAND";
    private static final String RIGHT_BACK_COMMAND = "RIGHT CLICK BACK COMMAND";
    private static final String REFRESH_COMMAND = "REFRESH COMMAND";
    private static final String ACCEPT_MESSAGE_COMMAND = "ACCEPT MESSAGE COMMAND";
    private static final String TOGGLE_RUNQUEUE_BREAK = "TOGGLE RUNQUEUE BREAK";
    private static final String TOGGLE_FULL_NAMES = "TOGGLE FULL NAMES";
    private static final String TOGGLE_SHOW_ADDRESSES = "TOGGLE SHOW ADDRESSES";
    private static final String INSPECT_SOURCE_COMMAND = "INSPECT SOURCE COMMAND";
    private static final String INSPECT_MESSAGE_COMMAND = "INSPECT MESSAGE COMMAND";
    private static final String INSPECT_TARGET_COMMAND = "INSPECT TARGET COMMAND";
    private static final String SHOW_QUEUE_COMMAND = "SHOW QUEUE COMMAND";
    private static final String SHOW_REFERENCERS_COMMAND = "SHOW REFERENCERS COMMAND";
    private static final String DUMP_AS_ROOT_COMMAND = "DUMP AS ROOT COMMAND";
    private static final String SHOW_ALL_REFERENCERS_COMMAND = "SHOW ALL REFERENCERS COMMAND";

    private static int SPACING = 4;

    // Unfortunately, in what follows there are some implicit
    // assumptions about LEFT_MARGIN being equal to X_SPACING and
    // TOP_MARGIN being equal to Y_SPACING so if you change any of
    // these you may have to do more work thatn you asked for.

    private static int LEFT_MARGIN = SPACING;
    private static int RIGHT_MARGIN = SPACING;
    private static int TOP_MARGIN = SPACING;
    private static int BOTTOM_MARGIN = SPACING;

    private static int X_SPACING = SPACING;
    private static int Y_SPACING = SPACING;

     

    private static int BUTTON_HEIGHT = 20;
    private static int BUTTON_WIDTH = 80;

    private static int CHECKBOX_WIDTH = 120;
    private static int MESSAGE_WIDTH = 250;

    /** Height of view */
    public static int HEIGHT = TOP_MARGIN + BOTTOM_MARGIN + 3 *(BUTTON_HEIGHT + Y_SPACING);

    /** Minimum width */
    public static int MIN_WIDTH = 300;

    /** Minimum height */
    public static int MIN_HEIGHT = HEIGHT;

    private Popup inspectMode = null;
    private TextField inspectingBits;
    private Button backButton;
    private Button refreshButton;
    private Button fullNamesCheckBox;
    private Button showAddressesCheckBox = null;

    private Button runQueueSourceBreakCheckBox;
    private Button runQueueTargetBreakCheckBox;

    private TextField nextMessage = null;
    private Button acceptButton = null;
    private Button showMessageInQueueButton = null;
    private Button showReferencersButton = null;
    private Button dumpAsRootButton = null;
    private Button printAllReferencersButton = null;
    private Button inspectSourceButton = null;
    private Button inspectMessageButton = null;

    private InspectorView inspectorView;
    private Object object = null;

    /**

     * Constructor.

     * @param x - Toolbar initial x position in our containing view.
     * @param y - Toolbar initial y position in our containing view.

     * @param width - Toolbar initial width
     * @param height - Toolbar initial height

     * @param inspectorView trusted notNull - Our containing view.

     */

    InspectorViewToolBar(int x, int y, int width, int height,
                         InspectorView inspectorView,
                         boolean showBackButton,
                         boolean showBreakBox,
                         boolean targetBroken,
                         boolean showStepButtons,
                         boolean showReferencerButtons) {
        super(x, y, width, height);
        this.inspectorView = inspectorView;

        // We put the refresh button as far down as we can
        // since we also have a title bar below, without a border, it won't look crammed

        if (showBackButton) {
            backButton = new InspectorButton(LEFT_MARGIN,
                                             TOP_MARGIN + 0 * (BUTTON_HEIGHT+Y_SPACING),
                                             BUTTON_WIDTH,
                                             BUTTON_HEIGHT);
            backButton.setTitle("Back");
            backButton.setTarget(this);
            backButton.setCommand(BACK_COMMAND);
            addSubview(backButton);  
        }

        refreshButton = Button.createPushButton(LEFT_MARGIN,
                                                TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                                BUTTON_WIDTH,
                                                BUTTON_HEIGHT);
        refreshButton.setTitle("Refresh");
        refreshButton.setTarget(this);
        refreshButton.setCommand(REFRESH_COMMAND);
        addSubview(refreshButton);  

        if (showBreakBox) {
            runQueueTargetBreakCheckBox =
              Button.createCheckButton(LEFT_MARGIN,
                                       TOP_MARGIN + 2 * (BUTTON_HEIGHT + Y_SPACING),
                                       CHECKBOX_WIDTH, 
                                       BUTTON_HEIGHT);
            runQueueTargetBreakCheckBox.setState(targetBroken);
            runQueueTargetBreakCheckBox.setTitle("Target Break");
            runQueueTargetBreakCheckBox.setTarget(this);
            runQueueTargetBreakCheckBox.setCommand(TOGGLE_RUNQUEUE_BREAK);
            addSubview(runQueueTargetBreakCheckBox);   
        }

        fullNamesCheckBox =
          Button.createCheckButton(LEFT_MARGIN + CHECKBOX_WIDTH + X_SPACING,
                                   TOP_MARGIN + 2 * (BUTTON_HEIGHT + Y_SPACING),
                                   CHECKBOX_WIDTH, 
                                   BUTTON_HEIGHT);
        fullNamesCheckBox.setRaisedBorder(ECScrollBorder.border());
        fullNamesCheckBox.setState(false);
        fullNamesCheckBox.setTitle("Full type names");
        fullNamesCheckBox.setTarget(this);
        fullNamesCheckBox.setCommand(TOGGLE_FULL_NAMES);
        addSubview(fullNamesCheckBox);   

        if (showStepButtons) {
            acceptButton = new Button(LEFT_MARGIN, 
                                      TOP_MARGIN,
                                      BUTTON_WIDTH,
                                      BUTTON_HEIGHT);
            acceptButton.setState(targetBroken);
            acceptButton.setTitle("Accept Msg");
            acceptButton.setTarget(this);
            acceptButton.setCommand(ACCEPT_MESSAGE_COMMAND);
            addSubview(acceptButton);   

            nextMessage = new TextField(LEFT_MARGIN + BUTTON_WIDTH + X_SPACING,
                                        TOP_MARGIN,
                                        200, 
                                        20);
            nextMessage.setStringValue("");
            nextMessage.setTransparent(false);
            nextMessage.setBorder(null);
            nextMessage.setEditable(false);
            addSubview(nextMessage);

            inspectSourceButton = new Button(LEFT_MARGIN + 1 * (BUTTON_WIDTH+X_SPACING),
                                             TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                             BUTTON_WIDTH,
                                             BUTTON_HEIGHT);
            inspectSourceButton.setState(targetBroken);
            inspectSourceButton.setTitle("Insp. Src");
            inspectSourceButton.setTarget(this);
            inspectSourceButton.setCommand(INSPECT_SOURCE_COMMAND);
            addSubview(inspectSourceButton);   

            inspectMessageButton = new Button(LEFT_MARGIN + 2 * (BUTTON_WIDTH+X_SPACING),
                                              TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                              BUTTON_WIDTH,
                                              BUTTON_HEIGHT);
            inspectMessageButton.setState(targetBroken);
            inspectMessageButton.setTitle("Insp. Msg");
            inspectMessageButton.setTarget(this);
            inspectMessageButton.setCommand(INSPECT_MESSAGE_COMMAND);
            addSubview(inspectMessageButton);   

            showMessageInQueueButton = new Button(LEFT_MARGIN + 3 * (BUTTON_WIDTH+X_SPACING),
                                                  TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                                  BUTTON_WIDTH,
                                                  BUTTON_HEIGHT);
            showMessageInQueueButton.setState(targetBroken);
            showMessageInQueueButton.setTitle("Show Queue");
            showMessageInQueueButton.setTarget(this);
            showMessageInQueueButton.setCommand(SHOW_QUEUE_COMMAND);
            addSubview(showMessageInQueueButton);   
        }

        dumpAsRootButton = new Button(LEFT_MARGIN + 2 * (BUTTON_WIDTH+X_SPACING),
                                      TOP_MARGIN + 0 * (BUTTON_HEIGHT+Y_SPACING),
                                      BUTTON_WIDTH,
                                      BUTTON_HEIGHT);
        dumpAsRootButton.setState(targetBroken);
        dumpAsRootButton.setTitle("Dump ->");
        dumpAsRootButton.setTarget(this);
        dumpAsRootButton.setCommand(DUMP_AS_ROOT_COMMAND);
        addSubview(dumpAsRootButton);   

        if (showReferencerButtons) {
            showReferencersButton = new Button(LEFT_MARGIN + 3 * (BUTTON_WIDTH+X_SPACING),
                                               TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                               BUTTON_WIDTH + 12,
                                               BUTTON_HEIGHT);
            showReferencersButton.setState(targetBroken);
            showReferencersButton.setTitle("Referencers");
            showReferencersButton.setTarget(this);
            showReferencersButton.setCommand(SHOW_REFERENCERS_COMMAND);
            addSubview(showReferencersButton);   

            printAllReferencersButton = new Button(LEFT_MARGIN + 2 * (BUTTON_WIDTH+X_SPACING),
                                                   TOP_MARGIN + 1 * (BUTTON_HEIGHT+Y_SPACING),
                                                   BUTTON_WIDTH,
                                                   BUTTON_HEIGHT);
            printAllReferencersButton.setState(targetBroken);
            printAllReferencersButton.setTitle("Dump <-");
            printAllReferencersButton.setTarget(this);
            printAllReferencersButton.setCommand(SHOW_ALL_REFERENCERS_COMMAND);
            addSubview(printAllReferencersButton);   

            showAddressesCheckBox =
              Button.createCheckButton(LEFT_MARGIN + 2 * (CHECKBOX_WIDTH + X_SPACING),
                                       TOP_MARGIN + 2 * (BUTTON_HEIGHT + Y_SPACING),
                                       CHECKBOX_WIDTH, 
                                       BUTTON_HEIGHT);
            showAddressesCheckBox.setRaisedBorder(ECScrollBorder.border());
            showAddressesCheckBox.setState(false);
            showAddressesCheckBox.setTitle("Show addresses");
            showAddressesCheckBox.setTarget(this);
            showAddressesCheckBox.setCommand(TOGGLE_SHOW_ADDRESSES);
            addSubview(showAddressesCheckBox);   

        }
    }

    /**

     * Clear the window reference for our InspectorView - someone
     * decided to re-use it for some other view.

     */

    public void lostItsWindow() {
        inspectorView.lostItsWindow();
    }

    public void setMessage(String message) {
        nextMessage.setStringValue(message);
    }

    void updateColors(InspectorView previousView) {
        FieldView.updateButtonColor(backButton,previousView);
    }

    /**

     * Perform command; responsibility from Target interface, called when we
     * are target of a command from an IFC control etc.

     * @param command - String, the command to execute.
     * @param arg suspect nullOk - Ignored argument.

     */

    public void performCommand(String command, Object arg) {
        if (command.equals(ACCEPT_MESSAGE_COMMAND)) {
            inspectorView.acceptMessage();
        } else if (command.equals(INSPECT_SOURCE_COMMAND)) {
            inspectorView.refreshObjectView();
        } else if (command.equals(INSPECT_MESSAGE_COMMAND)) {
            inspectorView.refreshObjectView();
        } else if (command.equals(INSPECT_TARGET_COMMAND)) {
            inspectorView.refreshObjectView();
        } else if (command.equals(BACK_COMMAND)) {

            // Right click gives new window.
            // Control key reverses meaning of mouse.

            boolean reUseWindow =
              ((!((InspectorButton)arg).wasRightClick()) ^
               ((InspectorButton)arg).wasControlClick());
            inspectorView.backCommand(reUseWindow);
        } else if (command.equals(REFRESH_COMMAND)) {
            inspectorView.refreshObjectView();
        } else if (command.equals(SHOW_QUEUE_COMMAND)) {
            inspectorView.refreshObjectView();
        } else if (command.equals(TOGGLE_FULL_NAMES)) {
            inspectorView.setUseFullNames(fullNamesCheckBox.state());
        } else if (command.equals(TOGGLE_SHOW_ADDRESSES)) {
            inspectorView.setShowAddresses(showAddressesCheckBox.state());
        } else if (command.equals(TOGGLE_RUNQUEUE_BREAK)) {
            inspectorView.setRunQueueTargetBreakState(runQueueTargetBreakCheckBox.state());
        } else if (command.equals(SHOW_REFERENCERS_COMMAND)) {
            inspectorView.showReferencers();
        } else if (command.equals(DUMP_AS_ROOT_COMMAND)) {
            inspectorView.dumpAsRoot();
        } else if (command.equals(SHOW_ALL_REFERENCERS_COMMAND)) {
            inspectorView.showAllReferencers();
        } else {
            throw new Error("Unexpected command: " + command + " arg = " + arg);
        }
    }
}

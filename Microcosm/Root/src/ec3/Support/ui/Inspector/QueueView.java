package ec.ui;

import ec.ifc.app.*;
import netscape.application.*;

import netscape.application.Font;
import ec.e.run.RtQ;
import ec.e.run.RtQObj;
import ec.e.run.RtEnvelope;
import ec.e.run.OnceOnlyException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import ec.e.inspect.Runlet;
import ec.e.inspect.RunQueueInspector;
import ec.e.inspect.Inspector;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970616
 */

/**

 * View class for displaying a Run Queue, or rather, a vector of
 * Runlets wrapping events that make up this run queue.

 */

class QueueView extends View implements WindowOwner, Target, EventProcessor, RunletProcessor {

    private String myName;
    private Runlet myRunlet;
    private Runlet myNextRunlet = null; // Next runlet to display whenever it's ready for it.
    private Runlet selectedRunlet = null; // Current selection, if single Runlet (displays detail)
    private Vector selectedRunlets = null; // Current selection, if more than one (no detail display)

    private Vector myMarkedQueue = null;
    private Vector myRunQueue = null;

    private int myHoldState = RunQueueInspector.HOLD_NONE;

    /* Scroll group containing the field views */

    private ECExternalWindow myParentWindow = null;
    private ECExternalWindow myWindow = null;
    private Application myApplication = null;

    public int BUTTON_X_SPACING = 8; // Horizontal spacing between buttons
    public int BUTTON_Y_SPACING = 8; // Vertical spacing between button rows

    private int STEP_BUTTON_WIDTH = 55; // Width of "Step" button
    private int FINISH_BUTTON_WIDTH = 55;
    private int RELEASE_ALL_BUTTON_WIDTH = 55;
    private int RELEASE_BUTTON_WIDTH = 55;
    private int FROM_BUTTON_WIDTH = 100;
    private int MESSAGE_BUTTON_WIDTH = 100;
    private int TO_BUTTON_WIDTH = 100;
    private int CAUSALITY_CHECKBOX_WIDTH = 120;
    private int CAUSALITY_BUTTON_WIDTH = 120;
    private int SHOW_CHRONO_CHECKBOX_WIDTH = 140;
    private int GOTO_BUTTON_WIDTH = 55;
    private int MIDDLE = 400;

    private int NAME_HEIGHT = 18;
    private int UP_BUTTON_WIDTH = 50;

    private int LEFT_MARGIN = 10;
    private int LABEL_WIDTH = 50; 
    private int LABEL_HEIGHT = NAME_HEIGHT; 
    private int LABEL_INDENT = LEFT_MARGIN + LABEL_WIDTH + BUTTON_X_SPACING; 
    private int RIGHT_MARGIN = LEFT_MARGIN;
    private int BUTTON_LEFT_MARGIN = LEFT_MARGIN + LABEL_INDENT; // Indentation for buttons with margin labels
    private int BUTTON_RIGHT_MARGIN = RIGHT_MARGIN;
    private int FRAME_SPACING = 4; // Spacing for containerviews around widgets
    private int RADIO_MARGIN = LEFT_MARGIN + UP_BUTTON_WIDTH + BUTTON_X_SPACING + 10; // 10 Extra!
    private int RADIO_LEFT_MARGIN = RADIO_MARGIN + LABEL_WIDTH; // Leave room for margin labels
    private int BUTTON_HEIGHT = 16;
    private int RADIO_HEIGHT = 18;
    private int TOP_MARGIN = 10; // Vertical distance above first ui element
    private int BOTTOM_MARGIN = 10; // Vertical distance below last button
    private int RUN_BUTTON_WIDTH = 40; // Width of "Run" button
    private int RADIO_WIDTH = 65;

    private int MY_NAME_WIDTH = 80;
    private int PARENT_BUTTON_WIDTH = 30;

    private int TOP_BR1_OFFSET = TOP_MARGIN;
    private int TOP_BR2_OFFSET = TOP_BR1_OFFSET + RADIO_HEIGHT + BUTTON_Y_SPACING;
    private int TOP_BR3_OFFSET = TOP_BR2_OFFSET + NAME_HEIGHT + FRAME_SPACING; // A bit closer

    // These two are offsets *from the bottom* of the view

    private int BOTTOM_BR1_OFFSET = BUTTON_HEIGHT + BOTTOM_MARGIN;
    private int BOTTOM_BR2_OFFSET = BOTTOM_BR1_OFFSET + BUTTON_HEIGHT + BUTTON_Y_SPACING;
    private int BOTTOM_BR3_OFFSET = BOTTOM_BR2_OFFSET + BUTTON_HEIGHT + BUTTON_Y_SPACING;
    private int BOTTOM_BR4_OFFSET = BOTTOM_BR3_OFFSET + BUTTON_HEIGHT + BUTTON_Y_SPACING;

    private int DETAIL_WIDTH = 400;
    private int DETAIL_LEFT_MARGIN = LEFT_MARGIN + STEP_BUTTON_WIDTH + MESSAGE_BUTTON_WIDTH + 2 * BUTTON_X_SPACING;

    public static String RUNLET_MARKED_COMMAND = "Hold Marked";
    public static String RUNLET_ALL_COMMAND = "Hold All";
    public static String RUNLET_NONE_COMMAND = "Hold None";
    public static String RUNLET_PARENT_COMMAND = "Runlet Parent Display";
    public static String RUNLET_UP_COMMAND = "Up View Runlet";
    public static String RUNLET_DISPLAY_COMMAND = "Display Runlet";
    public static String RUNLET_STEP_COMMAND = "Step Runlet";
    public static String RUNLET_FINISH_COMMAND = "Finish Runlet";
    public static String RUNLET_RELEASE_COMMAND = "Release Runlet";
    public static String RUNLET_RELEASE_ALL_COMMAND = "Release All Runlets";
    public static String RUNLET_INSPECT_FROM_COMMAND = "Inspect Runlet Sender";
    public static String RUNLET_INSPECT_MESSAGE_COMMAND = "Inspect Runlet Message";
    public static String RUNLET_INSPECT_TO_COMMAND = "Inspect Runlet Recipient";
    public static String RUNLET_FORWARD_COMMAND = "Run Runqueue Forward";
    public static String RUNLET_RANDOMLY_COMMAND = "Run Runqueue Randomly";
    public static String RUNLET_REVERSE_COMMAND = "Run Runqueue In Reverse";
    public static String TOGGLE_CAUSALITY_TRACE_COMMAND = "Toggle Causality Trace";
    public static String SHOW_CAUSALITY_COMMAND = "Show Causality Trace";
    public static String SHOW_GOTO_DIALOG_COMMAND = "Show GoTo Dialog";
    public static String GOTO_COMMAND = "GoTo Command";
    public static String TOGGLE_SHOW_CHRONO_COMMAND = "Toggle Chronological Display";
    public static String PROFILE_RUNQUEUE_COMMAND = "Profile Runqueue";
    public static String PROFILE_EXECUTION_COMMAND = "Profile Execution";
    public static String TIMER_PROFILE_COMMAND = "Time Profile";
    public static String RESIZE_PROFILE_ARRAY_COMMAND = "Change Profile Array";
    public static String REFRESH_FREQUENCY_COMMAND = "Refresh Frequency";

    // Dimensions are fixed - For now.

    private int windowX = 40;
    private int windowY = 40;
    private int scrollAreaWidth = 800;
    private int scrollAreaHeight = 170;
    private int winContentWidth;
    private int winContentHeight;

    private RunletListView myRunletListView = null; // List view that manages our RunletViews.
    private ScrollGroup myRunletScrollGroup = null; // Scrolling view containing it
    private ECTextField myNameField = null;
    private Vector myTopLevelRunQueVector; // Vector containing interesting Runlets
    private Button causalityButton;
    private Button gotoButton;
    private Runlet prevDetailRunlet = null;
    private Button markedRadio;
    private Button allRadio;
    private Button noneRadio;
    private Button profileRadio;

    private Button myStepButton;
    private Button myFinishButton;
    private Button myReleaseButton;
    private Button myReleaseAllButton;
    private Button myFromButton;
    private Button myMessageButton;
    private Button myInspectButton;
    private Button myUpButton;

    ECTextField mySourceDetail;
    ECTextField myMessageDetail;
    ECTextField myTargetDetail;

    private int         myArraySize = 10;
    private ECTextField myLengthData;
    private ECTextField myLatencyData;
    private Timer       myTimer;
    private int         myDelay = 500;
    private int         myQueueLength = 0;
    private long        myLatency = 0;
    private long        myCounter = 1;
    private long[]      myTopTenTimes;
    private String[]    myTopTenNames;
    private Popup       myAverageSizePopup;
    private Popup       myRefreshFrequencyPopup;
    private boolean     myIsProfilingExecutionTimes = false;

    Button causalityTraceCheckBox;
    Button showChronologicallyCheckBox;

    // private LayoutManager layoutManager = new QueueViewLayoutManager(this);

    /**
     * Constructor
     *
     * @param name notNull - A String, the name of the Precedent button
     * @param consequences - A Vector of consequences Runlets. Each of this is displayed as a button.
     * @param width - The requested width for this view.
     * @param height - The requested height for this view.

     */

    QueueView(ECExternalWindow parentWindow, int width, int height)
         throws OnceOnlyException {
             super(0, 0, width, height);
             Inspector.setupRunQueueInspector(); // Setup inspector, if not done before.
             myName = null;
             myRunlet = null;
             myParentWindow = parentWindow;

             // Create the invariant parts of a QueueView
             setBuffered(true);
             ContainerView markedOrAllGroup = new ContainerView
                 (RADIO_MARGIN,
                  TOP_BR1_OFFSET - FRAME_SPACING,
                  LABEL_WIDTH + BUTTON_X_SPACING + 4 * (RADIO_WIDTH + BUTTON_X_SPACING),
                  RADIO_HEIGHT + 2 * FRAME_SPACING);
             markedOrAllGroup.setTransparent(true);
             markedOrAllGroup.setBorder(ECBevelBorder.border());
             addSubview(markedOrAllGroup);

             ECTextField holdLabel = new ECTextField(FRAME_SPACING,
                                                     FRAME_SPACING,
                                                     LABEL_WIDTH,
                                                     LABEL_HEIGHT);
             holdLabel.setTransparent(true);
             holdLabel.setBorder(null);
             holdLabel.setEditable(false);
             holdLabel.setStringValue("Hold:");
             markedOrAllGroup.addSubview(holdLabel);

             markedRadio = Button.createRadioButton(FRAME_SPACING + LABEL_WIDTH + BUTTON_X_SPACING,
                                                    FRAME_SPACING,
                                                    RADIO_WIDTH,
                                                    RADIO_HEIGHT);
             markedRadio.setTarget(this);
             markedRadio.setState(true);
             markedRadio.setCommand(RUNLET_MARKED_COMMAND);
             markedRadio.setTitle("Broken");
             markedOrAllGroup.addSubview(markedRadio);

             allRadio = Button.createRadioButton
                 (FRAME_SPACING + LABEL_WIDTH + RADIO_WIDTH + 2 * BUTTON_X_SPACING,
                  FRAME_SPACING,
                  RADIO_WIDTH,
                  RADIO_HEIGHT);
             allRadio.setTarget(this);
             allRadio.setCommand(RUNLET_ALL_COMMAND);
             allRadio.setTitle("All");
             markedOrAllGroup.addSubview(allRadio);

             noneRadio = Button.createRadioButton
                 (FRAME_SPACING + LABEL_WIDTH + 2 * RADIO_WIDTH + 2 * BUTTON_X_SPACING,
                  FRAME_SPACING,
                  RADIO_WIDTH,
                  RADIO_HEIGHT);
             noneRadio.setTarget(this);
             noneRadio.setCommand(RUNLET_NONE_COMMAND);
             noneRadio.setTitle("None");
             markedOrAllGroup.addSubview(noneRadio);
             
             profileRadio = Button.createRadioButton
                 (FRAME_SPACING + LABEL_WIDTH + 3 * RADIO_WIDTH + 2 * BUTTON_X_SPACING,
                  FRAME_SPACING,
                  RADIO_WIDTH,
                  RADIO_HEIGHT);
             profileRadio.setTarget(this);
             profileRadio.setCommand(PROFILE_RUNQUEUE_COMMAND);
             profileRadio.setTitle("Profile");
             markedOrAllGroup.addSubview(profileRadio);


             myUpButton = Button.createPushButton(LEFT_MARGIN,
                                                  TOP_BR1_OFFSET,
                                                  UP_BUTTON_WIDTH,
                                                  NAME_HEIGHT);
             myUpButton.setTarget(this);
             myUpButton.setCommand(RUNLET_UP_COMMAND);
             myUpButton.setTitle("Up");
             addSubview(myUpButton);     // Make upButton be subview of this QueueView

             myNameField = new ECTextField(LEFT_MARGIN,
                                         TOP_BR2_OFFSET,
                                         width - (LEFT_MARGIN + RIGHT_MARGIN),
                                         NAME_HEIGHT);
                                           
             myNameField.setTransparent(false);
             myNameField.setBorder(ECBevelBorder.border());
             myNameField.setEditable(false);
             myNameField.setStringValue("Run queue");
             myNameField.setCommand(RUNLET_PARENT_COMMAND);
             myNameField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             addSubview(myNameField);

             // Add a scroll group that will scroll the view that displays the Consequence Runlets

             scrollAreaWidth = width - (LEFT_MARGIN + RIGHT_MARGIN);
             scrollAreaHeight = height - (TOP_BR3_OFFSET + BOTTOM_BR4_OFFSET + 2 * FRAME_SPACING);
             myRunletScrollGroup = new ScrollGroup (LEFT_MARGIN, TOP_BR3_OFFSET,
                                                    scrollAreaWidth, scrollAreaHeight);
             myRunletScrollGroup.setHasVertScrollBar(true);
             myRunletScrollGroup.setHasHorizScrollBar(true);
             myRunletScrollGroup.setBorder(ECScrollBorder.border());
             myRunletScrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
             myRunletScrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             addSubview(myRunletScrollGroup);

             // Create a runlet list view. This is a fixed size view containing all 
             // runlets and it is displayed in the scrollgroup view.
             myRunletListView = new RunletListView
                 (0,0,5000, scrollAreaHeight, this); // We are the target for all runlet views
             myRunletListView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myRunletScrollGroup.setContentView(myRunletListView);

             myStepButton = Button.createPushButton(LEFT_MARGIN,
                                                    height - BOTTOM_BR4_OFFSET, // Third row from bottom
                                                    STEP_BUTTON_WIDTH,
                                                    BUTTON_HEIGHT);
             myStepButton.setTarget(this);
             myStepButton.setCommand(RUNLET_STEP_COMMAND);
             myStepButton.setTitle("Step");
             myStepButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myStepButton);     // Make stepButton be subview of this QueueView


             myFinishButton = Button.createPushButton(LEFT_MARGIN,
                                                      height - BOTTOM_BR3_OFFSET, // Second row from bottom
                                                      FINISH_BUTTON_WIDTH,
                                                      BUTTON_HEIGHT);
             myFinishButton.setTarget(this);
             myFinishButton.setCommand(RUNLET_FINISH_COMMAND);
             myFinishButton.setTitle("Finish");
             myFinishButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myFinishButton);     // Make finishButton be subview of this QueueView


             myReleaseButton = Button.createPushButton(LEFT_MARGIN,
                                                       height - BOTTOM_BR2_OFFSET, // Second row from bottom
                                                       RELEASE_BUTTON_WIDTH,
                                                       BUTTON_HEIGHT);
             myReleaseButton.setTarget(this);
             myReleaseButton.setCommand(RUNLET_RELEASE_COMMAND);
             myReleaseButton.setTitle("Release");
             myReleaseButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myReleaseButton); // Make releaseButton be subview of this QueueView


             myReleaseAllButton = Button.createPushButton(LEFT_MARGIN,
                                                          height - BOTTOM_BR1_OFFSET, // fourth row from bottom
                                                          RELEASE_ALL_BUTTON_WIDTH,
                                                          BUTTON_HEIGHT);
             myReleaseAllButton.setTarget(this);
             myReleaseAllButton.setCommand(RUNLET_RELEASE_ALL_COMMAND);
             myReleaseAllButton.setTitle("Rel All");
             myReleaseAllButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myReleaseAllButton); // Make releaseAllButton be subview of this QueueView


             myFromButton = Button.createPushButton(LEFT_MARGIN + STEP_BUTTON_WIDTH + BUTTON_X_SPACING,
                                                    height - BOTTOM_BR3_OFFSET, // Second row from bottom
                                                    FROM_BUTTON_WIDTH,
                                                    BUTTON_HEIGHT);
             myFromButton.setTarget(this);
             myFromButton.setCommand(RUNLET_INSPECT_FROM_COMMAND);
             myFromButton.setTitle("Inspect Source");
             myFromButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myFromButton);     // Make fromButton be subview of this QueueView

             myAverageSizePopup = new Popup(myFromButton.x(), 
                                            myFromButton.y() - BUTTON_Y_SPACING - BUTTON_HEIGHT - 1,
                                            FROM_BUTTON_WIDTH, 
                                            BUTTON_HEIGHT + 2);

             myAverageSizePopup.addItem("Top 10", RESIZE_PROFILE_ARRAY_COMMAND);
             myAverageSizePopup.addItem("Top 20", RESIZE_PROFILE_ARRAY_COMMAND);
             myAverageSizePopup.addItem("Top 50", RESIZE_PROFILE_ARRAY_COMMAND);
             myAverageSizePopup.addItem("Top 100", RESIZE_PROFILE_ARRAY_COMMAND);

             myAverageSizePopup.setTarget(this);
             myAverageSizePopup.selectItemAt(0);
             myAverageSizePopup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myAverageSizePopup.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myAverageSizePopup);

             myLengthData = new ECTextField
                 (myFromButton.x() + FROM_BUTTON_WIDTH + BUTTON_X_SPACING, 
                  myFromButton.y() - BUTTON_Y_SPACING - BUTTON_HEIGHT - 1,
                  2 * FROM_BUTTON_WIDTH - 40, 
                  BUTTON_HEIGHT + 2);
                                           
             myLengthData.setTransparent(false);
             myLengthData.setBorder(ECBevelBorder.border());
             myLengthData.setEditable(false);
             myLengthData.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myLengthData.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myLengthData);

             myLatencyData = new ECTextField(myLengthData.x() + myLengthData.width() + BUTTON_X_SPACING, 
                                             myLengthData.y(),
                                             myLengthData.width(), 
                                             myLengthData.height());
                                           
             myLatencyData.setTransparent(false);
             myLatencyData.setBorder(ECBevelBorder.border());
             myLatencyData.setEditable(false);
             myLatencyData.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myLatencyData.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myLatencyData);

             TextField label = ECTextField.createLabel("Refresh:");
             label.moveTo
                 (myLatencyData.x() + myLatencyData.width() + BUTTON_X_SPACING,
                  myLatencyData.y());
             label.sizeToMinSize();
             label.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             label.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(label);

             myRefreshFrequencyPopup = new Popup
                 (label.x() + label.width() + BUTTON_X_SPACING, 
                  label.y() - 1,
                  FROM_BUTTON_WIDTH, 
                  BUTTON_HEIGHT + 2);

             myRefreshFrequencyPopup.addItem("250", REFRESH_FREQUENCY_COMMAND);
             myRefreshFrequencyPopup.addItem("500", REFRESH_FREQUENCY_COMMAND);
             myRefreshFrequencyPopup.addItem("1000", REFRESH_FREQUENCY_COMMAND);
             myRefreshFrequencyPopup.addItem("2000", REFRESH_FREQUENCY_COMMAND);

             myRefreshFrequencyPopup.setTarget(this);
             myRefreshFrequencyPopup.selectItemAt(1);
             myRefreshFrequencyPopup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myRefreshFrequencyPopup.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myRefreshFrequencyPopup);

             myMessageButton = Button.createPushButton(LEFT_MARGIN + FINISH_BUTTON_WIDTH + BUTTON_X_SPACING,
                                                            height - BOTTOM_BR2_OFFSET, // Second row from bottom
                                                            MESSAGE_BUTTON_WIDTH,
                                                            BUTTON_HEIGHT);
             myMessageButton.setTarget(this);
             myMessageButton.setCommand(RUNLET_INSPECT_MESSAGE_COMMAND);
             myMessageButton.setTitle("Inspect Message");
             myMessageButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myMessageButton);     // Make messageButton be subview of this QueueView


             myInspectButton = Button.createPushButton(LEFT_MARGIN + RELEASE_BUTTON_WIDTH + BUTTON_X_SPACING,
                                                       height - BOTTOM_BR1_OFFSET, // Last row from bottom
                                                       TO_BUTTON_WIDTH,
                                                       BUTTON_HEIGHT);
             myInspectButton.setTarget(this);
             myInspectButton.setCommand(RUNLET_INSPECT_TO_COMMAND);
             myInspectButton.setTitle("Inspect Target");
             myInspectButton.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             addSubview(myInspectButton);     // Make toButton be subview of this QueueView

             mySourceDetail = new ECTextField(DETAIL_LEFT_MARGIN, 
                                            height - BOTTOM_BR3_OFFSET,
                                            width - (DETAIL_LEFT_MARGIN + RIGHT_MARGIN),
                                            LABEL_HEIGHT);
             mySourceDetail.setTransparent(false);
             mySourceDetail.setBorder(null);
             mySourceDetail.setEditable(false);
             mySourceDetail.setStringValue("");
             mySourceDetail.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             mySourceDetail.setHorizResizeInstruction(WIDTH_CAN_CHANGE); 
             addSubview(mySourceDetail);

             myMessageDetail = new ECTextField(DETAIL_LEFT_MARGIN, 
                                             height - BOTTOM_BR2_OFFSET,
                                             width - (DETAIL_LEFT_MARGIN + RIGHT_MARGIN),
                                             LABEL_HEIGHT);
             myMessageDetail.setTransparent(false);
             myMessageDetail.setBorder(null);
             myMessageDetail.setEditable(false);
             myMessageDetail.setStringValue("");
             myMessageDetail.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             myMessageDetail.setHorizResizeInstruction(WIDTH_CAN_CHANGE); 
             addSubview(myMessageDetail);

             myTargetDetail = new ECTextField(DETAIL_LEFT_MARGIN, 
                                            height - BOTTOM_BR1_OFFSET,
                                            width - (DETAIL_LEFT_MARGIN + RIGHT_MARGIN),
                                            LABEL_HEIGHT);
             myTargetDetail.setTransparent(false);
             myTargetDetail.setBorder(null);
             myTargetDetail.setEditable(false);
             myTargetDetail.setStringValue("");
             myTargetDetail.setVertResizeInstruction(TOP_MARGIN_CAN_CHANGE);
             myTargetDetail.setHorizResizeInstruction(WIDTH_CAN_CHANGE); 
             addSubview(myTargetDetail);


             winContentWidth = width;
             winContentHeight = height;

             //             updateLater();

             myWindow = new ECExternalWindow();
             myWindow.setTitle("Run Queue");
             Size windowSize = myWindow.windowSizeForContentSize(winContentWidth, winContentHeight);
             myWindow.setBounds(windowX, windowY, windowSize.width, windowSize.height);
             myWindow.setResizable(true);
             //        sizeTo(winContentWidth, winContentHeight);
             setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
             setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
             myWindow.addSubview(this);
             myWindow.setOwner(this);
             myApplication = Application.application();
             if (myApplication == null) {
                 throw new RuntimeException("Cannot open RunQueue inspector when not running under IFC");
             }
             myTimer = new Timer(myApplication.eventLoop(), this, TIMER_PROFILE_COMMAND, myDelay);
             myTopTenTimes = new long[myArraySize];
             myTopTenNames = new String[myArraySize];
    }

    public void refreshRunlet(Runlet runlet) {
        if (runlet != null) {
            Object viewObject = runlet.getUIRef();
            if (viewObject != null) {
                ((RunletView)viewObject).setDirty(true);
            }
        }
    }
        
    public void changeSelection(Runlet newSelection, boolean updateNow) {
        if (updateNow) {
            if (selectedRunlet != null) {
                Object viewObject = selectedRunlet.getUIRef();
                if (viewObject != null) {
                    ((RunletView)viewObject).unSelect();
                }
            }
            selectedRunlet = newSelection;
            if (selectedRunlet != null) {
                Object viewObject = selectedRunlet.getUIRef();
                if (viewObject != null) {
                    ((RunletView)viewObject).select();
                }
            }
            updateDetailInfo(selectedRunlet);
        } else {
            selectedRunlet = newSelection;
        }
    }



    /**

     * Create and refresh our myRunletListView and make it the
     * contents of the scrollgroup

     */

    public void updateRunletListView(boolean checkIfRun) {
        Vector con = null;
        boolean switchToProfileMode = false;

        // If there is a current runlet, update the display to show
        // it.  This means we display its consequences in the
        // RunletListView Otherwise we display the name of the queue
        // we are looking at (regular run queue or held messages
        // queue) and the contents of the queue.

        if (myRunlet == null) {
            switch (myHoldState) {
                case RunQueueInspector.HOLD_ALL:
                    myNameField.setStringValue("All");
                    con = myTopLevelRunQueVector;
                    break;
                case RunQueueInspector.HOLD_NONE:
                    myNameField.setStringValue("Running free");
                    con = null;
                    break;
                case RunQueueInspector.HOLD_SOME:
                    myNameField.setStringValue("Captured");
                    con = myTopLevelRunQueVector;
                    break;
                case RunQueueInspector.PROFILE_RUNQUEUE:
                    myNameField.setStringValue("Profile");
                    switchToProfileMode = true;
                    con = null;
                    break;
            }
        } 
        else {
            if (checkIfRun) {
                myRunlet.allHaveBeenRun();
            }
            con = myRunlet.consequences();
            
            myNameField.setStringValue(myRunlet.description(true));
            // XXX Make this field a real runlet field.
            
            //   if (myRunlet == selectedRunlet)
            //     myNameField.setBackgroundColor(displayColor(myRunlet).darkerColor());
            myNameField.setBackgroundColor(displayColor(myRunlet));
        }

        if (con != null) {
            Enumeration e = con.elements();
            while (e.hasMoreElements()) {
                Runlet r = (Runlet)e.nextElement();
                boolean selected = false;
                if (checkIfRun) {
                    r.allHaveBeenRun();
                }
                if (r == selectedRunlet) {
                    selected = true;
                }
                myRunletListView.addRunlet(r,selected); // XXX Need a better name here!
            }
        }

        setButtons(switchToProfileMode);
    }

    public static Color displayColor(Runlet runlet) {
        if (runlet == null) return netscape.application.Color.black;
        switch (runlet.colorNumber()) {
        case Runlet.VIRGIN_COLOR: return netscape.application.Color.white;
        case Runlet.LEAF_COLOR: return netscape.application.Color.green;
        case Runlet.DONE_COLOR: return netscape.application.Color.pink;
        default: return netscape.application.Color.yellow;
        }
    }

    void moveToFront() {
        myWindow.show();
    }

    public void setHoldState(int holdState) {
        myHoldState = holdState;
    }

    public void invalidateRunlet(String name, Object refreshObject) {
        if (refreshObject instanceof Runlet) {
            refreshRunlet((Runlet)refreshObject);
        }
    }

    public void refreshQueueView(String objName, long timeInQueue, int queueLength) {
        myQueueLength = Math.max(queueLength, myQueueLength);
        processTimes(objName, timeInQueue);
        myLatency += timeInQueue;
        ++myCounter;
    }

    public void refreshQueueView(String name, Object refreshObject) {
        // The RunQueue inspector is suggesting we refresh some display.
        // What to (possibly) refresh is indicated by the refreshObject.

        // If the refreshObject is a runlet, then we compare it to our
        // opinion about what to show when this call arrives.  We may
        // have prepared the next display to show by having assigned a
        // Runlet to myNextRunlet, which will become the next Runlet
        // parent to display the consequences for.

        // If the refreshObject is not nextRunlet, then we check if it
        // is a line in the consequentlist of nextRunlet, and then if
        // it is in the conseqentlist of myRunlet. In either case
        // (they may be the same is nextRunlet == myRunlet) we refresh
        // that line in the display.

        // If we don't know the next parent Runlet, then myNextRunlet
        // is null and we accept whatever the RunQueue inspector tells
        // us as the next value of myRunlet.

        if (refreshObject instanceof Runlet) {
            Runlet runlet = (Runlet)refreshObject;
            if (myNextRunlet == runlet) {
                if (myRunlet != myNextRunlet) { // Switch to expected runlet
                    myRunlet = myNextRunlet;
                }
            } 
            else if (refreshObject == myRunlet) {
                myRunlet.allHaveBeenRun();
                refreshRunlet(myRunlet); // Update display of displayed runlet
            } 
            else {
                Vector con = null;
                if (myRunlet != null) {
                    con = myRunlet.consequences();
                }
                else {
                    con = myTopLevelRunQueVector;
                }
                if (con != null) {
                    int index = con.indexOf(runlet);
                    if (index > -1) {
                        // Runlet was visible in the current display
                        runlet.allHaveBeenRun();
                        refreshRunlet(runlet);
                    }
                }
            }
        } 
        else if (refreshObject instanceof Vector) {

            // This is the only place where we set our toplevel run queue vector.
            // Kind of a strange choice, but it was easy to do this way.

            if (myTopLevelRunQueVector == null) {
                myTopLevelRunQueVector = (Vector)refreshObject;
            } 
            else {

                // XXX Hack - If the object is the top level vector,
                // we refresh the vector if that's what we are
                // showing, otherwise we ignore this.  If myRunlet is
                // null, then we are showing top level. However, if
                // the vector is some other vector, then we display
                // it.

                // Redo the API to allow more control of this instead of this hack.               

                if (refreshObject == myTopLevelRunQueVector &&
                    myRunlet != null) return; // If showing real runlet, ignore top level refresh
                
                // Vector is not top level vector. Probably
                // chronological vector. Just display it.
                updateRunletListView(true);
                return;
            }
        } 
        updateNow();
    }

    void addRunlet(String name, Runlet runlet, boolean selected) {
        myRunletListView.addRunlet(runlet, selected);
    }

    private void releaseAllTopLevelRunlets() {

        // Release all runlets at the top level!
        // *Start* by copying the vector and clearing the old one!

        if (myTopLevelRunQueVector != null) {
            changeSelection(null,false);

            // I don't know how enumerations work in detail so I'll
            // carefully clone the vector before creating an
            // enumeration from it.

            Vector tmpVector = (Vector)myTopLevelRunQueVector.clone();
            myTopLevelRunQueVector.removeAllElements();
            Enumeration e = tmpVector.elements();
            while (e.hasMoreElements()) {
                Runlet freedRunlet = (Runlet)e.nextElement();
                RunQueueInspector.releaseRunlet(freedRunlet);
            }
            updateNow();
        }
    }

    /**

     * Perform command; responsibility from Target interface.  Handles
     * all commands from UI elements in the window, including commands
     * from the consequence button list.

     * @param command nullOK trusted - A String, the command to perform.
     * @param arg nullOK untrusted - Ignored.

     */

    public void performCommand(String command, Object arg) {
        Runlet argRunlet = null;
        if (arg != null && arg instanceof Runlet) argRunlet = (Runlet)arg;

        if (PROFILE_RUNQUEUE_COMMAND.equals(command)) {
            myRunlet = null;
            myHoldState = RunQueueInspector.PROFILE_RUNQUEUE;
            RunQueueInspector.setHoldState(RunQueueInspector.PROFILE_RUNQUEUE);
            updateRunletListView(false);
            myTimer.start();
            resetArrays();
        }
        else if (TIMER_PROFILE_COMMAND.equals(command)) {
            if (myIsProfilingExecutionTimes) {
                myLengthData.setStringValue
                    ("Total messages: " + Long.toString(myCounter));
                myLatencyData.setStringValue
                    ("Avg Execution Time: " + Long.toString(myLatency / myCounter));
            }
            else {
                myLengthData.setStringValue
                    ("Max Queue Length: " + Integer.toString(myQueueLength));
                myLatencyData.setStringValue
                    ("Avg Latency: " + Long.toString(myLatency / myCounter));
            }
            myRunletListView.update(myTopTenTimes, myTopTenNames);
            myRunletListView.setDirty(true);
        }
        else if (RESIZE_PROFILE_ARRAY_COMMAND.equals(command)) {
            myArraySize = Integer.parseInt(myAverageSizePopup.selectedItem().title().substring(4));
            myTopTenTimes = new long[myArraySize];
            myTopTenNames = new String[myArraySize];
            resetArrays();
        }
        else if (REFRESH_FREQUENCY_COMMAND.equals(command)) {
            myDelay = Integer.parseInt(myRefreshFrequencyPopup.selectedItem().title());
            myTimer.setDelay(myDelay);
        }
        else if (PROFILE_EXECUTION_COMMAND.equals(command)) {
            if (myIsProfilingExecutionTimes) {
                myIsProfilingExecutionTimes = false;
                myInspectButton.setTitle("=> Execution");
                myHoldState = RunQueueInspector.PROFILE_RUNQUEUE;
                RunQueueInspector.setHoldState(RunQueueInspector.PROFILE_RUNQUEUE);
            }
            else {
                myIsProfilingExecutionTimes = true;
                myInspectButton.setTitle("=> Runqueue");
                myHoldState = RunQueueInspector.PROFILE_EXECUTION;
                RunQueueInspector.setHoldState(RunQueueInspector.PROFILE_EXECUTION);
            }
            resetArrays();
            myLengthData.setStringValue("");
            myLatencyData.setStringValue("");
        }
        else if (RUNLET_INSPECT_FROM_COMMAND.equals(command)) {
            if (selectedRunlet != null)
                ec.e.inspect.Inspector.inspect(selectedRunlet.sourceToInspect(),
                                               selectedRunlet.sourceString());

        }
        else if (RUNLET_INSPECT_MESSAGE_COMMAND.equals(command)) {
            if (selectedRunlet != null)
                ec.e.inspect.Inspector.inspect(selectedRunlet.messageToInspect(),
                                               selectedRunlet.messageString());

        }
        else if (RUNLET_INSPECT_TO_COMMAND.equals(command)) {
            if (selectedRunlet != null)
                ec.e.inspect.Inspector.inspect(selectedRunlet.targetToInspect(),
                                               selectedRunlet.targetString());

        }
        else if (RUNLET_MARKED_COMMAND.equals(command)) {
            stopProfiling();
            myHoldState = RunQueueInspector.HOLD_SOME;
            RunQueueInspector.setHoldState(RunQueueInspector.HOLD_SOME);
            updateRunletListView(false);
        }
        else if (RUNLET_ALL_COMMAND.equals(command)) {
            stopProfiling();
            myHoldState = RunQueueInspector.HOLD_ALL;
            RunQueueInspector.setHoldState(RunQueueInspector.HOLD_ALL);
            updateRunletListView(false);
        }
        else if (RUNLET_NONE_COMMAND.equals(command)) {
            stopProfiling();
            myHoldState = RunQueueInspector.HOLD_NONE;
            RunQueueInspector.setHoldState(RunQueueInspector.HOLD_NONE);
            updateRunletListView(false);
            releaseAllTopLevelRunlets();
        }
        else if (RUNLET_UP_COMMAND.equals(command)) {
            Runlet oldRunlet = myRunlet;
            if (myRunlet != null) {
                myRunlet = myRunlet.parent(); // Allow null parent
            }
            changeSelection(oldRunlet,true);
            updateNow();
        }
        else if (RUNLET_PARENT_COMMAND.equals(command)) {
            Runlet oldRunlet = myRunlet;
            if (myRunlet != null) {
                myRunlet = myRunlet.parent(); // Allow null parent
            }
            changeSelection(oldRunlet,true);
            updateNow();
        }
        else if (RUNLET_FINISH_COMMAND.equals(command)) {
            if (selectedRunlet != null) {
                RunQueueInspector.step(selectedRunlet,myRunlet,500);
            }
        }
        else if (RUNLET_STEP_COMMAND.equals(command)) {
            RunQueueInspector.step(selectedRunlet,selectedRunlet,1);
        }
        else if (RUNLET_RELEASE_ALL_COMMAND.equals(command)) {
            if ((myHoldState == RunQueueInspector.PROFILE_RUNQUEUE) || 
                (myHoldState == RunQueueInspector.PROFILE_EXECUTION)) {
                resetArrays();
            }
            else {
                releaseAllTopLevelRunlets();
            }
        }
        else if (RUNLET_RELEASE_COMMAND.equals(command)) {
            if (selectedRunlet != null) {
                Runlet freedRunlet = selectedRunlet;
                RunQueueInspector.releaseRunlet(freedRunlet);
                changeSelection(null,false);
                if (myRunlet != null && myRunlet.consequences() != null) {
                    myRunlet.consequences().removeElement(freedRunlet);
                } else {        // myrunlet == null -> top level?
                    if (myTopLevelRunQueVector != null) // Hope this works...
                        myTopLevelRunQueVector.removeElement(freedRunlet);
                }
                updateNow();
            }
        }
        else if (TOGGLE_CAUSALITY_TRACE_COMMAND.equals(command)) {
            RunQueueInspector.setCausalityTrace(causalityTraceCheckBox.state());
        }
        else if (SHOW_CAUSALITY_COMMAND.equals(command)) {
        }
        else if (TOGGLE_SHOW_CHRONO_COMMAND.equals(command)) {
            //         RunQueueInspector.showChronologically(showChronologicallyCheckBox.state());
        } 
    }

    public void processEvent(Event event) {
        if (event instanceof InspectorEvent) {
            InspectorEvent iEvent = (InspectorEvent)event;
            Runlet argRunlet = null;
            Object argObject = iEvent.getObject();
            if (argObject != null && argObject instanceof Runlet) {
                argRunlet = (Runlet)argObject;
            }

            switch (iEvent.type()) {
                case InspectorEvent.RUNLET_CONTROL_DOUBLE_CLICK_EVENT:
                    if (selectedRunlet != null) {
                        RunQueueInspector.step(selectedRunlet,myRunlet,500);
                    }
                    break;
                case InspectorEvent.RUNLET_RELEASE_EVENT:
                case InspectorEvent.RUNLET_SHIFT_DOUBLE_CLICK_EVENT:
                    if (selectedRunlet != null) {
                        Runlet freedRunlet = selectedRunlet;
                        RunQueueInspector.releaseRunlet(freedRunlet);
                        changeSelection(null,false);
                        if (myRunlet != null && myRunlet.consequences() != null) {
                            myRunlet.consequences().removeElement(freedRunlet);
                        } else {        // myrunlet == null -> top level?
                            if (myTopLevelRunQueVector != null) // Hope this works...
                                myTopLevelRunQueVector.removeElement(freedRunlet);
                        }
                        updateNow();
                    }
                    break;
                case InspectorEvent.RUNLET_DOUBLE_CLICK_EVENT:
                    if (argRunlet != null) {
                        // If something is runnable, we run it but don't
                        // descend unless there is something interesting to
                        // descend to.

                        if (argRunlet.isRunnable()) {
                            changeSelection(null,true);
                            myNextRunlet = argRunlet;
                            RunQueueInspector.step(argRunlet,argRunlet,1); // and if runnable, run it first
                            refreshRunlet(argRunlet); // XXX Flailing. 
                        } else {
                            myRunlet = argRunlet; // If not runnable, descend into it no matter what.
                            updateNow();
                        }
                    }
                    break;
                case InspectorEvent.RUNLET_CONTROL_SINGLE_CLICK_EVENT:
                    changeSelection(argRunlet,true);
                    break;
                case InspectorEvent.RUNLET_SHIFT_SINGLE_CLICK_EVENT:
                    break;
                case InspectorEvent.RUNLET_SINGLE_CLICK_EVENT:
                    changeSelection(argRunlet,true);
                    break;
            }
        }
    }


    /**

     * Update the detail information fields from the given Runlet.

     */

    private void updateDetailInfo(Runlet runlet) {
        if (runlet == null) {
            mySourceDetail.setStringValue("");
            myMessageDetail.setStringValue("");
            myTargetDetail.setStringValue("");
        } 
        
        if (prevDetailRunlet == runlet) return;
        prevDetailRunlet = runlet;
        
        if (runlet != null) {
            mySourceDetail.setStringValue(" " + runlet.sourceString());
            myMessageDetail.setStringValue(" " + runlet.messageString());
            myTargetDetail.setStringValue(" " + runlet.targetString());
        }
    }

    private void stopProfiling() {
        myLengthData.setStringValue("");
        myLatencyData.setStringValue("");
        myTimer.stop();
        myQueueLength = 0;
        myRunletListView.update(null, null);
        myRunletListView.setDirty(true);

        for (int i = 0; i < myArraySize; i++) {
            myTopTenTimes[i] = 0;
            myTopTenNames[i] = "";
        }
    }

    /**
     * Store top ten times in descending order
     */
    private void processTimes(String name, long timeInQueue) {
        for (int i = 0; i < myArraySize; i++) {
            if (myTopTenTimes[i] < timeInQueue) {
                for (int j = myArraySize; --j > i; ) {
                    myTopTenTimes[j] = myTopTenTimes[j - 1];
                    myTopTenNames[j] = myTopTenNames[j - 1];
                }
                myTopTenTimes[i] = timeInQueue;
                myTopTenNames[i] = name;
                break;
            }
        }
    }

    private void resetArrays() {
        for (int i = 0; i < myArraySize; i++) {
            myTopTenTimes[i] = 0;
            myTopTenNames[i] = "";
        }
        myRunletListView.update(null, null);
        myRunletListView.setDirty(true);
        myLatency = 0;
        myCounter = 1;
        myQueueLength = 0;
    }

    private void setButtons(boolean switchToProfileMode) {
        if (switchToProfileMode) {
            myStepButton.setEnabled(false);
            myFinishButton.setEnabled(false);
            myReleaseButton.setEnabled(false);
            myFromButton.setEnabled(false);
            myMessageButton.setEnabled(false);
            myUpButton.setEnabled(false);

            myAverageSizePopup.setEnabled(true);
            myRefreshFrequencyPopup.setEnabled(true);
            myReleaseAllButton.setTitle("Reset");
            myRunletListView.removeAllRunlets();
            myRunletListView.setDirty(true);
            updateDetailInfo(null);
            myInspectButton.setCommand(PROFILE_EXECUTION_COMMAND);
            myInspectButton.setTitle("=> Execution");
            myIsProfilingExecutionTimes = false;
        }
        else {
            myStepButton.setEnabled(true);
            myFinishButton.setEnabled(true);
            myReleaseButton.setEnabled(true);
            myFromButton.setEnabled(true);
            myMessageButton.setEnabled(true);
            myUpButton.setEnabled(true);

            myAverageSizePopup.setEnabled(false);
            myRefreshFrequencyPopup.setEnabled(false);
            myReleaseAllButton.setTitle("Rel All");
            myRunletListView.update(null, null);
            myInspectButton.setCommand(RUNLET_INSPECT_TO_COMMAND);
            myInspectButton.setTitle("Inspect Target");
        }
    }

    /**

     * Handle refresh display events from (low level) RunQueueInspector

     */

    public void updateNow() {
        if (selectedRunlet != null) {
            selectedRunlet.allHaveBeenRun(); // Must update this manually
            refreshRunlet(selectedRunlet);
        }
        updateRunletListView(true);
        updateDetailInfo(selectedRunlet);
    }

    public void handleEventLater(Object anObject, int eventType) {
        if (myApplication == null) {
            myApplication = Application.application();
            if (myApplication == null) {
                System.out.println("Cannot send update event - Not running under IFC");
                return;
            }
        }
        InspectorEvent event = new InspectorEvent(eventType, anObject);
        event.setProcessor(this);
        myApplication.eventLoop().addEvent(event);
     }

    /**

     * Responsibilities from WindowOwner

     */

    public void windowDidBecomeMain(netscape.application.Window window) {
    }

    public void windowDidHide(netscape.application.Window window) {
    }

    public void windowDidResignMain(netscape.application.Window window) {
    }

    public void windowDidShow(netscape.application.Window window) {
    }

    public boolean windowWillHide(netscape.application.Window window) {
        return true;
    }

    public boolean windowWillShow(netscape.application.Window window) {
        return true;
    }

    public void windowWillSizeBy(netscape.application.Window window, Size size) {
    }
}


public interface RunletProcessor {
    public void handleEventLater(Object anObject, int eventType);
}

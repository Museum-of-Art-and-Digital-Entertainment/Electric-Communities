package ec.ifc.app.test;

import netscape.application.*;
import ec.ifc.app.ECTabView;
import ec.ifc.app.ECTabItem;

public class TabTester extends Application
{
    public static void main (String args[])  {
        TabTester application = new TabTester();
        TabController controller = new TabController(args, application);
        application.run();
    }
}

class TabController implements Target, WindowOwner  {

    private static TabTester myApplication;
    private static ExternalWindow myWindow;

    private static final int MARGIN = 8;
    private static final int LABEL_WIDTH = 50;
    private static final int CONTROL_LEFT = LABEL_WIDTH + MARGIN;
    private static final int TAB_WIDTH = 30;
    private static final int RADIO_WIDTH = 130;
    private static final int FIELD_WIDTH = 50;
    private static final int CONTROL_HEIGHT = 25;
    
    private ECTabView targetTabs = null;
    private ECTabView horizTabs = null;
    private ECTabView vertTabs = null;
    
    private TextField spacingField = null;
    private Button allCapsRadioButton = null;
    private Button initialCapsRadioButton = null;
    
    private Font labelFont = Font.fontNamed("Helvetica", Font.BOLD, 12);
    
    // command constants
    private static final String commandQuitApplication = "quit application";
    private static final String commandTargetHorizontal = "target horizontal";
    private static final String commandTargetVertical = "target vertical";
    private static final String commandSetSpacing = "set spacing";
    private static final String commandSetAllCaps = "set all caps";
    private static final String commandSetInitialCaps = "set initial caps";
    
    //
    // constructors
    //
        
    public TabController (String args[], TabTester application)  {
        myApplication = application;
        createTabs();
    }
    
    //
    // public methods
    //
    

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command))
            System.exit(0);
        else if (commandTargetHorizontal.equals(command)) {
            targetTabs = horizTabs;
            resetTabControls();
        }
        else if (commandTargetVertical.equals(command)) {
            targetTabs = vertTabs;
            resetTabControls();
        }
        else if (commandSetSpacing.equals(command)) {
            targetTabs.setSpacing(spacingField.intValue());
            // update value in field, which might have been pinned
            resetSpacingField();
        }
        else if (commandSetAllCaps.equals(command)) {
            changeTitleCase(targetTabs, true);
        }
        else if (commandSetInitialCaps.equals(command)) {
            changeTitleCase(targetTabs, false);
        }
    }

    //
    // WindowOwner methods
    //
    public void windowDidBecomeMain(Window window) {
    }
        
    public void windowDidHide(Window window) {
        performCommand(commandQuitApplication, this);
    }
        
    public void windowDidResignMain(Window window){
    }
    
    public void windowDidShow(Window window){
    }

    public boolean windowWillHide(Window window){
        return(true);
    }

    public boolean windowWillShow(Window window){
        return(true);
    }

    public void windowWillSizeBy(Window window, Size size){
    }

    //
    // private methods
    //

    private static void addTabItem(ECTabView tabView,
                                   String title, 
                                   Color color) {
        ECTabItem item = tabView.addItem();
        item.setTitle(title);
        item.setBackgroundColor(color);
    }
        
    /** convenience routine for adding a text label to a view */
    private TextField addTextLabel(String text, View container,
                                  int x, int y, int width, int height) {
        TextField label = TextField.createLabel(text, labelFont);
        label.setJustification(Graphics.RIGHT_JUSTIFIED);
        label.setBounds(x, y, width, height);
        container.addSubview(label);
        return label;
    }
    
    /** toggles title case between initial-caps and all-caps */
    private void changeTitleCase(ECTabView tabView, boolean toAllCaps) {
        int count = tabView.count();
        for (int index = 0; index < count; index += 1) {
            ECTabItem item = tabView.itemAt(index);
            String oldTitle = item.title();
            if (toAllCaps) {
                item.setTitle(oldTitle.toUpperCase());
            }
            else {
                item.setTitle(oldTitle.toLowerCase());
            }
        }
        
        tabView.setDirty(true);
    }

    /** set up all the viewables */
    private void createTabs()  {

        myWindow = new ExternalWindow();
        myWindow.setTitle("tab playground");
        myWindow.setOwner(this);
        myWindow.rootView().setColor(Color.lightGray);
        myWindow.rootView().setBuffered(true);

        Size windowSize = myWindow.windowSizeForContentSize(400, 300);
        myWindow.setBounds(360, 10, windowSize.width, windowSize.height);
        Size contentSize = myWindow.contentSize();
        
        // create vertical tabs
        vertTabs = new ECTabView(contentSize.width - MARGIN - TAB_WIDTH,
                           TAB_WIDTH + MARGIN,
                           TAB_WIDTH,
                           contentSize.height - 2*MARGIN - TAB_WIDTH,
                           ECTabView.RIGHT_OF_CONTENT);
        vertTabs.setMinTabLength(50);
        ECTabItem prototypeItem = vertTabs.prototypeItem();
        prototypeItem.setSelectedColor(Color.green);
        prototypeItem.setFont(labelFont);
        vertTabs.addItem().setTitle("yappy");
        vertTabs.addItem().setTitle("spunky");
        vertTabs.addItem().setTitle("cocoa-nut");
        vertTabs.addItem().setTitle("max");
        vertTabs.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        vertTabs.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        myWindow.addSubview(vertTabs);
        
        // create horizontal tabs
        horizTabs = new ECTabView(MARGIN, MARGIN,
                       contentSize.width - 2*MARGIN - TAB_WIDTH,
                       TAB_WIDTH, ECTabView.ABOVE_CONTENT);
        prototypeItem = horizTabs.prototypeItem();
        prototypeItem.setFont(labelFont);
        addTabItem(horizTabs, "aye-aye", Color.red);
        addTabItem(horizTabs, "indri", Color.magenta);
        addTabItem(horizTabs, "sifaka", Color.cyan);
        addTabItem(horizTabs, "mouse lemur", Color.yellow);
        addTabItem(horizTabs, "ring-tailed lemur", Color.green);
        horizTabs.setAutomaticTabLength(true);
                       
        horizTabs.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myWindow.addSubview(horizTabs);
        
        // start with horiz tabs as the target
        targetTabs = horizTabs;
        
        // start laying out controls
        int controlY = 2*MARGIN + TAB_WIDTH;
        
        // create radio buttons that switch between horiz and vert tabs
        Button radioButton;
        
        radioButton = Button.createRadioButton(CONTROL_LEFT,
                                               controlY,
                                               RADIO_WIDTH,
                                               CONTROL_HEIGHT);
        radioButton.setTitle("adjust horizontal");
        radioButton.setFont(labelFont);
        radioButton.setTarget(this);
        radioButton.setCommand(commandTargetHorizontal);
        radioButton.setState(true);
        myWindow.addSubview(radioButton);
        
        radioButton = Button.createRadioButton(CONTROL_LEFT + RADIO_WIDTH + MARGIN,
                                               controlY,
                                               RADIO_WIDTH,
                                               CONTROL_HEIGHT);
        radioButton.setTitle("adjust vertical");
        radioButton.setFont(labelFont);
        radioButton.setTarget(this);
        radioButton.setCommand(commandTargetVertical);
        myWindow.addSubview(radioButton);
        
        // a little extra space below this since it's different than the others
        controlY += CONTROL_HEIGHT + 2*MARGIN;
        
        // create spacing controller
        spacingField = new TextField(CONTROL_LEFT,
                                     controlY,
                                     FIELD_WIDTH,
                                     CONTROL_HEIGHT);
        spacingField.setTarget(this);
        spacingField.setCommand(commandSetSpacing);
        myWindow.addSubview(spacingField);
        
        addTextLabel("spacing:", myWindow.rootView(), 0, controlY,
                     LABEL_WIDTH, CONTROL_HEIGHT);
        
        controlY += CONTROL_HEIGHT + MARGIN;
        
        // Create radio buttons that switch between initial caps and all caps
        // Put them in their own container so they don't fight with vert/horiz
        ContainerView radioButtonHolder 
            = new ContainerView(CONTROL_LEFT, controlY,
                                2*RADIO_WIDTH + 2*MARGIN,
                                CONTROL_HEIGHT);
        radioButtonHolder.setBorder(null);
        myWindow.addSubview(radioButtonHolder);
        
        radioButton = Button.createRadioButton(0, 0,
                                               RADIO_WIDTH, CONTROL_HEIGHT);
        allCapsRadioButton = radioButton;
        radioButton.setTitle("all caps");
        radioButton.setTarget(this);
        radioButton.setCommand(commandSetAllCaps);
        radioButtonHolder.addSubview(radioButton);
        
        radioButton = Button.createRadioButton(MARGIN + RADIO_WIDTH, 0,
                                               RADIO_WIDTH, CONTROL_HEIGHT);
        initialCapsRadioButton = radioButton;
        radioButton.setTitle("initial caps");
        radioButton.setTarget(this);
        radioButton.setCommand(commandSetInitialCaps);
        radioButtonHolder.addSubview(radioButton);
        
        addTextLabel("labels:", myWindow.rootView(), 0, controlY,
                     LABEL_WIDTH, CONTROL_HEIGHT);
        
        controlY += CONTROL_HEIGHT + MARGIN;

        // initialize control values
        resetTabControls();
    
        myWindow.show();
    }
    
    /**
     * Set state of controls that affect selected tabs to reflect
     * the selected tabs (which probably just changed)
     */
    private void resetTabControls() {
        resetSpacingField();
        
        // manually check whether first title is all caps
        String firstTitle = targetTabs.itemAt(0).title();
        boolean targetIsAllCaps = firstTitle.equals(firstTitle.toUpperCase());
        initialCapsRadioButton.setState(!targetIsAllCaps);
        allCapsRadioButton.setState(targetIsAllCaps);
    }
    
    private void resetSpacingField() {
        spacingField.setIntValue(targetTabs.spacing());
    }
}


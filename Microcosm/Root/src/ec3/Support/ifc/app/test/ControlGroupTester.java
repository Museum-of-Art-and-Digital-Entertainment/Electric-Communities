package ec.ifc.app.test;

import netscape.application.*;
import ec.ifc.app.ECControlGroup;
import ec.ifc.app.ECControlGroupItem;
import ec.ifc.app.ECSimpleCGItem;
import ec.ifc.app.ECStaticTextCGItem;
import ec.ifc.app.ECTwoControlCGItem;

public class ControlGroupTester extends Application
{
    public static void main (String args[])  {
        ControlGroupTester application = new ControlGroupTester();
        ControlGroupSample sample = new ControlGroupSample(args, application);
        application.run();
    }
}

class ControlGroupSample implements Target, WindowOwner  {

    private static ControlGroupTester myApplication;
    private ExternalWindow myWindow;
    private ECControlGroup controlGroup;
    private Button showLabelCheckbox;
    private TextField labelWidthField;
    
    // layout constants
    private static final int MARGIN = 8;
    private static final int CONTROL_HEIGHT = 25;
    private static final int SHOW_LABELS_WIDTH = 100;
    private static final int LABEL_WIDTH_LABEL_WIDTH = 70;
    private static final int LABEL_WIDTH_FIELD_WIDTH = 40;
    
    // command constants
    private static final String commandQuitApplication = "quit application";
    private static final String commandToggleLabels = "toggle labels";
    private static final String commandChangeLabelWidth = "change label width";
    
    //
    // constructors
    //
        
    public ControlGroupSample (String args[], ControlGroupTester application)  {
        myApplication = application;
        createAndShowWindow();
    }
    
    //
    // public methods
    //
    
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command))
            System.exit(0);
        else if (commandToggleLabels.equals(command)) {
            controlGroup.setShowLabels(!controlGroup.showLabels());
        }
        else if (commandChangeLabelWidth.equals(command)) {
            int newWidth = labelWidthField.intValue();
            controlGroup.setLabelWidth(newWidth);
            labelWidthField.setIntValue(controlGroup.getLabelWidth());
        }
    }

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

    /** set up all the viewables */
    private void createAndShowWindow()  {

        // create and set up window
        myWindow = new ExternalWindow();
        myWindow.setTitle("Control Group test");
        myWindow.setOwner(this);
        myWindow.rootView().setColor(Color.lightGray);
        myWindow.rootView().setBuffered(true);

        Size windowSize = myWindow.windowSizeForContentSize(400, 300);
        myWindow.setBounds(360, 10, windowSize.width, windowSize.height);
        Size contentSize = myWindow.contentSize();
        
        // create and set up label controls
        Button showLabelCheckBox = Button.createCheckButton(
            MARGIN, MARGIN, SHOW_LABELS_WIDTH, CONTROL_HEIGHT);
        showLabelCheckBox.setTitle("show labels");
        showLabelCheckBox.setTarget(this);
        showLabelCheckBox.setState(true);
        showLabelCheckBox.setCommand(commandToggleLabels);
        myWindow.addSubview(showLabelCheckBox);
        
        int x = 2*MARGIN + SHOW_LABELS_WIDTH;
        TextField labelWidthLabel = TextField.createLabel("label width:");
        labelWidthLabel.setBounds(x, MARGIN,
                                  LABEL_WIDTH_LABEL_WIDTH, CONTROL_HEIGHT);
        myWindow.addSubview(labelWidthLabel);
        
        x += LABEL_WIDTH_LABEL_WIDTH + MARGIN;
        labelWidthField = new TextField();
        labelWidthField.setBounds(x, MARGIN,
                                  LABEL_WIDTH_FIELD_WIDTH, CONTROL_HEIGHT);
        labelWidthField.setTarget(this);
        labelWidthField.setCommand(commandChangeLabelWidth);
        myWindow.addSubview(labelWidthField);
        
        // create and set up control group
        ScrollGroup scrollGroup = new ScrollGroup(
            MARGIN,
            CONTROL_HEIGHT + 2*MARGIN,
            contentSize.width - 2*MARGIN,
            contentSize.height - 3*MARGIN - CONTROL_HEIGHT);
        scrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        scrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        myWindow.addSubview(scrollGroup);
        
        controlGroup = new ECControlGroup(scrollGroup);
        fillControlGroup(controlGroup);         
        
        labelWidthField.setIntValue(controlGroup.getLabelWidth());
        myWindow.show();
    }
    
    /** fills the control group with a bunch of stuff */
    private static void fillControlGroup(ECControlGroup controlGroup) {
        addNewStaticTextItem(controlGroup, "Here's my favorite cow poem",
                    Graphics.LEFT_JUSTIFIED);
        addNewTextFieldItem(controlGroup, "I've:");
        addNewTextFieldItem(controlGroup, "never:");
        addNewTextFieldItem(controlGroup, "seen:");
        addNewTextFieldItem(controlGroup, "a:");
        addNewTextFieldItem(controlGroup, "purple\ncow:");
        addNewStaticTextItem(controlGroup, "(pause)",
                    Graphics.CENTERED);
        addNewTextFieldItem(controlGroup, "I:");
        addNewTextFieldItem(controlGroup, "never:");
        addNewTextFieldItem(controlGroup, "hope:");
        addNewTextFieldItem(controlGroup, "to:");
        addNewTextFieldItem(controlGroup, "see:");
        addNewTextFieldItem(controlGroup, "one:");
        addNewStaticTextItem(controlGroup, "...hope you liked it!",
                    Graphics.RIGHT_JUSTIFIED);
    }
    
    private static void addNewStaticTextItem(ECControlGroup controlGroup,
                                             String text,
                                             int justification) {
        ECStaticTextCGItem newItem = new ECStaticTextCGItem(text);
        newItem.sizeTo(0, randomBetween(20, 30));
        newItem.setFont(Font.fontNamed("Helvetica", Font.BOLD, 12));
        newItem.setJustification(justification);
        controlGroup.addItem(newItem);                                          
    }
    
    private static void addNewTextFieldItem(ECControlGroup controlGroup,
                                            String label) {
        TextField control = new TextField();
        control.setBackgroundColor(randomColor());
        ECSimpleCGItem newItem;
        if (randomBoolean()) {
            newItem = new ECSimpleCGItem(
                control, null, label, randomBetween(20, 60));           
        }
        else {
            Button checkbox = Button.createCheckButton(0, 0, 20, 20);
            newItem = new ECTwoControlCGItem(
                control, checkbox, null, label, randomBetween(20, 60));         
        }
        newItem.setControlResizesAutomatically(randomBoolean());
        controlGroup.addItem(newItem);
    }
    
    private static Color randomColor() {
        return new Color((float)Math.random(),
                         (float)Math.random(),
                         (float)Math.random());
    }
    
    private static int randomBetween(int low, int high) {
        return low + (int)(Math.random() * (high - low));
    }
    
    private static boolean randomBoolean() {
        return Math.random() < 0.5;
    }
}


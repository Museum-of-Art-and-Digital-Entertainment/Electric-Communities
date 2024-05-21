/**
 * StoneLookTester.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 * This is test code for the custom interface look.
 * To run this test, type to a shell:
 * java ec.e.start.EBoot ec.ifc.stonelook.test.Tester1
 */
package ec.ifc.stonelook.test;

import java.io.File;
import java.util.Vector;

import netscape.application.Application;
import netscape.application.Button;
import netscape.application.Color;
import netscape.application.ContainerView;
import netscape.application.ListView;
import netscape.application.Popup;
import netscape.application.Rect;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.TextField;
import netscape.application.View;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.ifc.stonelook.SLButton;
import ec.ifc.stonelook.SLChecklist;
import ec.ifc.stonelook.SLComboBox;
import ec.ifc.stonelook.SLListView;
import ec.ifc.stonelook.SLPopup;
import ec.ifc.stonelook.SLScrollGroup;
import ec.ifc.stonelook.SLTextField;
import ec.ifc.stonelook.SLWindow;
import ec.ifc.stonelook.StoneLook;

import ec.ifc.app.ECApplication;
import ec.ifc.app.ECPageTurner;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;
import ec.ifc.app.ECTextView;
import ec.ifc.app.ECChecklist;
import ec.ifc.app.ECExternalWindow;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.start.crew.CrewCapabilities;

import ec.e.file.EStdio;


eclass Tester1 implements ELaunchable {
    emethod go(EEnvironment env) {
        if (env.args().length > 0 && env.args()[0].equals("o")) {
            // if there's an overrides file lying around, use it
            StoneLook.applyOverridesFromFile(new File("SLOverrides.txt"));          
        }       

        StoneLookTester1 tester = new StoneLookTester1(env);
        tester <- go();
    }
}

eclass StoneLookTester1 {
    private EEnvironment myEnv;

    public StoneLookTester1(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {

        Vat vat = myEnv.vat();

        try {
            Object repository = CrewCapabilities.createCrewRepository(myEnv.props());
            CrewCapabilities.setCrewRepository(repository);
            StoneLook.setRepository(repository);

            Application application = new ECApplication();
            Window1 tester = new Window1();
            application.run();
        } 
        catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}

class Window1 implements Target, WindowOwner  {
    private static StoneLookTester1 application;

    /** window full of assorted UI elements */
    private SLWindow myWindow;
    
    /** elements that can be enabled/disabled en masse */
    private Vector deactivatosaurs = new Vector();

    // layout constants
    /** extra blank space to left of checkmarks column */
    private static final int CHECKMARK_MARGIN = 4;
    /** width of column of checkmarks in segment list */
    private static final int CHECKMARK_WIDTH = 15;
    /** space between UI elements and window edges (not used everywhere) */
    private static final int MARGIN = 8;
    /** initial long dimension of tabs, subject to stretching */
    private static final int TAB_THICKNESS = 30;
    
    // command constants
    private static final String COMMAND_QUIT_APPLICATION = "quit application";
    private static final String COMMAND_TOGGLE_ENABLED = "toggle enabled";

    public Window1 ()  {
        myWindow = new SLWindow(200, 30, 280, 275);
        ContainerView cv = myWindow.getContainerView();
        myWindow.setOwner(this);

        // put some checkboxes in the container view
        Button button;
        button = SLButton.createRadioButton(10, 10, 130, 20);
        button.setTitle("This one");
        button.setState(true);
        deactivatosaurs.addElement(button);
        cv.addSubview(button);

        button = SLButton.createCheckButton(130, 10, 130, 20);
        button.setTitle("I am a checkbox");
        button.setState(true);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(button);
        cv.addSubview(button);

        button = SLButton.createRadioButton(10, 35, 130, 20);
        button.setTitle("No, this one");
        deactivatosaurs.addElement(button);
        cv.addSubview(button);
        
        // here's a combo box of dog breeds
        SLComboBox combo = new SLComboBox(130, 33, 140, 25);
        combo.addItem("Bernese Mountain Dog");
        combo.addItem("Miniature Pinscher");
        combo.addItem("Mutt");
        combo.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(combo);
        cv.addSubview(combo);
        
        // here's a nice popup of saxophone types
        Popup popup = new SLPopup();
        popup.setBounds(130, 70, 140, 25);
        popup.addItem("sopranino", null);
        popup.addItem("soprano", null);
        popup.addItem("alto", null);
        popup.addItem("tenor", null);
        popup.addItem("baritone", null);
        popup.addItem("bass", null);
        popup.addItem("contrabass", null);
        popup.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(popup);
        cv.addSubview(popup);

        button = SLButton.createPushButton(130, 160, 100);
        button.setTitle("Standard");
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(button);
        cv.addSubview(button);

        button = SLButton.createPushButton(130, 200, 100);
        button.setTitle("Default");
        myWindow.setDefaultButton((SLButton)button);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(button);
        cv.addSubview(button);

        button = SLButton.createPushButton(130, 240, 142);
        button.setTitle("Toggle Enabled-ness");
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        button.setCommand(COMMAND_TOGGLE_ENABLED);
        button.setTarget(this);
        cv.addSubview(button);

        // how about a nice scrolling list?
        ListView listView = new SLListView();
        listView.setAllowsEmptySelection(true);
        listView.setAllowsMultipleSelection(false);
        listView.setBounds(0, 0, 150, 200);
        listView.addItem().setTitle("Axolotl");
        listView.addItem().setTitle("Bullfrog");
        listView.addItem().setTitle("Coral snake");
        listView.addItem().setTitle("Death adder");
        listView.addItem().setTitle("Eft");
        listView.addItem().setTitle("Frog");
        listView.addItem().setTitle("Goanna");
        listView.addItem().setTitle("Hellbender");
        listView.addItem().setTitle("Iguana");
        listView.addItem().setTitle("Jackson's Chameleon");
        listView.addItem().setTitle("Komodo dragon");
        listView.addItem().setTitle("Leopard gecko");
        listView.addItem().setTitle("Matamata");
        listView.addItem().setTitle("Newt");
        listView.addItem().setTitle("Oustalet's Chameleon");
        listView.addItem().setTitle("Python");
        
        listView.sizeToMinSize();
        deactivatosaurs.addElement(listView);

        SLScrollGroup scrollingList = new SLScrollGroup();
        scrollingList.setBounds(10, 70, 100, 95);
        scrollingList.setContentView(listView);
        scrollingList.setHasVertScrollBar(true);
        scrollingList.setHasHorizScrollBar(true);
        scrollingList.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);

        cv.addSubview(scrollingList);

        // and here's a scrolling list of checkboxes; the checkboxes are
        // multiply-selectable, but the names to their right are only
        // singly-selectable
        SLListView bondlist = new SLListView();
        bondlist.setBounds(0,0,150,200);
        Rect tempBounds = bondlist.bounds();
        tempBounds.x += CHECKMARK_MARGIN + CHECKMARK_WIDTH;
        tempBounds.width -= CHECKMARK_MARGIN + CHECKMARK_WIDTH;
        bondlist.setBounds(tempBounds);

        SLChecklist checklist = new SLChecklist();
        tempBounds.x = CHECKMARK_MARGIN;
        tempBounds.width = CHECKMARK_WIDTH;
        checklist.setBounds(tempBounds);

        addCheckableItem(checklist, bondlist, "Sean Connery");
        addCheckableItem(checklist, bondlist, "George Lazenby");
        addCheckableItem(checklist, bondlist, "Woody Allen");
        addCheckableItem(checklist, bondlist, "David Niven");
        addCheckableItem(checklist, bondlist, "Roger Moore");
        addCheckableItem(checklist, bondlist, "Timothy Dalton");
        addCheckableItem(checklist, bondlist, "Pierce Brosnan");

        deactivatosaurs.addElement(checklist);
        
        ContainerView bondlistContainer = new ContainerView(0,0,150,200);
        bondlistContainer.setTransparent(true);
        bondlistContainer.setBorder(null);
        bondlistContainer.addSubview(bondlist);
        bondlistContainer.addSubview(checklist);

        // size the container to just hold its subviews
        bondlistContainer.sizeTo(bondlistContainer.width(),
                                 checklist.height());
 
        scrollingList = new SLScrollGroup();
        scrollingList.setContentView(bondlistContainer);
        // must set bounds after setting content view or the scroll group
        // ends up overlapping the bottom of the window (this is a mystery)
        scrollingList.setBounds(10, 175, 100, 95); 
        scrollingList.setHasVertScrollBar(true);
        scrollingList.setHasHorizScrollBar(true);
        scrollingList.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        scrollingList.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        cv.addSubview(scrollingList);


        // let's throw in a text field, what the heck
        TextField textField = new SLTextField();
        textField.setBounds(130, 110, 140, 25);
        textField.setStringValue("type text here");
        textField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        deactivatosaurs.addElement(textField);
        cv.addSubview(textField);

        // make that first window snap right in there, by pre-loading
        // the stuff needed to draw it
        myWindow.rootView().draw();
        myWindow.show();
    }

    /** add an item to a checklist/listview pair */
    private void addCheckableItem(ECChecklist checklist,
                                  ListView listView,
                                  String name) {
        checklist.addItem();
        listView.addItem().setTitle(name);                              
        checklist.sizeToMinSize();
        listView.sizeToMinSize();
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (COMMAND_QUIT_APPLICATION.equals(command)) {
            System.exit(0);
        } else if (COMMAND_TOGGLE_ENABLED.equals(command)) {
            toggleDeactivatosaurs();
        }
    }

    //
    // WindowOwner methods
    //
    public void windowDidBecomeMain(Window window) {
    }
        
    public void windowDidHide(Window window) {
        performCommand(COMMAND_QUIT_APPLICATION, this);
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
    
    private void toggleDeactivatosaurs() {
        int count = deactivatosaurs.size();
        for (int i = 0; i < count; i += 1) {
            Object item = deactivatosaurs.elementAt(i);
            
            if (item instanceof Button) {
                Button button = (Button)item;
                button.setEnabled(!button.isEnabled());
            } else if (item instanceof Popup) {
                Popup popup = (Popup)item;
                popup.setEnabled(!popup.isEnabled());
            } else if (item instanceof ListView) {
                ListView listView = (ListView)item;
                listView.setEnabled(!listView.isEnabled());
            } else if (item instanceof SLComboBox) {
                SLComboBox combo = (SLComboBox)item;
                combo.setEnabled(!combo.isEnabled());
            } else if (item instanceof TextField) {
                TextField textfield = (TextField)item;
                textfield.setEditable(!textfield.isEditable());
            } else {
                System.out.println(
                    "deactivatosaurs contains untoggleable item " + item);
            }
        }
    }
}

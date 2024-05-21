/**
 * StoneLookTester.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 * This is test code for the marble-controls-on-sandstone look.
 */
package ec.ifc.stonelook.test;

import netscape.application.*;
import ec.ifc.stonelook.*;
import ec.ifc.app.ECPageTurner;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;
import ec.ifc.app.ECTextView;
import ec.ifc.app.ECChecklist;
import ec.ifc.app.ECExternalWindow;
import java.util.Vector;

import java.io.File;

/**
 * Application subclass that starts up the stone look test windows.
 * Call this from the command line with
 * "java ec.ifc.stonelook.test.StoneLookTester"
 */
public class StoneLookTester extends Application
{
    public static void main (String args[])  {
//      System.getProperties().list(System.out);
        StoneLookTester application = new StoneLookTester();
        
        if (args.length > 0 && args[0].equals("o")) {
            // if there's an overrides file lying around, use it
            StoneLook.applyOverridesFromFile(new File("SLOverrides.txt"));          
        }       
        
        SLController controller = new SLController(args, application);
        application.run();
    }
}

class SLController implements Target, WindowOwner  {

    private static StoneLookTester application;

    /** window full of assorted UI elements */
    private SLWindow myWindow;

    /** vector of color-changing UI elements */
    private Vector chameleons;

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
    private static final String commandQuitApplication = "quit application";
    private static final String commandToggleCloseBox = "toggle close box";
    private static final String commandToggleTitle = "toggle title";
    private static final String commandUseBlues = "use blues";
    private static final String commandUsePurples = "use purples";

    private static Integer FontSizes[] = {
        new Integer(9), new Integer(10), new Integer(12),
        new Integer(14), new Integer(18), new Integer(24)
    };



    public SLController (String args[], StoneLookTester application)  {
        this.application = application;
        chameleons = new Vector();
        makeSomeStuff();
    }

    private void makeSomeStuff()  {
        /*
        SLWindow window = new SLWindow(200, 30, 280, 275);
        window.show();
        */

        myWindow = new SLWindow(200, 30, 260, 275);
        ContainerView cv = myWindow.getContainerView();

        // put some checkboxes in the container view
        Button button;
        button = SLButton.createRadioButton(10, 10, 130, 20);
        button.setTitle("Blues");
        button.setState(true);
        button.setTarget(this);
        button.setCommand(commandUseBlues);
        cv.addSubview(button);

        button = SLButton.createCheckButton(130, 10, 130, 20);
        button.setTitle("Show Close Box");
        button.setState(true);
        button.setTarget(this);
        button.setCommand(commandToggleCloseBox);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        cv.addSubview(button);

        button = SLButton.createRadioButton(10, 35, 130, 20);
        button.setTitle("Purples");
        button.setTarget(this);
        button.setCommand(commandUsePurples);
        cv.addSubview(button);
        
        button = SLButton.createCheckButton(130, 35, 130, 20);
        button.setTitle("Show Title");
        button.setState(true);
        button.setTarget(this);
        button.setCommand(commandToggleTitle);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        cv.addSubview(button);

        // here's a nice popup of saxophone types
        Popup popup = new SLPopup();
        popup.setBounds(130, 70, 124, 25);
        popup.addItem("sopranino", null);
        popup.addItem("soprano", null);
        popup.addItem("alto", null);
        popup.addItem("tenor", null);
        popup.addItem("baritone", null);
        popup.addItem("bass", null);
        popup.addItem("contrabass", null);
        popup.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        chameleons.addElement(popup);
        cv.addSubview(popup);

        button = SLButton.createPushButton(130, 160, 100);
        button.setTitle("Standard");
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        chameleons.addElement(button);
        cv.addSubview(button);

        button = SLButton.createPushButton(130, 200, 100);
        button.setTitle("Default");
        myWindow.setDefaultButton(button);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        chameleons.addElement(button);
        cv.addSubview(button);

        button = SLButton.createPushButton(130, 240, 100);
        button.setTitle("Disabled");
        button.setEnabled(false);
        button.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        button.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        chameleons.addElement(button);
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

        SLScrollGroup scrollingList = new SLScrollGroup();
        scrollingList.setBounds(10, 70, 100, 95);
        scrollingList.setContentView(listView);
        scrollingList.setHasVertScrollBar(true);
        scrollingList.setHasHorizScrollBar(true);
        scrollingList.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        chameleons.addElement(scrollingList);

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
        chameleons.addElement(scrollingList);

        cv.addSubview(scrollingList);


        // let's throw in a text field, what the heck
        TextField textField = new SLTextField();
        textField.setBounds(130, 110, 124, 25);
        textField.setStringValue("type text here");
        textField.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        cv.addSubview(textField);

        // make that first window snap right in there, by pre-loading
        // the stuff needed to draw it
        myWindow.rootView().draw();
        myWindow.show();

        createAndShowFontTestWindow();
        createAndShowTabTestWindow();
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

    private void createAndShowFontTestWindow() {
        SLWindow window = new SLWindow(200, 350, 260, 250);
        window.setOwner(this);
        window.setResizable(true);
        ContainerView cv = window.getContainerView();
        Rect bounds = cv.bounds();
        
        // gonna throw in a page turning gizmo here too just to see what
        // it looks like. Note that SLPageTurners set their own size.
        SLPageTurner pageTurner = new SLPageTurner();
        pageTurner.moveTo(bounds.width - pageTurner.width(), 0);
        pageTurner.setPageState(ECPageTurner.ON_MIDDLE_PAGE);
        cv.addSubview(pageTurner);

        Rect textBounds = new Rect(10, 40, bounds.width - 20, bounds.height - 50);
        TextView text = new SLTextView();
        text.setBounds(textBounds);
        text.setTransparent(true);
        text.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        text.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);

        ScrollGroup sg = new SLScrollGroup();
        sg.setBounds(textBounds);
        sg.setContentView(text);
        sg.setHasVertScrollBar(true);
        sg.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        sg.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        chameleons.addElement(sg);

        cv.addSubview(sg);


        int charSetSize = 255;
        int initialBogusCharCount = 34;
        char data[] = new char[charSetSize - initialBogusCharCount + 2];
        for (int i = initialBogusCharCount - 1; i < charSetSize; i += 1) {
            data[i - (initialBogusCharCount - 1)] = (char)i;
        }

        // separate font samples with a blank line
        data[data.length - 2] = '\n';
        data[data.length - 1] = '\n';
        String allChars = new String(data);

        addFontSamples(text, allChars, "Helvetica");
        addFontSamples(text, allChars, "TimesRoman");
        addFontSamples(text, allChars, "Courier");
        addFontSamples(text, allChars, "Dialog");
        addFontSamples(text, allChars, "DialogInput");
        
        window.show();
    }

    private void createAndShowTabTestWindow() {
        int tabWindowContentWidth = 260;
        int tabWindowContentHeight = 275;
        
        SLWindow window = new SLWindow(500, 30, 
            tabWindowContentWidth, tabWindowContentHeight);
        window.setOwner(this);
        window.setResizable(true);
        ContainerView cv = window.getContainerView();

        Rect bounds = cv.bounds();
        
        int borderType = BezelBorder.RAISED;
        Border border = new SLBezelBorder(borderType);

        // use our inside knowledge that all SLBezel margins are the
        // same to keep this simpler
        int bezelMargin = border.rightMargin();

        Rect contentsBounds = new Rect(     
            MARGIN + TAB_THICKNESS - bezelMargin, 
            MARGIN + TAB_THICKNESS - bezelMargin, 
            tabWindowContentWidth - 2*(MARGIN + TAB_THICKNESS - bezelMargin),
            tabWindowContentHeight - 2*(MARGIN + TAB_THICKNESS - bezelMargin));
        
        // create container view that the tabs are placed around, to
        // simulate the appearance that tabs would have in a real context
        ContainerView tabContents = new SLContainerView(borderType);
        tabContents.setBounds(contentsBounds);
        tabContents.setBorder(border);
        tabContents.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        tabContents.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        cv.addSubview(tabContents);
        
        // create right tabs
        SLTabView rightTabs = new SLTabView(contentsBounds.maxX() - bezelMargin, 
                                            contentsBounds.y,
                                            TAB_THICKNESS, contentsBounds.height, 
                                            ECTabView.RIGHT_OF_CONTENT);
        // need to set it to false if you want to scroll it, it is set to true by default
        rightTabs.setAutomaticTabLength(false);
        rightTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        rightTabs.setMinTabLength(50);
        rightTabs.setSpacing(5);

        ECTabItem prototypeItem = rightTabs.prototypeItem();
        prototypeItem.setSelectedColor(Color.green);
        prototypeItem.setFont(StoneLook.standardFont());
        addTabItem(rightTabs, "Yappy", Color.red);
        addTabItem(rightTabs, "Spunky", Color.blue);
        addTabItem(rightTabs, "Cocoa-nut", Color.green);
        addTabItem(rightTabs, "Max", Color.magenta);
        addTabItem(rightTabs, "Mityaha", Color.cyan);
        addTabItem(rightTabs, "Alex", Color.yellow);
        addTabItem(rightTabs, "Elen", Color.black);
        addTabItem(rightTabs, "John", Color.white);
        rightTabs.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        rightTabs.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        cv.addSubview(rightTabs);
       
        // create top tabs
        SLTabView topTabs = new SLTabView(contentsBounds.x, 
                                          contentsBounds.y + bezelMargin - TAB_THICKNESS,
                                          contentsBounds.width, 
                                          TAB_THICKNESS, 
                                          ECTabView.ABOVE_CONTENT);
        // need to set it to false if you want to scroll it, it is set to true by default
        topTabs.setAutomaticTabLength(false);
        topTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        topTabs.setMinTabLength(40);

        prototypeItem = topTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFont());
        addTabItem(topTabs, "Aye-Aye", Color.red);
        addTabItem(topTabs, "Indri", Color.magenta);
        addTabItem(topTabs, "Sifaka", Color.cyan);
        addTabItem(topTabs, "Mouse lemur", Color.yellow);
        addTabItem(topTabs, "Ring-tailed lemur", Color.green);
        topTabs.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        cv.addSubview(topTabs);
                
        // create left tabs
        // LEFT_OF_CONTENT is default location, so the location isn't passed as argument
        SLTabView leftTabs = new SLTabView(contentsBounds.x + bezelMargin - TAB_THICKNESS,
                                           contentsBounds.y,
                                           TAB_THICKNESS,
                                           contentsBounds.height); 
        leftTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        prototypeItem = leftTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFont());
        addTabItem(leftTabs, "AB", Color.red);
        addTabItem(leftTabs, "CD", Color.magenta);
        addTabItem(leftTabs, "EF", Color.cyan);
        addTabItem(leftTabs, "GH", Color.yellow);
        addTabItem(leftTabs, "IJ", Color.green);
        addTabItem(leftTabs, "KL", Color.red);
        addTabItem(leftTabs, "MN", Color.magenta);
        addTabItem(leftTabs, "OP", Color.cyan);
        leftTabs.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        cv.addSubview(leftTabs);
                
        // create bottom tabs
        SLTabView bottomTabs = new SLTabView(contentsBounds.x,
                                             contentsBounds.maxY() - bezelMargin,
                                             contentsBounds.width,
                                             TAB_THICKNESS,
                                             ECTabView.BELOW_CONTENT);
        bottomTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        prototypeItem = bottomTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFont());
        addTabItem(bottomTabs, "Once", Color.red);
        addTabItem(bottomTabs, "Upon", Color.magenta);
        addTabItem(bottomTabs, "A", Color.cyan);
        addTabItem(bottomTabs, "Midnight", Color.yellow);
        addTabItem(bottomTabs, "Dreary", Color.green);
        bottomTabs.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        bottomTabs.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        cv.addSubview(bottomTabs);
                
        window.show();
    }
    
    private static void addTabItem(ECTabView tabView,
                                   String title, 
                                   Color color) {
        ECTabItem item = tabView.addItem();
        item.setTitle(title);
        item.setTitleColor(color);
    }
    

    private static void addFontSamples(TextView tv, String s, String fontName) {
        for (int i = 0; i < FontSizes.length; i += 1) {
            addStringInFont(tv, s, Font.fontNamed(
                fontName, Font.BOLD, FontSizes[i].intValue()));
        }
    }

    private static void addStringInFont(TextView tv, String s, Font f) {
        String namedSample = f.name() + " " + f.size() + " (bold)\n" + s;
        Range newRange = tv.appendString(namedSample);
        tv.addAttributeForRange(TextView.FONT_KEY, f, newRange);
    }

    private static void createBorderedView(Rect r, Border border, String title, View superview) {
        ContainerView cv = new ContainerView();
        cv.setBounds(r);
        cv.setTitle(title);
        cv.setBorder(border);
        cv.setTransparent(true);
        superview.addSubview(cv);
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command))
            System.exit(0);
        else if (commandUseBlues.equals(command))
            changeColorScheme(StoneLook.STANDARD_COLORS);
        else if (commandUsePurples.equals(command))
            changeColorScheme(StoneLook.ALTERNATE_COLORS);
        else if (commandToggleCloseBox.equals(command))
            myWindow.setHasCloseBox(!myWindow.getHasCloseBox());
        else if (commandToggleTitle.equals(command)) {
            if (myWindow.getTitleImage() == null)
                myWindow.setTitleImage(StoneLook.standardWindowTitleImage());
            else
                myWindow.setTitleImage(null);
        }
    }

    /** change the color scheme of a set of objects */
    private void changeColorScheme(int colorScheme) {
        int count = chameleons.size();
        for (int i = 0; i < count; i += 1) {
            HasColorScheme chameleon = (HasColorScheme)chameleons.elementAt(i);
            chameleon.setColorScheme(colorScheme);
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
}

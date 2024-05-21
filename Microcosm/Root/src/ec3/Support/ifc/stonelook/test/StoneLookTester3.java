/**
 * StoneLookTester.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by John Sullivan
 *
 * This is test code for the marble-controls-on-sandstone look.
 */
package ec.ifc.stonelook.test;

import java.io.File;
import java.io.IOException;

import netscape.application.Application;
import netscape.application.BezelBorder;
import netscape.application.Bitmap;
import netscape.application.Button;
import netscape.application.Border;
import netscape.application.Color;
import netscape.application.ContainerView;
import netscape.application.Rect;
import netscape.application.Scrollable;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.View;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.cert.CryptoHash;

import ec.ifc.app.ECApplication;
import ec.ifc.app.ECBitmap;
import ec.ifc.app.ECImageView;
import ec.ifc.app.ECSlider;
import ec.ifc.app.ECTabItem;
import ec.ifc.app.ECTabView;

import ec.ifc.stonelook.SLBezelBorder;
import ec.ifc.stonelook.SLButton;
import ec.ifc.stonelook.SLContainerView;
import ec.ifc.stonelook.SLSlider;
import ec.ifc.stonelook.SLTabView;
import ec.ifc.stonelook.SLWindow;
import ec.ifc.stonelook.StoneLook;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.start.crew.CrewCapabilities;
import ec.e.file.EStdio;
import ec.e.rep.steward.SuperRepository;

import ec.util.NestedException;

/**
 * To run this test, type to a shell:
 * java ec.e.start.EBoot ec.ifc.stonelook.test.Tester3
 */
eclass Tester3 implements ELaunchable {
    emethod go(EEnvironment env) {
        if (env.args().length > 0 && env.args()[0].equals("o")) {
            // if there's an overrides file lying around, use it
            StoneLook.applyOverridesFromFile(new File("SLOverrides.txt"));          
        }       

        StoneLookTester3 tester = new StoneLookTester3(env);
        tester <- go();
    }
}

eclass StoneLookTester3 {
    private EEnvironment myEnv;

    public StoneLookTester3(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {

        Vat vat = myEnv.vat();

        try {
            Object repository = CrewCapabilities.createCrewRepository(myEnv.props());
            CrewCapabilities.setCrewRepository(repository);
            StoneLook.setRepository(repository);

            Application application = new ECApplication();
            Window3 tester = new Window3();
            application.run();
        } 
        catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}

class Window3 implements Target, WindowOwner  {
    /** window full of assorted UI elements */
    private SLWindow     myWindow;
    private SLTabView    myLeftTabs;
    private SLTabView    myBottomTabs;
    private ECSlider     myLeftSlider;
    private ECSlider     myBottomSlider;
    private ECImageView  myImageView1;
    private ECImageView  myImageView2;
    private ECImageView  myImageView3;
    private ECImageView  myImageView4;      
    
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
    private static final String commandDragLeftSlider = "draq left slider";
    private static final String commandDragBottomSlider = "drag bottom slider";
    private static final String commandLeftTabSelected = "select left tab";
    private static final String commandBottomTabSelected = "select bottom tab";

    public Window3 ()  {
        int tabWindowContentWidth = 260;
        int tabWindowContentHeight = 275;
        
        SLWindow window = new SLWindow(100, 30, 
            tabWindowContentWidth, tabWindowContentHeight);
        window.setOwner(this);
        window.setResizable(true);
        ContainerView cv = window.getContainerView();
        cv.setBackgroundColor(StoneLook.darkBackgroundColor());

        Rect bounds = cv.bounds();
        
        int borderType = BezelBorder.RAISED;

        // create container view that the tabs are placed around, to
        // simulate the appearance that tabs would have in a real context
        ContainerView tabContents = new SLContainerView(borderType);

        // use our inside knowledge that all SLBezel margins are the
        // same to keep this simpler
        int bezelMargin = tabContents.border().rightMargin();

        Rect contentsBounds = new Rect(     
            MARGIN + TAB_THICKNESS - bezelMargin, 
            MARGIN + TAB_THICKNESS - bezelMargin, 
            tabWindowContentWidth - 2*(MARGIN + TAB_THICKNESS - bezelMargin),
            tabWindowContentHeight - 2*(MARGIN + TAB_THICKNESS - bezelMargin));

        tabContents.setBounds(contentsBounds);
        tabContents.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        tabContents.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        cv.addSubview(tabContents);
        
        // create right tabs
        SLTabView rightTabs = new SLTabView(contentsBounds.maxX() - bezelMargin, 
                                            contentsBounds.y + bezelMargin,
                                            TAB_THICKNESS, contentsBounds.height - 2*bezelMargin, 
                                            ECTabView.RIGHT_OF_CONTENT);
        // need to set it to false if you want to scroll it, it is set to true by default
        rightTabs.setAutomaticTabLength(false);
        rightTabs.setMinTabLength(50);
        rightTabs.setSpacing(5);

        ECTabItem prototypeItem = rightTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFontBold());
        addTabItem(rightTabs, "Yappy");
        addTabItem(rightTabs, "Spunky");
        addTabItem(rightTabs, "Cocoa-nut");
        addTabItem(rightTabs, "Max");
        addTabItem(rightTabs, "Mityaha");
        addTabItem(rightTabs, "Alex");
        addTabItem(rightTabs, "Elen");
        addTabItem(rightTabs, "John");
        rightTabs.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        rightTabs.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
        cv.addSubview(rightTabs);
       
        // create top tabs
        SLTabView topTabs = new SLTabView(contentsBounds.x + bezelMargin, 
                                          contentsBounds.y + bezelMargin - TAB_THICKNESS,
                                          contentsBounds.width - 2*bezelMargin, 
                                          TAB_THICKNESS, 
                                          ECTabView.ABOVE_CONTENT);
        // need to set it to false if you want to scroll it, it is set to true by default
        topTabs.setAutomaticTabLength(false);
        topTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        topTabs.setMinTabLength(40);

        prototypeItem = topTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFontBold());
        addTabItem(topTabs, "Aye-Aye");
        addTabItem(topTabs, "Indri");
        addTabItem(topTabs, "Sifaka");
        addTabItem(topTabs, "Mouse lemur");
        addTabItem(topTabs, "Ring-tailed lemur");
        topTabs.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        cv.addSubview(topTabs);
                
        // create left tabs
        // LEFT_OF_CONTENT is default location, so the location isn't passed as argument
        myLeftTabs = new SLTabView(contentsBounds.x + bezelMargin - TAB_THICKNESS,
                                           contentsBounds.y + bezelMargin,
                                           TAB_THICKNESS,
                                           contentsBounds.height - 2*bezelMargin); 
        myLeftTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        prototypeItem = myLeftTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFontBold());
        addTabItem(myLeftTabs, "AB");
        addTabItem(myLeftTabs, "CD");
        addTabItem(myLeftTabs, "EF");
        addTabItem(myLeftTabs, "GH");
        addTabItem(myLeftTabs, "IJ");
        addTabItem(myLeftTabs, "KL");
        addTabItem(myLeftTabs, "MN");
        addTabItem(myLeftTabs, "OP");
        myLeftTabs.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
        myLeftTabs.setTarget(this);
        myLeftTabs.setCommand(commandLeftTabSelected);
        cv.addSubview(myLeftTabs);
                
        // create bottom tabs
        myBottomTabs = new SLTabView(contentsBounds.x + bezelMargin,
                                             contentsBounds.maxY() - bezelMargin,
                                             contentsBounds.width - 2*bezelMargin,
                                             TAB_THICKNESS,
                                             ECTabView.BELOW_CONTENT);
        myBottomTabs.setSelectionStyle(SLTabView.RAISED_SELECTION);
        prototypeItem = myBottomTabs.prototypeItem();
        prototypeItem.setFont(StoneLook.standardFontBold());
        addTabItem(myBottomTabs, "Once");
        addTabItem(myBottomTabs, "Upon");
        addTabItem(myBottomTabs, "A");
        addTabItem(myBottomTabs, "Midnight");
        addTabItem(myBottomTabs, "Dreary");
        myBottomTabs.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        myBottomTabs.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
        myBottomTabs.setTarget(this);
        myBottomTabs.setCommand(commandBottomTabSelected);
        cv.addSubview(myBottomTabs);

        myLeftSlider = new SLSlider(
            contentsBounds.x + 10,
            contentsBounds.y + 30,
            contentsBounds.height - contentsBounds.y - 30,
            Scrollable.VERTICAL);
        myLeftSlider.setTarget(this);
        myLeftSlider.setCommand(commandDragLeftSlider);
        myLeftSlider.setLimits(0, myLeftTabs.count());
        cv.addSubview(myLeftSlider);

        myBottomSlider = new SLSlider(
            contentsBounds.x + 30,
            contentsBounds.y + contentsBounds.height - 30,
            contentsBounds.width - contentsBounds.x - 30,
            Scrollable.HORIZONTAL);
        myBottomSlider.setTarget(this);
        myBottomSlider.setCommand(commandDragBottomSlider);
        myBottomSlider.setLimits(0, myBottomTabs.count());
        cv.addSubview(myBottomSlider);


        // This tests the BmpLoader
        SuperRepository theRep = (SuperRepository)CrewCapabilities.getTheSuperRepository();

        try {
            // The image is 45 x 31, which tests the 4-byte padding requirements
            // in the BMP format

            Object cryptoKey = theRep.getCryptoHash("gui/main_win/strips/btn_text_selected.bmp");
            // Tony, this case isn't showing on the screen!
            if (cryptoKey != null) {
                byte[] imageBytes = (byte[])theRep.get((CryptoHash)cryptoKey);

                myImageView1 = new ECImageView(ECBitmap.bitmapFromByteArray(imageBytes),
                                               contentsBounds.x + 40,
                                               contentsBounds.y + 10);
                cv.addSubview(myImageView1);
            }

            // 8-bit, RLE encoded BMP
            cryptoKey = theRep.getCryptoHash("gui/main_win/borders/top_strip.bmp");
            if (cryptoKey != null) {
                byte[] imageBytes = (byte[])theRep.get((CryptoHash)cryptoKey);

                myImageView2 = new ECImageView(ECBitmap.bitmapFromByteArray(imageBytes),
                                               contentsBounds.x + 40,
                                               contentsBounds.y + 40);
                cv.addSubview(myImageView2);
            }

            /* Tony, this case isn't working: here is the spam:
<=======               
Woof. IOException.
java.io.EOFException
        at ec.misc.LittleEndianInputStream.readUnsignedByte(LittleEndianInputStre
am.java:67)
        at ec.misc.graphics.BmpLoader.input(BmpLoader.java:171)
        at ec.ifc.app.ECBitmap.bitmapFromByteArray(ECBitmap.java:42)
        at ec.ifc.stonelook.test.Window3.<init>(StoneLookTester3.java:307)
========>
            // 8-bit, RGB encoded BMP
            cryptoKey = theRep.getCryptoHash("gui/main_win/strips/jump_unselected.bmp");
            if (cryptoKey != null) {
                byte[] imageBytes = (byte[])theRep.get((CryptoHash)cryptoKey);

                myImageView3 = new ECImageView(ECBitmap.bitmapFromByteArray(imageBytes),
                                               contentsBounds.x + 40,
                                               contentsBounds.y + 80);
                cv.addSubview(myImageView3);
            }
            */
            cryptoKey = theRep.getCryptoHash("gui/main_win/buttons/map_selected.bmp");
            if (cryptoKey != null) {
                byte[] imageBytes = (byte[])theRep.get((CryptoHash)cryptoKey);

                myImageView4 = new ECImageView(ECBitmap.bitmapFromByteArray(imageBytes),
                                               contentsBounds.x + 40,
                                               contentsBounds.y + 120);
                cv.addSubview(myImageView4);
            }
        } catch (IOException iox) {
            throw new NestedException("Crew access to in-vat SuperRepository", iox);
        }

        window.show();
    }

    private static void addTabItem(ECTabView tabView,
                                   String title) {
        ECTabItem item = tabView.addItem();
        item.setTitle(title);
    }
    

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command)) {
            System.exit(0);
        }
        else if (commandToggleCloseBox.equals(command)) {
            myWindow.setHasCloseBox(!myWindow.getHasCloseBox());
        }
        else if (commandDragLeftSlider.equals(command)) {
            myLeftTabs.setSelectedIndex(myLeftSlider.value());
        }
        else if (commandDragBottomSlider.equals(command)) {
            myBottomTabs.setSelectedIndex(myBottomSlider.value());
        }
        else if(commandLeftTabSelected.equals(command)) {
            myLeftSlider.setValue(
                myLeftSlider.minValue() + (myLeftSlider.maxValue() - myLeftSlider.minValue()) *
                myLeftTabs.selectedIndex() / (myLeftTabs.count() - 1)
                );
        }
        else if(commandBottomTabSelected.equals(command)) {
            myBottomSlider.setValue(
                myBottomSlider.minValue() + (myBottomSlider.maxValue() - myBottomSlider.minValue()) * 
                myBottomTabs.selectedIndex() / (myBottomTabs.count() - 1)
                );
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

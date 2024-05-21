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
import java.util.Vector;

import netscape.application.Application;
import netscape.application.ContainerView;
import netscape.application.Font;
import netscape.application.Range;
import netscape.application.Rect;
import netscape.application.ScrollGroup;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.TextView;
import netscape.application.View;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.ifc.stonelook.SLPageTurner;
import ec.ifc.stonelook.SLScrollGroup;
import ec.ifc.stonelook.SLTextView;
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

/**
 * To run this test, type to a shell:
 * java ec.e.start.EBoot ec.ifc.stonelook.test.Tester2
 */
eclass Tester2 implements ELaunchable {
    emethod go(EEnvironment env) {
        if (env.args().length > 0 && env.args()[0].equals("o")) {
            // if there's an overrides file lying around, use it
            StoneLook.applyOverridesFromFile(new File("SLOverrides.txt"));          
        }       

        StoneLookTester2 tester = new StoneLookTester2(env);
        tester <- go();
    }
}

eclass StoneLookTester2 {
    private EEnvironment myEnv;

    public StoneLookTester2(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {

        Vat vat = myEnv.vat();

        try {
            Object repository = CrewCapabilities.createCrewRepository(myEnv.props());
            CrewCapabilities.setCrewRepository(repository);
            StoneLook.setRepository(repository);

            Application application = new ECApplication();
            Window2 tester = new Window2();
            application.run();
        } 
        catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}

class Window2 implements Target, WindowOwner  {
    /** window full of assorted UI elements */
    private SLWindow myWindow;

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
    private static final String commandUseBlues = "use blues";
    private static final String commandUsePurples = "use purples";

    private static Integer FontSizes[] = {
        new Integer(9), new Integer(10), new Integer(12),
        new Integer(14), new Integer(18), new Integer(24)
    };

    public Window2 ()  {
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

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command))
            System.exit(0);
        else if (commandToggleCloseBox.equals(command))
            myWindow.setHasCloseBox(!myWindow.getHasCloseBox());
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

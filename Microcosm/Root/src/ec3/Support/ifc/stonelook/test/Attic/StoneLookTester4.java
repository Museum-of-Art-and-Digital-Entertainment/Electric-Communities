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

import java.net.URL;

import netscape.application.Application;
import netscape.application.Color;
import netscape.application.ContainerView;
import netscape.application.Rect;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.View;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.ifc.stonelook.SLWindow;
import ec.ifc.stonelook.StoneLook;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.net.ByteArrayFactory;
import ec.e.rep.crew.CrewRepository;
import ec.e.rep.StandardRepository;

/**
 * To run this test, type to a shell:
 * java ec.e.start.EBoot ec.ifc.stonelook.test.Tester4
 */
eclass Tester4 implements ELaunchable {
    emethod go(EEnvironment env) {
        URL.setURLStreamHandlerFactory(new ByteArrayFactory());

        StoneLookTester4 tester = new StoneLookTester4(env);
        tester <- go();
    }
}

eclass StoneLookTester4 {
    private EEnvironment myEnv;

    public StoneLookTester4(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {

        Vat vat = myEnv.vat();

        try {
            StandardRepository.summon(myEnv); // As a side effect, makes CrewRepository available

            Application application = new Application();
            Window4 tester = new Window4();
            application.run();
        } 
        catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}

class Window4 implements Target, WindowOwner  {
    /** window full of assorted UI elements */
    private SLWindow myWindow;

    // command constants
    private static final String commandQuitApplication = "quit application";

    public Window4 ()  {
        int tabWindowContentWidth = 260;
        int tabWindowContentHeight = 275;
        
        SLWindow window = new SLWindow(100, 30, 
            tabWindowContentWidth, tabWindowContentHeight);
        window.setOwner(this);
        window.setResizable(true);
        ContainerView cv = window.getContainerView();

        Rect bounds = cv.bounds();
        
        window.show();
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        if (commandQuitApplication.equals(command)) {
            System.exit(0);
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

/**
 * CheckpointReviveTester.java
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * written by Sidney Markowitz
 *
 * This is a standalone test app for Checkpoint and Revival.
 */
package ec.e.inspect;

import java.io.File;
import java.util.Vector;

import java.io.IOException;

import netscape.application.Application;
import netscape.application.ContainerView;
import netscape.application.Button;
import netscape.application.Graphics;
import netscape.application.TextField;
import netscape.application.Rect;
import netscape.application.Size;
import netscape.application.Target;
import netscape.application.View;
import netscape.application.ExternalWindow;
import netscape.application.Window;
import netscape.application.WindowOwner;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.e.rep.StandardRepository;
import ec.util.NestedException;
import ec.e.quake.StableStore;


/**
 * To run this test, type to a shell:
 * java ec.e.start.EBoot ec.e.inspect.CheckpointReviveTester filename
 */
eclass CheckpointReviveTester implements ELaunchable {
    emethod go(EEnvironment env) {
        StoneLookTester tester = new StoneLookTester(env);
        tester <- go();
    }
}

eclass StoneLookTester {
    private EEnvironment myEnv;
    private StableStore myStore;

    public StoneLookTester(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {

        String args[] = myEnv.args();

        String filename = null;
        if (args.length < 1) {
            filename = myEnv.getProperty("checkpoint");
        }
        else {
            filename = args[0];
        }

        String passphrase = null;
        if (args.length < 2) {
            passphrase = myEnv.getProperty("passphrase");
        }
        else {
            passphrase = args[1];
        }
        if (filename == null) {
            throw new IllegalArgumentException
                ("usage: java ec.e.start.EBoot ec.e.inspect.CheckpointReviveTester filename");
        }
        EStdio.out().println("loading from " + filename + " with passphrase <<" + passphrase + ">>");

        StableStore checkstore = new StableStore(filename, passphrase);

        try {
            Application application = new Application();
            TestWindow tester = new TestWindow(checkstore, passphrase);
            application.run();
            
        } 
        catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw:" + e);
            e.printStackTrace(EStdio.err());
        }
    }
}

class TestWindow implements Target, WindowOwner  {

    private StableStore myStore;
    private Vat myVat = null;
    private String passphrase = null;

    private ExternalWindow myWindow;

    // command constants
    private static final String commandQuitApplication = "quit application";
    private static final String commandCheckpoint = "Checkpoint";
    private static final String commandRevive = "Revive";
    private static final String commandNewPass = "NewPassPhrase";

    public TestWindow (StableStore checkstore, String startpassphrase)  {
        myStore = checkstore;
        passphrase = startpassphrase;

        ContainerView cv = new ContainerView(new Rect(0,0,200,130));
        myWindow = new ExternalWindow();
        Size s = myWindow.windowSizeForContentSize(cv.width(), cv.height());
        myWindow.sizeTo(s.width,s.height);
        myWindow.addSubview(cv);
        myWindow.moveTo(200, 350);
        myWindow.setOwner(this);
        myWindow.setResizable(false);
        myWindow.setTitle("Test Me");
        //Rect bounds = cv.bounds();
        
        Button checkButton = new Button(10, 10, 80, 30);
        checkButton.setCommand(commandCheckpoint);
        checkButton.setTarget(this);
        checkButton.setTitle("Checkpoint");
        cv.addSubview(checkButton);
 
        Button reviveButton = new Button(110, 10, 80, 30);
        reviveButton.setCommand(commandRevive);
        reviveButton.setTarget(this);
        reviveButton.setTitle("Revive");
        cv.addSubview(reviveButton);
 
        // Make a TextField which the user can edit for password.

        TextField textField = new TextField(10, 50, 180, 30);
        textField.setCommand(commandNewPass);
        textField.setTarget(this);
        textField.setStringValue(passphrase);
        textField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        textField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        cv.addSubview(textField);


        TextField labelField = new TextField(10, 80, 180, 30);
        labelField.setStringValue("Type new password then Enter");
        labelField.setJustification(Graphics.CENTERED);
        labelField.setBorder(null);
        labelField.setEditable(false);
        labelField.setBackgroundColor(cv.backgroundColor());
        labelField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
        labelField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
        cv.addSubview(labelField);


        myWindow.show();
    }
    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
        //          EStdio.out().println("Command is " + command);
        if (commandQuitApplication.equals(command))
            System.exit(0);
        else if (command.equals(commandCheckpoint))
            Checkpoint();
        else if (command.equals(commandRevive))
            Revive();
        else if (command.equals(commandNewPass))
            NewPassWasEntered(arg);
    }

    // Save the vat last revived into
    private void Checkpoint() {
        if (myVat != null) {
            EStdio.out().println("Checkpoint");
            myWindow.setTitle("Checkpointing...");
            try {
                myStore.save(myVat);
            }
            catch (IOException e) {
                throw new NestedException("error in storing checkpoint file", e);
            }
            myWindow.setTitle("Test Me");
        }
    }
 
    // throw away the previously revived Vat and make another
    private void Revive() {
        EStdio.out().println("Revive");
        myWindow.setTitle("Reviving...");
        try {
            myVat = (Vat)myStore.restore();
        }
        catch (IOException e) {
            throw new NestedException("error in loading checkpoint file", e);
        }
        myWindow.setTitle("Test Me");
    }

    /** This method is invoked when the user hits return in the TextField.
      */
    public void NewPassWasEntered(Object textField) {
        passphrase = ((TextField)textField).stringValue();
        myStore.setPassphrase(passphrase);
        EStdio.out().println("Passphrase will be '" + passphrase + "' after next checkpoint");
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

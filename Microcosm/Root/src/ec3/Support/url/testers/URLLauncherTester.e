package ec.url.tester;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;
import ec.url.steward.*;
import ec.e.file.EStdio;

import ec.url.crew.*;

public eclass URLLauncherTester implements ELaunchable {
    EStdio       eio;
    EEnvironment myEnv;
    TimeMachine  myTM;
    
    public URLLauncherTester() {
        /*
        */
    }

    emethod go(EEnvironment env) {

        myEnv = env;

        try {
            eio.initialize(myEnv.vat());
        } catch (Exception e) {
        }

        try {
            myTM = TimeMachine.summon(myEnv);
        } catch (Exception e) {
            eio.out().println("Cant summon a timeMachine : " + e.getMessage());
        }

        myTM <- nextQuake(new QuakeReporter());

        this <- start();
    }
    
    emethod start() {
        TestGuest               theGuest = null;
        URLLauncherMaker        theURLLauncherMaker = null;
        URLLauncherSteward      theURLLauncherSteward = null;


        try {
            theURLLauncherMaker = URLLauncherMaker.summon(myEnv);
        } catch (Exception e) {
            eio.out().println("Cant summon the URLLauncherMaker : " +
                              e.getMessage());
        }

        theURLLauncherSteward = theURLLauncherMaker.makeURLLauncherSteward();
        
        theGuest = new TestGuest(theURLLauncherSteward);

        // theGuest.hereIs(theURLLauncherSteward);

        theGuest <- doYourThing();

        eio.out().println("hibernating ?");

        myTM <- quakeDrill(null);  // will exit here
        myTM <- hibernate(null,0);  // will exit here

        theGuest <- doYourOtherThing();
    }
}



public eclass TestGuest {
    URLLauncherSteward myURLLauncherSteward = null;

    public TestGuest() {}

    /** get the URLLauncherSteward on construction
     *
     */
    public TestGuest(URLLauncherSteward aURLLauncherSteward) {
        myURLLauncherSteward = aURLLauncherSteward;
    }

    emethod doYourThing() {
        /* Cant do this 
         *   URLLauncher  ul = new URLLauncher();
         *   ul.nativeOpenURL("http://www.test.com");
         */

        myURLLauncherSteward.openURL("http://www.communities.com");
    }

    emethod doYourOtherThing() {

        myURLLauncherSteward.openURL("http://www-int.communities.com");
    }

    /*
    public void cantDoThis() {
        Vat avat = null; // some value
        URLLauncherSteward foo = new URLLauncherSteward(avat);

        foo.openURL("http://www.test.org");
    }
    */
}

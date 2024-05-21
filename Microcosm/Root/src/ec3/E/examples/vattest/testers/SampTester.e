package ec.samp.tester;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;
import ec.e.file.EStdio;

import ec.samp.compute.TestResults;
import ec.samp.compute.ETestResults;
import ec.samp.steward.*;
import ec.samp.crew.*;

public eclass SampTester implements ELaunchable {
    EStdio       eio;
    EEnvironment myEnv;
    TimeMachine  myTM;
    
    public SampTester() {
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
        SampMaker               theSampMaker = null;
        SampSeismoSteward       theSampSeismoSteward = null;

        try {
            theSampMaker = SampMaker.summon(myEnv);
        } catch (Exception e) {
            eio.out().println("Cant summon the SampMaker : " +
                              e.getMessage());
        }

        theSampSeismoSteward = theSampMaker.makeSampSteward();
        
        theGuest = new TestGuest(theSampSeismoSteward, myTM);

        theGuest <- doYourThing();

        eio.out().println("hibernating ?");

        theGuest <- doYourOtherThing();
    }
}

public eclass TestGuest {
    SampSeismoSteward   mySampSteward = null;
    EStdio              eio;
    TimeMachine         myTM;

    public TestGuest() { }

    /** get the SampSteward on construction
     *
     */
    public TestGuest(SampSeismoSteward aSampSteward, TimeMachine tm) {
        myTM = tm;
        mySampSteward = aSampSteward;
    }

    emethod doYourThing() {
        ETestResults myEResult;

        mySampSteward <- doSamp("Doing my thing", &myEResult);

        myTM <- hibernate(null,0);  // will exit here
                                    // "gnumake revive" restores to here
                                    // note: this causes the requests
                                    // to be run in reverse order

        ewhen myEResult (TestResults myTestResult) {
            
            eio.out().println("did my thing .. result is -> " +
                              myTestResult);
        }
    }

    emethod doYourOtherThing() {
        ETestResults myEResult;

        mySampSteward <- doSamp("Doing my OTHER thing", &myEResult);
        
        ewhen myEResult (TestResults myTestResult) {
            
            eio.out().println("did my OTHER thing .. result -> " +
                              myTestResult);
        }
    }
}


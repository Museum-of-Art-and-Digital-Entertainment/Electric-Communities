package ec.samp.steward;

import java.lang.Thread;

import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;
import ec.e.start.Vat;
import ec.e.start.SmashedException;

import ec.samp.steward.SampCrewHolderInt;

import ec.samp.crew.Samp;
import ec.samp.crew.SampCrewThread;

public eclass SampSeismoSteward implements Seismologist, SampCrewHolderInt {

    private Tether /* SampCrewThread */ mySampCrewThreadTether;
    private Vat    myVat;
    
    public SampSeismoSteward(Vat vat) {
        myVat = vat;
        reconstruct(vat);
    }

    /**
     * noticeQuake() -- A quake has occured, we are being revived
     */
    emethod noticeQuake(TimeQuake quake) {
        System.out.println("SampSeismoSteward: quake " + quake);
        quake.waitForNext(this);
    }

    /**
     * noticeCommit() -- 
     */
    emethod noticeCommit() {
        System.err.println("SampSeismoSteward: committed");
    }

    emethod doSamp(String someTestVal, EResult guestResultHandler) {

        // Create a resulthandler to actually do the request.
        // The resulthandler gets a reference to this as a SampCrewHolderInt,
        // which allows the resulthandler to getCrew (get the crew thread),
        // which is needed to actually send the request.

        SampResultHandler resultHandler = new SampResultHandler(myVat, 
                                                    guestResultHandler,
                                                    someTestVal,
                                                    (SampCrewHolderInt)this);

    }

    local SampCrewThread getCrew() {
        SampCrewThread mySampCrewThread = null;

        try {
            mySampCrewThread = (SampCrewThread) mySampCrewThreadTether.held();
        } catch (SmashedException se) {
            System.out.println("SampSeismo .. Got smashed, reconstructing" + 
                               se.getMessage());

            mySampCrewThread = reconstruct(mySampCrewThreadTether.vat());
        }

        return mySampCrewThread;
    }

    private SampCrewThread reconstruct(Vat vat) {
        SampCrewThread sampCrewThread;
        Samp           theSamp;

        theSamp = new Samp("My Samp");

        // note: the actual crew thread start should be in the crew.
        sampCrewThread = new SampCrewThread(theSamp);
        mySampCrewThreadTether = new Tether(vat, sampCrewThread);
        new Thread(sampCrewThread).start();
        
        return sampCrewThread;
    }    
}

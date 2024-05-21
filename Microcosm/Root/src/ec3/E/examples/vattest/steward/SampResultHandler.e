package ec.samp.steward;

import ec.samp.compute.TestResults;
import ec.samp.compute.ETestResults;

import ec.e.start.Vat;
import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;

import ec.samp.crew.SampCrewThread;

public eclass SampResultHandler implements Seismologist, SampResultHandlerInt {
    
    Vat               myVat;
    EResult      myResultWaiter = null;
    String            myRequest;
    SampCrewHolderInt myCrewHolder;

    public SampResultHandler(Vat               vat, 
                             EResult      resultWaiter,
                             String            request,
                             SampCrewHolderInt crewHolder) {
        myVat          = vat;
        myResultWaiter = resultWaiter;
        myRequest      = request;
        myCrewHolder   = crewHolder;
        
        doRequest();

    }

    private void doRequest() {
        SampCrewThread crewThread = myCrewHolder.getCrew();

        System.out.println("SampResultHandler making a fragile root for itself");

        Tether resultRootHolder = myVat.makeFragileRoot((Seismologist)this);

        crewThread.performSampRequest(myRequest, this);
    }

    emethod handleResults(TestResults testResults) {
        myResultWaiter <- forward (new ETestResults(testResults));
    }


    emethod noticeQuake(TimeQuake quake) {
        System.out.println("resultHandler noticed quake... resending request");
        
        doRequest();
    }
    
    emethod noticeCommit() {
    }

}

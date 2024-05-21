package ec.samp.crew;

import ec.e.start.Tether;
import ec.e.start.SmashedException;

import ec.samp.compute.TestResults;
import ec.samp.compute.ETestResults;


public class Samp  {
    private String myStateVal;

    public Samp(String initialVal) {
        
        myStateVal = initialVal;
    }

    public void doCrewSamp(SampRequestItem sampRequest) {

        TestResults  testResult;
        
        System.out.println("Samp is doing its thing! " + 
                           sampRequest.myRequest);

        System.out.println("Samp is now sending the reply! ");

        testResult = new TestResults("Results: you said " + 
                                    sampRequest.myRequest);

        // call the steward.. as a callback.
        sampRequest.myResultHandler <- handleResults(testResult);
        
    }
}



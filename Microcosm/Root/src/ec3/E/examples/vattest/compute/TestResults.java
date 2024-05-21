package ec.samp.compute;

/**
 * Sample "results" from the crew
 */

public class TestResults {

    private String resultVal;
    
    public TestResults(String val) {
        resultVal = val;
    }
    
    public String toString() {
        return resultVal;
    }
}

/**
 * And E wrapper for a TestResults.  Allows the receiver
 * of the results to "ewhen anETestResults(aTestResults)"
 * to actually get the results.
 */

public eclass ETestResults {

    private TestResults myTestResults;

    public ETestResults(TestResults testResult) {
        myTestResults = testResult;
    }
    
    TestResults value() {
        return myTestResults;
    }
}



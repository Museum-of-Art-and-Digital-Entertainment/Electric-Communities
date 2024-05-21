package ec.tests.java.svm;

import java.svm.*;

/**
 * Currently, an extremely trivial test of the quakeDrill mechanism
 */
public class Shaker {

    static public void main(String[] args) {
	System.err.println("foo\n");
	System.err.println(System.getProperty("seismology"));
	
        TimeMachine.theOne().commit();
	System.err.println(TimeMachine.theOne().quakeDrill(args));
    }
}


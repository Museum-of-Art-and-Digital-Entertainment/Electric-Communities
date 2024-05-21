/* 
   Counter.e
   v 0.1 
   A simple counting computation to be made persistent
   Feb 26, 1997
   Mark S. Miller Copyright Electric Communities
   */


package ec.examples.cou;

import ec.e.cap.EEnvironment;
import ec.e.start.ELaunchable;


public class Counter implements ELaunchable {
    
    public void go(EEnvironment env) {
        Counter1 first = new Counter1(env);
        first <- start();
    }
}

eclass Counter1 {
    private EEnvironment myEEnv;
    private TimeMachine myTM;
    private int myCount;

    public Counter1(EEnvironment eEnv) {
        myEEnv = eEnv;
        myTM = (TimeMachine)myEEnv.get("timeMachine");
        myTM <- nextQuake(new QuakeReporter());
    }

    emethod start() {
        myCount = 0;
        this <- step();
    }

    emethod step() {
        ++myCount;
        if (myCount % 1000 == 0) {
            System.err.println("count == " + myCount);
            if (myCount == 5000) {
                myTM <- quakeDrill(null);
            }
            if (myCount == 6000) {
                myTM <- hibernate(null, 0);
            }
        }
        if (myCount <= 10000) {
            this <- step();
            this <- step();
        }
    }
}


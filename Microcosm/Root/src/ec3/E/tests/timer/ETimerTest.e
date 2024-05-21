/* 
    ETimerTest.e
    v 0.1
    RtTimer services test
    Gordie Freedman Copyright Electric Communities
    Proprietary and Confidential
*/


package ec.tests.timer;

import ec.e.run.EBoolean;
import ec.e.timer.RtTimer;
import ec.e.lang.EInteger;

public class ETimerTest
{
    public static void main(String args[])
    {
        Object cancel = null;
        ETimerTester et = new ETimerTester();
        //RtRun.tr.traceMode(true);
        if (args.length > 0) {
            System.out.println("Will cancel 3rd timer");
            cancel = new Object();
        }
        et <- phoneHome(cancel);
    }
}

eclass ETimerTester 
{
    static final long waitTime1 = (long)1000;
    static final long waitTime2 = (long)2000;
    static final long waitTime3 = (long)3000;
    static final long waitTime4 = (long)4000;
    emethod phoneHome (Object cancel)
    {
        EBoolean key1;
        EBoolean key2;
        EBoolean key3;
        EBoolean key4;
        EInteger num = new EInteger(1);
        EResult key1d = &key1;
        EResult key2d = &key2;
        EResult key3d = &key3;
        EResult key4d = &key4;
        int tid1;
        int tid2;
        int tid3;
        int tid4;
        int counter = 0;
        RtTimer timer = new RtTimer(false);
        //RtEnvelope env;
        //RtEnvelope env2 = ETimerTest::handleTimeout(timer, 1234);

        //env = ETimerTester::handleTimeout(timer, 1234);
        //this <- env;

        System.out.println("Started up, setting timers at " + System.currentTimeMillis()); 

        tid4 = timer.setTimeout(waitTime4, key4d);
        tid2 = timer.setTimeout(waitTime2, key2d);
        tid3 = timer.setTimeout(waitTime3, key3d);
        tid1 = timer.setTimeout(waitTime1, key1d);

        //timer <- setTimeoutWithEnvelope(waitTime1, this, env);
        //timer <- setTimeoutWithEnvelope(waitTime1, this, ETimerTester::handleTimeout(null, 0));

        //timer <- cancelTimeout(key1d);

        ewhen num (int i) {
            System.out.println("Beat the time at " + System.currentTimeMillis());
        }       
        eorwhen key1 (boolean b) {
            System.out.println("First timer hit at " + System.currentTimeMillis());
        }
        
        eif (key2) {
            System.out.println("Second timer hit at " + System.currentTimeMillis());
            if (cancel != null) {
                timer.cancelTimeout(tid3);
            }
        }
        
        eif (key3) {
            if (cancel != null) {
                System.out.println("*** Error, third timer hit, but it should have been cancelled!");
            }
            else {
                System.out.println("Third timer hit at " + System.currentTimeMillis());
            }
        }
        
        ewhen key4 (boolean b) {
            System.out.println("Final timer hit at " + System.currentTimeMillis() + " (" + counter + ")");
            timer.terminate();
        }
    }

    emethod handleTimeout (RtTimer timer, int num) {
        System.out.println("Timer callback called through envelope on " + timer + " with num " + num + " at " + System.currentTimeMillis());
    }
}


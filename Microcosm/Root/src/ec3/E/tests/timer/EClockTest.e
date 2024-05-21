/* 
    EClockTest.e
    v 0.1
    EClock services test
    Gordie Freedman Copyright Electric Communities
    Proprietary and Confidential
*/


package ec.tests.timer;

import ec.e.lang.EInteger;
import ec.e.timer.RtClock;

public class EClockTest
{
    public static void main(String args[])
    {
        Object cancel = null;
        EClockTester et = new EClockTester();
        et <- phoneHome();
    }
}

eclass EClockTester 
{
    emethod phoneHome ()
    {
        EInteger key;
        
        RtClock clock = new RtClock((long)1000, &key);
        System.out.println("Starting clock at " +
            System.currentTimeMillis());
        clock.start();
                        
        ewhenever key (int tick) {
            if (tick > 5)
                clock.terminate();
                //RtClockTerminator.terminateAllClocks();
            else
                System.out.println("Clock tick " + tick + " at " +
                    System.currentTimeMillis());
        }       
    }
}


/* 
    ETimerTest2.e
    v 0.1
    RtTimer services test
    Gordie Freedman Copyright Electric Communities
    Proprietary and Confidential
*/


package ec.tests.timer;

import ec.e.timer.RtTimer;
import ec.e.lang.EInteger;
import ec.e.run.EBoolean;

public class ETimerTest2
{
    public static void main(String args[])
    {
        ETimerTester et = new ETimerTester2();
        et <- phoneHome();
    }
}

eclass ETimerTester2 
{
    static final Integer waitTime1 = new Integer(200000);
    static final Integer waitTime2 = new Integer(3000);
    emethod phoneHome ()
    {
        EBoolean key1;
        EBoolean key2;
        EInteger num = new EInteger(1);
        //EInteger num; 
        EResult key1d = &key1;
        EResult key2d = &key2;
        
        RtTimer timer = new RtTimer(true);

        //timer <- setTimeout(waitTime1, key1d);
                
        ewhen num (int i) {
            //timer <- terminateTimer(); 
            timer <- setTimeout(waitTime2, key2d);
            System.out.println("Beat the timer!");
        }   
        /*eorwhen key1 (boolean b) {
            //timer <- terminateTimer(); 
            //timer <- setTimeout(waitTime2, key2d);
            System.out.println("First timer hit!");
        }*/

        eif (key2) {
            System.out.println("Second timer hit!");
            timer <- terminateTimer();
        }
    }
}


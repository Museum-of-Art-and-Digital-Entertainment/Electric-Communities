
package ec.tests.timer;

import java.lang.*;
import ec.e.timer.RtTimeoutHandling;
import ec.e.timer.RtTimer;

interface Foo {
    void foo ();
}

interface Bar extends Foo {
    void bar ();
}

class TimerTest implements RtTimeoutHandling
{
    static final long timeToWait = 2000L;
    public static void main (String args[]) {
        TimerTest test = new TimerTest();
        RtTimer timer = new RtTimer();
        int tid;
        Object obj = new Object();
        Class theClass = obj.getClass();
        System.out.println("The class for Object is: " + theClass);

        tid = timer.setTimeout(timeToWait, test, timer);
        System.out.println("Waiting for " + timer + " in " + timeToWait + " millis with id " + tid);
    }

    public void handleTimeout (Object arg, int tid)
    {
        RtTimer timer = (RtTimer)arg;
        System.out.println("Timer fired for " + timer + " id " + tid);
        timer.terminate();
    }
}


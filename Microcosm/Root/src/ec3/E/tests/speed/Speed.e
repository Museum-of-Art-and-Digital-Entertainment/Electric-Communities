// Run like this: java ec.tests.speed.Go [0-5] n
// where n is the number of times to perform the test; it gives decent
// results with n ~>= 10000 for the E tests and n ~>= 100000 for the
// Java tests.

package ec.tests.speed;

import ec.e.lang.ELong;

eclass ETest
{
    emethod blip ()
    {
    }

    emethod blip (int x)
    {
    }

    emethod blip (Object o)
    {
    }
}

class Test
{
    void blip ()
    {
    }

    void blip (int x)
    {
    }

    void blip (Object o)
    {
    }
}

class RunNothing
implements Runnable
{
    public void run ()
    {
    }
}

class RunE0
implements Runnable
{
    static ETest theObj = new ETest ();
    public void run () { theObj <- blip (); }
}

class RunE1
implements Runnable
{
    static ETest theObj = new ETest ();
    public void run () { theObj <- blip (1); }
}

class RunE2
implements Runnable
{
    static ETest theObj = new ETest ();
    public void run () { theObj <- blip (theObj); }
}

class RunJ0
implements Runnable
{
    static Test theObj = new Test ();
    public void run () { theObj.blip (); }
}

class RunJ1
implements Runnable
{
    static Test theObj = new Test ();
    public void run () { theObj.blip (1); }
}

class RunJ2
implements Runnable
{
    static Test theObj = new Test ();
    public void run () { theObj.blip (theObj); }
}

eclass Tester
{
    emethod doTest (Runnable r, int count, EResult result)
    {
        long startAt = System.currentTimeMillis ();
        while (count > 0)
        {
            r.run ();
            count--;
        }
        this <- sendResult (startAt, result);
    }

    emethod sendResult (long startTime, EResult result)
    {
        long total = System.currentTimeMillis() - startTime;
        result <- forward (new ELong (total));
    }

    emethod doOneForReal (int which, int count, long overhead)
    {
        ELong result;
        Runnable r = null;
        switch (which)
        {
            case 0: r = new RunE0 (); break;
            case 1: r = new RunE1 (); break;
            case 2: r = new RunE2 (); break;
            case 3: r = new RunJ0 (); break;
            case 4: r = new RunJ1 (); break;
            case 5: r = new RunJ2 (); break;
        }
        doTest (r, count, &result);
        ewhen result (long l)
        {
            l -= overhead;
            double rate = count / ((double) l) * 1000;
            System.out.println (count + " times; " + l + " msec");
            System.out.println (rate + " per sec");
        }
    }

    emethod doOne (int which, int count)
    {
        ELong result;
        doTest (new RunNothing (), count, &result);
        ewhen result (long overhead)
        {
            System.out.println ("overhead is " + overhead);
            doOneForReal (which, count, overhead);
        }
    }
}

public class Go
{
    static public void main (String[] args)
    {
        RtRun.bootCheat ();
        int which = Integer.parseInt (args[0]);
        int count = Integer.parseInt (args[1]);
        Tester t = new Tester ();
        t <- doOne (which, count);
    }
}
/*
 * Kid's Walkie-Talkie Example
 *
 * There are several avatars fighting for the two ends of the Canophone
 * through which they can communicate.
 *
 * Dan Bornstein
 *
 */

package ec.examples.canophone;

import java.util.Random;
import java.util.Hashtable;
import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;

public eclass ECanophone
{
    //static final ENull enull = new ENull ();
    private ETalkitar holder1 = null;
    private ETalkitar holder2 = null;
    private ECan can1 = null;
    private ECan can2 = null;
    
    emethod grab (ETalkitar wanter, String wanterName, EResult canD)
    {
        if (holder1 == null)
        {
            System.out.println ("*** " + wanterName + " gets can 1.");
            holder1 = wanter;
            can1 = new ECan (this, wanterName);
            canD <- forward (can1);
        }
        else if (holder2 == null)
        {
            System.out.println ("*** " + wanterName + " gets can 2.");
            holder2 = wanter;
            can2 = new ECan (this, wanterName);
            canD <- forward (can2);
        }
        else
        {
            canD <- forward (enull);
        }
    }

    // "private" emethod; you should only drop cans directly    
    emethod canGotDrop (ECan can, String name)
    {
        if (can == can1)
        {
            can1 = null;
            holder1 = null;
        }
        else if (can == can2)
        {
            can2 = null;
            holder2 = null;
        }
        else {
            System.out.println("Error dropping unknown can for " + name);
        }
        System.gc();
    }

    // "private" emethod; you should only talk into cans directly
    emethod canGotTalk (ECan source, String talkerName, String s)
    {
        if (source == can1)
        {
            if (holder2 != null)
            {
                holder2 <- hear (talkerName, s);
            }
        }
        else if (source == can2)
        {
            if (holder1 != null)
            {
                holder1 <- hear (talkerName, s);
            }
        }
    }   
}

public eclass ECan
{
    private ECanophone canophone;
    private String holderName;

    private static int canCount = 6;

    ECan (ECanophone canophone, String holderName)
    {
        this.canophone = canophone;
        this.holderName = holderName;
    }
    
    Object value ()
    {
        return (this);
    }

    emethod drop ()
    {
        System.out.println ("--- " + holderName + " drops the can.");
        canophone <- canGotDrop (this, holderName);
        canophone = null;
        //if ((--canCount) <= 0) System.exit(0);
    }
    
    emethod talk (String s)
    {
        System.out.println (holderName + " says: " + s);
        canophone <- canGotTalk (this, holderName, s);
    }
}

public eclass ETalkitar
{
    String name;
    ECan can = null;
    private static int talkerCount = 0;

    ETalkitar (String name)
    {
        talkerCount++;
        this.name = name;
    }

    protected void finalize () {
        System.out.println("Talkitar " + name + " being finalized, count is " + talkerCount);
        if (--talkerCount <= 0) timer.terminate();
    }

    emethod hear (String talkerName, String s)
    {
        System.out.println (name + " hears " + talkerName + " say: " + s);
        // wait a little to keep to a human pace
        eif (waitFor (2000))
        {       
            if (can != null)
            {
                if (s.equals ("Hello."))
                {
                    can <- talk ("Whassup?");
                }
                else if (s.equals ("Whassup?"))
                {
                    can <- talk ("Not much.");
                }
                else if (s.equals ("Not much."))
                {
                    can <- talk ("Same here.");
                }
            }
        }
    }
    
    emethod play (ECanophone canophone)
    {
        ECan canC;
        canophone <- grab (this, name, &canC);
        ewhen canC (Object canObject)
        {
            if (canObject == null)
            {
                // didn't get a can; wait a bit then try again
                eif (waitFor (randomTime () + 10000))
                {
                    System.out.println (name + " is waiting for a can.");
                    this <- play (canophone);
                }
            }
            else
            {
                can = (ECan) canObject;
                // got a can; say something and hang out and listen
                can <- talk ("Hello.");
                // (XXX)
                eif (waitFor (randomTime () + 10000))
                {
                    // we've now hung out long enough
                    can <- drop ();
                    can = null;
                }
                //
            }
        }
    }

    private static Random rand = new Random (); 
    private static int randomTime ()
    {
        return (rand.nextInt () % 10000);
    }
    
    private static RtTimer timer = new RtTimer (CanophoneDemo.debugFlag);
    private static EBoolean waitFor (int time)
    {
        EBoolean ping;
        //if (time < 0) time = -time;
        timer.setTimeout (time, &ping);
        return (ping);
    }
}

public class CanophoneDemo implements ELaunchable
{
    public static boolean debugFlag = false;

    public void go(EEnvironment env)
    {
        debugFlag = env.getPropertyAsBoolean("debug");

        ECanophone canophone = new ECanophone ();

        ETalkitar t1 = new ETalkitar ("Horgie Hooterbee");
        ETalkitar t2 = new ETalkitar ("Felixaloola");
        //
        ETalkitar t3 = new ETalkitar ("Karltone");
        ETalkitar t4 = new ETalkitar ("Dave 'The Wave' Krieger");
        ETalkitar t5 = new ETalkitar ("Whacker");
        ETalkitar t6 = new ETalkitar ("Shemkel");
        //

        t1 <- play (canophone);
        t2 <- play (canophone);
        //
        t3 <- play (canophone);
        t4 <- play (canophone);
        t5 <- play (canophone);
        t6 <- play (canophone);
        //
    }
}

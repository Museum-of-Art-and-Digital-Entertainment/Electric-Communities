/* 
    Die rolling over comm example
    v 2.0
    Jan 25, 1996
    Arturo Bejar & Gordie Freedman
    Copyright 1996 Electric Communities, all rights reserved.
        
    You may specify server=true or url=e://... for client
    (Specifying neither defaults to both in same process)
*/  

package ec.examples.drc;

import ec.e.lang.EInteger;
import java.util.Hashtable;
import java.util.Random;
import ec.e.start.ELaunchable;
import ec.e.net.ERegistrar;
import ec.e.net.ERegistration;
import ec.e.net.RtDirectoryEException;
import ec.e.cap.EEnvironment;
import ec.e.cap.ERestrictedException;
import ec.e.net.ERegistrationException;
import ec.e.net.EInvalidUrlException;

public class DieRollComm implements ELaunchable
{
    static boolean server = false;
    static boolean client = false;
    
    public void go (EEnvironment env) {
        ERegistration roller_reg;
        ERegistrar reg;
        DieRoller roller;
        String url;

        server = env.getPropertyAsBoolean("server");
        url = env.getProperty("url");
        if (url != null) client = true;

        if (!server && !client) {
            server = true;
            client = true;
        }

        reg = (ERegistrar)env.get("registrar.root");

        try {
            if (server) {
                reg.startup(0);
                roller = new DieRoller(reg, "server");
                roller_reg = reg.register(roller);
                url = roller_reg.getURL() ;
                System.out.println("Starting server at url=" + url);
            }
            if (client) {
                System.out.println("Connecting to server at url: " + url);
                roller = new DieRoller(reg, "client");
                roller <- rollClient(url);
            }
        } catch (ERestrictedException e) {
            e.printStackTrace();
        } catch (ERegistrationException e) {
            e.printStackTrace();
        }
    }
}

public eclass DieRoller 
{
    String name;
    boolean do_shutdown = false;
    Random random = new Random();
    
    // Each Die Roller hands distributors to these to it's
    // peer, and waits on the values using ewhen
    EInteger otherKey;
    EInteger otherIntermediate;
    
    // Keep track of the other roller so we can shut it down when we're done
    DieRoller otherGuy = null;

    ERegistrar myReg;
    
    DieRoller (ERegistrar reg, String which) {
        myReg = reg;
        name = which;
    }

    emethod rollClient (String url) {
        DieRoller theOtherGuy;        
        EInteger myKey;
        EInteger myResult;

        do_shutdown = true;

        etry {
            try {
                myReg.lookupURL(url, &theOtherGuy);
                theOtherGuy <- startRoll (this);
                this <- startRoll (theOtherGuy);
            } catch (ERestrictedException e) {
                e.printStackTrace();
            } catch (EInvalidUrlException e) {
                e.printStackTrace();
            }
        } ecatch (RtDirectoryEException e) {
            System.out.println("DieRoller caught exception on lookup: " + e.getMessage());
        }
    }
    
    emethod startRoll (DieRoller theOtherGuy) {
        otherGuy = theOtherGuy;
        otherGuy <- dieRoll (&otherIntermediate, &otherKey);
    }
    
    emethod dieRoll (EResult intermediateD, EResult keyD) { 
        // Our half of the die roll 
        int myX = getRandomPrimeValue(random, 0);
        // Used to hide our half
        int actualKey = getRandomPrimeValue(random, myX);
    
        System.out.println(name + ": My X is " + myX + " and key is " + actualKey);//XXX PT
    
        // Each side can determine the other side's half of the roll
        // from the intermediate result and the random key that was
        // used to hide that half of the roll. We "hide" our half in
        // an intermediate result using a random key, and send it over.
        // The other side does the same, so each side only knows a little
        // about the other side when the ewhen fires.
        intermediateD <- forward( new EInteger( Fn( myX, actualKey ) ) );
        ewhen otherIntermediate(int intermediate) {
            System.out.println(name + ": Other's intermediate result = " + intermediate);//XXX PT
        
            // Now that we have an intermediate result from the other side, 
            // we can tell it what we hid our half with, and wait for the other
            // side to do the same for us.
            keyD <- forward( new EInteger( actualKey ) );
            ewhen otherKey(int key) {
                int otherX;
                int finalResult;
        
                // We can derive their half of the roll from
                // the key they just sent, so we combine it with
                // our half of the roll to get the final result.
                otherX = FnInv ( intermediate, key );
                finalResult = Combine( myX, otherX );
                System.out.println(name + ": Other's X is " + otherX);//XXX PT
                System.out.println(name + ": Roll result = " + finalResult);
                if (do_shutdown)
                    otherGuy <- shutdownComSystem();
            }       
        }       
    }
    
    emethod shutdownComSystem () {
System.out.println(name + " got shutdown");
        try {
            myReg.shutdown();
        } catch (ERestrictedException e) {
            e.printStackTrace();
        }
    }
    
    static int Fn (int a, int b) {
        int altb = (a < b) ? 1 : 0;
        return ( (a * b) + altb ); 
    }
    
    static int FnInv (int prod, int b) {
        boolean altb = false;
        int np = prod;
        int a;
        if ((prod % 2) == 0) {
            np--;
            altb = true;
        }
        a = np / b;
        if ((altb && (a > b)) ||
            (!altb && (a < b))) {
                System.out.println("Hey - the other side lied! " + a + " " + b + " " + prod);
            }
        return a;
    }
    
    static int Combine (int a, int b) {
        return ( ( ((a>>1) ^ (b>>1)) % 6 ) + 1 );
    }
    
    static int getRandomPrimeValue (Random random, int notThis) {
        int attempt = random.nextInt();
        if (attempt < 0) attempt = -attempt;
        attempt = (attempt % 9999);
        if ((attempt % 2) == 0) attempt++;
        if (attempt == notThis) {
            System.out.println("Have to recurse, attempt is " + attempt + " notThis is " + notThis);//XXX PT
            return getRandomPrimeValue(random, notThis);
        }
        else {
            return attempt;
        }
    }
}

/* 
   Counter4.e
   Mark S. Miller Copyright Electric Communities
   */


package ec.examples.cou;

import ec.e.file.EStdio;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;
import ec.util.NestedException;
import java.util.Hashtable;

/**
 * A Variant of Counter1 whose purpose is to demonstrate the Hashtable
 * circularity bug, or its absence.
 */
public eclass Counter4 implements ELaunchable {
    private EEnvironment myEEnv;
    private TimeMachine myTM;

    private HashKeyObj myKey = new HashKeyObj();

    public Counter4() {}
    
    emethod go(EEnvironment eEnv) {
        try {
            myTM = TimeMachine.summon(eEnv);

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new NestedException("can't start", ex);
        }
        myEEnv = eEnv;
        myTM <- nextQuake(new QuakeReporter());
        this <- start(0);
    }
    
    emethod start(long n) {
        if (n % 100 == 0) {
            EStdio.err().println("n == " + n);
            if (n == 500) {
                myTM <- quakeDrill(null);
            }
            if (n % 200 == 0) {
                myTM <- hibernate(null, 0);
            }
        }
        if (n <= 1000) {
            this <- start(n+1);
        }
    }
}


/**
 * This object holds onto an otherwise-unreachable Hashtable that
 * points back at it.  This object's hash depends on a field that
 * comes after the circular pointer to the Hashtable.  The bug
 * manifests on revival, when the Hashtable is reconstructed before
 * 'myRealKey' is filled in, leading to hashCode failing with a
 * NullPointerException. 
 */
/*package*/ class HashKeyObj {
    /*
     * We have to declare the instance variable in the reverse of the
     * intended order, since ecomp reverses layout order on us.
     */
    private Object myRealKey;
    private Hashtable myTable;

    /*package*/HashKeyObj() {
        myTable = new Hashtable();
        myRealKey = new Object();
        myTable.put(this, this);
    }

    public int hashCode() {
        int hash = myRealKey.hashCode() * 10;
        return hash;
    }
}

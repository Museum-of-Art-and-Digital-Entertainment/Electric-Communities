/* 
   CounterLong.e
   v 0.1 
   A simple counting computation to be made persistent
   Feb 26, 1997
   Mark S. Miller Copyright Electric Communities
   */


package ec.examples.cou;

import ec.e.file.EStdio;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;


public eclass CounterLong implements ELaunchable {
    private EEnvironment myEEnv;
    private TimeMachine myTM;

    public CounterLong() {}
    
    emethod go(EEnvironment eEnv) {
        try {
            EStdio.initialize(eEnv.vat());
            myTM = TimeMachine.summon(eEnv);
        } catch (Exception ex) {
            throw new RuntimeException("can't start: " + ex);
        }
        myEEnv = eEnv;
        myTM <- nextQuake(new QuakeReporter());
        this <- start(0);
    }
    
    emethod start(long n) {
        if (n % 1000000000000L == 0) {
            EStdio.err().println("n == " + n);
            if (n == 5000000000000L) {
                myTM <- quakeDrill(null);
            }
            if (n == 6000000000000L) {
                myTM <- hibernate(null, 0);
            }
        }
        if (n <= 10000000000000L) {
            this <- start(n+10000000000L);
        }
    }
}


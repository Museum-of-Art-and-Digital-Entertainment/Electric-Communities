/* 
   Counter3.e
   v 0.1 
   A simple counting computation to be made persistent
   Feb 26, 1997
   Mark S. Miller Copyright Electric Communities
   Variation by Chip
   */


package ec.examples.cou;

import ec.e.file.EStdio;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;


public eclass Counter3 implements ELaunchable {
    private EEnvironment myEEnv;
    private TimeMachine myTM;
    private int myCount;

    public Counter3() {}
    
    emethod go(EEnvironment eEnv) {
        try {
            myTM = TimeMachine.summon(eEnv);
        } catch (Exception ex) {
            throw new RuntimeException("can't start: " + ex);
        }
        myEEnv = eEnv;
        myTM <- nextQuake(new QuakeReporter());
        this <- start(1000);
    }

    emethod start(int n) {
        myCount = 0;
        for (int i=0; i<n; ++i)
            this <- step();
    }

    emethod step() {
        ++myCount;
        if (myCount % 100 == 0) {
            EStdio.err().println("count == " + myCount);
            if (myCount == 500) {
                myTM <- quakeDrill(null);
            }
            if (myCount == 600) {
                myTM <- hibernate(null, 0);
            }
        }
    }
}



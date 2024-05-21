/* 
   Counter1.e
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
import ec.util.NestedException;


public eclass Counter1 implements ELaunchable {
    private EEnvironment myEEnv;
    private TimeMachine myTM;

    public Counter1() {}
    
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


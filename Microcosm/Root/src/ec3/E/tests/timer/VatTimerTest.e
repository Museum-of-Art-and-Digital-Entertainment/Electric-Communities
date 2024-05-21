package ec.tests.timer;

// By Gordie. Copyright 1997 Electric Communitites.

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency (the EEnvironment)
import ec.e.timer.Timer;
import ec.e.timer.RtTimer;
import ec.e.timer.RtTimeoutHandling;
import ec.e.quake.TimeMachine;

import ec.e.file.EStdio;
 
/* To easily run this code, you may want to define
alias vtt 'java -debug ec.e.start.EBoot ec.tests.timer.VatTimerTest checkpoint=foo'
alias vtr 'java -debug ec.e.quake.Revive foo.evat'
*/

eclass VatTimerTest implements ELaunchable, RtTimeoutHandling
{
    private Timer quakeTimer;
    private Timer smashTimer;
    private TimeMachine timeMachine;
    private int cancelId;
    private RtTimer timer;
    
    emethod go (EEnvironment env) {
        quakeTimer = Timer.TheQuakeProofTimer();
        smashTimer = Timer.TheSmashingTimer();
        
        timer = new RtTimer();
        timer.setTimeout(1000, this, "Timer1");
        cancelId = timer.setTimeout(3000, this, "Timer2");
        timer.setTimeout(4000, this, "Timer3");
        
        try {
            timeMachine = (TimeMachine)env.magicPower("ec.e.quake.crew.TimeMachineMaker");      
        } catch (Exception e) {
            EStdio.reportException(e);
            env.vat().exit(-1);
        }
                            
        EBoolean first;
        quakeTimer.setTimeout(5000, etrue, &first);         
        timeMachine <- commit(null);
        this <- report("Initialized"); // Ain't POE great?      
        ewhen first (boolean ignored) {
            EStdio.out().println("First timer went off, setting other timers");
            setOtherTimers(env);
        }
    }
    
    emethod report(String s)  {
        EStdio.out().println(s);        
    }   
    
    emethod setOtherTimers(EEnvironment env)  {
        int id2;
        EBoolean quake1;
        quakeTimer.setTimeout(30000, etrue, &quake1);
        ewhen quake1 (boolean ignored) {
            EStdio.out().println("Quake Timeout 1 should happen after revival");
            timeMachine <- commit(null);
            this <- report("Checkpointed after quake timer 1"); // Ain't POE great? 
            quakeTimer.cancelTimeout(id2);  
        }
        EBoolean quake2;
        id2 = quakeTimer.setTimeout(35000, etrue, &quake2);
        ewhen quake2 (boolean ignored) {
            EStdio.out().println("*** ERROR! Quake Timeout 2 should have been cancelled!");
            timeMachine <- commit(null);
            this <- report("Checkpointed after quake timer 2"); // Ain't POE great?         
        }
        EBoolean quake3;
        quakeTimer.setTimeout(40000, etrue, &quake3);
        ewhen quake3 (boolean ignored) {
            EStdio.out().println("Quake Timeout 3 should happen after revival");
            timeMachine <- commit(null);
            this <- report("Checkpointed after quake timer 3"); // Ain't POE great? 
            VatTimerTest loser;
            loser <- report("Smashing timer after Quake 3 went off");
            smashTimer.setTimeout(5000, this, &loser);          
        }
        
        EBoolean smash1;
        smashTimer.setTimeout(31000, etrue, &smash1);
        ewhen smash1 (boolean ignored) {
            EStdio.out().println("*** ERROR! Smash Timeout 1 should NOT happen after revival");
        }
        EBoolean smash2;
        smashTimer.setTimeout(36000, etrue, &smash2);
        ewhen smash2 (boolean ignored) {
            EStdio.out().println("*** ERROR! Smash Timeout 2 should NOT happen after revival");
        }
        timeMachine <- commit(null);
        report("Committed after setting all timers");
    }
    
    local void handleTimeout(Object arg, int id)  {
        EStdio.out().println("Timer for arg " + arg + " id " + id);
        if (cancelId > 0)  {
            EStdio.out().println("Cancelling RtTimer 2");
            timer.cancelTimeout(cancelId);
            cancelId = 0;
        }
    }
    
}

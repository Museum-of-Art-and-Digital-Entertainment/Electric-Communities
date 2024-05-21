package ec.tests.timer;

// By Gordie. Copyright 1997 Electric Communitites.

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency (the EEnvironment)
import ec.e.timer.Clock;
import ec.e.timer.ClockController;
import ec.e.timer.ETickHandling;
import ec.e.timer.RtClock;
import ec.e.timer.RtTickHandling;
import ec.e.quake.TimeMachine;

import ec.e.file.EStdio;
 
/* To easily run this code, you may want to define
alias vtt 'java -debug ec.e.start.EBoot ec.tests.timer.VatClockTest checkpoint=bar'
alias vtr 'java -debug ec.e.quake.Revive bar.evat'
*/

eclass VatClockTest implements ELaunchable, RtTickHandling, ETickHandling
{
    private ClockController quakeClockController;
    private ClockController smashClockController;
    private TimeMachine timeMachine;
    private int cancelId;
    private Clock quakeClock1;
    private Clock quakeClock2;
    private Clock quakeClock3;
    private Clock smashClock1;
    private Clock smashClock2;
    private Clock smashClock3;
    
    emethod go (EEnvironment env) {
        quakeClockController = ClockController.TheQuakeProofClockController();
        smashClockController = ClockController.TheSmashingClockController();
        try {
            timeMachine = TimeMachine.summon(env);
        } catch (Exception e) {
            EStdio.reportException(e);
            env.vat().exit(-1);
        }

        RtClock clock = new RtClock(1000, this, null);
        clock.start();                          
    }
    
    emethod report(String s)  {
        EStdio.out().println(s);
    }
    
    emethod handleTick(int tick, Clock clock, Object arg)  {
        Object[] args = (Object[])arg;
        String name = (String)args[0];
        int max = ((Integer)args[1]).intValue();
        EStdio.out().println(name + ": Tick " + tick);      
        if (tick > max)  {
            EStdio.out().println("Terminating " + name);
            clock.terminate();
        }
        timeMachine <- commit(null);
        report("Committed after tick");
    }   
    
    emethod setOtherClocks ()  {
        Object[] quakeArgs1 = { "Quake clock 1", new Integer(10) };
        quakeClock1 = quakeClockController.newClock(5000, this, quakeArgs1);
        quakeClock1.start();
        
        Object[] smashArgs1 = { "Smash clock 1", new Integer(15) };
        smashClock1 = smashClockController.newClock(7500, this, smashArgs1);
        smashClock1.start();
        
        timeMachine <- commit(null);
        report("Committed after setting all clocks");
    }
    
    local void tick(Object arg, int tick)  {
        EStdio.out().println("RtClock " + arg + " tick " + tick);
        if (tick > 3)  {
            RtClock clock = (RtClock)arg;
            clock.terminate();
            this <- setOtherClocks();
        }
    }   
}

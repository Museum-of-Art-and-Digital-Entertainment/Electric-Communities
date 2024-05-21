
package ec.tests.ae;

import ec.util.NestedError;
import ec.e.file.EStdio;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.start.Seismologist;
import ec.e.quake.TimeMachine;
import ec.e.start.TimeQuake;
import java.io.IOException;

public eclass EncryptedCheckpoint implements ELaunchable, Seismologist
{
    EncryptedCheckpoint chan;
    
    emethod go(EEnvironment env) {
        TimeMachine tm;
        try {
            tm = TimeMachine.summon(env);
        }
        catch (Exception e) {
            throw new NestedError("couldn't make a TimeMachine", e);
        }

        String passphrase = env.getProperty("Passphrase");
        if (passphrase != null) {
            EStdio.out().println("setting timemachine passphrase to <<" + passphrase + ">>");
            tm <- setPassphrase(passphrase);
        }
        
        tm <- hibernate(this, 0);
            
        chan <- gotIt();
    }

    emethod noticeQuake(TimeQuake q) {
        EStdio.out().println("quaking in my EBoots: " + q);
    }

    emethod noticeCommit() {
        &chan <- forward(this);
    }

    emethod gotIt() {
        EStdio.out().println("Got it!");
    }
}

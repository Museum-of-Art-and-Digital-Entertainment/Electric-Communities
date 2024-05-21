package ec.e.io;

/* These classes are STEWARD */

import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;
import ec.e.start.Vat;
import ec.e.start.Tether;
import ec.e.io.crew.RtConsole;

public class ConsoleTest implements ELaunchable {
    public void go(EEnvironment env) {
        ConsoleTester tester = new ConsoleTester(env);
        tester <- go();
    }
}

eclass ConsoleTester implements EInputHandler {
    private EEnvironment myEnv;
    private boolean doneFlag;

    public ConsoleTester(EEnvironment env) {
        myEnv = env;
        doneFlag = false;
    }

    emethod handleInput (String line) {
        if (line == null)
            doneFlag = true;
        else
            System.out.println("You said: '" + line + "'");
        
    }

    emethod contemplateNavel(int count) {
        if (count % 1000 == 0) {
            System.out.println("count has reached " + count);
            Thread.yield();
        }
        if (!doneFlag)
            this <- contemplateNavel(count + 1);
    }

    emethod go() {
        Vat vat = (Vat)myEnv.get("vat");
        Tether handlerHolder = new Tether(vat, (Object) this);
        RtConsole.setupConsoleReader(handlerHolder, System.in, null);

        this <- contemplateNavel(0);
    }
}

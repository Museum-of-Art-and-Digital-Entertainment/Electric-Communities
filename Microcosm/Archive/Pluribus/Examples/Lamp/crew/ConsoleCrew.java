package ec.pl.examples.lamp;

import ec.e.io.crew.RtConsole;
import ec.e.io.EInputHandler;
import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.pl.runtime.UIFramework;
import ec.pl.runtime.UIFrameworkOwner;

public class ConsoleLampFrameworkOwner implements UIFrameworkOwner
{  
    public void run() {
        synchronized (this) {
            try {
                wait(0);
            } catch (Exception e) {
                System.out.println("Console woke up, exiting!");
            }
        }
    }

    public UIFramework framework (EEnvironment env) {
        LampFramework framework = (LampFramework)innerFramework(env);
        return new LampFrameworkSteward(env, framework);
    }
    
    public Object innerFramework (EEnvironment env) {
        return new ConsoleLampFramework(env);
    }
    
    public void initFramework()  {
        
    }
}


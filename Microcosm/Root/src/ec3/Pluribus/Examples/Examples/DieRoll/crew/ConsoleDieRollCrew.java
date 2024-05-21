package ec.pl.examples.dieroll;

import ec.e.start.EEnvironment;
import ec.pl.runtime.UIFramework;
import ec.pl.runtime.UIFrameworkOwner;

public class ConsoleDieRollFactory implements UIFrameworkOwner
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
        DieRollFramework framework = (DieRollFramework)innerFramework(env);
        return new DieRollFrameworkSteward(env, framework);
    }
    
    public Object innerFramework (EEnvironment env) {
        return new ConsoleDieRollFramework(env);
    }
    
    public void initFramework()  {
        
    }
}

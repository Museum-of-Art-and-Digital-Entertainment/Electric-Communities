package ec.pl.examples.web;

import ec.e.start.EEnvironment;
import ec.pl.runtime.UIFramework;
import ec.pl.runtime.UIFrameworkOwner;

public class ConsoleWebFactory implements UIFrameworkOwner
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
        WebFramework framework = (WebFramework)innerFramework(env);
        return new WebFrameworkSteward(env, framework);
    }

    public Object innerFramework (EEnvironment env) {
        return new ConsoleWebFramework(env);
    }
    
    public void initFramework()  {
        
    }
}

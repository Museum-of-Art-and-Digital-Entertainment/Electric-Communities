package ec.pl.runtime;

import ec.e.start.MagicPowerMaker;
import java.lang.String;
import java.lang.Thread;
import ec.e.start.EEnvironment;

public class MagicUIPowerMaker implements MagicPowerMaker {

    public Object make (EEnvironment env) {
        String frameworkClassName = null;
        Class frameworkClass;
        UIFramework framework = null;

        // Find the Application framework
        try {
            frameworkClassName = env.getProperty("UI");
            frameworkClass = Class.forName(frameworkClassName);
            framework = (UIFramework)frameworkClass.newInstance();
            framework.setEnvironment(env);
        } catch (Exception e) {
            /* XXX bad exception handling -- fix */
            if (frameworkClassName != null) {
                System.out.println("Can't get UI Framework");
                e.printStackTrace();
            }
            framework = null;
        }
        if (framework != null) {
            new MagicUIThread(framework);
        }
        return framework;
    }
}

class MagicUIThread extends Thread
{
    private UIFramework framework;
    
    MagicUIThread (UIFramework framework) {
        this.framework = framework;
        ////this.setDaemon(true); - Program didn't run very long with this !!!
        this.start();
    }
    
    public void run () {
        framework.run();
    }
}

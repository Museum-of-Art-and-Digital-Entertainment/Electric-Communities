package ec.pl.runtime;

import ec.vcache.ClassCache;
import ec.util.NestedException;
import ec.util.Native;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.file.EStdio;
import ec.e.run.Trace;
import java.lang.String;
import java.lang.Thread;

public class MagicUIPowerMaker implements MagicPowerMaker {
    private static Trace tr_timer = new Trace("StartupTimer");

    public Object make (EEnvironment env) {
        UIFramework framework = null;
        UIFrameworkOwner owner = null;

        owner = getFrameworkOwner(env);

        // XXX - The UIFramework *MUST* be a Steward!
        framework = owner.framework();
        runFramework(owner);
        return framework;
    }

    public void runFramework (UIFrameworkOwner owner) {
        new MagicUIThread(owner);
        long start = 0;
        if(tr_timer.debug && Trace.ON) {
           tr_timer.debugm("!!Initing Framework ... ");
           start = Native.queryTimer();
        }
        //this is a test
        owner.initFramework();
        if(tr_timer.debug && Trace.ON) tr_timer.debugm("   !!Time to init framework: "
            + Native.deltaTimerMSec(start) + " msec.");
    }

    public UIFrameworkOwner getFrameworkOwner (EEnvironment env) {
        String frameworkClassName = null;
        Class frameworkClass;
        UIFrameworkOwner owner = null;
        // Find the UI framework
        // XXX - The UIFrameworkOwner *MUST* be Crew!
        try {
            frameworkClassName = env.getProperty("UI");
            frameworkClass = ClassCache.forName(frameworkClassName);
            owner = (UIFrameworkOwner)frameworkClass.newInstance();
        } catch (Exception e) {
            throw new MagicUIPowerException("Cannot establish UI Framework", e);
        }
        return owner;
    }
}

class MagicUIThread extends Thread
{
    private UIFrameworkOwner owner;

    MagicUIThread (UIFrameworkOwner owner) {
        this.owner = owner;
        this.start();
    }

    public void run () {
        owner.run();
    }
}

class MagicUIPowerException extends NestedException
{
    MagicUIPowerException (String message, Throwable t) {
        super(message, t);
    }
}

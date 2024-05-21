package ec.pl.examples.lamp;
import ec.pl.runtime.MagicUIPowerMaker;
import ec.pl.runtime.UIFramework;
import ec.pl.runtime.UIFrameworkOwner;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import ec.e.start.Syncologist;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import ec.e.run.Trace;

class LampFrameworkSteward implements UIFramework, LampFramework, Syncologist
{
    private Tether /* DieRollFramework */ tether;
    private EEnvironment env;
    
    public LampController getLampController() {
        LampController controller = new LampControllerSteward(env, this);
        return controller;
    }
    
    LampFrameworkSteward (EEnvironment env, LampFramework framework) {
        this.env = env;
        this.tether = new Tether(env.vat(), framework);
        env.vat().setSyncQuakeNoticer(this);
    }
    
    public void noticeQuakeSync(TimeQuake quake) {
        MagicUIPowerMaker maker = new MagicUIPowerMaker();
        UIFrameworkOwner owner = maker.getFrameworkOwner(env);
        if (owner == null) {
            // XXX - Raise
            throw new RuntimeException ("Can't reestablish UI Framework");
        }
        LampFramework framework = (LampFramework)owner.innerFramework(env);
        tether = new Tether(env.vat(), framework);
        maker.runFramework(owner);
        env.vat().setSyncQuakeNoticer(this);
    }
    
    LampFramework getCrew() {
        LampFramework framework = null;
        try {
            framework = (LampFramework)tether.held();
        } catch (SmashedException e) {
            System.out.println("Invariant violated - LampFramework Tether's framework smashed");
            e.printStackTrace();
            throw new Error("Invariant violated - LampFramework Tether's framework smashed:" + e);
        }
        return framework;
    }

    public void initializeApplicationUI() {
    }
}

class LampControllerSteward implements LampController {
    private EEnvironment env;
    private Tether /* DieRollController */ tether;
    private LampPeer peer;
    private LampFrameworkSteward frameworkSteward;
    private boolean hostState;
    private boolean lampState;
    
    public void postEvent (int eventType, boolean state) {
        LampController controller = getCrew();
        controller.postEvent(eventType, state);
        lampState = state;
    }

    public void setPeer (LampPeer peer, boolean hostState, boolean lampState) {
        this.hostState = hostState;
        this.lampState = lampState;
        this.peer = peer;
        LampController controller = getCrew();
        controller.setPeer(peer, hostState, lampState);
    }

    LampControllerSteward (EEnvironment env, LampFrameworkSteward frameworkSteward) {
        this.env = env;
        this.frameworkSteward = frameworkSteward;
        establish();
    }

    private LampController establish () {
        LampController controller = frameworkSteward.getCrew().getLampController();
        this.tether = new Tether(env.vat(), controller);
        if (peer != null) {
            controller.setPeer(peer, hostState, lampState);
        }
        return controller;
    }
    
    private LampController getCrew() {
        LampController controller = null;
        try {
            controller = (LampController)tether.held();
        } catch (SmashedException e) {
            env.vat().println("Reestablishing crew for LampController");
            controller = establish();
        }
        return controller;
    }
}


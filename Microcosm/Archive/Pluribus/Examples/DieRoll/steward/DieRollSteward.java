package ec.pl.examples.dieroll;

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

class DieRollFrameworkSteward implements UIFramework, DieRollFramework, Syncologist
{
    private Tether /* DieRollFramework */ tether;
    private EEnvironment env;
    
    public DieRollController getDieRollController() {
        DieRollController controller = new DieRollControllerSteward(env, this);
        return controller;
    }
    
    DieRollFrameworkSteward (EEnvironment env, DieRollFramework framework) {
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
        else {
            System.out.println("Reestablishing UI Framework");
        }
        DieRollFramework framework = (DieRollFramework)owner.innerFramework(env);
        tether = new Tether(env.vat(), framework);
        maker.runFramework(owner);
        env.vat().setSyncQuakeNoticer(this);
    }
    
    DieRollFramework getCrew() {
        DieRollFramework framework = null;
        try {
            framework = (DieRollFramework)tether.held();
        } catch (SmashedException e) {
            System.out.println("Invariant violated - DieRollFramework Tether's framework smashed");
            e.printStackTrace();
            throw new Error("Invariant violated - DieRollFramework Tether's framework smashed:" + e);
        }
        return framework;
    }

}

class DieRollControllerSteward implements DieRollController {
    private EEnvironment env;
    private Tether /* DieRollController */ tether;
    private DieRollPeer peer;
    private DieRollFrameworkSteward frameworkSteward;
    
    public void postEvent (int eventType, int value, Object data) {
        DieRollController controller = getCrew();
        controller.postEvent(eventType, value, data);
    }

    public void setPeer (DieRollPeer peer) {
        this.peer = peer;
        DieRollController controller = getCrew();
        controller.setPeer(peer);
    }

    private DieRollController establish () {
        DieRollController controller = frameworkSteward.getCrew().getDieRollController();
        this.tether = new Tether(env.vat(), controller);
        if (peer != null) {
            controller.setPeer(peer);
        }
        return controller;
    }
    
    DieRollControllerSteward (EEnvironment env, DieRollFrameworkSteward frameworkSteward) {
        this.env = env;
        this.frameworkSteward = frameworkSteward;
        establish();
    }

    private DieRollController getCrew() {
        DieRollController controller = null;
        try {
            controller = (DieRollController)tether.held();
        } catch (SmashedException e) {
            env.vat().println("Reestablishing crew for DieRollController");
            controller = establish();
        }
        return controller;
    }
}


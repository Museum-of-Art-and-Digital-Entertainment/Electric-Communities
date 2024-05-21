package ec.pl.examples.web;

import ec.pl.runtime.MagicUIPowerMaker;
import ec.pl.runtime.UIFrameworkOwner;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import ec.e.start.Syncologist;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

class WebFrameworkSteward implements WebFramework, Syncologist
{
    private Tether /* WebFramework */ tether;
    private EEnvironment env;
    
    public WebController getWebController() {
        WebController controller = new WebControllerSteward(env, this);
        return controller;
    }
    
    WebFrameworkSteward (EEnvironment env, WebFramework framework) {
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
        WebFramework framework = (WebFramework)owner.innerFramework(env);
        tether = new Tether(env.vat(), framework);
        maker.runFramework(owner);
        env.vat().setSyncQuakeNoticer(this);
    }
    
    WebFramework getCrew() {
        WebFramework framework = null;
        try {
            framework = (WebFramework)tether.held();
        } catch (SmashedException e) {
            System.out.println("Invariant violated - WebFramework Tether's framework smashed");
            e.printStackTrace();
            throw new Error("Invariant violated - WebFramework Tether's framework smashed:" + e);
        }
        return framework;
    }
}

class WebControllerSteward implements WebController {
    private int myStart;
    private int myEnd;
    private EEnvironment myEnv;
    private String myLink;
    private Tether /* WebController */ myTether;
    private WebPeer myPeer;
    private WebFrameworkSteward myFrameworkSteward;
    
    public void postEvent (int eventType, boolean state) {
        WebController controller = getCrew();
        controller.postEvent(eventType, state);
    }

    public void postStatus (String status) {
        WebController controller = getCrew();
        controller.postStatus(status);
    }

    public void postLink (String link) {
        WebController controller = getCrew();
        controller.postLink(link);
    }

    public void postSelection (int start, int end) {
        WebController controller = getCrew();
        controller.postSelection(start, end);
    }

    public void setPeer (WebPeer peer, String link, int start, int end) {
        this.myPeer  = peer;
        this.myLink  = link;
        this.myStart = start;
        this.myEnd   = end;
        WebController controller = getCrew();
        controller.setPeer(peer, link, start, end);
    }

    private WebController establish () {
        WebController controller = myFrameworkSteward.getCrew().getWebController();
        this.myTether = new Tether(myEnv.vat(), controller);
        if (myPeer != null) {
            controller.setPeer(myPeer, myLink, myStart, myEnd);
        }
        return controller;
    }
    
    WebControllerSteward (EEnvironment env, WebFrameworkSteward frameworkSteward) {
        this.myEnv = env;
        this.myFrameworkSteward = frameworkSteward;
        establish();
    }

    private WebController getCrew() {
        WebController controller = null;
        try {
            controller = (WebController)myTether.held();
        } catch (SmashedException e) {
            myEnv.vat().println("Reestablishing crew for WebController");
            controller = establish();
        }
        return controller;
    }
}


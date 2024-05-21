package ec.pl.examples.dieroll;

import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.SmashedException;

import ec.ifc.app.ECApplication;

import java.util.Properties;

public class IFCDieRollFramework implements DieRollFramework
{
    private Tether tether;
    private EEnvironment env;
    
    public DieRollController getDieRollController() {
        ECApplication crew = (ECApplication)getCrew();
        return new IFCDieRollController(env, crew);
    }
    
    IFCDieRollFramework (EEnvironment env) {
        this.env = env;
    }   

    ECApplication getRunner () {
        return getCrew();
    }

    private IFCCrewDieRollFramework makeTether () {
        IFCCrewDieRollFramework framework = new IFCCrewDieRollFramework();
        tether = new Tether(env.vat(), framework);
        return framework;
    }
    
    private IFCCrewDieRollFramework getCrew() {
        if (tether == null) {
            return makeTether();
        }
        IFCCrewDieRollFramework crew = null;
        try {
            crew = (IFCCrewDieRollFramework) tether.held();
        } catch (SmashedException e) {
            crew = makeTether();
        }
        return crew;
    }
}

public class IFCDieRollController implements DieRollController {
    private EEnvironment env;
    private ECApplication application;
    private Tether tether;
    
    public void postEvent (int eventType, int value, Object data) {
        IFCCrewDieRollController crew = getCrew();
        crew.postEvent(eventType, value, data);
    }

    public void setPeer (DieRollPeer peer) {
        IFCCrewDieRollController crew = getCrew();
        crew.setPeerTether(tether.vat().makeFragileRoot(peer, null));
    }

    IFCDieRollController (EEnvironment env, ECApplication application) {
        this.env = env;
        this.application = application;
    }
    
    private IFCCrewDieRollController makeTether () {
        IFCCrewDieRollController controller = new IFCCrewDieRollController(env.props(), application);
        tether = new Tether(env.vat(), controller);
        return controller;
    }
    
    private IFCCrewDieRollController getCrew() {
        if (tether == null) return makeTether();
        IFCCrewDieRollController crew = null;
        try {
            crew = (IFCCrewDieRollController) tether.held();
        } catch (SmashedException e) {
            crew = makeTether();
        }
        return crew;
    }    
}


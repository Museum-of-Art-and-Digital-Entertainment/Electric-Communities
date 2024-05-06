package ec.pl.examples.lamp;

import ec.e.io.crew.RtConsole;
import ec.e.io.EInputHandler;
import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.e.file.EStdio;
import ec.pl.runtime.UIFramework;
import ec.pl.runtime.UIFrameworkOwner;

public class ConsoleLampFactory implements UIFrameworkOwner
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

public class ConsoleLampFramework implements LampFramework
{
    private EEnvironment env;
    
    ConsoleLampFramework (EEnvironment env) {
        this.env = env;
    }
    
    public LampController getLampController() {
        return new ConsoleLampController(env);
    }
}

public eclass ConsoleLampController implements LampController, EInputHandler
{
    private LampPeer peer;
    private int value;
    private Vat vat;
    private String hostString;
    private boolean hostState;
    private boolean setup = false;
    
    // The local user typed a newline character - so toggle the lamp.
    emethod handleInput (String line) {
        if (line != null && line.equals("quit")) {
            vat.exit(0);
        }
        if (line != null && line.equals("invalidate")) {
            if (peer != null) peer <- lampInvalidate();
        }
        else {
            if (peer != null) peer <- lampToggle();
        }
    }

    local void postEvent (int eventType, boolean state) {
        switch (eventType) {
            case LampController.EVENT_LAMP_STATUS:
                EStdio.out().println("A " + hostString + " lamp now exists");
                // Fall through
            case LampController.EVENT_LAMP_STATE:
                EStdio.out().println(hostString + ": Lamp is " + (state ? "on" : "off"));
                break;
            default:
                EStdio.out().println("Warning, unknown type in postEvent: " + eventType);
        }
    }

    local void postStatus (String status) {
        System.out.println(status);
    }

    local void setPeer (LampPeer peer, boolean hostState, boolean lampState) {
        if (setup == false) {
            RtConsole.setupConsoleReader(new Tether(vat, this), System.in);
            setup = true;
        }
        this.peer = peer;
        hostString = (hostState ? "Host" : "Client");
        this.hostState = hostState;
    }

    //
    // Starting it all up
    //

    public ConsoleLampController (EEnvironment env) {
        vat = env.vat();
    }
}

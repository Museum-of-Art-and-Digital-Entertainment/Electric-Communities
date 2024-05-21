package ec.pl.examples.dieroll;

import ec.e.io.crew.RtConsole;
import ec.e.io.EInputHandler;
import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.Vat;

public class ConsoleDieRollFactory implements DieRollFactory
{
    private EEnvironment env;
    
    public DieRollController getDieRollController() {
        return new ConsoleDieRollController(env);
    }

    public void setEnvironment (EEnvironment env) {
        this.env = env;
    }
    
    public void run() {
        synchronized (this) {
            try {
                wait(0);
            } catch (Exception e) {
                System.out.println("Console woke up, exiting!");
            }
        }
    }
}

public eclass ConsoleDieRollController implements DieRollController, EInputHandler
{
    private DieRollPeer peer;
    private int value;
    private Vat vat;
    
    // The local user typed a newline character - so toggle the lamp.
    emethod handleInput (String line) {
        if (line != null && line.equals("quit")) {
            System.exit(0);
        }
        else {
            if (peer != null) peer <- dieroll();
        }
    }

    local void postEvent (int eventType, int value, Object data) {
        switch (eventType) {
            case DieRollController.EVENT_DIEROLL_VALUE:
                System.out.println("Value is " + value);
                break;
            default:
                System.out.println("Warning, unknown type in postEvent: " + eventType);
        }
    }

    local void setPeer (DieRollPeer peer) {
        RtConsole.setupConsoleReader(new Tether(vat, this), System.in);
        this.peer = peer;
    }

    //
    // Starting it all up
    //

    public ConsoleDieRollController (EEnvironment env) {
        vat = env.vat();
    }
}

package ec.pl.examples.web;

import ec.e.io.crew.RtConsole;
import ec.e.io.EInputHandler;
import ec.e.start.EEnvironment;
import ec.e.start.Tether;
import ec.e.start.Vat;

public eclass ConsoleWebController implements WebController, EInputHandler
{
    WebPeer peer;
    private Vat vat;

    emethod handleInput (String line) {
    if (line != null && (line.indexOf("://") != -1)) {
        if (peer != null) peer <- webLink(line);
    }
    }

    local void postEvent (int eventType, boolean state) {
    }

    local void postStatus (String status) {
        System.out.println(status);
    }

    local void postLink (String link) {
        System.out.println("Now linked to " + link);
    }

    local void postSelection (int start, int end) {
        System.out.println("Selection is now " + start + ":" + end);
    }

    local void setPeer (WebPeer peer, String link, int start, int end) {
        RtConsole.setupConsoleReader(new Tether(vat, this), System.in);
    this.peer = peer;
    postLink(link);
    }

    //
    // Starting it all up
    //

    public ConsoleWebController (EEnvironment env) {
        vat = env.vat();
    }
}

public class ConsoleWebFramework implements WebFramework
{
    private EEnvironment env;
    
    ConsoleWebFramework (EEnvironment env) {
        this.env = env;
    }
    
    public WebController getWebController() {
        return new ConsoleWebController(env);
    }
}



package ec.tests.comm;

import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;
import ec.e.net.RtDirectoryEException;
import ec.e.net.ENetUtility;
import ec.e.net.EDirectoryServer;
import ec.e.stream.*;
import java.util.*;
import ec.e.db.*;

public class RemoteLoad implements ELaunchable
{
    public void go (EEnvironment env) {
        String url = env.getProperty("url");
        if (!ENetUtility.startNet(env, 0)) System.exit(1);
        ENetUtility.autoShutdown(env, 20000);
        HelloGuy guy = new HelloGuy(env);
        if (url != null) // client specifies url
            guy <- send(url);
        else
            guy <- receive();
    }
}

eclass HelloGuy 
{
    EEnvironment env;
    
    public HelloGuy (EEnvironment Env) {
        env = Env;
    }
    
    emethod send (String url) {
        HelloGuy otherGuy;
        XXXRemotePerson whacker = new XXXRemotePerson("Whacker");
        
        etry {
            EDirectoryServer.lookupURL(env, url, &otherGuy);
        } ecatch (RtDirectoryEException e) {
            System.out.println("Caught exception on lookup: " + e.getMessage());
        }        
        otherGuy <- hello(whacker);       
        otherGuy <- goodbye(this);
    }
    
    emethod goodbye (EObject other) {
        other <- shutdownComSystem();
        this <- shutdownComSystem();
    }
    
    emethod shutdownComSystem () {
        ENetUtility.stopNet(env);
    }
    
    emethod receive () {
        ENetUtility.publishWithPropertyName(this, "Doohickey", env, null, "Published receiver with url=");
    }

    emethod hello (Object person) {
        System.out.println("Hola Mundo from " + person);
    }
}

package ec.tests.comm;

import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefMaker;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.InvalidURLException;

import ec.e.net.RtDirectoryEException;
import ec.e.file.EStdio;
import ec.e.lang.EString;
import java.io.IOException;

// first run:

// erun -trace HelloHandoff user.name=Host reg=host

//
// This guy is totally trusted and makes subenvironment
// things that are passed to other less trusted
// thingies.
public eclass HelloHandoff implements ELaunchable
{
    emethod go(EEnvironment env) {
        String hostURL = env.getProperty("hostURL");
        String brokerURL = env.getProperty("brokerURL");
        String reg = env.getProperty("reg");
        Registrar registrar = Registrar.summon(env);

        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }
        SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
        SturdyRefFileImporter refImporter = registrar.getSturdyRefFileImporter(env);
        SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
        
        if (hostURL != null) { // broker specifies hostURL
            SturdyRef hostRef = null;
            try {
                hostRef = refImporter.importRef(hostURL);
            } catch (Exception e) {
                EStdio.err().println("Exception occured importing ref");
                env.vat().exit(-1);
            }
            HelloHandoffBroker guy = new HelloHandoffBroker();
            SturdyRef brokerRef = refMaker.makeSturdyRef(guy);
            try {
                refExporter.exportRef(brokerRef, reg);
            }
            catch (Exception e) {
                EStdio.err().println("Exception occured exporting ref");
                env.vat().exit(-1);
            }
            
            EStdio.out().println("Broker: erun -trace HelloHandoff user.name=Client reg=client brokerURL=" + reg);
            guy <- broker(registrar, hostRef);
        }
        else if (brokerURL != null) { // client specifies brokerURL
            SturdyRef brokerRef = null;
            try {
                brokerRef = refImporter.importRef(brokerURL);
            } catch (Exception e) {
                EStdio.err().println("Exception occured importing ref");
                env.vat().exit(-1);
            }       
            HelloHandoffClient guy = new HelloHandoffClient();
            guy <- client(registrar, brokerRef);
        }
        else { // host does not specify a url
            HelloHandoffHost guy = new HelloHandoffHost();
            SturdyRef ref = refMaker.makeSturdyRef(guy);
            try {
                refExporter.exportRef(ref, reg);
            }
            catch (Exception e) {
                EStdio.err().println("Exception occured exporting ref");
                env.vat().exit(-1);
            }
            
            EStdio.out().println("Host: erun -trace HelloHandoff user.name=Broker reg=broker hostURL=" + reg);
            guy <- host(registrar);
        }
    }
}

eclass HelloHandoffGuy 
{
    static Registrar myRegistrar;

    emethod stopNetwork () {
        if (myRegistrar != null) {
            try {
                myRegistrar.offTheAir();
            } catch (RegistrarException e) {
                EStdio.out().println("net shutdown: " + e);
            }
        }
    }

    static void setRegistrar (Registrar reg) {
        myRegistrar = reg;
    }
}

eclass HelloHandoffClient extends HelloHandoffGuy {

    emethod client (Registrar registrar, SturdyRef brokerRef) {
        HelloHandoffHost host;
        HelloHandoffBroker broker;

        this.setRegistrar(registrar);
        EStdio.out().println("Connecting to broker");
        brokerRef.followRef(&broker);
        EStdio.out().println("Asking broker for handoff");
        broker <- handoff (&host);
        ewhen host (Object ignored) {
            EStdio.out().println("Got handoff, sending hello");
            host <- helloHandoff("This is a string", this);
        }
    }
}


eclass HelloHandoffBroker extends HelloHandoffGuy
{
    HelloHandoffHost host;  
    HelloHandoffHost hostProxy = null;
    EResult clientDist = null;

    emethod broker (Registrar registrar, SturdyRef hostRef) {
        this.setRegistrar(registrar);
        EStdio.out().println("Looking up host");
        hostRef.followRef(&host);
        EStdio.out().println("Asking host to send itself as proxy");
        host <- sendProxy(this);
    }

    emethod receiveProxy (HelloHandoffHost proxy) {
        EStdio.out().println("ReceiveProxy entered");
        synchronized (this) {
            EStdio.out().println("Setting hostProxy to received proxy");
            hostProxy = proxy;
            if (clientDist != null) {
                EStdio.out().println("Forwarding Host Proxy after waiting");
                clientDist <- forward(hostProxy);
            }
        }
    }

    emethod handoff (EResult dist) {
        EStdio.out().println("Handoff entered");
        if (hostProxy == null) {
            EStdio.out().println("Waiting for hostProxy to get set");
            clientDist = dist;
        }   
        else {
            EStdio.out().println("Forwarding Host Proxy");
            dist <- forward(hostProxy);
        }
    }
}

eclass HelloHandoffHost extends HelloHandoffGuy
{
    HelloHandoffBroker theBroker = null;

    emethod host (Registrar registrar) {
        this.setRegistrar(registrar);
    }

    emethod sendProxy (HelloHandoffBroker broker) {
        theBroker = broker;
        EStdio.out().println("Sending Broker host proxy");
        broker <- receiveProxy(this);
    }

    emethod helloHandoff (String theString, HelloHandoffGuy sender) {
        EStdio.out().println("Hola Mundo");
        if (theBroker != null) theBroker <- stopNetwork();
        if (sender != null) sender <- stopNetwork();
        this <- stopNetwork();
    }

    Object value() {
        //return this;
        return null;
    }
}
/*
class NetworkDelegate implements ENotificationHandler {
    ERegistrar reg;
    String pid;

    public NetworkDelegate(ERegistrar Reg, String Pid) {
        reg = Reg;
        pid = Pid;
    }

    public void handleNotification(String type, Object arg, Object info) {
        EStdio.out().println(pid + ": Notification " + type + " " + arg + " " + info);
        if (type == ERegistrar.NewConnection) {
            RtConnection connection = (RtConnection)arg;
            try {
                reg.AskForNotification(connection, this);
            } catch (ERestrictedException e) {
                EStdio.out().println(pid + ": AskForNotification: " + e);
            }
        }
    }
}

*/

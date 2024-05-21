package ec.tests.comm;

import ec.e.comm.*;

//
// This is where it all starts, the main function 
//
public class HelloExample
{
    public static void main(String args[]) {
        RtLauncher.launch(new HelloExampleLauncher(), args);
    }
}

//
// This guy is totally trusted and makes subenvironment
// things that are passed to other less trusted
// thingies.
eclass HelloExampleLauncher implements ELaunchable
{
    emethod go(RtEEnvironment env) {
		RtNetworkController con;
		int defaultPortNumber = env.getDefaultPortNumber();

		String hostname = env.getProperty("host");
		if (hostname == null) {
			// We'll be the host
			HelloExampleHost host = new HelloExampleHost();
			con = env.startNetworkEnvironment(defaultPortNumber);
			host <- goHost(env, con);
		}
		else {
			// We're the client, connect to host
			HelloExampleClient client = new HelloExampleClient();
			con = env.startNetworkEnvironment(defaultPortNumber+1);
			client <- goClient(env, con, hostname);
		}
    }
}

eclass HelloExampleNetworkObject
{
	static RtNetworkController networkController = null;

	emethod stopNetwork () {
		this.stopNetworkEnvironment();
	}

	static void setNetworkController (RtNetworkController con) {
		networkController = con;
	}

	static void stopNetworkEnvironment () {
		if (networkController != null) 
			networkController.stopNetworkEnvironment();
	}
}

eclass HelloExampleClient extends HelloExampleNetworkObject
{
	emethod goClient (RtEEnvironment env, RtNetworkController con, String hostname) {
		HelloExampleHost host;
		this.setNetworkController(con);
        etry {
			System.out.println("Connecting to host");
            env.getConnector().lookupOnPort(hostname, 
				"HelloExampleHost",
				RtComMonitor.keBasePortNumber,
				&host);
        } ecatch (RtDirectoryEException e) {
            System.out.println("Client caught exception: " + e.getMessage());
        }
		host <- helloExampleThere("This is a string", this);
	}
}

eclass HelloExampleHost extends HelloExampleNetworkObject
{
    emethod goHost (RtEEnvironment env, RtNetworkController con) {
		this.setNetworkController(con);
		System.out.println("Registering host");
        env.getRegistrar().register("HelloExampleHost", this);
    }

    emethod helloExampleThere (String theString, EObject sender) {
        System.out.println("Hola Mundo");
		if (sender != null) sender <- stopNetwork();
		this <- stopNetwork();
    }
}

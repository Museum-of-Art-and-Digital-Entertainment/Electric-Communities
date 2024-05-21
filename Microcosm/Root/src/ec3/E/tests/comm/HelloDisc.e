/* 
   HelloDisc.e -- Version 0.2 -- Simple comm test

   Electric Communities
   19-February-1996
   
   Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.comm;

import ec.util.*;
import ec.e.comm.*;
import ec.e.dgc.*;
//import ec.e.db.RtEncoder;
//import ec.e.db.RtDecoder;
import ec.e.lang.*;

public class HelloDisc
{
    public static void main(String args[]) {
        RtLauncher.launch(new HelloDiscLauncher(), args);
    }
}

eclass HelloDiscLauncher implements ELaunchable
{
    emethod go(RtEEnvironment env) {
		int port = 5432;
		boolean client = true;
		boolean server = true;
		String args[] = (String[])env.getObjectFromDictionary("Args");
		HelloDiscSender sender = new HelloDiscSender();
		HelloDiscReceiver receiver = new HelloDiscReceiver();
		if ((args != null) && (args.length > 0)) {
			if (args[0].equals("client")) {
				server = false;
				port = 5678;
			}
			else if (args[0].equals("server")) client = false;
		}
		RtNetworkController con = env.startNetworkEnvironment(port);
		if (server) receiver <- receiveHelloDisc(env, con);
		if (client) sender <- sendHelloDisc(env, con);
    }
}

class ServerHandler implements RtNotificationHandler {

	public void handleNotification(String type, Object arg, Object info) {
		System.out.println("Notification " + type + " " + arg + " " + info);
		if (type == RtComMonitor.RtNewConnection) {
			RtConnection connection = (RtConnection)info;
			connection.registerForNotification(this, 
					this, RtConnection.RtDisconnectionNotification);
		}
	}
}

class ClientHandler implements RtNotificationHandler {
	Object object;
	Checker checker = new Checker();

	public ClientHandler (Object obj) {
		object = obj;
	}

	public void handleNotification(String type, Object arg, Object info) {
		EObject channel1;
		EObject channel2;
		EInteger theEInteger = new EInteger(1);
		RtConnection connection = (RtConnection)info;
		System.out.println("Notification " + type + " " + arg + " " + info);
		if (connection.isProxyOnConnection(object)) {
			System.out.println("Object is proxy, as expected");
		}
		else {
			System.out.println("*** Error: object is not proxy: " + object);
		}
		if (connection.isProxyOnConnection(channel1)) {
			System.out.println("*** Error: Channel1 is proxy: " + this);
		}
		else {
			System.out.println("Channel1 is not proxy, as expected");
		}
		EObject eo = (EObject)object;
		if (connection.isProxyOnConnection(eo)) {
			System.out.println("EObject is proxy, as expected");
		}
		else {
			System.out.println("*** Error: EObject is not proxy: " + object);
		}
		if (connection.isProxyOnConnection(this)) {
			System.out.println("*** Error: Object is proxy: " + this);
		}
		else {
			System.out.println("This object is not proxy, as expected");
		}
		if (connection.isProxyOnConnection(null)) {
			System.out.println("*** Error: Null Object is proxy: " + this);
		}
		else {
			System.out.println("Null object is not proxy, as expected");
		}
		&channel1 <- forward(eo);
		&channel2 <- forward(theEInteger);
		checker <- checkForProxies(connection, channel1, channel2);
	}
}

eclass Checker
{
	emethod checkForProxies (RtConnection connection, EObject channel1, EObject channel2) {
		if (connection.isProxyOnConnection(channel1)) {
			System.out.println("Channel1 is now proxy, as expected");
		}
		else {
			System.out.println("*** Error: Channel1 is not proxy");
		}
		if (connection.isProxyOnConnection(channel2)) {
			System.out.println("*** Error: Channel2 is proxy");
		}
		else {
			System.out.println("Channel2 is not proxy, as expected");
		}
	}
}

eclass HelloDiscSender 
{
	RtNetworkController networkController;
	RtConnection connection;

    emethod sendHelloDisc (RtEEnvironment env, RtNetworkController con) {
        HelloDiscReceiver otherGuy;
		networkController = con;
		con.setNetworkDelegate(new ServerHandler());
		String hostname = env.getProperty("host");
		if ((hostname == null) || 
			(hostname == RtEEnvironment.DefaultPropertyValue)) 
			hostname = "localhost";
		connection = con.getConnectionForHostAndPort(hostname, 5432);
        etry {
            env.getConnector().lookupOnPort(hostname, "Doohickey", 5432, &otherGuy);
        } ecatch (RtDirectoryEException e) {
            System.out.println("HelloDiscSender catches exception: " + e);
        }
		if (connection != null) {
			ewhen otherGuy (Object realGuy) {
				networkController.stopNetworkEnvironment(false);
				System.out.println("Registering for invalidation");
				connection.registerForNotification(
					new ClientHandler(realGuy),
					this, RtConnection.RtDisconnectionNotification);
			}
		}
		else {
			System.out.println("Couldn't get connection to register");
		}
        otherGuy <- helloDisc(this);
    }

	emethod disconnect () {
		connection.disconnect();
	}
}

eclass HelloDiscReceiver 
{
    RtNetworkController networkController;
    RtRegistration reg;

    emethod receiveHelloDisc (RtEEnvironment env, RtNetworkController con) {
		con.setNetworkDelegate(new ServerHandler());
		networkController = con;
        reg = env.getRegistrar().register("Doohickey", (EObject)this);
    }
    
    emethod helloDisc(EObject other) {
        System.out.println("Hola Mundo");
		networkController.stopNetworkEnvironment(false);
		other <- disconnect();
    }

	Object value () { return this; }
}



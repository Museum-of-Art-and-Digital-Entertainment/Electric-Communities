/* 
   HelloComm.e -- Version 0.2 -- Simple comm test

   Arturo Bejar & Chip Morningstar
   Electric Communities
   19-February-1996
   
   Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.loader;

import ec.e.comm.*;
import ec.e.dgc.*;
import ec.e.db.RtStandardEncoder;
import ec.e.db.RtStandardDecoder;

public class HelloLoader
{
    public static void main(String args[]) {
		int level = 0;
		if ((args != null) && (args.length > 0)) {
			level = (new Integer(args[0])).intValue();
		}
		RtLauncher.setupClassLoader(level);
        RtLauncher.launch(new HelloLauncher(), args);
    }
}

eclass HelloLauncher implements ELaunchable
{
    emethod go(RtEEnvironment env) {
	HelloSender sender = new HelloSender();
	HelloReceiver receiver = new HelloReceiver();
	RtNetworkController con = env.startNetworkEnvironment();
	receiver <- receiveHello(env, con);
	sender <- sendHello(env);
    }
}

eclass HelloSender 
{
    emethod sendHello (RtEEnvironment env) {
        HelloReceiver otherGuy;
        etry {
            env.getConnector().lookup("localhost", "Doohickey", &otherGuy);
        } ecatch (RtDirectoryEException e) {
            System.out.println("HelloSender catches exception: "
                               /*KSS*//* + e.getMessage()*/);
	                      
        }
        otherGuy <- hello();
    }
}

eclass HelloReceiver 
{
    RtNetworkController networkController;
    RtRegistration reg;
    
    emethod receiveHello (RtEEnvironment env, RtNetworkController con) {
	networkController = con;
        //KSS reg = env.getRegistrar().register("Doohickey", this);
        //KSS Add (EObject) casting--bogus!
        reg = env.getRegistrar().register("Doohickey", (EObject)this);
    }
    
    emethod hello() {
        System.out.println("Hola Mundo");
	try {
	    reg.unregister();
	} catch (Exception e) {
	    System.out.println("Couldn't unregister Doohickey");
	}
	networkController.stopNetworkEnvironment();
    }
}



	package ec.tutorial.cointoss;
	import ec.plgen.*;
	import ec.e.start.ELaunchable;
	import ec.e.cap.*;
	import ec.e.net.*;


// You can provide a command line argument ("who") that
// specifies a file which names a particular CoinTosser on the network.  If
// the "who" argument is specified, this code uses that file to obtain the
// name of a pre-existing CoinTosser unum, and then looks up that unum using
// ENetUtility.  If the "who" argument is not specified, the code creates a
// brand new CoinTosser unum, and registers it on the net.  Either way, the
// code winds up using the unum (whether newly created or looked up on the
// network) to toss a coin.

public eclass CoinTosser implements Agent {
  emethod go (EEnvironment env) {	
    String envWho = env.getProperty("who");

    Unum tosser;
    EBoolean didIWin;
    Unum tosserChannel;
    if (envWho != null) {
	etry {
	  System.out.println("Trying to connect: " + envWho);
	  String errorString = ENetUtility.lookupWithName(&tosserChannel, env, envWho);
         if (errorString != null) {
            System.out.println(errorString);

     System.exit(0);
         }
	  ewhen tosserChannel (Unum aTosser) {
         System.out.println("Starting up a client tosser: \n"+ aTosser);
	    tosser = aTosser;

    // we want heads
         tosser <- uTossCoin(1, &didIWin);
	  }			
      } ecatch (RtEException e) {
        System.out.println("E Exception on lookup: " + e);
        System.exit(0);
      }
    } else {
	System.out.println("Starting new tosser. This is the host.");

	// This horrible line is temporary syntax; we will eventually have
	// much more readable ways to create una.
	tosser = ui$_uiCoinTosser_.createUnum();
    // we want heads
         tosser <- uTossCoin(1, &didIWin);

	String url = ENetUtility.registerWithPropertyName(tosser, env,
		"EARLFile", "CoinTosser:who=");		
    }	

    // OK, now tosser references a real live CoinTosser unum, no matter 
	// how we got it.  Now use it to toss a coin!


    ewhen didIWin (boolean yes) {
      if (yes)
	 System.out.println("I WON I WON I WON!!!");
      else
	 System.out.println("Boo hoo... I *lost*!");
    }
  }
}


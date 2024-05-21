package ec.pl.examples.simple;

import ec.pl.runtime.*;
import ec.e.start.ELaunchable;
import ec.e.cap.*;
import ec.e.net.*;

public eclass Simple implements Agent
{
  emethod go (EEnvironment env) {
    
    System.out.println("\nIn Simple.go()\n");
    Unum simple = null;

    // This would be the spot to set up the user interface peer, which
    // is a java class intended to handle all user interface events.
    // In this example, we don't care.

    String who = env.getProperty("who");
    if (who != null) {

      System.out.println("Argument: who=" + who);
      System.out.println("Attempting to connect via EARL...");

      // If we were instructed to connect to another host presence
      // (i.e. by an argument 'who=<EARL>') do it now.

      Unum simpleChannel;
      simple = simpleChannel;
      etry {
        String errorString = 
          ENetUtility.lookupWithName(&simpleChannel, env, who);
        if (errorString != null) {
          System.out.println(errorString);
          System.exit(1);
        }
      } ecatch (RtEException e) {
          System.out.println("E Exception on lookup: " + e);
          System.exit(1);
      }
    } else {

      // We weren't instructed to connect to another host presence, 
      // so we should create our own.

      simple = (Unum) ui$_uiSimple_.createUnum(env);
      ENetUtility.registerWithPropertyName(
        simple, env, "EARLFile", "Simple: who=");
    }

    System.out.println("\nSending message simple<-acceptMessageAndAddSomething()\n");
    simple<-acceptMessageAndAddSomething(7);

    System.out.println("\nEnd of Simple.go()\n");

  }
}

// FYI, an example EARL argument:
// who=e://harpo:36301/harpo:felix:848017628603/#1h2o08u6apl42

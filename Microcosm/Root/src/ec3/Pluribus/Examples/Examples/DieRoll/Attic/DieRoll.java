package ec.pl.examples.dieroll;

import ec.pl.runtime.*;
import ec.e.start.ELaunchable;
import ec.e.cap.*;
import ec.e.net.*;

public eclass DieRoll implements Agent
{
	static DieRollFactory factory;	

	emethod go (EEnvironment env) {
		factory = (DieRollFactory) env.get("UIFactory");
		if (factory == null) {
			System.out.println("Error, can't get UI factory");
			System.exit(0);
		}
	
        Unum die = null;
        String prop = env.getProperty("who");
        if (prop == null) {
        	// We're the first pioneers here, advertise a host Presence
			die = ui$_DieRoll_ui_.createUnum(true);
			ENetUtility.registerWithPropertyName(die, env, "EARLFile", "DieRoll: who=");			
		}
		else {
			// We're supposed to connect to a host Presence
        	Unum dieChannel;
        	die = dieChannel;
        	etry {
        		String errorString = ENetUtility.lookupWithName(&dieChannel, env, prop);
        		if (errorString != null) {
        			System.out.println(errorString);
        			System.exit(0);
        		}
			} ecatch (RtEException e) {
				System.out.println("E Exception on lookup: " + e);
				System.exit(0);
			}
		}
		die <- ki$_DieRoll_uk_if_.setupUI(factory.getDieRollController());
	}
}


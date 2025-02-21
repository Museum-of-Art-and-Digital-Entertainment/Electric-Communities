package ec.pl.examples.lamp;

import ec.pl.runtime.Unum;
import ec.pl.runtime.UnumStateHolder;
import ec.pl.runtime.Agent;
import ec.ifc.codable.WorldObjectState;

import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;
import ec.e.cap.ERestrictedException;
import ec.e.net.ERegistrar;
import ec.e.net.EInvalidUrlException;

import java.util.Hashtable;

public einterface LampPeer {
	lampToggle();
	lampTransfer();
	lampArchive();
}

public eclass Lamp implements Agent
{
	private final static String LightBulb = "LightBulb";
	
	emethod go (EEnvironment env) {	
        String prop = env.getProperty("who");
        String archiveName = env.getProperty("archive");
        String unarchiveName = env.getProperty("unarchive");
        String who = null;
		ERegistrar reg = (ERegistrar)env.get("registrar.root");
        ki$_Lamp_uk_if_ lamp = null;
        if (prop != null) {
        	ki$_Lamp_uk_if_ lampChannel;
        	lamp = lampChannel;
        	etry {
				try {
					who = RtUtil.readStringFromFile(prop);
					if (who == null) who = prop;
					reg.lookupURL(who, &lampChannel);
				} catch (ERestrictedException e) {
					System.out.println("lookupURL restricted: " + e);
					System.exit(0);
				} catch (EInvalidUrlException e) {
					System.out.println("Not a valid URL: " + who);
					System.exit(0);
				}
			} ecatch (RtEException e) {
				System.out.println("E Exception: " + e);
				System.exit(0);
			}
			lamp <- toggle();
		}
		else {
			LampState lampState;
			if (unarchiveName != null) {
				Object objs[] = WorldObjectState.unarchiveObjectsFromFile(unarchiveName);
				lampState = (LampState) objs[0];
			}
			else {
				lampState = new LampState();
				lampState.name = LightBulb;
			}
			Hashtable dictionary = new Hashtable(1);
			dictionary.put("environment", env);
			lamp = (ki$_Lamp_uk_if_) lampState.getWorldObject(dictionary);
        	if (archiveName != null) {
        		WorldObjectState objs[] = new WorldObjectState[1];
        		objs[0] = ((UnumStateHolder)lamp).getWorldObjectState();
        		WorldObjectState.archiveObjectsToFile(archiveName, objs);
        	}
		}
		lamp <- setupUI(new ConsoleLampController());
	}
}


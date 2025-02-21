package ec.pl.examples.web;

import java.io.*;
import java.lang.*;
import ec.pl.runtime.*;
import ec.e.cap.EEnvironment;
import ec.e.net.ERegistrar;
import ec.e.cap.ERestrictedException;
import ec.e.net.EInvalidUrlException;

public einterface WebPeer {
	webLink(String link);
	webSelection(int start, int end);
}

public eclass Web implements Agent
{
	private static WebFactory factory;
	
	emethod go (EEnvironment env) {
		factory = (WebFactory) env.get("UIFactory");
		if (factory == null) {
			System.out.println("Error, can't get UI factory");
			System.exit(0);
		}
		
        String prop = env.getProperty("who");
        String who = null;
		ERegistrar reg = (ERegistrar)env.get("registrar.root");
        Unum web = null;
        if (prop != null) {
        	Unum webChannel;
        	web = webChannel;
        	etry {
				try {
					who = RtUtil.readStringFromFile(prop);
					if (who == null) who = prop;
					System.out.println("Looking up Web Unum host " + who);
					reg.lookupURL(who, &webChannel);
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
		}
		else {
			web = ui$_Web_ui_.createUnum(env);
		}
		web <- ki$_Web_uk_if_.setupUI(factory.getWebController());
	}
}


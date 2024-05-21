/*****
 * TestBed
 *****
 * This is a testbed for testing Ingredients and Inter-ingredient messaging
 * across Presence, Unum and machine boundaries.  Unums with Client-Server
 * presence models and all the plumbing already done for you are provided,
 * just plug in your ingredients and test away.
 *****
 * How it works
 *****
 * The TestBed instantiates two Unums in two different processes (agencies)
 * and gives each Unum a reference (basically a pointer) to the other using 
 * E's network support.  This reference is held by a Referent ingredient
 * within each Unum; your ingredient can send messages to the remote
 * Unum by talking to this Referent ingredient.
 *
 * The Unums have a Client-Server presence model, so to use the testbed
 * you'll need to write both a Client and a Server ingredient for each
 * Unum.
 *****
 * Usage
 *****
 * Copy the entire TestBed directory into a new directory of your own.
 *
 * The source files AIngredients.plu and BIngredients.plu are source
 * files for the Client and Server ingredients for the two Unums, 
 * ATestUnum and BTestUnum.  You need to take any existing Ingredient 
 * source code and plug it into these files.
 *
 * A few Pluribus kinds are defined in these files, and these kinds are 
 * referred to in the Unum source files, so that the TestBed Unums and
 * their Presences can implement the kinds you define for your Ingredients.
 *
 * For instance, in AIngredients.plu, you should group under the kind 
 * "ukATest" any kinds and methods that the Unums that contain your 
 * Ingredient should implement.  Group under "pkATestServer" any kinds
 * and methods that the Server presence of the Unum that contains your 
 * Ingredient should implement.  Read that 6 times.
 *
 * When you want to send messages to the remote Unum, put them in an
 * envelope and send the envelope to the Referent ingredient in your 
 * presence, whose name within any of your Ingredients is iinReferent.
 * Canonical usage would be:
 *
 * method uDoSomething() {
 *	  RtEnvelope env;
 *
 *	  env <- uSomeMessageForAnUnum(someParm);
 *	  iinReferent <- uSendCurrentReference(env);
 * }
 *
 * Your Client Ingredient can send messages to the Server Ingredient
 * in the Server Presence of the Unum by sending messages to "myServerPresence",
 * a reference which is set up for you automatically.
 *
 * myServerPresence <- pServerStateUpdate(value);
 *
 * Your Server Ingredient can send a message to all of your Client
 * Ingredients (in all of the Client Presences of the Unum) by calling
 * sendToClients (a java method that is specific to the TestBed) with
 * an RtEnvelope as an argument:
 *
 * method pServerStateUpdate(someType value) {
 *    RtEnvelope env;
 *    env <- pClientStateUpdate(someType value);
 *    sendToClients(env);
 * }
 *
 * Charles Kendrick
 */ 

package ec.pl.examples.testbed;

import ec.pl.runtime.*;
import ec.e.start.ELaunchable;
import ec.e.cap.*;
import ec.e.net.*;

public eclass TestBed implements Agent
{
  emethod go (EEnvironment env) {
    
    System.out.println("In TestBed.go()");

    String Pid = env.getProperty("PublisherId");
	String url = env.getProperty("url");

	ERegistrar reg = (ERegistrar)env.get("registrar.root");

	if (url == null) 
	{
    	try { reg.startup(0); } catch (ERestrictedException e) { System.out.println("reg.startup: " + e); }
		Unum unumA = (Unum) ui$_uiATest_.createUnum();
		ENetUtility.publishWithPropertyName((EObject)unumA, "Doohicky", env, null, "url=");
		//								^^^^^^^^^^^^^^^^^ this cast should not be necessary
	} else {
		Unum unumA;
    	Unum unumB = (Unum) ui$_uiBTest_.createUnum();
		etry {
			try {
				reg.lookupURL(url, &unumA);
			} catch (Exception e) {
				System.out.println("lookupURL: " + url + " " + e);
			}
		} ecatch (RtDirectoryEException e) {
			System.out.println("TestBed caught exception on remote Unum lookup: " +
			e.getMessage());
		}
		unumB <- uSetUnumReference((EObject)unumA);
		unumA <- uSetUnumReference((EObject)unumB);
	}

    System.out.println("End of TestBed.go()");

  }
}

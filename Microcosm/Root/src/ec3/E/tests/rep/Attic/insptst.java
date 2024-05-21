package ec.tests.rep;

// Use this trivial code as a starting point for other testcases
// Just replace xxx with some other string to avoid class name collisions
// By Kari. Copyright 1997 Electric Communitites.

import ec.plgen.Agent;			// Needed for Agency (calls go())
import ec.e.cap.EEnvironment;	// Needed for Agency (the EEnvironment)
import ec.e.db.RtEncodingParameters; // Needed for parameter collections
import ec.e.db.RtDecodingParameters; // Needed for parameter collections
import java.util.Hashtable;		// Needed only for the props hashtable
import ec.e.rep.*;				// Needed to use Repository

import ec.tests.rep.*;			// Needed only for Canine and Feline
import ec.inspect.*;			// Needed only for Inspector.gather() calls
import ec.ifc.app.*;			// Needed only for IFC
import netscape.application.*;	// Needed only for IFC

/* To easily run this code, you may want to define
alias insptst 'java -debug ec.e.start.EBoot ec.plgen.Agency Agent=ec.tests.rep.insptst'
*/

public eclass insptst implements Agent
{
  emethod go (EEnvironment env) {	

    ECApplication app = new ECApplication(); // Create the app

    RtEncodingParameters params = new RtEncodingParameters();
    RtDecodingParameters revParams = new RtDecodingParameters();
    Hashtable props = new Hashtable(10);
 
	Inspector.gather(env,"Environment");
	Inspector.gather(params,"Params");
	Inspector.gather(revParams,"RevParams");
	Inspector.gather(props,"Props");

    try {

      Repository repry = new Repository(env);
      Feline cat1, cat2;
      Canine dog1, dog2;

      cat1 = new Feline(null, "Cat1");
      cat1.whatISay = "meow";

      dog1 = new Canine();
      dog1.whatISay="Woof";
      dog1.Dboolean = true;
      dog1.Dchar = 'X';
      dog1.Dlong = 1234;
      dog1.Ddouble = 3.14159;
      dog1.Dfloat = (float)41.999;
      dog1.cat = cat1;
      dog1.Dshort = 567;
      dog1.Dint = 9876;
      dog1.Dbyte = 5;
      dog1.Dnull = null;
      dog1.Ddummy = 666;
      dog1.nums[0] = 11111111;
      dog1.nums[1] = 22222222;
      dog1.nums[2] = 33333333;

      dog1.ch[0]='M';
      dog1.ch[1]='e';
      dog1.ch[2]='o';
      dog1.ch[3]='w';

      dog1.smurf[0] = 50;
      dog1.smurf[1] = 60;
      dog1.smurf[2] = 70;
      dog1.smurf[3] = 80;
      dog1.smurf[4] = 90;
      dog1.flags[0] = false;
      dog1.flags[1] = true;
      dog1.flags[2] = true;
      dog1.flags[3] = false;
      dog1.flags[4] = true;
      dog1.flags[5] = false;
      dog1.guy[0] = 4000;
      dog1.guy[1] = 5000;
      dog1.guy[2] = 6000;

      dog1.furr[0] = (float) 4.0004;
      dog1.furr[1] = (float) 5.0005;
      dog1.furr[2] = (float) 6.0006;

      dog1.Dobj[0] = new Integer(1);
      dog1.Dobj[1] = new String("Hi Gordie");

      dog1.Dobj2[0][0] = new String("0,0");
      dog1.Dobj2[0][1] = new String("0,1");
      dog1.Dobj2[1][0] = new String("1,0");
      dog1.Dobj2[1][1] = new String("1,1");

      repry = new Repository(env); // Open Repository
      repry.put("pooch",dog1, params, props); // Store dog in repository
      repry.commit();			// Commit changes (Important)
      repry.close();			// Close repository

      repry = new Repository(env);	// Re-open Repository
      dog2 = (Canine) repry.get("pooch", revParams); // Retrieve dog1 from repository
      repry.close();

	  Inspector.gather(repry,"Repository");
	  Inspector.gather(dog1,"dog1");
	  Inspector.gather(dog2,"Dog2");

      if (dog1.equals(dog2)) System.out.println("[SUCCESS] DOGSREQL - Dogs are equalp");
      else System.out.println("[FAILURE] DOGSRDIF - Dogs are different");

    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
    }
	app.run();
	//    System.exit(0);
  }
}

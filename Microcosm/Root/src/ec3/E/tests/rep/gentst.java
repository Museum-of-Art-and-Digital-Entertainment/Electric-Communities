package ec.tests.rep;

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency
import ec.e.rep.*;      // Needed to use Repository
import ec.e.db.RtEncodingParameters; // Needed for parameter collections
import ec.e.db.RtDecodingParameters; // Needed for parameter collections
import java.util.*;     // Needed only for class Enumeration
import ec.tests.rep.Feline; // Needed only for Feline class used in these examples

// To easily run this code, you may want to define
// % alias gentst 'java -debug ec.e.start.EBoot ec.tests.rep.gentst'

eclass gentst implements ELaunchable
{
    emethod go (EEnvironment env) {   

    RtEncodingParameters params = new RtEncodingParameters();
    RtDecodingParameters revParams = new RtDecodingParameters();
    Hashtable props = new Hashtable(10);
 
    try {

      Repository repry = new Repository(env);
      Feline cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8, cat9;
      Canine dog1,dog2;

      // We use capital first letter for names and all lower case for keys

      cat6 = new Feline(null, "Cat6");  // Last one has no sibling
      cat6.whatISay = "meoow";

      cat5 = new Feline(cat6,"Cat5"); // cat5 has cat6 as a sibling
      cat5.whatISay = "meoww";

      cat4 = new Feline(cat5, "Cat4");
      cat4.whatISay = "mmmeow";

      cat3 = new Feline(cat4, "Cat3");
      cat3.whatISay = "meowww";

      // In experiment two we will prune at cat5, so cat5 and cat6 will not be externalized

      dog1 = new Canine();

      dog1.whatISay="Woof";
      dog1.Someboolean = true;
      dog1.Somechar = 'X';
      dog1.Somelong = 1234;
      dog1.Somedouble = 3.14159;
      dog1.Somefloat = (float)41.999;
      dog1.cat = cat3;  // Note - cat3 is referenced from dog1
      dog1.Someshort = 567;
      dog1.Someint = 9876;
      dog1.Somebyte = 5;
      dog1.Somenull = null;
      dog1.Somedummy = 666;
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

      dog1.Someobj[0] = new Integer(1);
      dog1.Someobj[1] = new String("Hi Gordie");

      dog1.Someobj2[0][0] = new String("0,0");
      dog1.Someobj2[0][1] = new String("0,1");
      dog1.Someobj2[1][0] = new String("1,0");
      dog1.Someobj2[1][1] = new String("1,1");

      // Test of parameterObjects array
      // Add cat5 to our two parameter collections
      // We will prune at cat5, so cat5 and cat6 will not be externalized

      repry = new Repository(env);  // Re-open Repository
      GenSymKey cat5GenSym = repry.put(cat5);   // Store cat5 in DB
      params.put(cat5, cat5GenSym); // identify cat5 as prune point in params (value=GenSymKey)

      repry.put("pooch",dog1, params, props); // Store dog, pruning at cat5 (since cat5 is in params)
      repry.commit();
      repry.close();

      repry = new Repository(env);  // Re-open Repository
      dog2 = (Canine) repry.get("pooch", revParams);
      repry.close();

      if (dog1.equals(dog2))
    System.out.println("[SUCCESS] GENSYMOK - Dogs are equalp after GenSym parameter resolution");
      else System.out.println("[FAILURE] DOGSRDIF - Dogs are different - gensym auto lookup failed");

      if (revParams.get(cat5GenSym) == null) {
    System.out.println("[FAILURE] NOCATGEN - No gensym entry for " + cat5GenSym);
      } else {

    if (cat5.equals(revParams.get(cat5GenSym))) 
      System.out.println("[SUCCESS] PARCATEQ - Tabled cat equal after gensym lookup");
    else System.out.println("[FAILURE] PARCATDIF - Cat in parameter table different");
      }

      System.out.println("[SUCCESS] TESTDONE - Test is done");
      System.exit(0);

    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
    }
    System.exit(0);     // Removing this line causes a coredump in close() in finalize() in GC
  }
}

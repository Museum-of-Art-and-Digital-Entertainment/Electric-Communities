package ec.tests.rep;

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency
import ec.e.rep.*;      // Needed to use Repository
import ec.e.db.RtEncodingParameters; // Needed for parameter collections
import ec.e.db.RtDecodingParameters; // Needed for parameter collections
import java.util.*;     // Needed only for class Enumeration
import ec.tests.rep.Feline; // Needed only for Feline class used in these examples

// To easily run this code, you may want to define
// % alias partst 'java -debug ec.e.start.EBoot ec.e.start.Agency Agent=ec.tests.rep.partst'

eclass partst implements ELaunchable
{
    emethod go (EEnvironment env) {   

      RtEncodingParameters params = new RtEncodingParameters();
      RtDecodingParameters revParams = new RtDecodingParameters();
      RtDecodingParameters revParams2 = new RtDecodingParameters();
      Hashtable props = new Hashtable(10);
 
      try {

    Repository repry = new Repository(env); // If there is a property "repository" use that as path, else use "./repository"

    Feline cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8, cat9;
    Canine dog1, dog2, dog3, dog4, dog5;

    // We use capital first letter for names and all lower case for keys

    cat1 = new Feline(null, "Cat1");
    if (cat1 == null) System.out.println("[FAILURE] CATLESS - Could not create cat");

    repry.put("cat1",cat1); // Store Cat1 under name cat1
    repry.commit();     // Write out the batch of changes we've done.
    repry.close();      // Close the Repository.

    repry = new Repository(env); // Re-open the Repository
    cat2 = (Feline)repry.get("cat1");   // Get it back from Repository

    // Check the results and report.

    if (cat2 == null) System.out.println("[FAILURE] CATNULL - get() returned null");
    else if (! cat2.equals(cat1)) System.out.println("[FAILURE] CATSNEQ - get() returned non-equal data object");
    else System.out.println("[SUCCESS] CATRETR - get() returned correct data");

    // So far, so good. Now create a cat with a chain of siblings
    // so wqe can test pruning with a parameter object.

    cat6 = new Feline(null, "Cat6");    // Last one has no sibling
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
    dog1.cat = cat3;
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

    // Externalize the dog

    repry.put("dog1",dog1, params, props);
    System.out.println("[SUCCESS] STOREDOBJ - Successfully stored object");
    repry.commit();
    repry.close();

    repry = new Repository(env);

    // internalize a dog

    dog2 = (Canine)repry.get("dog1", revParams);
    System.out.println("[SUCCESS] GOTOBJ - Got object for dog2Key");

    // Compare the original dog with the one we internalized

    if (dog2.equals(dog1)) System.out.println("[SUCCESS] DOGEQUALP - Dogs are equalp");
    else System.out.println("[FAILURE] DOGSDIF1 - Dogs are different!");

    repry.commit();
    repry.close();

    // Test of parameterObjects array
    // Add cat5 to our two parameter collections
    // We will prune at cat5, so cat5 and cat6 will not be externalized
    // Note - we leave revParams2 as an empty collection

    params.put(cat5,"parameter-cat");
    revParams.put("parameter-cat",cat5);

    repry = new Repository(env);    // Re-open Repository
    repry.put("dog3",dog1, params, props); // Store dog, pruning at cat5 (since cat5 is in params)

    // Internalize the dog, using the revParams to patch up the pruned cat sibling chain

    dog4 = (Canine) repry.get("dog3", revParams);
    System.out.println("[SUCCESS] GOTDOG4 - Got object for dog4Key");

    if (dog1.equals(dog4)) System.out.println("[SUCCESS] DOGEQ2 - Dogs are equalp");
    else System.out.println("[FAILURE] DOGSDIF2 - Error *** Dogs are different!");

    // Internalize the dog again but this time use an empty revparams array
    // This should result in an error throw since we are missing a value for "parameter-cat"
    // Prepare the cat cat4 for this (since it will blow up when we are decoding its sibling)

    dog5 = (Canine) repry.get("dog3", revParams2); //  This should blow up since revParams2 is empty

    if (dog1.equals(dog5)) System.out.println("[FAILURE] FAIDOGEQ - Dogs are equalp when they should not be");
    else System.out.println("[SUCCESS] DOGSRDIF - Dogs are different, which was expected");

    System.out.println("[SUCCESS] TESTDONE - Test is done");
    System.exit(0);

      } catch (Exception e) {
    System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
    e.printStackTrace();
      }
      System.exit(0);       // Removing this line causes a coredump in close() in finalize() in GC
    }
  }

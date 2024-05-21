package ec.tests.rep;

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency
import ec.e.rep.*;      // Needed to use Repository
import java.util.*;     // Needed only for class Enumeration
import ec.tests.rep.Feline; // Needed only for Feline class used in these examples

/* You can grep for the first word on the next line in all testcases and feed it all to a shell
define alias ptst 'java -debug ec.e.start.EBoot ec.tests.rep.ptst repository=foo\;fie'
*/

eclass ptst implements ELaunchable
{
    emethod go (EEnvironment env) {   
    try {
      Repository repry = new Repository("fie",true,false,null);
      Feline cat1 = new Feline(null,"Maurice"); // Make a cat without a sibling. Name is Maurice.
      if (cat1 == null) System.out.println("[FAILURE] CATLESS - Could not create cat");

      repry.put("cat1",cat1);   // Store under name cat1
      repry.commit();       // Write out the batch of changes we've done.
      repry.close();        // Close the Repository.

      // Re-open the Repository fie and another one called foo ahead of it,
      // as specified by the repository env variable in alias abovea

      Repository repry1 = new Repository(env); 
      Feline cat2 = (Feline)repry1.get("cat1"); // Get it back from Repository fie

      Feline cat3 = new Feline(cat2,"cat3");
      repry1.put("cat3",cat3);
      repry1.commit();
      repry1.close();       // Close Repository when we're done.

      // Check the results and report.

      if (cat2 == null) System.out.println("[FAILURE] CATNULL - get() returned null");
      else if (! cat2.equals(cat1)) System.out.println("[FAILURE] CATSNEQ - get() returned non-equal data object");
      else System.out.println("[SUCCESS] CATRETR - get() returned correct data");
    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
    }
    //    System.exit(0);       // Removing this line causes a coredump in close() in finalize() in GC
  }
}

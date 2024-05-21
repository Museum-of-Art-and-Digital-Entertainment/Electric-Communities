package ec.tests.rep;

import ec.e.start.ELaunchable;      // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency
import ec.e.rep.*;      // Needed to use Repository
import java.util.*;     // Needed only for class Enumeration
import ec.tests.rep.Feline; // Needed only for Feline class used in these examples

// You may want to define alias rtst 'java -debug ec.e.start.EBoot ec.e.start.Agency Agent=ec.tests.rep.rtst'

eclass rtst implements ELaunchable
{
    emethod go (EEnvironment env) {   
    try {
      Repository repry = new Repository(env); // If there is a property "repository" use that as path, else use "./repository"
      Feline cat1 = new Feline(null,"Maurice"); // Make a cat without a sibling. Name is Maurice.
      if (cat1 == null) System.out.println("[FAILURE] CATLESS - Could not create cat");

      repry.put("cat1",cat1);   // Store under name cat1
      repry.commit();       // Write out the batch of changes we've done.
      repry.close();        // Close the Repository.

      Repository repry1 = new Repository(env); // Re-open the Repository
      Feline cat2 = (Feline)repry1.get("cat1"); // Get it back from Repository
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

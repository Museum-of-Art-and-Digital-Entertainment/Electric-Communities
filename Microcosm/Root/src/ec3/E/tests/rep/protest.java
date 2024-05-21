package ec.tests.rep;

import ec.e.start.ELaunchable;    // Needed for Agency (calls go())
import ec.e.start.EEnvironment;   // Needed for Agency
import ec.e.rep.*;      // Needed to use Repository
import java.util.*;     // Needed only for class Enumeration
import ec.tests.rep.Feline; // Needed only for Feline class used in these examples

// You may want to define alias rtst 'java -debug ec.e.start.EBoot ec.e.start.Agency Agent=ec.tests.rep.protest'

eclass protest implements ELaunchable
{
    emethod go (EEnvironment env) {   
 
    Hashtable props = new Hashtable(10);
    props.put("Foo","Bar"); // A property that we want to make available to Bovine.Encode()

    try {
      Repository repry = new Repository(env); // If there is a property "repository" use that as path, else use "./repository"
      Bovine cow1 = new Bovine(null,"Bessie");  // Make a cow without a sibling. Name is Bessie
      if (cow1 == null) System.out.println("[FAILURE] COWLESS - Could not create cow");

      repry.put("cow1",cow1,null,props); // Store under name cow1. Supply the properties table to EncodingManager
      repry.commit();       // Write out the batch of changes we've done.
      repry.close();        // Close the Repository.

      Repository repry1 = new Repository(env); // Re-open the Repository
      Bovine cow2 = (Bovine)repry1.get("cow1"); // Get it back from Repository
      repry1.close();       // Close Repository when we're done.

      // Check the results and report.

      if (cow2 == null) System.out.println("[FAILURE] COWNULL - get() returned null");
      else if (! cow2.equals(cow1)) System.out.println("[FAILURE] COWSNEQ - get() returned non-equal data object");
      else System.out.println("[SUCCESS] COWRETR - get() returned correct data");
    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
    }
    System.exit(0);     // Removing this line causes a coredump in close() in finalize() in GC
  }
}

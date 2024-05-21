package ec.tests.rep;

import ec.e.start.ELaunchable;    // Needed for startup (calls go())
import ec.e.start.EEnvironment;   // Needed only for EEnvironment declaration
import ec.e.rep.*;      // Needed to use Repository
import java.util.*;     // Needed only for class Enumeration

// You may want to define alias rls 'java -debug ec.e.start.EBoot ec.e.start.Agency Agent=ec.tests.rep.rls'

eclass rls implements ELaunchable
{
  emethod go (EEnvironment env) {   
    try {
      Repository repry = new Repository(env); // If there is a property "repository" use that as path, else use "./repository"
      Enumeration keys = repry.elements(); // elements() may not be included in released version of Repository

      while (keys.hasMoreElements()) {
    Object key = keys.nextElement();
    Object value = repry.get(key);
    System.out.println(key + "\t" + value);
      }
      repry.close();
    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
    }
    System.exit(0);
  }
}

package ec.tests.insp;

import ec.e.inspect.*;
import ec.ui.IFCInspectorUI;
import ec.ifc.app.*;
import java.util.*;
import netscape.application.*;
import ec.e.run.RtRun;
import ec.tests.rep.Canine;
import ec.tests.rep.Feline;

/**

 * Class to test the Inspector. 

 */

class inspectortest  {

    public static void main(String[] args) {
        ec.ui.IFCInspectorUI.start("full"); // Start inspector and IFC in its own thread

        Object dog1 = new Canine(); // Create a test object
        ec.e.inspect.Inspector.gather(dog1, "Animals", "dog1");

        Feline cat2 = new Feline(null,"Cat2");
        Feline cat1 = new Feline(cat2,"Cat1"); // Sibling of cat2

        ec.e.inspect.Inspector.gather(cat1, "Animals", "cat1");

        Hashtable sounds = new Hashtable(10);
        sounds.put("Cow", "Moo");
        sounds.put("Cat", "Meow");
        sounds.put("Dog", "Arf");
        sounds.put("Sheep", "Baa");

        ec.e.inspect.Inspector.gather(sounds, "Animals", "sounds");
        ec.e.inspect.Inspector.gather(sounds, "sounds");

        String[] birds = {"goose","dove","falcon","eagle","hen"};
        ec.e.inspect.Inspector.gather(birds,"Animals","birds");

        Vector primates = new Vector();
        primates.addElement("Chimp");
        primates.addElement("Baboon");
        primates.addElement("Gorilla");
        primates.addElement("Homo Sapiens");

        ec.e.inspect.Inspector.gather(primates,"Animals", "primates");

        if (RtRun.theOne() == null) {
            new RtRun(new Object()); // Create a run queue
        }
    }
}


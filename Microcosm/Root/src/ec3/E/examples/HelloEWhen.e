/*
  HelloEWhen.e
  v 1.1
  Really simple ewhen test
  Jan 25, 1996
  Arturo Bejar
  Copyright 1996 Electric Communities, all rights reserved.

  Program output:

  Sent StartHelloEWhenInstance
  In HelloEWhenObj
  In HelloEWhenObj2
  Hola Mundo with 7
    
*/

package ec.examples.heew;

import ec.e.lang.EInteger;
import ec.e.cap.EEnvironment;
import ec.e.start.ELaunchable;

public class HelloEWhen implements ELaunchable
{
    public void go(EEnvironment env) {
        HelloEWhenObj theInstance = new HelloEWhenObj();
        theInstance <- StartHelloEWhenInstance();
        System.out.println("Sent StartHelloEWhenInstance");
    }
}

eclass HelloEWhenObj
{
    EInteger anEInteger;

    emethod StartHelloEWhenInstance() {
    HelloEWhenObj2 first = new HelloEWhenObj2();

        System.out.println("In HelloEWhenObj");
    
        first <- hello(&anEInteger);

        ewhen anEInteger (int thisInt) {
            System.out.println("Hola Mundo with " + thisInt);
        }
    }
}


eclass HelloEWhenObj2
{
    emethod hello(EResult putEIntHere) {
        System.out.println("In HelloEWhenObj2");
                putEIntHere <- forward( new EInteger(7) );
    }
}



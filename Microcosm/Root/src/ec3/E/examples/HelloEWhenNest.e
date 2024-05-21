/*
   HelloEWhenNest.e
   v 1.1
   Really simple ewhen test
   Jan 25, 1996
   Arturo Bejar
   Copyright 1996 Electric Communities, all rights reserved.

   Program output:

   Sent StartHelloEWhenInstance
   Outer Hola Mundo with 7
   Inner Hola Mundo with 67
   and from outer 7

*/

package ec.examples.heewn;

import ec.e.lang.EInteger;
import ec.e.cap.EEnvironment;
import ec.e.start.ELaunchable;

public class HelloEWhenNest implements ELaunchable
{
    public void go(EEnvironment env) {
        HelloENestObj theInstance = new HelloENestObj();
    theInstance <- StartHelloEWhenInstance();
        System.out.println("Sent StartHelloEWhenInstance");
    }
}

eclass HelloENestObj
{
    EInteger unEInteger;
    EInteger anEInteger;

    emethod StartHelloEWhenInstance() {
    HelloENestObj2 first = new HelloENestObj2();
    first <- hello(&anEInteger, &unEInteger);
    
    ewhen anEInteger (int thisInt) {
        System.out.println("Outer Hola Mundo with " + thisInt);
    
        ewhen unEInteger (int anotherInt) {
        System.out.println("Inner Hola Mundo with " + anotherInt);
        System.out.println("and from outer " + thisInt);
        }
    }
    }
}

eclass HelloENestObj2
{
    emethod hello(EResult putEIntHere, EResult putEIntHereToo) {
    putEIntHere <- forward(new EInteger(7));
    putEIntHereToo <- forward(new EInteger(67));
    }
}



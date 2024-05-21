/*
   HelloEWhenInv.e
   v 1.1
   Another simple ewhen test, in this case we pass an unforwarded E
   object reference and then forward it.
   Jan 25, 1996
   Arturo Bejar
   Copyright 1996 Electric Communities, all rights reserved.

   Program output:

   Sent StartHelloEWhenInstance
   In HelloEWhenInvObj after forward
   In HelloEWhenInvObj2
   Hola Mundo with 7
*/

package ec.examples.heewi;

import ec.e.lang.EInteger;
import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;

public class HelloEWhenInv implements ELaunchable
{
    public void go(EEnvironment env) {
        HelloEWhenInvObj theInstance = new HelloEWhenInvObj();
        theInstance <- StartHelloEWhenInstance();
        System.out.println("Sent StartHelloEWhenInstance");
    }
}

eclass HelloEWhenInvObj
{
    EInteger anEInteger;

    emethod StartHelloEWhenInstance() {
        HelloEWhenInvObj2 first = new HelloEWhenInvObj2();

        first <- hello(anEInteger);

        &anEInteger <- forward(new EInteger(7));

        System.out.println("In HelloEWhenInvObj after forward");
    }
}


eclass HelloEWhenInvObj2
{
    emethod hello(EInteger putEIntHere) {
        System.out.println("In HelloEWhenInvObj2");

        ewhen putEIntHere (int thisInt) {
            System.out.println("Hola Mundo with " + thisInt);
        }
    }
}


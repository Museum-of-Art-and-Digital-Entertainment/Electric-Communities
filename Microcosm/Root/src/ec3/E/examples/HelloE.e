/* 
    HelloE.e
    v 0.1 
    Really simple hello world to get to compile
    Jan 1, 1996
    Arturo Bejar Copyright Electric Communities
*/


package ec.examples.he;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;

public eclass HelloE implements ELaunchable
{
    emethod go(EEnvironment env) {
    
    HelloWorldDoohickey first = new HelloWorldDoohickey();
    
    first <- hello();
    }
}

eclass HelloWorldDoohickey 
{
    emethod hello() {
    System.out.println("Hola Mundo");
    }
}

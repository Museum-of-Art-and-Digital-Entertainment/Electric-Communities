package ec.pl.examples.simple;

import ec.pl.runtime.*;
import ec.e.start.ELaunchable;
import ec.e.cap.*;
import ec.e.net.*;

public eclass Simple implements Agent
{
  emethod go (EEnvironment env) {
    
    System.out.println("In Simple.go()");

    // Here, if we were instructed to do so, we would typically attempt
    // to connect to this unum elsewhere.  In this example, we don't care.

    Unum simple = (Unum) ui$_uiSimple_.createUnum();
    // FYI, arguments sent to createUnum() are the same used by the 
    // presence init methods.  In this case, there are none.

    simple<-acceptMessageAndDoSomething(true);

    System.out.println("End of Simple.go()");

  }
}

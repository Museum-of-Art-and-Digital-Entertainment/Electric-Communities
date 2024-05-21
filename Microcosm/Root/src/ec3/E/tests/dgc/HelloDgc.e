/* 
  HelloDgc.e -- Version 0.2 -- Simple comm test

  Arturo Bejar & Chip Morningstar
  Electric Communities
  19-February-1996

  Copyright 1996 Electric Communities, all rights reserved.
*/

package ec.tests.dgc;

eimport ec.e.comm.EConnector;
eimport ec.e.comm.ERunnable;

import ec.e.comm.*;
import ec.runtime.DgcDBG;
import ec.runtime.Dgct;
import ec.runtime.RRNotify;
import java.lang.*;
import ec.runtime.BRMsgQueue;
public class HelloDgc
{
    static{
    	System.loadLibrary("jruntime");
    	System.out.println("######################################Hey, I'm gittin loaded");
  	}

    public static void main(String args[]) {
		RtRun.tr.traceMode(true);
    	Dgct.t.traceMode(true);
    	HelloSender foo = new HelloSender();
        RtLauncher.launch(new HelloReceiver(), args);
        RtLauncher.launch(foo, args);
    }
}

eclass HelloSender eimplements ERunnable
{
    emethod go(RtEEnvironment env) {
        HelloReceiver otherGuy;
        etry {
            env.getConnector() <- lookup("localhost", "Doohickey", &otherGuy);
        } ecatch (RtDirectoryEException e) {
            System.out.println("HelloSender catches exception: " +
                               e.getMessage());
            RtRun.shutdown();
        }
        otherGuy <- hello();
   }
}

eclass HelloReceiver eimplements ERunnable
{
    emethod go(RtEEnvironment env) {
        env.getRegistrar().register("Doohickey", this);
    }

    emethod hello() {
		System.out.println("");
		System.out.println("");
        System.out.println("Hola Mundo");
		System.out.println("");
		System.out.println("");
		RRNotify.per_item();
		RRNotify.per_item();
		System.gc();
		RRNotify.per_item();
		System.gc();
		System.out.println("");
		RRNotify.per_item();
		
		System.out.println("++++++++++++++++++++++++++++++");
 		BRMsgQueue.deliver();
		System.out.println("++++++++++++++++++++++++++++++");
		System.out.println("");
		
		System.gc();
		RRNotify.per_item();
		System.gc();
		RRNotify.per_item();
		RRNotify.per_item();
		System.runFinalization();
        RtRun.shutdown();
    }
}



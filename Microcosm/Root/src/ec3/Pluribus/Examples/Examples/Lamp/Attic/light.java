package ec.plexamples.light;

// Lamp unum test routine.
// To activate, type:
// 		mj -- to make the files
// setenv CLASSPATH .:$CLASSPATH -- to augment your CLASSPATH to point at the .classes
// 		javaec ec.plgen.Light   -- to create the initial lamp unum
// to create a local presence of a lamp unum on another machine, type:
//		javaec ec.plgen.Light who=servername

import java.io.*;
import java.lang.*;
import ec.e.comm.*;
import ec.plgen.*;

public class Light extends Agency
{
    public static void main(String argv[]) {
    	Agency.setTheAgency(new Light());
		RtLauncher.launch(new EAgency(), argv);
    }
    
	protected void startup (RtEEnvironment env) {
        String connectTo = env.getProperty("who");
        if(connectTo != null) {
			Unum otherGuy;
			env.getConnector().lookup(connectTo, "Bulb1", &otherGuy);
			System.out.println("About to call toggler");
			otherGuy <- toggle();
		} else
			Unum conv = ui$_Light_ui_.createUnum(env, "Bulb1", "");
	}    
}


package ec.tests.rep;

import ec.ifc.app.*;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.tests.rep.Feline;
import ec.tests.rep.Canine;
import ec.e.file.*;
import ec.e.rep.*;

/* 
   To run this test, type

   java ec.e.start.EBoot ec.tests.rep.nrep

   to a Windows bash shell.
   You ocould remove all inspector code if you wanted.
   */
 

eclass nrep implements ELaunchable {
    emethod go(EEnvironment env) {
        RepCapTester tester = new RepCapTester(env);
        tester <- go();
    }
}

eclass RepCapTester {
    private EEnvironment myEnv;
    
    Canine makeDog(String sound) {

        Canine dog = new Canine();

        dog.whatISay = sound;

        dog.Someboolean = true;
        dog.Somechar = 'X';
        dog.Somelong = 1234;
        //        dog.Somedouble = 3.14159;
        dog.Somefloat = (float)41.999;
        dog.cat = null;
        dog.Someshort = 567;
        dog.Someint = 9876;
        dog.Somebyte = 5;
        dog.Somenull = null;
        dog.Somedummy = 666;
        dog.nums[0] = 11111111;
        dog.nums[1] = 22222222;
        dog.nums[2] = 33333333;

        dog.ch[0]='M';
        dog.ch[1]='e';
        dog.ch[2]='o';
        dog.ch[3]='w';

        dog.smurf[0] = 50;
        dog.smurf[1] = 60;
        dog.smurf[2] = 70;
        dog.smurf[3] = 80;
        dog.smurf[4] = 90;
        dog.flags[0] = false;
        dog.flags[1] = true;
        dog.flags[2] = true;
        dog.flags[3] = false;
        dog.flags[4] = true;
        dog.flags[5] = false;
        dog.guy[0] = 4000;
        dog.guy[1] = 5000;
        dog.guy[2] = 6000;

        dog.furr[0] = (float) 4.0004;
        dog.furr[1] = (float) 5.0005;
        dog.furr[2] = (float) 6.0006;

        dog.Someobj[0] = new Integer(1);
        dog.Someobj[1] = new String("Hi Gordie");

        dog.Someobj2[0][0] = new String("0,0");
        dog.Someobj2[0][1] = new String("0,1");
        dog.Someobj2[1][0] = new String("1,0");
        dog.Someobj2[1][1] = new String("1,1");

        return dog;
    }

    public RepCapTester(EEnvironment env) {
        myEnv = env;
        EStdio.err().println("in constructor");
    }

    emethod go() {
        EStdio.err().println("in go");
        Vat vat = myEnv.vat();

        ECApplication app = null;

        // To run without inspector, comment out next two lines.

        // app = new ECApplication(); // Create the IFC app
        //        ec.ui.IFCInspectorUI.initialize("full"); // Use IFC for Inspector UI

        try {
            EStdio.err().println("Here");
            EDirectoryRootMaker rootMaker = new EDirectoryRootMaker(vat);
            EEditableDirectory rootDir =
              rootMaker.makeDirectoryRoot("/home/kari/testbed");
            EEditableFile anEditFile = rootDir.mkfile("repository");
            // EFileEditor anEditor = anEditFile.editor();
            Repository rep = new Repository(anEditFile,true);

            Canine dog1 = makeDog("Woof");
            //            ec.e.inspect.Inspector.gather(dog1, "dog1");
            rep.put("dog1", dog1);
            Canine dog2 = (Canine) rep.get("dog1");
            //            ec.e.inspect.Inspector.gather(dog2, "dog2");

            if (dog2 == null) {
                EStdio.err().println("[FAILURE] DOG2NUL - Dog2 retrieved as null");
            }


            if (dog1.equals(dog2)) EStdio.err().println("[SUCCESS] DOGSREQL - Dogs are equalp");
            else EStdio.err().println("[FAILURE] DOGSRDIF - Dogs are different");
            rep.close();
            //            ec.e.inspect.Inspector.inspect(null,null); // Force inspector window to appear
            if (app != null) app.run();

        } catch (Exception e) {
            EStdio.err().println("[FAILURE] UNXTHROW - Unexpected throw");
            e.printStackTrace(EStdio.err());
        }
    }
}

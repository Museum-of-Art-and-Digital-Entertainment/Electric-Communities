
package ec.tests.db;

import ec.plgen.Unum;
import ec.plgen.Agent;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.net.ERegistrar;
import java.util.*;
import java.io.IOException;
import ec.e.db.*;

/*
java ec.e.start.EBoot ec.plgen.Agency Agent=ec.tests.db.StreamTest
*/

public eclass StreamTest implements ELaunchable {

  private final String dbname  = "testDB";

  emethod go (EEnvironment env) {   

    try {

    System.out.println("About to create PObjDB");

    PObjDB btdb = new PObjDB(dbname);
//    PObjDB btdb = new PObjDB();

    System.out.println("DB created");
   
    Feline cat1, cat2, cat3;
    Canine dog1, dog2;

    cat1 = new Feline();
    cat1.whatISay = "Meow";
    System.out.println("The cat is " + cat1);

    RtStreamKey catKey = btdb.put(cat1);
    System.out.println("The cat key is " + catKey);

    btdb.closeDB();

    System.out.println("Closed btdb");

    PObjDB db1 = new PObjDB(dbname);
    PObjDB db2 = new PObjDB(db1);
    
    cat2 = (Feline) db2.get(catKey);
    System.out.println("The cat from db2 is " + cat2);

    dog1 = new Canine();

    dog1.whatISay="Woof";
    dog1.Dboolean = true;
    dog1.Dchar = 'X';
    dog1.Dlong = 1234;
    dog1.Ddouble = 3.14159;
    dog1.Dfloat = (float)41.999;
    dog1.cat = cat1;
    dog1.Dshort = 567;
    dog1.Dint = 9876;
    dog1.Dbyte = 5;
    dog1.Dnull = null;
    dog1.Ddummy = 666;
    dog1.nums[0] = 11111111;
    dog1.nums[1] = 22222222;
    dog1.nums[2] = 33333333;

    dog1.ch[0]='M';
    dog1.ch[1]='e';
    dog1.ch[2]='o';
    dog1.ch[3]='w';

    dog1.smurf[0] = 50;
    dog1.smurf[1] = 60;
    dog1.smurf[2] = 70;
    dog1.smurf[3] = 80;
    dog1.smurf[4] = 90;
    dog1.flags[0] = false;
    dog1.flags[1] = true;
    dog1.flags[2] = true;
    dog1.flags[3] = false;
    dog1.flags[4] = true;
    dog1.flags[5] = false;
    dog1.guy[0] = 4000;
    dog1.guy[1] = 5000;
    dog1.guy[2] = 6000;

    dog1.furr[0] = (float) 4.0004;
    dog1.furr[1] = (float) 5.0005;
    dog1.furr[2] = (float) 6.0006;

    dog1.Dobj[0] = new Integer(1);
    dog1.Dobj[1] = new String("Hi Gordie");

    dog1.Dobj2[0][0] = new String("0,0");
    dog1.Dobj2[0][1] = new String("0,1");
    dog1.Dobj2[1][0] = new String("1,0");
    dog1.Dobj2[1][1] = new String("1,1");

//  System.out.println(dog1.Dobj[0]);
//  System.out.println(dog1.Dobj[1]);

    RtStreamKey dogKey = db2.put(dog1);
    db2.put("Smurgle", dogKey);
    System.out.println("Successfully stored object");
    db2.commit();
    db2.closeDB();
    db1.closeDB();

    PObjDB db3 = new PObjDB(dbname);
    RtStreamKey dog2Key = db3.get("Smurgle");
    dog2 = (Canine) db3.get(dog2Key);
    System.out.println("Got object for dog2Key");

    System.out.println(dog2.whatISay);
    System.out.println(dog2.Dboolean);
    System.out.println(dog2.Dchar);
    System.out.println(dog2.Dlong);
    System.out.println(dog2.Dfloat);
    System.out.println(dog2.Ddouble);
    System.out.println(dog2.cat);
    System.out.println(dog2.Dshort);
    System.out.println(dog2.Dint);
    System.out.println(dog2.Dbyte);
    System.out.println(dog2.Dnull);
    System.out.println(dog2.Ddummy);
    System.out.println(dog2.nums[0]);
    System.out.println(dog2.nums[1]);
    System.out.println(dog2.nums[2]);
    System.out.println(dog2.smurf[0]);
    System.out.println(dog2.smurf[1]);
    System.out.println(dog2.smurf[2]);
    System.out.println(dog2.smurf[3]);
    System.out.println(dog2.smurf[4]);
    System.out.println(dog2.ch[0]);
    System.out.println(dog2.ch[1]);
    System.out.println(dog2.ch[2]);
    System.out.println(dog2.ch[3]);
    System.out.println(dog2.flags[0]);
    System.out.println(dog2.flags[1]);
    System.out.println(dog2.flags[2]);
    System.out.println(dog2.flags[3]);
    System.out.println(dog2.flags[4]);
    System.out.println(dog2.flags[5]);
    System.out.println(dog2.guy[0]);
    System.out.println(dog2.guy[1]);
    System.out.println(dog2.guy[2]);
    System.out.println(dog2.furr[0]);
    System.out.println(dog2.furr[1]);
    System.out.println(dog2.furr[2]);

    System.out.println(dog2.Dobj[0]);
    System.out.println(dog2.Dobj[1]);

    System.out.println(dog2.Dobj2[0][0]);
    System.out.println(dog2.Dobj2[0][1]);
    System.out.println(dog2.Dobj2[1][0]);
    System.out.println(dog2.Dobj2[1][1]);


    RtDBViewLimiter readOnlyView = new RtDBViewLimiter(db3, true, false, false, null, null);
    System.out.println("Reading from readOnlyView");
    dog2 = (Canine) readOnlyView.get(dog2Key);
    System.out.println("Attempting to write to readOnlyView");
    System.out.println("(This should produce a DBAccessException)");
   readOnlyView.put(new String("FurrPurr"));
    System.out.println("Security failure");

    
    } catch (Exception e) {e.printStackTrace();}
    System.exit(0);
    }
}

public class Feline implements RtCodeable {
    public String whatISay;

    public Feline() {
        whatISay ="";
    }

    public void encode(RtEncoder coder) {
    try {
        coder.writeUTF(whatISay);
    } catch (Exception e) {e.printStackTrace();}
    }

    public  Object decode(RtDecoder coder) {
    try {
        whatISay = coder.readUTF();
    } catch (Exception e) {e.printStackTrace();}

    return(this);

    }
  public String classNameToEncode(RtEncoder coder) {
    return getClass().getName();
  }
}

public class Animal {
    public String whatISay;
    public boolean Dboolean;
}

public class Canine extends Animal {
    public char Dchar;
    public long Dlong;
    public float Dfloat;
    public double Ddouble;
    public Feline cat;
    public short Dshort;
    public float furr[] = new float[3];
    public int Dint;
    public byte Dbyte;
    public boolean flags[] = new boolean[6];
    public Object Dnull;
    public long nums[] = new long[3];
    public char ch[] = new char[4];
    public byte smurf[] = new byte[5];
    public short guy[] = new short[3];
    public int Ddummy;
    public Object Dobj[] = new Object[2];
    public Object Dobj2[][] = new Object[2][2];
    public Canine() {
        System.out.println("Canine made");
    }
}

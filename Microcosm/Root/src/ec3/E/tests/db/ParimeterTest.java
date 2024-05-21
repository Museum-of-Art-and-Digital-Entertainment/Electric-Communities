package ec.tests.db;

import ec.plgen.Unum;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.net.ERegistrar;
import java.util.*;
import java.io.IOException;
import ec.e.db.*;
import ec.util.Equals;

/*
java ec.e.start.EBoot ec.plgen.Agency Agent=ec.tests.db.ParimeterTest
*/

public eclass ParimeterTest implements ELaunchable
{
  private final String dbname  = "testDB";

  emethod go (EEnvironment env) {   

    RtEncodingParameters params = new RtEncodingParameters(10);
    RtDecodingParameters revParams = new RtDecodingParameters(10);
    RtDecodingParameters revParams2 = new RtDecodingParameters(10);
    Hashtable props = new Hashtable(10);
 
    try {

        //      PObjDB btdb = new PObjDB(dbname);

        Repository rep = 

      System.out.println("[SUCCESS] POBJDBCRE - DB created");
   
      Feline1 cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8, cat9;
      Canine1 dog1, dog2, dog3, dog4, dog5;

      cat1 = new Feline1(null, "Cat1");
      cat1.whatISay = "Meow";

      RtStreamKey catKey = btdb.put(cat1, params, props);
      btdb.put("Fritz", catKey, params, props);

      btdb.closeDB();

      System.out.println("[SUCCESS] DBCLOSE - Closed btdb");

      PObjDB db1= new PObjDB(dbname);
      PObjDB db2 = new PObjDB(db1);
    
      cat2 = (Feline1) db2.get(catKey, revParams);
      System.out.println("[SUCCESS] DBCAT2 - Internalized cat2");

      cat6 = new Feline1(null, "Cat6");
      cat6.whatISay = "meoow";

      cat5 = new Feline1(cat6,"Cat5");
      cat5.whatISay = "meoww";

      cat4 = new Feline1(cat5, "Cat4");
      cat4.whatISay = "mmmeow";

      cat3 = new Feline1(cat4, "Cat3");
      cat3.whatISay = "meowww";

      // In experiment two we will prune at cat5, so cat5 and cat6 will not be externalized

      dog1 = new Canine1();

      dog1.whatISay="Woof";
      dog1.Dboolean = true;
      dog1.Dchar = 'X';
      dog1.Dlong = 1234;
      dog1.Ddouble = 3.14159;
      dog1.Dfloat = (float)41.999;
      dog1.cat = cat3;
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

      // Externalize the dog

      RtStreamKey dogKey = db2.put(dog1, params, props);
      db2.put("Smurgle", dogKey, params, props);
      System.out.println("[SUCCESS] STOREDOBJ - Successfully stored object");
      db2.commit();
      db2.closeDB();
      db1.closeDB();

      PObjDB db3 = new PObjDB(dbname);

      // internalize a dog

      RtStreamKey dog2Key = db3.get("Smurgle", revParams);
      dog2 = (Canine1) db3.get(dog2Key, revParams);
      System.out.println("[SUCCESS] GOTOBJ - Got object for dog2Key");

      // Compare the original dog with the one we internalized

      if (dog2.equals(dog1)) System.out.println("[SUCCESS] DOGEQUALP - Dogs are equalp");
      else System.out.println("[FAILURE] DOGSDIF1 - Dogs are different!");

      // Test of parameterObjects array
      // Add cat5 to our two parameter hashtables
      // We will prune at cat5, so cat5 and cat6 will not be externalized

      params.put(cat5,"parameter-cat");
      revParams.put("parameter-cat",cat5);

      // Note - we leave revParams2 as an empty hash table

      RtStreamKey dog3Key = db3.put(dog1, params, props);
      db3.put("Glufs", dog3Key, params, props);
      System.out.println("[SUCCESS] STORED3 - Successfully stored object in db3");
      db3.commit();
      db3.closeDB();

      db3 = new PObjDB(dbname); // Re-open the DB

      // Internalize the dog, using the revParams to patch up the pruned cat sibling chain

      RtStreamKey dog4Key = db3.get("Glufs", revParams);
      dog4 = (Canine1) db3.get(dog4Key, revParams);
      System.out.println("[SUCCESS] GOTDOG4 - Got object for dog4Key");

      if (dog1.equals(dog4)) System.out.println("[SUCCESS] DOGEQ2 - Dogs are equalp");
      else System.out.println("[FAILURE] DOGSDIF2 - Error *** Dogs are different!");

      // Internalize the dog again but this time use an empty revparams array
      // This should result in an error throw since we are missing a value for "parameter-cat"
      // Prepare the cat cat4 for this (since it will blow up when we are decoding its sibling)

      RtStreamKey dog5Key = db3.get("Glufs", revParams2);
      dog5 = (Canine1) db3.get(dog5Key, revParams2); //  This should blow up

      if (dog1.equals(dog5)) System.out.println("[FAILURE] FAIDOGEQ - Dogs are equalp when they should not be");
      else System.out.println("[SUCCESS] DOGSRDIF - Dogs are different, which was expected");

      System.out.println("[SUCCESS] TESTDONE - Test is done");
      System.exit(0);

    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
      System.exit(0);
    }
  }
}


public class Feline1 implements RtCodeable {
  public String whatISay;
  public Feline1 sibling;
  public String catsName;

  public Feline1() {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = null;
  }

  public Feline1(Feline1 aSibling) {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = aSibling;
  }

  public Feline1(Feline1 aSibling, String name) {
    whatISay ="";
    catsName = name;
    sibling = aSibling;
  }

  public void encode(RtEncoder coder) {
    try {
      coder.writeUTF(whatISay);
      coder.writeUTF(catsName);
      coder.encodeObject(sibling);
    } catch (Exception e) {e.printStackTrace();}
  }

  public  Object decode(RtDecoder coder) {
    try {
      whatISay = coder.readUTF();
      catsName = coder.readUTF();
      sibling = (Feline1)coder.decodeObject();
    } catch (Exception e) {
      System.out.println("[SUCCESS] FELDECX Feline1 decode() caught an exception");
    }
    return(this);
  }

  public String classNameToEncode(RtEncoder coder) {
    return getClass().getName();
  }

   public String toString() {
     return catsName;
   }

  //   public boolean equals(Object o) {
  //     return false;
  //   }

  public boolean equals(Feline1 x) {
    if (x == null) return false;
    if (! catsName.equals(x.catsName)) return false;
    if (! whatISay.equals(x.whatISay)) return false;
    if (! ((sibling == null) && (x.sibling == null))) {
      if (sibling != null) {
    if (! sibling.equals(x.sibling)) return false;
      }
      else return false;    // our sibling was null but x.sibling wasn't
    }
    return true;
  }
}

  public class Animal1 {
    public String whatISay;
    public boolean Dboolean;

    //    public boolean equals(Object o) {
    //      return false;
    //    }

    public boolean equals (Animal1 x) {
      if (! (x instanceof Animal1)) return false;
      if (! (x.Dboolean != Dboolean)) return false;
      if (! (x.whatISay.equals(whatISay))) return false;
      return true;
    }
  }


  public class Canine1 extends Animal1 {
    public char Dchar;
    public long Dlong;
    public float Dfloat;
    public double Ddouble;
    public Feline1 cat;
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
    public Canine1() {
      System.out.println("[SUCCESS] CANMADE - Canine1 made");
    }

    //    public boolean equals (Object x) {
    //      return false;
    //    }

    public boolean equals(Canine1 x) {
      if (! (x.Dchar == Dchar)) return false;
      if (! (x.Dlong == Dlong)) return false;
      if (! (x.Dfloat == Dfloat)) return false;
      if (! (x.Ddouble == Ddouble)) return false;
      if (! (x.Dint == Dint)) return false;
      if (! (x.Dbyte == Dbyte)) return false;
      if (! (x.Dnull == Dnull)) return false;
      if (! (x.Ddummy == Ddummy)) return false;
      if (! (x.Dchar == Dchar)) return false;
      if ((x.cat != null) || (cat != null)) // If both are null then we are still fine
    if (x.cat != null) {
      if (! (x.cat.equals(cat))) return false;
    } else return false;    // x.cat was null but cat wasn't
      if (! Equals.equals(x.nums,nums)) return false;
      if (! Equals.equals(x.flags,flags)) return false;
      if (! Equals.equals(x.furr,furr)) return false;
      if (! Equals.equals(x.ch,ch)) return false;
      if (! Equals.equals(x.smurf,smurf)) return false;
      if (! Equals.equals(x.guy,guy)) return false;
      if (! Equals.equals(x.Dobj,Dobj)) return false;
      return true;
    }
  }

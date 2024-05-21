package ec.tests.db;

import ec.plgen.Unum;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.net.ERegistrar;
import java.util.*;
import java.io.IOException;
import ec.e.db.*;
import ec.util.Equals;
import ec.util.ReadOnlyHashtable;

/*
java ec.e.start.EBoot ec.plgen.Agency Agent=ec.tests.db.PropTest
*/

public eclass PropTest implements ELaunchable
{
  private final String dbname  = "testDB";

  emethod go (EEnvironment env) {   

    RtEncodingParameters params = new RtEncodingParameters(10);
    RtDecodingParameters revParams = new RtDecodingParameters(10);
    Hashtable props = new Hashtable(10);
 
    props.put("Foo","Bar");

    Feline2 cat1, cat2, cat3, cat4, cat5, cat6;
    Canine2 dog1, dog2;

    cat6 = new Feline2(null,"Cat6");
    cat6.whatISay = "meoow";

    cat5 = new Feline2(cat6,"Cat5");
    cat5.whatISay = "meoww";

    cat4 = new Feline2(cat5, "Cat4");
    cat4.whatISay = "mmmeow";

    cat3 = new Feline2(cat4, "Cat3");
    cat3.whatISay = "meowww";

    cat2 = new Feline2(cat3, "Cat2");
    cat2.whatISay = "mew";

    cat1 = new Feline2(cat2, "Cat1");
    cat1.whatISay = "Meow";

    try {

      PObjDB db1= new PObjDB(dbname);
    
      dog1 = new Canine2();

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

      // Externalize the dog

      RtStreamKey dogKey = db1.put(dog1, params, props);
      db1.put("Smurgle", dogKey, params, props);
      System.out.println("[SUCCESS] STOREDOBJ - Successfully stored object");
      db1.commit();
      db1.closeDB();

      PObjDB db2 = new PObjDB(dbname);

      // internalize the dog

      RtStreamKey dog2Key = db2.get("Smurgle", revParams);
      dog2 = (Canine2) db2.get(dog2Key, revParams);
      if (dog2 != null)
    System.out.println("[SUCCESS] GOTOBJ - Got object for dog2Key");

      // Compare the original dog with the one we internalized

      if (dog2.equals(dog1)) System.out.println("[SUCCESS] DOGEQUALP - Dogs are equalp");
      else System.out.println("[FAILURE] DOGSDIF1 - Dogs are different!");

      System.out.println("[SUCCESS] TESTDONE - Test is done");
      System.exit(0);

    } catch (Exception e) {
      System.out.println("[FAILURE] UNXTHROW - Unexpected throw");
      e.printStackTrace();
      System.exit(0);
    }
  }
}


public class Feline2 implements RtCodeable {
  public String whatISay;
  public Feline2 sibling;
  public String catsName;

  public Feline2() {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = null;
  }

  public Feline2(Feline2 aSibling) {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = aSibling;
  }

  public Feline2(Feline2 aSibling, String name) {
    whatISay ="";
    catsName = name;
    sibling = aSibling;
  }

  public void encode(RtEncoder coder) {
    ReadOnlyHashtable props = coder.getProperties();

    if (props == null)
      System.out.println("[FAILURE] Could not get a properties hashtable from coder");

    Object v = props.get("Foo");

    if (! "Bar".equals(v))
      System.out.println("[FAILURE] Could not retrieve property from property table");
    else 
      System.out.println("[SUCCESS] Retrieved property from property table");

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
      sibling = (Feline2)coder.decodeObject();
    } catch (Exception e) {
      System.out.println("[FAILURE] FELDUX Feline2 decode() caught an unexpected exception");
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

  public boolean equals(Feline2 x) {
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

  public class Animal2 {
    public String whatISay;
    public boolean Dboolean;

    //    public boolean equals(Object o) {
    //      return false;
    //    }

    public boolean equals (Animal2 x) {
      if (! (x instanceof Animal2)) return false;
      if (! (x.Dboolean != Dboolean)) return false;
      if (! (x.whatISay.equals(whatISay))) return false;
      return true;
    }
  }


  public class Canine2 extends Animal2 {
    public char Dchar;
    public long Dlong;
    public float Dfloat;
    public double Ddouble;
    public Feline2 cat;
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
    public Canine2() {
      System.out.println("[SUCCESS] CANMADE - Canine2 made");
    }

    //    public boolean equals (Object x) {
    //      return false;
    //    }

    public boolean equals(Canine2 x) {
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

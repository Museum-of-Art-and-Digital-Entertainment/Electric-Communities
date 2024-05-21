package ec.tests.insp;
import java.util.*;     // Needed only for class Enumeration
import ec.util.Equals;      // Needed for comprehensive equals() methods
import ec.util.ReadOnlyHashtable; // Needed for properties in Decode()

// Cats and dogs - some tangible classes used in all Repository test cases

public class Feline implements RtCodeable {
  public String whatISay;
  public Feline sibling;
  public String catsName;

  public Feline() {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = null;
  }

  public Feline(Feline aSibling) {
    whatISay ="";
    catsName ="<unnamed>";
    sibling = aSibling;
  }

  public Feline(Feline aSibling, String name) {
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
      sibling = (Feline)coder.decodeObject();
    } catch (Exception e) {
      System.out.println("[SUCCESS] FELDECX Feline decode() caught an exception");
    }
    return(this);
  }

  public String classNameToEncode(RtEncoder coder) {
    return getClass().getName();
  }

  public String toString() {
    return catsName;
  }

  public boolean equals(Object o) {
    if (!(o instanceof Feline)) return false;
    Feline x = (Feline)o;
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

public class Animal {
    public String whatISay;
    public boolean Someboolean;
    public int numberLegs;
}

public class Canine extends Animal {
    public char Somechar;
    public long Somelong;
    public float Somefloat;
    //    public double Somedouble;
    public Feline cat;
    public short Someshort;
    public float furr[] = new float[3];
    public int Someint;
    public byte Somebyte;
    public boolean flags[] = new boolean[6];
    public Object Somenull;
    public long nums[] = new long[3];
    public char ch[] = new char[4];
    public byte smurf[] = new byte[5];
    public short guy[] = new short[3];
    public int Somedummy;
    public Object Someobj[] = new Object[2];
    public Object Someobj2[][] = new Object[4][2];
    public Canine() {
        System.out.println("Canine made");
    }

    public boolean equals(Object inx) {
        if (inx == null) return false;
        if (! (inx instanceof Canine)) return false;
        Canine x = (Canine)inx;

        if (! (x.Somelong == Somelong)) return false;
        if (! (x.Somefloat == Somefloat)) return false;
        //        if (! (x.Somedouble == Somedouble)) return false;
        if (! (x.Someint == Someint)) return false;
        if (! (x.Somebyte == Somebyte)) return false;
        if (! (x.Somenull == Somenull)) return false;
        if (! (x.Somedummy == Somedummy)) return false;
        if (! (x.Somechar == Somechar)) return false;
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
        // if (! Equals.equals(x.Someobj,Dobj)) return false;
        return true;
    }

}
public class Bovine implements RtCodeable {
  public String whatISay;
  public Bovine sibling;
  public String cowsName;

  public Bovine() {
    whatISay ="Moo";
    cowsName ="<unnamed>";
    sibling = null;
  }

  public Bovine(Bovine aSibling) {
    whatISay ="";
    cowsName ="<unnamed>";
    sibling = aSibling;
  }

  public Bovine(Bovine aSibling, String name) {
    whatISay ="";
    cowsName = name;
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
      coder.writeUTF(cowsName);
      coder.encodeObject(sibling);
    } catch (Exception e) {e.printStackTrace();}
  }

  public  Object decode(RtDecoder coder) {
    try {
      whatISay = coder.readUTF();
      cowsName = coder.readUTF();
      sibling = (Bovine)coder.decodeObject();
    } catch (Exception e) {
      System.out.println("[SUCCESS] BOVIDEX Bovine decode() caught an exception");
    }
    return(this);
  }

  public String classNameToEncode(RtEncoder coder) {
    return getClass().getName();
  }

  public String toString() {
    return cowsName;
  }

  public boolean equals(Object notACow) {
    return false;       // If it's not a cow then we aren't equal()
  }

  public boolean equals(Bovine x) {
    if (! cowsName.equals(x.cowsName)) return false;
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


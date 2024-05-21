package ec.tests;

import ec.e.inspect.*;
import ec.ui.IFCInspectorUI;
import ec.ifc.app.*;
import ec.tests.rep.*;			// For these test cases only
import java.util.*;
import netscape.application.*;

/**
 * Test class
 */
class InspectorViewTester implements Inspectable
{
  private boolean iBoolean = false;
  private byte iByte = 5;
  private char iChar = 'x';
  private short iShort = 3001;
  private int iInt = 0xffff * 2;
  private long iLong = 0xffffffffl * 2;
  private float iFloat = 0.5f;
  private double iDouble = 2.3;
  private String iString = "yadayada";
  private Object iObject = this;
  private int[] iArray = {1,2,3};
	private static int sjutton = 17;

  public Inspector createInspector ()   {
    SemanticInspectorField[] fields = {
      new SemanticInspectorField("A boolean",
				InspectorType.BOOLEAN,
				fun (Object o) { return (new Boolean(iBoolean)); },
				fun (Object o, Object v) { iBoolean = ((Boolean)v).booleanValue(); },
				null),
      new SemanticInspectorField("A byte",
				InspectorType.BYTE,
				fun (Object o) { return (new Integer(iByte)); },
				fun (Object o, Object v) { iByte = (byte)((Byte)v).byteValue(); },
				null),
      new SemanticInspectorField("A character",
				InspectorType.CHAR,
				fun (Object o) { return (new Character(iChar)); },
				fun (Object o, Object v) { iChar = ((Character) v).charValue(); },
				null),
      new SemanticInspectorField("A short",
				InspectorType.SHORT,
				fun (Object o) { return (new Short(iShort)); },
				fun (Object o, Object v) { iShort = (short)((Short)v).shortValue(); },
				null),
      new SemanticInspectorField("An integer",
				InspectorType.INT,
				fun (Object o) { return (new Integer(iInt)); },
				fun (Object o, Object v) { iInt = ((Integer) v).intValue(); },
				null),
      new SemanticInspectorField("A long",
				InspectorType.LONG,
				fun (Object o) { return (new Long(iLong)); },
				fun (Object o, Object v) { iLong = ((Long) v).longValue(); },
				null),
      new SemanticInspectorField("A float",
				InspectorType.FLOAT,
				fun (Object o) { return (new Float(iFloat)); },
				fun (Object o, Object v) { iFloat = ((Float) v).floatValue(); },
				null),
      new SemanticInspectorField ("A double",
				 InspectorType.DOUBLE,
				 fun (Object o) { return (new Double(iDouble)); },
				 fun (Object o, Object v) { iDouble = ((Double) v).doubleValue(); },
				null),
      new SemanticInspectorField ("A string",
				 InspectorType.STRING,
				 fun (Object o) { return (iString); },
				 fun (Object o, Object v) {iString = (String)v; },
				 null),
      new SemanticInspectorField ("An Object",
				 InspectorType.OBJECT,
				 fun (Object o) { return (iObject); },
				 fun (Object o, Object v) { iObject = (Object) v; },
				 null),
      new SemanticInspectorField ("An Array",
				 InspectorType.ARRAY,
				 fun (Object o) { return (iArray); },
				 fun (Object o, Object v) { iArray = (int[]) v; },
				 null)
    };
    
    return (new Inspector(fields));
  }

  public static void main(String[] args) {

    ECApplication app = new ECApplication(); // Create the app

	ec.ui.IFCInspectorUI.initialize(); // Use IFC for Inspector UI

    Object testObject = new Canine(); // Create a test object
    ec.e.inspect.Inspector.gather(testObject, "Test Object");
    testObject = new Feline(null,"Fritz");
    ec.e.inspect.Inspector.gather(testObject, "Fritz");
	Hashtable ht = new Hashtable(10);
	ht.put("foo", "value of foo");
	ht.put("fie", "value of fie");
	ec.e.inspect.Inspector.gather(ht, "ht");
	InspectorViewTester ivt = new InspectorViewTester();
	ec.e.inspect.Inspector.gather(ivt, "ivt");
	String swedishGreeting[] = {"Hej","hopp","ditt","feta","nylle"};
	ec.e.inspect.Inspector.gather(swedishGreeting,"Swedish Greeting");
    app.run();
  }
}


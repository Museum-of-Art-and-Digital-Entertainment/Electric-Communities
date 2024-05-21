
package ec.examples.dbe;

    import java.lang.*;
    import ec.e.db.*;
    import ec.e.stream.StreamDB;

public class DBexample  {
	public static void main (String args[]) {

    String dbFileName;

    if(args.length > 1) {
    	StreamDB.setDebugOn();
   	}

	try {

    String name = System.getProperty("user.name");
    System.out.println("Creating PObjDB for " + name);
    if(args.length == 0)
        dbFileName = "/tmp/" + name + "ExampleDB";
       else dbFileName = args[0];

    PObjDB btdb = new PObjDB(dbFileName);

    System.out.println("DB created named: " + dbFileName);

    Object cat1[] = new Object[2];
    Object cat2;
 
    cat1[0] = "Meow";
    cat1[1] = new Integer(123456);

    System.out.println("Storing first test object.");
    RtStreamKey catKey = btdb.put(cat1);
	System.out.println(catKey);

	System.out.println("Closing root PObjDB file");
    btdb.closeDB();

	System.out.println("Reopening root file with an update view.");

    PObjDB db1 = new PObjDB(dbFileName);
    PObjDB db2 = new PObjDB(db1);

	cat2 = db2.get(catKey);
	System.out.println("Retrieved object " + cat2);

    System.out.println("Writing a string named DogName to the update view");
	String dog1 = new String("Woof");

	RtStreamKey dogKey = db2.put(dog1);
    db2.put("DogName", dogKey);

    System.out.println("Committing the update view back against the root file.");
	db2.commit();
    db2.closeDB();
    db1.closeDB();

    System.out.println("Reopening the root file using a read only view limiter");

    PObjDB db3 = new PObjDB(dbFileName);
    /* Now open the view limiter */
    RtDBViewLimiter readOnlyView = new RtDBViewLimiter(db3, true, false, false, null, null);

    RtStreamKey dog2Key = db3.get("DogName");
	Object dog2 = db3.get(dog2Key);
	System.out.println("Object retrieved under DogName is " + dog2);

    System.out.println("Attempting to write to the readOnlyView");
    System.out.println("(This should produce a DBAccessException)");
    readOnlyView.put(new String("BadGuy"));
    System.out.println("Security failure");

	} catch (Exception e) {e.printStackTrace();}
	}
}

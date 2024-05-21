package ec.misc;

import netscape.util.Archive;
import netscape.util.Archiver;
import netscape.util.ClassInfo;
import netscape.util.Codable;
import netscape.util.CodingException;
import netscape.util.Decoder;
import netscape.util.Encoder;
import netscape.util.Unarchiver;

import java.util.Hashtable;
import java.io.FileInputStream;
import java.io.FileOutputStream;

abstract public class WorldObjectState implements Codable
{
	static private final int VERSION = 1;
	static private String className = null;
	
	static private final String INFO = "Info";
	
	private String info = null;
	
	public WorldObjectState () {
	}
	
    public static void archiveObjectsToFile (String fileName, Codable objects[]) {
        FileOutputStream os = null;
        int i;
        try {
            System.out.println("Opening archive " + fileName + " to write");
            os = new FileOutputStream(fileName);
            Archive archive = new Archive();
            Archiver archiver = new Archiver(archive);
            System.out.println("Encoding objects");
            for (i = 0; i < objects.length; i++) {
                System.out.println("Encoding object " + i);
                archiver.archiveRootObject(objects[i]);
            }
            System.out.println("Writing out ascii archive");
            archive.writeASCII(os, true);
        } catch (Exception e) {
            System.out.println("Error archiving to file " + fileName);
            e.printStackTrace();
        }
    }

    public static Object[] unarchiveObjectsFromFile (String fileName) {
        FileInputStream is = null;
        int rootIdentifiers[];
        int i;
        try {
            System.out.println("Opening archive " + fileName + " to read");
            is = new FileInputStream(fileName);
            Archive archive = new Archive();
            System.out.println("Reading in ascii archive");
            archive.readASCII(is);
            Unarchiver unarchiver = new Unarchiver(archive);
            System.out.println("Getting roots");
            rootIdentifiers = archive.rootIdentifiers();
            if (rootIdentifiers == null) {
            	System.out.println("There are no root identifiers in archive");
            	return null;
            }
            System.out.println("There are " + rootIdentifiers.length + " roots");
            Object[] objects = new Object[rootIdentifiers.length];
            for (i = 0; i < rootIdentifiers.length; i++) {
                System.out.println("Decoding object " + i);
                objects[i] = unarchiver.unarchiveIdentifier(rootIdentifiers[i]);
            }
           return objects;
        } catch (Exception e) {
            System.out.println("Error unarchiving from " + fileName);
            e.printStackTrace();
            return null;
        }
    }

	public void setInfo (String info) {
		if (this.info != null) return;
		this.info = info;
	}
	
	public String getInfo () {
		return info;
	}
	
	// Returns the proper Unum for this WorldStateObject
	// Dictionary has everything a boy could want to parameterize
	// data needed to initialize the prime presences.
	abstract public Object getWorldObject (Hashtable dictionary);
			
	//
	// Subclasses implement Codable methods by first calling super()
	//
 
	public void describeClassInfo (ClassInfo info) {
		if (className == null) className = getClass().getName();
		info.addClass(className, VERSION);
		info.addField(INFO, Codable.STRING_TYPE);
	}
	
	public void encode (Encoder encoder) throws CodingException {
		encoder.encodeString(INFO, info);
	}
	
	public void decode (Decoder decoder) throws CodingException {
		info = decoder.decodeString(INFO);
	}
	
	public void finishDecoding () throws CodingException {
	}
}

 

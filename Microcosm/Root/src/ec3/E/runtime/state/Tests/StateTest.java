package ec.tests.state;

import ec.state.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Hashtable;
import java.util.Vector;

public class StateTest
{
    static String inputFileName = null;
    static String outputFileName = null;
    
    final static String INFILE_KEY = "infile";
    final static String OUTFILE_KEY = "outfile";
    final static String HELP_ARG = "help";
    final static String QUESTION_ARG = "?";
        
    public static void main (String args[]) {
        String dictionaryString = null;
        BigBundle big = null;
        StateDictionary dictionary = null;
        
        parseArgs(args);
                
        System.out.println("Starting");
        
        if (inputFileName != null) {
            System.out.println("Reading Dictionary State from " + inputFileName);
            try {
                FileInputStream fis = new FileInputStream(inputFileName);
                int size = fis.available();
                byte bytes[] = new byte[size];
                fis.read(bytes);
                dictionaryString = new String(bytes);
                fis.close();
            } catch (Exception e) {
                System.out.println("*** Error getting the String out of the file");
                e.printStackTrace();
                System.out.println("\n");
            }
            try {
                dictionary = StateDictionary.parseString(dictionaryString); 
            } catch (StateDictionaryParsingException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if (dictionaryString == null) {
            // Make the object graph, print it out      
            System.out.println("Creating new Object graph and StateDictionary");
            big = new BigBundle("Mr. Big", 9999, 7654.321);
            LittleBundle little = new LittleBundle(1234, big);
            big.addLittleBundle(little);
            System.out.println("*** BigBundle before ***\n" + big);
        
            // Create StateDictionary, recreate Object graph from it
            // (Note LittleBundle wakeup will print it out)     
            dictionary = StateDictionary.CreateStateDictionary(big);
        }
        
        big = (BigBundle)dictionary.getStatefulObject();
        
        // Turn the StateDictionary graph into a String, and print it       
        dictionaryString = dictionary.toString();
        System.out.println("\n*** First State Dictionary ***\n");
        System.out.println(dictionaryString);
        
        if (outputFileName != null) {
            try {
                FileOutputStream fos = new FileOutputStream(outputFileName);
                fos.write(dictionaryString.getBytes());
                fos.close();
            } catch (Exception e) {
                System.out.println("\n*** Error putting the String into the file");
                e.printStackTrace();
                System.out.println("\n");
            }
        }       
        
        // Create a new StateDictionary graph from String, print it
        try {
            dictionary = StateDictionary.parseString(dictionaryString); 
        } catch (StateDictionaryParsingException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("\n*** Parsed State Dictionary ***\n");
        System.out.println(dictionary.toString() + "\n");
        
        // Recreate Object graph again from StateDictionary graph
        // (Note wakeup will print it)      
        big = (BigBundle)dictionary.getStatefulObject();
        
        // Once more for posterity, make new StateDict, print it,
        // recreate Object graph (which wakeup implicitly prints)       
        dictionary = StateDictionary.CreateStateDictionary(big);
        System.out.println("\n*** Second State Dictionary ***\n");
        System.out.println(dictionary.toString() + "\n");
        big = (BigBundle)dictionary.getStatefulObject();
    }

    private static void parseArgs (String args[]) {
        int i;      
        for (i = 0; i < args.length; i++) {
            String name = args[i];
            if (name.equals(HELP_ARG) || name.equals(QUESTION_ARG)) {
                System.out.println("Usage: java ec.tests.state.StateTest " +
                    "[" + INFILE_KEY + "=filename] [" + OUTFILE_KEY + "=filename]");
                System.exit(0);
            }
            int index = name.indexOf('=');
            if (index >= 0) {
                String key = name.substring(0, index);
                String value = name.substring(index+1);
                if (key.equals(INFILE_KEY)) {
                    inputFileName = value;
                }
                else if (key.equals(OUTFILE_KEY)) {
                    outputFileName = value;
                }
                else {
                    System.out.println("Unknown argument: " + name);
                }
            }
            else {
                System.out.println("Unknown argument: " + name);
            }
        }       
    }   
}   

public class BigBundle implements Stateful
{
    private static final int VectorSize = 3;
    
    private String name;
    private LittleBundle little;
    private Vector simples;
    private int intValue;
    private double doubleValue;
    private Stateful nobody = null;
    private boolean whatever = true;
    private boolean whenever = false;
    
    private boolean dumping;
    
    public BigBundle () {
    }   
    
    BigBundle (String name, int intValue, double doubleValue) {
        this.name = name;
        this.simples = new Vector(VectorSize);
        for (int i = 0; i < VectorSize; i++) {
            simples.addElement(new SimpleBundle());
        }
        this.intValue = intValue;
        this.doubleValue = doubleValue;
    }
    
    void addLittleBundle (LittleBundle little) {
        this.little = little;
    }
    
    public void encodeState (WriteableStateDictionary dictionary) {
        dictionary.addStringMapping("Name", name);
        dictionary.addStatefulMapping("LittleBundle", little);
        dictionary.addVectorMapping("SimpleBundles", simples);
        dictionary.addIntMapping("IntValue", intValue);
        dictionary.addDoubleMapping("DoubleValue", doubleValue);
        dictionary.addBooleanMapping("Whatever", whatever);
        dictionary.addBooleanMapping("Whenever", whenever);
        dictionary.addStatefulMapping("Nobody", nobody);
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        name = dictionary.getStringMapping("Name");
        whatever = dictionary.getBooleanMapping("Whatever");
        whenever = dictionary.getBooleanMapping("Whenever");
        little = (LittleBundle)dictionary.getStatefulMapping("LittleBundle");
        simples = (Vector)dictionary.getVectorMapping("SimpleBundles");
        intValue = dictionary.getIntMapping("IntValue");
        doubleValue = dictionary.getDoubleMapping("DoubleValue");
        nobody = dictionary.getStatefulMapping("Nobody");   
    }
    
    public String toString() {
        String s = super.toString();
        if (dumping) return s;
        dumping = true;
        s = s + "\n\t" + name + "\n\t" + little + "\n\t" + simples.size() + " simples\n\t";
        s = s + "\n\t" + intValue + "\n\t" + doubleValue;
        s = s + "\n\t" + whatever + "\n\t" + whenever;
        dumping = false;
        return s;
    }   
}   

public class LittleBundle implements Stateful, StateAwakener
{
    private static final int VectorSize = 2;

    private Vector refs;
    private int type;
    private BigBundle big;
    private Vector emptyVector;
    private Hashtable emptyHashtable;
    private Hashtable table;
    
    public LittleBundle () {
    }   
    
    LittleBundle (int type, BigBundle big) {
        this.refs = new Vector(VectorSize);
        for (int i = 0; i < VectorSize; i++) {
            refs.addElement("Ref String " + i);
        }
        refs.addElement(null);
        refs.addElement(big);
        this.type = type;
        this.big = big;
        this.emptyVector = new Vector();
        this.emptyHashtable = new Hashtable();
        table = new Hashtable();
        table.put("Big", big);
        table.put("Little", this);
        table.put("Description", "This is the Hashtable with stuff in it");
    }

    public void encodeState (WriteableStateDictionary dictionary) {
        dictionary.addVectorMapping("Refs", refs);
        dictionary.addStatefulMapping("BigBundle", big);
        dictionary.addIntMapping("Type", type);
        dictionary.addVectorMapping("EmptyVector", emptyVector);
        dictionary.addHashtableMapping("EmptyHashtable", emptyHashtable);
        dictionary.addHashtableMapping("SuperTable", table);
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        refs = dictionary.getVectorMapping("Refs");
        big = (BigBundle)dictionary.getStatefulMapping("BigBundle");
        type = dictionary.getIntMapping("Type");
        emptyVector = dictionary.getVectorMapping("EmptyVector");
        emptyHashtable = dictionary.getHashtableMapping("EmptyHashtable");
        table = dictionary.getHashtableMapping("SuperTable");
    }
    
    public void wakeupAfterStateDecoding () {
        System.out.println("*** LittleBundle wakeup, BigBundle after ***\n" + big);
    }   

    public String toString() {
        String s = super.toString();
        s = s + "\n\t\t" + refs.size() + " refs\n\t\t" + big + "\n\t\t" + type;
        return s;
    }       
}   

public class SimpleBundle implements Stateful, StateAwakener
{

    public SimpleBundle () {
    }
    
    public void encodeState (WriteableStateDictionary dictionary) {
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
    }

    public void wakeupAfterStateDecoding () {
        System.out.println("SimpleBundle wakeup");
    }       
}


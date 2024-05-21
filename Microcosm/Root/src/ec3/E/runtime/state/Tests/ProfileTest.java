package ec.tests.state;

import java.util.Vector;
import ec.state.*;

public class ProfileTest
{
    public static void main (String args[]) {
        long start;
        long end;
        long allstart;
        long allend;
        
        int count = (new Integer(args[0])).intValue();
                
        Outer outer = new Outer();
        System.out.println("Starting");
        for (int i = 0; i < count; i++) {
            outer.addBundle(makeBundle("Mr. Big" + i, "Little" + i, i));
        }
        
        allstart = System.currentTimeMillis();      
        // Start to make Dictionary
        start = allstart;
        StateDictionary dictionary = StateDictionary.CreateStateDictionary(outer);
        end = System.currentTimeMillis();
        System.out.println("Creating Dictionary took " + (end - start) + " millis");
        // End making Dictionary
        
        // Start converting to String
        start = end;
        String s = dictionary.toString();
        end = System.currentTimeMillis();
        System.out.println("Converting Dictionary to String took " + (end - start) + " millis");
        // End converting to String
        
        // Start decoding   
        start = end;    
        outer = (Outer)dictionary.getStatefulObject();
        end = System.currentTimeMillis();
        System.out.println("Decoding Dictionary took " + (end - start) + " millis");
        // End decoding

        allend = System.currentTimeMillis();
        System.out.println("Total time for " + count + " objects is " + (allend - allstart) + " millis");
    }
    
    private static BigProfileBundle makeBundle (String bigName, String littleName, int number) {
        BigProfileBundle big = new BigProfileBundle(bigName, new SimpleProfileBundle(), number);
        LittleProfileBundle little = new LittleProfileBundle(littleName, big);
        big.addLittleProfileBundle(little);
        return big;
    }
}   

public class Outer implements Stateful
{
    Vector bundles = new Vector();
    
    ////private static final String NumberOfBundlesKey = "Number of Bundles";
    private static final String BundleKey = "Bundle";
    
    public Outer () {
    }
    
    public void addBundle (BigProfileBundle bundle) {
        bundles.addElement(bundle);
    }
    
    public void encodeState (WriteableStateDictionary dictionary) {
        /*
        int size = bundles.size();
        dictionary.addStringMapping(NumberOfBundlesKey, String.valueOf(size));
        for (int i = 0; i < size; i++) {
            BigProfileBundle bundle = (BigProfileBundle)bundles.elementAt(i);
            dictionary.addStatefulMapping(BundleKey + i, bundle);
        }
        */
        dictionary.addVectorMapping(BundleKey, bundles);
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        /*
        String sizeString = dictionary.getStringMapping(NumberOfBundlesKey);
        int size = (new Integer(sizeString)).intValue();
        bundles = new Vector(size);
        for (int i = 0; i < size; i++) {
            bundles.addElement(dictionary.getStatefulMapping(BundleKey + i));
        }
        */
        bundles = dictionary.getVectorMapping(BundleKey);
    }
}

public class BigProfileBundle implements Stateful
{
    private String name;
    private LittleProfileBundle little;
    private SimpleProfileBundle simple;
    private int number;
    private Stateful nobody = null;
    
    public BigProfileBundle () {
    }   
    
    BigProfileBundle (String name, SimpleProfileBundle simple, int number) {
        this.name = name;
        this.simple = simple;
        this.number = number;
    }
    
    void addLittleProfileBundle (LittleProfileBundle little) {
        this.little = little;
    }
    
    public void encodeState (WriteableStateDictionary dictionary) {
        dictionary.addStringMapping("Name", name);
        dictionary.addStatefulMapping("LittleProfileBundle", little);
        dictionary.addStatefulMapping("SimpleProfileBundle", simple);
        dictionary.addStringMapping("Number", String.valueOf(number));
        dictionary.addStatefulMapping("Nobody", nobody);
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        name = dictionary.getStringMapping("Name");
        little = (LittleProfileBundle)dictionary.getStatefulMapping("LittleProfileBundle");
        simple = (SimpleProfileBundle)dictionary.getStatefulMapping("SimpleProfileBundle");
        String numString = dictionary.getStringMapping("Number");
        number = (new Integer(numString)).intValue();
        nobody = dictionary.getStatefulMapping("Nobody");   
    }   
}   

public class LittleProfileBundle implements Stateful
{
    private String ref;
    private BigProfileBundle big;
    
    public LittleProfileBundle () {
    }   
    
    LittleProfileBundle (String ref, BigProfileBundle big) {
        this.ref = ref;
        ////this.big = big;
    }

    public void encodeState (WriteableStateDictionary dictionary) {
        dictionary.addStringMapping("Ref", ref);
        ////dictionary.addStatefulMapping("BigProfileBundle", big);
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
        ref = dictionary.getStringMapping("Ref");
        ////big = (BigProfileBundle)dictionary.getStatefulMapping("BigProfileBundle");
        String typeString = dictionary.getStringMapping("Type");
    }   
}   

public class SimpleProfileBundle implements Stateful
{

    public SimpleProfileBundle () {
    }
    
    public void encodeState (WriteableStateDictionary dictionary) {
    }
    
    public Stateful decodePrefaceState (ReadableStateDictionary dictionary) {
        return this;
    }
    
    public void decodeBodyState (ReadableStateDictionary dictionary) {
    }
}


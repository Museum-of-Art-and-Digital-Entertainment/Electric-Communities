package ec.ez.collect;

import java.util.Hashtable;
import java.util.Enumeration;

public class MappingImpl implements Mapping {

    private Hashtable myTable;

    private MappingImpl(Hashtable table) {
        myTable = table;
    }

    /**
     * 
     */
    static public MappingImpl run(Tuple keys, Tuple values) {
        int size = keys.size();
        if (size != values.size()) {
            throw new IllegalArgumentException
                ("must have same number of keys and values");
        }
        Hashtable oTable = new Hashtable();
        for (int i = 0; i < size; i++) {
            Object key;
            Object val;
            try {
                key = NullMarker.encode(keys.index(i));
                val = NullMarker.encode(values.index(i));
            } catch (NotFoundException ex) {
                throw new Error("internal: can't happen " + ex);
            }
            oTable.put(key, val);
        }
        return new MappingImpl(oTable);
    }

    static public MappingImpl make(Hashtable table) {
        return new MappingImpl((Hashtable)table.clone());
    }

    static public MappingImpl fromMapping(Mapping original) {
        if (original instanceof MappingImpl) {
            return (MappingImpl)original;
        }
        Hashtable oTable = new Hashtable();
        for (Enumeration keys = original.keys(); keys.hasMoreElements(); ) {

            Object key = keys.nextElement();
            Object val;
            try {
                val = original.get(key);
            } catch (NotFoundException ex) {
                throw new Error("can't happen");
            }
            key = NullMarker.encode(key);
            val = NullMarker.encode(val);
            oTable.put(key, val);
        }
        return new MappingImpl(oTable);
    }

    public int size() {
        return myTable.size();
    }

    public boolean containsKey(Object key) {
        return myTable.containsKey(NullMarker.encode(key));
    }

    public Object get(Object key) throws NotFoundException {
        Object optVal = myTable.get(NullMarker.encode(key));
        return NullMarker.optDecode(optVal, key.toString());
    }

    public Mapping with(Object key, Object value) {
        Object k = NullMarker.encode(key);
        Object v = NullMarker.encode(value);
        Hashtable t = (Hashtable)myTable.clone();
        t.put(k, v);
        return new MappingImpl(t);
    }

    public Mapping without(Object key) {
        Object k = NullMarker.encode(key);
        Hashtable t = (Hashtable)myTable.clone();
        t.remove(k);
        return new MappingImpl(t);
    }

    public Enumeration keys() {
        return new DecodingEnumeration(myTable.keys());
    }

    public Enumeration asEnumeration() {
        return new DecodingEnumeration(myTable.elements());
    }

    public AssociationEnumeration associations() {
        return new KeyValueEnumeration(myTable);
    }

    public Tuple occlude(Mapping under) {
        MappingImpl undr = (MappingImpl)fromMapping(under);
        Hashtable image = (Hashtable)(undr.myTable.clone());
        Hashtable hidden = new Hashtable();
        for (Enumeration keys = myTable.keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object covered = image.put(key, myTable.get(key));
            if (covered != null) {
                hidden.put(key, covered);
            }
        }
        return TupleImpl.run(new MappingImpl(image), new MappingImpl(hidden));
    }
}

class DecodingEnumeration implements Enumeration {
    private Enumeration myEncoded;

    DecodingEnumeration(Enumeration encoded) {
        myEncoded = encoded;
    }

    public boolean hasMoreElements() {
        return myEncoded.hasMoreElements();
    }

    public Object nextElement() {
        return NullMarker.optDecode(myEncoded.nextElement());
    }
}

class KeyValueEnumeration implements AssociationEnumeration {
    Enumeration keyEnumeration;
    Enumeration valueEnumeration;
    Object lastKey = null;

    KeyValueEnumeration(Hashtable forTable) {
        keyEnumeration = forTable.keys();
        valueEnumeration = forTable.elements();
    }

    public boolean hasMoreElements() {
        return valueEnumeration.hasMoreElements();
    }

    public Object nextElement() {
        Object val = NullMarker.optDecode(valueEnumeration.nextElement());
        lastKey = NullMarker.optDecode(keyEnumeration.nextElement());
        return val;
    }

    public Object currentKey() {
        return lastKey;
    }
}

package ec.ez.runtime;

import java.util.Hashtable;


public class MappingImpl implements Mapping {

    private Hashtable myTable;
    private Object myNullMarker;

    public MappingImpl(Hashtable table, Object nullMarker) {
        myTable = (Hashtable)table.clone();
        myNullMarker = nullMarker;
    }

    public Object get(Object key) throws NotFoundException {
        Object result = myTable.get(key);
        if (result == myNullMarker) {
            return null;
        } else if (result != null) {
            return result;
        } else {
            throw new NotFoundException(key.toString());
        }
    }

    public Mapping occlude(Mapping under) {
        throw new RuntimeException("XXX NotYetImplemented");
    }
}

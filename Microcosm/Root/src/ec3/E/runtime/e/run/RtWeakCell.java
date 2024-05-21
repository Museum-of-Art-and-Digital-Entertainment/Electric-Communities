package ec.e.run;

public class RtWeakCell implements RtCodeable
{
    public RtWeakCell (RtWeakling object) {
        if (object != null) {
            object.addedToWeakCell(this);
        }
        store(object);
    }

    native public RtWeakling get ();

    public void clear () {
        RtWeakling object = get();
        object.removedFromWeakCell(this);
        store(null);
    }
    
    public void put (RtWeakling object) {
        RtWeakling other = get();
        if (other == object) return;
        if (other != null) other.removedFromWeakCell(this);
        object.addedToWeakCell(this);
        store(object);
    }
    
    public boolean equals (Object obj) {
        return get().equals(obj);
    }

    public int hashCode () {
        return get().hashCode();
    }

    private RtWeakCell () {
    }

    public String classNameToEncode (RtEncoder encoder) {
        return null;
    }

    public void encode (RtEncoder encoder) {
        throw new RuntimeException("Trying to encode WeakCell!");
    }

    public Object decode (RtDecoder decoder) {
        return null;
    }

    //
    // Private API
    //
    private int target;
    native private void store (RtWeakling obj);

    static {
        System.loadLibrary("run");
    }
}



package ec.e.util;

public class ExpireCollectionEnumeratorEntry 
{
    public Object key;
    public Object value;
    public long expirationDate;
    ExpireCollectionEnumeratorEntry next;
    
    public ExpireCollectionEnumeratorEntry(Object key, Object value, long expirationDate) {
        this.key = key;
        this.value = value;
        this.expirationDate = expirationDate;
    }

    public String toString() {
        return "ExpirationEntry(" + key + ", " + value + ", " + expirationDate + ")" ;
    }
}

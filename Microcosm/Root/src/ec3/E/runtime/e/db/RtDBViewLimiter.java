/* An implementation of a DBViewFilter that imposes access control list
   semantics on an underlying database view.
   
   The grantor creates a RtDBViewLimiter object, initialized with the policy
   control parameters desired. This can be passed on to the grantee, who will
   be restricted to the operations allowed. These objects can be chained, each
   link serves to further limit the powers of the ultimate grantee. An attempt
   to establish a less restrictive policy will fail because all operations pass
   control back through the delegation chain.
   
   There are three master control flags, readPolicy, writePolicy, and
   commitPolicy.  There are also two key control lists, which control which
   root keys the grantee is permitted to read or change.  A key control list
   that is null is taken to mean "wide open access permitted".  Otherwise the
   list restricts the user to read or store the root keys given and no others.
   These lists do not effect access to stream keys, which are considered to be
   capabilities in themselves.
   */
package ec.e.db;
import java.util.Hashtable;

public class RtDBViewLimiter implements RtDBViewFilter {
    RtDBViewFilter parentView;
    boolean readOK;
    boolean writeOK;
    boolean commitOK;
    Hashtable okReadKeys;
    Hashtable okWriteKeys;
    
    public RtDBViewLimiter(RtDBViewFilter parent, boolean readPolicy,
                           boolean writePolicy, boolean commitPolicy,
                           Hashtable readKeys, Hashtable writeKeys) {
        parentView = parent;
        commitOK = commitPolicy;
        writeOK = writePolicy;
        readOK = readPolicy;
        okReadKeys = readKeys;
        okWriteKeys = writeKeys;
    }
    
    public RtStreamKey put(Object object) throws DBAccessException {
        if (!writeOK)
            throw(new DBAccessException());
        
        return(parentView.put(object));
    }
    
    public void put(Object rootKey, RtStreamKey streamKey)
    throws DBAccessException {
        if (!writeOK)
            throw(new DBAccessException());
        
        if (okWriteKeys == null) {
            parentView.put(rootKey, streamKey);
            return;
        }
        
        if (okWriteKeys.contains(rootKey)) {
            parentView.put(rootKey, streamKey);
            return;
        } else
            throw(new DBAccessException());
    }
    
    public  RtStreamKey get(Object rootKey) throws DBAccessException {
        if (!readOK)
            throw(new DBAccessException());
        
        if (okReadKeys == null)
            return(parentView.get(rootKey));
        if (okReadKeys.contains(rootKey))
            return(parentView.get(rootKey));
        else
            throw(new DBAccessException());
    }
    
    public  Object get(RtStreamKey key) throws DBAccessException {
        if (!readOK)
            throw(new DBAccessException());
        
        return(parentView.get(key));
    }
    
    public  boolean contains(Object key) throws DBAccessException {
        if (!readOK)
            throw(new DBAccessException());
        
        return(parentView.contains(key));
    }
    
    public  void commit() throws DBAccessException {
        if (!commitOK)
            throw(new DBAccessException());
        parentView.commit();
    }
}

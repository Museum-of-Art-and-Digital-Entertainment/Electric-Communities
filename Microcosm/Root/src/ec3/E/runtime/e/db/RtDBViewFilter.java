/** An interface to a Persistent object database which can be used to
  impose access restrictions.  This interface is not accessible from
  the Repository level and has been deprecated. If you need this
  functionality, you must ask for it */

package ec.e.db;

public interface RtDBViewFilter {
    RtStreamKey put(Object object) throws DBAccessException;
    void put(Object rootKey, RtStreamKey streamKey) throws DBAccessException;
    RtStreamKey get(Object rootKey) throws DBAccessException;
    Object get(RtStreamKey key) throws DBAccessException;
    boolean contains(Object key) throws DBAccessException;
    void commit() throws DBAccessException;
}

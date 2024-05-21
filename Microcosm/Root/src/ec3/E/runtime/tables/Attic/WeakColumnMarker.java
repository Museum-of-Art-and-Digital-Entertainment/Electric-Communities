package ec.tables;


/**
 * A Marker interface indicating (to some appropriate garbage
 * collector) that members of this Column that are not otherwise
 * referenced should be smashed.  This enables an efficient-for-Java
 * implementation of ParcPlace Smalltalk's weak-collection and
 * finalization semantics.
 *
 * @see ec.tables.Column
 */
public interface WeakColumnMarker {}

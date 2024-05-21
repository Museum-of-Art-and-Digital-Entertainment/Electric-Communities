package ec.e.lang;

/**
* This class provides a safe wrapper for the Java System.arraycopy method.
* This operation is, in principle, a compute level operation rather than a
* steward level operation, but since it is embedded in an unsafe class (System)
* we need this steward class to wrap it.
*
* XXX This class really shouldn't be in ec.e.lang, since it needs to be a
* steward class while the rest of ec.e.lang could otherwise be treated as a
* compute class.
*/
final public class EServices {
    /**
    * @see System.arraycopy()
    */
    public static void arraycopy(Object src, int src_position, Object dst,
                                 int dst_position, int length) {
        System.arraycopy(src, src_position, dst, dst_position, length);
    }
}

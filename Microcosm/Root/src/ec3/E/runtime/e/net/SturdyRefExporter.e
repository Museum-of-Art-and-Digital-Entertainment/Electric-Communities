package ec.e.net;

public final class SturdyRefExporter {
    private SturdyRefExporter() {}
    
    SturdyRefExporter(Registrar registrar) {
        if (registrar == null) {
            throw new SecurityException("Need real registrar to make SturdyRefExporter");
        }
    }

    public String exportRefUniqueId(SturdyRef ref) {
        return ref.myRemoteRID + "/" + ref.myRemoteObjectID ;
    }
    
    public String exportRef(SturdyRef ref) {
        return (new EARL(ref.myRemoteSearchPath, ref.myRemoteRID, ref.myRemoteObjectID, null)).url();
    }
}

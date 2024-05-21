package ec.e.run;

import ec.e.net.EConnection;

public class EObjectExport
{
    static public Trace tr = new Trace("ec.e.run.EObjectExport");

    int referenceCount = 0;
    Exportable object;
    EConnection connection;

    public EObjectExport (Exportable object, EConnection connection) {
        this.object = object;
        this.connection = connection;
    }

    public Exportable getObject() {
        return object;
    }

    public void touch () {
        if (referenceCount == -1) {
            throw new RtRuntimeException ("Removed EObjectExport was referenced");
        }
        referenceCount++;
    }

    public synchronized void dgcSuspectTrash (int otherReferenceCount) {
        if ((referenceCount - otherReferenceCount) == 0) {
            if (tr.tracing) tr.$("EObjectExport " + object + " is trash & getting removed");
            connection.dgcWRemoveMe(object.getIdentity());
            referenceCount = -1;
        } else {
            if (tr.tracing) tr.$("EObjectExport " + object + 
                " suspected to be trash, but ref count > 0 so it survives");
        }
    }
}

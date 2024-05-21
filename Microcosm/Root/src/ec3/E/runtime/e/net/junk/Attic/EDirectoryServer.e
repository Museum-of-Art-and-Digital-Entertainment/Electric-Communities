package ec.e.net;

public eclass EDirectoryServer {
    private RtRegistrarServer myReg;
    private RtPublicationServer myPub;
    
    EDirectoryServer(RtRegistrarServer reg, RtPublicationServer pub) {
        myReg = reg;
        myPub = pub;
    }
    
    emethod lookupName(String name, EDistributor result) {
        try {
            if (name.startsWith("#"))
                myReg.lookupName(name, result);
            else
                myPub.lookupName(name, result);
        } catch (Exception e) {
            ethrow new RtDirectoryEException(e.getMessage());
        }
    }
}

public class RtDirectoryEException extends RtEException {
    public RtDirectoryEException(String message) {
        super(message);
    }
}

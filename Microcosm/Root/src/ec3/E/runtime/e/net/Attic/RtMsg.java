/*
  Declaration of message type codes for low-level Comm system.
*/

package ec.e.net;

public class RtMsg {
    /*
      Normal priority messages.
    */
    public static final int kcEnvelope = 1;     /* an E Envelope */
    
    /*
      Distributed GC messages
    */
    public static final int kcWResponse = 2;    /* Response to LR query. */
    public static final int kcWRemoveMe = 3;    /* Proxy can be removed */
    public static final int kcWAreYouLR = 4;    /* Are you locally rooted? */
    public static final int kcSuspectTrash = 5; /* Suspect trash */
    
    /*
      Connection admin
    */
    public static final int kcExportObjectRequest = 6;/* Exp for handoff req */
    public static final int kcExportObjectReply = 7; /* Exp for handoff reply*/
    public static final int kcNewClasses = 8;/* New classes previously unsent*/
    
    public static final int kcMaxNormalPriority = 8;
    
    private static String NormalBlobNames[] = {
        "InvalidNormalPriorityMessage",
        "Envelope",
        "WResponse",
        "WRemoveMe",
        "WAreYouLR",
        "SuspectTrash",
        "ExportObject",
        "ExportObjectReply",
        "NewClasses"
    };
    
    private static String stringForNormalPriorityMessage(int index) {
        if (index >= NormalBlobNames.length) 
            return("InvalidNormalPriorityMessage");
        return(NormalBlobNames[index]);
    }
    
    public static String stringForMessage(int index) {
        return(stringForNormalPriorityMessage(index));
    }
    
    public static boolean validMessage (int index) {
        if (index <= 0)
            return(false);
        else
            return(index <= kcMaxNormalPriority); 
    }
}

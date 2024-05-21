/*
  Declaration of message type codes for low-level Comm system.
*/

package ec.e.net;

public class Msg {
    /*
      values 1-12 were used by version 0 protocol.  See comment below for values.
    */
    static final byte PROTOCOL_ERROR        = 2;// old startup packet from pre-version 1

    /*
      Connection admin
    */
    public static final String[] Version     = {"4", "4"};
    public static final int VersionInt       =  4;          // Obsolete, used for compatibility w/version 2
    
    public static final byte PROTOCOL_VERSION= 16;// Initial message followed by version string above
    public static final byte STARTUP         = 17;// Conn. startup protocol msg
    static final byte NEW_CLASSES            = 18;// New classes prev. unsent
    static final byte EXPORT_OBJECT_REQUEST  = 19;// Export for handoff request
    static final byte EXPORT_OBJECT_REPLY    = 20;// Export for handoff reply
    static final byte SUSPEND                = 21;// Take down physical connection
                                                 //   leaving logical connection intact
    // New with version 3
    public static final byte PROTOCOL_ACCEPTED = 22;// Followed by version string of the selected protocol version

    /*
      Distributed GC messages
    */
    static final byte W_RESPONSE             = 32;// Response to LR query
    static final byte W_REMOVE_ME            = 33;// Proxy can be removed
    static final byte W_ARE_YOU_LR           = 34;// Are you locally rooted?
    static final byte SUSPECT_TRASH          = 35;// Suspect trash
    static final byte UNREGISTER_IMPORT      = 36;// Unregister imported Object
    static final byte UNREGISTER_UNIQUE      = 37;// Unregister Uniquely Imported Object
    static final byte UNREGISTER_EXPORT      = 38;// Unregister Exported Object

    /*
      Normal messages.
    */
    static final byte ENVELOPE              = 64;// Normal E envelope
}

/* version 0 protocol values below:
   
    static final byte ENVELOPE              = 1;// Normal E envelope
    public static final byte STARTUP        = 2;// Conn. startup protocol msg
    static final byte NEW_CLASSES           = 3;// New classes prev. unsent
    static final byte EXPORT_OBJECT_REQUEST = 4;// Export for handoff request
    static final byte EXPORT_OBJECT_REPLY   = 5;// Export for handoff reply
    static final byte SUSPEND               = 6;// Take down physical connection
                                                //   leaving logical connection intact
    static final byte W_RESPONSE            = 6;// Response to LR query
    static final byte W_REMOVE_ME           = 7;// Proxy can be removed
    static final byte W_ARE_YOU_LR          = 8;// Are you locally rooted?
    static final byte SUSPECT_TRASH         = 9;// Suspect trash
    static final byte UNREGISTER_IMPORT     = 10;// Unregister imported Object
    static final byte UNREGISTER_UNIQUE     = 11;// Unregister Uniquely Imported Object
    static final byte UNREGISTER_EXPORT     = 12;// Unregister Exported Object
*/

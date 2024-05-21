package ec.pl.runtime;

public interface SecretUnum  {
    ClientPresenceTracker makeClientPresenceTracker (String presenceKindname,
                                                     long swissNumber);

// RobJ pspread: this is the wrong construction now
//    Unum invalidateAndMakeNewUnum(SecretKey secret);

    // synchronously make new unum internally
    // ...may be a way around this eventually....
    UnumRouter makeNewUnum (Object vskey, SecretKey secret);

    void invalidate(SecretKey secret);
    void killUnum(SecretKey secret);
    void setNewPresence (PresenceRouter presence, SecretKey secret);
    UnumSoul getUnumSoul (SecretKey secret);
    void createUnumSoul (SecretKey secret);
    
    // XXX PSPREAD this goes away when environment.unum becomes a deflector
    Object unumKindInfo (SecretKey secret);
    
    void setSessionKey (Object sessionKey, SecretKey secret);
    Object sessionKey (SecretKey secret);
}



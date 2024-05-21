/*
  PresenceEnvironment.java -- Information needed by ingredients.

  Copyright 1998 Electric Communities, all rights reserved worldwide.
*/

package ec.pl.runtime;

import java.util.Vector;

/**
 * A struct full of information needed by all the ingredients of a presence.
 */
public class PresenceEnvironment {
    /** What presence we are an ingredient of */
    public PresenceRouter presence;
    /** Deflector to the first unum init'ed (i.e. the prime presence or the
      one decode) */
    public Unum unumDeflector;
    /** The unum incarnation we are a part of */
    public UnumRouter unum;
    /** Behavioral flags describing this presence (set by Pluribus compiler */
    public int flags;
    /** Behavioral flag values */
    public static final int DieWhenNoOtherPresences = 0x00000001;
    public static final int IsClientPresence        = 0x00000004;
    public static final int IsHostPresence          = 0x00000008;
    public static final int Encodeable              = 0x00000010;
    public static final int Invalidated             = 0x08000000;
    
    /** Harmless old constant that much code still needs to compile */
    public static final int TrackOtherPresences     = 0x00000000;
    
    /** Other presences of this unum (e.g., all the clients if I'm a host) */
    public Vector /* PresenceEntry */ otherPresences = new Vector(1);
    /** Other incarnations of this unum */
    public Vector /* UnumEntry */ otherUna = new Vector(1);
    /** Deflector to my host */
    public PresenceHost hostPresenceDeflector;
    /** The unum's soul */
    public UnumSoul soul;
}

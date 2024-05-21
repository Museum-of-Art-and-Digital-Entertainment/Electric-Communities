// Sound.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;
import java.net.URL;


/** Object subclass representing a sound.  Typically, you retrieve a Sound
  * by name from an 8 bit, µlaw, 8000 Hz, one-channel, Sun ".au" file:
  * <pre>
  *     sound = Sound.soundNamed("MySound.au");
  * </pre>
  */


public class Sound implements Codable {
    String                name;
    java.applet.AudioClip awtSound;
    boolean               shouldLoop;

    static final String         NAME_KEY = "name";

    /** Constructs a Sound with no sound data. This method is only useful
      * when decoding.
      */
    public Sound() {
        super();
    }

    /** Returns the Sound named <b>soundName</b>. The application maintains
      * a cache of named Sounds that it checks first. If not located in the
      * cache, this method looks for the specified Sound in the "sounds"
      * direcotry in the same directory as the Application's index.html
      * file.  In other words, it constructs the URL
      * <pre>
      *     "codebase"/sounds/soundName
      * </pre>
      * and attempts to load this file.  <b>soundName</b> can specify a
      * file name or path, such as "newSounds/MySound.au".
      *
      * <p><i><b>Note:</b> No error will be reported if the specified Sound
      * file does not exist or is the wrong format.</i>
      */
    public static synchronized Sound soundNamed(String soundName) {
        Application app;
        Sound sound;
        URL url;

        if (soundName == null || soundName.equals(""))
            return null;

        app = Application.application();
        sound = (Sound)app.soundByName.get(soundName);

        if (sound != null)
            return sound;

        url = app._appResources.urlForSoundNamed(soundName);
        sound = soundFromURL(url);

        // ALERT.  This is a bogus check.  The AWT never returns null.
        if (sound == null) {
            System.err.println("Unknown sound: " + url);
            return null;
        }

        app.soundByName.put(soundName, sound);
        sound.name = soundName;

        return sound;
    }

    /** Returns a Sound initialized with data from <b>url</b>.
      */
    public static Sound soundFromURL(URL url) {
        java.applet.AudioClip awtSound;
        Sound sound;

        awtSound = AWTCompatibility.awtApplet().getAudioClip(url);

        sound = new Sound();
        sound.awtSound = awtSound;

        return sound;
    }

    // This is used in decoding.  Take a look at this again to see if it
    // causes problems by having multiple sounds with the same name.  ALERT!

    synchronized void nameSound(String soundName, Sound sound) {
        Application.application().soundByName.put(soundName, sound);
    }

    /** Returns the Sound's name, if any.
      */
    public String name() {
        return name;
    }

    /** Sets the Sound to automatically replay upon completion, or just play
      * once to completion.
      */
    public void setLoops(boolean flag) {
        shouldLoop = flag;
    }

    /** Returns <b>true</b> if the Sound automatically replays upon
      * completion.
      * @see #setLoops
      */
    public boolean doesLoop() {
        return shouldLoop;
    }

    /** Plays the Sound.
      */
    public void play() {
        if (awtSound != null) {
            if (shouldLoop) {
                awtSound.loop();
            } else {
                awtSound.play();
            }
        }
    }

    /** Stops the Sound playback.
      */
    public void stop() {
        if (awtSound != null) {
            awtSound.stop();
        }
    }

    /** Returns the Sound's String representation.
      */
    public String toString() {
        if (name != null) {
            return "Sound(" + name + ")";
        } else
            return super.toString();
    }

    /** Describes the Sound class' coding information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.Sound", 1);
        info.addField(NAME_KEY, STRING_TYPE);
    }

    /** Encodes the Sound.  A Sound can only be encoded if it has a name.
      * @see #soundNamed
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        if (name == null) {
            throw new CodingException("An encoded Sound must have a name");
        }

        encoder.encodeString(NAME_KEY, name);
    }

    /** Decodes the Sound.  A Sound can only be decoded if it has a name.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        URL url;
        Application app;

        name = decoder.decodeString(NAME_KEY);
        if (name == null) {
            throw new CodingException("A decoded Sound must have a name");
        }

        app = Application.application();
        url = app._appResources.urlForSoundNamed(name);
        awtSound = app.applet.getAudioClip(url);

        nameSound(name, this);
    }

    /** Finishes the Sound's decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}

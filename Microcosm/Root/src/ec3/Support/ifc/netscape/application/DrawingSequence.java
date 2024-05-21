// DrawingSequence.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Abstract Image subclass that simplifies frame-by-frame animation.  A
  * DrawingSequence works with a collection of "frames."  It animates
  * its frames by incrementing or decrementing its current frame number and
  * drawing the new frame.  A DrawingSequence is not a View - it must ask its
  * owner, typically a View, to display its new frame by calling its
  * <b>drawingSequenceFrameChanged()</b> method.<p>
  * Note that the machinery within the DrawingSequence abstract class does not
  * know what it's animating, only that it has a current frame number that
  * must be incremented or decremented.  As a subclasser overriding the
  * <b>drawAt()</b> method, you can perform whatever drawing operation you
  * want, such as draw a Bitmap or fill a Rect, based on the value returned by
  * <b>currentFrameNumber()</b>.  The ImageSequence class is
  * a DrawingSequence subclass that specifically animates a collection of
  * Images.  If you only need to animate Images, you should use
  * an instance of the ImageSequence class.
  * @see DrawingSequenceOwner
  * @see ImageSequence
  * @note 1.0 stop constant was fixed to 'stop'
  */
public abstract class DrawingSequence extends Image implements Target, Codable{
    DrawingSequenceOwner            owner;
    String              name;
    Timer               animateTimer;
    int                 currentFrameNumber, frameCount, frameRate;
    int                 playbackMode;
    boolean             animating, resetOnStart, resetOnStop,
                        bounceForward = true;

    /** Increment the frame number; upon reaching the sequence's last frame,
        stop animating. */
    public final static int     FORWARD = 0;

    /** Increment the frame number;  upon reaching the sequence's last frame,
        return to the first frame and continue animating. */
    public final static int     FORWARD_LOOP = 1;

    /** Decrement the frame number; upon reaching the sequence's first frame,
        stop animating. */
    public final static int     BACKWARD = 2;

    /** Decrement the frame number;  upon reaching the sequence's first frame,
        return to the last frame and continue animating. */
    public final static int     BACKWARD_LOOP = 3;

    /** Increment the frame number; upon reaching the sequence's last frame,
        decrement to the first frame, then start again. */
    public final static int     BOUNCE = 4;

    final static int            MIN_FRAME_RATE = 1;

    final static String         OWNER_KEY = "owner", NAME_KEY = "name",
                                CURRENTFRAME_KEY = "currentFrameNumber",
                                FRAMECOUNT_KEY = "frameCount",
                                FRAME_RATE_KEY = "frameRate",
                                PLAYBACKMODE_KEY = "playbackMode",
                                ANIMATING_KEY = "animating",
                                RESETONSTART_KEY = "resetOnStart",
                                RESETONSTOP_KEY = "resetOnStop";

    /** Command that starts the DrawingSequence. */
    public final static String START = "start";

    /** Command that stops the DrawingSequence. */
    public final static String STOP = "stop";

    /** Command that moves the DrawingSequence to its next frame. */
    public final static String NEXT_FRAME = "nextFrame";

    /* constructors */

    /** Constructs a DrawingSequence without an owner.
      */
    public DrawingSequence() {
        this(null);
    }

    /** Constructs a DrawingSequence with owner <B>owner</B>, frame
      * rate of 1 millisecond, current frame number of 0, and FORWARD
      * direction.
      */
    public DrawingSequence(DrawingSequenceOwner owner) {
        super();

        this.owner = owner;
        frameRate = MIN_FRAME_RATE;
        playbackMode = FORWARD;
    }

    /** Sets the DrawingSequence's owner, the object that the DrawingSequence
      * contacts when its current frame number changes or the DrawingSequence
      * reaches its final frame.
      * @see DrawingSequenceOwner
      */
    public void setOwner(DrawingSequenceOwner anObject) {
        owner = anObject;
    }

    /** Returns the DrawingSequence's owner.
      * @see #setOwner
      */
    public DrawingSequenceOwner owner() {
        return owner;
    }

    /** Sets the DrawingSequence's name. */
    public void setName(String aName) {
        name = aName;
    }

     /** Returns the DrawingSequence's name.
      * @see #setName
      */
    public String name() {
        return name;
    }

    /** Implemented to respond to the NEXT_FRAME, START and STOP commands.
      * The NEXT_FRAME command results in calls to the
      * <b>nextFrame()</b> method. If the DrawingSequence completes its
      * animation, it sends the <b>drawingSequenceCompleted()</b> message to
      * its owner and calls its <b>stop()</b> method, otherwise it sends the
      * <b>drawingSequenceFrameChanged()</b> message to its owner. When the
      * frame changes, the owner is responsible for redisplaying the
      * DrawingSequence.  The START and STOP commands start or stop the
      * DrawingSequence.
      * @see DrawingSequenceOwner
      * @see #stop
      */
    public void performCommand(String command, Object data) {
        if (START.equals(command))
            start();
        else if (STOP.equals(command))
            stop();
        else if (NEXT_FRAME.equals(command)) {
            nextFrame();
        } else
            throw new NoSuchMethodError("unknown command: " + command);
    }

    /** Instructs the DrawingSequence to animate itself by changing its current
      * frame every <b>frameRate()</b> milliseconds.
      * @see #setFrameRate
      */
    public void start() {
        if (resetOnStart) {
            reset();
        }

        animating = true;

        if (animateTimer == null) {
            if (owner != null) {
                owner.drawingSequenceFrameChanged(this);
            }

            animateTimer = new Timer(this, "nextFrame", frameRate);
            animateTimer.start();
        }
    }

    /** Returns <b>true</b> if the DrawingSequence is currently animating
      * itself.
      * @see #start
      */
    public boolean isAnimating() {
        return animating;
    }

    /** Stops the DrawingSequence's animation.
      * @see #start
      */
    public void stop() {
        animating = false;
        if (animateTimer != null) {
            animateTimer.stop();
            animateTimer = null;
        }

        if (resetOnStop) {
            reset();
        }
    }

    /** Sets the DrawingSequence's current frame to its initial frame:
      * the first frame if it has a forward or bounce direction, its last
      * if a backward direction.
      */
    public void reset() {
        if (playbackMode == FORWARD || playbackMode == FORWARD_LOOP ||
               playbackMode == BOUNCE) {
            currentFrameNumber = 0;
        } else {
            currentFrameNumber = frameCount - 1;
        }
    }

    /** Sets the number of frames the DrawingSequence animates. */
    public void setFrameCount(int count) {
        if (count > 0) {
            frameCount = count;
        } else {
            frameCount = 0;
        }
    }

    /** Returns the number of frames the DrawingSequence animates.
      * @see #setFrameCount
      */
    public int frameCount() {
        return frameCount;
    }

    /** Sets the DrawingSequence's current frame number.  When asked to draw
      * itself, a DrawingSequence displays its current frame.
      */
    public void setCurrentFrameNumber(int anInt) {
        if (anInt < 0) {
            anInt = 0;
        } else if (anInt >= frameCount) {
            anInt = frameCount - 1;
        }

        currentFrameNumber = anInt;
    }

    /** Returns the DrawingSequence's current frame number.
      * @see #setCurrentFrameNumber
      */
    public int currentFrameNumber() {
        return currentFrameNumber;
    }

    /** Sets the millisecond delay between calls to <b>nextFrame()</b>.
      * @see #nextFrame
      */
    public void setFrameRate(int milliseconds) {
        if (milliseconds > MIN_FRAME_RATE) {
            frameRate = milliseconds;
        } else {
            frameRate = MIN_FRAME_RATE;
        }

        if (animateTimer != null) {
            animateTimer.setDelay(frameRate);
        }
    }

    /** Returns the millisecond delay between calls to <b>nextFrame()</b>.
      * @see #setFrameRate
      */
    public int frameRate() {
        return frameRate;
    }

    /** Sets the DrawingSequence's frame playback mode.  Currently, the five
      * options are FORWARD, FORWARD_LOOP, BACKWARD, BACKWARD_LOOP, and
      * BOUNCE.
      * Resets the DrawingSequence's current frame to its initial frame by
      * calling <b>reset()</b>.
      * @see #reset
      */
    public void setPlaybackMode(int mode) {
        if (mode < 0 || mode > BOUNCE) {
            return;
        }
        playbackMode = mode;

        reset();
    }

    /** Returns the DrawingSequence's playback mode.
      * @see #setPlaybackMode
      */
    public int playbackMode() {
        return playbackMode;
    }

    /** Configures the DrawingSequence to automatically reset its frame to its
      * initial frame (by calling <b>reset()</b>) before it begins
      * animating.
      * @see #setPlaybackMode
      * @see #reset
      * @see #start
      */
    public void setResetOnStart(boolean flag) {
        resetOnStart = flag;
    }

    /** Returns <b>true</b> if the DrawingSequence resets its frame
      * to its initial frame before it begins animating.
      * @see #setResetOnStart
      */
    public boolean doesResetOnStart() {
        return resetOnStart;
    }

    /** Sets the DrawingSequence to automatically reset its frame to its
      * initial frame (by calling <b>reset()</b>) after it finishes
      * animating.
      * @see #setPlaybackMode
      * @see #reset
      * @see #stop
      */
    public void setResetOnStop(boolean flag) {
        resetOnStop = flag;
    }

    /** Returns <b>true</b> if the DrawingSequence resets its frame
      * to its initial frame after it finishes animating.
      * @see #setResetOnStop
      */
    public boolean doesResetOnStop() {
        return resetOnStop;
    }

    /** Returns <b>true</b> if the DrawingSequence continues animating
      * from its initial frame after reaching its final frame, or if it
      * animates in BOUNCE mode.
      * @see #setPlaybackMode
      */
    public boolean doesLoop() {
        return (playbackMode == FORWARD_LOOP ||
                playbackMode == BACKWARD_LOOP || playbackMode == BOUNCE);
    }

    /** Moves the DrawingSequence to its next frame, incrementing or
      * decrementing its frame number.  Returns <b>false</b> if the
      * DrawingSequence has reached its final frame and its playback mode does
      * not allow it to loop back to its initial frame.
      * @see #setPlaybackMode
      */
    public boolean nextFrame() {
        boolean framesLeft = true;

        if (frameCount == 0) {
            stop();
            throw new InconsistencyException("Frame count is 0");
        }

        if (playbackMode == FORWARD || playbackMode == FORWARD_LOOP) {
            currentFrameNumber++;
            if (currentFrameNumber >= frameCount) {
                if (playbackMode != FORWARD_LOOP) {
                    currentFrameNumber = frameCount - 1;
                    framesLeft = false;
                } else {
                    currentFrameNumber = 0;
                }
            }
        } else if (playbackMode == BOUNCE) {
            framesLeft = true;
            if (frameCount == 1) {
                currentFrameNumber = 0;
            } else if (bounceForward == true) {
                currentFrameNumber++;
                if (currentFrameNumber >= frameCount) {
                    bounceForward = false;
                    currentFrameNumber = currentFrameNumber - 2;
                }
            } else if (bounceForward == false) {
                currentFrameNumber--;
                if (currentFrameNumber < 0) {
                    bounceForward = true;
                    currentFrameNumber = currentFrameNumber + 2;
                }
            }
        } else {
            currentFrameNumber--;
            if (currentFrameNumber < 0) {
                if (playbackMode != BACKWARD_LOOP) {
                    currentFrameNumber = 0;
                    framesLeft = false;
                } else {
                    currentFrameNumber = frameCount - 1;
                }
            }
        }

        if (framesLeft) {
            if (owner != null) {
                owner.drawingSequenceFrameChanged(this);
            }
            return true;
        } else {
            stop();

            if (owner != null) {
                owner.drawingSequenceCompleted(this);
            }
            return false;
        }
    }

    /** Subclassers must implement this method to return the
      * DrawingSequence's width.
      */
    public abstract int width();

    /** Subclassers must implement this method to return the
      * DrawingSequence's height.
      */
    public abstract int height();

    /** Displays the DrawingSequence's current frame at the given location.
      * Subclassers must implement this method to display the current frame.
      */
    public abstract void drawAt(Graphics g, int x, int y);



/* archiving */


    /** Describes the DrawingSequence class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.DrawingSequence", 1);
        info.addField(OWNER_KEY, OBJECT_TYPE);
        info.addField(NAME_KEY, STRING_TYPE);
        info.addField(CURRENTFRAME_KEY, INT_TYPE);
        info.addField(FRAMECOUNT_KEY, INT_TYPE);
        info.addField(FRAME_RATE_KEY, INT_TYPE);
        info.addField(PLAYBACKMODE_KEY, INT_TYPE);
        info.addField(ANIMATING_KEY, BOOLEAN_TYPE);
        info.addField(RESETONSTART_KEY, BOOLEAN_TYPE);
        info.addField(RESETONSTOP_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the DrawingSequence instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(OWNER_KEY, (Codable)owner);

        encoder.encodeString(NAME_KEY, name);

        encoder.encodeInt(CURRENTFRAME_KEY, currentFrameNumber);
        encoder.encodeInt(FRAMECOUNT_KEY, frameCount);
        encoder.encodeInt(FRAME_RATE_KEY, frameRate);
        encoder.encodeInt(PLAYBACKMODE_KEY, playbackMode);

        encoder.encodeBoolean(ANIMATING_KEY, animating);
        encoder.encodeBoolean(RESETONSTART_KEY, resetOnStart);
        encoder.encodeBoolean(RESETONSTOP_KEY, resetOnStop);
    }

    /** Decodes the DrawingSequence instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        owner = (DrawingSequenceOwner)decoder.decodeObject(OWNER_KEY);

        name = decoder.decodeString(NAME_KEY);

        currentFrameNumber = decoder.decodeInt(CURRENTFRAME_KEY);
        frameCount = decoder.decodeInt(FRAMECOUNT_KEY);
        frameRate = decoder.decodeInt(FRAME_RATE_KEY);
        playbackMode = decoder.decodeInt(PLAYBACKMODE_KEY);

        animating = decoder.decodeBoolean(ANIMATING_KEY);
        resetOnStart = decoder.decodeBoolean(RESETONSTART_KEY);
        resetOnStop = decoder.decodeBoolean(RESETONSTOP_KEY);
    }

    /** Finishes the DrawingSequence decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
        if (animating) {
            animating = false;
            start();
        }
    }
}

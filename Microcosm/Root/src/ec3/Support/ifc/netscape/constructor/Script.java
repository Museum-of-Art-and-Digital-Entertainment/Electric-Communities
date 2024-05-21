// Script.java
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.constructor;

import netscape.util.*;
import netscape.application.*;
import netscape.javascript.*;

/** Object representing a JavaScript script. The JavaScript code can be
  * set through setScriptText() and the code can be executed by calling
  * run() or through the performCommand() method. When the script is run,
  * any Constructor named objects will be published to the window's JavaScript
  * scope. This allows you to reference any named component in the .plan file
  * from the JavaScript code. Note that this only works under Netscape
  * Navigator.  When the script finishes executing, the names are removed from
  * the window's scope.
  *
  */
public class Script implements Target, Codable {
    Target              target;
    String              script, command;
    boolean             running;
    static JSObject     jsObject;
    static boolean      neverAttempted;
    boolean             usingLiveConnect;
    Hashtable           namedObjects;

    /** Command to execute the Script contained in this Script.*/
    public static final String  RUN_COMMAND = "runScript";

    static final String         SCRIPT_KEY = "JavaScript";
    static final String         TARGET_KEY = "onScriptFinish";
    static final String         COMMAND_KEY = "command";
    static final String         LIVECONNECT_KEY = "liveConnect";

    static {
        jsObject = null;
        neverAttempted = true;
    }

    /* Creates a new Script object with Script Text equal to an empty string.*/
    public Script() {
        setScriptText("");
        usingLiveConnect = true;
    }

    /* Creates a new Script object */
    public Script(String scriptText) {
        setScriptText(scriptText);
        usingLiveConnect = true;
    }

    /** Sets the JavaScript code. */
    public void setScriptText(String scriptText) {
        script = scriptText;
    }

    public String scriptText() {
        return script;
    }

    /** Sets the object to notified when the Script finishes execution.*/
    public void setTarget(Target aTarget) {
        target = aTarget;
    }

    public Target target() {
        return target;
    }

    /** Sets the command to send to <b>target</b> when the Script finishes execution.*/
    public void setCommand(String command) {
        this.command = command;
    }

    public String command() {
        return command;
    }

    /** If the Script will be using LiveConnect and wants to have name access to the
      * components within this plan file, set this variable to true.
      * The nameToComponent values from the .plan file will be put in the JavaScript
      * scope before the execution of the script. These variables will be removed upon
      * completetion of the Script.
      * @private
      */
    public void setUsingLiveConnect(boolean value) {
        usingLiveConnect = value;
    }

    /**
      * @private
      */
    public boolean isUsingLiveConnect() {
        return usingLiveConnect;
    }

    /** The Hashtable of names to be placed in the JavaScript scope before execution. */
    public Hashtable namedObjects() {
        return namedObjects;
    }

    /** This will shallow copy the names hashtable into an internal hashtable. */
    public void setNamedObjects(Hashtable names)    {
        if(names != null)
            namedObjects = (Hashtable)names.clone();
        else
            namedObjects = null;
    }

    /** Called prior to running the script, sets up the variables if unsing LiveConnect. */
    synchronized void setRunning(boolean flag) {
        running = flag;
        if(isUsingLiveConnect())    {
            if(running)
                setNames(namedObjects());
            else
                removeNames(namedObjects());
        }
    }

    /** Returns true if the JavaScript is currently executing.*/
    public synchronized boolean isRunning() {
        return running;
    }

    /** Executes the JavaScript.*/
    public void run() {
        setRunning(true);

        if (jsObject() != null) {
            try {
                jsObject.eval(script);
            } catch (Exception e)   {
                System.err.println("JSObject.eval() failed for: "
                                        + this);
                System.err.println("     " + e + ": message: " + e.getMessage());
            }
        } else {
            System.err.println("Could not call JSObject.eval() for: "
                                    + this);
        }

        setRunning(false);

        if (target != null && command != null) {
            target.performCommand(command, this);
        }
    }

    /* target */
    public void performCommand(String command, Object anObject) {
        if (RUN_COMMAND.equals(command)) {
            run();
        }
    }

    /** Publishes the names in nameTable to the JavaScript namespace.
      * <b>nameTable</b> is a Hashtable of name->component mappings.
      * @private
      */
    public boolean setNames(Hashtable nameTable) {
        Enumeration     keys;
        Object          nextKey;

        if (nameTable != null && jsObject() != null) {
            keys = nameTable.keys();
            while (keys.hasMoreElements()) {
                nextKey = keys.nextElement();
                try {
                    jsObject.setMember((String)nextKey, nameTable.get(nextKey));
                } catch (Exception e)   {
                    System.err.println("Could not setMember to JavaScript" + e + " - " + e.getMessage());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Removes the names in nameTable from the JavaScript namespace.
      * <b>nameTable</b> is a Hashtable of name->component mappings.
      * @private
      */
    public boolean removeNames(Hashtable nameTable) {
        Enumeration     keys;
        Object          nextKey;

        if (nameTable != null && jsObject() != null) {
            keys = nameTable.keys();
            while (keys.hasMoreElements()) {
                nextKey = keys.nextElement();
                try {
                    jsObject.removeMember((String)nextKey);
                } catch (Exception e)   {
                    System.err.println("Could not removeMember to JavaScript" + e + " - " + e.getMessage());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /** Returns the JSObject for this application. Will return null on failure.
      * @private
      */
    static JSObject jsObject() {
        if(jsObject == null && neverAttempted)  {
            neverAttempted = false;
            try {
                jsObject = JSObject.getWindow(AWTCompatibility.awtApplet());
            } catch (Exception e) {
                System.out.println(e);
                jsObject = null;
            } catch (Error e) {
                System.out.println(e);
                jsObject = null;
            }
        }
        return jsObject;
    }


    /* archiving */


    /** Describes the Script class' coding info.
      * @private
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.constructor.Script", 2);
        info.addField(SCRIPT_KEY, STRING_TYPE);
        info.addField(TARGET_KEY, OBJECT_TYPE);
        info.addField(COMMAND_KEY, STRING_TYPE);
        info.addField(LIVECONNECT_KEY, BOOLEAN_TYPE);
    }

    /** Encodes the Script.
      * @private
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeString(SCRIPT_KEY, script);
        encoder.encodeObject(TARGET_KEY, (Codable)target);
        encoder.encodeString(COMMAND_KEY, command);
        encoder.encodeBoolean(LIVECONNECT_KEY, usingLiveConnect);
    }

    /** Decodes the Script.
      * @private
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        script = decoder.decodeString(SCRIPT_KEY);
        target = (Target)decoder.decodeObject(TARGET_KEY);
        command = decoder.decodeString(COMMAND_KEY);
        if (decoder.versionForClassName("netscape.constructor.Script") > 1)
            usingLiveConnect = decoder.decodeBoolean(LIVECONNECT_KEY);
        else
            usingLiveConnect = true;
    }

    /** Finishes the Script decoding.  This method does nothing.
      * @private
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }


}

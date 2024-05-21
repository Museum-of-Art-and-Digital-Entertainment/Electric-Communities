// TargetProxyManager.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.constructor;

import netscape.util.*;
import netscape.application.*;

/** This class manages the hashtable of names for the TargetProxy objects.
  * The TargetProxy objects ask this object if there is a real object
  * to replace them during unarching. This object automatically adds the
  * Application.application() and TargetChain.applicationChain() objects to
  * the hashtable under special names.
  *
  * @private
  */
public class TargetProxyManager implements Codable {
    Hashtable targets;
    boolean neverReplace;
    boolean hasSingleTarget;

    final static String TARGETS_KEY = "targets";

    /** If targets() contains a value for this key, then all requests from
      * targetNamed() other than application and TargetChain will return
      * the object with this key. This is normally only used by Plan, when
      * it has been asked to unarchiveObjects() with a single Target value.
      * @private
      */
    public static final String SINGLE_TARGET_PROXY_KEY = "__nEdEtCoDe";

    /** Key used in the TargetProxy Hashtable to map the Application.application()
      * object into the TargetProxies.
      */
    public static final String APPLICATION_TARGET_PROXY_KEY = "__APPLICATION__";

    /** Key used in the TargetProxy Hashtable to map the
      * TargetChain.applicationChain() object into the TargetProxies.
      */
    public static final String TARGET_CHAIN_TARGET_PROXY_KEY = "__TARGETCHAIN__";

    /** Never replace any TargetProxy object. Return null for all cases.
      * Useful in supressing the replacement behavior.
      * @private
      */
    public static final String NEVER_REPLACE_KEY = "__nEdEtCoDeNonReplacing";


    /* constructors */
    public TargetProxyManager() {
    }

    /* methods */

    /** The hashtable of names to real application objects */
    public Hashtable targets()    {
        if(targets == null)
            targets = new Hashtable();
        return targets;
    }

    /** Copies the contents of newTargets into the internal Hashtable.
      * All elements in newTargets MUST implement the Target iterface.
      * Elements that do not, will not be copied into the internal Hashtable.
      * A warning message will print on System.err.
      */
    public void setTargets(Hashtable newTargets) {
        Enumeration enumeration;
        Object key;

        targets().clear();

        if(newTargets == null)
            return;

        neverReplace = newTargets.containsKey(NEVER_REPLACE_KEY);
        if(neverReplace)
            return;

        enumeration = newTargets.keys();
        while (enumeration.hasMoreElements()) {
            key = enumeration.nextElement();
            if(newTargets.get(key) instanceof Target)   {
                targets.put(key, newTargets.get(key));
            } else  {
                System.out.println("TargetProxyManager: setTargets: "
                                    + "Non-Target object found, will not be added"
                                    + ": name = "  + key
                                    + "\n, object = " + newTargets.get(key));

            }
        }

        hasSingleTarget = targets.containsKey(SINGLE_TARGET_PROXY_KEY);
    }

    /** Returns the object called <b>name</b>.
      * If <B>targets()</B> does not contain <B>APPLICATION_TARGET_PROXY_KEY</B> and
      * Application.application() implements Target, it will be returned.
      * If <B>targets()</B> does not contain <B>TARGET_CHAIN_TARGET_PROXY_KEY</B> and
      * TargetChain.applicationChain() will be returned.
      * If <B>targets()</B> contains <B>SINGLE_TARGET_PROXY_KEY</B>, it will be returned
      * for all other requests.
      */
    public Target targetNamed(String name)  {

        if(targets == null || neverReplace) {
            return null;
        }

        if(APPLICATION_TARGET_PROXY_KEY.equals(name)
            && !targets.containsKey(APPLICATION_TARGET_PROXY_KEY))  {
            if (Application.application() instanceof Target)
                return (Target)Application.application();
            return null;
        }

        if(TARGET_CHAIN_TARGET_PROXY_KEY.equals(name)
            && !targets.containsKey(TARGET_CHAIN_TARGET_PROXY_KEY)) {
            return TargetChain.applicationChain();
        }

        if(hasSingleTarget)    {
            return(Target)targets.get(SINGLE_TARGET_PROXY_KEY);
        }

        return (Target)targets.get(name);
    }


    /* archiving */

    /** Describes the coding info.
      * @see Codable#describeClassInfo
      * @private
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.constructor.TargetProxyManager", 1);
        info.addField(TARGETS_KEY, Codable.OBJECT_TYPE);
    }

    /** Encodes the object.
      * @see Codable#encode
      * @private
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(TARGETS_KEY, targets);
    }

    /** Decodes the object.
      * @see Codable#decode
      * @private
      */
    public void decode(Decoder decoder) throws CodingException {
        setTargets((Hashtable)decoder.decodeObject(TARGETS_KEY));
    }

    /** Finishes the decoding.  This method does nothing.
      * @see Codable#finishDecoding
      * @private
      */
    public void finishDecoding() throws CodingException {
    }

}

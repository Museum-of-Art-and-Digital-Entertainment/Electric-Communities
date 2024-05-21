package ec.e.inspect;

import ec.vcache.ClassCache;
import ec.e.file.EStdio;
import ec.e.openers.*;
import ec.e.run.OnceOnlyException;
import ec.e.run.RtQ;
import ec.e.run.RtQObj;
import ec.e.start.EEnvironment;
import ec.e.start.MagicPowerMaker;
import ec.e.start.SmashedException;
import ec.e.start.Tether;
import ec.e.start.Vat;
import ec.e.start.crew.CrewCapabilities;
import ec.util.NestedException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970401
 */

/**

  * The Inspector class serves two purposes: As a dynamic class, it
  * encapsulates inspectability of a class or an Array. To inspect an
  * object, you create an inspector for it and hand it to an
  * InspectorUI. The InspectorUI reference is kept in a static
  * variable in Inspector.  The IFC-based Inspector UI is one possible
  * and useful value for this.

  * As a static class it contains methods to collect and manage global
  * collections of inspectable objects, gathered using the gather()
  * methods, and a collection of already inspected objects to ensure
  * that there is only one instance of inspector and Inspector UI
  * (window) for each object.

  * If gatherEnabled is false, then gathering is disabled. Gathering
  * objects can (potentially) be done before defining an inspector UI.

  */

public class Inspector {

    protected Object inspectedObject;     // The object we are inspecting
    protected String myObjectName;        // The name we know the object by
    protected Class  myClazz;             // The class of inspectedObject

    // Static variables to manage a set of inspectable objects

    protected static Hashtable objectWindows = null;
    protected static Tether myGatherer = null;
    protected static Gatherer myCrewGatherer = null;
    protected static Vector runQueueInspectorTopLevel = new Vector(200);
    protected static InspectorUI inspectorUI = null;
    public static int nextGensym = 0;
    private static RunQueueInspector theRunQueueInspector = null;
    private static boolean initialized = false;
    private static int initialRQIHold = RunQueueInspector.HOLD_SOME;

    private static boolean gatherEnabled = true; // Gather is a no-op unless this gets set

    public static final int INSPECT = 1; // Used as an event type to UI
    public static final int REFRESH_RUNQUEUE = 2; // Another event type to UI
    public static final int REFRESH_HOLDSTATE = 3; // Another event type to UI
    public static final int INVALIDATE_RUNLET = 4; // Another event type to UI
    public static final int PROFILE_RUNQUEUE = 5; // Another event type to UI
    public static final int PROFILE_EXECUTION = 6; // Another event type to UI
    public static int dumpLineNo = 0;  // A dump line number.

    // initialize static state

    private static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        VarOpener.staticSelfTest(); // Force native opener library load immediately
        myGatherer = getTheGatherer(); // Get one using a magic power
        myCrewGatherer = new Gatherer(); // Just make one for the crew.
    }

    //  Constructors

    protected Inspector(Object object, String objectName) {
        this.inspectedObject = object;
        this.myObjectName = objectName;
        this.myClazz = inspectedObject.getClass();
    }

    private static Tether getTheGatherer() {
        try {
            Class makerClass = GathererMaker.class;
            MagicPowerMaker maker = (MagicPowerMaker)makerClass.newInstance();
            return new Tether(CrewCapabilities.getTheVat(),
                              maker.make(CrewCapabilities.getTheEnvironment()));
        } catch (ClassNotFoundException x) {
            throw new NestedException("RemoteRetriever class missing??", x);
        } catch (IllegalAccessException x) {
            throw new NestedException("RemoteRetriever access exception??", x);
        } catch (InstantiationException x) {
            throw new NestedException("RemoteRetriever did not instantitate??", x);
        }
    }

    /**

     * Construct a reasonably short descriptive name for any object
     * that we can use when no other information is available.

     */

    public static String objectName(Object object) {
        if (object == null) return "<null>";
        Class clazz = object.getClass();
        String result = clazz.getName();
        if (runningUnderDebuggingVM()) {
            result += "@" + Inspector.addressString
              (Inspector.addressOf(object),1);
        } else result += "#" + object.hashCode();
        return result;
    }

    /**

     * Dump an object, and all objects it (transitively) refers to to
     * a given PrintStream. This is a convenience method that calls
     * dumpRoot() if you only want to dump one simple object without
     * cutting off anywhere.

     */

    public static void dumpObject(Object object, PrintStream os) {
        Inspector.dumpLineNo = 0;
        Hashtable printedObjects = new Hashtable(8000);
        if (object != null) dumpRoot(object,"Unnamed",os, printedObjects);
        os.println("Total number of objects dumped: " + printedObjects.size());
    }

    /**

     * Dump the inspected object, and all objects it (transitively)
     * refers to to a given PrintStream. This is a convenience method
     * that calls dumpRoot() if you only want to dump one simple
     * object without cutting off anywhere.

     */

    public void dumpObject(PrintStream os) {
        Inspector.dumpLineNo = 0;
        Hashtable printedObjects = new Hashtable(8000);
        dumpRoot(inspectedObject,myObjectName,os, printedObjects);
        os.println("Total number of objects dumped: " + printedObjects.size());
    }

    /**

     * Dump an object, and all objects it (transitively) refers to to
     * a given PrintStream.  A Hashtable is also passed in to keep
     * track of objects that have already been printed. To cutoff
     * printing at certain objects you could add them to the passed-in
     * hashtable before calling dump() (kind of like a parimeter table).

     * dumpRoot() prints the top level info for its object and then
     * calls dumpSubitems() to dump the parts of the object.

     */

    public static void dumpRoot(Object object, String rootName, PrintStream os, Hashtable printed) {
        String prefix = "*";    // Prefix on each line after line number
        String label = rootName + " line " + Inspector.dumpLineNo + " = " + object.toString();
        printed.put(object,label);
        String signature = humanizeSignature(object.getClass().toString().substring(6),object);
        os.println(prefix + " " + Inspector.dumpLineNo++ + " " + signature + " " + rootName + " = " + object.toString());
        Inspector inspector = createInspectorForObject(object,rootName); // Inspector for the object
        if (inspector != null) inspector.dumpSubItems(os,printed,0,prefix + "*");
    }

    /**
     * Returns the dump lineno as a fixed width string
     */

    private static String width5(int i) {
        String result = Integer.toString(i);
        if (i >= 100000) return result;
        return "     ".substring(result.length()) + result;
    }

    /**

     * dumpSubItems dumps out names and values for all subitems for a
     * given object. The object itself should be dumped by the caller.

     */

    public void dumpSubItems(PrintStream os, Hashtable printed, int recursionDepth, String prefix) {
        int numberSubObjects = getNumberFields();
        if (recursionDepth > 120) {
            os.println(prefix + " " + Inspector.dumpLineNo++ + " " + "PRUNED ****");
            return;
        }

        for (int i = 0; i< numberSubObjects; i++) {
            Object sub = get(i); // The subObject
            String subName = getName(i); // Instance variable name (or array index)
            String subSignature = humanizeSignature(getAssignedSignature(i),sub); // Assigned signature
            if (subSignature.startsWith("java.lang.")) subSignature = subSignature.substring(10);
            if (isReference(i)) {
                if (sub == null)
                    os.println(prefix + " " + Inspector.dumpLineNo++ + " " + subSignature + " " + subName+ " = null");
                else if (sub instanceof String) {
                    // We need to count the string as a reference object. And save it in the table!
                    // We still print it out, rather than point to the previous occurrence.
                    String strValue = (String)sub;
                    if (strValue.length() > 200) strValue = strValue.substring(0,197) + "...";
                    String label = subName + " line " + Inspector.dumpLineNo +
                      " = \"" + strValue + "\"";
                    printed.put(sub,label); // Save label as value of this string!
                    os.println(prefix + " " + Inspector.dumpLineNo++ + " " + "String " +
                               subName + " = \"" + strValue +"\"");
                } else {        // Bona fide object
                    String label = (String)printed.get(sub);
                    if (label != null) {
                        os.println(prefix + " " + Inspector.dumpLineNo++ + " " + subSignature +
                                   " " + subName + " ----> " + label);
                    } else {
                        String strValue = sub.toString();
                        os.println(prefix + " " + Inspector.dumpLineNo++ + " " +
                                   subSignature + " " +
                                   subName + " = " + strValue);
                        if (strValue.length() > 200)
                            strValue = strValue.substring(0,197) + "...";
                        label = subName + " line " + Inspector.dumpLineNo + " = " + strValue;
                        printed.put(sub,label);
                        if (sub.getClass().isArray()) {
                            Array arr = null;
                            try {
                                arr = (Array)sub;
                            }
                            catch (ClassCastException e) {
                                os.println("??? array (not dumped)");
                                continue;
                            }
                            if (java.lang.reflect.Array.getLength(arr) == 0)
                                os.println("Array of length 0");
                            else {
                                Object o = java.lang.reflect.Array.get(arr,0);
                                if (o instanceof java.lang.Byte) {
                                    os.println("Byte array (not dumped)");
                                    continue;
                                }
                                if (o instanceof Integer) {
                                    os.println("Integer array (not dumped)");
                                    continue;
                                }
                                if (o instanceof Long) {
                                    os.println("Long int array (not dumped)");
                                    continue;
                                }
                                if (o instanceof Double) {
                                    os.println("Double array (not dumped)");
                                    continue;
                                }
                                if (o instanceof Float) {
                                    os.println("Float array (not dumped)");
                                    continue;
                                }
                            }
                        }
                        Inspector subInsp = createInspector(i); // Create subitem inspector;
                        if (subInsp == null) {
                            os.println(prefix + " " + Inspector.dumpLineNo++ + " " + "Can't create inspector!");
                        }
                        else {
                            subInsp.dumpSubItems(os, printed, recursionDepth + 1, prefix + "*");
                        }
                    }
                }
            } else {            // Primitive datatypes (Wrapped, now)
                os.println(prefix + " " + Inspector.dumpLineNo++ + " " + subSignature +
                           " " + subName + " = " + sub.toString());

            }
        }
    }

    /**

     * Immediately inspect an object with a given name, using whatever
     * user interface has been set up. If no user interface is
     * available, then this is a no-op - The rationale is that since
     * this is mostly used for debugging, and there may be many
     * reasons to disable this feature (including user preference and
     * security considerations) the unability to Inspect should never
     * annoy an innocent user. Developers will be savvy enough to
     * figure out why the Inspector does not work - typically noone
     * has called setUI() which typically means noone has created the
     * GUI half of the Inspector (currently typically based on
     * IFC). Note that for the IFC GUI case it may have been created
     * and linked in as a UI but may not appear on the screen until it
     * has been asked to inspect something.

     * @param object, nullOK, suspect - The object to be
     * inspected. If the object is null, then this call has no effect
     * besides (if not done before) initializing the Inspector's UI
     * and thereby possibly forcing an inspector control panel window
     * to appear in the case of a Graphical UI such as IFC.

     * @param name nullOK, suspect - A string to use as the name of
     * the object. If the name is null, we use the name "Object". If
     * the name is already used for a different object, then we will
     * add a uniquefying integer to the name.

     * @Note This is a major capability and security leak. It should
     * still be a static method, but the level to which it is
     * effective should be controlled from the TCB.

     */

    public static void inspect(Object object, String name) {
        if (inspectorUI != null) inspectorUI.inspectObject(object,name);
    }

    public static void setHoldUpdateUI(int holdState) {
        if (theRunQueueInspector != null) {
            theRunQueueInspector.setHoldUpdateUI(holdState);
        }
        else initialRQIHold = holdState; // Must defer this until we create RQI (below).
    }

    /**

     * Create a runqueue inspector - enable runqueue inspection

     */

    public static synchronized void setupRunQueueInspector()
         throws OnceOnlyException {
             if (theRunQueueInspector != null) return; // Already set
             if (inspectorUI == null) return; // No UI, can't do it yet
             theRunQueueInspector = new RunQueueInspector
               (inspectorUI, runQueueInspectorTopLevel);
             theRunQueueInspector.setHoldUpdateUI(initialRQIHold);
             RtRun.theOne().setRunQueueDebugger(theRunQueueInspector);
             inspectorUI.refreshRunqueueDisplay(runQueueInspectorTopLevel);
    }

    public static void refreshRunqueueDisplay(Object target) {
        if (theRunQueueInspector != null)
            theRunQueueInspector.refreshRunqueueDisplay(target);
    }

    /**
     * Enable or disable gathering of objects
     */

    public static void enableGathering(boolean enabled) {
        gatherEnabled = enabled;
    }

    /**

     * Forget all objects we've gathered. You may want to do this if
     * you are performing some measurements or want to clean out
     * memory for some other reason. Note that the easiest way to
     * accomplish this is to not gather any objects at all in the
     * first place by turning the inspector off.

     */

    public void forgetAllGatheredObjects() {
        try {
            ((Gatherer)myGatherer.held()).forgetAllGatheredObjects();
        } catch (SmashedException smex) { // Can't happen
        }
        myCrewGatherer.forgetAllGatheredObjects();
    }

    /**

     * gather() (in its six variants - three in guest.Inspector and
     * three in crew.inspector) will gather the 'object' argument into
     * a hashtable under a given or generated string key. The user can
     * select any of these collected objects for inspection at any
     * time.

     * The methods below are the backward-comptible methods which will
     * be deprecated as soon as the buildenvironment has been updated.

     */

    /**

     * gather - collect an object for possible later inspection.

     * @param object, nullOK, suspect - The object to be
     * gathered. You can only mark any object for inspection once so
     * if it is already been marked in another category or by another
     * name then another marking will be ignored. Not always what you
     * want, but allowing duplicates would use lots of hashtable entries
     * if you attempted to mark an object for inspection when in a
     * loop. Also, please note that any objects you mark for
     * inspection will never be garbage collected since the inspector
     * holds a reference to it. Therefore there exists a way to delete
     * all entries from the gathering hashtables.

     * @param category, nullOK, suspect - A string category name;
     * like a folder that can be used to organize objects to be
     * inspected. It may be null.

     * @param name, nullOK, suspect - A string to use as the name of
     * the object. If the name is already used in the category given
     * (if any) by another object then we add a unique integer to its
     * name.

     * @Note This is a major capability and security leak. There
     * should probably be a capability required to gather objects to
     * each category, and each of the categories should be inspectable
     * only if you have the capability to that category at inspection
     * request time. This all should be controlled from the TCB.

     */

    public static void gather(Object iobj, String category, String name) {
        Object oldObject;
        Hashtable catTable;

        if (iobj == null) return; // Can't store these in a hashtable
        if (name == null) name = "Object";

        // Gathering shold be possible from any thread, so we synchronize on all our tables

        synchronized(ec.e.inspect.Inspector.getGatheredObjects()) { // Inspected guest objects

            // See if the object already exists in the global object collection

            Object oldName = ec.e.inspect.Inspector.getGatheredObjects().get(iobj);
            if (oldName != null) return; // Object has already been marked for inspection.

            // Supplying a null category means we should collect to the "uncategorized" table

            if (category == null) catTable = ec.e.inspect.Inspector.getObjectsWithoutCategory();
            else {
                synchronized(ec.e.inspect.Inspector.getObjectCategories()) {
                    catTable = (Hashtable)ec.e.inspect.Inspector.getObjectCategories().
                      get(category);
                    if (catTable == null) { // No entries under this category name before
                        catTable = new Hashtable(10); // so add the category
                        ec.e.inspect.Inspector.getObjectCategories().
                          put(category,catTable); // and save the category table
                    }
                }
            }
        }

        // catTable is now our target Hashtable.

        synchronized(catTable) {
            oldObject = catTable.get(name); // Check if some other object has used this name here
            if (oldObject != null) // if so, add uniqueifying number
                name = name + " " + ec.e.inspect.Inspector.nextGensym++;
            catTable.put(name,iobj); // Gather the object
        }
    }

    /**

     * gather - Collect an object that we don't want to classify in
     * any category for possible inspection sometime later.

     * @param object, nullOK, suspect - The object to be
     * gathered. You can only mark any object for inspection once so
     * if it is already been marked in another category or by another
     * name then another marking will be a no-op. Not always what you
     * want. Allowing duplicates would use lots of hashtable entries
     * if you attempted to mark an object for inspection when in a
     * loop. Also, please note that any objects you mark for
     * inspection will never be garbage collected since the inspector
     * holds a reference to it. Therefore there exists a way to delete
     * all entries from the gathering hashtables.

     * @param name, nullOK, suspect - A string to use as the name of
     * the object. If the name is already used in the category given
     * (if any) by another object then we add a unique integer to its
     * name.

     */

    public static void gather(Object object, String name) { gather(object,null,name); }

    /**

     * Compare our inspected object against another object. Provided for Inspector UI.
     * @param object, nullOk, suspect - Object to compare our inspected object to.

     */

     public boolean isInspecting(Object object) {
        return (inspectedObject == object);
    }

    /**

     * Determine whether our inspected object is an E object.

     */

     public boolean isEObject() {
        return (inspectedObject instanceof EObject);
    }

    /**

     * Mark/unmark our object as a watched source or target in the
     * RunQueueInspector's tables

     */

    public boolean isWatchedSource() {
        if (theRunQueueInspector == null) return false;
        return RunQueueInspector.watchedSource(inspectedObject);
    }

    public boolean isWatchedTarget() {
        try {
            if (theRunQueueInspector == null) setupRunQueueInspector();
        } catch (OnceOnlyException ooe) {
        }
        return RunQueueInspector.watchedTarget(inspectedObject);
    }

    public void watchAsSource() {
        try {
            if (theRunQueueInspector == null) setupRunQueueInspector();
        } catch (OnceOnlyException ooe) {
        }
        RunQueueInspector.watchSource(inspectedObject);
    }

    public void watchAsTarget() {
        try {
            if (theRunQueueInspector == null) setupRunQueueInspector();
        } catch (OnceOnlyException ooe) {
        }
        RunQueueInspector.watchTarget(inspectedObject);
    }

    public void dontWatchAsSource() {
        if (theRunQueueInspector == null) return;
        RunQueueInspector.dontWatchSource(inspectedObject);
    }

    public void dontWatchAsTarget() {
        if (theRunQueueInspector == null) return;
        RunQueueInspector.dontWatchTarget(inspectedObject);
    }

    public void setRunQueueTargetBreakState(boolean newState) {
        try {
            if (theRunQueueInspector == null) setupRunQueueInspector();
        } catch (OnceOnlyException ooe) {
        }
        if (newState) watchAsTarget();
        else dontWatchAsTarget();
    }

    /**

     * Provided for Inspector UI.<p> XXX Major capability leak and
     * security hole. Anyone can access all gathered uncategorized
     * objects in the system.

     */

    public static Hashtable getObjectsWithoutCategory() {
        if (myGatherer == null) initialize();
        while (true) {
            try {
                return ((Gatherer)myGatherer.held()).objectsWithoutCategory;
            } catch (SmashedException smex) {
                initialize();
            }
        }
    }

    /**

     * Provided for Inspector UI. <p> XXX Major capability leak and
     * security hole. Anyone can access all gathered categorized
     * objects in the system.

     */

    public static Hashtable getObjectCategories() {
        if (myGatherer == null) initialize();
        while (true) {
            try {
                return ((Gatherer)myGatherer.held()).objectCategories;
            } catch (SmashedException smex) {
                initialize();
            }
        }
    }

    /**

     * Provided for Inspector UI. <p> XXX Major capability leak and
     * security hole. Anyone can access all gathered objects in the
     * system.

     */

    public static Hashtable getGatheredObjects() {
        if (myGatherer == null) initialize();
        while (true) {
            try {
                return ((Gatherer)myGatherer.held()).gatheredObjects;
            } catch (SmashedException smex) {
                initialize();
            }
        }
    }

    public static Hashtable getCrewObjectCategories() {
       if (myCrewGatherer == null) initialize();
       return myCrewGatherer.objectCategories;
    }

    public static Hashtable getGatheredCrewObjects() {
       if (myCrewGatherer == null) initialize();
       return myCrewGatherer.gatheredObjects;
    }

    public static Hashtable getCrewObjectsWithoutCategory() {
       if (myCrewGatherer == null) initialize();
       return myCrewGatherer.objectsWithoutCategory;
    }

    /**

     * Assign an Inspector UI to the Inspector.

     * <p> XXX Major capability leak and security hole. Anyone can call this method and
     * thereby gain access to all inspected objects in the system.

     * Ability to set oneself up as an inspector in a running system
     * should be a closely held capability.

     * We attempt to ward off some such attempts by requiring that
     * whoever calls this, has the existing reference, or can guess it
     * (as in the case of null).

     */

    public static void setUI(InspectorUI oldInspectorUI, InspectorUI newInspectorUI)
         throws OnceOnlyException {
             if (Inspector.inspectorUI == oldInspectorUI) {
                 Inspector.inspectorUI = newInspectorUI;
             }
             else throw new OnceOnlyException("Attempt to set inspector user interface to " +
                                              newInspectorUI +
                                              " but it was already set by someone else");
    }

    /**

     * Create an inspector for a given object. Provided as a low-level
     * call to be used only from the InspectorUI subsystem.

     * <p> XXX Major security hole. Anyone can (currently) access internal
     * state by inspecting any object they can reach in the system.
     * Access to this capability should be limited to a debugging
     * situation.

     * Access to object internals should be limited by the openers you
     * possess. Here we use the global root opener and can therefore
     * open anything we have a reference to.

     * @param object suspect - The object to inspect. The object cannot be null.
     * @param name suspect - The name of the object. The name cannot be null.

     */

    public static Inspector createInspectorForObject(Object object, String name) {
        Inspector result = null;
        ObjOpener opener;

        try {
            opener = RootClassRecipe.ROOT_OPENER_RECIPE.forEncoding(object);
        } catch (Error err) {
            err.printStackTrace();
            return null;
        }
        if (object instanceof Hashtable)
            result = new HashtableInspector(object,name,opener);
        else if (object instanceof Vector)
            result = new VectorInspector(object,name,opener);
        else if (object instanceof TraceInfo[])
            result = new ReferencersInspector((TraceInfo[])object,name,opener);
        else if (opener instanceof ArrayOpener)
            result = new ArrayInspector(object,name,(ArrayOpener)opener);
        else if (opener instanceof Surgeon)
            result = new ObjectInspector(object, name, opener);
        //else {
        //    else what?
        //}

        return result;
    }

    /**

     * Create another inspector inspecting the same object but
     * (in future) using a different opener. Note that we
     * need a way to specify which opener to use so this choice will
     * (eventually) be passed in as as an argument.

     */

    /* XXX Add argument to determine what opener to use */

    public Inspector reCreateInspector() {
         return createInspectorForObject(inspectedObject, myObjectName);
     }

    /**

     * Some methods to help present the data in the GUI

     */

    /**

     * Return the name of the inspected object itself (the top level
     * name, as opposed to names of specific fields)

     */

    public String getMyObjectName() { return myObjectName; }

    /*
     * Return the inspected object. XXX Security problem, don't use this.
      /// public Object getInspectedObject() { return inspectedObject; }
     */

    /**

     * Return the number of instance variables (visible using our
     * current opener) in the inspected object or array elements if
     * the inspected object is an array. Basically, this number will
     * likely determine the number of "lines" of visible output in the
     * Inspector UI used.

     */

    public boolean isEditable(int n) { return true; } // XXX Default for now

    /** Tells whether a field with a given index is a reference object or not */

    public boolean isReference(int n) {
        char sig0char = getDeclaredSignature(n).charAt(0);
        return ((sig0char == 'L') || (sig0char == '[')); // if Array or Object, return true
    }

    /**

     * Create an inspector for the data in a field, given the field
     * index.

     */

    public Inspector createInspector(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {

         Object nextObject = get(fieldIndex);
         String name = getName(fieldIndex);
         return Inspector.createInspectorForObject(nextObject, name);
    }

    /**

     * Return the signature of the actually assigned data in a field.
     * Note that this may differ from the declared signature in some
     * cases, like if a field is declared a reference to some class of
     * object and it has been assigned an object of some subclass
     * thereof

     */

    public String getAssignedSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             String sig = getDeclaredSignature(fieldIndex);
             if (sig.charAt(0) != 'L') return sig; // Non-Object signatures are correct.
             Object value = get(fieldIndex);
             // if (value == null) return "N"; // Object was null
             if (value == null) return sig; // Object was null, trust declared signature
             return JavaUtil.signature(value.getClass().getName()); // Compute signature of value itself
    }

    /**

     * Methods for indexed data access. For Objects, the
     * index is the index of the instance variable accessible through
     * the current opener. For arrays, it's the actual element index. <p>

     * These should really have been declared abstract here but then
     * the static methods won't work before instantiation, which is
     * unacceptable

     */

    /**

     * Return the number of fields (slots or array elements) in the object or array

     */

    public int getNumberFields() {
        throw new RuntimeException
          ("In getNumberFields() - Inspector instantiated - Please use a subclass instead");
    }

    /**

     * Return an array of ints listing the number of visible instance variables
     * at each level.

     */

    public int[] getInheritedNumberFields() {
        throw new RuntimeException
          ("In getInheritedNumberFields() - Inspector instantiated - Please use a subclass");
    }

    /**

     * Return a String which is the class name for the class at a
     * specified superclass level. Used only by ObjectInspector,
     * others may return null.

     */

    public String getInheritedClassName(int superLevel) {
        return null;
    }
    /**

     * Return the variable name of a field.

     */

    public String getName(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             throw new RuntimeException
               ("In getName() - Inspector instantiated - Please use a subclass instead");
    }

    /**

     * Return the declared signatures for a field

     */

    public String getDeclaredSignature(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             throw new RuntimeException
               ("In getDeclaredSignature() - Inspector instantiated - Please use a subclass");
    }

    /**

     * Return the value of a field of the currently inspected Object,
     * given a field index.  Returns a reference data type. Primitive
     * data types will be wrapped.

     * @param fieldIndex - The index of the local variable field
     * according to the used opener, or the index of the element if
     * the object being inspected is an array.

     * @return Object, nullOK, suspect - the object designated by
     * fieldIndex and the used opener. Primitive data types are
     * wrapped into Objects.

     */

         /* <p> XXX No indication is given to show whether a wrapped
          * primitive data type was *originally* wrapped or got
          * wrapped by the access itself.  This may not be a problem
          * with the current opener strategy - investigate. */

    public Object get(int fieldIndex)
         throws ArrayIndexOutOfBoundsException {
             throw new RuntimeException
               ("in get() - Inspector instantiated - Please use a subclass instead");
    }

    /**

     * Set the value of a field, given a field index, and a new value.
     * Only works for newValue arguments that are reference data types
     * - primitive data types should be wrapped.

     * @param fieldIndex - The index of the local variable field
     * according to the used opener, or the index of the element if
     * the object being inspected is an array.

     * @param newValue, nullOK, trusted - The new value to assign to
     * the instance variable. Note that this variable is marked as
     * "trusted" - The inspector itself is not assuming the value to
     * be trusted but since we are adding the reference to another
     * arbitrary inspected object we are in fact relaying the
     * reference and all capabilities it embodies to the receiving
     * object.

     */

    public void set(int fieldIndex, Object newValue)
         throws ArrayIndexOutOfBoundsException {
             throw new RuntimeException
               ("In set() - Inspector instantiated - Please use a subclass instead");
    }


    /**

     * Determine whether we are running under a debug version of java
     * VM. If so, we can do some extra tricks.

     */

    public static boolean runningUnderDebuggingVM() {
        return ec.e.inspect.Tracer.runningUnderDebuggingVM();
    }


    /**

     * Parse an unmunged address string to a munged address as int

     */

    public static int parseAddress(String address) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        return Tracer.parseAddress(address);
    }

    /**

     * Print a munged integer address as an unmunged string to a minimum width.

     */

    public static String addressString(int address, int minWidth) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        return Tracer.addressString(address,minWidth);
    }

    /**

     * Return the address for an object as an integer

     */

    public static int addressOf(Object o) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        return Tracer.addressOf(o);
    }

    /**

     * Dump all memory in the heap to System.out for analysis

     */

    public static void memoryDump() {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        Tracer.memoryDump();
    }

    /**

     * Return the address for this object as an integer

     */

    public int addressOf() {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        return Tracer.addressOf(inspectedObject);
    }

    /**

     * Return the object at a certain address

     */

    public static Object objectify(int addr) {
        if (!runningUnderDebuggingVM())
            throw new RuntimeException("Not running under debug VM");
        return Tracer.objectify(addr);
    }

    /**

     * Determine whether an address actually points to a valid object

     */

    public static boolean isValidObject(int address) {
        TraceInfo[] ti = Tracer.traceAddress(address);
        if (ti == null) return false;
        if (ti.length == 0) return false;
        return true;
    }

    /**

     * Return an array of Objects that reference this one

     */

    public TraceInfo[] getReferencers() {
        int address = addressOf();

        java.lang.System.gc();  // Experiment!


        TraceInfo[] tiOne = Tracer.traceAddress(address);
        java.lang.System.gc();
        TraceInfo[] tiTwo = Tracer.traceAddress(address);

        if (tiOne == null) System.out.println("pre-gc traceaddress returned null");
        if (tiTwo == null) System.out.println("post-gc traceaddress returned null");

        if (tiOne == null) return tiTwo; // Don't try comparing contents
        if (tiTwo == null) return null; // if one result is missing

        if (tiOne.length != tiTwo.length) {
            System.out.println("Traceinfo lengths differ - " +
                               tiOne.length + " vs " + tiTwo.length);
        }
        int len = tiOne.length;
        if (len > tiTwo.length) len = tiTwo.length; // Min

        for (int i = 0; i < len ; i++) {

            if (tiOne[i] == null) continue;
            if (tiTwo[i] == null) continue;
            if (tiOne[i].address != tiTwo[i].address) {
                System.out.println("TraceInfo entrie's addresses differ: " +
                                   tiOne[i].address + " vs " + tiTwo[i].address);
                System.out.print("first collected object is ");
                Object o1 = Inspector.objectify(tiOne[i].address);
                System.out.println(o1);

                System.out.print("Second collected object is ");
                Object o2 = Inspector.objectify(tiTwo[i].address);
                System.out.println(o2);
            }
        }
        return tiTwo;           // Return the one POST GC (!).
    }

    /**

     * Dump (to error output) information about all Objects that
     * reference this one, recursively, to the top, without duplicates

     */

    public void printFullTrace() {
        ((RecursiveTraceInfo)Tracer.fullTrace(addressOf())).println
          (new PrintWriter(System.err),0);
    }

    /** A few class name cleanup utility functions.
     * These used to be in Inspector UI but they are now needed here for dump() */

    /**

     * primitiveDatatypeName returns a string denoting the datatype
     * when given a signature string, if the signature indeed denotes
     * a primitive datatype. Returns null otherwise.

     */

    public static String primitiveDatatypeName(String s) {

        // System.out.println("primitiveDatatypeName(" + s + ")");
        char c = s.charAt(0);
        if ((c=='[') || (c=='L')) return null; // Not primitive
        switch (c) {
        case 'I': return "int";
        case 'J': return "long";
        case 'Z': return "boolean";
        case 'B': return "byte";
        case 'C': return "char";
        case 'S': return "short";
        case 'F': return "float";
        case 'D': return "double";
        }
        return null;
    }

    /**

     * Convert a signature string into something human-readable The
     * object can be given to automatically add array size designators
     * to all array types.

     * @param s trusted notNull - A signature string.  @param object
     * suspect nullOK - The object to which the signature string
     * applies.  May be null, in which case you sometimes get a less
     * descriptive string.

     */

    public static String humanizeSignature(String s, Object object) {
        int cardinality = 0;
        String result = "";
        int prunePoint = 0;
        int dim;

        // System.out.println("Humanizing signature " + s + " with object " + object);

        if (s.charAt(prunePoint) == '[') { // Arrays
            while (s.charAt(prunePoint++) == '[') cardinality++; // Count brackets = array cardinality
            prunePoint--;       // Undo last increment

            s = s.substring(prunePoint); // Remove brackets, keep bottommost signature string in s

            // Final data type (after all array declaration brackets).
            // Identify bottommost primitive data types immediately
            // and recurse for all non-primitive bottommost types

            result = primitiveDatatypeName(s); // See if this is a primitive data type
            if (result == null) {
                result = humanizeSignature(s,object); // No, recurse to clean it up.
            }

            // Result is now the bottommost datatype, in human-readable form, like "int" or "String"
            // Now handle the brackets.

            if (cardinality > 0) {
                while (cardinality-- > 1) {
                    if (object == null) result = result.concat(" [ ] ");
                    else {
                        dim = ((Object[])object).length;
                        result = result.concat(" [" + dim + "] ");
                        if (dim > 0) object = ((Object[])object)[0];
                        else object = null;
                    }
                }
                if (cardinality == 0)
                    if (object == null) result = result.concat(" [ ] ");
                    else {
                        String arrSig = object.toString();
                        if ((arrSig.length() < 2) || (arrSig.charAt(0) != '[')) {
                            System.out.println("Surprising non-array " + arrSig + " - Result =" + result);
                            return result;
                        }
                        switch (arrSig.charAt(1)) {
                        case 'L':   result = result.concat(" [" + ((Object[])object).length + "] "); break;
                        case 'Z':   result = result.concat(" [" + ((boolean[])object).length + "] "); break;
                        case 'B':   result = result.concat(" [" + ((byte[])object).length + "] "); break;
                        case 'C':   result = result.concat(" [" + ((char[])object).length + "] "); break;
                        case 'S':   result = result.concat(" [" + ((short[])object).length + "] "); break;
                        case 'I':   result = result.concat(" [" + ((int[])object).length + "] "); break;
                        case 'J':   result = result.concat(" [" + ((long[])object).length + "] "); break;
                        case 'F':   result = result.concat(" [" + ((float[])object).length + "] "); break;
                        case 'D':   result = result.concat(" [" + ((double[])object).length + "] "); break;
                        }
                    }
            }
            //  System.out.println("Humanizesignature (array) result = " + result);
            return result;
        }

        /* Not an array */

        result = primitiveDatatypeName(s); // See if this is a primitive data type
        if (result == null) {   // No, it wasn't.
            if ((s.charAt(0) == 'L') && (s.charAt(s.length() -1) == ';'))
                s = s.substring(1,s.length() - 1); // Skip the leading L and trailing semicolon
            result = s.replace('/','.'); // If it uses slash notation, fix it.
        }
        // System.out.println("Humanizesignature result = " + result);
        return result;
    }

    public static String textAfterLastDot(String sig) {
        if (sig == null) return null;
        int lastDot = sig.lastIndexOf('.');
        if ((lastDot > -1) && (sig.length() > lastDot)) sig = sig.substring(lastDot + 1);
        return sig;
    }

    /**

     * Start the Inspector user interface.
     * This could be based on IFC (for now, that's the only one we have)
     * but potentially we could write other user interfaces.
     * We therefore allow the user to specify the class name on the command line.

     */

    public static void checkForAndStartInspector(Object iEnv){

        // This cast allows InspectorDummies to compile without any imports.

        EEnvironment env = (EEnvironment)iEnv;
        checkForAndStartInspector(env.getProperty("Inspector"),
                                  env.getProperty("InspectorClass"));
    }

    public static void checkForAndStartInspector(String inspectorLevel,
                                                 String inspectorClassName) {
        try {
            if (inspectorLevel == null) return; // We don't want any inspector.
            if (inspectorClassName == null) // We didn't specify a UI -
                inspectorClassName = "ec.ui.IFCInspectorUI"; // use the default.

            Class inspectorClass = ClassCache.forName(inspectorClassName);
            Class[] envClassArray = new Class[1];
            envClassArray[0] = inspectorClassName.getClass();
            Method method = inspectorClass.getMethod("start", envClassArray);
            if (method != null)  {
                Object[] envArray = new Object[1];
                envArray[0] = inspectorLevel;
                method.invoke(null, envArray);
            }
        } catch (Exception e) {
        }
    }
}


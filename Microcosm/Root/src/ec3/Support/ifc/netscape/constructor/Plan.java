package netscape.constructor;

import netscape.application.*;
import netscape.util.*;

/** The Plan object represents the contents of a .plan file created with
  * Netscape's Constructor. When the .plan file is saved in Constructor, the
  * actual objects and all of their state is recorded. The Plan object allows
  * you to reload the information stored in the .plan file and recreate the
  * objects contained therein exactly as they were when they were saved.
  * <BR><BR>
  * One of the key features of the Plan object involves the use of the
  * TargetProxy object within a .plan file. Many times you will want to
  * connect events from objects within the .plan file to objects outside
  * of the .plan file. One example of this would be the Application object.
  * Normally, you want to get the application to perform some command by
  * pressing a button, but the application object is not created in the
  * .plan file, so you cannot directly connect the button's onClick event to
  * the Application object. The TargetProxy object allows you to place a
  * named stand-in object to represent the Application object and connect the
  * button's onClick event to this proxy. The Plan file allows you to replace
  * this TargetProxy with the real object at load time. This allows you to
  * wire object events to objects outside the .plan file.
  * <BR><BR>
  * There are several ways to get the views out of the Plan object. There are
  * methods
  * to put all the root components into a new View, InternalWindow, or
  * ExternalWindow. You can also get to any named component by using the
  * <B>componentNamed()</B> method. A vector containing all the root components
  * and the Hashtable containing the name to component mappings are also available.
  * <BR><BR>
  * The standard constructors take the .plan file name and some object(s) to
  * replace the TargetProxies within the .plan file. They call one of the
  * <B>unarchiveObjects()</B> methods to create the actual objects. After the constructor
  * successfully returns, you can get to the view objects and place them in the
  * proper view hierarchy. After you have retrived all of the view objects properly,
  * you can throw away the Plan object. If you want to create multiple copies of the
  * objects within the .plan file, you may call one of <B>unarchiveObjects()</B> methods
  * to create these copies. All references to the previous views will be cleared.
  * If you wish to keep the Plan object around, but remove any references to the
  * real objects from the Plan object, call <B>releaseObjects()</B>. This will keep the
  * reference to the archiveData, but remove the references to the actual objects.
  * Here is a summary of what objects are stored where:<BR>
  * <UL>
  * <LI> <B>components</B> - all components dragged from the Constructor trays
  * irrespective of their position in the view heirarchy. </LI>
  * <LI> <B>rootComponents</B> - components dragged from the Constructor trays and
  * dropped on the document area. These views will have a null superclass.
  * Additionally, all non-View derived classes will be found here. </LI>
  * <LI> <B>objectToBounds</B> - All non-View derivied classes will be found here,
  * mapped to a Rect describing their location in the .plan file. </LI>
  * <LI> <B>nameToComponent</B> - Any component with a "Constructor Name" set will
  * have an entry here. </LI>
  * </UL> <BR><BR>
  * <i><b>Note:</b>  This documentation has been limited to the mimimum information
  *               necessary to start using Plan objects. The Plan object itself
  *               has a much richer API set than is documented here. Please see
  *               the source file Plan.java for full comments on the capabilities
  *               of the Plan object.</i>
  * @see TargetProxy
  *
  * @note 1.0b1 made ASCII_TYPE and BINARY_TYPE publicly documented
  * @note 1.0b1 changed allComponents() to components()
  * @note 1.0b1 changed addContentsTo() to addContentsToView()
  * @note 1.0b1 changed componentForName() to componentNamed()
  * @note 1.0b1 changed streamFromURL() to try url first, then codeBase+url, then file
  * @note 1.0b1 TargetProxies replaced with View objects will not be added to
  *             the View heirarchy during calls to addContentsToView()
  * @note 1.0b1 removed Component interface implementation. It was empty and unnecessary.
  * @note 1.0b1 all streams now using buffered versions
  * @note 1.0b2 If you pass a url in without an extension, we'll try it like that first,
  *             if that fails we'll try the url with the binary or the ascii file in an
  *             attempt to resolve the proper filename.
  * @note 1.0b2 Warning messages on viewWithContents removed. Information confused people.
  * @note 1.0b2 TargetProxies that are replaced with Views during loading are ignored
  *             when doing methods that operate on Views.
  * @note 1.0b2 added archiveObjectsToArchiveData() method
  * @note 1.0b2 added constructorComponentWasView() method
  */
public class Plan extends Object implements ExtendedTarget,
                                            Codable, Cloneable {
    Archive             archiveData;

    TargetProxyManager  targetProxyManager;

    Hashtable           nameToComponent;
    Hashtable           objectToBounds;
    Vector              rootComponents;
    Vector              components;
    int                 versionNumber;
    Size                documentSize;

    String              planURL;
    int                 archiveFormat;

    boolean             validArchive;
    boolean             validObjects;

    Color               backgroundColor;

    /** Plan file is saved in an unknown format.
      * @private
      */
    public static final int UNKNOWN_TYPE = 0;
    /** Plan file is saved in ASCII format and has a
      * filename extention of <b>ASCII_FILE_EXTENSION</b>
      */
    public static final int ASCII_TYPE = 1;
    /** Plan file is saved in binary format and has a
      * filename extention of <b>BINARY_FILE_EXTENSION</b>
      */
    public static final int BINARY_TYPE = 2;

    /** File name extension for ASCII files. */
    public static final String ASCII_FILE_EXTENSION = "plana";
    /** File name extension for Binary files. */
    public static final String BINARY_FILE_EXTENSION = "planb";

    /** Key in the document hashtable for the nameToComponent Hashtable
      * @private
      */
    public static final String NAME_TO_COMPONENT_KEY = "nameToComponent";
    /** Key in the document hashtable for the objectToBounds Hashtable
      * @private
      */
    public static final String OBJECT_TO_BOUNDS_KEY = "objectToBounds";
    /** Key in the document hashtable for the rootComponents Vector
      * @private
      */
    public static final String ROOT_COMPONENTS_KEY = "rootComponents";
    /** Key in the document hashtable for the components Vector
      * @private
      */
    public static final String ALL_COMPONENTS_KEY = "allComponents";
    /** Key in the document hashtable for the versionNumber Integer object
      * @private
      */
    public static final String VERSION_NUMBER_KEY = "versionNumber";
    /** Key in the document hashtable for the documentSize Size object
      * @private
      */
    public static final String DOCUMENT_SIZE_KEY = "documentSize";
    /** Key in the document hashtable for the background Color object
      * @private
      */
    public static final String BACKGROUND_COLOR_KEY = "backgroundColor";

    /** The current document version number.
      * @private
      */
    public static final int CURRENT_VERSION_NUMBER = 2;

///////////////////////////////////////////////////////////////////////////////
//  Constructors Methods
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  Static Constructors

    /** Convience method for the Plan constructor of the same format.
      * Returns <b>null</b> if the constructor throws.
      */
    public static Plan createPlan(String url)   {
        Plan plan;
        try {
            plan = new Plan(url);
        } catch (java.io.IOException e) {
            plan = null;
        }
        return plan;
    }

    /** Convience method for the Plan constructor of the same format.
      * Returns <b>null</b> if the constructor throws.
      */
    public static Plan createPlan(String url, Hashtable targets)    {
        Plan plan;
        try {
            plan = new Plan(url, targets);
        } catch (java.io.IOException e) {
            plan = null;
        }
        return plan;
    }

    /** Convience method for the Plan constructor of the same format.
      * Returns <b>null</b> if the constructor throws.
      */
    public static Plan createPlan(String url, Target target)  {
        Plan plan;
        try {
            plan = new Plan(url, target);
        } catch (java.io.IOException e) {
            plan = null;
        }
        return plan;
    }

    /** Convience method for the Plan constructor of the same format.
      * Returns <b>null</b> if the constructor throws.
      */
    public static Plan createPlan(java.io.InputStream stream, int formatOfStream)   {
        Plan plan;
        try {
            plan = new Plan(stream, formatOfStream);
        } catch (java.io.IOException e) {
            plan = null;
        }
        return plan;
    }

///////////////////////////////////////////////////////////////////////////////
//  Normal Constructors

    /** Constructs an empty plan. This is necessary for support of the
      * Codable interface. All objects are set to null, booleans to false,
      * <B>versionNumber</B> to CURRENT_VERSION_NUMBER and <B>archiveFormat</B>
      * to UNKNOWN_TYPE.
      * @private
      */
    public Plan() {
        archiveData = null;
        targetProxyManager = null;
        nameToComponent = null;
        objectToBounds = null;
        rootComponents = null;
        components = null;
        documentSize = null;
        validArchive = false;
        validObjects = false;
        planURL = null;
        versionNumber = CURRENT_VERSION_NUMBER;
        archiveFormat = UNKNOWN_TYPE;
        backgroundColor = Color.lightGray;
    }

    /** Loads the archive data from the <b>url</b>, and calls unarchiveObjects().
      * The <b>url</b> should be relative to the application's codeBase(). The
      * codeBase() will be appended to the <b>url</b> before the <b>url</b> is opened.
      */
    public Plan(String url) throws java.io.IOException {
        java.io.InputStream stream;

        validArchive = false;
        validObjects = false;
        if(url == null || url.length() < 1)
            return;
        stream = streamFromURL(url);
        initFrom(stream, archiveFormat);
        planURL = new String(url);
        if(validArchive)    {
            unarchiveObjects();
        }
    }

    /** Loads the archive data from the <b>url</b>, and calls unarchiveObjects(targets).
      * The <b>url</b> should be relative to the application's codeBase(). The
      * codeBase() will be appended to the <b>url</b> before the <b>url</b> is opened.
      */
    public Plan(String url, Hashtable targets) throws java.io.IOException {
        java.io.InputStream stream;

        validArchive = false;
        validObjects = false;
        if(url == null || url.length() < 1)
            return;
        stream = streamFromURL(url);
        initFrom(stream, archiveFormat);
        planURL = new String(url);
        if(validArchive)    {
            unarchiveObjects(targets);
        }
    }

    /** Loads the archive data from the <b>url</b>, and calls unarchiveObjects(target).
      * The <b>url</b> should be relative to the application's codeBase(). The
      * codeBase() will be appended to the <b>url</b> before the <b>url</b> is opened.
      */
    public Plan(String url, Target target) throws java.io.IOException {
        java.io.InputStream stream;

        validArchive = false;
        validObjects = false;
        if(url == null || url.length() < 1)
            return;
        stream = streamFromURL(url);
        initFrom(stream, archiveFormat);
        planURL = new String(url);
        if(validArchive)    {
            unarchiveObjects(target);
        }
    }

    /** Loads the archive data from the <b>stream</b>. The <b>formatOfStream</b>
      * must be either <b>ASCII_TYPE</b> or <b>BINARY_TYPE</b> indicating the
      * type of data on the stream. This constructor only
      * creates the archive data. To get the actual objects contained within
      * this Plan, you must call one of the <b>unarchiveObjects()</b> methods.
      */
    public Plan(java.io.InputStream stream, int formatOfStream)
            throws java.io.IOException  {
        initFrom(stream, formatOfStream);
    }

    /**Primative init method for the Plan object
      * @private
      */
    protected void initFrom(java.io.InputStream stream, int formatOfStream)
            throws java.io.IOException  {
        archiveFormat = formatOfStream;
        validArchive = false;
        validObjects = false;
        planURL = null;
        versionNumber = CURRENT_VERSION_NUMBER;
        if(stream == null)
            return;
        archiveData = archiveFromStream(stream, formatOfStream);
        if(archiveData != null)
            validArchive = true;
    }

    /** This is a convience method to release the references we have to the
      * objects within the Plan file. Everything except the <B>archiveData</B> will be
      * set to null. If you want to create another set of objects, call one of the
      * <B>unarchiveObjects()</B> methods with the appropriate TargetProxy replacements.
      */
    public void releaseObjects()    {
//      archiveData = null;
        targetProxyManager = null;
        nameToComponent = null;
        objectToBounds = null;
        rootComponents = null;
        components = null;
        documentSize = null;
//        validArchive = false;
        validObjects = false;
//        planURL = null;
//        versionNumber = CURRENT_VERSION_NUMBER;
//        archiveFormat = UNKNOWN_TYPE;
//        backgroundColor = Color.lightGray();
    }

///////////////////////////////////////////////////////////////////////////////
//  unarchive Methods
//     Normally called after the constructors to unarchive the real objects
///////////////////////////////////////////////////////////////////////////////
    /** Creates the objects stored in the archive data that was loaded from one
      * of the constructors. TargetProxies in the archive with names matching
      * those in the <b>targetProxies</b> will be replaced with the object from
      * the <b>targetProxies</b> Hashtable. Any objects in <b>targetProxies</b>
      * that do not implement the Target interface will be ignored.
      * <B>validObjects</B> will be set to true if the objects were unarchived
      * correctly.
      */
    public boolean unarchiveObjects(Hashtable targets)    {
        validObjects = false;
        if(!validArchive)
            return validObjects;
        validObjects = unarchiveFrom(archiveData, targets);
        if(validObjects)
            finishUnarchiving();
        return validObjects;
    }

    /** Convience method for <B>unarchiveObjects(Hashtable)</B>.
      * The <B>target</B> object will replace all TargetProxy
      * objects contained in the archive data. This is convient when
      * there is only a single TargetProxy in the plan archive.
      */
    public boolean unarchiveObjects(Target target) {
        Hashtable targets;

        targets = new Hashtable(1);
        targets.put(TargetProxyManager.SINGLE_TARGET_PROXY_KEY, target);

        return unarchiveObjects(targets);
    }

    /** Convience method for <B>unarchiveObjects(Hashtable)</B>.
      * A null Hashtable is passed in, resulting in none of the
      * TargetProxies being replaced. Useful if you have no
      * targetProxy objects in the plan archive, or you are
      * going to me manipulating the archived objects and want
      * to retain the TargetProxy objects as is.
      */
    public boolean unarchiveObjects()    {
        return unarchiveObjects((Hashtable)null);
    }

    /** Called at the end of the unarchiveObjects() methods to allow any
      * post processing of the new objects. <BR>
      * Current implementation makes sure that LiveConnected Script objects
      * keep a handle on the nameToComponent hashtable.
      * @private
      */
    protected void finishUnarchiving()  {
        int i;
        Script nextScript;

        i = rootComponents().count();
        while(--i >= 0) {
            if(rootComponents().elementAt(i) instanceof Script) {
                nextScript = (Script)rootComponents().elementAt(i);
                if(nextScript.isUsingLiveConnect())
                    nextScript.setNamedObjects(nameToComponent());
            }

        }
    }

///////////////////////////////////////////////////////////////////////////////
//  Manipulation Methods
///////////////////////////////////////////////////////////////////////////////
    /** The TargetProxyManager manages the replacement of the TargetProxies at
      * unarchive time. You will not normally need to deal with this object.
      * @private
      */
    public TargetProxyManager targetProxyManager()  {
        if(targetProxyManager == null)   {
            targetProxyManager = new TargetProxyManager();
        }
        return targetProxyManager;
    }

    /** The TargetProxyManager manages the replacement of the TargetProxies at
      * unarchive time. You will not normally need to deal with this object.
      * @private
      */
    public void setTargetProxyManager(TargetProxyManager manager)   {
        targetProxyManager = manager;
    }

    /** Hashtable of any name to component object mappings. This table may
      * be empty if none of the components have been named. These names
      * correspond to the "Constructor Name" attribute in Constructor.
      */
    public Hashtable nameToComponent()  {
        if(nameToComponent == null) {
            nameToComponent = new Hashtable();
        }
        return nameToComponent;
    }

    /**
      * @private
      */
    public void setNameToComponent(Hashtable table) {
        nameToComponent = (Hashtable)table.clone();
    }

    /** Convience method for getting a component named <b>name</b>
      * Equivalent to:<BR>
      * <PRE> return nameToComponent().get(name); </PRE>
      */
    public Object componentNamed(String name) {
        return nameToComponent().get(name);
    }

    /** Objects that are not derived from View generally do not store
      * their own bounds Rect. So that these objects can maintain
      * their visual position in the plan file, their 'bounds'
      * are stored in this Hashtable. This Hashtable will normally have
      * all the non-View derived objects in it. Normally all non-View
      * objects are also stored in the <b>rootComponents</b>
      * vector (because they are not views and therefore cannot
      * participate in the view hierarchy)
      * and additionally in the <b>components</b> vector.
      */
    public Hashtable objectToBounds()   {
        if(objectToBounds == null)  {
            objectToBounds = new Hashtable();
        }
        return objectToBounds;
    }

    /**
      * @private
      */
    public void setObjectToBounds(Hashtable table)  {
        objectToBounds = table;
    }

    /** This is a list of the components that are at the top of the
      * view heirarchy within the plan file. All views are reachable
      * by traversing these objects. All non-View derived objects will
      * be in this vector as well as any View based objects dropped
      * on the background area in Constructor. Any object in
      * <b>rootComponents</b> should also be in the <b>components</b>
      * vector.
      */
    public Vector rootComponents()  {
        if(rootComponents == null)  {
            rootComponents = new Vector();
        }
        return rootComponents;
    }

    /**
      * @private
      */
    public void setRootComponents(Vector vector)    {
        rootComponents = vector;
    }

    /** This is a list of all views that Constructor considers to be
      * Components. Generally, any view that was dropped from a Tray in
      * Constructor is on this list. This list is used by Constructor to
      * determine which views are components and which views are simply
      * subviews of other components. <BR><BR>
      * A simple example would be the ContainerView object. It normally
      * has a single subview that displays it's title string. This subview
      * is not in this list of components, but if you where to the
      * traverse the view hierarchy, you would find it. Therefore
      * Constructor ignores the title subview when determining what view
      * you clicked on and want to edit. This Vector is normally not
      * interesting outside of Constructor.
      */
    public Vector components()   {
        if (components == null)   {
            components = new Vector();
        }
        return components;
    }

    /**
      * @private
      */
    public void setComponents(Vector vector)    {
        components = vector;
    }

    /** The version of the document.
      * @private
      */
    public int versionNumber()  {
        return versionNumber;
    }

    /**
      * @private
      */
    public void setVersionNumber(int version)   {
        versionNumber = version;
    }

    /** The size of the document. This is the size of the
      * document area that was set in Constructor.
      */
    public Size size()  {
        if(documentSize == null)
            documentSize = new Size(0, 0);
        return documentSize;
    }

    /**
      * @private
      */
    public void setSize(Size size)  {
        documentSize = size;
    }

    /**
      * @private
      */
    public void setSize(int width, int height)  {
        documentSize = new Size(width, height);
    }

    /** The url used to load this plan file. */
    public String url() {
        return planURL;
    }

    /**
      * @private
      */
    public void setURL(String url)  {
        planURL = url;
    }

    /** The format of the archive data. Valid values are
      * <b>ACSII_TYPE</b> and <b>BINARY_TYPE</b>.
      * @private
      */
    public int archiveFormat()  {
        return archiveFormat;
    }

    /**
      * @private
      */
    public void setArchiveFormat(int format)    {
        archiveFormat = format;
    }

    /** @private
      */
    public void setValidArchive(boolean value)   {
        validArchive = value;
    }

    /** Indicates if the archive was sucessfully read in.
      * If this is false, there was a problem reading the archive data.
      * @private
      */
    public boolean isValidArchive()   {
        return validArchive;
    }


    /** @private
      */
    public void setValidObjects(boolean value)   {
        validObjects = value;
    }
    /** Indicates if the archive was sucessfully unarchived back
      * into the original objects.
      * @private
      */
    public boolean hasValidObjects()   {
        return validObjects;
    }

    /** The archive that was created by one the constructors.
      * @private
      */
    public Archive archiveData()    {
        return archiveData;
    }

    /** The archive that was created by one the constructors.
      * @private
      */
    public void setArchiveData(Archive archive)    {
        archiveData = archive;
    }

    /** Background color used for the document area. Normally used as the
      * background color on an ExternalWindow.
      */
    public Color backgroundColor()  {
        return backgroundColor;
    }

    /**
      * @private
      */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }


///////////////////////////////////////////////////////////////////////////////
//  Save Methods
///////////////////////////////////////////////////////////////////////////////
    /** Saves the plan file out to <b>url()</b> in <b>archiveFormat</b>.
      * If either of these values is invalid, the save will fail.
      * Returns <b>true</b> if the save was successful.
      * @private
      */
    public boolean save()   {
        java.io.OutputStream    stream;
        boolean                 result = false;

        if (planURL == null
            || planURL.equals("")
            || (archiveFormat != ASCII_TYPE
                && archiveFormat != BINARY_TYPE))    {
            return result;
        }

        try {
            stream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(planURL));
            result = saveToStream(stream, archiveFormat);
            stream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            return result;
        }
        return result;
    }

    /** Saves the plan file out to <b>stream()</b> in <b>format</b>.
      * If either of these values is invalid, the save will fail.
      * Returns <b>true</b> if the save was successful.
      * @private
      */
    public boolean saveToStream(java.io.OutputStream stream, int format)    {
        Archive                 archive;

        if(stream == null)
            return false;

        if (format != ASCII_TYPE
                && format != BINARY_TYPE)    {
            return false;
        }

        archive = new Archive();
        try {
            archiveTo(archive);
            if(format == ASCII_TYPE)
                archive.writeASCII(stream, true);
            else
                archive.write(stream);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            return false;
        } catch (CodingException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

///////////////////////////////////////////////////////////////////////////////
//  Archive Reading Methods
///////////////////////////////////////////////////////////////////////////////
    /** Returns <B>ASCII_TYPE</B>, <B>BINARY_TYPE</B> or
      * <B>UNKNOWN_TYPE</B> based on the extension of <b>url</b>.
      * @private
      */
    protected int archiveFormatOf(String url)    {

        if(url == null || url.length() < 1)
            return UNKNOWN_TYPE;

        if(url.endsWith(BINARY_FILE_EXTENSION))
            return BINARY_TYPE;
        else if(url.endsWith(ASCII_FILE_EXTENSION))
            return ASCII_TYPE;
        return UNKNOWN_TYPE;
    }

    /** Returns an InputStream from <b>url</b>.
      * If <b>url</b> does not end with a valid plan extension, we will try
      * it plain first, then append the binary extension, and if that
      * fails, we'll try the ascii extension.
      * We try to create a new java.net.URL from <b>url</b>. If this fails,
      * we will prepend the codeBase() and try again, if that fails
      * we'll try a FileInputStream on <b>url</b>.
      * A call to this method will also call setArchiveFormat() properly.
      * @private
      */
    protected java.io.InputStream streamFromURL(String url)
            throws java.io.IOException   {
        Archive                 archive;
        java.io.InputStream     stream = null;
        int                     format;
        java.net.URL            newURL;
        boolean                 stillTrying = false,
                                triedASCII = false,
                                triedBinary = false,
                                wasUnknown = false;
        String                  origUrl = url;

        if(url == null || url.length() == 0)
            return null;

        format = archiveFormatOf(url);
        if(format == ASCII_TYPE)    {
            triedASCII = true;
        } else if(format == BINARY_TYPE)    {
            triedBinary = true;
        } else {
            wasUnknown = true;
        }

        do  {
        stillTrying = false;
        try {
        newURL = new java.net.URL(url);
        stream = new java.io.BufferedInputStream(newURL.openStream());
        } catch (java.io.IOException e1) {
            try {
            newURL = new java.net.URL(Application.application().codeBase(), url);
            stream = new java.io.BufferedInputStream(newURL.openStream());
            } catch (java.io.IOException e2) {
                try {
                newURL = null;
                stream = new java.io.BufferedInputStream(
                                        new java.io.FileInputStream(url));
                } catch (java.io.IOException e3)    {
                    if(!triedBinary && wasUnknown)  {
                        url = origUrl + "." + BINARY_FILE_EXTENSION;
                        triedBinary = true;
                        stillTrying = true;
                        format = BINARY_TYPE;
                    } else if(!triedASCII && wasUnknown)    {
                        url = origUrl + "." + ASCII_FILE_EXTENSION;
                        triedASCII = true;
                        stillTrying = true;
                        format = ASCII_TYPE;
                    } else  {
                        throw e3;
                    }
                }
            }
        }
        } while(stillTrying);

        if(stream != null)
            setArchiveFormat(format);
        return stream;
    }

    /** Creates an archive from <B>stream</B>.
      * Will return null in error cases outside of IOException.
      * @private
      */
    protected Archive archiveFromStream(java.io.InputStream stream,
                                        int format)
            throws java.io.IOException {
        Archive   archive;

        if(stream == null
            || (format != ASCII_TYPE
                && format != BINARY_TYPE))
            return null;

        archive = new Archive();
        try {
            if(format == ASCII_TYPE)
                archive.readASCII(stream);
            else if(format == BINARY_TYPE)
                archive.read(stream);
            else
                return null;
        } catch (java.io.IOException e) {
            throw(e);
        } catch (CodingException e) {
            e.printStackTrace(System.err);
            return null;
        } catch (DeserializationException e)    {
            e.printStackTrace(System.err);
            return null;
        }
        return archive;
    }

///////////////////////////////////////////////////////////////////////////////
//  Unarchive Methods
///////////////////////////////////////////////////////////////////////////////
    /** Primative method that converts the archive data into objects.
      * All of the unarchiveObjects() result in a call to this method.
      * There are two root objects in the archive. The first is the
      * document Hashtable that represents the objects in the plan file.
      * the _KEY strings defined by this object are the keys to this
      * Hashtable. The second is the TargetProxyManager, who manages the
      * replacement of the TargetProxy objects within the archive.
      * @private
      */
    protected boolean unarchiveFrom(Archive archive, Hashtable proxies) {
        Unarchiver          unarchiver;
        int                 rootIds[];
        Hashtable           documentValues;

        if(archive == null)
            return false;

        try {
            rootIds = archive.rootIdentifiers();
            unarchiver = new Unarchiver(archive);
            if(rootIds.length > 1)  {
                targetProxyManager = (TargetProxyManager)unarchiver.unarchiveIdentifier(rootIds[1]);
                targetProxyManager.setTargets(proxies);
            }
            documentValues = (Hashtable)unarchiver.unarchiveIdentifier(rootIds[0]);
        } catch (CodingException e) {
            e.printStackTrace(System.err);
            return false;
        }

        decodeDocumentInformation(documentValues);
        return true;
    }

    /** Primative method that retrives the document information
      * from the <b>info</b> Hashtable and sets our variables
      * properly. This is called from the <b>unarchiveFrom()</b> method
      * after all the objects have been unarchived.
      * @private
      */
    protected void decodeDocumentInformation(Hashtable info) {
        Integer ver;

        ver = (Integer)info.get(VERSION_NUMBER_KEY);
        if(ver != null)
            versionNumber = ver.intValue();
        else
            versionNumber = 0;

        if(versionNumber != CURRENT_VERSION_NUMBER) {
            decodeBETADocumentInformation(info);
            return;
        }

        nameToComponent = (Hashtable)info.get(NAME_TO_COMPONENT_KEY);
        rootComponents = (Vector)info.get(ROOT_COMPONENTS_KEY);
        components = (Vector)info.get(ALL_COMPONENTS_KEY);

        documentSize = (Size)info.get(DOCUMENT_SIZE_KEY);
        objectToBounds = (Hashtable)info.get(OBJECT_TO_BOUNDS_KEY);
        backgroundColor = (Color)info.get(BACKGROUND_COLOR_KEY);
    }

    /** Same as the <B>decodeDocumentInformation</B> method, but supports
      * older file formats.
      * @private
      */
    protected void decodeBETADocumentInformation(Hashtable info) {
        String  COMPONENTS_KEY = "components";
        String  NAMED_COMPONENTS_KEY = "namedComponents";
        String  SIZE_KEY = "documentSize";
//      String  SCOPE_JAVASCRIPT_KEY = "scopeJavaScript";   // Looking for better support for JavaScript
        String  NON_VIEW_BOUNDS_KEY = "nonViewBounds";
        String  DOCUMENT_VERSION_KEY = "documentVersion";
        String  builderComponentsKey = "builderComponents"; // THIS WAS NOT PUBLIC IN VER 1 FILE

        Enumeration enum;
        Object      component;
        Hashtable   componentToName;
        Vector      testResult;
        Integer     ver;

        testResult = (Vector)info.get(COMPONENTS_KEY);

        if(testResult == null)  {
            System.err.println("Unsupported Plan file format. Could not find "
                                + COMPONENTS_KEY
                                + " in Hashtable: " + info);
            return;
        }

        ver = (Integer)info.get(DOCUMENT_VERSION_KEY);
        if(ver != null)
            versionNumber = ver.intValue();
        else
            versionNumber = 0;

        System.err.println("Upgrading plan file " + url()
                            + " from version: " + versionNumber
                            + " to: " + CURRENT_VERSION_NUMBER + ".");
        // We are converting here, so rev the versionNumber
        versionNumber = CURRENT_VERSION_NUMBER;

        rootComponents = (Vector)info.get(COMPONENTS_KEY);
        components = (Vector)info.get(builderComponentsKey);

        documentSize = (Size)info.get(SIZE_KEY);
        objectToBounds = (Hashtable)info.get(NON_VIEW_BOUNDS_KEY);
        backgroundColor = Color.lightGray;  // We didn't have a color then

        /// The name table stored in version 1 files was inverted
        // for historical reasons. Flip it to the current orientation.
        componentToName = (Hashtable)info.get(NAMED_COMPONENTS_KEY);
        if(componentToName != null) {
            nameToComponent().clear();
            enum = componentToName.keys();
            while(enum.hasMoreElements())   {
                component = enum.nextElement();
                nameToComponent().put((String)componentToName.get(component), component);
            }
        }
    }


///////////////////////////////////////////////////////////////////////////////
//  Archive Writing Methods
///////////////////////////////////////////////////////////////////////////////
    /** This method will push the component values into archiveData, the
      * previous archiveData (if any) will be lost.
      * @private
      */
    public void archiveObjectsToArchiveData()   {
        archiveData = new Archive();
        archiveTo(archiveData);
    }

    /** Stores the currect document information into <b>archive</b>.
      * Encodes both the TargetProxyManager and the document hashtable.
      * @private
      */
    protected void archiveTo(Archive archive) {
        Archiver        archiver;
        Hashtable       documentValues;

        archiver = new Archiver(archive);

        documentValues = new Hashtable();
        encodeDocumentInformation(documentValues);
        try {
            archiver.archiveRootObject(documentValues);
            archiver.archiveRootObject(targetProxyManager());
        } catch (CodingException e) {
            e.printStackTrace();
        }
    }

    /** Stores the document specific information into <B>hashtable</B>.
      * Called by <B>archiveTo()</B> to record the proper information;
      * @private
      */
    protected void encodeDocumentInformation(Hashtable hashtable)  {

        if(nameToComponent != null)
            hashtable.put(NAME_TO_COMPONENT_KEY, nameToComponent);
        else
            hashtable.put(NAME_TO_COMPONENT_KEY, new Hashtable());

        if(objectToBounds != null)
            hashtable.put(OBJECT_TO_BOUNDS_KEY, objectToBounds);
        else
            hashtable.put(OBJECT_TO_BOUNDS_KEY, new Hashtable());

        if(rootComponents != null)
            hashtable.put(ROOT_COMPONENTS_KEY, rootComponents);
        else
            hashtable.put(ROOT_COMPONENTS_KEY, new Vector(0));

        if (components != null)
            hashtable.put(ALL_COMPONENTS_KEY, components);
        else
            hashtable.put(ALL_COMPONENTS_KEY, new Vector(0));

        hashtable.put(VERSION_NUMBER_KEY, new Integer(versionNumber));

        if(documentSize != null)
            hashtable.put(DOCUMENT_SIZE_KEY, documentSize);
        else
            hashtable.put(DOCUMENT_SIZE_KEY, new Size(0, 0));

        if(backgroundColor != null)
            hashtable.put(BACKGROUND_COLOR_KEY, backgroundColor);
        else
            hashtable.put(BACKGROUND_COLOR_KEY, Color.lightGray);

    }

///////////////////////////////////////////////////////////////////////////////
//  ExtendedTarget Methods
///////////////////////////////////////////////////////////////////////////////
    /** @private */
    public void performCommand(String command, Object object)   {
    }

    /** @private */
    public boolean canPerformCommand(String command)    {
        return false;
    }

///////////////////////////////////////////////////////////////////////////////
//  Codable Methods
///////////////////////////////////////////////////////////////////////////////
    /** Descibes the Plan object to the archiving system.
      * @see Codable#describeClassInfo
      * @private
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.constructor.Plan", CURRENT_VERSION_NUMBER);
        info.addField(NAME_TO_COMPONENT_KEY, OBJECT_TYPE);
        info.addField(OBJECT_TO_BOUNDS_KEY, OBJECT_TYPE);
        info.addField(ROOT_COMPONENTS_KEY, OBJECT_TYPE);
        info.addField(ALL_COMPONENTS_KEY, OBJECT_TYPE);
        info.addField(VERSION_NUMBER_KEY, OBJECT_TYPE);
        info.addField(DOCUMENT_SIZE_KEY, OBJECT_TYPE);
        info.addField(BACKGROUND_COLOR_KEY, OBJECT_TYPE);
    }

    /** Encodes the Plan information into the <B>encoder</B>.
      * @see Codable#encode
      * @private
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(NAME_TO_COMPONENT_KEY, nameToComponent);
        encoder.encodeObject(OBJECT_TO_BOUNDS_KEY, objectToBounds);
        encoder.encodeObject(ROOT_COMPONENTS_KEY, rootComponents);
        encoder.encodeObject(ALL_COMPONENTS_KEY, components);
        encoder.encodeObject(VERSION_NUMBER_KEY, new Integer(versionNumber));
        encoder.encodeObject(DOCUMENT_SIZE_KEY, documentSize);
        encoder.encodeObject(BACKGROUND_COLOR_KEY, backgroundColor);
    }

    /** Retrieves the information from the <B>decoder</B> and resets the object state.
      * @see Codable#decode
      * @private
      */
    public void decode(Decoder decoder) throws CodingException {
        Integer value;
        nameToComponent = (Hashtable)decoder.decodeObject(NAME_TO_COMPONENT_KEY);
        objectToBounds = (Hashtable)decoder.decodeObject(OBJECT_TO_BOUNDS_KEY);
        rootComponents = (Vector)decoder.decodeObject(ROOT_COMPONENTS_KEY);
        components = (Vector)decoder.decodeObject(ALL_COMPONENTS_KEY);
        value = (Integer)decoder.decodeObject(VERSION_NUMBER_KEY);
        if(value != null)
            versionNumber = value.intValue();
        else
            versionNumber = 0;
        documentSize = (Size)decoder.decodeObject(DOCUMENT_SIZE_KEY);
        backgroundColor = (Color)decoder.decodeObject(BACKGROUND_COLOR_KEY);
    }

    /** This method currently does nothing. Supports the Codable interface.
      * @see Codable#finishDecoding
      * @private
      */
    public void finishDecoding() throws CodingException {
    }

///////////////////////////////////////////////////////////////////////////////
//  Clone Method
///////////////////////////////////////////////////////////////////////////////
    /** Clones the Plan object by calling archiveTo() and unarchiveFrom()
      * Does not go through the unarchiveObjects() methods.
      * @private
      */
    public Object clone() {
        Plan       newPlan = null;
        Archive     archive;
        Hashtable   table;

        try {
            newPlan = (Plan)super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println(e);
            e.printStackTrace();
        }

        table = new Hashtable(1);
        table.put(TargetProxyManager.NEVER_REPLACE_KEY, new Object());

        newPlan.targetProxyManager = null;
        newPlan.nameToComponent = null;
        newPlan.objectToBounds = null;
        newPlan.rootComponents = null;
        newPlan.components = null;
        newPlan.documentSize = null;
        newPlan.backgroundColor = null;

        archive = new Archive();
        archiveTo(archive);
        newPlan.unarchiveFrom(archive, table);

        return newPlan;
    }

///////////////////////////////////////////////////////////////////////////////
//  View Manipultation Method
///////////////////////////////////////////////////////////////////////////////
    /** Returns the bounding box that fits all View
      * derived components. InternalWindows are ignored in calculating this
      * Rect.
      */
    public Rect boundingRect()   {
        int i;
        Rect rect = null;
        Object nextComponent;

        i = rootComponents().count();
        while(--i >= 0) {
            nextComponent = rootComponents().elementAt(i);
            if(nextComponent instanceof View
                && constructorComponentWasView(nextComponent)
                && !(nextComponent instanceof InternalWindow))   {
                if(rect == null)    {
                    rect = new Rect(((View)nextComponent).bounds);
                } else  {
                    rect.unionWith(((View)nextComponent).bounds);
                }
            }
        }
        if(rect == null)
            return new Rect(0, 0, 0, 0);

        return rect;
    }

    /** Resets size() to be boundingBox() size and adjusts views accordingly,
      * i.e. moves the upper left most component to 0,0 and moves all other
      * components the same delta amounts.
      */
    public void sizeToFit() {
        Rect bRect;

        bRect = boundingRect();
        moveBy(-bRect.x, -bRect.y);
        setSize(new Size(bRect.width, bRect.height));
    }

    /** Sends moveBy(x, y) to all the rootComponents() that are views. */
    public void moveBy(int x, int y)    {
        int i;
        Object nextComponent;

        i = rootComponents().count();
        while(--i >= 0) {
            nextComponent = rootComponents().elementAt(i);
            if(nextComponent instanceof View
                && constructorComponentWasView(nextComponent))   {
                    ((View)nextComponent).moveBy(x, y);
            }
        }
    }

    /** Adds all the rootComponents() that are views to <b>aView</b>.
      */
    public void addContentsToView(View aView)   {
        int i, count;
        Object nextComponent;

        count = rootComponents().count();
        for(i = 0; i < count; i++) {
            nextComponent = rootComponents().elementAt(i);
            if(nextComponent instanceof InternalWindow
                && constructorComponentWasView(nextComponent)) {
                InternalWindow internalWindow = (InternalWindow)nextComponent;
                if(aView instanceof RootView)   {
                    internalWindow.setRootView((RootView)aView);
                    if(internalWindow.onscreenAtStartup())
                        internalWindow.show();
                } else  {
//                    System.err.println(url() + " could not add InternalWindow: "
//                                    + nextComponent + " to view: " + aView);
                }
            } else if(!constructorComponentWasView(nextComponent)
                        && nextComponent instanceof View)   {
//                System.err.println(url() + " component: "
//                                + nextComponent + " was not originally a subclass "
//                                + "of View, will not add to view: " + aView);
            } else if(nextComponent instanceof View)    {
                aView.addSubview((View)nextComponent);
            }
        }

    }

    /** Returns true of the object was placed in Constructor was a
      * subclass of View object. (This is complicated by the TargetProxy
      * implementation.)
      * @private
      */
    protected boolean constructorComponentWasView(Object object)    {
        return !objectToBounds.containsKey(object);
    }

    /** Creates a new View(size().width, size().height), adds all View based
      * components to it and returns the view.
      */
    public View viewWithContents() {
        View aView;

        aView = new View(0, 0, size().width, size().height);
        addContentsToView(aView);
        return aView;
    }

    /** Adds all the rootComponents() that are views to the contentView()
      * of a new InternalWindow. Uses the current document size to correctly
      * size the internal window.
      */
    public InternalWindow internalWindowWithContents() {
        InternalWindow iWindow;
        Size winSize;
        View bView;

        iWindow = new InternalWindow();
        winSize = iWindow.windowSizeForContentSize(size().width, size().height);
        iWindow.setBounds(0, 0, winSize.width, winSize.height);
        iWindow.contentView().setBackgroundColor(backgroundColor());
        addContentsToView(iWindow.contentView());

        return iWindow;
    }

    /** Adds all the rootComponents() that are views to the rootView()
      * of a new ExternalWindow. Uses the current document size to correctly
      * size the internal window.
      */
    public ExternalWindow externalWindowWithContents() {
        ExternalWindow eWindow;
        Size winSize;
        View bView;

        eWindow = new ExternalWindow();
        winSize = eWindow.windowSizeForContentSize(size().width, size().height);
        eWindow.setBounds(0, 0, winSize.width, winSize.height);
        eWindow.rootView().setColor(backgroundColor());
        addContentsToView(eWindow.rootView());

        return eWindow;
    }
}

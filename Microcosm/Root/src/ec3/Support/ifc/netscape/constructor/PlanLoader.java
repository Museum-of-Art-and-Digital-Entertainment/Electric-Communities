package netscape.constructor;

import netscape.application.*;
import netscape.util.*;

/** The PlanLoader object is a convience object for loading plan files through
  * the Target interface. You can specify the .plan file name and whether to
  * load the plan in an external or internal window. It implements the Target
  * interface so that you can wire some event to it's action. Like a button
  * click <I>Note: In test mode, the codeBase() is the constructor application.
  * Resolving the location of the requested planURL() may be challenging and
  * fail.</I>
  *
  */
public class PlanLoader extends Object implements Target, Codable    {
    String      url;
    boolean     isRelativeURL;
    boolean     asExternalWindow;
    Plan        plan;

    static boolean     rememberWindows = false;
    static Vector      windowVector;

    public static final String CREATE_PLAN = "Load Plan";
    static final String HIDE_WINDOWS = "Hide Windows";
    static final String SHOW_WINDOWS = "Show Windows";

    static final String URL_KEY = "Plan URL";
    static final String IS_RELATIVE_KEY = "URL is relative to codebase";
    static final String LOAD_INTO_EXTERNAL_WINDOW = "Use External Window";

////////////////////////////////////////////////////////////////////////////////////////////
//  Constructors Methods
////////////////////////////////////////////////////////////////////////////////////////////

    /** Constructs an empty PlanLoader. */
    public PlanLoader() {
        url = null;
        plan = null;
        isRelativeURL = false;
        asExternalWindow = true;
    }

    /** Constructs a PlanLoader. */
    public PlanLoader(String planURL) {
        this(planURL, false);
    }

    public PlanLoader(String planURL, boolean isRelative) {
        url = planURL;
        plan = null;
        isRelativeURL = isRelative;
        asExternalWindow = true;
    }

////////////////////////////////////////////////////////////////////////////////////////////
//  Attribute Methods
////////////////////////////////////////////////////////////////////////////////////////////

    public void setPlan(Plan aPlan)    {
        plan = aPlan;
    }

    public Plan plan() {
        return plan;
    }

    public void setPlanURL(String urlString)    {
        url = urlString;
    }

    public String planURL() {
        return url;
    }

    /** @private */
    public String fullURL() {
//        if(isRelativeURL())
//            return Application.application().codeBase() + planURL();
        return planURL();
    }

    /** @private */
    public void setRelativeURL(boolean value)   {
        isRelativeURL = value;
    }

    /** @private */
    public boolean isRelativeURL()  {
        return isRelativeURL;
    }

    public  void performCommand(String command, Object object)  {
        if(CREATE_PLAN.equals(command))   {
            createPlan();
        } else if(HIDE_WINDOWS.equals(command))   {
            hideWindows();
        } else if(SHOW_WINDOWS.equals(command))   {
            showWindows();
        }
    }

    /** Indicates if the Plan should be loaded in an ExternalWindow.*/
    public boolean loadInExternalWindow()   {
        return asExternalWindow;
    }

    /** Sets if the Plan should be loaded in an ExternalWindow.*/
    public void setLoadInExternalWindow(boolean value)  {
        asExternalWindow = value;
    }
////////////////////////////////////////////////////////////////////////////////////////////
//  Load Methods
////////////////////////////////////////////////////////////////////////////////////////////
    /** @private */
    public void loadPlan()   {
        int         format;
        java.io.InputStream     stream;

        if(plan() != null)  {
            plan.unarchiveObjects();
            return;
        }

        if(planURL() == null || planURL().length() < 1)
            return;

        try {
            stream = new java.io.FileInputStream(fullURL());
            plan = new Plan();
            format = plan.archiveFormatOf(fullURL());
            plan.initFrom(stream, format);
            plan.unarchiveObjects();
        } catch (java.io.IOException e1) {
            try {
                plan = new Plan(fullURL());
            } catch (java.io.IOException e2) {
                System.err.println("PlanLoader could not load plan file: \"" + fullURL() + "\"");
                return;
            }
        }
    }

    public InternalWindow putPlanInInternalWindow() {
        InternalWindow iWindow;

        loadPlan();
        if(plan() == null)
            return null;

        iWindow = plan().internalWindowWithContents();
        iWindow.setCloseable(true);

        if(rememberWindows())
            windowVector().addElement(iWindow);

        return iWindow;
    }

    public ExternalWindow putPlanInExternalWindow() {
        ExternalWindow eWindow;

        loadPlan();
        if(plan() == null)
            return null;

        eWindow = plan().externalWindowWithContents();

        if(rememberWindows())
            windowVector().addElement(eWindow);
        return eWindow;
    }

    /** puts a fresh copy of the plan file objects into a new
      * ExternalWindow/InternalWindow can calls show().
      * @private
      */
    public void createPlan()  {
        if(asExternalWindow)    {
            ExternalWindow ex = putPlanInExternalWindow();
            if(ex != null)
                ex.show();
        } else  {
            InternalWindow in = putPlanInInternalWindow();
            if(in != null)
                in.show();
        }
        // After we load the plan into a window, we do not want to keep it around
        // in case we are asked to load the plan again. So we release our reference
        // to it. In the case of Constructor, something else has a handle on the window
        // that just got created here.
        if(plan() != null)
            plan().releaseObjects();
    }

////////////////////////////////////////////////////////////////////////////////////////////
//  Archive Methods
////////////////////////////////////////////////////////////////////////////////////////////
    /**
      * @see Codable#describeClassInfo
      * @private
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.constructor.PlanLoader", 1);
        info.addField(URL_KEY, STRING_TYPE);
        info.addField(IS_RELATIVE_KEY, BOOLEAN_TYPE);
        info.addField(LOAD_INTO_EXTERNAL_WINDOW, BOOLEAN_TYPE);
    }

    /**
      * @see Codable#encode
      * @private
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeString(URL_KEY, url);
        encoder.encodeBoolean(IS_RELATIVE_KEY, isRelativeURL);
        encoder.encodeBoolean(LOAD_INTO_EXTERNAL_WINDOW, asExternalWindow);
    }

    /**
      * @see Codable#decode
      * @private
      */
    public void decode(Decoder decoder) throws CodingException {
        url = decoder.decodeString(URL_KEY);
        isRelativeURL = decoder.decodeBoolean(IS_RELATIVE_KEY);
        asExternalWindow = decoder.decodeBoolean(LOAD_INTO_EXTERNAL_WINDOW);
    }

    /**
      * @see Codable#finishDecoding
      * @private
      */
    public void finishDecoding() throws CodingException {
    }

////////////////////////////////////////////////////////////////////////////////////////////
//  Static windowVector Methods
////////////////////////////////////////////////////////////////////////////////////////////
    /** @private */
    static public void setRememberWindows(boolean value)    {
        rememberWindows = value;
    }

    /** @private */
    static public boolean rememberWindows() {
        return rememberWindows;
    }

    /** @private */
    static public Vector windowVector()    {
        if(windowVector == null)
            windowVector = new Vector();
        return windowVector;
    }

    /** @private */
    static public void hideWindows()   {
        int i;

        i = windowVector().count();
        if(i < 1)
            return;
        while(--i >= 0) {
            if(windowVector().elementAt(i) instanceof ExternalWindow)
                ((ExternalWindow)windowVector().elementAt(i)).dispose();
            else if(windowVector().elementAt(i) instanceof InternalWindow)
                ((InternalWindow)windowVector().elementAt(i)).hide();
        }
        windowVector.removeAllElements();
    }

    /** @private */
    static public void showWindows()   {
        int i;

        i = windowVector().count();
        if(i < 1)
            return;
        while(--i >= 0) {
            ((Window)windowVector().elementAt(i)).show();
        }
    }

}




package ec.e.inspect;

import ec.ifc.app.*;
import java.util.*;
import ec.util.PEHashtable;
import netscape.application.*;

public class IFCInspectorUI extends View implements WindowOwner, Target, InspectorUI, EventProcessor {

	private static Vector inspectorViews;	// All currently open InspectorViews (One per inspector window)
	private static ECApplication cApplication = null;
	private static boolean panelInitialized = false;
	private static boolean startedApplication = false;

	private int x = 40;
	private int y = 40;
	private int width = 188;
	private int height = 188;
	private ECExternalWindow panelWindow = null; // The control panel window
	private InspectableObjectPopup objectChoices = null;
	private Button clearPopupButton = null;
	private final String CLEAR_POPUP_COMMAND = "ClearPopup";
	private final String NOTHINGTOINSPECT = "NothingToInspect";
	private final String INSPECT_ERQ_COMMAND = "InspectERQCommand";
	private Button erqButton;

	public void refreshObjectDirectory() {
		Enumeration e;
		String name;
		boolean empty = true;
		Hashtable objectsWithoutCategory = Inspector.getObjectsWithoutCategory();
		Hashtable objectCategories = Inspector.getObjectCategories();

		objectChoices.removeAllItems();

		if (objectsWithoutCategory != null) {
			e = objectsWithoutCategory.keys();
      
			while (e.hasMoreElements()) {
				name = (String)e.nextElement();
				objectChoices.addItem(name,name);
				empty = false;
			}
		}

		if (objectCategories != null) {
			e = objectCategories.keys();
    
			while (e.hasMoreElements()) {
				name = (String)e.nextElement();
				objectChoices.addItem(name,"=" + name); // They start with "=", illegal in variable syntax
				empty = false;
			}
		}
		if (empty) {
			objectChoices.addItem("Inspectable Objects",NOTHINGTOINSPECT);
		}
	}

	public IFCInspectorUI() {
		this(null,null);
	}

	public IFCInspectorUI(Object object, String name) {
		inspectObject(object,name);		// Just to get ourselves started
	}

	/**
	 * Responsibility from EventProcessor, called to process events. Only called from IFC thread.
	 */

	public void processEvent(Event event) {
		if (! panelInitialized) initIFCInspectorUI(); // Initialize now, in the IFC thread
		if (event instanceof InspectorEvent) {
			InspectorEvent iEvent = (InspectorEvent)event;
			System.out.println("Received an event");
			if ((iEvent.getName() == null) && (iEvent.getObject() == null)) return;
			switch (iEvent.type()) {
			case 0:	return;
			case Inspector.INSPECT: if (iEvent.getObject() != null)
				inspect(iEvent.getObject(), iEvent.getName(), (ECExternalWindow)null);
			break;
			}
		}
	}

	/** Inspect an object with a given name. This is done by sending
	  our IFCInspectorUI an IFC event with the object and
	  the name. This way inspection is done in the IFC thread,
	  regardless of which thread the inspect method was invoked from.  */

	public void inspectObject(Object object, String name) {
		System.out.println("Inspect(" + ((name == null)?"null":name) + ")");
		if (cApplication == null) {
			try {
				cApplication = (ECApplication)Application.application();
			}
			catch (ClassCastException e) {
				cApplication = null;
			}
			if (cApplication == null) {
				System.out.println("Cannot inspect " + name + " - not running as an ECApplication");
				return;
			}
		}
		System.out.println("Sending event to inspect " + name);
		ECEvent event = new InspectorEvent(object,name);
		event.setProcessor(this);
		cApplication.getEventLoop().addEvent(event);
	}

	public void initIFCInspectorUI() {
		setBounds(0, 0, width, height); // Bounds for view (in window)
		inspectorViews = new Vector(30); // Currently open inspectorviews of inspected objects
		panelWindow = new ECExternalWindow(); // This call cannot be made outside of IFC thread
		Size windowSize = panelWindow.windowSizeForContentSize(width, height);
		panelWindow.setBounds(x, y, windowSize.width, windowSize.height);
		panelWindow.setResizable(false);
		panelWindow.addSubview(this);
		panelWindow.setOwner(this);
		panelWindow.setTitle("Inspector");
		setBuffered(true);

		// Inspector panel buttons

		clearPopupButton = Button.createPushButton(4, 50, 90, 16);
		clearPopupButton.setTitle("Drop all");
		clearPopupButton.setTarget(this);
		clearPopupButton.setCommand(CLEAR_POPUP_COMMAND);
		addSubview(clearPopupButton);	
    
		// Inspector popup menu 

		objectChoices = new InspectableObjectPopup(4, 10, 180, 24);
		objectChoices.addItem("Inspectable Objects",NOTHINGTOINSPECT);
		objectChoices.setTarget(this);
		addSubview(objectChoices);
		panelWindow.show();
		panelInitialized = true;
	}

	public static InspectorView findInspectorView(Object object, int index) {
		Enumeration e = inspectorViews.elements();
		InspectorView iv;

		while (e.hasMoreElements()) {
			iv = (InspectorView)e.nextElement();
			if (iv.isInspecting(object,index)) return iv;
		}
		return null;
	}

	public static boolean hasBitInspectCapability() {
		return true;				// for now
	}

	public static void addInspectorView(InspectorView iv) {
		inspectorViews.addElement(iv);
	}

	public static void removeInspectorView(InspectorView iv) {
		inspectorViews.removeElement(iv);
	}

	// These next few versions of inspect () can only be used on bona fide java objects

	static void inspect(Object object, String name, ECExternalWindow parentWindow) {

		String signature = object.getClass().toString().substring(6); // Strip "class "
		String fullName = Inspector.humanizeSignature(signature,null) + " " + name;

		// Is object already inspected in some inspectorview?

		InspectorView inspectorView = findInspectorView(object,InspectorView.OBJECT_ITSELF);

		if (inspectorView == null) { // No, go ahead and create new InspectorView
			Inspector iInspector = Inspector.createInspectorForObject(object,fullName,true,true);
			if (iInspector != null) {
				inspectorView = new InspectorView(); // Create new inspectorView
				inspectorView.setInspector(iInspector, InspectorView.OBJECT_ITSELF, object); // Connect the object to it
				IFCInspectorUI.addInspectorView(inspectorView); // Add this view to the control panel
				inspectorView.refreshObjectView(); // Update view's contents from the object (using description in the inspector)
				inspectorView.createInspectorWindow(fullName, parentWindow); // Create a window to display view in
			}
		} else {
			inspectorView.bringToFront();	// Already inspecting it - just show window
		}
	}

	/**
	 * Perform command; responsibility from Target interface, called when we
	 * are target of a command from an IFC control etc.
	 * Our commands come from the Inspector control panel popup (and buttons??)
	 */

	public void performCommand(String command, Object arg) {
		if (! panelInitialized) return;	// Ignore requests until we have our windows set up right
		if (NOTHINGTOINSPECT.equals(command)) return;
		if (CLEAR_POPUP_COMMAND.equals(command)) {
			Inspector.dropAllGatheredObjects();
			return;
		}
		if (INSPECT_ERQ_COMMAND.equals(command)) {
			// Inspect E Run Queue
		} else if (command.charAt(0) == '=') {
			// Category command
		} else {
			objectChoices.removeAllItems();
			objectChoices.addItem("Inspectable Objects",NOTHINGTOINSPECT);
			Object object = Inspector.getObjectsWithoutCategory().get(command);
			if (object != null) Inspector.inspect(object,command); // Command happens to be its name
			else throw new Error("Unexpected IFC Inspector command: " + command + " arg = " + arg);
			return;
		}
	}

	/*
	 * Responsibilities from WindowOwner - we only care about windowDidHide...
	 */

	public void windowDidBecomeMain(netscape.application.Window window) {
	}

	public void windowDidHide(netscape.application.Window window) {
		System.exit(0);
	}

	public void windowDidResignMain(netscape.application.Window window) {
	}

	public void windowDidShow(netscape.application.Window window) {
	}

	public boolean windowWillHide(netscape.application.Window window) {
		return true;
	}

	public boolean windowWillShow(netscape.application.Window window) {
		return true;
	}

	public void windowWillSizeBy(netscape.application.Window window, Size size) {
	}
}

/**
 * Class for displaying values in an Inspector (see ec.inspect.Inspector) in an
 * IFC view.
 *
 * The InspectorView displays the name, type and current value of all the
 * inspector fields. The user can update the value of the field (unless
 * it is of type OBJECT). The fields are displayed in a scrolling area.
 *
 * Known Bug: IFC text fields don't let you type "." (period). This is a show
 * stopper when entering floating point numbers, we need to add a workaround
 */
public class InspectorView extends View implements WindowOwner {

	/** The currently displayed inspector, describes the object. Can be semantic or bit inspector */
	private Inspector iInspector = null;
	private Object iObject = null;
	private int elementIndex = -1; // if this is non-negative, then we are displaying a primitive array view

	/** Scroll group containing the field views */
	private ScrollGroup iScrollGroup = null;

	private ECExternalWindow parentWindow = null;
	private ECExternalWindow ourWindow = null;

	private InspectorViewToolBar toolBar = null;
	private InspectorViewTitleBar titleBar = null;
	private boolean viewIsSemantic = true;

	public static final int OBJECT_ITSELF = -1;
	private FieldListView listView = null;
	private int winContentHeight = 400;
	private int winContentWidth = 500;

	/**
	 * Constructor, creates default sized view */
	public InspectorView() {
		this(0, 0, 600, 400);
	}

	/**
	 * Constructor, creates inspector view with given dimensions
	 */
	public InspectorView(int x, int y, int width, int height) {
		super(x, y, width, height + InspectorViewTitleBar.HEIGHT + InspectorViewToolBar.HEIGHT);	// SSS-5
		setBuffered(true);

		// Add scroll group for holding inspector field list view
		int toolHeight = InspectorViewToolBar.HEIGHT;
		int titleHeight = InspectorViewTitleBar.HEIGHT;
		//		System.out.println("scrollgroup width = " + width);

		iScrollGroup = new ScrollGroup(0, titleHeight + toolHeight, width, height);	// SSS-5
		iScrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
		iScrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		iScrollGroup.setHasVertScrollBar(true);
		iScrollGroup.setBorder(ECScrollBorder.border());
		addSubview(iScrollGroup);

		// Add tool bar

		toolBar = new InspectorViewToolBar(0, 0, ECApplication.getVertScrollBar(iScrollGroup).width(),toolHeight, this);
		toolBar.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		toolBar.sizeTo(width, toolBar.height());
		addSubview(toolBar);

		// Add title bar
		titleBar = new InspectorViewTitleBar(0, toolHeight,
											 ECApplication.getVertScrollBar(iScrollGroup).width(),titleHeight);
		titleBar.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		titleBar.sizeTo(width, titleBar.height());

		addSubview(titleBar);
	}

	public boolean isInspecting(Object object, int index) {
		if ((object == iObject) && (index == elementIndex)) return true;
		return false;
	}

	public void refreshObjectView(boolean useSemanticView) {
		if (viewIsSemantic != useSemanticView) {

			// Previous inspector is of the wrong kind - create new one.
			Inspector nextInspector = Inspector.createInspectorForObject(iObject, iInspector.getTopName(),
																		 !useSemanticView, useSemanticView);
			if (nextInspector == null) {
				System.out.println("Could not create other inspector for " + iObject);
				return;				// Couldn't create other inspector!
			}
			iInspector = nextInspector;
			viewIsSemantic = useSemanticView;
		}

		// Create appropriately sized list view
		//		System.out.println("iScrollGroup.width() = " + iScrollGroup.width());

		int availableWidth = iScrollGroup.width() - ECApplication.getVertScrollBar(iScrollGroup).width();
		listView = new FieldListView(iInspector, iObject);
		listView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		listView.sizeTo(availableWidth, listView.height()); // SSS-5 Affects scroll view height but not scroll bar
		//		System.out.println("availableWidth = " + availableWidth);
    
		// Put into scroll group
		iScrollGroup.setContentView(listView);

		// Save hints about window width and height to use if we later create window (we may have one already).
		winContentWidth = iScrollGroup.horizScrollBar().width();
		//	winContentHeight = listView.height() + titleBar.height() + toolBar.height() + 6; // 6 is a fudge 
		winContentHeight = ECApplication.getVertScrollBar(iScrollGroup).height();
	}

	public void refreshObjectView() {
		refreshObjectView(viewIsSemantic);
	}

	public void bringToFront() {	// Thanks to John Sullivan for this solution
		java.awt.Window awtWindow = AWTCompatibility.awtWindowForExternalWindow(ourWindow);
		awtWindow.hide();
		awtWindow.show();
	}

	void createInspectorWindow(String title, ECExternalWindow parentWindow) {
		int x = 0;
		int y = 0;
	
		if (parentWindow != null) {
			Rect parentBounds = ECApplication.getBoundsForExternalWindow(parentWindow);
			x = parentBounds.x + 20;
			y = parentBounds.y + 40; // Stagger new window from parent if we have one
		} // Otherwise just start at top left of screen

		ourWindow = new ECExternalWindow();
		Size windowSize = ourWindow.windowSizeForContentSize(winContentWidth, winContentHeight); // SSS
		ourWindow.setBounds(x, y, windowSize.width, windowSize.height); // SSS Bounds for window, not view
		ourWindow.setResizable(true);
		setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
		setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);

		// 	sizeTo(ourWindow.contentSize().width,ourWindow.contentSize().height);
		sizeTo(winContentWidth, winContentHeight);
		ourWindow.addSubview(this);
		this.parentWindow = parentWindow;
		ourWindow.setOwner(this);
		ourWindow.setTitle(title);	// An Inspector never knows its name - has to be provided from caller.
		ourWindow.show();
	}

	/**
	 * Set inspector to be viewed
	 */
	public void setInspector(Inspector iInspector, int n, Object iObject) {

		//	System.out.println("In setInspector - inspector is " + iInspector + " and object is " + iObject);
		this.iInspector = iInspector;
		this.iObject = iObject;
		elementIndex = n;

		// This is the first time we know what object we are displaying

		boolean allowBits = IFCInspectorUI.hasBitInspectCapability();
		boolean allowSemantic = iObject instanceof Inspectable;
		if (allowBits && allowSemantic) toolBar.enablePopup();
		else if (allowBits) toolBar.setBits();
	}

	/**
	 * Return inspector being viewed
	 */
	Inspector getInspector() {
		return iInspector;
	}

	public Rect getBounds() {
		if (ourWindow != null) return ECApplication.getBoundsForExternalWindow(ourWindow);
		return null;
	}
    
	/*
	 * Responsibilities from WindowOwner - we only care about windowDidHide...
	 */

	public void windowDidBecomeMain(netscape.application.Window window) {
	}

	public void windowDidHide(netscape.application.Window window) {
		IFCInspectorUI.removeInspectorView(this);
	}

	public void windowDidResignMain(netscape.application.Window window) {
	}

	public void windowDidShow(netscape.application.Window window) {
	}

	public boolean windowWillHide(netscape.application.Window window) {
		return true;
	}

	public boolean windowWillShow(netscape.application.Window window) {
		return true;
	}

	public void windowWillSizeBy(netscape.application.Window window, Size size) {
	}
}

/**
 * View class for containing a list of field views, each of which will
 * display a single field
 */
class FieldListView extends View {

	/** Margin at top of area before first entry */
	public static final int TOP_MARGIN = 4;

	private InspectorView inspectorView;

	/**
	 * Create view which contains a list of FieldViews
	 */
	FieldListView(Inspector inspector, Object iObject) {
		super();

		// Create field view for each field in inspector

		int size = inspector.getSize(iObject);
		int y = TOP_MARGIN;

		if (iObject != null) {
			for (int i = 0; i < size; i++) {

				FieldView fieldView = new FieldView(0, y, inspector, i, iObject);
				fieldView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
				addSubview(fieldView);

				// Increment y position
				y += fieldView.height();
			}
		}

		// Make ourselves tall enough to show all fields, set min size.
		sizeTo(0, y);				// SSS-OK
		setMinSize(FieldView.MIN_WIDTH, y);	// SSS-OK
	}
}


/**
 * Common superclass for FieldView and InspectorViewTitleBar, both
 * of which have to display text fields in proportional areas
 */
class InspectorProportionalTextFieldView extends View {

	/** Height of view */
	public static final int HEIGHT = 30;

	/** Minimum width */
	public static final int MIN_WIDTH = 100;

	/** Minimum height */
	public static final int MIN_HEIGHT = HEIGHT;

	/* Proportions of the different components; must add up to 1.0 */
	private static final double NAME_PROPORTION = 0.25;
	private static final double TYPE_PROPORTION = 0.25;
	private static final double VALUE_PROPORTION = 0.5;

	/* Margins */
	private int iLeftMargin = 0;
	private int iRightMargin = 0;

	/* The components */
	protected TextField iName = null;
	protected TextField iType = null;
	protected Button iValueButton = null; // Sometimes we have a button, not a textfield.
	protected Button iValueCheckBox = null; // Sometimes we have a checkbox
	protected TextField iValue = null; // Use this iff iValueButton is null.

	/**
	 * Constructor. Doesn't create text fields; subclass constructor must do that
	 */
	InspectorProportionalTextFieldView(int x, int y, int leftMargin, int rightMargin) {
		super(x, y, 0, HEIGHT);

		// Set margins
		iLeftMargin = leftMargin;
		iRightMargin = rightMargin;

		// Set resize behaviour
		setAutoResizeSubviews(false);
		setMinSize(MIN_WIDTH, MIN_HEIGHT);
	}


	/**
	 * Resize behaviour - resizes text fields proportionally
	 */
	public void didSizeBy(int deltaWidth, int deltaHeight) {

		// Let superclass do its standard stuff
		super.didSizeBy(deltaWidth, deltaHeight);

		// Proportionally resize text fields
		int xPos = iLeftMargin;
		int availableWidth = width() - (iLeftMargin + iRightMargin);

		// Name field at left
		iName.setBounds(xPos, 0,
						(int)(NAME_PROPORTION * availableWidth), height());
		xPos += iName.width();

		// Type field next
		iType.setBounds(xPos, 0,
						(int)(TYPE_PROPORTION * availableWidth), height());
		xPos += iType.width();

		if (iValueButton != null) {
			iValueButton.setBounds(xPos, 4, availableWidth - xPos, height() - 8);
		} else if (iValueCheckBox != null) {
			iValueCheckBox.setBounds(xPos+16, 6, 16, 16);
		} else iValue.setBounds(xPos, 0, availableWidth - xPos, height()); // Value field at right 
	}


	/**
	 * Utility to add a label style text field
	 */
	protected TextField addLabel(String value) {
		TextField label = new ECTextField();
		label.setStringValue(value);
		label.setTransparent(true);
		label.setBorder(null);
		label.setEditable(false);
		addSubview(label);
		return label;
	}

}


/**
 * View class for displaying a single Field. Displays name and
 * type as labels and provides editable text field for displaying and setting
 * the value
 */
class FieldView extends InspectorProportionalTextFieldView
implements Target
{

	/** Margin to left and right of components */
	public static final int MARGIN = 5;

	/* Commands */
	private static final String VALUE_CHANGED_COMMAND = "VALUE CHANGED COMMAND";
	private static final String TOGGLE_BOOLEAN_COMMAND = "TOGGLE BOOLEAN COMMAND";
	private static final String INSPECT_SUBOBJECT_COMMAND = "INSPECT SUBOBJECT COMMAND";

	private Inspector inspector = null;
	private Object fiObject = null;
	private int index;
	private String declaredSignature = null;;
	private String assignedSignature = null;

	/**
	 * Constructor
	 */
	FieldView(int x, int y, Inspector inspector, int index, Object iObject) {
		super(x, y, MARGIN, MARGIN);

		this.inspector = inspector;
		this.index = index;
		this.fiObject = iObject;

		String name = inspector.getName(index);
		Object value;
		boolean editable = inspector.isEditable(index);

		iValue = null;
		iValueButton = null;
		iValueCheckBox = null;

		declaredSignature = inspector.getDeclaredSignature(index);
		assignedSignature = declaredSignature; // True *most* of the time.

		// System.out.println("declaredSignature is " + declaredSignature);

		if ("Ljava/lang/String;".equals(declaredSignature)) { // A String

			iValue = new InspectorTextField();
			value = inspector.get(index, iObject);
			assignedSignature = InspectableClass.getAssignedSignature(value);
			if (value != null) iValue.setStringValue(value.toString());
			else {
				iValue.setStringValue("\"null\"");
				editable = false;
			}
		} else switch (declaredSignature.charAt(0)) {
 
		case 'L':			// An Object, which is not a String

			// If inspectable object is of type OBJECT we create a button for it

			value = inspector.get(index, iObject);
			if (value != null) {
				iValueButton = Button.createPushButton(x+5, y+2 ,90, 16);
				iValueButton.setTitle(value.toString());
			}
			else {
				iValueButton = Button.createPushButton(x+5, y+2 ,90, 16);
				iValueButton.setTitle("<null>");
			}
			break;

		case '[':

			iValueButton = Button.createPushButton(x+5, y+2, 90, 16);
			value = inspector.get(index, iObject);
			assignedSignature = InspectableClass.getAssignedSignature(value);
			if (value != null) iValueButton.setTitle(value.toString());
			else iValueButton.setTitle("[null]");
			break;

			// Primitive types. Note use of **FALL THROUGH**  in this switch statement

		case 'V':

			editable = false;			// For display only

		case 'B':
		case 'C':
		case 'F':
		case 'D':
		case 'I':
		case 'J':
		case 'S':

			iValue = new InspectorTextField();
			value = inspector.get(index, iObject);
			if (value != null) iValue.setStringValue(value.toString());
			else iValue.setStringValue("\"null\"");
			break;
      
		case 'Z':			// Make checkbox for boolean!

			iValueCheckBox = Button.createCheckButton(x+16, y+6, 12, 12);
			iValueCheckBox.setRaisedBorder(ECScrollBorder.border());
			iValueCheckBox.setLoweredBorder(ECScrollBorder.border());
			value = inspector.get(index, iObject);
			assignedSignature = InspectableClass.getAssignedSignature(value);
			iValueCheckBox.setState(((Boolean)value).booleanValue());
			break;
		case 'E':
		case '(':
		case 'A':
		default:			// Unknown type. Show toString() value but don't allow editing

			System.out.println(name + " is an unknown data type");
			iValue = new InspectorTextField();
			editable = false;		// Display only
			value = inspector.get(index, iObject);
			if (value != null) iValue.setStringValue(value.toString());
			else iValue.setStringValue("null");
		}

		// Create labels
		iName = addLabel(name);
		iType = addLabel(Inspector.humanizeSignature(declaredSignature,value));

		// Create text field or button, depending on the element type

		if (iValue != null) {
			iValue.setTarget(this);
			iValue.setEditable(editable);
			iValue.setCommand(VALUE_CHANGED_COMMAND);
			addSubview(iValue);
		} else if (iValueButton != null) {
			iValueButton.setTarget(this);
			iValueButton.setCommand(INSPECT_SUBOBJECT_COMMAND);
			addSubview(iValueButton);	
		} else if (iValueCheckBox != null) {
			iValueCheckBox.setTarget(this);
			iValueCheckBox.setCommand(TOGGLE_BOOLEAN_COMMAND);
			addSubview(iValueCheckBox);	
		}
	}

	/**
	 * Perform command; responsibility from Target interface, called when we
	 * are target of a command from an IFC control etc.
	 */
	public void performCommand(String command, Object arg) {
		if (command.equals(VALUE_CHANGED_COMMAND)) {
			valueChanged(((TextField)arg).stringValue());
		} else if (command.equals(TOGGLE_BOOLEAN_COMMAND)) {
			toggleCheckBox();
		} else if (command.equals(INSPECT_SUBOBJECT_COMMAND)) {
			ECExternalWindow w = null;
			RootView rv = rootView();
			if (rv != null) w = (ECExternalWindow)rv.externalWindow();
			Object nobject = inspector.get(index,fiObject);
			IFCInspectorUI.inspect(nobject,inspector.getName(index),w);
		}
		else throw new Error("Unexpected command: " + command + " arg = " + arg);
	}

	/**
	 * Called when value of field has changed
	 */
	private void valueChanged(String newValueString) {
		Object newValue = null;
		String elementSignature = inspector.getDeclaredSignature(index);

		// System.out.println("sig=" + inspector.getDeclaredSignature(index) + " type= " + InspectorType.STRING.getSignature());
		// System.out.println("newvaluestring is " + newValueString);

		try {
			if (InspectorType.BOOLEAN.getSignature().equals(elementSignature)) {
				newValue = new Boolean(newValueString);
			}
			else if (InspectorType.BYTE.getSignature().equals(elementSignature)) {
				newValue = new Byte(integerInRange(newValueString, -128, 255).intValue());
			}
			else if (InspectorType.CHAR.getSignature().equals(elementSignature)) {
				if (newValueString.length() == 1) {
					newValue = new Character(newValueString.charAt(0));
				}
				else if (newValueString.length() == 3 &&
						 newValueString.charAt(0) == '\'' &&
						 newValueString.charAt(2) == '\'') {
					newValue = new Character(newValueString.charAt(1));
				}
				else { // Should use Character.MIN_VALUE and MAX_VALUE, when available
					newValue = new Character((char)(integerInRange(newValueString, 0, 65535).intValue()));
				}
			}
			else if (InspectorType.DOUBLE.getSignature().equals(elementSignature)) {
				newValue = new Double(newValueString);
			}
			else if (InspectorType.FLOAT.getSignature().equals(elementSignature)) {
				newValue = new Float(newValueString);
			}
			else if (InspectorType.INT.getSignature().equals(elementSignature)) {
				newValue = new Integer(newValueString);
			}
			else if (InspectorType.LONG.getSignature().equals(elementSignature)) {
				newValue = new Long(newValueString);
			}
			else if (InspectorType.SHORT.getSignature().equals(elementSignature)) {
				newValue = new Short(newValueString);
			}
			else if (InspectorType.STRING.getSignature().equals(elementSignature)) {
				newValue = newValueString;
			}
			else {
				// Object, or unknown type. Nothing we can do
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}

		// Set new value, if there is one
		if (newValue != null) {
			inspector.set(index,fiObject,newValue);
		} else {
			String message = "Could not set value -\n" + newValueString +
			  " does not have signature " + elementSignature;
			// Wanted to use runAlertExternally but centering does not work in AWT 1.0
			Alert.runAlertInternally("Set value error", message, "Continue", null, null);
		}
		iValue.setStringValue(inspector.get(index,fiObject).toString());
	}

	private void toggleCheckBox() {
		inspector.set(index,fiObject,new Boolean(iValueCheckBox.state()));
		//	iValueCheckBox.setState(inspector.get(index,fiObject).booleanValue());
	}

	/**
	 * Check given string corresponds to integer in given range
	 */
	private Integer integerInRange(String stringValue, int low, int high) {
		Integer i = new Integer(stringValue);
		if (i.intValue() < low || i.intValue() > high) {
			throw new NumberFormatException("Out of range");
		}
		return i;
	}

}


/**
 * Tool bar, used by InspectorView above title bar
 */
class InspectorViewToolBar extends View implements Target {

	private static final String REFRESH_COMMAND = "REFRESH COMMAND";
	private static final String INSPECT_BITS_COMMAND = "INSPECT BITS COMMAND";
	private static final String INSPECT_SEMANTIC_COMMAND = "INSPECT SEMANTIC COMMAND";
	private static final Rect INSPECT_MODE_RECT = new Rect(4, 10, 96, 24);

	/** Height of view */
	public static final int HEIGHT = 50;

	/** Minimum width */
	public static final int MIN_WIDTH = 100;

	/** Minimum height */
	public static final int MIN_HEIGHT = HEIGHT;

	/* Proportions of the different components; must add up to 1.0 */
	private static final double NAME_PROPORTION = 0.25;
	private static final double TYPE_PROPORTION = 0.25;
	private static final double VALUE_PROPORTION = 0.5;
	private Popup inspectMode = null;
	private TextField inspectingBits;
	private Button refreshButton;
	private InspectorView inspectorView;

	/* Margins */
	//   private int iLeftMargin = 0;
	//   private int iRightMargin = 0;

	/* The components */
	//  protected TextField iName = null;
	//  protected TextField iType = null;
	//  protected Button iValueButton = null; // Sometimes we have a button, not a textfield.
	//  protected TextField iValue = null; // Use this iff iValueButton is null.

	/**
	 * Constructor
	 */
	InspectorViewToolBar(int x, int y, int width, int height, InspectorView inspectorView) {
		super(x, y, width, height);
		this.inspectorView = inspectorView;
		refreshButton = Button.createPushButton(180, 10, 60, 16);
		refreshButton.setTitle("Refresh");
		refreshButton.setTarget(this);
		refreshButton.setCommand(REFRESH_COMMAND);
		addSubview(refreshButton);	
	}

	void setBits() {
		if (inspectingBits == null) {
			inspectingBits = new TextField(INSPECT_MODE_RECT); // Replaces the popup menu with a static text string "Bits"
			inspectingBits.setStringValue("Bits");
			inspectingBits.setTransparent(true);
			inspectingBits.setBorder(null);
			inspectingBits.setEditable(false);
			addSubview(inspectingBits);
		}
	}

	void enablePopup() {
		if (inspectMode != null) return; // Already enabled
		if (inspectingBits != null) {
			removeSubview(inspectingBits);
			inspectingBits = null;
		}
		inspectMode= new Popup(INSPECT_MODE_RECT);
		inspectMode.addItem("Semantic",INSPECT_SEMANTIC_COMMAND);
		inspectMode.addItem("Bits",INSPECT_BITS_COMMAND);
		inspectMode.setTarget(this);
		//    inspectMode.setMinSize(MIN_WIDTH, MIN_HEIGHT);
		addSubview(inspectMode);
	}

	/**
	 * Perform command; responsibility from Target interface, called when we
	 * are target of a command from an IFC control etc.
	 */
	public void performCommand(String command, Object arg) {
		if (command.equals(INSPECT_SEMANTIC_COMMAND)) {
			inspectorView.refreshObjectView(true);
		} else if (command.equals(INSPECT_BITS_COMMAND)) {
			inspectorView.refreshObjectView(false);
		} else if (command.equals(REFRESH_COMMAND)) {
			inspectorView.refreshObjectView();
		} else {
			throw new Error("Unexpected command: " + command + " arg = " + arg);
		}
	}
}

/**
 * Title bar, used by InspectorView above scrolling area which displays
 * fields
 */
class InspectorViewTitleBar extends InspectorProportionalTextFieldView {

	/**
	 * Constructor
	 */
	InspectorViewTitleBar(int x, int y, int leftMargin, int rightMargin) {
		// Initialize super, allowing for margins values will have
		super(x, y,
			  leftMargin + FieldView.MARGIN,
			  rightMargin + FieldView.MARGIN);

		// Add labels
		iName = addLabel("Name");
		iType = addLabel("Type");
		iValue = addLabel("  Value");	// Extra spaces help alignment

	}


	/**
	 * Extend addLabel so our labels will be bold
	 */
	protected TextField addLabel(String value) {
		TextField result = super.addLabel(value);

		// Use bold font
		Font font = result.font();
		Font boldFont = Font.fontNamed(font.name(), Font.BOLD, font.size());
		if (boldFont != null) {
			result.setFont(boldFont);
		}

		return result;
	}

}


/**
 * Variation on text field with bigger indents
 */
class InspectorTextField extends ECTextField {

	public static final int EXTRA_INDENT = 5;


	public int leftIndent() {
		return super.leftIndent() + EXTRA_INDENT;
	}


	public int rightIndent() {
		return super.rightIndent() + EXTRA_INDENT;
	}

}

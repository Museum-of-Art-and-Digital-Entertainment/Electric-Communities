package ec.ui;


import netscape.application.*;

import ec.util.InspectorType;
import ec.util.InspectorElement;
import ec.util.Inspector;
import ec.util.Inspectable;
import ec.util.StandardInspectorElement;

import ec.util.*;


/**
 * Class for displaying values in an Inspector (see ec.util.Inspector) in an
 * IFC view.
 *
 * The InspectorView displays the name, type and current value of all the
 * inspector elements. The user can update the value of the element (unless
 * it is of type OBJECT). The elements are displayed in a scrolling area.
 *
 * Known Bug: IFC text fields don't let you type "." (period). This is a show
 * stopper when entering floating point numbers, we need to add a workaround
 */
public class InspectorView extends View {

  /** The current inspector */
  private Inspector iInspector = null;

  /** Scroll group containing the element views */
  private ScrollGroup iScrollGroup = null;


  /**
   * Constructor, creates zero sized view
   */
  public InspectorView() {
    this(0, 0, 0, 0);
  }


  /**
   * Constructor, creates inspector view with given dimensions
   */
  public InspectorView(int x, int y, int width, int height) {
    super(x, y, width, height);

    // Add scroll group for holding inspector element list view
    int titleHeight = InspectorViewTitleBar.HEIGHT;
    iScrollGroup = new ScrollGroup(0, titleHeight,
				   width, height - titleHeight);
    iScrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    iScrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    iScrollGroup.setHasVertScrollBar(true);
    iScrollGroup.setBorder(ECScrollBorder.border());
    addSubview(iScrollGroup);

    // Add title bar
    InspectorViewTitleBar titleBar = 
      new InspectorViewTitleBar(0, 0,
				0, ECApplication.getVertScrollBar(iScrollGroup).width());
    titleBar.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    titleBar.sizeTo(width, titleBar.height());
    addSubview(titleBar);
  }


  /**
   * Set inspector to be viewed. Should only be called from IFC thread
   */
  public void setInspector(Inspector inspector) {

    // Set instance variable
    iInspector = inspector;

    // Create appropriately sized list view
    int availableWidth =
      iScrollGroup.width() - ECApplication.getVertScrollBar(iScrollGroup).width();
    InspectorElementListView listView =
      new InspectorElementListView(inspector);
    listView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    listView.sizeTo(availableWidth, listView.height());
    
    // Put into scroll group
    iScrollGroup.setContentView(listView);
  }


  /**
   * Return inspector being viewed
   */
  Inspector getInspector() {
    return iInspector;
  }

}


/**
 * View class for containing a list of element views, each of which will
 * display a single element
 */
class InspectorElementListView extends View {

  /** Margin at top of area before first entry */
  public static final int TOP_MARGIN = 4;


  /**
   * Create view which contains a list of InspectorElementViews
   */
  InspectorElementListView(Inspector inspector) {
    super();

    // Create element view for each element in inspector
    int size = inspector.getSize();
    int y = TOP_MARGIN;
    for (int i = 0; i < size; i++) {

      // Create new element view, add in
      InspectorElementView elementView =
	new InspectorElementView(0, y, inspector.get(i));
      elementView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
      addSubview(elementView);

      // Increment y position
      y += elementView.height();
    }

    // Make ourselves tall enough to show all elements, set min size. If
    // we had no entries we set height to zero, no need for a margin
    int height = (y == TOP_MARGIN) ? 0 : y;
    sizeTo(0, height);
    setMinSize(InspectorElementView.MIN_WIDTH, height);
  }

}


/**
 * Common superclass for InspectorElementView and InspectorViewTitleBar, both
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
  private static final double NAME_PROPORTION = 0.3;
  private static final double TYPE_PROPORTION = 0.3;
  private static final double VALUE_PROPORTION = 0.4;

  /* Margins */
  private int iLeftMargin = 0;
  private int iRightMargin = 0;

  /* The components */
  protected TextField iName = null;
  protected TextField iType = null;
  protected TextField iValue = null;


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

    // Value field at right
    iValue.setBounds(xPos, 0, availableWidth - xPos, height());
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
 * View class for displaying a single InspectorElement. Displays name and
 * type as labels and provides editable text field for displaying and setting
 * the value
 */
class InspectorElementView extends InspectorProportionalTextFieldView
 implements Target
{

  /** Margin to left and right of components */
  public static final int MARGIN = 5;

  /* Commands */
  private static final String VALUE_CHANGED_COMMAND = "VALUE CHANGED COMMAND";

  /** Inspector element */
  private InspectorElement iElement = null;


  /**
   * Constructor
   */
  InspectorElementView(int x, int y, InspectorElement element) {
    super(x, y, MARGIN, MARGIN);

    // Set element instance variable
    iElement = element;

    // Create labels
    iName = addLabel(element.getName());
    iType = addLabel(element.getType().toString());

    // Create editable value field
    iValue = new InspectorTextField();
    iValue.setStringValue(element.getValue().toString());
    iValue.setTarget(this);
    iValue.setCommand(VALUE_CHANGED_COMMAND);

    // If element is of type OBJECT we don't know how to update it
    if (element.getType() == InspectorType.OBJECT) {
      iValue.setEditable(false);
      iValue.setBackgroundColor(Color.lightGray);
    }

    // Add value field
    addSubview(iValue);
  }


  /**
   * Perform command; responsibility from Target interface, called when we
   * are target of a command from an IFC control etc.
   */
  public void performCommand(String command, Object arg) {
    if (command.equals(VALUE_CHANGED_COMMAND)) {
      valueChanged(((TextField)arg).stringValue());
    }
    else {
      throw new Error("Unexpected command: " + command + " arg = " + arg);
    }
  }


  /**
   * Called when value of element has changed
   */
  private void valueChanged(String newValueString) {
    Object newValue = null;
    try {
      if (iElement.getType() == InspectorType.BOOLEAN) {
	newValue = new Boolean(newValueString);
      }
      else if (iElement.getType() == InspectorType.BYTE) {
	newValue = integerInRange(newValueString, -128, 255);
      }
      else if (iElement.getType() == InspectorType.CHAR) {
	if (newValueString.length() == 1) {
	  newValue = new Character(newValueString.charAt(0));
	}
	else if (newValueString.length() == 3 &&
		 newValueString.charAt(0) == '\'' &&
		 newValueString.charAt(2) == '\'') {
	  newValue = new Character(newValueString.charAt(1));
	}
	else {
	  // Should use Character.MIN_VALUE and MAX_VALUE, when available
	  Integer intValue = integerInRange(newValueString, 0, 65535);
	  newValue = new Character((char)(intValue.intValue()));
	}
      }
      else if (iElement.getType() == InspectorType.DOUBLE) {
	newValue = new Double(newValueString);
      }
      else if (iElement.getType() == InspectorType.FLOAT) {
	newValue = new Float(newValueString);
      }
      else if (iElement.getType() == InspectorType.INT) {
	newValue = new Integer(newValueString);
      }
      else if (iElement.getType() == InspectorType.LONG) {
	newValue = new Long(newValueString);
      }
      else if (iElement.getType() == InspectorType.SHORT) {
	// This can be fixed in Java 1.1 when there is a real Short type;
	// Short will probably have its own string constructor
	newValue = integerInRange(newValueString, -32768, 32767);
      }
      else {
	// Object, or unknown type. Nothing we can do
      }
    }
    catch (NumberFormatException e) {
    }

    // Set new value, if there is one
    if (newValue != null) {
      iElement.setValue(newValue);
    }
    else {
      String message =
	"Could not set value;\n" + newValueString +
	" is not a valid " + iElement.getType().toString();
      Alert.runAlertExternally("Set value error", message,
			       "Continue", null, null);
    }
    iValue.setStringValue(iElement.getValue().toString());
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
 * Title bar, used by InspectorView above scrolling area which displays
 * elements
 */
class InspectorViewTitleBar extends InspectorProportionalTextFieldView {

  /**
   * Constructor
   */
  InspectorViewTitleBar(int x, int y, int leftMargin, int rightMargin) {
    // Initialize super, allowing for margins values will have
    super(x, y,
	  leftMargin + InspectorElementView.MARGIN,
	  rightMargin + InspectorElementView.MARGIN);

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


/**
 * Test class
 */
class InspectorViewTester implements Inspectable, WindowOwner
{
  private boolean iBoolean = false;
  private byte iByte = 5;
  private char iChar = 'x';
  private short iShort = 3001;
  private int iInt = 0xffff * 2;
  private long iLong = 0xffffffffl * 2;
  private float iFloat = 0.5f;
  private double iDouble = 2.3;
  private String iString = "yadayada";

  public Inspector createInspector ()   {

    StandardInspectorElement[] elements = {
      new StandardInspectorElement(InspectorType.BOOLEAN,
				   "A boolean",
				   fun () { return (new Boolean(iBoolean)); },
				   fun (Object v) {
				     iBoolean = ((Boolean)v).booleanValue();
				   }),
      new StandardInspectorElement(InspectorType.BYTE,
				   "A byte",
				   fun () { return (new Integer(iByte)); },
				   fun (Object v) {
				     iByte = (byte)((Integer)v).intValue();
				   }),
      new StandardInspectorElement(InspectorType.SHORT,
				   "A short",
				   fun () { return (new Integer(iShort)); },
				   fun (Object v) {
				     iShort = (short)((Integer)v).intValue();
				   }),
      new StandardInspectorElement(InspectorType.INT,
				   "An integer",
				   fun () { return (new Integer(iInt)); },
				   fun (Object v) {
				     iInt = ((Integer) v).intValue();
				   }),
      new StandardInspectorElement(InspectorType.LONG,
				   "A long",
				   fun () { return (new Long(iLong)); },
				   fun (Object v) {
				     iLong = ((Long) v).longValue();
				   }),
      new StandardInspectorElement(InspectorType.CHAR,
				   "A character",
				   fun () { return (new Character(iChar)); },
				   fun (Object v) {
				     iChar = ((Character) v).charValue();
				   }),
      new StandardInspectorElement(InspectorType.FLOAT,
				   "A float",
				   fun () { return (new Float(iFloat)); },
				   fun (Object v) {
				     iFloat = ((Float) v).floatValue();
				   }),
      new StandardInspectorElement (InspectorType.DOUBLE,
				    "A double",
				    fun () { return (new Double(iDouble)); },
				    fun (Object v) {
				      iDouble = ((Double) v).doubleValue();
				    }),
      new StandardInspectorElement (InspectorType.OBJECT,
				    "A string",
				    fun () { return (iString); },
				    fun (Object v) {
				      iString = (String) v;
				    })
    };
    
    return (new Inspector (elements));
  }


  public static void main(String[] args) {

    // Application
    ECApplication app = new ECApplication();

    // Main window
    int contentWidth = 300;
    int contentHeight = 200;
    ECExternalWindow window = new ECExternalWindow();
    Size windowSize =
      window.windowSizeForContentSize(contentWidth, contentHeight);
    window.setBounds(100, 100, windowSize.width, windowSize.height);
    window.setResizable(true);

    InspectorView inspectorView =
      new InspectorView(0, 0, contentWidth, contentHeight);
    inspectorView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
    inspectorView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
    window.addSubview(inspectorView);

    InspectorViewTester tester = new InspectorViewTester ();
    Inspector inspector = tester.createInspector ();
    inspectorView.setInspector(inspector);

    window.setOwner(tester);
    window.show();
    app.run();
  }


  /*
   * Responsibilities from WindowOwner - we only care about windowDidHide...
   */

  public void windowDidBecomeMain(Window window) {
  }

  public void windowDidHide(Window window) {
    System.exit(0);
  }

  public void windowDidResignMain(Window window) {
  }

  public void windowDidShow(Window window) {
  }

  public boolean windowWillHide(Window window) {
    return true;
  }

  public boolean windowWillShow(Window window) {
    return true;
  }

  public void windowWillSizeBy(Window window, Size size) {
  }

}

  

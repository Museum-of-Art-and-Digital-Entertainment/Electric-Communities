package ec.ui;

import ec.e.inspect.*;
import ec.e.openers.*;
import ec.ifc.app.*;
import netscape.application.*;
import java.util.Vector;
import java.util.Hashtable;

/**

 * View class for displaying a single Field. Displays name and type as
 * labels and provides editable text field for displaying and setting
 * the value.  Under the variable name and the value field/button, the
 * declared type of the variable is (optionally) displayed. This
 * string is the value of the ivar typeText.

 */

class FieldView extends InspectorProportionalTextFieldView
implements Target {

    /** Margin to left and right of components */
    public static final int MARGIN = 5;

    /* Commands */
    private static final String VALUE_CHANGED_COMMAND = "VALUE CHANGED COMMAND";
    private static final String TOGGLE_BOOLEAN_COMMAND = "TOGGLE BOOLEAN COMMAND";
    private static final String INSPECT_SUBOBJECT_COMMAND = "INSPECT SUBOBJECT COMMAND";
    private static final int INSPBUTTONMAXTEXT = 44; // Max number of chars on a value button

    private Inspector inspector = null;
    private int index;
    private String declaredSignature = null;;
    private String assignedSignature = null;
    private String typeText = null; // Small print - type information
    private IFCInspectorUI myUI = null;
    private String myParentName = null;
    private InspectorView parentView = null;
    private Object myValue = null;

    // public static Color showingColor = new Color(128,0,255); // Color indicating window is open
    public static Color showingColor = netscape.application.Color.cyan;
    public static Color seenColor = new Color(128,0,255); // Netscape visited-link purple
    // public static Color seenColor = new Color(200,0,255); // Netscape visited-link purple

    private String[] buttonText(Object value, String declaredSignature,
                                boolean showAddresses, boolean useFullNames) {
        String result[] = new String[2]; // Button text and fine print text, in an array.
        boolean showFinePrint = true;
        String valueAsText = null;
        result[0] = null;

        if (value == null) result[0] = "null"; // This is the text in the button itself
        else {
            if (value instanceof Hashtable)
                valueAsText = "H[ " + ((Hashtable)value).size() + " ]";
            else if (value instanceof Vector)
                valueAsText = "V[ " + ((Vector)value).size() + " ]";
            else try {
                valueAsText = value.getClass().getName();
            } catch (Throwable e) {
                result[0] = "**Exception** " + e;
                // EStdio.err().println("Exception when creating button text:" + e);
            }
            if (declaredSignature.equals(valueAsText.replace('.','/')))
                showFinePrint = false;
            valueAsText = Inspector.humanizeSignature(valueAsText,value);
            if (!useFullNames)
                valueAsText = Inspector.textAfterLastDot(valueAsText);
            if (showAddresses && Inspector.runningUnderDebuggingVM())
                valueAsText += " @ " + Inspector.addressString(Inspector.addressOf(value),1);
            result[0] = valueAsText;
        }

        if (showFinePrint) {
            if (useFullNames)
                result[1] = Inspector.humanizeSignature(declaredSignature, value);
            else result[1] = Inspector.textAfterLastDot
                   (Inspector.humanizeSignature(declaredSignature, value));
        } else result[1] = null; // No fine print
        return result;
    }

    /**

     * Constructor.

     */


    FieldView(int x, int y, Inspector inspector, int index,
              boolean useFullNames, boolean showAddresses,
              IFCInspectorUI myUI, String parentName,
              InspectorView parentView) {
        super(x, y, MARGIN, MARGIN);

        this.inspector = inspector;
        this.index = index;
        this.myUI = myUI;
        this.parentView = parentView;
        myParentName = parentName;
        this.myValue = inspector.get(index);
        String name = inspector.getName(index);
        boolean editable = inspector.isEditable(index);

        iValue = null;
        iValueButton = null;
        iValueCheckBox = null;

        declaredSignature = inspector.getDeclaredSignature(index);
        assignedSignature = declaredSignature; // True *most* of the time.

        if (declaredSignature.startsWith("L")) { // They *may* be different!
            assignedSignature = inspector.getAssignedSignature(index);
        }

        if (("Ljava/lang/String;".equals(declaredSignature)) ||
            ("Ljava/lang/String;".equals(assignedSignature))) { // A String
            iValue = new InspectorTextField();
            assignedSignature = inspector.getAssignedSignature(index);
            typeText = "String";
            if (myValue != null) iValue.setStringValue(myValue.toString());
            else {
                iValue.setStringValue("<null>");
                editable = false;
            }
        } else {
            char sig0 = declaredSignature.charAt(0);

        outer:
            switch (sig0) {
 
            case 'L':           // An Object, which is not a String
            case '[':

                String[] texts = buttonText(myValue, declaredSignature, showAddresses, useFullNames);
                iValueButton = new InspectorButton(x+5, y+2 ,90, 16);
                iValueButton.setTitle(texts[0]);
                updateFieldColor();
                typeText = texts[1];
                parentView.addToFieldViewsVector(this); // Collect buttons in InspectorView vector
                break;

                // Primitive types. Note use of **FALL THROUGH**  in this switch statement

            case 'V':

                editable = false;           // For display only
                break;

            case 'Z':           // Make checkbox for boolean!

                typeText = "boolean";
                assignedSignature = inspector.getAssignedSignature(index);
                if (editable) {
                    iValueCheckBox = Button.createCheckButton(x+16, y+6, 12, 12);
                    iValueCheckBox.setRaisedBorder(ECScrollBorder.border());
                    iValueCheckBox.setLoweredBorder(ECScrollBorder.border());
                    iValueCheckBox.setState(((Boolean)myValue).booleanValue());
                }
                else {              // uneditable booleans are shown as background text
                    iValue = new InspectorTextField();
                    if (((Boolean)myValue).booleanValue()) iValue.setStringValue("true");
                    else iValue.setStringValue("false");
                }
                break;

            default:            // Primitive type (except boolean) or unknown type. 
                                // These are the data types that are displayed 
                                // in an editable text field.

                switch (sig0) { // Switching on the same character again!

                    // I would have preferred to use a goto here! The code would have been clearer.

                case 'B':       typeText = "byte"; break;
                case 'C':       typeText = "char"; break;
                case 'F':       typeText = "float"; break;
                case 'D':       typeText = "double"; break;
                case 'I':       typeText = "int"; break;
                case 'J':       typeText = "long"; break;
                case 'S':       typeText = "short"; break;

                default:            // Unknown data type

                    System.out.println(name + " is of an unknown data type with declared signature '" + declaredSignature + "'");
                    iValue = new InspectorTextField();
                    editable = false;       // Display only
                    if (myValue != null) iValue.setStringValue(myValue.toString());
                    else iValue.setStringValue("null");
                    assignedSignature = inspector.getAssignedSignature(index);
                    typeText = Inspector.humanizeSignature(assignedSignature,myValue);
                    break outer;    // Done with unknown data types
                }

                // Handle primitive data types
                // They are all shown in an editable text field

                iValue = new InspectorTextField();
                iValue.setStringValue(myValue.toString());
                break;
            }
        }

        // Create labels
        iName = addLabel(name);
        iName.setJustification(Graphics.RIGHT_JUSTIFIED);

        // Create text field or button, depending on the element type

        if (iValue != null) {
            iValue.setEditable(editable);
            if (editable) {
                iValue.setTarget(this);
                iValue.setCommand(VALUE_CHANGED_COMMAND);
            } else {
                iValue.setTransparent(true);
                iValue.setBorder(null);
            }
            addSubview(iValue);
        } else if (iValueButton != null) {
            iValueButton.setTarget(this);
            iValueButton.setCommand(INSPECT_SUBOBJECT_COMMAND);
            addSubview(iValueButton);   
        } else if (iValueCheckBox != null) {
            if (editable) {     // We can create non-responsive checkbox if we want to!
                iValueCheckBox.setTarget(this);
                iValueCheckBox.setCommand(TOGGLE_BOOLEAN_COMMAND);
            }
            addSubview(iValueCheckBox); 
        }
        iType = addFinePrint(typeText);
    }

    public void updateFieldColor() {
        updateButtonColor(iValueButton,myUI.findInspectorView(myValue));
    }

    public static void updateButtonColor(Button button, InspectorView oldView) {
        if (button != null && oldView != null) {
            if (oldView.hasWindow()) {
                button.setRaisedColor(showingColor); // Aggressive Highlight
                button.setTitleColor(netscape.application.Color.black);
            } else {
                button.setTitleColor(seenColor); // look like visited URL
                button.setRaisedColor(netscape.application.Color.lightGray);
            }
            button.setDirty(true);
        }
    }

    /**

     * Perform command; responsibility from Target interface, called
     * when we are target of a command from an IFC control etc.

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
            Object nobject = inspector.get(index); // Use myValue??
            if (nobject == null) return; // Can't inspect null

            // Right click gives new window.
            // Control key reverses meaning of mouse.

            boolean reUseWindow =
              ((!((InspectorButton)arg).wasRightClick()) ^
               ((InspectorButton)arg).wasControlClick());
            myUI.inspect(nobject,inspector.getName(index), w,
                          parentView, myParentName, reUseWindow);
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
            switch(elementSignature.charAt(0)) {
            case 'Z': {
                newValue = new Boolean(newValueString);
                break;
            }
            case 'B': {
                newValue = new java.lang.Byte((byte)integerInRange(newValueString, -128, 255).intValue());
                break;
            }
            case 'C': {
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
                break;
            }
            case 'S': {
                newValue = new java.lang.Short(newValueString);
                break;
            }
            case 'I': {
                newValue = new Integer(newValueString);
                break;
            }
            case 'J': {
                newValue = new Long(newValueString);
                break;
            }
            case 'F': {
                newValue = new Float(newValueString);
                break;
            }
            case 'D': {
                newValue = new Double(newValueString);
                break;
            }
            default: {
                newValue = newValueString; // Any object - Should be String only at this point XXX Verify this
                break;
            }
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;             // Don't assign anything
        }

        // Set new value, if there is one
        if (newValue != null) {
            inspector.set(index,newValue);
        } else {
            String message = "Could not set value -\n" + newValueString +
              " does not have signature " + elementSignature;
            // Wanted to use runAlertExternally but centering does not work in AWT 1.0
            Alert.runAlertExternally("Set value error", message, "Continue", null, null);
        }
        iValue.setStringValue(inspector.get(index).toString()); // Use myValue??
    }

    private void toggleCheckBox() {
        inspector.set(index,new Boolean(iValueCheckBox.state()));
        //  iValueCheckBox.setState(inspector.get(index).booleanValue()); // Use myValue??
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

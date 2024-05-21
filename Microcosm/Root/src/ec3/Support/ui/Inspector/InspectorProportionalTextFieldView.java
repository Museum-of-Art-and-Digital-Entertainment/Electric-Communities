package ec.ui;

import ec.ifc.app.*;
import netscape.application.*;

/**

 * Common superclass for FieldView and InspectorViewTitleBar, both of
 * which have to display text fields in proportional areas

 */

class InspectorProportionalTextFieldView extends View {

    /** Height of view */
    public static final int HEIGHT = 40;

    /** Minimum width */
    public static final int MIN_WIDTH = 100;

    /** Minimum height */
    public static final int MIN_HEIGHT = HEIGHT;

    /** height of type information */
    public static final int TYPE_HEIGHT = 14;

    /** Indentation of type information */

    public static final int TYPE_MARGIN = 30;

    /* Proportions of the different components; must add up to 1.0 */
    private static final double NAME_PROPORTION = 0.4;
    private static final double VALUE_PROPORTION = 0.6;

    /** Vertical spacing between button/value field and type info */

    private static final int VERTICAL_SPACING = 4;

    /* Height of variable name and buttons/value field.
       The type information (if displayed) takes up some TYPEHEIGHT of vertical space */

    private static int MAIN_HEIGHT = HEIGHT - TYPE_HEIGHT - VERTICAL_SPACING;

    /* Margins */
    private int iLeftMargin = 0;
    private int iRightMargin = 0;

    /* The components */
    protected TextField iName = null;
    protected TextField iType = null;
    protected InspectorButton iValueButton = null; // Sometimes we have a button, not a textfield.
    protected Button iValueCheckBox = null; // Sometimes we have a checkbox
    protected TextField iValue = null; // Use this iff iValueButton is null.
    protected static Font tinyFont = new Font("Times", 0, 10);
    protected static Font classNameFont = new Font("Times", netscape.application.Font.BOLD, 16);

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
                        (int)(NAME_PROPORTION * availableWidth), MAIN_HEIGHT);
        xPos += iName.width() + 10; // Leave some space after name

        if (iValueButton != null) {
            iValueButton.setBounds(xPos, 4, availableWidth - xPos, MAIN_HEIGHT);
        } else if (iValueCheckBox != null) {
            iValueCheckBox.setBounds(xPos+16, 6, 16, 16);
        } else iValue.setBounds(xPos, 0, availableWidth - xPos, MAIN_HEIGHT); // Value field at right 

        // Type field, if it exists, is a line below in fine print
        if (iType != null) iType.setBounds(xPos + 10, MAIN_HEIGHT + VERTICAL_SPACING, availableWidth - (xPos + 20),
                                           TYPE_HEIGHT - VERTICAL_SPACING);
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

    /**

     * Returns the height of the field 

     */

    public int height() {
        if (iType == null) return MAIN_HEIGHT; // If no type info, then we are smaller
        else return HEIGHT;
    }

    /*

     * Fine print field used to explicitly specify class names when
     * ambiguities are possible

     */

  protected TextField addFinePrint(String value) {
    TextField result = addLabel(value);
    result.setFont(tinyFont);
    return result;
  }
}


package ec.ui;
import ec.e.inspect.*;
import netscape.application.*;

/**

 * View class for containing a list of field views, each of which will
 * display a single field

 */

class FieldListView extends View {
    private static final int TOP_MARGIN = 4; // Margin at top of area before first entry

    /**

     * Constructor. Create view which contains a list of
     * FieldViews. For instances the display is split into sections,
     * one for each classLevel of class inheritance, with the
     * superclasses on top, and therefore "Object" at the very top.
     * Each per-class section starts with a yellow horizontal line and
     * the name of the class to the left below the line.

     */

    FieldListView(Inspector inspector, boolean useFullNames, boolean showAddresses,
                  IFCInspectorUI myUI, String parentName, InspectorView parentView) {
        super();
        if (inspector == null) return;
        int y = TOP_MARGIN;
        int totalNumberOfFields = inspector.getNumberFields();
        int[] varsAtClassLevel = inspector.getInheritedNumberFields();

        if (varsAtClassLevel != null) { // We are displaying an object with superclass structure
            int subClassDepth = varsAtClassLevel.length; // Subclass depth including Object
            int fieldNumber = 0; // Running index of field numbers
            int lastField = 0;  // Last field number for a certain level
            
//             System.out.println("Class hierarchy:");
//             for (int i = 0; i<varsAtClassLevel.length; i++) {
//                 System.out.print(" -> " + inspector.getInheritedClassName(i));
//             }
//             System.out.println("");

            for (int classLevel = 0; classLevel < subClassDepth; classLevel++) {
                
                // Starting a new subclass. Draw a line across the display.

                ContainerView horLine = new ContainerView(0, y + 1, 100, 6);
                horLine.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
                horLine.setBackgroundColor(netscape.application.Color.yellow);
                addSubview(horLine);
                y += horLine.height() + 2; // Increment y position by the height of the line we added

                // Then add the class name to the display. The
                // inherited class names are ordered backwards from
                // what we want, so we subtract the index from subClassDepth;              

                String className = inspector.getInheritedClassName((subClassDepth - 1) - classLevel);

                if (className != null) {
                    if (!useFullNames) className = Inspector.textAfterLastDot(className);
                    TextField classNameView = new TextField(5, y + 1, 200, 12);
                    classNameView.setTransparent(true);
                    classNameView.setBorder(null);
                    classNameView.setStringValue(className);
                    addSubview(classNameView);
                    y += classNameView.height() + 2; // Increment y position by height of label we added
                }

                // Display our instance variable fields, if any.

                lastField += varsAtClassLevel[classLevel]; // Index of last one.
                
                while (fieldNumber < lastField) { // May not be any!
                    FieldView fieldView = new FieldView(0, y, inspector, fieldNumber, useFullNames, showAddresses, myUI, parentName, parentView);
                    fieldView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
                    addSubview(fieldView);
                    y += fieldView.height(); // Increment y position by the height of the view we added
                    fieldNumber++;
                }
            }
        } else { /* Handle arrays in a similar manner */
            for (int i = 0; i < totalNumberOfFields; i++) {
                FieldView fieldView = new FieldView(0, y, inspector, i, useFullNames, showAddresses, myUI, parentName, parentView);
                fieldView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
                addSubview(fieldView);
                y += fieldView.height(); // Increment y position by the height of the line we added
            }
        }
        sizeTo(0, y);           // Make ourselves tall enough to show all fields, set min size.
        setMinSize(FieldView.MIN_WIDTH, y);
    }
}


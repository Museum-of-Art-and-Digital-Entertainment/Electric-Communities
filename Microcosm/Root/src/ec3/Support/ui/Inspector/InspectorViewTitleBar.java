package ec.ui;

import netscape.application.*;

/**
 * Title bar, used by InspectorView above scrolling area which displays
 * fields
 */
class InspectorViewTitleBar extends InspectorProportionalTextFieldView {

    // public static int HEIGHT = 17;

    /**

     * Constructor

     */

    InspectorViewTitleBar(int x, int y, int leftMargin, int rightMargin) {
        // Initialize super, allowing for margins values will have
        super(x, y,
              leftMargin,       //  + FieldView.MARGIN,
              rightMargin);     //  + FieldView.MARGIN);

        // Add labels
        iName = addLabel("Name");
        iName.setJustification(Graphics.RIGHT_JUSTIFIED);
        iValue = addLabel("  Value");   // Extra spaces help alignment
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

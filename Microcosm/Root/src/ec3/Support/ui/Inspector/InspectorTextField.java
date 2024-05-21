package ec.ui;

import ec.ifc.app.*;

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

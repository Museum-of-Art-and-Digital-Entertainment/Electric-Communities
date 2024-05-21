package ec.ui;
import netscape.application.*;

/**

 * Class added to allow the ButtonListView class manage its own layout
 * while avoiding a method name collision. (If you try to implement
 * the LayOutManager interface in ButtonListView, then you get two
 * different methods named addSubview() which is pessimal)

 */

public class ButtonListLayoutManager implements LayoutManager {

    ButtonListView ourButtonListView;

    ButtonListLayoutManager(ButtonListView view) {
        ourButtonListView = view;
    }

    public void addSubview(View view) {};
    public void removeSubview(View view) {};

    public void layoutView(View subview, int deltaWidth, int deltaHeight) {
        ourButtonListView.layoutView1(subview,deltaWidth,deltaHeight);
    }
}

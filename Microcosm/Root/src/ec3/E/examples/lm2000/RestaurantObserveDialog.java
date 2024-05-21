
package ec.examples.lm2000;

/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    RestaurantObserveDialog theRestaurantObserveDialog;
    theRestaurantObserveDialog = new RestaurantObserveDialog(this);
    theRestaurantObserveDialog.show();

    You can add controls to AboutBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

import java.awt.*;

public class RestaurantObserveDialog extends Dialog {

    public void Init(Restaurant r) {
        RestaurantNameLabel.setText(r.name);
        Double d_cost = new Double(r.cost);
        AveragePriceLabel.setText(d_cost.toString());
        Integer i_minutes = new Integer(r.minutes);
        MealTimeLabel.setText(i_minutes.toString());
    }

    public RestaurantObserveDialog(Frame parent) {

	    super(parent, "View Restaurant", true);

	    //{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 434, insets().top + insets().bottom + 204);
        label=new Label("Restaurant");
        add(label);
        label.reshape(insets().left + 27,insets().top + 24,153,24);
        label2=new Label("Price");
        add(label2);
        label2.reshape(insets().left + 27,insets().top + 72,117,24);
        label3=new Label("Minutes per Meal");
        add(label3);
        label3.reshape(insets().left + 27,insets().top + 120,162,32);
        OkayButton=new Button("OK");
        add(OkayButton);
        OkayButton.reshape(insets().left + 333,insets().top + 128,63,32);

        RestaurantNameLabel=new Label("");
        add(RestaurantNameLabel);
        RestaurantNameLabel.reshape(insets().left + 189,insets().top + 24,216,24);
        AveragePriceLabel=new Label("");
        add(AveragePriceLabel);
        AveragePriceLabel.reshape(insets().left + 189,insets().top + 72,126,24);
        MealTimeLabel=new Label("");
        add(MealTimeLabel);
        MealTimeLabel.reshape(insets().left + 216,insets().top + 120,72,32);
        //}}

    	setResizable(false);
    }

    public synchronized void show() {

    	Rectangle bounds = getParent().bounds();
    	Rectangle abounds = this.bounds();

    	move(bounds.x + (bounds.width - abounds.width)/ 2,
    	     bounds.y + (bounds.height - abounds.height)/2);

    	super.show();
    }

    public synchronized void wakeUp() {
    	notify();
    }

    public boolean handleEvent(Event event) {

        if (event.id == Event.ACTION_EVENT && event.target == OkayButton) {
                clickedOkayButton();
                return true;
        }
        else
    	if (event.id == Event.WINDOW_DESTROY) {
    	    hide();
    	    return true;
    	}
    	return super.handleEvent(event);
    }

    //{{DECLARE_CONTROLS
    Label label;
    Label label2;
    Label label3;
    Button OkayButton;
    Label RestaurantNameLabel;
    Label AveragePriceLabel;
    Label MealTimeLabel;
    //}}

    public void clickedOkayButton() {
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
    }
}




package ec.examples.lm2000;
/*
    This class is a basic extension of the Dialog class.  It can be used
    by subclasses of Frame.  To use it, create a reference to the class,
    then instantiate an object of the class (pass 'this' in the constructor),
    and call the show() method.

    example:

    RestaurantAddDialog theRestaurantAddDialog;
    theRestaurantAddDialog = new RestaurantAddDialog(this);
    theRestaurantAddDialog.show();

    You can add controls to AboutBox with Cafe Studio.
    (Menus can be added only to subclasses of Frame.)
 */

import java.awt.*;

public class RestaurantAddDialog extends Dialog {

    lm2000 state;
    public void Init(lm2000 l)
    {
        // set up
        state = l;
    }

    public RestaurantAddDialog(Frame parent) {

	    super(parent, "Add Restaurant", true);

	    //{{INIT_CONTROLS
        setLayout(null);
        addNotify();
        resize(insets().left + insets().right + 325, insets().top + insets().bottom + 182);
        label1=new Label("Restaurant");
        add(label1);
        label1.reshape(insets().left + 14,insets().top + 15,119,22);
        label2=new Label("Price");
        add(label2);
        label2.reshape(insets().left + 14,insets().top + 60,91,22);
        label3=new Label("Minutes per Meal");
        add(label3);
        label3.reshape(insets().left + 14,insets().top + 105,126,30);
        RestaurantNameTextField=new TextField(16);
        add(RestaurantNameTextField);
        RestaurantNameTextField.reshape(insets().left + 140,insets().top + 7,168,30);
        AveragePriceTextField=new TextField(9);
        add(AveragePriceTextField);
        AveragePriceTextField.reshape(insets().left + 140,insets().top + 52,98,30);
        MealTimeTextField=new TextField(5);
        add(MealTimeTextField);
        MealTimeTextField.reshape(insets().left + 161,insets().top + 97,56,31);
        OkayButton=new Button("OK");
        add(OkayButton);
        OkayButton.reshape(insets().left + 252,insets().top + 112,49,30);
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
    Label label1;
    Label label2;
    Label label3;
    TextField RestaurantNameTextField;
    TextField AveragePriceTextField;
    TextField MealTimeTextField;
    Button OkayButton;
    //}}
    public void clickedOkayButton() {
        String restaurantName = RestaurantNameTextField.getText();
        Double d = Double.valueOf(AveragePriceTextField.getText());
        double restaurantCost = d.doubleValue();
        Integer i = Integer.valueOf(MealTimeTextField.getText());
        int restaurantMinutes = i.intValue();
        Restaurant r = new Restaurant(restaurantName,
            restaurantCost, restaurantMinutes);
        state.server<-processAddRestaurant(r);
        handleEvent(new Event(this, Event.WINDOW_DESTROY, null));
    }
}



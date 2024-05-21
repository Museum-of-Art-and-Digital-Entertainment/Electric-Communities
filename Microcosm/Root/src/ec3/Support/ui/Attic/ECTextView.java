package ec.ui;

import netscape.application.KeyEvent;
import netscape.application.TextView;

public class ECTextView extends TextView
{
	public ECTextView() {
		super();
	}
	
	public ECTextView (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void keyUp (KeyEvent event) {
		if ((event.modifiers == 0) && (event.key == '.'))
		{
			keyDown(event);
		}
		super.keyUp(event);
	}
}

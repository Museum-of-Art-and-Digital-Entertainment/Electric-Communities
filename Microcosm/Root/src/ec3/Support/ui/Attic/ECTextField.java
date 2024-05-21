package ec.ui;

import netscape.application.KeyEvent;
import netscape.application.TextField;

public class ECTextField extends TextField
{
	public ECTextField() {
		super();
	}
	
	public ECTextField (int x, int y, int width, int height) {
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


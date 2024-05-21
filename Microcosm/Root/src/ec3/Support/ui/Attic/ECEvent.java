
package ec.ui;

import netscape.application.*;
import netscape.util.*;

public class ECEvent extends Event {

	public ECEvent (int type) {
		super();
		setType(type);
	}

	public int getType () {
		return type();
	}
}


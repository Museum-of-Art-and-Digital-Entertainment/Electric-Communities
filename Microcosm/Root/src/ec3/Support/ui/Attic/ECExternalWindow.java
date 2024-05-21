
package ec.ui;

import netscape.application.*;
import netscape.util.*;

public class ECExternalWindow extends ExternalWindow
{
	//
	// Constructors
	//
	
	public ECExternalWindow() {
		super();
		FoundationPanel myPanel = panel();
		myPanel.setRootView(new ECRootView());
	}
	
	public ECExternalWindow(int type) {
		super(type);
		FoundationPanel myPanel = panel();
		myPanel.setRootView(new ECRootView());
	}
	
	
	//
	// Convenience public API to set/remove tips
	//
	
	public void setTipForView(String tip, View view) {
		((ECRootView)this.rootView()).setTipForView(tip, view);
	}
	
	public void setTipForView(ECTipViewOwner owner, View view) {
		((ECRootView)this.rootView()).setTipForView(owner, view);
	}
	
	public void removeTipForView(View view) {
		((ECRootView)this.rootView()).removeTipForView(view);
	}
}


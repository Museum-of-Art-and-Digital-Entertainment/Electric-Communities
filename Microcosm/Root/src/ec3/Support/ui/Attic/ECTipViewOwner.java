
package ec.ui;
import netscape.application.View;

public interface ECTipViewOwner
{
	// Position is in view's relative coordinate system
	String getTipForPositionInView (View view, int x, int y);
}


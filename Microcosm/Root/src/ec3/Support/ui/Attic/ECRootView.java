
package ec.ui;

import netscape.application.*;
import netscape.util.*;

public class ECRootView extends RootView
{
	private int lastx;
	private int lasty;
	private Timer timer;
	private boolean mouseDown = false;
	private boolean showingTip = false;
	private Hashtable tips; 
	private TextField tipField;
	private Object lastTipElement = null;
	private View lastView = null;
	private View poppedUpView = null;
	private static Vector commands = new Vector();
	
	private static final String ShowTip = "ShowTip";
	private static final String HideTip = "HideTip";
	
	private static int ShowTipDelay = 1000;
	private static int HideTipDelay = 3400;
	
	private static int TipFieldHeight = 16;
	
	static {
        commands.addElement(ShowTip);
        commands.addElement(HideTip);
	}
	
    public ECRootView()
    {
    	super();
		setupRootView();
    }

	/**
	 * Pins width and height to those of minSize(), unless minSize is null. This allows
	 * a workaround for subview resizing bugs that occur after subviews have been sized
	 * to negative widths or heights. This would not be necessary if ExternalWindow 
	 * respected its own minSize, but a comment in the IFC documentation claims that
	 * this is a bug in AWT.
	 */
	public void setBounds(int x, int y, int width, int height)  {
		Size minSize = minSize();

		if (minSize != null)  {
			if (minSize.width > width)  {
				width = minSize.width;
				System.out.println("root view width pinned to minSize.width (" + width + ")");			
			}

			if (minSize.height > height)  {
				height = minSize.height;
				System.out.println("root view height pinned to minSize.height (" + height + ")");
			}
		}

		super.setBounds(x, y, width, height);
	}

	private void setupRootView() {
		timer = new Timer(this, ShowTip, ShowTipDelay);
		timer.setRepeats(false);		
		tips = new Hashtable();		
    }
    
    public void processEvent (Event event)
	{
		super.processEvent(event);
		
		if (showingTip) {
			hideTip(true);
		}

		timer.stop();
		
		if (!(event instanceof MouseEvent)) return;
		MouseEvent mouseEvent = (MouseEvent)event;
		if ((lastx == mouseEvent.x) && (lasty == mouseEvent.y)) return;

		int type = mouseEvent.type();
		if (type == MouseEvent.MOUSE_DOWN) {
			mouseDown = true;
		}
		else if (type == MouseEvent.MOUSE_UP) {
			mouseDown = false;
		}
		if (mouseDown) return;
		
		lastx = mouseEvent.x;
		lasty = mouseEvent.y;

		lastView = viewForMouse(lastx, lasty);
		if (lastView == null) return;
		if (lastView == poppedUpView) return;
		poppedUpView = null;
		lastTipElement = tips.get(lastView);
		if (lastTipElement == null) return;
		timer.setCommand(ShowTip);
		timer.setInitialDelay(ShowTipDelay);
		timer.start();
	}

    public void performCommand (String string, Object object)
    {
        if (ShowTip.equals(string))
        {
            showTip();
        }
        else if (HideTip.equals(string))
        {
            hideTip(false);
        }
        else {
        	super.performCommand(string, object);
        }
    }

    public boolean canPerformCommand (String string)
    {
        return (commands.contains(string) || super.canPerformCommand(string));
    }

	public void setTipForView(String tip, View view) {
		tips.put(view, tip);
	}
	
	public void setTipForView(ECTipViewOwner owner, View view) {
		tips.put(view, owner);
	}
	
	public void removeTipForView(View view) {
		tips.remove(view);
	}
	
	private void showTip () {
		if (tipField == null) {
			tipField = new TextField(0, 0, 100, TipFieldHeight);
			tipField.setEditable(false);
			tipField.setBorder(LineBorder.blackLine());
			tipField.setJustification(Graphics.CENTERED);
			tipField.setBackgroundColor(Color.white);
		}
		int x = lastx;
		int y = lasty;
		String tipString = null;
		timer.stop();
		if (lastTipElement == null) return;
		if (lastTipElement instanceof ECTipViewOwner) {
			Point p = convertToView(lastView, x, y);
			ECTipViewOwner owner = (ECTipViewOwner)lastTipElement;
			tipString = owner.getTipForPositionInView(lastView, p.x, p.y);
			if (tipString == null) return;
		}
		else {
			tipString = (String)lastTipElement;
		}
		////this.disableDrawing();
		tipField.setStringValue(tipString);
		tipField.sizeToMinSize();
		tipField.sizeBy(6, 0);
		if (y > TipFieldHeight)
			y -= TipFieldHeight;
		int tipWidth = tipField.width();
		int rootWidth = this.width();
		if ((tipWidth < rootWidth) && ((x + tipWidth) > rootWidth))
			x = rootWidth - tipWidth;
		tipField.moveTo(x, y);
		this.addSubview(tipField);
		////this.reenableDrawing();
		this.draw();
		timer.setCommand(HideTip);
		timer.setInitialDelay(HideTipDelay);
		timer.start();
		showingTip = true;
	}
	
	private void hideTip (boolean displayAgain) {
		if ((tipField == null) || (showingTip == false)) return;
		if ((displayAgain) || (lastTipElement instanceof ECTipViewOwner)) {
			poppedUpView = null;
		}
		else {
			poppedUpView = lastView;
		}
		this.removeSubview(tipField);
		this.draw();
		showingTip = false;
	}
}

// Web.java

package ec.pl.examples.web;

import netscape.application.*;
import netscape.util.*;

import ec.e.cap.*;
import ec.ifc.app.*;

public class IFCWebFactory extends WebApplication implements WebFactory
{
	private static EEnvironment env = null;
	private static WebApplication application;
	
	public IFCWebFactory () {
		if (application != null) return; // XXX Squawk!
		application = this;
	}
	
	// This is required since the Factory is made via Class.newInstance();
	// And hence we couldn't pass anything into the constructor
	public void setEnvironment (EEnvironment theEnv) {
		if (env != null) return; // Squawk!
		env = theEnv;
	}	

	public WebController getWebController() {
		return new IFCWebController(env, application);
	}
}

public class IFCWebController implements Target, EventProcessor, WebController, TextViewOwner, TextFilter, ECTipViewOwner {
	WebPeer peer = null;
	EEnvironment env;
	WebApplication application;
	ECExternalWindow webWindow;
	String linkRoot;
	ECTextField webTextField;
	TextField statusField;
	MyTextView webTextView;
	Range currentRange;
	boolean linkSelected;
	
	//
	// ECTipViewOwner methods
	//
	public String getTipForPositionInView(View view, int x, int y) {
		if (view != webTextView) {
			System.out.println("Asked for tip, but not in WebTextView!");
			return null;
		}
		String url = webTextView.getUrlAtCoordinates(x, y);
		////if (url == null) return "If you lived here you'd be home by now";
		if (url == null) return null;
		else return absolutifyLink(url);
	}
		
    //
    // WebController methods
    //
	public void postEvent (int eventType, boolean state) {
		WebEvent webEvent = new WebEvent(eventType, state);
		setupEvent(webEvent);
	}

	public void postStatus (String status) {
		WebEvent webEvent = new WebEvent(WebController.EVENT_STATUS, status);
		setupEvent(webEvent);
	}

	public void postLink (String link) {
		link = absolutifyLink(link);
		WebEvent webEvent = new WebEvent(WebController.EVENT_LINK, link);
		setupEvent(webEvent);
	}
	
	public void postSelection (int start, int end) {
		WebEvent webEvent = new WebEvent(WebController.EVENT_SELECTION, new Range(start, end));
		setupEvent(webEvent);
	}

	//
	// TextFilter methods
	//
	public boolean acceptsEvent(Object textObject, KeyEvent event, Vector events) {
		// XXX (GJF) Windows doesn't treat '.' character correctly, this never called!
		return true;
	}
	
	//
	// TextViewOwner methods
	//


    public void textEditingDidBegin(TextView textView) {

	}



    public void textEditingDidEnd(TextView textView) {

	}



    public void textWillChange(TextView textView, Range aRange) {

	}



    public void textDidChange(TextView textView, Range aRange) {

	}



    public void attributesWillChange(TextView textView, Range aRange) {

	}



    public void attributesDidChange(TextView textView,Range aRange) {

	}



	public void selectionDidChange (TextView textView) {
	}
	
	public void linkWasSelected (TextView sender, Range linkRange, String stringURL) {
		System.out.println("Link selected " + stringURL);
		linkSelected = true;
		String link = absolutifyLink(stringURL);
		if (peer != null) peer <- webLink(link);
	}
	

	void gotMouseLink(TextView sender, String url) {
		String string;
		if (url == null) {
			string = "";
		}
		else {
			string = absolutifyLink(url);
		}
		statusField.setStringValue(string);
	}
	
	void gotMouseUp (TextView textView) {
		boolean saveSelected = linkSelected;
		linkSelected = false;
		Range range = textView.selectedRange();
		if (saveSelected) return;
		currentRange = range;
		////System.out.println("Telling peer about new range: " + range);
		if (peer != null) 
			peer <- webSelection(WebApplication.getIndexForRange(range), WebApplication.getLengthForRange(range));
	}
	
	private void showSelection (TextView view, Range range) {
		////System.out.println("Showing selection: " + range);
		view.selectRange(range);
		view.scrollRangeToVisible(range);
	}
	
	private void setLinkRoot (String link) {
		int lastIndex = link.lastIndexOf('/');
		linkRoot = link.substring(0, lastIndex + 1);
		////System.out.println("Set root to " + linkRoot);
	}
	
	private String absolutifyLink (String link) {
		String string;
		int index;
		int slashIndex;
		int dotIndex;
		
		////System.out.println("AbsolutifyLink called with " + link);
		index = link.indexOf("://");
		if (index != -1) {
			string = link;
			////System.out.println("Absolute link, will link to " + string);
		}
		else {
			if (link.charAt(0) == '/') {
				index = linkRoot.indexOf("://");
				slashIndex = linkRoot.indexOf('/', index + 3);
				string = linkRoot.substring(0, slashIndex) + link;
			}
			else {
				string = linkRoot + link;
			}
			////System.out.println("Relative link, will link to " + string);
		}
		
		index = string.indexOf("://");
		slashIndex = string.lastIndexOf('/');
		if (slashIndex != (string.length() - 1)) {
			if ((slashIndex != -1) && (slashIndex != (index + 2))) {
				dotIndex = string.indexOf('.', slashIndex);
				if (dotIndex == -1) {
					////System.out.println("No dot after last slash, adding ending slash");
					string = string + "/";
				}
				else {
					////System.out.println("Dot after last slash, leaving alone");
				}
			}
			else {
				////System.out.println("No slash after ://, adding ending slash");
				string = string + "/";
			}
		}
		else {
			////System.out.println("Slash is last, leaving alone");
		}
		return string;
	}
	
	private void goToLink (TextView textView, Range linkRange, String link) {		
		////System.out.println("GoToLink called with link " + link);
		setLinkRoot(link);
		webTextField.setStringValue(link);
		webWindow.setTitle(link);
		////WebThread thread = new WebThread(textView, link);
		////thread.setDaemon(true);
		////thread.start();
		textView.importHTMLFromURLString(link);
	}
	
	public void setPeer (WebPeer peer, String link, int start, int end) {
		Object array[] = new Object[4];
		array[0] = peer;
		array[1] = link;
		array[2] = new Integer(start);
		array[3] = new Integer(end);
		WebEvent webEvent = new WebEvent(WebController.EVENT_PEER, array);
		setupEvent(webEvent);		
	}
	
	private void setupPeer (WebPeer peer, String link, int start, int end) {
		System.out.println("SetPeer entered");
		this.peer = peer;

		Button button;
		Size size;

		currentRange = new Range(start, end);
		
		webWindow = new ECExternalWindow();
		ECRootView rv = (ECRootView)webWindow.rootView();
		rv.setColor(Color.lightGray);
		rv.setBuffered(true);
        size = webWindow.windowSizeForContentSize(606, 430);
		webWindow.setBounds(360, 10, size.width, size.height);

		ContainerView cv = new ContainerView(0, 0, 606, 430);
		cv.setBackgroundColor(Color.lightGray);
		Border border = ECWindowBorder.border();
		cv.setBorder(border);
		cv.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		cv.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
		rv.addSubview(cv);
	
		webTextField = new ECTextField(6, 10, 595, 20);
		webTextField.setEditable(true);
		webTextField.setTarget(this);
		webTextField.setCommand("URL");
		webTextField.setFilter(this);
		webTextField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		webTextField.setVertResizeInstruction(View.BOTTOM_MARGIN_CAN_CHANGE);
		rv.addSubview(webTextField);
							
		statusField = new TextField(5, 405, 560, 20);
		statusField.setEditable(false);
		statusField.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		statusField.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
		statusField.setBorder(ECBevelBorder.border());
		statusField.setBackgroundColor(Color.lightGray);
		rv.addSubview(statusField);
		webWindow.setTipForView("Status field denoting URL cursor is positioned over", statusField);

		Button quitButton = new Button(570, 405, 31, 20);
		quitButton.setTitle("Exit");
		quitButton.setTarget(this);
		quitButton.setCommand("quit");
		quitButton.setHorizResizeInstruction(View.LEFT_MARGIN_CAN_CHANGE);
		quitButton.setVertResizeInstruction(View.TOP_MARGIN_CAN_CHANGE);
		rv.addSubview(quitButton);
						
		webTextView = new MyTextView(0, 0, 592, 362, this);
		webTextView.setEditable(false);
		webTextView.setOwner(this);
		webTextView.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		webTextView.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
		
		ScrollGroup scrollGroup = new ScrollGroup(5, 40, 596, 362);
		scrollGroup.setContentView(webTextView);
		scrollGroup.setHasHorizScrollBar(true);
		scrollGroup.setHasVertScrollBar(true);
		scrollGroup.setHorizScrollBarDisplay(ScrollGroup.AS_NEEDED_DISPLAY);
		scrollGroup.setVertScrollBarDisplay(ScrollGroup.AS_NEEDED_DISPLAY);
		scrollGroup.setBorder(new BezelBorder(BezelBorder.GROOVED, Color.lightGray));
		scrollGroup.setBackgroundColor(Color.white);
		scrollGroup.setHorizResizeInstruction(View.WIDTH_CAN_CHANGE);
		scrollGroup.setVertResizeInstruction(View.HEIGHT_CAN_CHANGE);
		rv.addSubview(scrollGroup);
		webWindow.setTipForView(this, webTextView);
		
		webWindow.show();
		
		postLink(link);
		postSelection(start, end);
	}

    //
    // EventProcessor methods
    //
	public void processEvent (Event event) {
		WebEvent webEvent = (WebEvent)event;
		switch (webEvent.getType()) {
			case WebController.EVENT_LINK:
				goToLink(webTextView, null, (String)webEvent.getData());
			    break;			    
			case WebController.EVENT_SELECTION:
				showSelection(webTextView, (Range)webEvent.getData());
			    break;			
			case WebController.EVENT_PEER:
			{
				Object array[] = (Object[]) webEvent.getData();
				WebPeer peer = (WebPeer)array[0];
				String link = (String)array[1];
				int start = ((Integer)array[2]).intValue();
				int end = ((Integer)array[3]).intValue();
				setupPeer(peer, link, start, end);
			}
			break;			    
			default:
				System.out.println("Warning, unknown WebEvent: " + webEvent.getType());
		}
	}

    private void setStatus(String status) {
            System.out.println(status);
    }

    //
    // Private utility methods
    //
    private void setupEvent(WebEvent webEvent) {
		webEvent.setProcessor(this);
		application.getEventLoop().addEvent(webEvent);
    }

    //
    // Target methods
    //
    public void performCommand(String command, Object arg) {
		if (command.equals("quit")) {
			System.exit(0);
		}
		else if (command.equals("URL")) {
			String link = webTextField.stringValue();
			if (link.indexOf("://") == -1) {
				if (link.indexOf('.') == -1) {
					link = "www." + link + ".com";
				}
				link = "http://" + link;
			}
			link = absolutifyLink(link);
		    if (peer != null) peer <- webLink(link);
		}
    }

	IFCWebController (EEnvironment env, WebApplication application) {
		this.env = env;
		this.application = application;
	}
}

class WebThread extends Thread {
	TextView textView;
	String link;
	
	private WebThread() {
	}
	
	WebThread (TextView textView, String link) {
		this.textView = textView;
		this.link = link;
	}
	
	public void run() {
		textView.importHTMLFromURLString(link);
	}
}


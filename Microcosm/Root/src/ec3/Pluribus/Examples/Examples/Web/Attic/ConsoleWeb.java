package ec.pl.examples.web;
import ec.e.io.*;
import ec.e.cap.*;

public class ConsoleWebFactory implements WebFactory
{
	public WebController getWebController () {
		return new ConsoleWebController();
	}

	public void setEnvironment (EEnvironment env) {
	}

	public void run () {
	}
}

public eclass ConsoleWebController implements WebController, EInputHandler
{
    WebPeer peer;

	emethod handleInput (String line) {
		if (line != null && (line.indexOf("://") != -1)) {
		    if (peer != null) peer <- webLink(line);
		}
	}



	local void postEvent (int eventType, boolean state) {
	}



	local void postStatus (String status) {
        System.out.println(status);
	}



	local void postLink (String link) {
        System.out.println("Now linked to " + link);
	}



	local void postSelection (int start, int end) {
        System.out.println("Selection is now " + start + ":" + end);
	}



	local void setPeer (WebPeer peer, String link, int start, int end) {
        RtConsole.setupConsoleReader(this, System.in);
		this.peer = peer;
		postLink(link);

	}

}

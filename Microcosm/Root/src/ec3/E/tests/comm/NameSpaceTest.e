package ec.tests.comm;

import ec.e.start.ELaunchable;
import ec.e.cap.EEnvironment;
import ec.e.net.EPublisher;
import ec.e.net.ERegistrar;
import ec.e.net.EPublication;

public class NameSpaceTest implements ELaunchable {
	public void go(EEnvironment env) {
		NSTest ns = new NSTest();
		ns <- go(env);
	}
}

eclass NSTest {
	EPublisher pub;
	ERegistrar reg;
	NSTest result;
	EPublication ferdpub;
	String url;

	emethod go(EEnvironment env) {
		try {
			pub = (EPublisher)env.get("publisher.root");
			if (env.getPropertyAsBoolean("listen")) {

				reg = (ERegistrar)env.get("registrar.root");
				reg.startup(0);

				ferdpub = pub.publish("ferd", this);
				System.out.println("registered url=" + ferdpub.getURL());

				return;
			}
			url = (String)env.getProperty("url");
			pub.lookupURL(url, &result);
			result <- hello();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	emethod hello() {
		System.out.println("hi there.");
		try {
			reg.shutdown();
		} catch (Throwable t) {
			System.out.println("can't shutdown: " + t);
		}
	}
}

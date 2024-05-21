package ec.tests.comm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import ec.e.comm.ELaunchable;
import ec.e.comm.RtLauncher;
import ec.e.comm.RtNetConnectionFactory;
import ec.e.comm.RtEEnvironment;
import ec.e.comm.RtNet;
import ec.e.comm.RtNetAddr;
import ec.e.comm.RtNetListener;
import ec.e.comm.RtRestrictedException;

public class NetCapabilityTest {
	public static void main(String args[]) {
		RtLauncher.launch(new NetCapTest(), args);
	}
}

eclass NetCapTest implements ELaunchable {
	RtNet net;
	RtNetListener listener;

	emethod go(RtEEnvironment env) {
		net = (RtNet)env.get("net.root");
		try {
			listener = net.listen(6789, new NetCapTestFactory(net, null));
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("listening...");
	}
}

public class NetCapTestFactory implements RtNetConnectionFactory {
	RtNet net;
	NetCapTestConnection other;

	public NetCapTestFactory(RtNet network, NetCapTestConnection otherconnection) {
		net = network;
		other = otherconnection;
	}

	public Object manufacture(Socket sock, RtNetAddr localAddr, RtNetAddr remoteAddr, boolean incoming) throws RtRestrictedException, IOException {
		NetCapTestConnection conn = new NetCapTestConnection(sock, incoming, net, other);
		conn.start();
		return (Object)conn;
	}
}

public class NetCapTestConnection extends Thread {
	Socket socket;
	BufferedInputStream inputStream;
	BufferedOutputStream outputStream;
	PrintStream printStream;
	boolean incoming;
	RtNet net;
	NetCapTestConnection other;

	public NetCapTestConnection(Socket sock, boolean in, RtNet network, NetCapTestConnection otherconnection) throws IOException {
		socket = sock;
		inputStream = new BufferedInputStream(sock.getInputStream());
		outputStream = new BufferedOutputStream(sock.getOutputStream());
		printStream = new PrintStream(outputStream);
		incoming = in;
		net = network;
		other = otherconnection;
	}

	public void run() {
		try {
			if (incoming) {
				NetCapTestFactory factory = new NetCapTestFactory(net, this);

				printStream.println("you've gotten here");
				outputStream.flush();
				try {
					other = (NetCapTestConnection)net.connect("localhost:25", factory);
					loopback();
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
				printStream.println("that's all folks");
				outputStream.flush();
				socket.close();
			}
			else {
				loopback();
				socket.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loopback() throws IOException {
		int c;

		while ((c = inputStream.read()) >= 0) {
			other.write(c);
		}
	}

	public void write(int c) throws IOException {
		outputStream.write(c);
		outputStream.flush();
	}
}

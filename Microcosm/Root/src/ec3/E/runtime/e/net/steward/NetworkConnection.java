package ec.e.net.steward;

import ec.util.NestedException;
import java.io.OutputStream;

public interface NetworkConnection {
    // incomingEnable returns our sender
    public NetworkSender incomingSetup(NetworkConnection inner, OutputStream innerReceiver);
    public void outgoingSetup(NetworkSender outerSender, String localAddr);
    public void noticeProblem(Throwable t);
    public void noticeShutdown(byte[] sendIV, byte[] receiveIV);
    public OutputStream getReceiver();
    public void updateSendCounts(int messageLength, int compressedLength);
    public void updateReceivedCounts(int messageLength, int compressedLength);
}


public class NetworkConnectionError extends NestedException
{
    public NetworkConnectionError(String msg) {
        super(msg);
    }

    public NetworkConnectionError(String msg, Throwable t) {
        super(msg, t);
    }
}

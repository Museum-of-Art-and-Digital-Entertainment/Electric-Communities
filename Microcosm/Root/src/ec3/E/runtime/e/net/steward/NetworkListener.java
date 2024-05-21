package ec.e.net.steward;

/**
   One of the elements of the Listener chain.
  */
public interface NetworkListener {
//  public NetworkListener(String listenAtAddr, NetworkListener innerListener);
    public void listening(String listenAddr);
    public void noticeConnection(NetworkConnection outer, String localAddr, String remoteAddr);
    public void noticeProblem(Throwable t, boolean listenProblem);
    public void shutdown();
    public void suspend();
    public void resume();
}


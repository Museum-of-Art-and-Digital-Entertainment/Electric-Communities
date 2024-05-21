package ec.e.run;

import ec.util.EThreadGroup;

import java.util.Vector;
import ec.e.net.ProxyDeathHandler;

interface EChannelHelperInterface {
    void setRecipients(eRecipient recipients);
    void notifyClients(EObject target);
    void requestNotificationFromHost(EChannelHelper host,
                                     EDistributor distributor);
    boolean isDistributorGone();
    void finalizeDistributor();
}

eclass EChannelHelper implements EChannelHelperInterface, ProxyDeathHandler
{
    boolean distributorGone = false; // Package scoped for Channel to see
    private Vector myData = null;
    private eRecipient myRecipients = null;
    private eParty myParty = null;
    
    public EChannelHelper(eParty party) {
        myParty = party;
    }
    
    local void setRecipients(eRecipient recipients) {
        ////System.out.println("Channel Helper setting recipients to " + recipients);
        myRecipients = recipients;
    }
    
    local synchronized void notifyClients(EObject target) {
        // XXX - Invariant that this shouldn't be called if data == this
        if (myData == null || distributorGone)
            return;
        Vector clients = myData;
        int size = clients.size();
        //System.out.println("NotifyClients called, " + size + " clients");
        for (int i = 0; i < size; /* i++ done in loop */) {
            EDistributor client = (EDistributor)clients.elementAt(i);
            try {
                //System.out.println("NotifyClients: Forwarding " + client +
                //    " to " + target);
                client <- forward(target);
                i++; // only if exception doesn't occur
            } catch (Exception e) {
                clients.removeElementAt(i);
                size--;
            }
        }
    }
    
    local void requestNotificationFromHost(EChannelHelper host,
                                           EDistributor distributor) {
        try {
            host <- requestNotification(distributor);
        } catch (Exception e) {
            /* XXX bad exception handling */
            System.err.println("ChannelHelper: Exception requesting notification from host");
            EThreadGroup.reportException(e);
        }
    }
    
    private synchronized void handleRequestNotification (EDistributor client) {
        synchronized(myParty) {
            if (myRecipients != null) {
                //System.out.println("HandleRequestNotification, recipients not null");
                eRecipient rec = myRecipients;
                if (rec != null) {
                    while (rec.next != null) {
                        try {
                            client <- forward((EObject)rec.recipient);
                        } catch (Exception e) {
                            /* XXX bad exception handling -- fix */
                            System.err.println("ChannelHelper: Exception sending forward notification to client");
                            EThreadGroup.reportException(e);
                        }
                        rec = rec.next;
                    }
                }
            } else {
                //System.out.println("HandleRequestNotification, no recipients");
            }
        }
        
        Vector clients;
        //System.out.println("HandleRequestNotification, adding client " + client);
        if (myData == null)
            myData = clients = new Vector(4);
        else if (distributorGone)
            return;
        else
            clients = myData;
        clients.addElement(client);
        if (client instanceof EProxyConnection) {
            // XXX - Should never not be, raise InvariantViolation if not
            // GC issues since this thing never gets cleaned up since interested in connection!
            //XXX register interest in proxy here
        } else {
            System.err.println("ChannelHelper handleRequestNotification: Client is not a Proxy: " + client);        
        }
    }
    
    emethod requestNotification (EDistributor client) {
        handleRequestNotification (client);
    }
    
    local synchronized boolean isDistributorGone () {
        return distributorGone;
    }
    
    local synchronized void finalizeDistributor () {
        // Assumes caller holds Vat Lock so no forwarding can occur during this
        ////System.out.println("Finalizing Distributor, myRecipients is " + myRecipients);
        if (myRecipients != null)  {
            ////System.out.println("Finalizing Distributor, myRecipients.next is " + myRecipients.next);
            if (myRecipients.next.recipient == null) {
                ////System.out.println("Finalizing Distributor, only one recipient which is " + myRecipients.recipient);
                if (myRecipients.recipient instanceof EChannel_$_Impl) {
                    ////System.out.println("EChannelHelper: Finalizing Distributor and recipient is a Channel");
                    EChannel_$_Impl channel = (EChannel_$_Impl)myRecipients.recipient;
                    if (channel.myRecipients != null) {
                        myRecipients = channel.myRecipients;
                    }
                }
            }
        }
        distributorGone = true;
    }
    
    local synchronized void noteProxyDeath(Object client, Object ignored) {
        Object lock = RtRun.getTheVatLock();
        synchronized(lock) {
            if (myData == null || distributorGone)
                return;
            Vector clients = myData;
            int i;
            int size = clients.size();
            for (i = 0; i < size; /* i++ done in loop */) {
                Object element = clients.elementAt(i);   
                if (client == element) {
                    clients.removeElementAt(i);
                    size--;
                } else {
                    i++;
                }
            }
        }
    }
}

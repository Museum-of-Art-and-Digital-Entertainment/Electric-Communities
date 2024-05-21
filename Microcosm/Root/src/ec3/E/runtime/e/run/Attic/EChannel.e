package ec.e.run;

eclass EChannel
{
    eRecipient myRecipients;
    eParty myParty;
    private EDistributor myDistributor = null;

    public void invokeNow(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        if (myParty == null)  {
            // Bit bucket for messages sent to channel without Distributor!
            return; 
        }

        eRecipient sendTo = myRecipients;
        // Skip last Recipient, since it is a dummy placeholder
        // for next Recipient to be added, avoiding forcing the
        // Distributor to hold onto last Recipient which will
        // end up being a GC leak if Channel goes away.
        if (sendTo != null) {
            while (sendTo.next != null)
            {
                // keeps badly implemented tethers from inadvertantly
                // using a previous exception env
                RtRun.CurrentExceptionEnv = null;
                
                // to maintain POE, we use invokeNow() instead of
                // invoke().
                sendTo.recipient.invokeNow(seal, args, ee);
                sendTo = sendTo.next;
            }
        }
        // XXX - Need to distribute messages to other channels
        // not for them to send, but to track. If we go away,
        // however, they will need to start sending them.
        
        // If our Distributor is still around
        // then we need to save messages so they
        // can be sent on to any Object we're forwarded to.
        // RobJ pspread: Gordie says this should always be called
        myParty.addMessage(seal, RtEnvelope.cloneArgs(args), ee);

        // XXX - Possible optimization - if only one recipient which
        // is channel and it has at least one recipient, we can short
        // circuit and set our recipients to our recipient's
        // recipients. Might be too expensive to check on every
        // message send.
    }

    public EDistributor distributor() {
        EDistributor result = myDistributor;
        myDistributor = null;
        return result;
    }

    public EChannel() {
        this(false);
    }

    public EChannel(boolean noDistributor) {
        if (noDistributor) {
            myParty = null;
            myDistributor = null;
        } else {
            myParty = new eParty(this);
            myDistributor = new EDistributor(myParty);
        }
    }

    void setRecipients(eRecipient recipient) {
        if (myRecipients == null) {
            myRecipients = recipient;
        }
    }
}

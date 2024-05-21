package ec.e.run;

class eParty
{
    static private final int INITIAL_TARGETS_SIZE = 10;
    static private final int INITIAL_ENVELOPES_SIZE = 10;

    /*package*/ boolean myChannelIsAlive;
    /*package*/ RtTether[] myExtraTargets;
    /*package*/ int myExtraTargetsSize;

    /*package*/ boolean myDistributorIsAlive;
    /*package*/ RtEnvelope[] myExtraEnvelopes;
    /*package*/ int myExtraEnvelopesSize;

    eParty() {
        myChannelIsAlive = true;
        myExtraTargets = new RtTether[INITIAL_TARGETS_SIZE];
        myExtraTargetsSize = 0;

        myDistributorIsAlive = true;
        myExtraEnvelopes = new RtEnvelope[INITIAL_ENVELOPES_SIZE];
        myExtraEnvelopesSize = 0;
    }

    void distributorGotCollected() {
        myDistributorIsAlive = false;
        myExtraEnvelopes = null;
        myExtraEnvelopesSize = 0;
    }

    void channelGotCollected() {
        myChannelIsAlive = false;
        myExtraTargets = null;
        myExtraTargetsSize = 0;
    }

    void addTarget(RtTether targ) {
        if (! myChannelIsAlive) {
            return;
        }

        if (myExtraTargetsSize == myExtraTargets.length) {
            // must grow our array
            int newsz = myExtraTargetsSize * 3 / 2;
            RtTether[] newt = new RtTether[newsz];
            System.arraycopy(myExtraTargets, 0, newt, 0, myExtraTargetsSize);
            myExtraTargets = newt;
        }

        myExtraTargets[myExtraTargetsSize] = targ;
        myExtraTargetsSize++;
    }

    void addMessage(RtSealer seal, Object[] args, RtExceptionEnv ee) {
        if (! myDistributorIsAlive) {
            return;
        }

        if (myExtraEnvelopesSize == myExtraEnvelopes.length) {
            // must grow our array
            int newsz = myExtraEnvelopesSize * 3 / 2;
            RtEnvelope[] newe = new RtEnvelope[newsz];
            System.arraycopy(myExtraEnvelopes, 0, newe, 0, 
                myExtraEnvelopesSize);
            myExtraEnvelopes = newe;
        }

        myExtraEnvelopes[myExtraEnvelopesSize] = 
            new RtEnvelope(seal, args, ee);
        myExtraEnvelopesSize++;
    }
}

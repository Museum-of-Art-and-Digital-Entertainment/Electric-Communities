package ec.e.net;

/**
  A simple class which represents traffic received by the message queue
  listening process.
*/

/*
  RtMsgQEntries represent messages we have received but not processed yet.
  Incoming messages which are not high priority go on a traffic queue, and
  incoming high priority requests are handled right away. If the processing
  of any entry results in the neccessity to retrieve information from the 
  other side of the connection (such as a class fault), a high priority
  request is sent to the other side, and a MsgQEntry is queued on a request
  queue, and a unique requestID is assigned to it.
  
  When a response is received, it indicates the requestID so we can remove
  the pending MsgEntry for the high priority request. No other traffic can be 
  processed until these are all clear.
*/
class RtMsgQEntry {
    private RtMsgQEntry myNext;
    private int myMsgCode;
    private long myMsgLength;
    private long myRequestID;
    private byte myMsg[];
    
    RtMsgQEntry(int msgCode, long msgLength, long requestID, byte msg[]) {
        myMsgCode = msgCode;
        myMsgLength = msgLength;
        myMsg = msg;
        myNext = null;
        myRequestID = requestID;
    }

    byte[] getMsg() {
        return myMsg;
    }

    int getMsgCode() {
        return myMsgCode;
    }
    
    long getMsgLength() {
        return myMsgLength;
    }

    RtMsgQEntry getNext() {
        return myNext;
    }

    void setNext(RtMsgQEntry next) {
        myNext = next;
    }

    long getRequestID() {
        return myRequestID;
    }
}

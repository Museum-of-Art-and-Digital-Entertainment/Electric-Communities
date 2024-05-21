package ec.samp.crew;

import ec.e.start.Tether;
import ec.e.util.crew.Queue;
import ec.e.util.crew.QueueReader;
import ec.e.util.crew.QueueWriter;

import ec.samp.steward.SampResultHandlerInt;

public class SampRequestItem {
    String                 myRequest;
    SampResultHandlerInt   myResultHandler;

    SampRequestItem(String request, SampResultHandlerInt resultHandler) {
        myRequest           = request;
        myResultHandler     = resultHandler;
    }
}


/**
 * SampCrewThread -- A thread that processes requests and does a 
 *                   doSamp() with the request.
 */
public class SampCrewThread implements Runnable {

    private QueueWriter myQWriter;
    private QueueReader myQReader;
    private Samp        mySamp;

    public SampCrewThread(Samp theSampToUse) { 
        mySamp = theSampToUse;
        Queue q = new Queue(new Object());
        myQReader = q.reader();
        myQWriter = q.writer();
    }

    public void run() {
        
        SampRequestItem request;
        
        try {
            while ((request = (SampRequestItem) myQReader.nextElement()) 
                                                                  != null) {
                mySamp.doCrewSamp(request);
            }
            
        } catch (Exception e) {
            System.out.println("got exception " + e.getMessage());
        }
    }

    /**
     * performSampRequest() -- queue up a request.  The thread will
     *                         dequeue it and process the request.
     */
    public void performSampRequest(String request, 
                                   SampResultHandlerInt resultHandler) {

        System.out.println("performing request... " + request);
        SampRequestItem sampRequest = new SampRequestItem(request, 
                                                          resultHandler);

        myQWriter.enqueue(sampRequest);
    }
}


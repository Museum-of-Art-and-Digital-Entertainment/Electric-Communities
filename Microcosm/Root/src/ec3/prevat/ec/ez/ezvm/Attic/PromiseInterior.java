package ec.ez.ezvm;
import ec.e.run.EZEnvelope;
import ec.e.run.*;

class PromiseInterior {
    eMessageEntry msgListHead = null;
    eMessageEntry msgListTail = null;

    Object target = null;
    Promise_$_Impl forPromise;
    String state = Promise_$_Impl.promise_pending;
    String reasonBroken = null;

    PromiseInterior(Promise_$_Impl thePromise) {
        forPromise = thePromise;
    }

    public String state() {
        return state;
    }

    public String reason() {
        return reasonBroken;
    }

    public void breakPromise(String reason) {
        state = Promise_$_Impl.promise_broken;
        reasonBroken = reason;
        target = null;  // null out the target
        msgListHead = null; // and discard pending messages
        msgListTail = null;
    }

    public void forwardTo(EObject_$_Intf theTarget) {
        eMessageEntry thisMsg;
        state = Promise_$_Impl.promise_kept;
        target = theTarget;

        // Deliver deferred messages.
        thisMsg = msgListHead;
        while (thisMsg != null) {
            RtRun.enqueue((EObject_$_Intf) target, thisMsg.envelope, thisMsg.exceptionEnv);
            thisMsg = thisMsg.next;
        }

        msgListHead = null; // cut loose the messages for possible GC.
        msgListTail = null;
    }

    public void acceptEnvelope(RtEnvelope envelope, RtExceptionEnv exceptionEnv) {
        if(target != null) {
            RtRun.enqueue((EObject_$_Intf) target, envelope, exceptionEnv);
            return;
        }

        eMessageEntry thisMsg = new eMessageEntry(envelope, exceptionEnv);

        // If its the first msg, put it up front.
        if(msgListHead == null) {
            msgListHead = thisMsg;
            msgListTail = thisMsg;
        } else { // Link it to the tail of the queue.
            msgListTail.next = thisMsg;
            msgListTail = thisMsg;
        }
    }
}

// XXX A very similar class to this one exists in myE.java
class eMessageEntry
{
    public RtEnvelope envelope;
    public RtExceptionEnv exceptionEnv;
    public eMessageEntry next;

    eMessageEntry (RtEnvelope in_$_envelope, RtExceptionEnv in_$_exceptionEnv)
    {
        envelope = in_$_envelope;
        exceptionEnv = in_$_exceptionEnv;
        next = null;
    }
}



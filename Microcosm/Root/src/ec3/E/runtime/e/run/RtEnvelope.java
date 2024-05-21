package ec.e.run;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import ec.util.EThreadGroup;

// ------------------------------------------------------------
// XXX JAY - removed final from encode/decode! Security problem????
// and removed private from IVs.
//
final public class RtEnvelope extends Object implements RtCodeable
{
    /*package*/ RtSealer mySealer;
    /*package*/ Object[] myArgs;
    /*package*/ RtExceptionEnv myEE; // may be null

    public RtEnvelope (RtEnvelope source)
    {
        mySealer = source.mySealer;
        myArgs = source.myArgs;
        myEE = source.myEE;
    }

    public RtEnvelope (RtSealer sealer, Object[] args, RtExceptionEnv ee) {
        mySealer = sealer;
        myArgs = args;
        myEE = ee;
    }

    public String classNameToEncode (RtEncoder encoder) {
        return null;
    }

// XXX JAY - removed final from encode/decode! Security problem????

    public  void encode (RtEncoder coder)
    {
        try
        {
            if (RtRun.tr.tracing)
                RtRun.tr.$ ("Envelope encoding self: " + this);
            coder.encodeObject(myArgs);
            coder.encodeObject(myEE);
            coder.encodeObject(mySealer);
        }
        catch (Exception e)
        {
            RtRun.tr.errorReportException(e, "Couldn't encode envelope!");
        }
    }

// XXX JAY - removed final from encode/decode! Security problem????

    public  Object decode(RtDecoder coder) {
        try {
            // NOTE: ugly coding order stuff to avoid flag day
            Object firstObj = coder.decodeObject();
            Object secondObj = coder.decodeObject();
            if (firstObj instanceof RtSealer) {
                mySealer = (RtSealer) firstObj;
                myArgs = (Object[]) secondObj;
                myEE = null;
            } else {
                myArgs = (Object[]) firstObj;
                myEE = (RtExceptionEnv) secondObj;
                mySealer = (RtSealer) coder.decodeObject();
            }
        } catch (Exception e) {
            RtRun.tr.errorReportException(e, "Couldn't decode envelope!");
            return null;
        }
        if (RtRun.tr.tracing)
            RtRun.tr.$("Envelope decoded self: " + this);
        return this;
    }

    public RtSealer getSealer() {
        return mySealer;
    }

    public Object[] cloneArgs() {
        if (myArgs != null) {
            Object[] result = new Object[myArgs.length];
            System.arraycopy(myArgs, 0, result, 0, myArgs.length);
            return result;
        } else {
            return null;
        }
    }

    public Object getArg(int n) {
        if (myArgs != null) {
            return myArgs[n];
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public int getArgCount() {
        if (myArgs != null) {
            return myArgs.length;
        } else {
            return 0;
        }
    }

    /** we don't trust anyone outside the package to *not* mess
     * with the contents of the args array.
     */
    /*package*/ Object[] getArgs() {
        return myArgs;
    }

    public RtExceptionEnv getKeeper() {
        return myEE;
    }

        //ABS Changed EObject to object, in particular because
        // it was neeeded for an RtTether (as casted inside)
        // but from discussions with Dan Object might be the
        // way to go. XXXDAN, pls check.
    public void sendTo(Object target) {
        ((RtTether)target).invoke(mySealer, myArgs, myEE);
    }

    public void sendException(Throwable t) {
        RtExceptionEnv.sendException(myEE, t);
    }

    public String toString()
    {
        return "#<" + getClass().getName() + " " + mySealer.toString() + " (" +
            argsToString(myArgs) + ")>";
    }

    static public String argsToString(Object[] args) {
        if (args == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < args.length; i++)
        {
            if (i != 0)
            {
                result.append(", ");
            }
            try {
                Object o = args[i];
                if (o == null) {
                    result.append("null");
                } else {
                    result.append(args[i].toString());
                }
            } catch (RuntimeException e) {
                result.append("<unprintable ");
                result.append(args[i].getClass().toString());
                result.append(">");
            }
        }

        return result.toString();
    }

    static public String messageToString(Object targ, RtSealer seal,
        Object[] args, RtExceptionEnv ee) {
        return targ + " <- sealer(" + seal + ") args(" +
            argsToString(args) + ") keeper(" + ee + ")";
    }

    static public Object[] cloneArgs(Object[] args) {
        int len = args.length;
        if (len == 0) {
            return args;
        } else {
            Object[] result = new Object[len];
            System.arraycopy(args, 0, result, 0, len);
            return result;
        }
    }
}




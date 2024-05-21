package ec.e.run;

final class EObjectEnvelope extends RtEnvelope {
    private String trace;

    EObjectEnvelope (String trace, RtEnvelope envelope)
    {
        super(envelope);
        this.trace = trace;
    }

    public void deliverTo (Object target)
    {
        // conspire with run to set causality trace string
        if (   (trace != null)
            && (RtRun.theOne().myCausalityTracing == true)) {
            RtRun.setCausalityTraceString(trace);
        } else {
            RtRun.clearCausalityTraceString();
        }
        super.deliverTo (target);
    }
}

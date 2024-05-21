package ec.e.db;

import ec.e.cap.EEnvironment;

public class DBTracing {
    private static boolean alreadyHandled = false;
    
    public static void TraceMode(EEnvironment env) {
        if (alreadyHandled)
            return;
        alreadyHandled = true;
        
        RtStandardEncoder.tr.traceMode(
            env.getPropertyAsBoolean("ECtraceRtStandardEncoder"));
        RtStandardEncoder.ptr.traceMode(
            env.getPropertyAsBoolean("ECtraceRtStandardEncoderProfiling"));
        RtStandardDecoder.tr.traceMode(
            env.getPropertyAsBoolean("ECtraceRtStandardDecoder"));
        RtStandardDecoder.ptr.traceMode(
            env.getPropertyAsBoolean("ECtraceRtStandardDecoderProfiling"));
        TypeTable.tr.traceMode(env.getPropertyAsBoolean("ECtraceTypeTable"));
    }
}

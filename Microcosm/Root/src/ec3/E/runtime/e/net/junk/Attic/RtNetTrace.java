package ec.e.net;

import ec.e.cap.EEnvironment;

public class RtNetTrace {
    private static boolean alreadyHandled = false;

    public static void TraceMode(EEnvironment env) {
        if (alreadyHandled)
            return;
        alreadyHandled = true;

        ENet.tr.traceMode(env.getPropertyAsBoolean("ECtraceENet"));
        EPublisher.tr.traceMode(env.getPropertyAsBoolean("ECtraceEPublisher"));
        RtPublicationServer.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtPublicationServer"));
        ERegistrar.tr.traceMode(env.getPropertyAsBoolean("ECtraceERegistrar"));
        RtRegistrarServer.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtRegistrarServer"));
        RtRegConnectionFactory.tr.traceMode(
            env.getPropertyAsBoolean("ECtraceRtRegConnectionFactory"));
        RtComMonitor.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtComMonitor"));
        RtComListener.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtComListener"));
        RtConnection.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtConnection"));
        RtConnection.ptr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtConnectionProfile"));
        EConnectionKeeper.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceEConnectionKeeper"));
        RtExportTable.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtExportTable"));
        RtImportTable.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtImportTable"));
        RtMsgReceiver.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtMsgReceiver"));
        RtMsgReceiver.ptr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtMsgReceiverProfile"));
        RtMsgSender.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtMsgSender"));
        RtMsgSender.ptr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtMsgSenderProfile"));
        RtTransceiver.tr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtTransceiver"));
        RtTransceiver.ptr.
            traceMode(env.getPropertyAsBoolean("ECtraceRtTransceiverProfile"));
    }
}

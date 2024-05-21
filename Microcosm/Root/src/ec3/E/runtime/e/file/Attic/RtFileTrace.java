package ec.e.file;

import ec.e.cap.EEnvironment;

public class RtFileTrace {
	private static boolean alreadyHandled = false;

	public static void TraceMode(EEnvironment env) {
		if (alreadyHandled) return;
		alreadyHandled = true;

		// BUG--what should this do? RtFile doesn't exist anymore.
		// RtFile.tr.traceMode(env.getPropertyAsBoolean("ECtraceRtFile"));
	}
}

package ec.e.stream;

import java.lang.reflect.Modifier;
import ec.e.openers.guest.SerializableMarker;


/**
 * Marker interface for what should be natively passed by copy through
 * the comm system
 */
public interface PassByCopy extends SerializableMarker {

    static public final int MODIFIER_MASK = Modifier.TRANSIENT;

    static public final int ELSE_MODE = IGNORE;

    static public final Object[] ALLOWED_ANYWAY = {
    };

    static public final Object[] ALLOWED_CLASSES = {
        ALLOWED_ANYWAY,
        "java.lang.Object",
        "ec.e.run.RtEnvelope",
        "ec.e.run.RtExceptionEnv",
        "ec.e.run.EDistributor_$_Sealer",
        "ec.e.run.RtSealer",
        "ec.e.run.EWhenClosure_$_Sealer",
    };

    /** must be empty until comm is switched to serialization */
    static public final String[][] DECODER_MAKERS = {};

    /** must be empty until comm is switched to serialization */
    static public final String[][] ENCODER_MAKERS = {};
}
        

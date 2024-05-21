package ec.e.hold;

import ec.e.openers.guest.SerializableMarker;


/**
 * Marker interface for what can go into a Repository
 */
public interface Reposable extends SerializableMarker {

    static public final Object[] ALLOWED_ANYWAY = {
    };

    static public final Object[] ALLOWED_CLASSES = {
        ALLOWED_ANYWAY,
        "ec.cert.Certificate",
        "ec.cert.CryptoHash",
        "ec.cosm.gui.appearance.Appearance2D",
        "ec.cosm.gui.appearance.Appearance3D",
        "ec.cosm.gui.appearance.Cel",
        "ec.cosm.gui.appearance.Frame",
        "ec.cosm.gui.appearance.Gesture2D",
        "ec.cosm.gui.appearance.Gesture3D",
        "ec.cosm.gui.appearance.Group",
        "ec.cosm.gui.appearance.Position",
        "ec.cosm.gui.appearance.jbyteArrayWrapper",
        "ec.cosm.gui.dynamics.SwatchInfo",
        "ec.cosm.gui.dynamics.SwatchManager",
        "ec.cosm.gui.dynamics.Swatchable",
        "ec.e.hold.DataHolderSteward",
        "ec.e.hold.Fulfiller",
        "ec.e.rep.steward.KeyPosition",
        "ec.misc.graphics.Point2DInt",
        "ec.misc.graphics.Point3D",
        "java.lang.Integer",
        "java.lang.Object",
        "java.util.Vector",

        "ec.cert.Verifier",
        "sun.security.provider.DSAPublicKey",
        "sun.security.x509.X509Key",
        "sun.security.x509.AlgIdDSA",
        "sun.security.x509.AlgorithmId",
        "sun.security.util.ObjectIdentifier",
        "sun.security.util.DerValue",
        "sun.security.util.DerInputBuffer",
        "java.io.ByteArrayInputStream",
        "java.io.InputStream",
        "sun.security.util.DerInputStream",
        "java.math.BigInteger",
        "java.lang.Number",
        "ec.cert.VerifierDescription",
        "ec.cosm.gui.appearance.Appearance",
        "ec.cosm.gui.appearance.Gesture",
    };
}


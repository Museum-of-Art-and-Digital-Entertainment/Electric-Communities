package ec.e.hold;

import ec.e.openers.guest.SerializableMarker;
import ec.e.openers.JavaUtil;


/**
 * Marker interface for what can go into a Repository
 */

public interface ReposableMarker extends SerializableMarker {

    /**
     * The ENCODER_MAKERS below should be identical to those in
     * ec.e.openers.guest.SerializableMarker except these contain
     * ec.e.hold.DataHolderSteward. DataHolders must be encoded
     * as-they-are into the checkpoint file since they reference their
     * Fulfillers and other in-vat objects in a manner that is not
     * possible to reconstruct before a vat exists; Therefore, at
     * revival time we cannot use the same recipes we'd use when
     * reconstituting from a Repository. Nor would we want to, since
     * that'd be more work.
     *
     * @see ec.e.openers.guest.SerializableMarker#ENCODER_MAKERS
     * @see ec.e.openers.guest.SerializableMarker#DECODER_MAKERS
     */

    static public final String[][] EXTRA_ENCODER_MAKERS = {
        { "ec.e.hold.DataHolderSteward", "ec.e.hold.DataHolderRecipe" },
    };

    static public final String[][] ENCODER_MAKERS =
        (String[][])JavaUtil.append(SerializableMarker.ENCODER_MAKERS,
                                    EXTRA_ENCODER_MAKERS);


    /** 
     * The DECODER_MAKERS below should be identical to those in
     * ec.e.openers.guest.SerializableMarker except these contain
     * ec.e.hold.DataHolderSteward. DataHolders must be encoded
     * as-they-are into the checkpoint file since they reference their
     * Fulfillers and other in-vat objects in a manner that is not
     * possible to reconstruct before a vat exists; Therefore, at
     * revival time we cannot use the same recipes we'd use when
     * reconstituting from a Repository. Nor would we want to, since
     * that'd be more work.

     * @see ec.e.openers.guest.SerializableMarker#ENCODER_MAKERS */

    static public final String[][] EXTRA_DECODER_MAKERS = {
        { "ec.e.hold.DataHolderSteward", "ec.e.hold.DataHolderRecipe" },
    };

    static public final String[][] DECODER_MAKERS =
        (String[][])JavaUtil.append(SerializableMarker.DECODER_MAKERS,
                                    EXTRA_DECODER_MAKERS);


    static public final Object[] ALLOWED_ANYWAY = {};

    static public final Object[] ALLOWED_CLASSES = {
        ALLOWED_ANYWAY,
        "opening ec.cert.Certificate",
        "opening ec.cert.CryptoHash",
        "opening ec.cert.Verifier",
        "opening ec.cert.VerifierDescription",
        "opening ec.cosm.gui.appearance.jbyteArrayWrapper",
        "opening ec.cosm.gui.dynamics.SwatchInfo",
        "opening ec.cosm.gui.dynamics.SwatchManager",
        "opening ec.cosm.gui.dynamics.Swatchable",
        "opening ec.cosm.ui.presenter.ButtonCommandPresenter",
        "opening ec.cosm.ui.presenter.ButtonPanelData",
        "opening ec.cosm.ui.presenter.CommandButtonPanelData",
        "opening ec.cosm.ui.presenter.CommandPresenterData",
        "opening ec.cosm.ui.presenter.ControlPanelData",
        "opening ec.cosm.ui.presenter.ImageData",
        "opening ec.cosm.ui.presenter.SelectableImageData",
        "opening ec.cosm.ui.presenter.UIPresenterData",
        "opening ec.cosm.ui.presenter.WindowBorderData",
        "opening ec.e.rep.steward.RepositoryDirectDataHandle",
        "opening ec.tables.Column",
        "opening ec.tables.EqualityKeyColumn",
        "opening ec.tables.KeyColumn",
        "opening ec.tables.RefColumn",
        "opening ec.tables.ShareCount",
        "opening ec.tables.SimTable",
        "opening ec.tables.Table",
        "opening java.awt.Point",
        "opening java.io.ByteArrayInputStream",
        "opening java.io.InputStream",
        "opening java.lang.Integer",
        "opening java.lang.Number",
        "opening java.lang.Object",
        "opening java.math.BigInteger",
        "opening java.util.Dictionary",
        "opening java.util.Vector",
        "opening sun.security.provider.DSAPublicKey",
        "opening sun.security.util.DerInputBuffer",
        "opening sun.security.util.DerInputStream",
        "opening sun.security.util.DerValue",
        "opening sun.security.util.ObjectIdentifier",
        "opening sun.security.x509.AlgIdDSA",
        "opening sun.security.x509.AlgorithmId",
        "opening sun.security.x509.X509Key",
    };
}

package ec.e.upgrade;

/**
 * UpgradeWrapperMaker is an "abstract" interface marking all WrapperMakers.
 * When an Object is to be sent or Proxied over the wire, the
 * class for the Object is looked up in the WrapperTable used 
 * for the connection, and if a WrapperMaker is found, it is used (in 
 * the correct context of a "concrete" WrapperMaker Interface) to match
 * versions of the Object between systems.
 *
 * @see ec.e.upgrade.WrapperTable
 *
 * @see ec.e.upgrade.StateWrapperMaker
 * @see ec.e.upgrade.InterfaceWrapperMaker
 */
public interface UpgradeWrapperMaker {
}   


package ec.e.upgrade;

/**
 * UpgradeConverter is an "abstract" interface marking all Converters.
 * When an Object is to be sent or Proxied over the wire, the
 * class for the Object is looked up in the UpgradeTable used 
 * for the connection, and if an UpgradeConverter is found, it is used (in 
 * the correct context of a "concrete" UpgradeConverter Interface) to match
 * versions of the Object between systems.
 *
 * @see ec.e.upgrade.UpgradeTable
 *
 * @see ec.e.upgrade.StateConverter
 * @see ec.e.upgrade.InterfaceConverter
 */
public interface UpgradeConverter {
}   


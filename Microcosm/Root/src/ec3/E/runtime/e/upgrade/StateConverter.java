package ec.e.upgrade;

import java.io.IOException;


/**
 * StateConverter is an UpgradeConverter interface that wrappers
 * for Objects that are sent over the wire by copying their state must
 * implement. When an Object is about to be copied over the wire
 * and a StateConverter is present for it, the StateConverter is
 * used to encode the Object onto an Encoder stream such that a prior 
 * version of the class can decode the object on the other side of
 * the wire. 
 *
 * Conversely, when an object is to be decoded and its class is
 * maps to a StateConverter, the StateConverter is used to
 * decode the Object.
 *
 * @see ec.e.upgrade.UpgradeConverter
 */
public interface StateConverter extends UpgradeConverter
{
    /**
     * Encode something over the wire which can be read by 
     * a prior version of the class on the other side.
     */
    void encode (Object object, RtEncoder encoder) throws IOException;
    
    /**
     * Decode into the Object (or return another) from a decoder
     * which contains the encoded contents of a prior version of
     * the class.
     */
    Object decode (Object object, RtDecoder decoder) throws IOException;
}       
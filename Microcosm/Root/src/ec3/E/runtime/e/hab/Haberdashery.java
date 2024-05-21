package ec.e.hab;

import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import ec.e.rep.steward.Repository;
import ec.cert.CryptoHash;

/**
  An open Haberdashery.
*/
public class Haberdashery {
    private Repository rep;

    /**
    * Construct a Haberdashery in a given Repository.
    *
    * @param rep The Repository containing the Haberdashery.
    */
    public Haberdashery(Repository rep) {
        this.rep = rep;
    }

    /**
    * Construct a Haberdashery in a given EEditableFile.
    *
    * @param editableFile The File to use
    */
    public Haberdashery(File aFile) throws IOException {
        this(new Repository(aFile));
    }

    /**
    * Initialize a Haberdashery to its empty state.
    *
    * This won't really do the right thing, since any other junk that is in
    * the Repository will still be there. Think of it like erasing a floppy
    * disk by rewriting the directory sectors. In a better version this will
    * be integrated with the Repository open/create operation, so that we'll
    * all be happy and safe.
    */
    public void init() throws IOException { /* XXX should be superceded! */
        putRoot(new Unit());
    }

    /**
    * Close a Haberdashery. This is probably not ideal and should be replaced
    * with something that is smarter and more automatic.
    */
    public void close() throws IOException { /* XXX should be superceded! */
        rep.close();
        rep = null; /* Become unusable once closed. */
    }

    //    /**
    //    * Make sure anything we've done to the Haberdashery is saved.
    //    */
    //    public void checkPoint() {
    //        rep.checkPoint();
    //    }

    /**
    * Fetch a directly designated object from the Haberdashery.
    *
    * @param what The (direct) Designator of the object to fetch.
    * @return The designated HaberdasheryObject.
    */
    public HaberdasheryObject get(Designator what) {
        return(((DirectDesignator)what).get(rep));
    }

    /**
    * Fetch an indirectly designated object from the Haberdashery via a
    * designated root unit.
    *
    * @param what The (indirect) Designator of the object to fetch.
    * @param root The (direct) Designator of the Unit to resolve the path.
    * @return The designated HaberdasheryObject.
    */
    public HaberdasheryObject get(Designator what,
                                  Designator root) {
        return(((IndirectDesignator)what).get(rep, root));
    }

    /**
    * Fetch an indirectly designated object from the Haberdashery via a
    * given root unit.
    *
    * @param what The (indirect) Designator of the object to fetch.
    * @param rootUnit A root Unit to resolve the path.
    * @return The designated HaberdasheryObject.
    */
    public HaberdasheryObject get(Designator what, Unit rootUnit) {
        return(((IndirectDesignator)what).get(rep, rootUnit));
    }

    /**
    * Determine the DirectDesignator that currently corresponds to a given
    * IndirectDesignator via a given root unit.
    *
    * @param what The (indirect) Designator of the object of interest.
    * @param rootUnit A root Unit to resolve the path.
    * @return The DirectDesignator of the indirectly designated object.
    */
    public DirectDesignator determine(Designator what, Unit rootUnit) {
        return(((IndirectDesignator)what).determine(rep, rootUnit));
    }

    /**
    * Delete a directly designated object from the Haberdashery.
    *
    * @param what The (direct) Designator of the object to delete.
    */
    public void delete(Designator what) {
        ((DirectDesignator)what).delete(rep);
    }

    /**
    * Delete an indirectly designated object from the Haberdashery via a
    * designated root unit.
    *
    * @param what The (indirect) Designator of the object to delete.
    * @param root The (direct) Designator of the Unit to resolve the path.
    */
    public void delete(Designator what, Designator root) {
        ((IndirectDesignator)what).delete(rep, root);
    }

    /**
    * Delete an indirectly designated object from the Haberdashery via a
    * given root unit.
    *
    * @param what The (indirect) Designator of the object to delete.
    * @param rootUnit A root Unit to resolve the path.
    */
    public void delete(Designator what, Unit rootUnit) {
        ((IndirectDesignator)what).delete(rep, rootUnit);
    }

    /**
    * Test if a Designator is fully determined in this Haberdashery.
    *
    * @return true iff the designated object is fully determined.
    * @param what The Designator of the object we are interested in
    * @param rootUnit The root unit for fetching indirectly designated objects.
    */
    public boolean isFullyDetermined(Designator what, Unit rootUnit) {
        return(what.isFullyDetermined(rep, rootUnit));
    }

    /**
    * Test if a Designator is fully specified in this Haberdashery.
    *
    * @return true iff the designated object is fully specified.
    * @param what The Designator of the object we are interested in
    * @param rootUnit The root unit for fetching indirectly designated objects.
    */
    public boolean isFullySpecified(Designator what, Unit rootUnit) {
        return(what.isFullySpecified(rep, rootUnit));
    }

    /**
    * Toss a new object into the Haberdashery.
    *
    * @param obj The HaberdasheryObject to add.
    * @return A DirectDesignator for the object.
    */
    public DirectDesignator put(HaberdasheryObject obj) throws IOException {
        CryptoHash key = rep.putHash(obj);
        return(new HashkeyDesignator(key));
    }

    /**
    * Write or update the root Unit object.
    *
    * @param The new instance of the root Unit object to store.
    */
    public void putRoot(Unit rootUnit) throws IOException {
        rep.put(RootDesignator.getRootKey(), rootUnit);
    }
}

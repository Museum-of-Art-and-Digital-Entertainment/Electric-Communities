package ec.e.net;

/**
 * Object which holds onto a registration previously made with the registrar.
 * Can produce a URL for giving to others or be used to unregister.
 */
public class Registration {
    private Registrar myRegistrar;
    private String myUrl;
    private String myObjectID;

    /**
     * Create a new registration object.
     *
     * @param registrar The Registrar which did this registration.
     * @param earl The EARL for the registered object.
     */
    Registration(Registrar registrar, EARL earl) {
        myRegistrar = registrar;
        myUrl = earl.url();
        myObjectID = earl.objectID();
    }

    /**
     * Return a URL for this registration.
     */
    public String url() {
        return myUrl;
    }

    /**
     * Unregister this registration.
     */
    public void unregister() throws RegistrarException {
        myRegistrar.unregister(myObjectID);
    }
}

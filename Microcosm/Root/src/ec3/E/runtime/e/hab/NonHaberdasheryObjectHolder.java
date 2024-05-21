package ec.e.hab;
import java.util.Enumeration;

/**
  HaberdasheryObject wrapper for random non-Haberdashery objects.
*/
public class NonHaberdasheryObjectHolder extends HaberdasheryObject {
    /** Who we're holding on to. */
    /*final*/ private Object heldObject;

    /**
    * Construct a HaberdasheryObject wrapper for some random Object.
    * @param heldObject The object to hold on to.
    */
    public NonHaberdasheryObjectHolder(Object heldObject) {
        this.heldObject = heldObject;
    }

    /**
    * Get the non-Haberdashery object we hold on to.
    *
    * @return The object held on to.
    */
    public Object getHeldObject() {
        return(heldObject);
    }
}

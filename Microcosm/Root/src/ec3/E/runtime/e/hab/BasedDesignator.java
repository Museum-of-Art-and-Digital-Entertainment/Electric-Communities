package ec.e.hab;

/**
  Class to hold on to a designator and manipulate it generically, regardless
  of whether it is direct or indirect.
*/
public class BasedDesignator {
    private Designator des;
    private DirectDesignator baseDes;
    private Unit base;
    private Haberdashery baseHab;

    /**
    * Construct a BasedDesignator given a (presumably direct) Designator.
    *
    * @param des A (direct) Designator to hold onto
    */
    public BasedDesignator(Designator des) {
        this.des = des;
        this.base = null;
        this.baseDes = null;
        this.baseHab = null;
    }

    /**
    * Construct a BasedDesignator given a (presumably indirect) Designator and
    * an accompanying DirectDesignator to base its path on.
    *
    * @param des An (indirect) Designator to hold onto.
    * @param baseDes A DirectDesignator of a Unit to base the path in 'des' on
    */
    public BasedDesignator(Designator des, DirectDesignator baseDes) {
        this.des = des;
        this.baseDes = baseDes;
        this.base = null;
        this.baseHab = null;
    }

    /**
    * Make sure we're actually holding onto the base Unit for this.
    *
    * @param hab A Haberdashery that the Unit should be obtained from
    */
    private void ensureBase(Haberdashery hab) {
        if (baseHab != hab || base == null) {
            baseHab = hab;
            base = (Unit) hab.get(baseDes);
        }
    }

    /**
    * Retrieve our designated object from a Haberdashery.
    *
    * @param hab A Haberdashery to get the object out of
    * @return An instance of our designated object.
    */
    public HaberdasheryObject get(Haberdashery hab) {
        if (des.isDirect()) {
            return(hab.get(des));
        } else {
            ensureBase(hab);
            return(hab.get(des, base));
        }
    }

    /**
    * Procure a (direct) BasedDesignator for the object which we currently
    * designate.
    *
    * @param hab A Haberdashery to get the object from.
    * @return A determined BasedDesignator for the object.
    */
    public BasedDesignator determine(Haberdashery hab) {
        if (des.isDirect()) {
            return(this);
        } else {
            ensureBase(hab);
            return(new BasedDesignator(hab.determine(des, base)));
        }
    }

    /**
    * Remove our designated object from a Haberdashery.
    *
    * @param hab A Haberdashery to remove the object from.
    */
    public void delete(Haberdashery hab) {
        if (des.isDirect()) {
            hab.delete(des);
        } else {
            ensureBase(hab);
            hab.delete(des, base);
        }
    }

    /**
    * Return the Designator we hold onto.
    */
    public Designator getDesignator() {
        return(des);
    }

    /**
    * Return the base we are based on.
    */
    public DirectDesignator getBase() {
        return(baseDes);
    }

    /**
    * Construct a new BasedDesignator like us but with a different base.
    *
    * @param newBaseDes The DirectDesignator of a different root.
    */
    public BasedDesignator rebase(DirectDesignator newBaseDes) {
        return(new BasedDesignator(des, newBaseDes));
    }

    /**
    * Generate a BasedDesignator for our hierarchy parent. This only works
    * if we are holding onto an IndirectDesignator!
    */
    public BasedDesignator parent() {
        if (!(des instanceof PathDesignator))
            throw new HaberdasherException("designator is an orphan");
        IndirectDesignator newDes = ((PathDesignator) des).parent();
        if (newDes == null)
            return(new BasedDesignator(baseDes)); /* baseDes hops over */
        else
            return(new BasedDesignator(newDes, baseDes));
    }

    /**
    * Return the last element in the pathname of the path we are holding onto
    * (and we had better be holding onto one).
    */
    public String childName() {
        if (!(des instanceof PathDesignator))
            throw new HaberdasherException("designator is nameless");
        return(((PathDesignator) des).childName());
    }

    /**
    * Test if our Designator is direct.
    * @return true if direct, false if indirect.
    */
    public boolean isDirect() {
        return(des.isDirect());
    }

    /**
    * Test if we are based at the root.
    * @return true if root based, false if not.
    */
    public boolean isRooted() {
        if (baseDes == null)
            return(false);
        else
            return(baseDes.isRoot());
    }

    /**
    * Produce a tidy string representation of ourselves.
    */
    public String toString() {
        if (base == null)
            return(des.toString());
        else if (isRooted())
            return("." + des.toString());
        else
            return(baseDes.toString() + "." + des.toString());
    }
}

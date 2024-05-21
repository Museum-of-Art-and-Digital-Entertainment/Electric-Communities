package ec.ez.collect;

/*package*/ class NullMarker {

    static private NullMarker THE_ONE = new NullMarker();

    private NullMarker() {}

    static /*package*/ Object encode(Object opt) {
        if (opt == null) {
            return THE_ONE;
        } else {
            return opt;
        }
    }

    static /*package*/ Object optDecode(Object optMarked, String complaint)
         throws NotFoundException {

        if (optMarked == THE_ONE) {
            return null;
        } else if (optMarked == null) {
            throw new NotFoundException(complaint);
        } else {
            return optMarked;
        }
    }

    /**
     * Without a complaint argument, the caller is effectively asserting
     * that 'marked' cannot be null.
     */
    static /*package*/ Object optDecode(Object marked) {
        try {
            return optDecode(marked, "");
        } catch (NotFoundException ex) {
            throw new Error("internal: a null can't happen here");
        }
    }
}


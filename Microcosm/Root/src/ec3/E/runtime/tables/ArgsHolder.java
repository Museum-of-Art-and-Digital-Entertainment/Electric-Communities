package ec.tables;


/**
 * 
 */
public final class ArgsHolder {

    static public final Class TYPE = new ArgsHolder(null, null).getClass();
    static public final Class[] MARKING_PARAMS = { TYPE };

    private IntTable myGuide;
    private Object[] myArgs;


    /**
     *
     */
    public ArgsHolder(IntTable guide, Object[] args) {
        myGuide = guide;
        myArgs = args;
    }

    /**
     *
     */
    public Object byIndex(int index) {
        return myArgs[index];
    }

    /**
     *
     */
    public Object get(String name, Object instead) {
        int i = myGuide.getInt(name, -1);
        if (i == -1) {
            return instead;
        } else {
            return myArgs[i];
        }
    }

    /**
     *
     */
    public Object get(String name) {
        int i = myGuide.getInt(name);
        return myArgs[i];
    }
}

        

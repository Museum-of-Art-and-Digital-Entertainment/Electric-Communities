package ec.e.rep.steward;
import ec.e.openers.Recipe;
import ec.e.openers.OpenerID;
// import ec.e.openers.RefOpener;

public class RepositoryHandleRecipe extends Recipe {

    // The variable and set method below are special to RepositoryHandleRecipes.

    private static RepositoryFile theCurrentRepositoryFile = null;

    // Anyone can set the file. Noone except this class can use it.

    public static void setTheRepositoryFile(RepositoryFile newFile) {
        theCurrentRepositoryFile = newFile;
    }

    // End of unorthodox Recipe stuff

    static public final Class REPOSITORYHANDLE_TYPE = new RepositoryHandle().getClass();

    static private final Object[][] PrefaceParams = {};

    static private final Object[][] BodyParams = {
        { Long.TYPE, "myDataPos" },
        { Integer.TYPE, "myDataLength" },
    };

    // The below statement needs to come after the ones above.
    // Tricky stuff, these static initializers.

    private static final RepositoryHandleRecipe THE_ONE = new RepositoryHandleRecipe();

    public RepositoryHandleRecipe() {
        super(PrefaceParams, BodyParams, REPOSITORYHANDLE_TYPE);
    }

    static public Recipe makeEncoder() {
        return THE_ONE;
    }

    public Class type() {
        return REPOSITORYHANDLE_TYPE;
    }

    static public Recipe makeDecoder(OpenerID opid) {
        if (THE_ONE.openerID().equals(opid)) {
            return THE_ONE;
        } else {
            return null;
        }
    }

    public Object[] prefaceArgs(Object obj) {
        return Recipe.NO_ARGS;
    }

    /**

     * Create a vector of Objects describing the object.  We just have
     * a long (the file position) and an integer (the length).

     */

    public Object[] bodyArgs(Object obj) {
        RepositoryHandle handle = (RepositoryHandle) obj;
        Object[] result = new Object[2];
        result[0] = new Long(handle.myDataPos);
        result[1] = new Integer(handle.myDataLength);
        return result;
    }

    /**
     * Defaults to calling halfBakedInstanceOf using type().
     */

    public Object halfBakedInstance(Object[] prefaceArgs) {
        return halfBakedInstanceOf(type(), prefaceArgs);
    }

    /**
     * Ignores the sub and always returns a RepositoryHandle
     */

    public Object halfBakedInstanceOf(Class sub, Object[] prefaceArgs) {
        return new RepositoryHandle();
    }

    public void cook(Object halfBaked, Object[] bodyArgs) {
        RepositoryHandle result = (RepositoryHandle)halfBaked;

        // Our bodyArgs are a Long and an Integer
        result.myDataPos = ((Long)bodyArgs[0]).longValue();
        result.myDataLength = ((Integer)bodyArgs[1]).intValue();

        // The current repositoryfile is not encoded into the file.
        // We just know which one it is since we are reading from it.

        result.myRepositoryFile = theCurrentRepositoryFile;
    }
}

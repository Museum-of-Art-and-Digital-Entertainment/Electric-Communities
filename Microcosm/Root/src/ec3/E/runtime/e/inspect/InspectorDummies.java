package ec.e.inspect;

public class Inspector {

    static {
        throw new ExceptionInInitializerError("Loaded dummy Inspector class");
    }

    private static void dummyCalled() {
        Error err = new Error("Dummy method called - please recompile in runtime/e/inspect directory");
        err.printStackTrace();
        throw err;
    }

    public Inspector() {
        dummyCalled();  
    }

    public static void inspect(Object object, String name) {
        dummyCalled();
    }

    public static synchronized void setupRunQueueInspector() {
        dummyCalled();
    }

    public static void enableGathering(boolean enabled) {
        dummyCalled();
    }
        
    public static void checkForAndStartInspector(Object env){
        dummyCalled();
    }

    public static void checkForAndStartInspector(String inspectorLevel,String inspectorClassName) {
        dummyCalled();
    }

    public static void gather(Object iobj, String category, String name) {
        dummyCalled();
    }

}

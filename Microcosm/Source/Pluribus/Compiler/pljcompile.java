package ec.plcompile;

public class PluribusCompiler {
    native static int runNativeMain(String args[]);

    public static void main(String args[]) {
        try {
            System.loadLibrary("pl");
            int resultCode = runNativeMain(args);
            System.exit(resultCode);
        } catch (UnsatisfiedLinkError e) {
            System.out.println("couldn't load libpl shared library");
            e.printStackTrace();
        }
    }
}

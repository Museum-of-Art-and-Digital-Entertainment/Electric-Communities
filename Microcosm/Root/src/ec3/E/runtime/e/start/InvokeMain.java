package ec.e.run;

import java.lang.reflect.Method;
import ec.vcache.ClassCache;
import ec.util.Native;

public class InvokeMain  {

    public static void main(String [] args)  {
        // Assume class name for application is first arg
        String className = args[0];
        String newArgs[] = new String[args.length-1];
        // Any/all other args are args for the application,
        // so pass them on to newArgs array
        if (args.length > 1)  {
          for(int i=1; i < args.length; i++)  {
            newArgs[i-1] = args[i];
          }
        }
        // Load in class of given name
        Class theClass = null;
        String zipName = "startup.zip";

        // XXX create security manager
        // This is a hacked-up security manager...used as a work around for the
        // interaction problem between a non-null classloader and native code.
        // See http://dugite.ee.uwa.edu.au/~gareth/jni_fix.html for more
        // information
        System.setSecurityManager(new ZippySecurityManager());

        ZippyClassLoader loader = null;

        try {
          loader = new ZippyClassLoader(zipName);
          System.out.println("Loading classes from "+zipName);
          long stime = Native.queryTimer();
          int size = loader.loadCacheWithClassBytes();
          long totalTime = (Native.queryTimer()-stime)/1000;
          System.out.println("Done loading "+size+" classes from "+zipName+
                             ", in (ms): "+totalTime);
          theClass = loader.loadClass(className);
        } catch (Exception e) {
          // deal with failure...by ignoring it!
          System.out.println("Can't load any classes from "+
                             zipName+" using null classloader.");
          loader = null;
      // If some problem loading classes from zip with zippy classloader, use null
      // classloader instead...if this fails...we're done
          try {
            theClass = ClassCache.forName(className);
          } catch (Exception e1) {
            System.out.println("Can't load application class "+
                               className+"...exiting.");
            System.exit(-1);
          }
        }

        System.out.println("The application class is "+
                           theClass.getName()+", loaded by classloader: "+
                           theClass.getClassLoader());
        System.out.println("Invoking main on class "+theClass.getName()+"...");
        try {
          new InvokeMain(theClass).invokeMain(newArgs);
        } catch (Exception e) {
          // Again can't deal with failure
          e.printStackTrace();
          System.exit(-1);
        }

    }

  private Method mainMethod= null;
  private Class theClass=null;

  public InvokeMain(Class theClass) throws Exception {
    this.theClass = theClass;
    // Create bogus string array so we can get class
    String bogus[] = new String[0];
    // Create params array for getMethod
    Class classes[] = new Class[1];
    // Set only element to Class String []
    classes[0] = bogus.getClass();
    // Get the main method for this class (if there is one)
         // If there is not, then this throws exception and caller will have to deal
         // with it
    mainMethod = theClass.getMethod("main", classes);
  }

  public void invokeMain(String args[]) throws Exception {
    Object argsArray[] = new Object[1];
    argsArray[0] = args;
    if (mainMethod != null) mainMethod.invoke(null, argsArray);
  }


}

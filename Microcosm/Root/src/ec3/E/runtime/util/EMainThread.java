package ec.util;

import ec.vcache.ClassCache;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EMainThread extends Thread
{
    private Object args[];
    private Method eMain;

    public EMainThread(ThreadGroup tg, String mainClassName, String args[]) throws ClassNotFoundException, NoSuchMethodException {
        super(tg, "EMainThread");
        Class mainClass = ClassCache.forName(mainClassName);
        Class EMainParamClasses[] = new Class[1];
        EMainParamClasses[0] = new String[0].getClass();
        try {
            eMain = mainClass.getMethod("EMain", EMainParamClasses);
        }
        catch (Exception e) {
            throw new NestedException("could not find public static method " + mainClassName + ".EMain(java.lang.String[])", e);
        }
        if (!Modifier.isStatic(eMain.getModifiers())) {
            throw new RuntimeException("method " + mainClassName + ".EMain is not declared static");
        }
        this.args = new Object[1];
        this.args[0] = args;
    }

    public void run() {
        try {
            eMain.invoke(null, args);
        }
        catch (Throwable t) {
            throw new NestedException("problem invoking EMain", t);
        }
    }

}

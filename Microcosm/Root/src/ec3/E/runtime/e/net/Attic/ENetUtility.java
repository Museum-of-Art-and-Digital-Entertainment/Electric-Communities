package ec.e.net;

import ec.e.run.RtUtil;
import ec.e.cap.*;

public class ENetUtility {
    public static boolean startNet(EEnvironment env, int port) { /*@BLOCK METHOD */
        try {
            ERegistrar reg = (ERegistrar)env.get("registrar.root");
            reg.startup(port);/*@BLOCK */
            return(true);
        } catch (Exception e) {
            /* XXX improper exception usage -- fix */
            System.out.println("Couldn't start network: " + e);
            e.printStackTrace();
            return(false);
        }
    }
    
    public static boolean stopNet(EEnvironment env) { /*@BLOCK METHOD */
        try {
            ERegistrar reg = (ERegistrar)env.get("registrar.root");
            reg.shutdown();/*@BLOCK */
            return(true);
        } catch (Exception e) {
            /* XXX improper exception usage -- fix */
            System.out.println("Couldn't stop network: " + e);
            e.printStackTrace();
            return(false);
        }
    }
    
    public static String registerWithPropertyName(EObject object,
                                                  EEnvironment env,
                                                  String propertyName,
                                                  String msg) {
        String ret = null;
        try {
            ERegistrar registrar = (ERegistrar)env.get("registrar.root");
            ERegistration reg = registrar.register(object);
            ret = reg.getURL();
            if (propertyName != null) {
                String fileName = env.getProperty(propertyName);
                if (fileName != null) {
                    RtUtil.writeStringInFile(ret, fileName);
                    if (msg != null)
                        System.out.println(msg + fileName);
                } else if (msg != null)
                    System.out.println(msg + ret);
            } else if (msg != null)
                System.out.println(msg + ret);
        } catch (Exception e) {
            /* XXX improper exception usage -- fix */
            ret = "Error registering object: " + e;
            if (msg != null)
                System.out.println(ret);
            e.printStackTrace();
        }
        return(ret);
    }
    
    public static String publishWithPropertyName(EObject object, String name,
                                                 EEnvironment env,
                                                 String propertyName,
                                                 String msg) {
        String ret = null;
        try {
            EPublisher publisher = (EPublisher)env.get("publisher.root");
            EPublication pub = publisher.publish(name, object);
            ret = pub.getURL();
            if (propertyName != null) {
                String fileName = env.getProperty(propertyName);
                if (fileName != null) {
                    RtUtil.writeStringInFile(ret, fileName);
                    if (msg != null)
                        System.out.println(msg + fileName);
                } else if (msg != null)
                    System.out.println(msg + ret);
            } else if (msg != null)
                System.out.println(msg + ret);
        } catch (Exception e) {
            /* XXX improper exception usage -- fix */
            ret = "Error publishing object " + name + ": " + e;
            if (msg != null)
                System.out.println(ret);
            e.printStackTrace();
        }
        return(ret);
    }
    
    public static String lookupWithName(EDistributor dist, EEnvironment env,
                                        String name) {
        String who = null;
        String errorString = null;
        ERegistrar reg = (ERegistrar)env.get("registrar.root");
        try {
            who = RtUtil.readStringFromFile(name);
            if (who == null) who = name;
            reg.lookupURL(who, dist);
        } catch (ERestrictedException e) {
            errorString = "lookupURL restricted: " + e;
        } catch (EInvalidUrlException e) {
            errorString = "Not a valid URL: " + who;
        }
        return(errorString);
    }
}

package ec.pl.runtime;

import ec.util.EThreadGroup;
import ec.e.file.EStdio;
import ec.e.file.EDirectoryRootMaker;
import ec.e.quake.TimeMachine;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Syncologist;
import ec.e.start.TimeQuake;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.util.Properties;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.IOException;

public eclass Login implements ELaunchable {
    static Trace tr = new Trace();

    private boolean isStarted = false;
    private EEnvironment myEnv;
    
    public Login () {
    }

    emethod go (EEnvironment env) {
        try {
            if (isStarted) {
                return;
            }
            isStarted = true;
            myEnv = env;

            if (tr.debug) tr.$("Establishing magic powers");

            UIFramework framework = (UIFramework)myEnv.magicPower("ec.pl.runtime.MagicUIPowerMaker");
            String avatarFile = myEnv.getProperty("AvatarFile");

            Properties avatarProps = null;
            if (avatarFile != null && avatarFile.length() > 0) {
                try {
                    avatarProps = new Properties();
                    FileInputStream is = new FileInputStream(avatarFile);
                    avatarProps.load(is);
                    is.close();
                    avatarProps.put("AvatarFileName", avatarFile);
                }
                catch (IOException e) {
                    EThreadGroup.reportException(e);
                    // we can safely ignore this since the login
                    // presenter will allow selection from a list of
                    // available avatar files.
                }
            }
            
            ELoginData eldata;

            framework.promptForLoginData(avatarProps, new SillyVerifier(), &eldata);

            ewhen eldata (Object props) {
                if (props != null) {
                    avatarProps = (Properties)props;
                    EStdio.err().println("User logged in as " + avatarProps.getProperty("AvatarName") + ", passphrase=" + avatarProps.getProperty("Passphrase"));
                    Enumeration en = avatarProps.propertyNames();
                    while (en.hasMoreElements()) {
                        String pname = (String)en.nextElement();
                        if (!pname.equals("Passphrase")) {
                            myEnv.setProperty(pname, avatarProps.getProperty(pname));
                        }
                    }
                    Agency agency = new Agency();
                    agency <- go(myEnv);
                }
                else {
                    EStdio.err().println("User login cancelled or failed.");
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {}             
                    env.vat().exit(0);
                }
            }

        }
        catch (Throwable t) {
            EStdio.err().println("Pluribus Login: fatal error during startup:");
            EStdio.reportException(t);
            EStdio.err().println("Aborting");
            try {
                Thread.currentThread().sleep(20000);
            } catch (InterruptedException e) {}             
            env.vat().exit(1);
        }
    }
}

class SillyVerifier implements PassphraseVerifier 
{
    SillyVerifier() { }
    
    public boolean verify(Properties props, String passphrase) {
        if (props.getProperty("Passphrase") == null) {
            return true;
        }
        return passphrase.equals(props.getProperty("Passphrase"));
    }
}

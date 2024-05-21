package ec.e.io;

import ec.e.run.Seismologist;
import ec.e.run.TimeQuake;
import ec.e.run.Vat;
import ec.e.run.EEnvironment;
import ec.e.run.FragileRootHolder;
import ec.e.io.RtConsole;
import java.io.InputStream;
import java.io.OutputStream;

public class EConsoleMaker {
    private static boolean issued = false;
    private Vat myVat;

    static public EConsoleMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException
    {
        return (EConsoleMaker)eEnv.magicPower("ec.e.io.EConsoleMakerMaker");
    }

    public EConsoleMaker(Vat vat) {
        if (issued)
            throw new SecurityException("EConsoleMaker already issued");
        issued = true;
        myVat = vat;
    }

    public void makeConsole(EInputHandler handler, InputStream is, OutputStream os) {
        EConsoleSeismologist seismo = new EConsoleSeismologist(myVat, handler, is, os);
        seismo <- noticeQuake(null);
    }
}

eclass EConsoleSeismologist implements Seismologist {
    private EInputHandler myHandler;
    private InputStream myIs;
    private OutputStream myOs;
    private Vat myVat;
    
    EConsoleSeismologist(Vat vat, EInputHandler handler, InputStream is, OutputStream os) {
        myVat = vat;
        myHandler = handler;
        myIs = is;
        myOs = os;
    }

    emethod noticeCommit() {
    }

    emethod noticeQuake(TimeQuake q) {
        FragileRootHolder handlerHolder = myVat.makeFragileRoot((Object) myHandler, (Seismologist) this);
        RtConsole.setupConsoleReader(handlerHolder, myIs, myOs);
    }
}

    

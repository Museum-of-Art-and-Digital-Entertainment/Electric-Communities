package ec.e.file;

import ec.e.start.Vat;
import ec.e.start.MagicPowerMaker;
import ec.e.start.Tether;
import ec.e.start.EEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;


public class URLStreamMaker
{
    private Vat myVat;
    
    static public URLStreamMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException
    {
        return (URLStreamMaker)eEnv.magicPower("ec.e.file.URLStreamMakerMaker");
    }
    private URLStreamMaker() {}
    
    public URLStreamMaker(Vat vat) {
        myVat = vat;
    }
    
    public InputStream getInputStream(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        InputStream is = url.openConnection().getInputStream();
        Tether isHolder = new Tether(myVat, is);
        return new EInputStream(isHolder);
    }
    
    public OutputStream getOutputStream(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        OutputStream os = url.openConnection().getOutputStream();
        Tether osHolder = new Tether(myVat, os);
        return new EOutputStream(osHolder);
    }
}

public class URLStreamMakerMaker implements MagicPowerMaker {
    public Object make(EEnvironment eEnv) {
        return new URLStreamMaker(eEnv.vat());
    }
}

        

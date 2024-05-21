package ec.e.start;

public class EEnvironment
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy EEnvironment class");
    }
}

public class Tether
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Tether class");
    }

    public Tether (Vat vat, Object object)  {
    }
    public Object held()  {
        return null;
    }
}

public interface Syncologist
{
}

public class Vat
{
    static {
        throw new ExceptionInInitializerError("Loaded dummy Vat class");
    }

    public Object vatLock() {
        return null;
    }
    public void setSyncQuakeNoticer(Syncologist obj) {
    }
}

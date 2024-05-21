package milk;

import java.io.IOException;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;

eclass Throw
implements ELaunchable
{
    public Throw(int x) throws IOException {
        throw new IOException("boo");
    }

    private void foo() throws IOException {
        throw new IOException("boo");
    }

    void biffy() throws IOException {
        throw new IOException("boo");
    }
    
    static void quijybu() throws IOException {
        throw new IOException("boo");
    }

    private static void quijybu2() throws IOException {
        throw new IOException("boo");
    }

    local void blixa() throws IOException {
        throw new IOException("boo");
    }

    static local void blixa2() throws IOException {
        throw new IOException("boo");
    }

    emethod go(EEnvironment env) {
        etry {
            this <- bar(22, null);
        } ecatch (IOException e) {
            System.out.println("Caught " + e);
            e.printStackTrace();
        }
    }

    emethod bar(int yummyluscious, Object gromble) throws IOException {
        throw new IOException("boo");
    }
}

einterface ThrowInt {
    emethod squibble() throws IOException;
}

eclass ThrowCla implements ThrowInt {
    emethod squibble() throws IOException {
        throw new IOException("gazonga");
    }
}

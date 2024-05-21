package ec.e.lang;

import java.io.PrintStream;

public eclass EPrintStream {
    PrintStream myStream;

    public EPrintStream(PrintStream stream) {
        myStream = stream;
    }
    
    emethod printObject(Object obj) {
        myStream.println(obj);
    }
    
    emethod printEObject(EObject anEObject) {
        ewhen anEObject (Object obj) {
            myStream.println(obj);
        }
    }
}


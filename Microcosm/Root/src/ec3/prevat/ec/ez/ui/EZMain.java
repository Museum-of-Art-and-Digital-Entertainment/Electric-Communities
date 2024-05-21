package ec.ez.ui;

import ec.e.run.OnceOnlyException;
import ec.e.start.EZStart;
import java.io.FileNotFoundException;
import java.io.IOException;


// This class is a kludge to call the EZStart class which has been
// placed in the ec.e.start package. Since ec.e.start does not occupy
// a directory hierarchy of the same form, begining there would always
// bring up an annoying dialog in Cafe seeking the source file
// location.
public class EZMain {

    public static void main(String args[])
         throws ClassNotFoundException, IllegalAccessException,
         InstantiationException, IOException, OnceOnlyException
    {
        EZStart.main(args);
    }
}

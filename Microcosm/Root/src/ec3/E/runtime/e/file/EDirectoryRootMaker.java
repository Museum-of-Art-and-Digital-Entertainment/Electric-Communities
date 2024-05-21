package ec.e.file;

import java.io.File;

import ec.e.run.Vat;
import ec.e.run.EEnvironment;


/**
* A privileged accessor class to top-out the recursion entailed in file and
* directory access using these classes. Only one instance of this object may
* be instantiated, and should be closely held.
*/
public class EDirectoryRootMaker {
    private static boolean issued = false;
    private Vat myVat;

    /**
     * Asks the EEnviroment to summon a EDirectoryRootMakerMaker, and
     * returns the EDirectoryRootMaker it conjures up.
     */
    static public EDirectoryRootMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException,
         IllegalAccessException, InstantiationException
    {
        return (EDirectoryRootMaker)eEnv.magicPower
            ("ec.e.file.EDirectoryRootMakerMaker");
    }

    /**
    * Construct a (the) directory root maker.
    *
    * @param vat The Vat in which this directory tree will be used.
    */
    public EDirectoryRootMaker(Vat vat) {
        if (issued)
            throw new SecurityException("EDirectoryRootMaker already issued");
        issued = true;
        myVat = vat;
    }

    /**
    * Produce a directory object for a file system root. Note that any
    * directory in the system with a known path name can function as a file
    * system root for this purpose.
    *
    * @param name The pathname of the root directory.
    * @return An EEditableDirectory object describing the given directory.
    */
    public EEditableDirectory makeDirectoryRoot(String name) {
        return(new EEditableDirectory(myVat, new File(name)));
    }

    /**
     * Produce an EEditableFile object for an arbitrary file.
     *
     * @param name The pathname of the file in question.
     * @return An EEditableFile object describing the given file.
     */
    public EEditableFile makeEditableFile(String name) {
        File theFile = new File(name);
        if (!theFile.isAbsolute())
            theFile = new File(theFile.getAbsolutePath());
        return new EEditableFile(myVat, theFile);
    }
}

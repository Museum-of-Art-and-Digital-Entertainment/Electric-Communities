package ec.e.file;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;


eclass FileTest implements ELaunchable {
    emethod go(EEnvironment env) {
        FileTester tester = new FileTester(env);
        tester <- go();
    }
}

eclass FileTester {
    private EEnvironment myEnv;
    
    public FileTester(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {
        Vat vat = myEnv.vat();

        try {
            EStdio.initialize(vat);
            EDirectoryRootMaker rootMaker = new EDirectoryRootMaker(vat);
            EEditableDirectory rootDir =
                rootMaker.makeDirectoryRoot("/home/kari/testbed");
            
            EEditableFile anEditFile;
            EAppendableFile anAppendFile;
            EReadableFile aReadFile;
            EEditableFile anExistingFile;

            EFileEditor anEditor;
            DataOutputStream anAppender;
            EFileReader aReader;

            EEditableDirectory aDir;
            int lineNumber;
            
            anExistingFile = rootDir.lookupFile("existing");
            aReadFile = anExistingFile.asReadableFile();
            aReader = aReadFile.reader();
            lineNumber = 1;
            while (true) {
                String line = aReader.readLine();
                if (line == null)
                    break;
                EStdio.out().println("line " + lineNumber++ + ": " + line);
            }
            aReader.close();

            anExistingFile = rootDir.lookupFile("deep/deeper/existing");
            aReadFile = anExistingFile.asReadableFile();
            aReader = aReadFile.reader();
            lineNumber = 1;
            while (true) {
                String line = aReader.readLine();
                if (line == null)
                    break;
                EStdio.out().println("line " + lineNumber++ + ": " + line);
            }
            aReader.close();

            anEditFile = rootDir.mkfile("afile");
            anEditor = anEditFile.editor();
            anEditor.writeBytes("this is a test, dude.\n");
            anEditor.close();
            
            aDir = rootDir.mkdir("sub");
            anEditFile = aDir.mkfile("asubfile");
            anEditor = anEditFile.editor();
            anEditor.writeBytes("this is a subtest, can't you tell?\n");
            anEditor.close();
            
            anEditFile = rootDir.lookupFile("afile");
            anAppendFile = anEditFile.asAppendableFile();
            anAppender = new DataOutputStream(anAppendFile.outputStream());
            anAppender.writeBytes("here is some more stuff.\n");
            anAppender.close();

            aReadFile = anEditFile.asReadableFile();
            aReader = aReadFile.reader();
            lineNumber = 1;
            while (true) {
                String line = aReader.readLine();
                if (line == null)
                    break;
                EStdio.out().println("line " + lineNumber++ + ": " + line);
            }
            aReader.close();

            aDir.contain(rootDir, aReadFile, "amovedfile");
        } catch (Exception e) {
            EStdio.err().println("something choked!");
            e.printStackTrace(EStdio.err());
        }
    }
}

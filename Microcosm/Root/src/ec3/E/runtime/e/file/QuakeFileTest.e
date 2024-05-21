package ec.e.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.DataOutputStream;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;

import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;

eclass QuakeFileTest implements ELaunchable {
    emethod go(EEnvironment env) {
        QuakeFileTester tester = new QuakeFileTester(env);
        tester <- go();
    }
}

eclass QuakeFileTester implements QuakeProofAppendFileListner {
    private EEnvironment myEnv;
    private TimeMachine  myTM = null;
    private QuakeProofAppendFile myAppendFile = null;
    private DataOutputStream myAppender;
    
    public QuakeFileTester(EEnvironment env) {
        myEnv = env;
    }

    emethod go() {
        Vat vat = myEnv.vat();

        try {
            myTM = TimeMachine.summon(myEnv);
        } catch (Exception e) {
            EStdio.out().println("Cant summon a timeMachine : " + 
                                 e.getMessage());
        }

        myTM <- nextQuake(new QuakeReporter());

        this <- doFirstPass(vat);
        myTM <- hibernate(null, 0);
        this <- afterRevive();
    }

    emethod doFirstPass(Vat vat) {

        try {

            myAppendFile = new QuakeProofAppendFile(vat, null, "quakeproof");

            EStdio.out().println("Tester is setting listner");
            myAppendFile.setReconstructListner(this);

            myAppender = new DataOutputStream(myAppendFile);

            myAppender.writeBytes("First line to the file\n\r");
            myAppender.writeBytes("Second line to the file\n\r");
            myAppender.writeBytes("Next line will be after revive\n\r");

            myAppender.flush();
            EStdio.out().println("File len is now " + myAppendFile.length());

        } catch (Exception e) {
            EStdio.err().println("something choked!");
            e.printStackTrace(EStdio.err());
        }
    }

    emethod afterRevive() {
        try {
            myAppender.writeBytes("appending to file in afterRevive\n\r");

            myAppender.flush();
            EStdio.out().println("After revive... File len is now " + 
                                 myAppendFile.length());

            myAppender.close();
        } catch (Exception e) {
            EStdio.err().println("problem in afterRevive");
            e.printStackTrace(EStdio.err());
        }
    }

    public void noticeFileReconstruction() {
        try {
            EStdio.out().println("File got reconstructed!");
            myAppender.writeBytes("File got reconstructed!!\n\r");
        } catch (Exception e) {
            EStdio.err().println("problem writing reconstruct message");
            e.printStackTrace(EStdio.err());
        }
    }

}


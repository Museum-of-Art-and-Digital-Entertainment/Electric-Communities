/* 

   rp.java - Test of PublishRepository, RepositoryPublisher,
   DataRequestor, and a half dozen other classes.

   by Kari Dubbelman 19971015

   To run this test, type to a shell (Windows or UNIX)

   % pubtest


   Based on:

   HelloComm.e -- Version 0.3 -- Simple comm test

   Arturo Bejar & Chip Morningstar
   Electric Communities
   19-February-1996
   
   Copyright 1997 Electric Communities, all rights reserved worldwide.

   Changed to new comm system:  Oct 1996, Eric Messick
   Ported to pre-Vat April 1997, Chip Morningstar */

package ec.tests.rep;

import ec.cert.CryptoHash;
import ec.e.file.EStdio;
import ec.e.hold.DataHolder;
import ec.e.hold.DataHolderSteward;
import ec.e.hold.DataRequestor;
import ec.e.hold.Fulfiller;
import ec.e.lang.EString;
import ec.e.net.ListenerInterest;
import ec.e.net.Registrar;
import ec.e.net.RegistrarException;
import ec.e.net.RegistrarLookupEException;
import ec.e.net.SturdyRef;
import ec.e.net.SturdyRefFileExporter;
import ec.e.net.SturdyRefFileImporter;
import ec.e.net.SturdyRefMaker;
import ec.e.rep.ParimeterizedRepository;
import ec.e.rep.RepositoryPublisher;
import ec.e.rep.StandardRepository;
import ec.e.rep.PublishRepository;
import ec.e.rep.steward.Repository;
import ec.e.rep.steward.RepositoryKeyNotFoundException;
import ec.e.rep.steward.SimpleRepository;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public eclass pubtest implements ELaunchable
{
    private EEnvironment myEnv;

    emethod go(EEnvironment env) {
        myEnv = env;

        String reg = env.getProperty("reg");
        String lookup = env.getProperty("lookup");
        System.out.println("Starting pubtest. reg = " + reg + " and lookup = " + lookup);
        Registrar registrar = Registrar.summon(env);
        try {
            registrar.onTheAir();
        }
        catch (RegistrarException e) {
            throw new Error("fatal registrar problem going on the air: " + e);
        }
        
        // This demo used to create a receiver that registered itself
        // and a sender that imported a SturdyRef to the receiver and
        // sent it a message. The message contained a real sturdyref
        // back to the sender and the receiver would send a message back.

        // We still do the first part. The sender/publisher (the part
        // that does the lookup and import of the first SturdyRef)
        // sends the receiver/requestor a message which contains a
        // cryptohash and a sturdyref. The sturdyref goes not back to
        // the sender but to the RepositoryPublisher that the sender
        // starts. And before this, the Sender/publisher has published
        // an object (the integer 17) into its PublishRepository.

        // The receiver carefully creates a DataHolder from the
        // SturdyRef and the cryptohash and attempts to fill it. This
        // causes a message to the RepositoryPublisher to get sent,
        // and the RepositoryPublisher finally fulfills the data
        // request (using a channel back to the receiver/requestor).

        if (reg != null) {
            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileExporter refExporter = registrar.getSturdyRefFileExporter(env);
            HelloSturdyRefReceiver receiver = new HelloSturdyRefReceiver(refMaker, refExporter, reg, myEnv);
        }
        else if (lookup != null) {
            // We are the sender/publisher. We will establish
            // connection to the receiver/requestor and tell them how
            // to request something from us. The the
            // receiver/requestor requests the data and we fulfill the
            // request.

            SturdyRefMaker refMaker = registrar.getSturdyRefMaker();
            SturdyRefFileImporter importer = registrar.getSturdyRefFileImporter(env);
            SturdyRef ref;
            SturdyRef me;
            
            try {
                ref = importer.importRef(lookup);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("problem importing reference " + lookup + ": " + e);
            }

            try {

                Object newObject = new Integer(17); // Our new object - scanned in artwork, or something.

                // Summon a PublishRepository capability

                PublishRepository publishRepository = (PublishRepository)PublishRepository.summon(env);
                System.out.println("Publisher created a publishRepository");

                // Publish our new object

                DataHolder publishedHolder = publishRepository.makeDataHolder(newObject); // Publish a random data object
                System.out.println("Publisher published the object");

                // That's all we normally do. In this test case we
                // don't pass the dataHolder to the recipient or
                // include it in the region or anything like that
                // (like we normally would in cosm). Instead, we
                // extract the cryptohash from the dataholder:

                CryptoHash hash = publishedHolder.getCryptohash(); // XXX, getCryptoHash() is misspelled in hold/

                // We would nbormally just pass the dataholder over
                // but we want to make sure the receiver/requestor
                // fulfills it from a different repository.

                RepositoryPublisher publisher = (RepositoryPublisher)RepositoryPublisher.summon(myEnv);
                System.out.println("Publisher creates RepositoryPublisher");
                SturdyRef dataRef = publisher.getSturdyRef(); // We do this instead of calling refmaker

                // Use the hello example to send the sturdyref and cryptohash to the receiver/requestor
                
                HelloSturdyRefSender sender = new HelloSturdyRefSender();
                sender <- sendHello(ref, dataRef, hash);
                System.out.println("Publisher sent the message");

            } catch (Exception e) {
                System.out.println("Publishrepository creation etc threw exception: " + e);
                e.printStackTrace();
            }
        }
        else {
            EStdio.err().println("supply reg=<relative_path_name> for server or lookup=<same_file> for client");
        }
    }
}

eclass HelloSturdyRefSender 
{
    emethod sendHello(SturdyRef requestorRef, SturdyRef publisherRef, CryptoHash hash) {
        etry {
            try {
                HelloSturdyRefReceiver otherGuy;
                requestorRef.followRef(&otherGuy);
                EStdio.out().println("Sender: sending hello()");
                otherGuy <- hello(publisherRef, hash);
            } catch (Throwable t) {
                EStdio.err().println("Sender: caught exception in lookup " +
                                     t.getMessage());
                EStdio.reportException(t);
                t.printStackTrace();
            }
        } ecatch (RegistrarLookupEException e) {
            EStdio.err().println("Sender: caught E exception in lookup "
                                 + e.getMessage());
        }
    }
}

eclass HelloSturdyRefReceiver {
    EEnvironment myEnv;

    HelloSturdyRefReceiver(SturdyRefMaker refMaker, SturdyRefFileExporter refExporter, String reg, EEnvironment env) {
        myEnv = env;
        try {
            SturdyRef ref;
            ref = refMaker.makeSturdyRef(this);
            refExporter.exportRef(ref, reg);
            EStdio.out().println("Receiver: run   java ec.e.start.EBoot ec.examples.hc.HelloSturdyRef lookup=" + reg);
        } catch (Throwable t) {
            EStdio.err().println("Receiver: caught exception in startup " +
                                 t.getMessage());
            EStdio.reportException(t);
            t.printStackTrace();
        }
    }

    /**

     * The receiver is a different vat. It gets its
     * environment directly from startup but it receives the
     * cryptohash and sturdyref as arguments.

     * It creates a Java object, the RepositoryRequestorTester, that
     * constructs a dataholder from the pieces (hash and reference)
     * and then attempts to fill it asynchrnously.

     * We could probably pass in the DataHolder we got earlier but I
     * wanted to first prove that the dataholder (here) looks the way
     * I expect it to look.

     * After this test I'll test sending the dataholder and fulfilling
     * it in the receiver.

     */

    emethod hello(SturdyRef publisherRef, CryptoHash hash) {
        System.out.println("Creating new RepositoryRequestorTester");
        RepositoryRequestorTester requestor = new RepositoryRequestorTester(publisherRef,hash,myEnv);
    }
}

public class RepositoryRequestorTester implements DataRequestor {
    private EEnvironment myEnv;
    private Fulfiller theFulfiller;
    private SturdyRef publisherRef;      // Ref we use for testing.
    
    // Make an object without a Repository that asks to have DataHolder filled in.

    public RepositoryRequestorTester(SturdyRef publisherRef, CryptoHash hash, EEnvironment env) {
        System.out.println("Constructing RepositoryRequestorTester for hash " + hash);
        myEnv = env;

        try {

        // We cannot just passs in the DataHolder since that would
        // guarantee we would get the data from the existing
        // Repository. We need to make the DataHolder from its parts
        // to make sure it works. Good thing we normally don't make
        // dataholders from scratch since we need lots of ingredients:

        // A ParimeterizedRepository

        ParimeterizedRepository myRep = (ParimeterizedRepository)ParimeterizedRepository.summon(env);
        System.out.println("Requestor created ParimeterizedRepository");

        // A hints vector

        Vector hints = new Vector(1);
        
        // hints contains the SturdyRef

        hints.addElement(publisherRef);

        // We can now make a Fulfiller.

        Fulfiller myFulfiller = new Fulfiller(myRep,null,hints);

        // And with the fulfiller and a CryptoHash we can make a DataHolder.

        DataHolder myDataHolder = new DataHolderSteward(hash, myFulfiller, null);
        System.out.println("Requestor created DataHolder");

        // This is the situation we'd normally be in in cosm. We are
        // given a dataholder and we ask for the data for it.

        myDataHolder.giveDataTo(this);
        System.out.println("Requestor requested DataHolder to be filled");

        } catch (Exception e) {
            System.out.println("Unexpected exception in RepositoryRequestor: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void acceptByteData(byte[] data, DataRequestor holder) {
        System.out.println("RepositoryRequestorTester success! Byte data is " + data);
    }

    public void acceptData(Object object, DataRequestor holder) {
        System.out.println("RepositoryRequestorTester success! Result object is " + object);
    }

    public void handleFailure(Exception e, DataRequestor holder) {
        System.out.println("RepositoryRequestorTester failure! Exception is " + e);
    }
}



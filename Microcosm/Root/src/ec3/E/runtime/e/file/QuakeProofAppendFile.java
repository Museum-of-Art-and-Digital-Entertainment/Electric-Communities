package ec.e.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import ec.e.start.Vat;
import ec.e.start.Tether;
import ec.e.start.SmashedException;

/* 
 * QuakeProofAppendFileListner - interface for
 * someone that has a QuakeProofAppendFile to
 * find out when quakes occur on the file.  The
 * listner can then take some action, such as
 * writing a record to the file to indicate that
 * a quake occured.
 */

public interface QuakeProofAppendFileListner {
    /**
     * noticeFileReconstruction -- this is called 
     * when a quake occurs and the file is reconstructed.
     */
    public void noticeFileReconstruction();
}

/*
 * QuakeProofAppendableFile - like an appendable file, but
 * withstands quakes.  The semantics for recovery is that the
 * file is reopened, and seek to the end is performed.
 * Note that semantics of Appendable file are different than
 * normal files: When returning from a quake we will always be
 * writing to the end of the file.  Hence this object can
 * hide the details of quake recovery.
 */

public class QuakeProofAppendFile extends OutputStream {
    private Vat              myVat;
    private String           myFilenamePath;
    private File             myFile;
    private QuakeProofAppendFileListner myReconstructListner = null;
    private Tether /* FileOutputStream */ myFileOutStreamHolder;
    private static Trace tr = new Trace("quakeproofappendfile");
    /**
     * QuakeProofAppendFile -- creates an appendable file in
     * directory path "path" with name "filename".  
     *
     * (Note: a constructor of the form (Vat, File) doesn't quite make
     * sense since recovery will have to be done by re-creating the
     * File object.  For consistency, we'll require path/filename
     * for object construction.)
     *
     * @param Vat vat -- Our vat.
     * @param String path -- Directory that the file will be created in. (null not ok)
     * @param String filename -- The name of the file to create. (null not ok)
     * 
     * @throws IOException -- Thrown when there is an io error.
     */
    public QuakeProofAppendFile(Vat vat, String path, String filename) throws IOException {
        
        myVat = vat;

        File file = new File(path, filename);
        myFilenamePath = file.getPath();

        this.reconstruct();
    }

    /**
     * setReconstructListner -- sets the listner that will be
     * invokes when the file is reconstructed.  (Note: there
     * can only be one -- there should probably only be one
     * user of the file anyhow.)
     *
     * @param QuakeProofAppendFileListner listner -- the object that
     *                                               will be notified.
     * note: null is ok -- this will just 'clear' the listner.
     */
    public void setReconstructListner(QuakeProofAppendFileListner listner) {
        if (tr.debug && Trace.ON) tr.debugm("setting listner to " + listner);
        myReconstructListner = listner;
    }

    /**
     * getPath() -- return the path of the underlying File used by this
     * quakeProof Append log.
     */
    public String getPath() {
        return myFilenamePath;
    }

    /* reconstruct() is invoked whenever there is a quake -- ie .held
     * throws a SmashedException.  reconstruct() reconstructs the 
     * fragile bits of the AppendFile: it creates a new FileOutputStream
     * in "append" mode.
     */
    private void reconstruct() throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("reconstructing QuakeProofAppendFile " + myFilenamePath);
        myFileOutStreamHolder = new Tether(myVat,
                                 new FileOutputStream(myFilenamePath, true));
        if (myReconstructListner != null) {
            tr.debugm("calling listener ");
            myReconstructListner.noticeFileReconstruction();
        }
    }

    private FileOutputStream getMyStream() throws IOException {
        try {
            return (FileOutputStream) myFileOutStreamHolder.held();
        } catch (SmashedException e) {
            this.reconstruct();
            return (FileOutputStream) myFileOutStreamHolder.held();
        }
    }

    /**
     * Returns the current length of the underlying file.
     * 
     * @throws IOException      If an I/O error occurs
     */
    public long length() throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("doing length in " + myFilenamePath);
        // Note: This seems like a round about way to just get the length.
        // From looking at the underlying code in getFD() and File, it
        // appears that it should be quick.

        // Why do we need to do the sync, and then create the File?
        // The sync actually causes bytes to get written out
        // to disk.  Afaik, File is the only way to get the length.
        // (fyi, this.flush(stream) does the work of sync'in the fd.)

        FileOutputStream stream = this.getMyStream();
        this.flush(stream);
        File file = new File(myFilenamePath);
        return file.length();
    }

    /**
    * Closes this stream and releases any system resources associated with it,
    * by calling the stream's close method.
    *
    * @throws IOException If an I/O error occurs.
    */
    public void close() throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("closing " + myFilenamePath);
        FileOutputStream stream = this.getMyStream();
        stream.close();
        myReconstructListner = null;
    }

    /**
    * Writes the specified byte to this stream.
    *
    * @param b The byte to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(int b) throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("doing write in " + myFilenamePath);
        FileOutputStream stream = this.getMyStream();
        stream.write(b);
    }

    /**
    * Writes buf.length bytes from the specified byte array to this stream.
    *
    * @param buf The bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[]) throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("doing write in " + myFilenamePath);
        FileOutputStream stream = this.getMyStream();
        stream.write(buf);
    }

    /**
    * Writes length bytes from the specified byte array starting at offset to
    * this stream.
    *
    * @param buf The bytes to be written.
    * @param offset The start offset (in buf) of the data.
    * @param length The number of bytes to be written.
    * @throws IOException If an I/O error occurs.
    */
    public void write(byte buf[], int offset, int length) throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("doing write in " + myFilenamePath);
        FileOutputStream stream = this.getMyStream();
        stream.write(buf, offset, length);
    }

    private void flush(FileOutputStream stream) throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("flushing stream " + myFilenamePath);
        FileDescriptor fd = stream.getFD();
        fd.sync();
    }

    public void flush() throws IOException { 
        if (tr.debug && Trace.ON) tr.debugm("flushing me " + myFilenamePath);
        FileOutputStream stream = this.getMyStream();
        this.flush(stream);
    }

    /**
    * Finalize by closing.
    */
    protected void finalize() throws IOException {
        if (tr.debug && Trace.ON) tr.debugm("Finalize called in QuakeProofAppendFile " + myFilenamePath);
        close();    
    }
}

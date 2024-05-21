// FileChooser.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Object subclass allowing the user to select a file.  FileChooser creates
  * a a modal window, blocking the calling thread and remaining onscreen until
  * the user dismisses it.
  */
public class FileChooser extends Object {
    java.awt.FileDialog         awtDialog;
    int                         type;

    /** Type used to create a FileChooser that loads files. */
    public final static int     LOAD_TYPE = 0;
    /** Type used to create a FileChooser that saves files. */
    public final static int     SAVE_TYPE = 1;



/* constructors */

    /** Constructs a FileChooser associated with the RootView
      * <b>rootView</b>, with title <b>title</b> and type <b>type</b>.
      * @see RootView
      * @see Application
      */
    public FileChooser(RootView rootView, String title, int type) {
        int     mode;

        if (rootView == null) {
            throw new InconsistencyException("No rootView for FileChooser");
        }

        this.type = type;
        if (type == SAVE_TYPE) {
            mode = java.awt.FileDialog.SAVE;
        } else {
            mode = java.awt.FileDialog.LOAD;
        }

        awtDialog = new java.awt.FileDialog(rootView.panel().frame(),
                                            title, mode);
    }

/* actions */

    /** Returns the FileChooser's type, either LOAD_TYPE or SAVE_TYPE. */
    public int type() {
        return type;
    }

    /** Sets the FileChooser to the specified directory. */
    public void setDirectory(String directory) {
        awtDialog.setDirectory(directory);
    }

    /** Returns the FileChooser's current directory.
      * @see #setDirectory
      */
    public String directory() {
        return awtDialog.getDirectory();
    }

    /** Sets the FileChooser to the specified file. */
    public void setFile(String filePath) {
        awtDialog.setFile(filePath);
    }

    /** Returns the FileChooser's current file.
      * @see #setFile
      */
    public String file() {
        return awtDialog.getFile();
    }

    /** Sets the filename filter.
      */
    public void setFilenameFilter(java.io.FilenameFilter aFilter) {
        awtDialog.setFilenameFilter(aFilter);
    }

    /** Returns the filename filter.
      * @see #setFilenameFilter
      */
    public java.io.FilenameFilter filenameFilter() {
        return awtDialog.getFilenameFilter();
    }

    /** Sets the FileChooser's Window's title. */
    public void setTitle(String title) {
        awtDialog.setTitle(title);
    }

    /** Returns the FileChooser's Window's title.
      * @see #setTitle
      */
    public String title() {
        return awtDialog.getTitle();
    }

    /** Brings the FileChooser onscreen. */
    public void showModally() {
        awtDialog.show();
    }
}

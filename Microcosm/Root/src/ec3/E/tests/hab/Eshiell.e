package ec.tests.hab;

import java.util.Vector;
import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import ec.e.file.EEditableDirectory;
import ec.e.file.EEditableFile;
import ec.e.rep.steward.Repository;
import ec.e.io.*;
import ec.e.hab.*;

/**
  An interactive shell for testing the Haberdasher and fiddling with the
  Haberdashery.
*/
public eclass Eshiell implements EInputHandler {
    static Trace t = new Trace("ec.tests.hab.Eshiell"); /* XXX debug only */

    private PrintStream out;                /* program output */
    private Haberdashery hab = null;        /* The Haberdashery... */
    private String habFile = null;          /* ...where it is */
    private Unit cwu = null;                /* Current Working Unit... */
    private BasedDesignator cwuDes = null;  /* ...designator of same */
    private EEditableDirectory myRootDir;   /* Directory root capability */

    /**
    * Construct a new shell. You specify the input and output streams
    * so that maybe someday this won't need to use the console.  You
    * also need to provide a directory capability so we can create and
    * read Haberdashery-Repository files.
    */
    public Eshiell(PrintStream out, EEditableDirectory rootDir) {
        this.out = out;
        this.myRootDir = rootDir;
    }

    emethod handleInput (String line) {
        parseNextCommand(line);
    }

    /********** Command line processing methods **********/

    /**
    * Parse a string representation of an object designator into an actual
    * object designator.
    *
    * @param str The String to be parsed.
    * @return A fully fleshed BasedDesignator
    */
    private BasedDesignator parseDesignator(String str) {
        if (str.equals(".")) { /* root */
            return(new BasedDesignator(Designator.getRoot()));
        } else if (str.equals("?")) { /* unknown */
            return(new BasedDesignator(Designator.getUnknown()));
        } else if (str.equals("0")) { /* null */
            return(new BasedDesignator(Designator.getNull()));
        } else if (str.startsWith("/")) { /* hashkey or hashkey rooted FQN */
            int delim = str.indexOf('.');
            if (delim < 0) { /* hashkey */
                return(new BasedDesignator(
                    new HashkeyDesignator(parseHashkey(str.substring(1)))));
            } else { /* hashkey rooted FQN */
                return(new BasedDesignator(
                    (Designator) new PathDesignator(str.substring(delim + 1)),
                    (DirectDesignator) new HashkeyDesignator(
                        parseHashkey(str.substring(1, delim)))));
            }
        } else if (str.startsWith(".")) { /* rooted FQN */
            return(new BasedDesignator(
                (Designator) new PathDesignator(str.substring(1)),
                (DirectDesignator) Designator.getRoot()));
        } else { /* normal FQN */
            if (cwuDes.isDirect()) {
                return(new BasedDesignator(
                    (Designator) new PathDesignator(str),
                    (DirectDesignator) cwuDes.getDesignator()));
            } else {
                return(new BasedDesignator(
                    (Designator) new PathDesignator(
                        (PathDesignator)cwuDes.getDesignator(), str),
                    cwuDes.getBase()));
            }
        }
    }

    /**
    * Parse a hex string into a hashkey.
    *
    * @param str The hex string to be parsed.
    * @return A byte array representing the resulting hashkey.
    */
    private byte[] parseHashkey(String str) {
        int keyLen = str.length() / 2 + str.length() % 2;
        boolean upperNybble = true;
        byte key[] = new byte[keyLen];

        for (int i=0; i<keyLen; ++i)
            key[i] = 0;
        str = str.toLowerCase();
        for (int i=0; i<str.length(); ++i) {
            char c = str.charAt(i);
            byte b = 0;
            if ('a' <= c && c <= 'f')
                b = (byte)(c - 'a' + 10);
            else if ('0' <= c && c <= '9')
                b = (byte)(c - '0');
            else
                simpleError("malformed hashkey /" + str);
            if (upperNybble)
                key[i/2] = (byte) (b << 4);
            else
                key[i/2] |= b;
            upperNybble = !upperNybble;
        }
        return(key);
    }

    /**
    * Parse a line String into a series of whitespace-delimited substrings
    * and return them as an array of Strings, in the mode of Unix shells.
    * We are pretty stupid about this, as we don't need to recognize literal
    * strings (i.e., things in quotation marks) or escape sequences.
    *
    * @param line The line to be parsed
    * @return An array of the whitespace-delimited elements of line
    */
    private String[] parseLine(String line) {
        Vector resultVector = new Vector();
        line = clean(line).trim();
        while (!line.equals("")) {
            String newElem;
            int delim = line.indexOf(' ');
            if (delim < 0) {
                newElem = line;
                line = "";
            } else {
                newElem = line.substring(0, delim);
                line = line.substring(delim).trim();
            }
            resultVector.addElement(newElem);
        }
        String[] result = new String[resultVector.size()];
        resultVector.copyInto(result);
        return(result);
    }

    /**
    * Parse an input line, treat it as a Haberdashiell command, and
    * execute it.
    *
    * @param line The line to be processed
    */

    private void parseNextCommand(String line) {
        if (line == null) /* EOF on input appears as null line */
            cmdQuit(null);
        String argv[] = parseLine(line);
        if (argv.length > 0) {
            String cmd = argv[0];
            if (cmd.equals("abort"))
                cmdAbort(argv);
            else if (cmd.equals("add"))
                cmdAdd(argv);
            else if (cmd.equals("cd"))
                cmdCd(argv);
            else if (cmd.equals("close"))
                cmdClose(argv);
            else if (cmd.equals("delete"))
                cmdDelete(argv);
            else if (cmd.equals("dump"))
                cmdDump(argv);
            else if (cmd.equals("eat"))
                cmdEat(argv);
            else if (cmd.equals("fetch"))
                cmdFetch(argv);
            else if (cmd.equals("help") || cmd.equals("?"))
                cmdHelp(argv);
            else if (cmd.equals("info"))
                cmdInfo(argv);
            else if (cmd.equals("init"))
                cmdInit(argv);
            else if (cmd.equals("inspect"))
                cmdInspect(argv);
            else if (cmd.equals("intern"))
                cmdIntern(argv);
            else if (cmd.equals("ls"))
                cmdLs(argv);
            else if (cmd.equals("mkdir"))
                cmdMkdir(argv);
            else if (cmd.equals("open"))
                cmdOpen(argv);
            else if (cmd.equals("pwd"))
                cmdPwd(argv);
            else if (cmd.equals("quit"))
                cmdQuit(argv);
            else if (cmd.equals("rm"))
                cmdRm(argv);
            else
                cmdUnknown(argv);
        }
        prompt();
    }

    /**
    * Prompt the user for the next command. This should probably be made less
    * flippant.
    */
    private void prompt() {
        out.print("whaddya want?> ");
        out.flush();
    }

    /********** Error and exception handling methods **********/

    /**
    * Assert that a Haberdashery must be open. Abort current command if not.
    */
    private void ensureOpenHaberdashery() {
        if (hab == null)
            simpleError("there is no Haberdashery currently open");
    }

    /**
    * Assert that we have the right number of args. Abort current command if
    * we don't.
    *
    * @param argv The arguments array.
    * @param minCount The minimum acceptable number of arguments.
    * @param maxCount The maximum acceptable number of arguments.
    * @param syntax A string describing the proper usage syntax to the user.
    */
    private void ensureUsage(String argv[], int minCount, int maxCount,
                             String syntax) {
        if (argv.length < minCount + 1 || maxCount + 1 < argv.length) {
            out.println("usage: " + syntax);
            throw new DashiellNonFatalErrorException();
        }
    }

    /**
    * Choke and die with an appropriate message. Invoked when we catch an
    * unrecoverable error exception. For now, dump the stack trace for
    * diagnositic purposes.
    *
    * @param e The exception that lead to our being here today.
    * @param msg A descriptive error message to give to the user.
    */
    private void fatalException(Exception e, String msg) {
        out.println("fatal error: " + msg);
        e.printStackTrace(out);
        throw new DashiellFatalErrorException();
    }

    /**
    * Abort current command and print an error message associated with a
    * non-fatal exception. Invoked when we catch a recoverable error exception.
    * For now, dump the stack trace for diagnostic purposes.
    *
    * @param e The exception that lead to our being here today.
    * @param msg A descriptive error message to give to the user.
    */
    private void nonFatalException(Exception e, String msg) {
        out.println("error: " + msg);
        e.printStackTrace(out);
        throw new DashiellNonFatalErrorException();
    }

    /**
    * Abort current command and print an error message associated with an
    * ordinary user level boo-boo.
    *
    * @param msg A descriptive error message to give to the user.
    */
    private void simpleError(String msg) {
        out.println("error: " + msg);
        throw new DashiellNonFatalErrorException();
    }

    /********** General grungy utilities, output format junk, etc. **********/

    /**
    * Turn all the whitespace characters in a String into spaces. This
    * shouldn't be necessary, but it is, due to inadequacies in Java's String
    * class API.
    *
    * @param str The string to be cleaned
    * @return The cleaned string
    */
    private String clean(String str) {
        char strChars[] = new char[str.length()];
        str.getChars(0, str.length(), strChars, 0);
        for (int i=0; i<str.length(); ++i)
            if (Character.isSpace(strChars[i]))
                strChars[i] = ' ';
        return(new String(strChars));
    }

    /**
    * Generate the indentation whitespace for a given tab stop.
    *
    * @param depth Indentation depth.
    * @return A whitespace string that will indent 'depth' tab stops.
    */
    private String indent(int depth) {
        String result = "";
        while (depth-- > 0)
            result += "  ";
        return(result);
    }

    /**
    * Generate a legible version of the class name returned by the Class
    * 'getName' method.
    *
    * @param className A class name as returned by Class.getName().
    * @return A more useful version of the class name.
    */
    private String prettyClassName(String className) {
        if (className.charAt(0) == '[')
            return(prettyClassName(className.substring(1)) + "[]");
        else if (className.equals("B"))
            return("byte");
        else if (className.equals("C"))
            return("char");
        else if (className.equals("D"))
            return("double");
        else if (className.equals("F"))
            return("float");
        else if (className.equals("I"))
            return("int");
        else if (className.equals("J"))
            return("long");
        else if (className.equals("S"))
            return("short");
        else if (className.equals("Z"))
            return("boolean");
        else
            return(className);
    }

    /**
    * Generate a pretty descriptive string for a unit entry or other
    * designator.
    *
    * @param des The Designator of the object of interest.
    * @param obj The object of interest itself.
    * @param name A name for the object of interest.
    * @return A pretty descriptive string for the object of interest
    */
    private String unitElemString(BasedDesignator des, Object obj,
                                  String name) {
        String result = des.toString();
        if (!name.equals(""))
            result += " " + name;
        if (obj == null) {
            result += " <unknown>";
        } else if (obj instanceof Unit) {
            result += " (unit)";
        } else {
            result += " (";
            if (obj instanceof NonHaberdasheryObjectHolder) {
                obj = ((NonHaberdasheryObjectHolder) obj).getHeldObject();
                result += "->";
            }
            result += prettyClassName(obj.getClass().getName()) + ")";
        }
        return(result);
    }

    /********** Command execution utility methods **********/

    /**
    * Add a mapping to the current working unit, resulting in a new unit
    * which becomes the new current working unit. If the current working unit
    * designator is indirect, unit changes are propagated up the tree to the
    * cwu's path base. If the cwu is based at root, this will preserve the
    * illusion of a mutable hierarchical directory structure. If the cwu is
    * not based at root, a new tree will appear to have been created. In either
    * case, the upwardly propagated revised base will become the base of the
    * new cwu (in the case of root, this is still root, since root is mutable).
    *
    * @param name The name whose mapping is to be added
    * @param des The designator to which the name is to map
    */
    private void addMapping(String name, BasedDesignator des) {
        if (des.isDirect()) {
            try {
                DirectDesignator newCwuRoot =
                    doAddMapping(name, (DirectDesignator)des.getDesignator(),
                                 cwu, cwuDes, cwuDes.isRooted());
                cwuDes = cwuDes.rebase(newCwuRoot);
                cwu = (Unit) cwuDes.get(hab);
                ec.e.inspect.Inspector.gather(cwu, "CWU", name);
            } catch (HaberdasherException e) {
                simpleError("add mapping failed -- " + e.getMessage());
            } catch (IOException e) {
                simpleError("add mapping failed with an IOException -- " + e.getMessage());
            }
        } else {
            simpleError("only DirectDesignators may be added to units");
        }
    }

    /**
    * Recursively ascend the unit hierarchy, replacing each unit until you
    * reach the root or base. Note that this method makes a single change at
    * the tail of a path and then propagates the resulting unit alterations
    * upward. If you are going to do a lot of unit manipulation at one sitting,
    * (say, add or remove a whole bunch of stuff), you probably should write a
    * new method that makes all the changes and then propagates them up in the
    * aggregate. Doing such an operation by calling this method repeatedly will
    * generate a lot of garbage units.
    *
    * @param name The name mapping to be added or changed at this level.
    * @param des The DirectDesignator to map name do (null=>remove mapping).
    * @param unit The unit which is to have it's mappings altered.
    * @param unitDes The (indirect) designator of unit.
    * @param purgeFlag If true, old versions of changed units will be deleted.
    * @return The DirectDesignator of the new root unit.
    */
    private DirectDesignator doAddMapping(String name, DirectDesignator des,
            Unit unit, BasedDesignator unitDes, boolean purgeFlag) throws IOException {
        Unit newUnit;
        if (des == null)
            newUnit = unit.copyWithout(name);
        else
            newUnit = unit.copyWith(name, des);
        if (unitDes.getDesignator().isRoot()) {
            hab.putRoot(newUnit);
            return((DirectDesignator) unitDes.getDesignator());
        } else {
            DirectDesignator newUnitDes = hab.put(newUnit);
            if (!unitDes.isDirect()) {
                BasedDesignator parentUnitDes = unitDes.parent();
                Unit parentUnit = (Unit) parentUnitDes.get(hab);
                String newUnitName = unitDes.childName();
                if (purgeFlag)
                    unitDes.delete(hab);
                return(doAddMapping(newUnitName, newUnitDes, parentUnit,
                                    parentUnitDes, purgeFlag));
            } else {
                return(newUnitDes);
            }
        }
    }

    /**
    * Cd to the root unit.
    */
    private void cdRoot() {
        try {
            cwuDes = new BasedDesignator(Designator.getRoot());
            cwu = (Unit) cwuDes.get(hab);
        } catch (Exception e) {
            fatalException(e, "can't cd to root");
        }
    }

    /**
    * Close up the currently open Haberdashery and reset all the relevant
    * associated pointers.
    */
    private void closeCurrentHaberdashery() {
        try {
            hab.close();
        } catch (Exception e) {
            fatalException(e, "attempt to close currently open Haberdashery " +
                           habFile + " failed.");
        }
        hab = null;
        habFile = null;
        cwu = null;
        cwuDes = null;
    }

    /**
    * Turn the bytes contained by an external file into an object in the
    * Haberdashery.
    *
    * @param filename The name of the file containing the data
    * @return A BasedDesignator for the new HaberdasheryObject
    */
    private BasedDesignator internFile(String filename) {
        try {
            File file = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            long size = file.length();
            byte data[] = new byte[(int)size];
            fileInputStream.read(data);
            fileInputStream.close();
            Designator newDes = hab.put(new NonHaberdasheryObjectHolder(data));
            return(new BasedDesignator(newDes));
        } catch (HaberdasherException e) {
            simpleError("intern failed -- " + e.getMessage());
        } catch (FileNotFoundException e) {
            simpleError("file " + filename + " not found");
        } catch (IOException e) {
            nonFatalException(e, "can't read " + filename);
        }
        return(null); /* Never reached, just makes the compiler happy */
    }

    /**
    * Print the contents of a unit or other Haberdashery object info.
    *
    * @param des Designator of the item of interest
    * @param infoFlag true->print unit info, false->print unit contents
    * @param recurseFlag true->recursively walk unit tree
    * @param depth Recursion depth (for pretty indentation)
    */
    private void ls(BasedDesignator des, boolean infoFlag, boolean recurseFlag,
                    int depth, String name) {
        try {
            HaberdasheryObject lsObj = des.get(hab);
            out.println(indent(depth) + unitElemString(des, lsObj, name));
            if (lsObj instanceof Unit && !infoFlag) {
                Unit unit = (Unit) lsObj;
                Enumeration contents = unit.designators();
                Enumeration names = unit.names();
                while (contents.hasMoreElements()) {
                    DirectDesignator elem =
                        (DirectDesignator) contents.nextElement();
                    String subName = (String) names.nextElement();
                    ls(new BasedDesignator(elem), !recurseFlag, recurseFlag,
                       depth + 1, subName);
                }
            }
        } catch (HaberdasherException e) {
            out.println(indent(depth) + unitElemString(des, null, name));
        }
    }

    /**
    * Print current working unit.
    */
    private void pwu() {
        out.println(unitElemString(cwuDes, cwu, "<-cwu"));
    }

    /**
    * Remove a mapping from the current working unit, resulting in a new unit
    * which becomes the new current working unit. If the current working unit
    * designator is indirect, unit changes are propagated up the tree to the
    * cwu's path base. If the cwu is based at root, this will preserve the
    * illusion of a mutable hierarchical directory structure. If the cwu is
    * not based at root, a new tree will appear to have been created. In either
    * case, the upwardly propagated revised base will become the base of the
    * new cwu (in the case of root, this is still root, since root is mutable).
    *
    * @param name The name whose mapping is to be removed
    */
    private void removeMapping(String name) {
        try {
            DirectDesignator newCwuRoot =
                doAddMapping(name, null, cwu, cwuDes, cwuDes.isRooted());
            cwuDes = cwuDes.rebase(newCwuRoot);
            cwu = (Unit) cwuDes.get(hab);
        } catch (HaberdasherException e) {
            simpleError("remove mapping failed -- " + e.getMessage());
        } catch (IOException e) {
            simpleError("remove mapping failed with an IOException -- " + e.getMessage());
        }
    }

    /********** The commands themselves **********/

    /**
    * Execute Dashiell's "abort" command.
    * Quit without saving.
    */
    private void cmdAbort(String argv[]) {
        ensureUsage(argv, 0, 0, "abort");
        if (hab != null)
            out.println("warning, exiting without committing changes");
        throw new DashiellFatalErrorException();
    }

    /**
    * Execute Dashiell's "add" command.
    * Add a new entry to the current working unit.
    */
    private void cmdAdd(String argv[]) {
        ensureUsage(argv, 2, 2, "add <name> <des>");
        ensureOpenHaberdashery();
        addMapping(argv[1], parseDesignator(argv[2]));
        pwu();
    }

    /**
    * Execute Dashiell's "cd" command.
    * Be sure that the proposed new current working unit is really there and
    * really a unit before switching to it!
    */
    private void cmdCd(String argv[]) {
        ensureUsage(argv, 1, 1, "cd <des>");
        ensureOpenHaberdashery();
        BasedDesignator newUnitDes = parseDesignator(argv[1]);
        HaberdasheryObject newUnitObj = null;
        try {
            newUnitObj = newUnitDes.get(hab);
        } catch (HaberdasherException e) {
            simpleError("cd failed -- " + e.getMessage());
        }
        if (newUnitObj instanceof Unit) {
            cwuDes = newUnitDes;/*.determine(hab);*/
            cwu = (Unit) newUnitObj;
        } else {
            simpleError("" + newUnitDes + " is not a unit");
        }
    }

    /**
    * Execute Dashiell's "close" command.
    * Close the currently open Haberdashery.
    */
    private void cmdClose(String argv[]) {
        ensureUsage(argv, 0, 0, "close");
        ensureOpenHaberdashery();
        closeCurrentHaberdashery();
    }

    /**
    * Execute Dashiell's "delete" command.
    * Remove an object from the Haberdashery.
    */
    private void cmdDelete(String argv[]) {
        ensureUsage(argv, 1, 1, "delete <des>");
        ensureOpenHaberdashery();
        BasedDesignator delDes = parseDesignator(argv[1]);
        try {
            delDes.delete(hab);
        } catch (HaberdasherException e) {
            simpleError("delete failed -- " + e.getMessage());
        }
    }

    /**
    * Execute Dashiell's "dump" command.
    * Print a list of all the objects in the Haberdashery.
    */
    private void cmdDump(String argv[]) {
        ensureUsage(argv, 0, 0, "dump");
        ensureOpenHaberdashery();
        try {
            Enumeration elements = hab.elements();
            while (elements.hasMoreElements()) {
                Object elem = elements.nextElement();
                if (elem instanceof byte[]) {
                    BasedDesignator des = new BasedDesignator(
                        new HashkeyDesignator((byte [])elem));
                    HaberdasheryObject obj = des.get(hab);
                    out.println(unitElemString(des, obj, ""));
                } else if (elem instanceof String) {
                    String elemString = (String) elem;
                    if (elemString.equals(RootDesignator.getRootKey()))
                        out.println(".");
                    else
                        out.println(elemString);
                } else {
                    out.println(elem.toString());
                }
            }
        } catch (HaberdasherException e) {
            simpleError("dump failed -- " + e.getMessage());
        }
    }

    /**
    * Execute Dashiell's "eat" command.
    * This is equivalent to an intern followed by an add.
    */
    private void cmdEat(String argv[]) {
        ensureUsage(argv, 1, 2, "eat <filename> [<name>]");
        ensureOpenHaberdashery();
        String filename = argv[1];
        String name;
        if (argv.length == 3)
            name = argv[2];
        else
            name = filename;
        BasedDesignator newObjDes = internFile(filename);
        addMapping(name, newObjDes);
        out.println(newObjDes.toString());
        pwu();
    }

    /**
    * Execute Dashiell's "fetch" command.
    * Fetch an object from the Haberdashery and write it to an external file.
    * For now, this only works with NonHaberdasheryObjectHolder objects which
    * are holding onto byte arrays (i.e., the sort of object generated when you
    * intern an external file).
    */
    private void cmdFetch(String argv[]) {
        ensureUsage(argv, 2, 2, "fetch <des> <filename>");
        ensureOpenHaberdashery();
        BasedDesignator des = parseDesignator(argv[1]);
        String filename = argv[2];
        try {
            HaberdasheryObject obj = des.get(hab);
            if (obj instanceof NonHaberdasheryObjectHolder) {
                Object data =
                    ((NonHaberdasheryObjectHolder) obj).getHeldObject();
                if (data instanceof byte[]) {
                    File file = new File(filename);
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    fileOutput.write((byte [])data);
                    fileOutput.close();
                } else {
                    simpleError("can only fetch byte array objects");
                }
            } else {
                simpleError("can only fetch non-Haberdashery object holders");
            }
        } catch (HaberdasherException e) {
            simpleError("fetch failed -- " + e.getMessage());
        } catch (IOException e) {
            nonFatalException(e, "can't write " + filename);
        }
    }

    /**
    * Execute Dashiell's "help" command.
    * Basically, just print a big message.
    */
    private void cmdHelp(String argv[]) {
        out.println("Available commands are:\n" +
"abort                 -- Exit immediately without saving\n" +
"add <name> <hashkey>  -- In current working unit map <name> to <hashkey>\n" +
"cd <des>              -- Change the current working unit to <des>\n" +
"close                 -- Close the currently open Haberdashery\n" +
"delete <des>          -- Remove the object <des> from the Haberdashery\n" +
"dump                  -- Print hashkeys & types of all objects\n" +
"eat <file> [<name>]   -- Intern then add; <name> defaults to <file>\n" +
"fetch <des> <file>    -- Retrieve <des> and store its bytes in <file>\n" +
"help                  -- Print this helpful command description\n" +
"info                  -- Print info about the currently open Haberdashery\n" +
"inspect <des>         -- Inspect object <des>\n" +
"init                  -- Initialize the currently open Haberdashery\n" +
"intern <file>         -- Get <file> into Haberdashery; print the hashkey\n" +
"ls [<flags>] [<des>]  -- List entries in unit <des> (default is current\n" +
"                         working unit), or print info about object <des>.\n" +
"                         Flags can be:\n" +
"                            -d -- List object info, even if it's a unit\n" +
"                            -R -- Recursively walk unit reference graph\n" +
"mkdir [<name>]        -- Create an empty unit & print its hashkey; <name>\n" +
"                         is it's optional name in current working unit\n" +
"open <file>           -- Open the Haberdashery in <file>\n" +
"pwd                   -- Print the hashkey of the current working unit\n" +
"quit                  -- Exit Haberdashiell\n" +
"rm <name>             -- Remove <name> from the current working unit\n" +
"\n" +
"<des> is an object designator. Possible formats are:\n" +
"   /<hexdigits>       -- A hashkey\n" +
"   <name>[.<name>]*   -- An FQN relative to current working unit\n" +
"   .<name>[.<name>]*  -- An FQN relative to the root unit\n" +
"   .                  -- The root unit\n" +
"   ?                  -- The unknown unit\n" +
"   0                  -- The null unit\n");
    }

    /**
    * Execute Dashiell's "info" command.
    * For now we're just going to print the name of the Haberdashery file (if
    * there is one). Eventually there should be more info dumped out here.
    */
    private void cmdInfo(String argv[]) {
        ensureUsage(argv, 0, 0, "info");
        if (hab == null) {
            out.println("no Haberdashery is currently open");
        } else {
            out.println("the current Haberdashery is in the file '" +
                        habFile + "'");
        }
    }

    /**
    * Execute Dashiell's "init" command.
    * Change to the (new) root unit after doing so.
    */
    private void cmdInit(String argv[]) {
        ensureUsage(argv, 0, 0, "init");
        ensureOpenHaberdashery();
        try {
            hab.init();
        } catch (Exception e) {
            fatalException(e, "Haberdashery init failed");
        }
        cdRoot();
    }

    /**
    * Execute Dashiell's "inspect" command.
    * Read in an object and inspect it.
    */
    private void cmdInspect(String argv[]) {
        ensureUsage(argv, 1, 1, "inspect <des>");
        ensureOpenHaberdashery();
        BasedDesignator inspDes = parseDesignator(argv[1]);
        try {
            Object inspectedObject = inspDes.get(hab);
            ec.e.inspect.Inspector.inspect(inspectedObject, argv[1]);
        } catch (HaberdasherException e) {
            simpleError("inspect failed -- " + e.getMessage());
        }
    }

    /**
    * Execute Dashiell's "intern" command.
    * Take the contents of an external file and turn it into an object in the
    * Haberdashery.
    */
    private void cmdIntern(String argv[]) {
        ensureUsage(argv, 1, 1, "intern <filename>");
        ensureOpenHaberdashery();
        out.println(internFile(argv[1]).toString());
    }

    /**
    * Execute Dashiell's "ls" command.
    * Print the contents of a unit or info about a given object in the
    * Haberdashery.
    */
    private void cmdLs(String argv[]) {
        ensureUsage(argv, 0, 3, "ls [-d] [-R] [<des>]");
        ensureOpenHaberdashery();
        boolean infoFlag = false;
        boolean recurseFlag = false;
        BasedDesignator des = null;
        for (int i=1; i<argv.length; ++i) {
            if (argv[i].equals("-d"))
                infoFlag = true;
            else if (argv[i].equals("-R"))
                recurseFlag = true;
            else
                des = parseDesignator(argv[i]);
        }
        if (des == null)
            des = cwuDes;
        ls(des, infoFlag, recurseFlag, 0, "");
    }

    /**
    * Execute Dashiell's "mkdir" command.
    * Create a new empty unit. Note that all empty units have the same hashkey,
    * so there really is only one empty unit that ever really exists!
    */
    private void cmdMkdir(String argv[]) {
        BasedDesignator newUnitDes = null;
        ensureUsage(argv, 0, 1, "mkdir [<name>]");
        ensureOpenHaberdashery();
        try {
            newUnitDes = new BasedDesignator(hab.put(new Unit()));
            out.println(newUnitDes.toString());
        } catch (HaberdasherException e) {
            simpleError("mkdir failed -- " + e.getMessage());
        }
        if (argv.length == 2) {
            addMapping(argv[1], newUnitDes);
            pwu();
        }
    }

    /**
    * Execute Dashiell's "open" command.
    * Open a new Haberdashery in the indicated file. If another Haberdashery is
    * already open it will be automatically closed first.
    */
    private void cmdOpen(String argv[]) {
        ensureUsage(argv, 1, 1, "open <filename>");
        String newFile = argv[1];
        if (hab != null)
            closeCurrentHaberdashery();
        try {
            EEditableFile editableFile = null;
            try {
                editableFile = myRootDir.lookupFile(newFile);
                Repository rep = new Repository(editableFile.editor());
                hab = new Haberdashery(rep);
            } catch (IOException iox) {
                out.println("[ File not found, creating new Haberdashery file " + newFile + " ]");
                editableFile = myRootDir.mkfile(newFile);
                Repository rep = new Repository(editableFile.editor());
                hab = new Haberdashery(rep);
                hab.init();
                ec.e.inspect.Inspector.gather(hab,"Haberdashery", "Haberdashery itself");
            }
        } catch (Exception e) {
            nonFatalException(e, "attempt to open Haberdashery in file " +
                              newFile + " failed.");
        }
        habFile = newFile;
        cdRoot();
    }

    /**
    * Execute Dashiell's "pwd" command.
    * Print the current working unit.
    */
    private void cmdPwd(String argv[]) {
        ensureUsage(argv, 0, 0, "pwd");
        ensureOpenHaberdashery();
        pwu();
    }

    /**
    * Execute Dashiell's "quit" command.
    */
    private void cmdQuit(String argv[]) {
        if (hab != null) {
            try {
                hab.close();
            } catch (Exception e) {
                fatalException(e,
                    "attempt to close currently open Haberdashery " +
                    habFile + " failed.");
            }
        }
        out.println("bye!");
        throw new DashiellFatalErrorException();
    }

    /**
    * Execute Dashiell's "rm" command.
    * Remove a mapping entry from the current working unit.
    */
    private void cmdRm(String argv[]) {
        ensureUsage(argv, 1, 1, "rm <name>");
        ensureOpenHaberdashery();
        removeMapping(argv[1]);
        pwu();
    }

    /**
    * Handle unknown commands.
    * Just complain without real consequence.
    */
    private void cmdUnknown(String argv[]) {
        out.println("unknown command '" + argv[0] + "'");
    }
}

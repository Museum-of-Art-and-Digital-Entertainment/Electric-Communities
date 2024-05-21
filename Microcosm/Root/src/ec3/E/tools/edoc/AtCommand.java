/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

abstract class AtCommand {

    /** returns how many annotations are valid, hence how many words the
     * parser should look ahead before deciding that the separating ';'
     * is missing & reading the rest as textual description regardless.
     */
    abstract int lookahead();

    /** returns whether we expect annotations to be separated from text
     * with a ';' */
    abstract boolean expectSemiColon();

    /** This method must be overridden by the actual AtCommand classes
     *  if they want to accept any annotations. Otherwise the attempts
     *  to add annotations will get dropped on the floor here.  */
    void addAnnotation(String annotation)
            throws StringNotUnderstoodException {
        System.out.println("AtCommand.addAnnotation("+annotation+")");
    }

    protected String myDescription = null;

    /** This method may be overridden by the subclasses of AtCommand,
     *  though the default implementation will presumably be adequate
     *  for most.
     *
     *  @param description nullOK; the string description for this command*/
    void addDescription(String description) {
        //System.out.println("AtCommand.addDescription("+description+")");
        myDescription = description;
    }

    /** This method returns this command's description string. this will
     *  presumably be overridden by any subclasses which modify addDescription
     *
     *  @returns nullOK; this command's description string
     *  @see AtCommand#addDescription
     */
    String description() {
        return myDescription;
    }

}

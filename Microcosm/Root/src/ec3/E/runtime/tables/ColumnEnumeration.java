package ec.tables;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This is the Enumeration for columns that parallel KeyColumns
 */
/*package*/ class ColumnEnumeration implements Enumeration {
    private Table myTable;
    private KeyColumn myKeys;
    private Column myColumn;
    private int myNumSlots;
    private int myCursor;
    private Object myElement;

    /**
     * Construct a ColumnEnumeration
     *
     * @param keys Says which indices are valid
     * @param column Contains the elements
     */
    /*package*/ ColumnEnumeration(Table table, Column column) {
        myTable = table;
        myKeys = table.myKeys;
        myColumn = column;
        myNumSlots = myKeys.numSlots();
        advance(0);
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public boolean hasMoreElements() {
        return myCursor != -1;
    }

    /**
     * Implementation of method from java.util.Enumeration.
     */
    public Object nextElement() {
        if (myCursor == -1) {
            throw new NoSuchElementException("ColumnEnumeration");
        }
        // return the element strongly held by the last advance,
        // rather than looking it up now.
        Object result = myElement;
        advance(myCursor + 1);
        return result;
    }

    /**
     * Good to call this before dropping the Enumeration, in order to
     * release the table and prevent unnecessary copying
     */
    public void skipRest() {
        if (myCursor != -1) {
            advance(myNumSlots);
        }
    }

    /** 
     * Advance the myCursor past any vacancies to the first occupied
     * slot at or after cursor, or -1 if there is no next non-vacant
     * slot to point at.  Remembers the found element strongly so it
     * won't disappear out from under us.
     */
    private void advance(int cursor) {
        myCursor = myKeys.firstTaken(cursor);
        if (myCursor == -1) {
            // We're done with the table.  Release it in
            // order to prevent unnecessary copying
            if (myTable != null) {
                myTable.clear();
                myTable = null;
                myKeys = null;
                myColumn = null;
                myElement = null;
            }
        } else {
            // hold the element strongly
            myElement = myColumn.get(myCursor);
        }
    }
}

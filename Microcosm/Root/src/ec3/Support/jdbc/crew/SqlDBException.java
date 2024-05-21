package ec.sqldb.crew;

/**
 *  AcctDBException -- thrown when there is an
 *  operational problem with the account db.
 */

class SqlDBException extends Exception {
    SqlDBException()
    {
        super();
    }

    SqlDBException(String msg)
    {
        super(msg);
    }
}

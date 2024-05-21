package ec.sqldb.crew;

import ec.vcache.ClassCache;
import java.lang.String;
import java.sql.*;

/**
 * SqlDB -- wraps the JDBC java.sql.* commonly used calls to
 * provide a simpler facade for db access.
 */
class SqlDB  {
    private boolean     initialized = false;
    private Connection  myConnection;
    private Statement   myStatement = null;
    private String      myURL;
    private String      myUsername;
    private String      myPassword;
    private String      myDriver;

    /** SqlDB constructor <p>
     *
     * @param String dbURL      - jdbc url to the database.
     * @param String dbUsername - database user name to use for access.
     * @param String dbPassword - password for database user.
     * @param String dbDriver   - jdbc driver to use for db access.
     */
    SqlDB(String dbURL,
          String dbUsername,
          String dbPassword,
          String dbDriver) {
        myURL      = dbURL;
        myUsername = dbUsername;
        myPassword = dbPassword;
        myDriver   = dbDriver;
    }

    /**
     * initDBConnection -- initialiaze the connection to the database.
     * This method loads the jdbc driver, and gets a connection to the
     * database using the url, user, and password specified to the
     * SqlDB constructor.
     */
    void initDBConnection() {
        if (initialized) {  // only allow one connection to the database
            return;
        }
        try {
            ClassCache.forName(myDriver);
            myConnection = DriverManager.getConnection(myURL,
                                                       myUsername,
                                                       myPassword);
            myConnection.setAutoCommit(false); // we say when to do the commit
        } catch (ClassNotFoundException e) {
            System.out.println("Can't load driver " + myDriver);
        } catch (SQLException e) {
            System.out.println("Can't connect to database " +
                               myURL + " with " + myUsername);
        }
        initialized = true;
    }

    /**
     * closeDBConnection -- cancels any uncommited database transactions
     * and closes the connection to the database.
     */
    void closeDBConnection() throws SqlDBException {
        if ( ! initialized) {
            return;
        }

        try {
            cancelDBTransaction();
            myConnection.close();
        } catch (SQLException e) {
            System.err.println("SqlDBCrew: closeDBConnection() " +
                               e.getMessage());
        }
        initialized = false;
    }

    /**
     * startDBTransation -- wraps jdbc createStatement().
     * This method is called prior to any query or update requests. <p>
     * If there are an existing outstanding transaction with this
     * SqlDB object, then that transaction will be canceled. <p>
     *
     * @return Statement - the jdbc statement used to perform other
     * jdbc operations.
     *
     * @see ec.sqldb.crew.SqlDB#commitDBTransaction
     * @see ec.sqldb.crew.SqlDB#finishDBTransaction
     * @see ec.sqldb.crew.SqlDB#cancelDBTransaction
     * @see ec.sqldb.crew.SqlDB#getStatement
     */
    Statement startDBTransaction() throws SqlDBException
    {
        try {
            cancelDBTransaction();
            myStatement = myConnection.createStatement();
        } catch (SQLException e) {
            throw new SqlDBException("startDBTransaction():" +
                                      e.getMessage());
        }
        return myStatement;
    }

    /**
     * commitDBTransation -- wraps jdbc Statement.close() and
     * Connection.commit().
     * This method is called to commit a previous jdbc update request
     * to the database.  There should have been a previous call to
     * startDBTransaction().  (If not there is no side effect.) <p>
     *
     * @see ec.sqldb.crew.SqlDB#startDBTransaction
     * @see ec.sqldb.crew.SqlDB#finishDBTransaction
     * @see ec.sqldb.crew.SqlDB#cancelDBTransaction
     * @see ec.sqldb.crew.SqlDB#getStatement
     */
    void commitDBTransaction() throws SqlDBException
    {
        if (myStatement == null) {
            return;
        }
        try {
            myStatement.close();
            myConnection.commit();
        } catch (SQLException e) {
            throw new SqlDBException("cancelDBConnection():" +
                                      e.getMessage());
        }
        myStatement = null;
    }

    /**
     * finishDBTransation -- wraps jdbc Statement.close().
     * This method is called to close out a statement without explicitly
     * commiting records to the database.
     *
     * @see ec.sqldb.crew.SqlDB#startDBTransaction
     * @see ec.sqldb.crew.SqlDB#commitDBTransaction
     * @see ec.sqldb.crew.SqlDB#cancelDBTransaction
     * @see ec.sqldb.crew.SqlDB#getStatement
     */
    void finishDBTransaction() throws SqlDBException
    {
        try {
            myStatement.close();
        } catch (SQLException e) {
            throw new SqlDBException("cancelDBConnection():" +
                                      e.getMessage());
        }
        myStatement = null;
    }

    /**
     * cancelDBTransation -- wraps jdbc Statement.close() and
     * Connection.rollback().
     * This method is called to rollback any database updates.
     *
     * @see ec.sqldb.crew.SqlDB#startDBTransaction
     * @see ec.sqldb.crew.SqlDB#commitDBTransaction
     * @see ec.sqldb.crew.SqlDB#finishDBTransaction
     * @see ec.sqldb.crew.SqlDB#getStatement
     */
    void cancelDBTransaction() throws SqlDBException
    {
        if (myStatement == null) {
            return;
        }

        try {
            myStatement.close();
            myConnection.rollback();
        } catch (SQLException e) {
            throw new SqlDBException("cancelDBConnection():" +
                                      e.getMessage());
        }
        myStatement = null;
    }

    /**
     * getStatement -- gets the jdbc Statement object used to
     * access the database "refered" to by this SqlDB.
     * This is called by classes that need to make jdbc calls
     * such as executeUpdate() or executeQuery().<p>
     *
     * @param Statement -- a jdbc Statement object
     */
    Statement getStatement() throws SqlDBException
    {
        if (myStatement == null) {
            this.startDBTransaction();
        }
        return myStatement;
    }

    /**
     * finalize method -- cleans up the db connection.
     *
     * @see ec.sqldb.crew.SqlDB#closeDBTransaction
     */
    protected void finalize() throws SqlDBException
    {
        closeDBConnection();
    }
}



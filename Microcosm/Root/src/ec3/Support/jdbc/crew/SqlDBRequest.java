package ec.sqldb;

import java.lang.String;
import java.sql.*;

/**
 * SqlDBRequest interface -- all Requests to the database look like this. <p>
 * 
 * An SqlDBRequest contains a constructor and an execute method.
 * Typically, the constructor takes an sql string as a parameter, which
 * the constructor saves in the object.  The execute method performs the 
 * neccessary jdbc work, using the information provided at instatiation
 * time. <p>
 *
 * An SqlDBRequest is instantiated by the visible SqlDBCrewThread API, 
 * queued up for the SqlDBCrewThread, and then 'executed' by the 
 * SqlDBCrewThread. <p>
 *
 * These xxxRequest classes encapsulate the database jdbc method calls.
 * (XXX to do: handle responses.  eg result sets.) <p>
 */
public interface SqlDBRequest {
    /**
     * exectute -- method that is invoked by the SqlDBCrewThread
     * when a request is dequeued. <p>
     *
     * @param SqlDB db -- The handle to the database that the 
     *                    request is executed on.
     */
    public void execute(SqlDB db);
}

/**
 * SqlDBUpdateRequest -- embodies the code neccessary to execute
 * an "update" request on the database.  This includes the
 * SQL commands INSERT, UPDATE or DELETE.
 */
public class SqlDBUpdateRequest implements SqlDBRequest {

    private String mySql;

    /**
     * SqlDBUpdateRequest constructor - create a request object that
     * will can perform an insert, update, or delete operation on the DB.
     */
    public SqlDBUpdateRequest(String sql) {
        mySql = sql;
    }
    
    /**
     * exectute -- actually does the jdbc work: essentially a jdbc
     * stmt.executeUpdate(sql) <p>
     *
     * @param SqlDB db -- the database to use for update.
     */
    public void execute(SqlDB db) {
        
        System.out.println("Crew doing update " + mySql);
        
        try {
            Statement stmt = db.startDBTransaction();
            stmt.executeUpdate(mySql);
            db.commitDBTransaction();
        } catch (SqlDBException e) {
            System.err.println("Doing Sql Insert: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Doing Sql Insert: " + e.getMessage());
        }
    }
}


/**
 * SqlDBUpdateRequest -- embodies the code neccessary to execute
 * an "query" request on the database, which can return 0 or 1
 * records.  This request is used for the SQL SELECT statement.
 */
public class SqlDBQueryRequest implements SqlDBRequest {

    private String mySql;

    /**
     * SqlDBQueryRequest constructor -- create an request object that
     * can perform a query (select) operation. <p>
     * 
     * @param String sql -- an SQL string that contains a select
     * statement.
     */
    public SqlDBQueryRequest(String sql) {
        mySql = sql;
    }

    /**
     * execute -- actually does the jdbc work: which is a jdbc 
     * stmt.executeQuery(). <p>
     *
     * @todo XXX handle result set
     * @param SqlDB db -- the database to perform the query on.
     */
    public void execute(SqlDB db) {

        ResultSet rs = null;  // XXX need to figure out how to get this
                              // XXX back to the requestor.
        
        System.out.println("Crew doing query " + mySql);
        
        try {
            Statement stmt = db.startDBTransaction();
            rs = stmt.executeQuery(mySql);
            db.commitDBTransaction();
        } catch (SqlDBException e) {
            System.err.println("Doing Sql Insert: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Doing Sql Insert: " + e.getMessage());
        }
    }
}


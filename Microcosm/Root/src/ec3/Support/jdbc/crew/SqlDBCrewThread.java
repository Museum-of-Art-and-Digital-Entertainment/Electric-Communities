package ec.sqldb;

import ec.e.util.Queue;
import ec.e.util.QueueReader;
import ec.e.util.QueueWriter;

import ec.sqldb.SqlDBRequest;
import ec.sqldb.SqlDB;

/**
 * SqlDBCrewThread -- This thread provides asynchronous database access.
 * The run() method receives SqlDBRequest objects from the 
 * SqlDBSeismoSteward and invokes the SqlDBRequest execute() method which
 * actually invokes the JDBC code.
 */
public class SqlDBCrewThread implements Runnable {

    private QueueWriter myQWriter;
    private QueueReader myQReader;
    private SqlDB       mySqlDB;

    /**
     * SqlDBCrewThread -- instantiate a SQLDBCrewThread.  (Note: The
     * constructor doesnt actually initiate the db connection.  This
     * is done in the run() method of the thread, because its possible
     * that connection initialization may block.)
     *
     * @param String url  -- jdbc style url to the db to use.
     * @param String user -- user to log in to the db with.
     * @param String pass -- password for the user.
     * @param String driver -- jdbc driver to use for db access.
     */
    public SqlDBCrewThread(String url,
                           String user,
                           String pass,
                           String driver) { 
        mySqlDB = new SqlDB(url, user, pass, driver);
        Queue q = new Queue(new Object());
        myQReader = q.reader();
        myQWriter = q.writer();
    }

    /** 
     * SqlDBCrewThread -- start our thread running.
     */
    public void startRunning() {
        new Thread(this).start();
    }


    /**
     * SqlUpdate -- This is the crew interface for performing
     * sql 'update' operations (ie INSERT, UPDATE, and DELETE).
     * This method creates an SqlDBUpdateRequest and queues it
     * to the crew thread.
     *
     * @param String sql -- the sql string containing an insert, update,
     * or delete command.
     */
    public void sqlUpdate(String sql) {
        SqlDBUpdateRequest updateRequest = new SqlDBUpdateRequest(sql);
        myQWriter.enqueue((SqlDBRequest)updateRequest);
    }

    /**
     * SqlQuery -- This is the crew interface for performing
     * sql 'query' operation, that is, an SQL SELECT statement.  This
     * method creates an SqlDBQueryRequest object and queues it to
     * the crew thread.
     *
     * @param String sql -- the sql string containing the SELECT statement.
     */
    public void sqlQuery(String sql) {
        SqlDBQueryRequest queryRequest = new SqlDBQueryRequest(sql);
        myQWriter.enqueue((SqlDBRequest)queryRequest);
    }

    /**
     * run() -- inits the database connection, then waits for a SqlDBRequest.
     *          When a request comes in, the thread invokes the execute()
     *          method of the dequeued SqlDBRequest object, performing the 
     *          appropriate database opertion: insert, update, delete, 
     *          query, etc.
     */
    public void run() {
        
        SqlDBRequest request;

        try {
            mySqlDB.initDBConnection();

            while ((request = (SqlDBRequest)myQReader.nextElement()) != null) {

                try {
                    request.execute(mySqlDB);
                } catch (Exception e) {
                    System.out.println("got exception executing db request" + 
                                       e.getMessage());
                }
            }
            
            mySqlDB.closeDBConnection();

        } catch (Exception e) {
            System.out.println("got exception " + e.getMessage());
        }
    }
}

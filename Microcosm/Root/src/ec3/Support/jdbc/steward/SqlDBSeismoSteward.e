package ec.sqldb.steward;

import ec.e.start.Seismologist;
import ec.e.start.Tether;
import ec.e.start.TimeQuake;
import ec.e.start.Vat;
import ec.e.start.SmashedException;
import ec.sqldb.crew.SqlDBCrewThread;

/**
 * SqlDBSeismoSteward -- the steward that handles SqlDB requests.
 */
public eclass SqlDBSeismoSteward implements Seismologist {

    private Tether /* SqlDBCrewThread */ mySqlDBCrewThreadTether;

    private String      myURL;
    private String      myUsername;
    private String      myPassword;
    private String      myDriver;
    
    /**
     * SqlDBSeismoSteward -- basically calls reconstruct, which creates
     * the SqlDBCrewThread, makes a tether to the crew thread, and
     * starts it.  (when the crew thread starts, it inits the connection
     * the the database described by url/user/pass/driver.
     *
     * @param vat       - our vat.
     * @param url       - jdbc url to the db server (eg oracle).
     * @param user      - user name to log into the db server with.
     * @param pass      - password for above mentioned user.
     * @param driver    - name of the jdbc driver to use for db access.
     */
    public SqlDBSeismoSteward(Vat       vat,
                              String    url,
                              String    user,
                              String    pass,
                              String    driver) {
        
        myURL      = url;
        myUsername = user;
        myPassword = pass;
        myDriver   = driver;

        reconstruct(vat, url, user, pass, driver);
    }

    /**
     * noticeQuake() -- A quake has occured, we are being revived
     */
    emethod noticeQuake(TimeQuake quake) {
        System.out.println("SqlDBSeismoSteward: quake " + quake);
        quake.waitForNext(this);
    }

    /**
     * noticeCommit() -- called when the vat is commiting to disk.
     */
    emethod noticeCommit() {
        System.err.println("SqlDBSeismoSteward: committed");
    }

    /**
     * sqlUpdate -- provides an asynch interface to (essentially) the
     * java.sql.Statement.executeUpdate(), This emethod passes the sql
     * request to the SqlDBCrew.
     *
     * @param String sql -- contains an INSERT, UPDATE, or DELETE sql stmt.
     */
    emethod sqlUpdate(String sql) {
        SqlDBCrewThread mySqlDBCrewThread = getCrew();
        mySqlDBCrewThread.sqlUpdate(sql);
    }

    /**
     * sqlQuery -- provides an asynch interface to (essentially) the
     * java.sql.Statement.executeQuery().  This emethod passes the sql
     * request on to the SqlDBCrew.
     *
     * @param String sql -- contains an SQL SELECT stmt that returns
     * a single result set.
     *
     * @todo XXX handle result set.
     */
    emethod sqlQuery(String sql) {
        SqlDBCrewThread mySqlDBCrewThread = getCrew();
        mySqlDBCrewThread.sqlQuery(sql);
    }

    /**
     * getCrew -- used to get the reference to our crew thread.  This does
     * a held() on the SqlDBCrewThreadTether, and reconstructs the crew
     * if help() throws a SmashedException.
     */
    private SqlDBCrewThread getCrew() {
        SqlDBCrewThread mySqlDBCrewThread = null;

        try {
            mySqlDBCrewThread = (SqlDBCrewThread)mySqlDBCrewThreadTether.held();
        } catch (SmashedException se) {
            System.out.println("SqlDBSeismo .. Got smashed " + se.getMessage());
            mySqlDBCrewThread = reconstruct(mySqlDBCrewThreadTether.vat(),
                                            myURL,      
                                            myUsername, 
                                            myPassword,
                                            myDriver);
        }

        return mySqlDBCrewThread;
    }

    /**
     * reconstruct -- called at startup, or when a quake has occured.
     * This method instantiates and starts an SqlDBCrewThread with the 
     * url, user, pass, and driver params.  
     *
     * @param Vat vat       - our vat.
     * @param String url    - the jdbc style url to the database to use.
     * @param String user   - the db user to use for the db connection.
     * @param String pass   - the password for above user.
     * @param String driver - the class name for the jdbc driver to use.
     * 
     * @returns SqlDBCrewThread - a reference to the thread that will
     *                            handle db requests for this steward.
     */
    private SqlDBCrewThread reconstruct(Vat    vat,
                                        String url,
                                        String user,
                                        String pass,
                                        String driver) {
        SqlDBCrewThread theThread;

        theThread = new SqlDBCrewThread(url, user, pass, driver);
        mySqlDBCrewThreadTether = new Tether(vat, theThread);
        theThread.startRunning();
        
        return theThread;
    }    
}


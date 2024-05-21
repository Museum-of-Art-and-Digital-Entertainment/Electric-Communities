package ec.sqldb.tester;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.start.Vat;
import ec.e.start.QuakeReporter;
import ec.e.quake.TimeMachine;
import ec.e.file.EStdio;

import ec.sqldb.steward.*;
import ec.sqldb.crew.*;

/**
 * SqlDBTester -- Simple test class to test out talking
 * the sql code from E.  This can also test out the
 * db code recovering from a checkpoint.
 */

public eclass SqlDBTester implements ELaunchable {
    EStdio       eio;
    EEnvironment myEnv;
    TimeMachine  myTM;

    String dbURL;
    String dbUsername;
    String dbPassword;
    String dbDriver;
    
    public SqlDBTester() {
        /*
        */
    }

    emethod go(EEnvironment env) {

        myEnv = env;

        try {
            eio.initialize(myEnv.vat());
        } catch (Exception e) {
        }

        try {
            myTM = TimeMachine.summon(myEnv);
        } catch (Exception e) {
            eio.out().println("Cant summon a timeMachine : " + e.getMessage());
        }

        myTM <- nextQuake(new QuakeReporter());

        this <- start();
    }
    
    emethod start() {
        TestGuest               theGuest = null;
        SqlDBMaker              theSqlDBMaker = null;
        SqlDBSeismoSteward      theSqlDBSeismoSteward = null;

        dbURL      = myEnv.getProperty("dburl" , 
                                       "jdbc:weblogic:oracle:harpo_tcp_WG73");
        dbUsername = myEnv.getProperty("dbuser", "svc");
        dbPassword = myEnv.getProperty("dbpass", "svc");
        dbDriver   = myEnv.getProperty("dbdriver", 
                                       "weblogic.jdbc.oci.Driver");

        // get the SqlDBMaker
        try {
            theSqlDBMaker = SqlDBMaker.summon(myEnv);
        } catch (Exception e) {
            eio.out().println("Cant summon the SqlDBMaker : " +
                              e.getMessage());
        }

        // make a SqlDBSeismoSteward
        theSqlDBSeismoSteward = theSqlDBMaker.makeSqlDBSteward(dbURL,
                                                               dbUsername,
                                                               dbPassword,
                                                               dbDriver);
        // create a test guest and give it the steward
        theGuest = new TestGuest(theSqlDBSeismoSteward);

        // tell the test guest to do something
        theGuest <- doYourThing();

        eio.out().println("hibernating ?");

        myTM <- hibernate(null,0);  // will exit here
    }
}



public eclass TestGuest {
    SqlDBSeismoSteward mySqlDBSteward = null;

    public TestGuest() {}

    /** get the SqlDBSteward on construction
     *
     */
    public TestGuest(SqlDBSeismoSteward aSqlDBSteward) {
        mySqlDBSteward = aSqlDBSteward;
    }

    emethod doYourThing() {

        // execute a test sql command.  Note, this will run only
        // once as the user_id needs to be unique.
        String sql = "insert into user_id_tbl " +
            " (user_id, email_addr, first_name, middle_iniital, last_name," +
            " city, state, zip_code) " +
            " VALUES ( " +
            " 4570, 'jeff@communities.com', 'Jeff', 'L.', 'Crilly', " +
            " 'Palo Alto', 'CA', '94306' " +
            " ) ";

        mySqlDBSteward <- sqlUpdate(sql);
    }
}

package ec.test.jdbc;

import java.util.*;
import java.net.URL;
import java.sql.*;

/**
 * <blockquote>
 * <pre>
 * <p>
 * Simple test program for trying out JDBC using the 
 * shareware mSQL database.
 * </p>
 *
 * <p>
 * New version: minor mods to make it work with oracle.
 * For this to work, the following env vars need to be 
 * defined:
 *    ORACLE_HOME=/home/oracle
 *    ORACLE_SID=WG73
 *
 * The follwing info below referring to msql no longer applies.
 * (Next version will be more generic.)
 * </p>
 *
 * <p>
 * Couple things are required to use this...
 *   1) mSQLd 1.0.16 needs to be running somewhere (currently it is on gracie).
 *   2) You need the 0.9.6 jdbc-msql driver in your classpath.
 *   3) java 1.0.2.
 *   3) jdbc 1.10
 * </p>
 * 
 * <p>
 * mSQL2.0 will not work -- there is no jdbc driver available.
 * </p>
 * 
 * <p>
 * This has only been tested with 1.0.2 on Solaris.. It also will work
 * on win32 but you need the jdbs-msql driver/classes plus 1.10 jdbc
 * installed there also.  There is a version of mSQLd for win32.
 * Likewise this app should be able to talk to mSQLd on Solaris
 * from a win32 box.
 * </p>
 * 
 * <p>
 * This app demonstrates the basic JDBC SQL commands for adding,
 * deleting, and retrieving records from a database.  The database
 * is a simple table containing the following columns...
 * +-----------------+----------+--------+----------+-----+
 * |     Field       |   Type   | Length | Not Null | Key |
 * +-----------------+----------+--------+----------+-----+
 * | user_id         | int      | 4      | Y        | Y   |
 * | email_addr      | char     | 40     | N        | N   |
 * | first_name      | char     | 15     | N        | N   |
 * | middle_iniital  | char     | 2      | N        | N   |
 * | last_name       | char     | 15     | N        | N   |
 * | acct_status     | int      | 4      | N        | N   |
 * | street_addr1    | char     | 80     | N        | N   |
 * | street_addr2    | char     | 80     | N        | N   |
 * | city            | char     | 80     | N        | N   |
 * | state           | char     | 80     | N        | N   |
 * | zip_code        | char     | 10     | N        | N   |
 * | country         | char     | 32     | N        | N   |
 * | paid_up_till    | int      | 4      | N        | N   |
 * | avatar1_id      | int      | 4      | N        | N   |
 * | avatar2_id      | int      | 4      | N        | N   |
 * +-----------------+----------+--------+----------+-----+
 * </p>
 *
 * <p>
 * The user_id is unique, and the "next user id" value is kept
 * in another table called user_id_counter.  It is just a simple
 * table with one row and one column...
 * +-----------------+----------+--------+----------+-----+
 * |     Field       |   Type   | Length | Not Null | Key |
 * +-----------------+----------+--------+----------+-----+
 * | next_user_id    | int      | 4      | N        | N   |
 * +-----------------+----------+--------+----------+-----+
 * </p>
 *
 * <p>
 * The usage for this app is...
 * </p>
 *
 * <p>
 * Add a user to the database...
 *  java UserDBTester adduser [user fields]
 *  [user fields] are the following items (don't forget to quote)
 *      email
 *      firstname
 *      middleinitial
 *      lastname
 *      streetaddr1
 *      city
 *      state
 *      zipcode
 *  Currently, all fields have to be there -- hey, its a tester.
 * </p>
 *
 * <p>
 * Show all users in the database (lists out the user_id, 
 * first/last name and email addr)...
 *  java UserDBTester dumptbl
 * </p>
 *
 * <p>
 * Delete a user from the database...
 *  java UserDBTester deleteuser [user_id]
 * </p>
 *
 * <p>
 * Show more fields for a given user_id (prints out more 
 * fields than dumptbl)...
 *  java UserDBTester showuser [user_id]
 * </p>
 *
 * <p>
 * Note: adduser uses the user_id_counter table to get and
 * increment the next_user_id.  Since the increment/update
 * isn't atomic, two users of UserDBTester could update
 * at the same time. (mSQL doesn't have a way of incrementing 
 * and updating a value atomically -- there needs to be
 * a lock.  Oracle class database servers do.)  
 * </p>
 *
 * <p>
 * Examples of usage:
 * </p>
 *
 * <p>
 *  $ java UserDBTester dumptbl
 *  dumptbl
 *   -- user_id  email_addr  first_name  last_name  --
 *  1 jeff@communities.com Linda Crilly
 *  2 jeff@communities.com Jeff Crilly
 *  3 tony@communities.com Tony Grant
 *  65 randy@communities.com Randy Farmer
 *  63 chip@communities.com Chip Mornigstar
 *  64 harry@communities.com Harry  Richardson
 *  ==== 6 rows returned.
 * </p>
 *
 * <p>
 *  $ java UserDBTester adduser "dana@communities.com" "Dana" " " "Timbrook" "downtown" "Los Gatos" "CA" "99445"  
 *  adduser
 *  Adding user
 *  INSERT INTO .. [verbose SQL command shown here]
 * </p>
 * 
 * <p>
 *  $ java UserDBTester dumptbl
 *  dumptbl
 *   -- user_id  email_addr  first_name  last_name  --
 *  1 jeff@communities.com Linda Crilly
 *  2 jeff@communities.com Jeff Crilly
 *  3 tony@communities.com Tony Grant
 *  65 randy@communities.com Randy Farmer
 *  63 chip@communities.com Chip Mornigstar
 *  64 harry@communities.com Harry  Richardson
 *  66 dana@communities.com Dana Timbrook
 *  ==== 7 rows returned.
 * </p>
 *
 * <p>
 *  $ java UserDBTester deleteuser 64
 *     deleteuser
 * </p>
 *
 * <p>
 *  $ java UserDBTester dumptbl
 *  dumptbl
 *   -- user_id  email_addr  first_name  last_name  --
 *  1 jeff@communities.com Linda Crilly
 *  2 jeff@communities.com Jeff Crilly
 *  3 tony@communities.com Tony Grant
 *  65 randy@communities.com Randy Farmer
 *  63 chip@communities.com Chip Mornigstar
 *  66 dana@communities.com Dana Timbrook
 *  ==== 6 rows returned.
 * </p>
 *
 * <p>
 *  $ java UserDBTester showuser 63
 *  showuser
 *  stmt.getMaxRows() is 4096
 *  ===================
 *  Chip Mornigstar
 *     otherstreet
 *     Palo Alto
 *     CA
 *     94123
 * </p>
 *   
 * <p>
 *  $ java UserDBTester showuser 66
 *  showuser
 *  stmt.getMaxRows() is 4096
 *  ===================
 *  Dana Timbrook
 *     downtown
 *     Los Gatos
 *     CA
 *     99445 
 * </p>
 * </pre>
 * </blockquote>
 */

class UserDBTester {
    Connection UserDBconn;
    // dburl is the URL to the database server.  Currently it is running
    // on gracie, port 4333.  jdbc:msql tells jdbc to use the
    // jdbc-msql jdbc driver.
    //  String dburl = "jdbc:msql://gracie.communities.com:4333/userdb";
    static final String dburl = "jdbc:weblogic:oracle";
    static final String drivername = "weblogic.jdbc.oci.Driver";

    // user can always be the same user.  user must be in msql.acl, the
    // mSQL access list.
    String user = "svc";

    // no passwords in the acl file, so we don't need one here.
    String password = "svc"; 
    
    // the following are used by adduser()
    static final int emailpos     = 1;
    static final int firstnamepos     = 2;
    static final int middleipos       = 3;
    static final int lastnamepos      = 4;
    static final int streetaddr1pos   = 5;
    static final int citypos      = 6;
    static final int statepos         = 7;
    static final int zipcodepos       = 8;
    static final int useridpos        = 9;
    static final int acctstatuspos    = 10;

    public UserDBTester() {
    }
    
    public static void main (String argv[])  {
        
        UserDBTester userdb = new UserDBTester();

        if (argv.length == 0) {
            System.out.println("usage:");
            System.out.println("  java UserDBTester adduser [user fields]");
            System.out.println("  [user fields] are the following items (don't forget to quote)");
            System.out.println("      email");
            System.out.println("      firstname");
            System.out.println("      middleinitial");
            System.out.println("      lastname");
            System.out.println("      streetaddr1");
            System.out.println("      city");
            System.out.println("      state");
            System.out.println("      zipcode");
            System.out.println(" ");
            System.out.println("  java UserDBTester dumptbl");
            System.out.println(" ");
            System.out.println("  java UserDBTester deleteuser [user_id]");
            System.out.println(" ");
            System.out.println("  java UserDBTester showuser [user_id]");
            System.out.println(" ");
            System.exit(0);
        }
        
        userdb.connect();
        
        System.out.println(argv[0]);
        
        if (argv[0].equalsIgnoreCase("adduser")) {
            System.out.println("Adding user");
            userdb.adduser(argv);
        } else if (argv[0].equalsIgnoreCase("showuser")) {
            userdb.showuser(argv);
        } else if (argv[0].equalsIgnoreCase("deleteuser")) {
            userdb.deleteuser(argv);
        } else if (argv[0].equalsIgnoreCase("dumptbl")) {
            userdb.dumptbl();
        }
        
    }
    
    /**
     * connect - connect to the database server
     */
    public void connect() {
        try {
            Class.forName(drivername);    // load jdbc driver
            UserDBconn = DriverManager.getConnection(dburl, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Can't load driver " + drivername);
        } catch (SQLException e) {
            System.out.println("can't connect to db " + dburl + " with " + user);
        }
    }
    
    /**
     * disconnect - disconnect from the database server
     */
    public void disconnect() {
        try {
            UserDBconn.close();
        } catch (SQLException e) {
            System.out.println("close db failed.");
        }
    }
    
    /**
     * getNextUserID -- get the next user id value from the database
     * this is used when adding a user to the db, and a unique user id
     * is needed.
     */
    // Note: this needs to be an atomic operation.  It currently isn't.
    private int getNextUserID() throws SQLException { 
        int returnVal = 0;
        
        ResultSet rs;
        Statement stmt = UserDBconn.createStatement();
        rs = stmt.executeQuery("select next_user_id from user_id_counter");
        
        rs.next();
        returnVal = rs.getInt("next_user_id");
        
        returnVal++;
        
        stmt.executeUpdate("update user_id_counter set next_user_id = " + returnVal);
        
        stmt.close();
        
        return returnVal;
    }
    
    /**
     * adduser -- add a user to the database
     */
    public void adduser(String argv[]) {
        int nextUserID;
        
        try {
            String insert = "INSERT INTO user_id_tbl "        +
                " (email_addr, first_name, middle_iniital, last_name, street_addr1, city, state, zip_code, user_id, acct_status) " + 
                " VALUES ("         +
                "\'" + argv[emailpos]       + "\', " +
                "\'" + argv[firstnamepos]   + "\', " +
                "\'" + argv[middleipos]     + "\', " +
                "\'" + argv[lastnamepos]    + "\', " +
                "\'" + argv[streetaddr1pos]     + "\', " +
                "\'" + argv[citypos]        + "\', " +
                "\'" + argv[statepos]       + "\', " +
                "\'" + argv[zipcodepos]     + "\', " +
                " user_id_seq.nextval, 1)";
            
            
            System.out.println(insert);
            Statement stmt = UserDBconn.createStatement();
            stmt.executeUpdate(insert);
            stmt.close();
            
        } catch (SQLException e) {
            while (e != null) {
                System.out.println("adduser " + argv[emailpos] + " to "+ dburl + " failed. ");
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Message:  " + e.getMessage());
                System.out.println("Vendor:   " + e.getErrorCode());
                e = e.getNextException();
                System.out.println("");
            }
        }       
    }
    
    /**
     * showuser -- show some interesting fields for a user
     */
    public void showuser(String argv[]) {
        String query = "SELECT * FROM user_id_tbl WHERE user_id = " + argv[1];
        ResultSet rs;
        
        try {
            
            Statement stmt = UserDBconn.createStatement();
            System.out.println("stmt.getMaxRows() is " + stmt.getMaxRows());
            
            rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                System.out.println("===================");
                System.out.println(rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println("   " + rs.getString("street_addr1"));
                System.out.println("   " + rs.getString("city"));
                System.out.println("   " + rs.getString("state"));
                System.out.println("   " + rs.getString("zip_code"));
                System.out.println(" ");
                
            }
            stmt.close();
            
        } catch (SQLException e) {
            while (e != null) {
                System.out.println("showuser " + argv[emailpos] + " to "+ dburl + " failed. ");
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Message:  " + e.getMessage());
                System.out.println("Vendor:   " + e.getErrorCode());
                e = e.getNextException();
                System.out.println("");
            }
        }
    }
    
    /**
     * delete a user -- deletes a user by user_id
     */
    public void deleteuser(String argv[]) {
        
        try {
            
            Statement stmt = UserDBconn.createStatement();
            stmt.executeUpdate("DELETE FROM user_id_tbl WHERE user_id = " + argv[1]);
            stmt.close();
            
        } catch (SQLException e) {
            while (e != null) {
                System.out.println("deleteuser " + argv[1] + " on " +  dburl + " failed. ");
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Message:  " + e.getMessage());
                System.out.println("Vendor:   " + e.getErrorCode());
                e = e.getNextException();
                System.out.println("");
            }
        }
    }      
    
    /**
     * dumptbl -- print out the contents of the user database
     */
    public void dumptbl() {
        String query = "SELECT user_id, email_addr, first_name, last_name FROM user_id_tbl";
        ResultSet rs;
        int counter = 0;
        
        try {
            
            Statement stmt = UserDBconn.createStatement();
            rs = stmt.executeQuery(query);
            
            System.out.println(" -- user_id  email_addr  first_name  last_name  --");
            
            while (rs.next()) {
                System.out.println(rs.getInt("user_id") + " " + 
                                   rs.getString("email_addr") + " " +
                                   rs.getString("first_name") + " " + 
                                   rs.getString("last_name"));
                counter++;
            }
            System.out.println("==== " + counter + " rows returned.");
            stmt.close();
            
        } catch (SQLException e) {
            while (e != null) {
                System.out.println("dumptbl " + " from "+ dburl + " failed. ");
                System.out.println("SQLState: " + e.getSQLState());
                System.out.println("Message:  " + e.getMessage());
                System.out.println("Vendor:   " + e.getErrorCode());
                e = e.getNextException();
                System.out.println("");
            }
        }
    }
}


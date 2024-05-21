package ec.sqldb;

import ec.e.run.Vat;
import ec.e.run.EEnvironment;

/**
 * SqlDBMaker -- provides the summon/magicPower maker functionality
 * required to make a SqlDBSeismoSteward()
 */
public class SqlDBMaker {
    private static boolean IExistAlready = false;
    private Vat myVat;

    /**
     * SqlDBMaker -- constructor
     * We allow only one SqlDBMaker to be created.
     *
     * @param Vat vat - our vat
     *
     */
    public SqlDBMaker(Vat vat) {
        if (IExistAlready) {
            throw new SecurityException("SqlDBMaker already exists");
        }
        IExistAlready = true;
        myVat = vat;
    }

    /**
     * summon -- summon up this vat's SqlDBMaker.  Used by trusted
     * code that already has an EEnvironment.
     *
     * @param EEnvironment eEnv -- the EEnvironment for this vat.
     */
    static public SqlDBMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException, 
                IllegalAccessException,
                InstantiationException
    {
        return (SqlDBMaker) eEnv.magicPower("ec.sqldb.SqlDBMagicPowerMaker");
    }

    /**
     * makeSqlDBSteward -- This creates a steward suitable for accessing
     * the database specified by url, user, pass, and driver.
     * 
     * @param url    - the jdbc style url for the steward's database.
     * @param user   - the db user name to use when accessing the db.
     * @param pass   - the password for above mentioned user name.
     * @param driver - the class name for the jdbc driver to use.
     */
    public SqlDBSeismoSteward makeSqlDBSteward(String url,
                                               String user,
                                               String pass,
                                               String driver) {
        return new SqlDBSeismoSteward(myVat, url, user, pass, driver);
    }
}



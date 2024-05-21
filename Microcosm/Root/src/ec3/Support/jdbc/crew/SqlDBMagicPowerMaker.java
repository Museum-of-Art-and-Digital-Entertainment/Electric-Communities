package ec.sqldb.crew;

import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;

import ec.sqldb.steward.SqlDBMaker;

/**
 * SqlDBMagicPowerMaker -- implements the make method which creates
 * a new SqlDBMaker object.  With the SqlDBMaker one can create
 * a new SqlDBSeismoSteward, which can be used to access a jdbc database.
 */
public class SqlDBMagicPowerMaker implements MagicPowerMaker {

    /**
     * make -- creates a SqlDBMaker object.  Used by EEnvironment.magicPower()
     * when trusted code wants to create an SqlDBMaker.
     *
     * @param EEnvironemtn env -- our EEnvironment (which is used to 
     * get the vat we are running in, which is required by SqlDBMaker).
     */
    public Object make (EEnvironment env) {
        return (Object) new SqlDBMaker(env.vat());
    }
}

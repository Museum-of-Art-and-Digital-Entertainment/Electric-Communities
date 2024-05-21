package ec.sqldb.crew;

import ec.sqldb.crew.SqlDB;

/**
 * Stubs for crew/SqlDBRequest.java 
 */

public interface SqlDBRequest {
    public void execute(SqlDB db);
}

public class SqlDBUpdateRequest implements SqlDBRequest {
    public SqlDBUpdateRequest(String sql) {
    }

    public void execute(SqlDB db) {
    }
}


public class SqlDBQueryRequest implements SqlDBRequest {
    public SqlDBQueryRequest(String sql) {
    }

    public void execute(SqlDB db) {
    }
}

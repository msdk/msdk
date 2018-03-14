package fr.profi.mzdb.util.sqlite;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;

/**
 * @author bouyssie
 *
 */
public abstract class SQLiteJobWrapper<T> implements ISQLiteConnectionFunction<T> {
	
    private SQLiteJob<T> sqliteJob;
    
	public void setSQLiteJob(SQLiteJob<T> sqliteJob) {
		this.sqliteJob = sqliteJob;
	}

	public SQLiteJob<T> getSQLiteJob() {
        return sqliteJob;
    }
    
    public SQLiteJobWrapper() {
        super();
    }
    
	public abstract T job(SQLiteConnection connection) throws Exception;
    
    @Override
	public T apply(SQLiteConnection connection) throws Exception {
    	return job(connection);
    }

}

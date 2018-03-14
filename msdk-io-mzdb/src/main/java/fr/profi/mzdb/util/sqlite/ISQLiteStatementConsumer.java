package fr.profi.mzdb.util.sqlite;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/**
 * @author bouyssie
 *
 */
public interface ISQLiteStatementConsumer {
	public void accept(SQLiteStatement stmt) throws SQLiteException;
}

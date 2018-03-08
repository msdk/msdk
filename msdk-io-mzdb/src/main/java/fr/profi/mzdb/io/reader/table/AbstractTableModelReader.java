package fr.profi.mzdb.io.reader.table;

import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;

/**
 * @author bouyssie
 *
 */
public abstract class AbstractTableModelReader<T> {
	
	protected SQLiteConnection connection;
	protected ISQLiteRecordExtraction<T> recordExtractor;
	
	protected AbstractTableModelReader(SQLiteConnection connection) {
		super();
		this.connection = connection;
		this.recordExtractor = buildRecordExtractor();
	}
	
	protected abstract ISQLiteRecordExtraction<T> buildRecordExtractor();
	
	protected T getRecord(String tableName, int id) throws SQLiteException {
		return new SQLiteQuery(connection, "SELECT * FROM "+tableName+" WHERE id = ?").bind(1, id)
			.extractRecord(recordExtractor);
	}
	
	protected List<T> getRecordList(String tableName) throws SQLiteException {
		return new SQLiteQuery(connection, "SELECT * FROM "+ tableName).extractRecordList(recordExtractor);
	}

}

package fr.profi.mzdb.util.sqlite;

import java.io.InputStream;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/**
 * @author David Bouyssie
 * 
 */
public class SQLiteRecord {

	private SQLiteQuery sqliteQuery = null;
	private SQLiteStatement stmt = null;

	public SQLiteRecord(SQLiteQuery sqliteQuery) {
		super();
		this.sqliteQuery = sqliteQuery;
		this.stmt = sqliteQuery.getStatement();
	}
	
	public SQLiteStatement getStatement() {
		return this.stmt;
	}
	
	// TODO: replace calls to name().toLowerCase() by toString() ?
	public String columnString(Enum<?> enumeration) throws SQLiteException {
		return this.columnString(enumeration.name().toLowerCase());
	}

	public String columnString(String columnName) throws SQLiteException {
		return this.stmt.columnString(sqliteQuery.getColumnIndex(columnName));
	}

	public int columnInt(Enum<?> enumeration) throws SQLiteException {
		return this.columnInt(enumeration.name().toLowerCase());
	}

	public int columnInt(String columnName) throws SQLiteException {
		return this.stmt.columnInt(sqliteQuery.getColumnIndex(columnName));
	}

	public double columnDouble(Enum<?> enumeration) throws SQLiteException {
		return this.columnDouble(enumeration.name().toLowerCase());
	}

	public double columnDouble(String columnName) throws SQLiteException {
		return this.stmt.columnDouble(sqliteQuery.getColumnIndex(columnName));
	}

	public long columnLong(Enum<?> enumeration) throws SQLiteException {
		return this.columnLong(enumeration.name().toLowerCase());
	}

	public long columnLong(String columnName) throws SQLiteException {
		return this.stmt.columnLong(sqliteQuery.getColumnIndex(columnName));
	}

	public byte[] columnBlob(Enum<?> enumeration) throws SQLiteException {
		return this.columnBlob(enumeration.name().toLowerCase());
	}

	public byte[] columnBlob(String columnName) throws SQLiteException {
		return this.stmt.columnBlob(sqliteQuery.getColumnIndex(columnName));
	}

	public InputStream columnStream(Enum<?> enumeration) throws SQLiteException {
		return this.columnStream(enumeration.name().toLowerCase());
	}

	public InputStream columnStream(String columnName) throws SQLiteException {
		return this.stmt.columnStream(sqliteQuery.getColumnIndex(columnName));
	}

	public boolean columnNull(Enum<?> enumeration) throws SQLiteException {
		return this.columnNull(enumeration.name().toLowerCase());
	}

	public boolean columnNull(String columnName) throws SQLiteException {
		return this.stmt.columnNull(sqliteQuery.getColumnIndex(columnName));
	}

}

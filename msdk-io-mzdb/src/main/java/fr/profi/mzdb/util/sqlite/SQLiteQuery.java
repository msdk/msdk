package fr.profi.mzdb.util.sqlite;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/**
 * @author David Bouyssie
 * 
 */
public class SQLiteQuery {

	private String queryString = null;
	private SQLiteStatement stmt = null;
	private SQLiteResultDescriptor resultDesc = null;

	public SQLiteQuery(SQLiteConnection connection, String sqlQuery, boolean cacheStmt)
			throws SQLiteException {
		super();

		this.queryString = sqlQuery;
		this.stmt = connection.prepare(sqlQuery, cacheStmt);

		HashMap<String, Integer> colIdxByColName = new HashMap<String, Integer>();

		int nbCols = stmt.columnCount();
		for (int colIdx = 0; colIdx < nbCols; colIdx++) {
			String colName = stmt.getColumnName(colIdx);
			colIdxByColName.put(colName, colIdx);
		}

		this.resultDesc = new SQLiteResultDescriptor(colIdxByColName);
	}

	public SQLiteQuery(SQLiteConnection connection, String sqlQuery) throws SQLiteException {
		this(connection, sqlQuery, true);
	}
	
	public int getColumnIndex(String colName) throws SQLiteException {
		if (resultDesc.colIdxByColName.containsKey(colName) == false) {
			throw new SQLiteException(-1, "undefined column '" + colName + "' in query: "+ this.queryString);
		}

		return resultDesc.colIdxByColName.get(colName);
	}
	

	public String[] getColumnNames() {
		return this.resultDesc.getColumnNames();
	}

	public SQLiteStatement getStatement() {
		return stmt;
	}

	public void dispose() {
		if (this.isStatementDisposed() == false) {
			this.stmt.dispose();
			this.stmt = null;
		}
	}

	public boolean isStatementDisposed() {
		if (this.stmt == null || this.stmt.isDisposed())
			return true;
		else
			return false;
	}
	

	public SQLiteQuery bind(int index, double value) throws SQLiteException {
		this.stmt.bind(index, value);
		return this;
	}

	public SQLiteQuery bind(int index, int value) throws SQLiteException {
		this.stmt.bind(index, value);
		return this;
	}

	public SQLiteQuery bind(int index, long value) throws SQLiteException {
		this.stmt.bind(index, value);
		return this;
	}

	public SQLiteQuery bind(int index, String value) throws SQLiteException {
		this.stmt.bind(index, value);
		return this;
	}

	public SQLiteQuery bind(int index, byte[] value) throws SQLiteException {
		this.stmt.bind(index, value);
		return this;
	}

	public SQLiteQuery bind(int index, byte[] value, int offset, int length) throws SQLiteException {
		this.stmt.bind(index, value, offset, length);
		return this;
	}

	public SQLiteQuery bindZeroBlob(int index, int length) throws SQLiteException {
		this.stmt.bindZeroBlob(index, length);
		return this;
	}

	public SQLiteQuery bindNull(int index) throws SQLiteException {
		this.stmt.bindNull(index);
		return this;
	}

	public OutputStream bindStream(int index) throws SQLiteException {
		return this.stmt.bindStream(index, 0);
	}

	public OutputStream bindStream(int index, int bufferSize) throws SQLiteException {
		return this.stmt.bindStream(index, bufferSize);
	}

	public SQLiteRecordIterator getRecordIterator() throws SQLiteException {
		return new SQLiteRecordIterator(this);
	}

	public void forEachRecord(ISQLiteRecordOperation op) throws SQLiteException {

		if (this.isStatementDisposed() == false) {

			// Iterate over each record
			int idx = 0;
			while (this.stmt.step()) {
				op.execute(new SQLiteRecord(this), idx);
				idx++;
			}

			// Dispose the statement
			this.dispose();
		}
	}
	
	public <T> T[] extractRecords(ISQLiteRecordExtraction<T> extractor, T[] records) throws SQLiteException {

		if (this.isStatementDisposed() == false) {

			int recordsCount = records.length;

			// Iterate over each record
			int idx = 0;
			while (this.stmt.step() && idx < recordsCount) {
				records[idx] = extractor.extract(new SQLiteRecord(this));
				idx++;
			}

			// Dispose the statement
			this.dispose();
		}

		return records;
	}
	
	public <T> List<T> extractRecordList(ISQLiteRecordExtraction<T> extractor) throws SQLiteException {

		List<T> records = new ArrayList<T>();
		
		if (this.isStatementDisposed() == false) {

			// Iterate over each record
			while (this.stmt.step()) {
				records.add(extractor.extract(new SQLiteRecord(this)));
			}

			// Dispose the statement
			this.dispose();
		}

		return records;
	}

	public <T> T extractRecord(ISQLiteRecordExtraction<T> extractor) throws SQLiteException {
		this.stmt.step();
		T obj = extractor.extract(new SQLiteRecord(this));
		this.dispose();
		return obj;
	}
	
	public String[] extractStrings(int bufferLength) throws SQLiteException {

		if (this.isStatementDisposed() == false) {
			
			String[] buffer = new String[bufferLength];

			// Iterate over each record
			int idx = 0;
			while (this.stmt.step()) {
				buffer[idx] = this.stmt.columnString(0);
				idx++;
			}

			// Dispose the statement
			this.dispose();
			
			return Arrays.copyOfRange(buffer, 0, idx - 1);
		} else {
			return new String[0];
		}
	}

	public int[] extractInts(int bufferLength) throws SQLiteException {

		int[] buffer = new int[bufferLength];
		int loadedInts = this.stmt.loadInts(0, buffer, 0, bufferLength);

		this.dispose();

		return Arrays.copyOfRange(buffer, 0, loadedInts - 1);
	}
	
	public long[] extractLongs(int bufferLength) throws SQLiteException {

		long[] buffer = new long[bufferLength];
		int loadedLongs = this.stmt.loadLongs(0, buffer, 0, bufferLength);

		this.dispose();

		return Arrays.copyOfRange(buffer, 0, loadedLongs - 1);
	}
	
	public float[] extractFloats(int bufferLength) throws SQLiteException {

		if (this.isStatementDisposed() == false) {
			
			float[] buffer = new float[bufferLength];

			// Iterate over each record
			int idx = 0;
			while (this.stmt.step()) {
				buffer[idx] = (float) this.stmt.columnDouble(0);
				idx++;
			}

			// Dispose the statement
			this.dispose();
			
			return Arrays.copyOfRange(buffer, 0, idx - 1);
		} else {
			return new float[0];
		}

	}
	
	public double[] extractDoubles(int bufferLength) throws SQLiteException {

		double[] buffer = null;

		if (this.isStatementDisposed() == false) {
			
			buffer = new double[bufferLength];

			// Iterate over each record
			int idx = 0;
			while (this.stmt.step()) {
				buffer[idx] = this.stmt.columnDouble(0);
				idx++;
			}

			// Dispose the statement
			this.dispose();
			
			return Arrays.copyOfRange(buffer, 0, idx - 1);
		} else {
			return new double[0];
		}
		
	}
	
	public String extractSingleString() throws SQLiteException {
		this.stmt.step();
		String result = this.stmt.columnString(0);
		this.dispose();
		return result;
	}

	public int extractSingleInt() throws SQLiteException {
		this.stmt.step();
		int result = this.stmt.columnInt(0);
		this.dispose();
		return result;
	}

	public long extractSingleLong() throws SQLiteException {
		this.stmt.step();
		long result = this.stmt.columnLong(0);
		this.dispose();
		return result;
	}
	
	public double extractSingleDouble() throws SQLiteException {
		this.stmt.step();
		double result = this.stmt.columnDouble(0);
		this.dispose();
		return result;
	}

	public byte[] extractSingleBlob() throws SQLiteException {
		this.stmt.step();
		if ( !stmt.hasRow() ) return null;
		byte[] result = this.stmt.columnBlob(0);
		this.dispose();
		return result;
	}

	public InputStream extractSingleInputStream() throws SQLiteException {
		this.stmt.step();
		InputStream result = this.stmt.columnStream(0);
		this.dispose();
		return result;
	}

}

package fr.profi.mzdb.util.sqlite;

import java.util.Iterator;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class SQLiteRecordIterator implements Iterator<SQLiteRecord> {

	protected final SQLiteQuery query;
	protected SQLiteStatement stmt;
	protected SQLiteRecord nextRecord = null;
	protected boolean _hasNext = false;
	protected boolean _hasNextChecked = false;

	public boolean isStatementDisposed() {
		if (this.stmt == null || this.stmt.isDisposed())
			return true;
		else
			return false;
	}

	public SQLiteRecordIterator(SQLiteQuery query) throws SQLiteException {
		super();
		this.query = query;
		this.stmt = query.getStatement();
		this.nextRecord = null;
	}

	public void dispose() {
		if (this.isStatementDisposed() == false && this.stmt != null) {
			this.stmt.dispose();
			this.stmt = null;
		}
	}

	public boolean hasNext() {

		try {
			if (_hasNextChecked == false) {
				this._hasNextChecked = true;

				if (stmt.step()) {
					this._hasNext = true;
				} else {
					this.dispose();
					this._hasNext = false;
				}
			}

			return this._hasNext;

		} catch (SQLiteException e) {
			e.printStackTrace();
			return false;
		}
	}

	public SQLiteRecord next() {

		try {
			if (this.hasNext()) {
				this.nextRecord = new SQLiteRecord(this.query);
				this._hasNextChecked = false;
				return nextRecord;
			} else {
				return null;
			}
		} catch (Exception e) {
			// this.nextElem = null;
			// don't throw exception => we have a problem with the statement which is
			// closing automatically
			// TODO: find a safe way to check if the statement has been closed
			// rethrow(e);
			return null;// obj;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException("Unsupported Operation");
	}

	protected void rethrow(SQLiteException e) {
		throw new RuntimeException(e.getMessage());
	}

}

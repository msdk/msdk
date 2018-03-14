package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public abstract class AbstractStatementIterator<E> implements Iterator<E>, IStatementExtractor<E> {
	
	protected final Logger logger = LoggerFactory.getLogger(AbstractStatementIterator.class);

	protected final SQLiteStatement statement;
	protected boolean isStatementClosed = false;
	protected E nextElem = null;

	/*
	 * public boolean isStatementClosed() { return isStatementClosed; }
	 */

	public AbstractStatementIterator(SQLiteStatement stmt) throws SQLiteException, StreamCorruptedException {
		super();
		this.statement = stmt;

		nextElem = null;
	}

	public void closeStatement() {
		statement.dispose();

		// if (! statement.isDisposed() ) {//!isStatementClosed) {
		// if (statement != null) {
		// isStatementClosed = true;
		// }
		// }
	}

	public boolean hasNext() {
		try {
			if (statement.step()) {
				return true;
			} else {
				this.closeStatement();
				return false;
			}

		} catch (SQLiteException e) {
			logger.error("SQLiteException has been caught: ", e);
			return false;
		}
	}

	public E next() {

		try {
			this.nextElem = this.extractObject(statement);
			return nextElem;
		} catch (Exception e) {
			logger.error("Exception has been caught: ", e);
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

	/*protected void rethrow(SQLiteException e) {
		throw new RuntimeException(e.getMessage());
	}*/

}

package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import fr.profi.mzdb.io.reader.cache.AbstractDataEncodingReader;
import fr.profi.mzdb.io.reader.cache.AbstractSpectrumHeaderReader;
import fr.profi.mzdb.model.BoundingBox;
import fr.profi.mzdb.util.sqlite.ISQLiteStatementConsumer;

public abstract class AbstractSpectrumSliceIterator {

	protected final SQLiteStatement statement;
	protected final BoundingBoxIterator boundingBoxIterator;
	protected BoundingBox firstBB;
	
	public AbstractSpectrumSliceIterator(
		AbstractSpectrumHeaderReader spectrumHeaderReader,
		AbstractDataEncodingReader dataEncodingReader,
		SQLiteConnection connection,
		String sqlQuery
	) throws SQLiteException, StreamCorruptedException {
		
		// Create a new statement (will be automatically closed by the StatementIterator)
		SQLiteStatement stmt = connection.prepare(sqlQuery, true); // true = cached enabled

		// Set some fields
		this.boundingBoxIterator = new BoundingBoxIterator(
			spectrumHeaderReader,
			dataEncodingReader,
			connection,
			stmt
		);
		this.statement = stmt;

		initBB();
	}
	
	public AbstractSpectrumSliceIterator(
		AbstractSpectrumHeaderReader spectrumHeaderReader,
		AbstractDataEncodingReader dataEncodingReader,
		SQLiteConnection connection,
		String sqlQuery,
		int msLevel,
		ISQLiteStatementConsumer stmtBinder
	) throws SQLiteException, StreamCorruptedException {
		
		// Create a new statement (will be automatically closed by the StatementIterator)
		SQLiteStatement stmt = connection.prepare(sqlQuery, true); // true = cached enabled
		
		// Call the statement binder
		stmtBinder.accept(stmt);
		
		// Set some fields
		this.boundingBoxIterator = new BoundingBoxIterator(
			spectrumHeaderReader,
			dataEncodingReader,
			connection,
			stmt,
			msLevel
		);
		this.statement = stmt;

		initBB();
	}

	public SQLiteStatement getStatement() {
		return this.statement;
	}

	protected void initBB() {
		if (boundingBoxIterator.hasNext())
			this.firstBB = boundingBoxIterator.next();
		else {
			this.firstBB = null;
		}
	}

	public void closeStatement() {
		statement.dispose();
	}

	public boolean hasNext() {

		if (this.firstBB != null) { // this.statement.hasRow() ) {//
			return true;
		} else {
			this.closeStatement();
			return false;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException("Unsuported Operation");
	}

}

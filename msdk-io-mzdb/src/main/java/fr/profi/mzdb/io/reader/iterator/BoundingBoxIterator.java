package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.Map;

import fr.profi.mzdb.io.reader.bb.BoundingBoxBuilder;
import fr.profi.mzdb.io.reader.cache.AbstractDataEncodingReader;
import fr.profi.mzdb.io.reader.cache.AbstractSpectrumHeaderReader;
import fr.profi.mzdb.model.BoundingBox;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumHeader;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class BoundingBoxIterator extends AbstractStatementIterator<BoundingBox> {

	protected final Map<Long, SpectrumHeader> spectrumHeaderById;
	protected final Map<Long, DataEncoding> dataEncodingBySpectrumId;
	
	public BoundingBoxIterator(
		AbstractSpectrumHeaderReader spectrumHeaderReader,
		AbstractDataEncodingReader dataEncodingReader,
		SQLiteConnection connection,
		SQLiteStatement stmt
	) throws SQLiteException, StreamCorruptedException {
		super(stmt);
		
		this.spectrumHeaderById = spectrumHeaderReader.getSpectrumHeaderById(connection);			
		this.dataEncodingBySpectrumId = dataEncodingReader.getDataEncodingBySpectrumId(connection);
	}
	
	public BoundingBoxIterator(
		AbstractSpectrumHeaderReader spectrumHeaderReader,
		AbstractDataEncodingReader dataEncodingReader,
		SQLiteConnection connection,
		SQLiteStatement stmt,
		int msLevel
	) throws SQLiteException, StreamCorruptedException {
		super(stmt);
		
		if( msLevel == 1 ) this.spectrumHeaderById = spectrumHeaderReader.getMs1SpectrumHeaderById(connection);
		else if( msLevel == 2 ) this.spectrumHeaderById = spectrumHeaderReader.getMs2SpectrumHeaderById(connection);
		else if( msLevel == 3 ) this.spectrumHeaderById = spectrumHeaderReader.getMs3SpectrumHeaderById(connection);
		else throw new IllegalArgumentException("unsupported MS level: " + msLevel);
		
		this.dataEncodingBySpectrumId = dataEncodingReader.getDataEncodingBySpectrumId(connection);
	}

	public BoundingBox extractObject(SQLiteStatement stmt) throws SQLiteException, StreamCorruptedException {

		int bbId = stmt.columnInt(0);
		byte[] bbBytes = stmt.columnBlob(1);
		int runSliceId = stmt.columnInt(2);
		int firstSpectrumId = stmt.columnInt(3);
		int lastSpectrumId = stmt.columnInt(4);

		BoundingBox bb = BoundingBoxBuilder.buildBB(
			bbId,
			bbBytes,
			firstSpectrumId,
			lastSpectrumId,
			this.spectrumHeaderById,
			this.dataEncodingBySpectrumId
		);		
		bb.setRunSliceId(runSliceId);
		
		return bb;
	}

}

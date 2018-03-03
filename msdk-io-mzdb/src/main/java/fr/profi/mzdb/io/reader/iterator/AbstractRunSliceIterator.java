package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.io.reader.cache.AbstractDataEncodingReader;
import fr.profi.mzdb.io.reader.cache.AbstractRunSliceHeaderReader;
import fr.profi.mzdb.io.reader.cache.AbstractSpectrumHeaderReader;
import fr.profi.mzdb.model.*;
import fr.profi.mzdb.util.sqlite.ISQLiteStatementConsumer;

public abstract class AbstractRunSliceIterator extends AbstractSpectrumSliceIterator implements Iterator<RunSlice> {

	protected SpectrumSlice[] spectrumSliceBuffer = null;
	protected boolean bbHasNext = true;
	protected final HashMap<Integer, RunSliceHeader> runSliceHeaderById;
	
	public AbstractRunSliceIterator(
		AbstractRunSliceHeaderReader runSliceHeaderReader,
		AbstractSpectrumHeaderReader spectrumHeaderReader,
		AbstractDataEncodingReader dataEncodingReader,
		SQLiteConnection connection,
		String sqlQuery,
		int msLevel,
		ISQLiteStatementConsumer stmtBinder
	) throws SQLiteException, StreamCorruptedException {
		super(spectrumHeaderReader, dataEncodingReader, connection, sqlQuery, msLevel, stmtBinder);
		
		this.runSliceHeaderById = runSliceHeaderReader.getRunSliceHeaderById(msLevel, connection);
	}
	
	protected void initSpectrumSliceBuffer() {
		
		this.spectrumSliceBuffer = this.firstBB.toSpectrumSlices();
		ArrayList<SpectrumSlice> sl = new ArrayList<SpectrumSlice>(Arrays.asList(this.spectrumSliceBuffer));

		while (bbHasNext = boundingBoxIterator.hasNext()) {
			BoundingBox bb = boundingBoxIterator.next();

			if (bb.getRunSliceId() == this.firstBB.getRunSliceId()) {
				sl.addAll(Arrays.asList(bb.toSpectrumSlices()));
			} else {
				this.firstBB = bb;
				break;
			}
		}

		this.spectrumSliceBuffer = sl.toArray(new SpectrumSlice[sl.size()]);

		if (!bbHasNext) {
			this.firstBB = null;
		}
	}

	public RunSlice next() {

		initSpectrumSliceBuffer();

		int runSliceId = this.spectrumSliceBuffer[0].getRunSliceId();
		RunSliceData rsd = new RunSliceData(runSliceId, this.spectrumSliceBuffer);
		// rsd.buildPeakListBySpectrumId();

		RunSliceHeader rsh = this.runSliceHeaderById.get(runSliceId);

		return new RunSlice(rsh, rsd);
	}

}

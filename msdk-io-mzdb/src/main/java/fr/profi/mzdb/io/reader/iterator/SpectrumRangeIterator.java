package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.Iterator;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.io.reader.bb.IBlobReader;
import fr.profi.mzdb.io.reader.cache.AbstractSpectrumHeaderReader;
import fr.profi.mzdb.model.BoundingBox;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.sqlite.ISQLiteStatementConsumer;

//import static fr.profi.mzdb.utils.lambda.JavaStreamExceptionWrappers.rethrowConsumer;

/**
 * @author Marco
 *
 */
public class SpectrumRangeIterator implements Iterator<Spectrum> {

	private int bbStartingSpectrumId;

	private int bbEndingSpectrumId;

	private int wantedStartingSpectrumId;

	private int wantedEndingSpectrumId;

	//private AbstractMzDbReader mzDbReader;

	private MsSpectrumRangeIteratorImpl _iter;

	private Long currentId;

	private String sqlQuery;

	private Long trueLastSpectrumId = 0L;

	// private boolean toStop = false;

	public SpectrumRangeIterator(AbstractMzDbReader mzDbReader, SQLiteConnection connection, int msLevel, int start, int end) throws SQLiteException, StreamCorruptedException {
		AbstractSpectrumHeaderReader spectrumHeaderReader = mzDbReader.getSpectrumHeaderReader();
		
		//this.mzDbReader = mzDbReader;
		this.wantedStartingSpectrumId = start;
		this.wantedEndingSpectrumId = end;
		this.bbStartingSpectrumId = spectrumHeaderReader.getSpectrumHeader(start, connection).getBBFirstSpectrumId();
		this.bbEndingSpectrumId = spectrumHeaderReader.getSpectrumHeader(end, connection).getBBFirstSpectrumId();
		sqlQuery = "SELECT bounding_box.* FROM bounding_box, spectrum WHERE spectrum.id = bounding_box.first_spectrum_id AND spectrum.ms_level= ? AND "
			+ "bounding_box.first_spectrum_id >= "
			+ this.bbStartingSpectrumId
			+ " AND bounding_box.first_spectrum_id <= " + this.bbEndingSpectrumId;

		this._iter = new MsSpectrumRangeIteratorImpl(mzDbReader, connection, msLevel);
	}

	public class MsSpectrumRangeIteratorImpl extends AbstractSpectrumSliceIterator implements Iterator<Spectrum> {

		protected int spectrumSliceIdx;

		protected SpectrumSlice[] spectrumSliceBuffer = null;
		protected boolean bbHasNext = true;

		public MsSpectrumRangeIteratorImpl(AbstractMzDbReader mzDbReader, SQLiteConnection connection, final int msLevel) throws SQLiteException,
				StreamCorruptedException {
			//super(mzDbReader, sqlQuery, msLevel, rethrowConsumer( (stmt) -> stmt.bind(1, msLevel) ) ); // Bind msLevel
			super(
				mzDbReader.getSpectrumHeaderReader(),
				mzDbReader.getDataEncodingReader(),
				connection,
				sqlQuery,
				msLevel,
				new ISQLiteStatementConsumer() {
					public void accept(SQLiteStatement stmt) throws SQLiteException {
						stmt.bind(1, msLevel); // Bind msLevel
					}
				}
			);
			
			this.initSpectrumSliceBuffer();
		}

		protected void initSpectrumSliceBuffer() {
			this.spectrumSliceBuffer = this.firstBB.toSpectrumSlices();

			this.spectrumSliceIdx = 0;

			// Build spectrum slice buffer
			while (bbHasNext = boundingBoxIterator.hasNext()) {// bbHasNext=

				BoundingBox bb = boundingBoxIterator.next();
				IBlobReader bbReader = bb.getReader();

				if (bb.getLastSpectrumId() > wantedEndingSpectrumId) {
					// FIXME: DBO => it was (bb.nbSpectra() - 2) when idOfSpectrumAt was based on a 1 value starting index
					// It may cause some issues in the future
					trueLastSpectrumId = (long) bbReader.getSpectrumIdAt(bb.getSpectraCount() - 1);
				} else if (bb.getLastSpectrumId() == wantedEndingSpectrumId) {
					trueLastSpectrumId = bb.getLastSpectrumId();
				} else {

				}
				SpectrumSlice[] sSlices = (SpectrumSlice[]) bb.toSpectrumSlices();

				if (sSlices == null)
					continue;

				if (sSlices[0].getSpectrumId() == spectrumSliceBuffer[0].getSpectrumId()) {
					for (int i = 0; i < sSlices.length; i++) {
						spectrumSliceBuffer[i].getData().addSpectrumData(sSlices[i].getData());
					}
				} else {
					// Keep this bounding box for next iteration
					this.firstBB = bb;
					break;
				}
			}
		}

		@Override
		public Spectrum next() {

			// firstSpectrumSlices is not null
			int c = spectrumSliceIdx;
			spectrumSliceIdx++;

			SpectrumSlice sSlice = spectrumSliceBuffer[c];

			if (spectrumSliceIdx == spectrumSliceBuffer.length) {
				if (bbHasNext)
					initSpectrumSliceBuffer();
				else
					this.firstBB = null;
			}
			if (sSlice.getSpectrumId() < wantedStartingSpectrumId) {
				return null;
			} else if (sSlice.getSpectrumId() > bbEndingSpectrumId && sSlice.getSpectrumId() < wantedEndingSpectrumId) {
				this.firstBB = null;
				currentId = sSlice.getSpectrumId();
				return sSlice;
			} else if (sSlice.getSpectrumId() == wantedEndingSpectrumId) {
				// toStop = true;
				currentId = sSlice.getSpectrumId();
				return sSlice;
			} else if (sSlice.getSpectrumId() > wantedEndingSpectrumId) {
				// toStop = true;
				currentId = null;
				return null;
			} else {
				return sSlice;// do nothing
			}

		}

	};

	@Override
	public boolean hasNext() {
		if (currentId != null && currentId.equals(trueLastSpectrumId))
			return false;
		return true;
	}

	@Override
	public Spectrum next() {
		Spectrum sSlice = _iter.next();
		while (_iter.hasNext() && sSlice == null) {// && ! toStop) {
			sSlice = _iter.next();
		}
		currentId = sSlice.getHeader().getId();
		return sSlice;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
	};
}

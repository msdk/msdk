package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.model.BoundingBox;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.profi.mzdb.util.sqlite.ISQLiteStatementConsumer;

//import static fr.profi.mzdb.utils.lambda.JavaStreamExceptionWrappers.rethrowConsumer;

public class SpectrumIterator extends AbstractSpectrumSliceIterator implements Iterator<Spectrum> {

	private static String allMsLevelsSqlQuery = "SELECT bounding_box.* FROM bounding_box, spectrum WHERE spectrum.id = bounding_box.first_spectrum_id";
	private static String singleMsLevelSqlQuery = allMsLevelsSqlQuery + " AND spectrum.ms_level= ?";
	private static int PRIORITY_QUEUE_INITIAL_CAPACITY = 200;
	
	protected int spectrumSliceIdx;
	protected final boolean usePriorityQueue;
	private final PriorityQueue<Spectrum> priorityQueue;

	protected SpectrumSlice[] firstSpectrumSlices = null;
	protected SpectrumSlice[] spectrumSliceBuffer = null;
	protected boolean bbHasNext = true;
	
	public SpectrumIterator(AbstractMzDbReader mzDbReader, SQLiteConnection connection) throws SQLiteException, StreamCorruptedException {
		super(mzDbReader.getSpectrumHeaderReader(), mzDbReader.getDataEncodingReader(), connection, allMsLevelsSqlQuery);

		this.usePriorityQueue = true;
		this.priorityQueue = new PriorityQueue<Spectrum>(PRIORITY_QUEUE_INITIAL_CAPACITY, new Comparator<Spectrum>() {
			public int compare(Spectrum s1, Spectrum s2) {
				return (int) (s1.getHeader().getId() - s2.getHeader().getId());
			}
		});

		this.initSpectrumSliceBuffer();
	}

	public SpectrumIterator(AbstractMzDbReader mzDbReader, SQLiteConnection connection, final int msLevel) throws SQLiteException, StreamCorruptedException {
		//super(inst, sqlQuery, msLevel, rethrowConsumer( (stmt) -> stmt.bind(1, msLevel) ) ); // Bind msLevel
		super(
			mzDbReader.getSpectrumHeaderReader(),
			mzDbReader.getDataEncodingReader(),
			connection,
			singleMsLevelSqlQuery,
			msLevel,
			new ISQLiteStatementConsumer() {
				public void accept(SQLiteStatement stmt) throws SQLiteException {
					stmt.bind(1, msLevel); // Bind msLevel
				}
			}
		);
		
		this.usePriorityQueue = false;
		this.priorityQueue = null;

		this.initSpectrumSliceBuffer();
	}

	protected void initSpectrumSliceBuffer() {
		
		if (this.firstSpectrumSlices != null) {
			this.spectrumSliceBuffer = this.firstSpectrumSlices;
		} else {
			this.spectrumSliceBuffer = this.firstBB.toSpectrumSlices();
		}
		
		this.spectrumSliceIdx = 0;
		
		boolean continueSlicesLoading = false;

		// Build spectrum slice buffer
		while (bbHasNext = boundingBoxIterator.hasNext()) {

			BoundingBox bb = boundingBoxIterator.next();
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
				this.firstSpectrumSlices = sSlices;
				
				if (usePriorityQueue) {
					// Put the loaded spectra in the priority queue
					for (Spectrum spectrum : spectrumSliceBuffer) {
						priorityQueue.add(spectrum);
					}
					
					SpectrumHeader curSpectrumHeader = spectrumSliceBuffer[0].getHeader();
					SpectrumHeader nextSpectrumHeader = sSlices[0].getHeader();
					int nextMsLevel = nextSpectrumHeader.getMsLevel();
						
					// Check if we need to continue loading the spectrum slices
					if (curSpectrumHeader.getCycle() == nextSpectrumHeader.getCycle() ||
						curSpectrumHeader.getMsLevel() == nextMsLevel || nextMsLevel > 1) {
						continueSlicesLoading = true;
					}
				}
				
				break;
			}
		}
		
		if (continueSlicesLoading) {
			this.initSpectrumSliceBuffer();
		}
	}

	public Spectrum next() {

		// firstSpectrumSlices is not null
		int spectrumSliceIdxCopy = spectrumSliceIdx;
		spectrumSliceIdx++;

		Spectrum spectrum = null;
		boolean noMoreSpectrum = false;
		if (usePriorityQueue) {
			spectrum = priorityQueue.poll();
			noMoreSpectrum = priorityQueue.isEmpty();
		} else {
			spectrum = spectrumSliceBuffer[spectrumSliceIdxCopy];
			noMoreSpectrum = spectrumSliceIdx == spectrumSliceBuffer.length;
		}

		// If no more spectrum slices
		if (noMoreSpectrum) {
			if (bbHasNext)
				initSpectrumSliceBuffer();
			else
				this.firstBB = null;
		}

		return spectrum;

	}

}
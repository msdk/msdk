package fr.profi.mzdb.io.reader.provider;

import java.io.StreamCorruptedException;
import java.util.Iterator;

import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.model.RunSliceData;

import com.almworks.sqlite4java.SQLiteException;

// TODO: Auto-generated Javadoc
/**
 * The Class RunSliceDataProvider.
 * 
 * @author David Bouyssie
 */
public class RunSliceDataProvider {

	/** The mz db instance. */
	MzDbReader mzDBInstance;

	/** The rsd iter. */
	Iterator<RunSlice> rsdIter;

	/**
	 * Instantiates a new run slice data provider.
	 * 
	 * @param mzDBInstance
	 *            the mz db instance
	 */
	/*public RunSliceDataProvider(MzDbReader mzDBInstance) {
		super();
		this.mzDBInstance = mzDBInstance;
	}*/

	/**
	 * Instantiates a new run slice data provider.
	 * 
	 * @param rsdIter
	 *            the rsd iter
	 */
	public RunSliceDataProvider(Iterator<RunSlice> rsdIter) {
		super();
		this.rsdIter = rsdIter;
	}

	/**
	 * Gets the run slice data.
	 * 
	 * @param runSliceId
	 *            the run slice id
	 * @return the run slice data
	 * @throws SQLiteException
	 *             the sQ lite exception
	 * @throws StreamCorruptedException 
	 */
	public RunSliceData getRunSliceData(int runSliceNumber) throws SQLiteException, StreamCorruptedException {
	    RunSliceData data =  this._getNextMatchingRunSliceData(runSliceNumber);
	    if (data == null) {
	      System.out.println("Non optimal run slice data fetching");
	      return mzDBInstance.getRunSliceData(runSliceNumber);
	    }
	    return data;
	}

	/**
	 * _get next matching run slice data.
	 * 
	 * @param runSliceNumber
	 *            the run slice number
	 * @return the run slice data
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	private RunSliceData _getNextMatchingRunSliceData(int runSliceNumber) throws SQLiteException {

		// Iterate over run slices to retrieve the wanted run slice
		
		while (rsdIter.hasNext()) {
			RunSlice tmpRs = rsdIter.next();
			// return tmpRs.getData();

			int curRsNumber = tmpRs.getHeader().getNumber();

			if (curRsNumber == runSliceNumber)
				return tmpRs.getData();
			else if (curRsNumber > runSliceNumber) 
				return null;
		}
		return null;
	}
}
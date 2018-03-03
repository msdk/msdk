package fr.profi.mzdb.io.reader.iterator;

import java.io.StreamCorruptedException;
import java.util.Iterator;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import fr.profi.mzdb.AbstractMzDbReader;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.util.sqlite.ISQLiteStatementConsumer;

//import static fr.profi.mzdb.utils.lambda.JavaStreamExceptionWrappers.rethrowConsumer;

/** Class used for DIA/SWATH data **/
public class LcMsnRunSliceIterator extends AbstractRunSliceIterator implements Iterator<RunSlice> {

	private static String sameIsolationWindowRunSlicesSqlQuery = "SELECT bounding_box.* FROM bounding_box, bounding_box_msn_rtree, run_slice "
			+ "WHERE bounding_box_msn_rtree.id = bounding_box.id "
			+ "AND bounding_box.run_slice_id = run_slice.id "
			+ "AND run_slice.ms_level = ? "
			+ "AND bounding_box_msn_rtree.max_parent_mz >= ? "
			+ "AND bounding_box_msn_rtree.min_parent_mz <= ? " + "ORDER BY run_slice.begin_mz";
	
	private static String sameIsolationWindowRunSlicesSubsetSqlQuery = "SELECT bounding_box.* FROM bounding_box, bounding_box_msn_rtree, run_slice "
			+ "WHERE bounding_box_msn_rtree.id = bounding_box.id "
			+ "AND bounding_box.run_slice_id = run_slice.id "
			+ "AND run_slice.ms_level = ? "
			+ "AND run_slice.end_mz >= ? "
			+ "AND run_slice.begin_mz <= ? "
			+ "AND bounding_box_msn_rtree.max_parent_mz >= ? "
			+ "AND bounding_box_msn_rtree.min_parent_mz <= ? "
			+ "ORDER BY run_slice.begin_mz";

	public LcMsnRunSliceIterator(
		AbstractMzDbReader mzDbReader,
		SQLiteConnection connection,
		final double minParentMz,
		final double maxParentMz
	) throws SQLiteException, StreamCorruptedException {
		/*super(mzDbReader, allRunSlicesSqlQuery, 2, rethrowConsumer( (stmt) -> {
			// Lambda require to catch Exceptions
			// For workarounds see: http://stackoverflow.com/questions/14039995/java-8-mandatory-checked-exceptions-handling-in-lambda-expressions-why-mandato		
			stmt.bind(1, 2); // Bind the msLevel
			stmt.bind(2, minParentMz); // Bind the minParentMz
			stmt.bind(3, maxParentMz); // Bind the maxParentMz
		}) );*/
		
		// Set msLevel to 2
		// FIXME: what about msLevel > 2 ?
		super(
			mzDbReader.getRunSliceHeaderReader(),
			mzDbReader.getSpectrumHeaderReader(),
			mzDbReader.getDataEncodingReader(),
			connection,
			sameIsolationWindowRunSlicesSqlQuery,
			2,
			new ISQLiteStatementConsumer() {
				public void accept(SQLiteStatement stmt) throws SQLiteException {
					stmt.bind(1, 2); // Bind the msLevel
					stmt.bind(2, minParentMz); // Bind the minParentMz
					stmt.bind(3, maxParentMz); // Bind the maxParentMz
				}
			}
		);
	}

	public LcMsnRunSliceIterator(
		AbstractMzDbReader mzDbReader,
		SQLiteConnection connection,
		final double minParentMz,
		final double maxParentMz,
		final double minRunSliceMz,
		final double maxRunSliceMz
	) throws SQLiteException, StreamCorruptedException {

		/*super(mzDbReader, runSlicesSubsetSqlQuery, 2, rethrowConsumer( (stmt) -> {
			// Lambda require to catch Exceptions
			// For workarounds see: http://stackoverflow.com/questions/14039995/java-8-mandatory-checked-exceptions-handling-in-lambda-expressions-why-mandato
			stmt.bind(1, 2); // Bind the msLevel
			stmt.bind(2, minParentMz); // Bind the minParentMz
			stmt.bind(3, maxParentMz); // Bind the maxParentMz
			stmt.bind(4, minRunSliceMz); // Bind the minRunSliceMz
			stmt.bind(5, maxRunSliceMz); // Bind the maxRunSliceMz
		}) );*/
		
		// Set msLevel to 2
		// FIXME: what about msLevel > 2 ?
		super(
			mzDbReader.getRunSliceHeaderReader(),
			mzDbReader.getSpectrumHeaderReader(),
			mzDbReader.getDataEncodingReader(),
			connection,
			sameIsolationWindowRunSlicesSubsetSqlQuery,
			2,
			new ISQLiteStatementConsumer() {
				public void accept(SQLiteStatement stmt) throws SQLiteException {
					stmt.bind(1, 2); // Bind the msLevel
					stmt.bind(2, minParentMz); // Bind the minParentMz
					stmt.bind(3, maxParentMz); // Bind the maxParentMz
					stmt.bind(4, minRunSliceMz); // Bind the minRunSliceMz
					stmt.bind(5, maxRunSliceMz); // Bind the maxRunSliceMz
				}
			}
		);
	}

}

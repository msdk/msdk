package fr.profi.mzdb.io.reader.table;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.MzDbHeader;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.db.table.MzdbTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteQuery;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class MzDbHeaderReader.
 * 
 * @author David Bouyssie
 */
public class MzDbHeaderReader extends AbstractTableModelReader<MzDbHeader> {

	/**
	 * Instantiates a new mzDB header reader.
	 * 
	 * @param connection the SQLite connection
	 */
	public MzDbHeaderReader(SQLiteConnection connection) {
		super(connection);
	}
	
	protected ISQLiteRecordExtraction<MzDbHeader> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<MzDbHeader>() {

			public MzDbHeader extract(SQLiteRecord r) throws SQLiteException {

				String version = r.columnString(MzdbTable.VERSION);
				int creationTimestamp = r.columnInt(MzdbTable.CREATION_TIMESTAMP);
				String paramTreeAsStr = r.columnString(MzdbTable.PARAM_TREE);
				ParamTree paramTree = ParamTreeParser.parseParamTree(paramTreeAsStr);

				return new MzDbHeader(version, creationTimestamp, paramTree);
			}
		};
	}

	/**
	 * Gets the mz db header.
	 * 
	 * @return the mz db header
	 * @throws SQLiteException the SQLite exception
	 */
	public MzDbHeader getMzDbHeader() throws SQLiteException {
		return new SQLiteQuery(connection, "SELECT * FROM " + MzdbTable.tableName).extractRecord(recordExtractor);
	}

}

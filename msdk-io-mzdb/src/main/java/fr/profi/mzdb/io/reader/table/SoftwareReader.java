package fr.profi.mzdb.io.reader.table;

import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.Software;
import fr.profi.mzdb.db.table.SoftwareTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class SoftwareReader.
 * 
 * @author David Bouyssie
 */
public class SoftwareReader extends AbstractTableModelReader<Software> {

	/**
	 * Instantiates a new software reader.
	 * 
	 * @param connection the connection
	 */
	public SoftwareReader(SQLiteConnection connection) throws SQLiteException {
		super(connection);
	}
	
	protected ISQLiteRecordExtraction<Software> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<Software>() {

			public Software extract(SQLiteRecord r) throws SQLiteException {

				int id = r.columnInt(SoftwareTable.ID);
				String name = r.columnString(SoftwareTable.NAME);
				String version = r.columnString(SoftwareTable.VERSION);
				String paramTreeAsStr = r.columnString(SoftwareTable.PARAM_TREE);

				return new Software(id, name, version, ParamTreeParser.parseParamTree(paramTreeAsStr));
			}
		};
	}

	/**
	 * Gets the software.
	 * 
	 * @param id the id
	 * @return the software
	 * @throws SQLiteException the SQLite Exception
	 */
	public Software getSoftware(int id) throws SQLiteException {
		return getRecord(Software.TABLE_NAME, id);
	}
	
	public List<Software> getSoftwareList() throws SQLiteException {
		return getRecordList(Software.TABLE_NAME);
	}
	
}

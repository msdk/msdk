package fr.profi.mzdb.io.reader.table;

import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.SourceFile;
import fr.profi.mzdb.db.table.SourceFileTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc

/**
 * The Class SourceFileReader.
 * 
 * @author David Bouyssie
 */
public class SourceFileReader extends AbstractTableModelReader<SourceFile> {
	/**
	 * Instantiates a new source file reader.
	 * 
	 * @param connection
	 *            the connection
	 */
	public SourceFileReader(SQLiteConnection connection) throws SQLiteException {
		super(connection);
	}

	protected ISQLiteRecordExtraction<SourceFile> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<SourceFile>() {

			public SourceFile extract(SQLiteRecord r) throws SQLiteException {

				int id = r.columnInt(SourceFileTable.ID);
				String name = r.columnString(SourceFileTable.NAME);
				String location = r.columnString(SourceFileTable.LOCATION);
				String paramTreeAsStr = r.columnString(SourceFileTable.PARAM_TREE);

				return new SourceFile(id, name, location, ParamTreeParser.parseParamTree(paramTreeAsStr));
			}
		};
	}

	public SourceFile getSourceFile(int id) throws SQLiteException {
		return getRecord(SourceFile.TABLE_NAME, id);
	}
	
	public List<SourceFile> getSourceFileList() throws SQLiteException {
		return getRecordList(SourceFile.TABLE_NAME);
	}
	
}

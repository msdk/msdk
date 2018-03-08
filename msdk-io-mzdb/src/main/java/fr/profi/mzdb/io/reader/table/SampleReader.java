package fr.profi.mzdb.io.reader.table;

import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.Sample;
import fr.profi.mzdb.db.table.SampleTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc

/**
 * The Class SampleReader.
 * 
 * @author David Bouyssie
 */
public class SampleReader extends AbstractTableModelReader<Sample> {

	public SampleReader(SQLiteConnection connection) {
		super(connection);
	}

	protected ISQLiteRecordExtraction<Sample> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<Sample>() {

			public Sample extract(SQLiteRecord r) throws SQLiteException {

				int id = r.columnInt(SampleTable.ID);
				String name = r.columnString(SampleTable.NAME);
				String paramTreeAsStr = r.columnString(SampleTable.PARAM_TREE);

				return new Sample(id, name, ParamTreeParser.parseParamTree(paramTreeAsStr));
			}
		};
	}

	public Sample getSample(int id) throws SQLiteException {
		return getRecord(Sample.TABLE_NAME, id);
	}
	
	public List<Sample> getSampleList() throws SQLiteException {
		return getRecordList(Sample.TABLE_NAME);
	}
	
}

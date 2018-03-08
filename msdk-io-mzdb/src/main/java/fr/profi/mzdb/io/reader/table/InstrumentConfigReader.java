package fr.profi.mzdb.io.reader.table;

import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import fr.profi.mzdb.db.model.InstrumentConfiguration;
import fr.profi.mzdb.db.table.InstrumentConfigurationTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class InstrumentConfigReader.
 * 
 * @author David Bouyssie
 */
public class InstrumentConfigReader extends AbstractTableModelReader<InstrumentConfiguration> {
	
	/**
	 * Instantiates a new instrument config reader.
	 * 
	 * @param connection
	 *            the connection
	 */
	public InstrumentConfigReader(SQLiteConnection connection) {
		super(connection);
	}
	
	protected ISQLiteRecordExtraction<InstrumentConfiguration> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<InstrumentConfiguration>() {
			public InstrumentConfiguration extract(SQLiteRecord r) throws SQLiteException {
				
				int id = r.columnInt(InstrumentConfigurationTable.ID);
				String name = r.columnString(InstrumentConfigurationTable.NAME);
				int softwareId = r.columnInt(InstrumentConfigurationTable.SOFTWARE_ID);
				String paramTreeAsStr = r.columnString(InstrumentConfigurationTable.PARAM_TREE);
				String insConfAsStr = r.columnString(InstrumentConfigurationTable.COMPONENT_LIST);
				
				return new InstrumentConfiguration(id, name, softwareId, ParamTreeParser
						.parseParamTree(paramTreeAsStr), ParamTreeParser
						.parseComponentList(insConfAsStr));
			}
		};
	}

	/**
	 * Gets the instrument config.
	 * 
	 * @param id
	 *            the id
	 * @return the instrument config
	 * @throws SQLiteException
	 *             the sQ lite exception
	 */
	public InstrumentConfiguration getInstrumentConfig(int id) throws SQLiteException {		
		return getRecord(InstrumentConfiguration.TABLE_NAME, id);
	}
	
	public List<InstrumentConfiguration> getInstrumentConfigList() throws SQLiteException {
		return getRecordList(InstrumentConfiguration.TABLE_NAME);
	}
}

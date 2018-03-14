package fr.profi.mzdb.io.reader.table;

//import java.time.Instant;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.profi.mzdb.db.model.Run;
import fr.profi.mzdb.db.table.RunTable;
import fr.profi.mzdb.util.sqlite.ISQLiteRecordExtraction;
import fr.profi.mzdb.util.sqlite.SQLiteRecord;

// TODO: Auto-generated Javadoc

/**
 * The Class RunReader.
 * 
 * @author David Bouyssie
 */
public class RunReader extends AbstractTableModelReader<Run> {
	
	final Logger logger = LoggerFactory.getLogger(RunReader.class);

	/**
	 * Instantiates a new source file reader.
	 * 
	 * @param connection
	 *            the connection
	 */
	public RunReader(SQLiteConnection connection) throws SQLiteException {
		super(connection);
	}

	protected ISQLiteRecordExtraction<Run> buildRecordExtractor() {
		return new ISQLiteRecordExtraction<Run>() {
	
			public Run extract(SQLiteRecord r) throws SQLiteException {
	
				int id = r.columnInt(RunTable.ID);
				String name = r.columnString(RunTable.NAME);
				// FIXME: switch to Instant when Java 8 is supported
				//Instant startTimestamp = Instant.parse( r.columnString(RunTable.START_TIMESTAMP));
				String startTimestampAsStr = r.columnString(RunTable.START_TIMESTAMP);
				Date startTimestamp = null;
				try {
					startTimestamp = DateUtils.parseDate(startTimestampAsStr, new String[]{ "yyyy-MM-dd'T'HH:mm:ss'Z'" });
				} catch (ParseException e) {
					logger.error("can't parse START_TIMESTAMP '"+startTimestampAsStr+"'in mzDB file: return current date");
				}
				String paramTreeAsStr = r.columnString(RunTable.PARAM_TREE);
	
				return new Run(id, name, startTimestamp, ParamTreeParser.parseParamTree(paramTreeAsStr));
			}
		};
	}

	public Run getRun(int id) throws SQLiteException {
		return getRecord(Run.TABLE_NAME, id);
	}
	
	public List<Run> getRunList() throws SQLiteException {
		return getRecordList(Run.TABLE_NAME);
	}
	
}

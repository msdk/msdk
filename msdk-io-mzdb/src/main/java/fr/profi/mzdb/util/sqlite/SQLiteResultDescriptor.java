package fr.profi.mzdb.util.sqlite;

import java.util.HashMap;

public class SQLiteResultDescriptor {

	protected HashMap<String, Integer> colIdxByColName = new HashMap<String, Integer>();

	public SQLiteResultDescriptor(HashMap<String, Integer> colIdxByColName) {
		super();
		this.colIdxByColName = colIdxByColName;
	}

	public HashMap<String, Integer> getColIdxByColName() {
		return colIdxByColName;
	}

	public int getColumnIndex(String colName) {
		return this.colIdxByColName.get(colName);
	}

	public String[] getColumnNames() {
		return this.colIdxByColName.keySet().toArray(new String[this.colIdxByColName.size()]);
	}

}

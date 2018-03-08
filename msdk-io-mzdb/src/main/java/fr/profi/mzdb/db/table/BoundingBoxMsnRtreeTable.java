package fr.profi.mzdb.db.table;

public enum BoundingBoxMsnRtreeTable {

    ID("id"), MIN_MS_LEVEL("min_ms_level"), MAX_MS_LEVEL("max_ms_level"), MIN_PARENT_MZ("min_parent_mz"), MAX_PARENT_MZ(
	    "max_parent_mz"), MIN_MZ("min_mz"), MAX_MZ("max_mz"), MIN_TIME("min_time"), MAX_TIME("max_time");

    public static String tableName = "bounding_box_msn_rtree";
    private final String columnName;

    private BoundingBoxMsnRtreeTable(String colName) {
	this.columnName = colName;
    }

    public String getValue() {
	return columnName;
    }

}

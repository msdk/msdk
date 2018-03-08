package fr.profi.mzdb.db.table;

public enum BoundingBoxRtreeTable {

    ID("id"), MIN_MZ("min_mz"), MAX_MZ("max_mz"), MIN_TIME("min_time"), MAX_TIME("max_time");

    public static String tableName = "bounding_box_rtree";
    private final String columnName;

    private BoundingBoxRtreeTable(String colName) {
	this.columnName = colName;
    }

    public String getValue() {
	return columnName;
    }

}

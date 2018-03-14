package fr.profi.mzdb.db.table;

public enum BoundingBoxTable {

  ID("id"),
  DATA("data"),
  RUN_SLICE_ID("run_slice_id"),
  FIRST_SPECTRUM_ID("first_spectrum_id"),
  LAST_SPECTRUM_ID("last_spectrum_id");
  
  public static String tableName = "bounding_box";
  private final String columnName;
  
  private BoundingBoxTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

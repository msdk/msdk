package fr.profi.mzdb.db.table;

public enum SourceFileScanSettingsMapTable {

  SCAN_SETTINGS_ID("scan_settings_id"),
  SOURCE_FILE_ID("source_file_id");
  
  public static String tableName = "source_file_scan_settings_map";
  private final String columnName;
  
  private SourceFileScanSettingsMapTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

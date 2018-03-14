package fr.profi.mzdb.db.table;

public enum ScanSettingsTable {

  ID("id"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id");
  
  public static String tableName = "scan_settings";
  private final String columnName;
  
  private ScanSettingsTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

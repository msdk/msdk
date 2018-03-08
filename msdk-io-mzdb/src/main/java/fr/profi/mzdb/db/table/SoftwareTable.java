package fr.profi.mzdb.db.table;

public enum SoftwareTable {

  ID("id"),
  NAME("name"),
  VERSION("version"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE("shared_param_tree_id");
  
  public static String tableName = "software";
  private final String columnName;
  
  private SoftwareTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

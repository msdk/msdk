package fr.profi.mzdb.db.table;

public enum SourceFileTable {

  ID("id"),
  NAME("name"),
  LOCATION("location"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id");
  
  public static String tableName = "source_file";
  private final String columnName;
  
  private SourceFileTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

package fr.profi.mzdb.db.table;

public enum SampleTable {

  ID("id"),
  NAME("name"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id");
  
  public static String tableName = "sample";
  private final String columnName;
  
  private SampleTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

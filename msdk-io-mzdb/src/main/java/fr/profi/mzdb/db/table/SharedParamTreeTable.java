package fr.profi.mzdb.db.table;

public enum SharedParamTreeTable {

  ID("id"),
  DATA("data"),
  SCHEMA_NAME("schema_name");
  
  public static String tableName = "shared_param_tree";
  private final String columnName;
  
  private SharedParamTreeTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

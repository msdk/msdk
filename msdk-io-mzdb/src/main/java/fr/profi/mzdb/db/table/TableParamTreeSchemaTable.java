package fr.profi.mzdb.db.table;

public enum TableParamTreeSchemaTable {

  TABLE_NAME("table_name"),
  SCHEMA_NAME("schema_name");
  
  public static String tableName = "table_param_tree_schema";
  private final String columnName;
  
  private TableParamTreeSchemaTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

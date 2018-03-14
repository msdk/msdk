package fr.profi.mzdb.db.table;

public enum ParamTreeSchemaTable {

  NAME("name"),
  TYPE("type"),
  SCHEMA("schema");
  
  public static String tableName = "param_tree_schema";
  private final String columnName;
  
  private ParamTreeSchemaTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

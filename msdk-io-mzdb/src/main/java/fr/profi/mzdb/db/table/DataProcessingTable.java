package fr.profi.mzdb.db.table;

public enum DataProcessingTable {

  ID("id"),
  NAME("name");
  
  public static String tableName = "data_processing";
  private final String columnName;
  
  private DataProcessingTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

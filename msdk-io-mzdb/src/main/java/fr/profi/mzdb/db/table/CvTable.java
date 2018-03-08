package fr.profi.mzdb.db.table;

public enum CvTable {

  ID("id"),
  FULL_NAME("full_name"),
  VERSION("version"),
  URI("uri");
  
  public static String tableName = "cv";
  private final String columnName;
  
  private CvTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

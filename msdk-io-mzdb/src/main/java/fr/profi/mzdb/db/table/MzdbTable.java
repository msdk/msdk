package fr.profi.mzdb.db.table;

public enum MzdbTable {

  VERSION("version"),
  CREATION_TIMESTAMP("creation_timestamp"),
  FILE_CONTENT("file_content"),
  CONTACTS("contact"),
  PARAM_TREE("param_tree");
  
  public static String tableName = "mzdb";
  private final String columnName;
  
  private MzdbTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

package fr.profi.mzdb.db.table;

public enum ControlledVocabularyTable {

  ID("id"),
  FULL_NAME("full_name"),
  VERSION("version"),
  URI("uri");
  
  public static String tableName = "controlled_vocabulary";
  private final String columnName;
  
  private ControlledVocabularyTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

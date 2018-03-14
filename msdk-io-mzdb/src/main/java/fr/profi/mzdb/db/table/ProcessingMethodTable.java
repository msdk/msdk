package fr.profi.mzdb.db.table;

public enum ProcessingMethodTable {

  ID("id"),
  NUMBER("number"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id"),
  DATA_PROCESSING_ID("data_processing_id"),
  SOFTWARE_ID("software_id");
  
  public static String tableName = "processing_method";
  private final String columnName;
  
  private ProcessingMethodTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

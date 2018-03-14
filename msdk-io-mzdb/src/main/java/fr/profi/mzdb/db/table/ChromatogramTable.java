package fr.profi.mzdb.db.table;

public enum ChromatogramTable {

  ID("id"),
  NAME("name"),
  ACTIVATION_TYPE("activation_type"),
  DATA_POINTS("data_points"),
  PARAM_TREE("param_tree"),
  PRECURSOR("precursor"),
  PRODUCT("product"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id"),
  RUN_ID("run_id"),
  DATA_PROCESSING_ID("data_processing_id"),
  DATA_ENCODING_ID("data_encoding_id");
  
  public static String tableName = "chromatogram";
  private final String columnName;
  
  private ChromatogramTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

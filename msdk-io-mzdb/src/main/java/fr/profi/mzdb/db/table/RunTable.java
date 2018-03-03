package fr.profi.mzdb.db.table;

public enum RunTable {

  ID("id"),
  NAME("name"),
  START_TIMESTAMP("start_timestamp"),
  PARAM_TREE("param_tree"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id"),
  SAMPLE_ID("sample_id"),
  DEFAULT_INSTRUMENT_CONFIG_ID("default_instrument_config_id"),
  DEFAULT_SOURCE_FILE_ID("default_source_file_id"),
  DEFAULT_SCAN_PROCESSING_ID("default_scan_processing_id"),
  DEFAULT_CHROM_PROCESSING_ID("default_chrom_processing_id");
  
  public static String tableName = "run";
  private final String columnName;
  
  private RunTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

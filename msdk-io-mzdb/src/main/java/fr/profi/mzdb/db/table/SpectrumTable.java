package fr.profi.mzdb.db.table;

public enum SpectrumTable {

  ID("id"),
  INITIAL_ID("initial_id"),
  TITLE("title"),
  CYCLE("cycle"),
  TIME("time"),
  MS_LEVEL("ms_level"),
  ACTIVATION_TYPE("activation_type"),
  TIC("tic"),
  BASE_PEAK_MZ("base_peak_mz"),
  BASE_PEAK_INTENSITY("base_peak_intensity"),
  MAIN_PRECURSOR_MZ("main_precursor_mz"),
  MAIN_PRECURSOR_CHARGE("main_precursor_charge"),
  DATA_POINTS_COUNT("data_points_count"),
  PARAM_TREE("param_tree"),
  SCAN_LIST("scan_list"),
  PRECURSOR_LIST("precursor_list"),
  PRODUCT_LIST("product_list"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id"),
  INSTRUMENT_CONFIGURATION_ID("instrument_configuration_id"),
  SOURCE_FILE_ID("source_file_id"),
  RUN_ID("run_id"),
  DATA_PROCESSING_ID("data_processing_id"),
  DATA_ENCODING_ID("data_encoding_id"),
  BB_FIRST_SPECTRUM_ID("bb_first_spectrum_id");
  
  public static String tableName = "spectrum";
  private final String columnName;
  
  private SpectrumTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

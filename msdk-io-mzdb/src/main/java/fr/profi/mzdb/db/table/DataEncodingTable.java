package fr.profi.mzdb.db.table;

public enum DataEncodingTable {

  ID("id"),
  MODE("mode"),
  COMPRESSION("compression"),
  BYTE_ORDER("byte_order"),
  MZ_PRECISION("mz_precision"),
  INTENSITY_PRECISION("intensity_precision"),
  PARAM_TREE("param_tree");
  
  public static String tableName = "data_encoding";
  private final String columnName;
  
  private DataEncodingTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

package fr.profi.mzdb.db.table;

public enum RunSliceTable {

  ID("id"),
  MS_LEVEL("ms_level"),
  NUMBER("number"),
  BEGIN_MZ("begin_mz"),
  END_MZ("end_mz"),
  PARAM_TREE("param_tree"),
  RUN_ID("run_id");
  
  public static String tableName = "run_slice";
  private final String columnName;
  
  private RunSliceTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

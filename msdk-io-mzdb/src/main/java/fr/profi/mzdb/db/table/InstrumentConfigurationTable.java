package fr.profi.mzdb.db.table;

public enum InstrumentConfigurationTable {

  ID("id"),
  NAME("name"),
  PARAM_TREE("param_tree"),
  COMPONENT_LIST("component_list"),
  SHARED_PARAM_TREE_ID("shared_param_tree_id"),
  SOFTWARE_ID("software_id");
  //INSTRUMENT_PARAM_TREE_ID("instrument_param_tree_id");
  
  public static String tableName = "instrument_configuration";
  private final String columnName;
  
  private InstrumentConfigurationTable(String colName) {
    this.columnName = colName;
  }
  
  public String getValue() {
    return columnName;
  }

}

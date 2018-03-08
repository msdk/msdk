package fr.profi.mzdb.db.model;

/**
 * @author Marco
 *
 */
public interface IMzDBParamNameGetter {
  
  public String getMs1BBTimeWidthParamName();
  public String getMsnBBTimeWidthParamName();
  public String getMs1BBMzWidthParamName();
  public String getMsnBBMzWidthParamName();
  public String getLossStateParamName();
}

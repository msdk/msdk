/**
 * 
 */
package fr.profi.mzdb.db.model;

/**
 * @author Marco
 *
 */
public class MzDBParamName_0_8 implements IMzDBParamNameGetter {

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMs1BBWidthParamName()
   */
  @Override
  public String getMs1BBTimeWidthParamName() {
    return "BB_width_ms1";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMsnBBWidthParamName()
   */
  @Override
  public String getMsnBBTimeWidthParamName() {
    return "BB_width_msn";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMs1BBHeightParamName()
   */
  @Override
  public String getMs1BBMzWidthParamName() {
    return "BB_height_ms1";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMsnBBHeightParamName()
   */
  @Override
  public String getMsnBBMzWidthParamName() {
    return "BB_height_msn";

  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getLossStateParamName()
   */
  @Override
  public String getLossStateParamName() {
    return "is_no_loss";

  }

  
}

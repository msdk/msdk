/**
 * 
 */
package fr.profi.mzdb.db.model;

/**
 * @author Marco
 *
 */
public class MzDBParamName_0_9 implements IMzDBParamNameGetter {

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMs1BBWidthParamName()
   */
  @Override
  public String getMs1BBTimeWidthParamName() {
    return "ms1_bb_time_width";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMsnBBWidthParamName()
   */
  @Override
  public String getMsnBBTimeWidthParamName() {
    return "msn_bb_time_width";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMs1BBHeightParamName()
   */
  @Override
  public String getMs1BBMzWidthParamName() {
    return "ms1_bb_mz_width";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getMsnBBHeightParamName()
   */
  @Override
  public String getMsnBBMzWidthParamName() {
    return "msn_bb_mz_width";
  }

  /* (non-Javadoc)
   * @see fr.profi.mzdb.db.model.IMzDBParamNameGetter#getLossStateParamName()
   */
  @Override
  public String getLossStateParamName() {
    return "is_lossless";
  }

}

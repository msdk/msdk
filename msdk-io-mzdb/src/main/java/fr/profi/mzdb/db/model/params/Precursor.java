package fr.profi.mzdb.db.model.params;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.profi.mzdb.db.model.params.param.CVEntry;

@XmlRootElement(name = "precursor")
public class Precursor {
  
  @XmlAttribute(required=true)
  protected String spectrumRef;
  
  @XmlElement(name="isolationWindow")
  protected IsolationWindowParamTree isolationWindow;
  
  @XmlElement(name="selectedIonList")
  protected SelectedIonList selectedIonList;
  
  @XmlElement(name="activation")
  protected Activation activation;
  
  public String getSpectrumRef() {
      return spectrumRef;
  }
  
  public IsolationWindowParamTree getIsolationWindow() {
      return isolationWindow;
  }
  
  public Activation getActivation() {
      return activation;
  }
  
  public SelectedIonList getSelectedIonList() {
      return selectedIonList;
  }
  
  public double parseFirstSelectedIonMz() {
	  
	  SelectedIonList sil = this.getSelectedIonList();
	  SelectedIon si = sil.getSelectedIons().get(0);
	  String precMzAsStr = si.getCVParam(CVEntry.SELECTED_ION_MZ).getValue();
	  
      return Double.parseDouble(precMzAsStr);
  }
  
}
 
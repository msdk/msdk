package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

public class SelectedIonList extends AbstractParamTree {

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected int count;

  @XmlElement( name="selectedIon" )
  protected List<SelectedIon> selectedIons;

  public SelectedIonList() {
  }
  
  public SelectedIonList(int c) {
    this.count = c;
  }
  
  public List<SelectedIon> getSelectedIons() {
    return selectedIons;
  }

}
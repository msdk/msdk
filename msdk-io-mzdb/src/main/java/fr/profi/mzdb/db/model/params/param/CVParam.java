package fr.profi.mzdb.db.model.params.param;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class CVParam {

	@XmlAttribute
	protected String cvRef;

	@XmlAttribute
	protected String accession;
	
	@XmlAttribute
	protected String name;
	
	@XmlAttribute
	protected String value;

	@XmlAttribute
	protected String unitCvRef;

	@XmlAttribute
	protected String unitAccession;

	@XmlAttribute
	protected String unitName;

	public String getCvRef() {
		return cvRef;
	}
	
	public String getAccession() {
		return accession;
	}

	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	public String getUnitCvRef() {
		return unitCvRef;
	}

	public String getUnitAccession() {
		return unitAccession;
	}

	public String getUnitName() {
		return unitName;
	}
	
	public void setCvRef(String cvRef) {
		this.cvRef = cvRef;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setUnitCvRef(String unitCvRef) {
		this.unitCvRef = unitCvRef;
	}

	public void setUnitAccession(String unitAccession) {
		this.unitAccession = unitAccession;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

}

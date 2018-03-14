package fr.profi.mzdb.db.model.params.param;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class UserParam {
	
	@XmlAttribute
	protected String cvRef;

	@XmlAttribute
	protected String accession;

	@XmlAttribute
	protected String name;

	@XmlAttribute
	protected String value;

	@XmlAttribute
	protected String type;// ="xsd:float"/>;
	
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

	public String getType() {
		return type;
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

	public void setType(String type) {
		this.type = type;
	}

}

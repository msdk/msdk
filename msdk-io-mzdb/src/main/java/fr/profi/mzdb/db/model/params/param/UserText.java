package fr.profi.mzdb.db.model.params.param;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Marco
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserText {
	
	@XmlAttribute
	protected String cvRef;
	
	@XmlAttribute
	protected String accession;
	
	@XmlAttribute
	protected String name;
	
	@XmlValue
	protected String text;
	
	@XmlAttribute
	protected String type;

	public String getCvRef() {
		return cvRef;
	}

	public String getAccession() {
		return accession;
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
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

	public void setText(String text) {
		this.text = text;
	}

	public void setType(String type) {
		this.type = type;
	}

}

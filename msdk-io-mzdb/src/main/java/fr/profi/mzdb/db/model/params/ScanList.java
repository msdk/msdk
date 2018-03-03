package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author David Bouyssie
 * 
 */
@XmlRootElement(name = "scanList")
public class ScanList extends AbstractParamTree {

	@XmlAttribute(required = true)
	@XmlSchemaType(name = "nonNegativeInteger")
	protected int count;

	@XmlElement( name="scan" )
	protected List<ScanParamTree> scans;

	public ScanList() {
	}
	
	public ScanList(int c) {
		this.count = c;
	}
	
	public List<ScanParamTree> getScans() {
		return scans;
	}

}

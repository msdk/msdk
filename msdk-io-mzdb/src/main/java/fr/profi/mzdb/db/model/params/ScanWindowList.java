package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author David Bouyssie
 *
 */
@XmlRootElement(name = "scanWindowList")
public class ScanWindowList extends AbstractParamTree {
	
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "nonNegativeInteger")
	protected int count;
	
	@XmlElementWrapper( name="scanWindow" )
	protected List<ScanWindow> scanWindows;
	
	public ScanWindowList() {
	}
	
	public ScanWindowList(int c) {
		this.count = c;
	}

}
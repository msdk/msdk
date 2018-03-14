package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * @author Marco
 * 
 */
public class ComponentList extends AbstractParamTree {

	@XmlElements({
		@XmlElement(name = "detector", required = true, type = DetectorComponent.class),
		@XmlElement(name = "analyzer", required = true, type = AnalyzerComponent.class),
		@XmlElement(name = "source", required = true, type = SourceComponent.class)
	})
	@XmlElementWrapper
	protected List<Component> components;

	@XmlAttribute(required = true)
	@XmlSchemaType(name = "nonNegativeInteger")
	protected int count;

	public ComponentList(int c) {
		this.count = c;
	}

	public ComponentList() {
	}

}

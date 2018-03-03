package fr.profi.mzdb.db.model.params;

import javax.xml.bind.annotation.XmlAttribute;

// TODO: Auto-generated Javadoc
/**
 * The Class Component.
 * 
 * @author David Bouyssie
 */
public class Component extends AbstractParamTree {

    /** The order. */
    @XmlAttribute
    protected int order;

    /**
     * Gets the order.
     * 
     * @return the order
     */
    public int getOrder() {
	return order;
    }

}

package fr.profi.mzdb.db.model;

import fr.profi.mzdb.db.model.params.ComponentList;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.util.misc.AbstractInMemoryIdGen;

// TODO: Auto-generated Javadoc
/**
 * The Class InstrumentConfiguration.
 * 
 * @author David Bouyssie
 */
public class InstrumentConfiguration extends AbstractInMemoryIdGen {
	
	public static final String TABLE_NAME = "instrument_configuration";

	/** The id. */
	protected int id;

	/** The name. */
	protected String name;

	/** The software id. */
	protected int softwareId;

	
	protected ParamTree paramTree;
	
	/** The param tree. */
	protected ComponentList componentList;
	

	/**
	 * Instantiates a new instrument configuration.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param softwareId
	 *            the software id
	 * @param paramTree
	 *            the param tree
	 */
	public InstrumentConfiguration(int id, String name, int softwareId, ParamTree paramTree, ComponentList comp) {
		super();
		this.id = id;
		this.name = name;
		this.softwareId = softwareId;
		this.paramTree = paramTree;
		this.componentList = comp;
	}

	/**
	 * Instantiates a new instrument configuration.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param softwareId
	 *            the software id
	 */
	public InstrumentConfiguration(int id, String name, int softwareId) {
		this(id, name, softwareId, null, null);
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the software id.
	 * 
	 * @return the software id
	 */
	public int getSoftwareId() {
		return softwareId;
	}

	public ParamTree getParamTree() {
	  return this.paramTree;
	}
	
	/**
	 * Gets the param tree.
	 * 
	 * @return the param tree
	 */
	public ComponentList getComponentList() {
		return componentList;
	}

	/**
	 * Sets the param tree.
	 * 
	 * @param paramTree
	 *            the new param tree
	 */
	/*public void setParamTree(InstrumentConfigParamTree paramTree) {
		this.paramTree = paramTree;
	}*/

}

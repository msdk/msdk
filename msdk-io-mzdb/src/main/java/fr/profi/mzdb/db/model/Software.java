package fr.profi.mzdb.db.model;

import fr.profi.mzdb.db.model.params.ParamTree;

// TODO: Auto-generated Javadoc
/**
 * The Class Software.
 * 
 * @author David Bouyssie
 */
public class Software extends AbstractTableModel {
	
	public static final String TABLE_NAME = "software";

	/** The name. */
	protected String name;

	/** The version. */
	protected String version;

	/**
	 * Instantiates a new software.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param version
	 *            the version
	 * @param paramTree
	 *            the param tree
	 */
	public Software(int id, String name, String version, ParamTree paramTree) {
		super(id, paramTree);
		this.name = name;
		this.version = version;
	}

	/**
	 * Instantiates a new software.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param version
	 *            the version
	 */
	public Software(int id, String name, String version) {
		this(id, name, version, null);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/*
	 * public void setName(String name) { this.name = name; }
	 */

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/*
	 * public void setVersion(String version) { this.version = version; }
	 */

}

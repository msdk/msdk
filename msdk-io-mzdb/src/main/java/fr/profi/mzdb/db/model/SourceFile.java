package fr.profi.mzdb.db.model;

import fr.profi.mzdb.db.model.params.ParamTree;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceFile.
 * 
 * @author David Bouyssie
 */
public class SourceFile extends AbstractTableModel {
	
	public static final String TABLE_NAME = "source_file";

	/** The name. */
	protected String name;

	/** The location. */
	protected String location;

	/**
	 * Instantiates a new source file.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param location
	 *            the location
	 * @param paramTree
	 *            the param tree
	 */
	public SourceFile(int id, String name, String location, ParamTree paramTree) {
		super(id, paramTree);
		this.name = name;
		this.location = location;
	}

	/**
	 * Instantiates a new source file.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param location
	 *            the location
	 */
	public SourceFile(int id, String name, String location) {
		this(id, name, location, null);
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
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

}

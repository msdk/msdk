package fr.profi.mzdb.db.model;

import fr.profi.mzdb.db.model.params.ParamTree;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceFile.
 * 
 * @author David Bouyssie
 */
public class Sample extends AbstractTableModel {
	
	public static final String TABLE_NAME = "sample";

	/** The name. */
	protected String name;

	/**
	 * Instantiates a new source file.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param paramTree
	 *            the param tree
	 */
	public Sample(int id, String name, ParamTree paramTree) {
		super(id, paramTree);
		this.name = name;
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
	public Sample(int id, String name) {
		this(id, name, null);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}

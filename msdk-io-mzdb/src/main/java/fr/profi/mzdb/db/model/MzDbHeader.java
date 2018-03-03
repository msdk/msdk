package fr.profi.mzdb.db.model;

import fr.profi.mzdb.db.model.params.ParamTree;

/**
 * The Class MzDbHeader.
 * 
 * @author David Bouyssie
 */
public class MzDbHeader extends AbstractTableModel {
	
	public static final String TABLE_NAME = "mzdb";

	/** The version. */
	protected String version;

	/** The creation timestamp. */
	protected int creationTimestamp;
	
	/**
	 * Instantiates a new mz db header.
	 * 
	 * @param version
	 *            the version
	 * @param creationTimestamp
	 *            the creation timestamp
	 * @param paramTree
	 *            the param tree
	 */
	public MzDbHeader(String version, int creationTimestamp, ParamTree paramTree) {
		super(1, paramTree);
		this.version = version;
		this.creationTimestamp = creationTimestamp;
	}

	/**
	 * Instantiates a new mz db header.
	 * 
	 * @param version
	 *            the version
	 * @param creationTimestamp
	 *            the creation timestamp
	 */
	public MzDbHeader(String version, int creationTimestamp) {
		this(version, creationTimestamp, null);
	}
	
	public String getVersion() {
	  return this.version;
	}

}

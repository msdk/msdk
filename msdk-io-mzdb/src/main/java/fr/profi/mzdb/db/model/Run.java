package fr.profi.mzdb.db.model;

import java.util.Date;

import fr.profi.mzdb.db.model.params.ParamTree;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceFile.
 * 
 * @author David Bouyssie
 */
public class Run extends AbstractTableModel {
	
	public static final String TABLE_NAME = "run";

	/** The name. */
	protected String name;

	/** The location. */
	//protected Instant startTimestamp;
	protected Date startTimestamp;

	/**
	 * Instantiates a new source file.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param startTimestamp
	 *            the startTimestamp
	 * @param paramTree
	 *            the param tree
	 */
	//public Run(int id, String name, Instant startTimestamp, ParamTree paramTree) {
	public Run(int id, String name, Date startTimestamp, ParamTree paramTree) {
		super(id, paramTree);
		this.name = name;
		this.startTimestamp = startTimestamp;
	}

	/**
	 * Instantiates a new source file.
	 * 
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param startTimestamp
	 *            the startTimestamp
	 */
	//public Run(int id, String name, Instant startTimestamp) {
	public Run(int id, String name, Date startTimestamp) {
		this(id, name, startTimestamp, null);
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
	 * @return the startTimestamp
	 */
	public Date getStartTimestamp() {
		return startTimestamp;
	}

}

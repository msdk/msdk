package fr.profi.mzdb.io.reader.cache;

import fr.profi.mzdb.AbstractMzDbReader;

/**
 * @author David Bouyssie
 *
 */
public abstract class MzDbEntityCacheContainer {

	/** The mzDB reader. */
//	private AbstractMzDbReader mzDbReader = null;
	private MzDbEntityCache entityCache = null;

	/**
	 * Instantiates a new abstract mz db reader helper.
	 *
	 * @param mzDbReader
	 *            the mz db reader
	 */
	public MzDbEntityCacheContainer(AbstractMzDbReader mzDbReader) {
		super();
//		this.mzDbReader = mzDbReader;
		this.entityCache = mzDbReader.getEntityCache();
	}

	/**
	 * @return the entityCache
	 */
	public MzDbEntityCache getEntityCache() {
		return entityCache;
	}

	/**
	 * @return reader
	 */
	public abstract AbstractMzDbReader getMzDbReader();

}
